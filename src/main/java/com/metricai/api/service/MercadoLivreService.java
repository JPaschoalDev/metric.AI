package com.metricai.api.service;

import com.metricai.api.dto.MercadoLivreTokenResponse;
import com.metricai.api.model.Usuario;
import com.metricai.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

// SERVICE - INTEGRAÇÃO COM MERCADO LIVRE
// Esta classe gerencia toda a comunicação com a API do Mercado Livre

// Responsabilidades:
// - Gerar URL de autenticação OAuth2 com PKCE
// - Trocar código por token de acesso com PKCE
// - Renovar tokens expirados
// - Salvar tokens no banco
// - Fazer requisições à API do Mercado Livre

// O QUE É PKCE? (Proof Key for Code Exchange)
// Camada extra de segurança no fluxo OAuth2
// Impede que alguém que intercepte o "code" consiga trocar por token
// Funciona assim:
// 1. Geramos um "segredo" aleatório → code_verifier
// 2. Criamos um HASH desse segredo → code_challenge = SHA256(code_verifier)
// 3. Enviamos o HASH para o Mercado Livre na URL de autorização
// 4. Quando trocamos o code por token, enviamos o SEGREDO ORIGINAL
// 5. Mercado Livre verifica: SHA256(verifier) == challenge? Se sim, libera!

@Service
public class MercadoLivreService {

    // Injeção das credenciais do Mercado Livre
    // Valores vêm do arquivo application-secrets.properties
    @Value("${mercadolivre.client.id}")
    private String clientId;

    @Value("${mercadolivre.client.secret}")
    private String clientSecret;

    @Value("${mercadolivre.redirect.uri}")
    private String redirectUri;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // RestTemplate = Cliente HTTP do Spring
    // Usado para fazer requisições HTTP (GET, POST, etc)
    private final RestTemplate restTemplate = new RestTemplate();

    // URLs da API do Mercado Livre
    private static final String AUTH_URL = "https://auth.mercadolivre.com.br/authorization";
    private static final String TOKEN_URL = "https://api.mercadolibre.com/oauth/token";

    // ARMAZENA OS CODE_VERIFIERS TEMPORARIAMENTE
    // Chave: usuarioId | Valor: code_verifier gerado para aquele usuário
    // Necessário pois o code_verifier é gerado no /auth e usado no /callback
    // ConcurrentHashMap = thread-safe (seguro para múltiplos usuários simultâneos)
    private final ConcurrentHashMap<Long, String> codeVerifiers = new ConcurrentHashMap<>();

    // GERAR CODE_VERIFIER
    // String aleatória de 32 bytes codificada em Base64 URL-safe
    // É o "segredo" que só o nosso backend conhece
    // @return String aleatória segura
    private String gerarCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(codeVerifier);
    }

    // GERAR CODE_CHALLENGE
    // Hash SHA-256 do code_verifier, codificado em Base64 URL-safe
    // Este HASH é enviado para o Mercado Livre na URL de autorização
    // O Mercado Livre guarda este HASH para verificar depois
    // @param codeVerifier - O segredo gerado anteriormente
    // @return Hash SHA-256 do code_verifier
    private String gerarCodeChallenge(String codeVerifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(
                codeVerifier.getBytes(StandardCharsets.UTF_8)
        );
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(hash);
    }

    // GERAR URL DE AUTENTICAÇÃO COM PKCE (Proof Key for Code Exchange)
    // Camada extra de segurança no fluxo OAuth2
    // Impede que alguém que intercepte o "code" consiga trocar por token
    
    // Agora inclui code_challenge e code_challenge_method na URL
    // Salva o code_verifier vinculado ao usuário para usar no callback

    // @param usuarioId - ID do usuário que está iniciando o fluxo OAuth
    // @return URL de autenticação do Mercado Livre com PKCE
    public String gerarUrlAutenticacao(Long usuarioId) throws Exception {

        // 1: Gerar o par code_verifier / code_challenge
        String codeVerifier = gerarCodeVerifier();
        String codeChallenge = gerarCodeChallenge(codeVerifier);

        // 2: Salvar o code_verifier para recuperar no callback
        // Vinculado ao ID do usuário para garantir que o mesmo usuário fez o fluxo
        codeVerifiers.put(usuarioId, codeVerifier);

        System.out.println("=== PKCE GERADO PARA USUÁRIO " + usuarioId + " ===");
        System.out.println("code_verifier: " + codeVerifier);
        System.out.println("code_challenge: " + codeChallenge);

        // 3: Retornar URL com PKCE incluído
        // code_challenge_method=S256 = SHA-256 (padrão recomendado)
        return AUTH_URL
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256";
    }

    // TROCAR CÓDIGO POR TOKEN COM PKCE
    // Após o usuário autorizar no Mercado Livre, recebemos um "code"
    // Este metodo troca esse código por um access_token USANDO o code_verifier

    // FLUXO:
    // 1. Recupera o code_verifier salvo para este usuário
    // 2. Monta os parâmetros da requisição (incluindo code_verifier)
    // 3. Faz POST para https://api.mercadolibre.com/oauth/token
    // 4. Mercado Livre verifica: SHA256(verifier) == challenge? Se sim, libera!
    // 5. Retorna o objeto com os tokens
    // 6. Remove o code_verifier do mapa (não é mais necessário)

    // @param code - Código recebido do Mercado Livre
    // @param usuarioId - ID do usuário para recuperar o code_verifier correto
    // @return Objeto com access_token e refresh_token
    public MercadoLivreTokenResponse trocarCodigoPorToken(String code, Long usuarioId) {

        // 1: Recuperar o code_verifier salvo para este usuário
        String codeVerifier = codeVerifiers.get(usuarioId);

        if (codeVerifier == null) {
            throw new RuntimeException(
                    "code_verifier não encontrado! Inicie o fluxo OAuth novamente."
            );
        }

        System.out.println("=== TROCANDO CÓDIGO POR TOKEN COM PKCE ===");
        System.out.println("code_verifier recuperado para usuário " + usuarioId);

        // 2: Preparar os parâmetros da requisição
        // Mercado Livre espera um formulário (application/x-www-form-urlencoded)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("code_verifier", codeVerifier); // PKCE: prova que somos nós!

        // 3: Configurar headers da requisição
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 4: Criar a requisição HTTP
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        // 5: Fazer POST para API do Mercado Livre
        ResponseEntity<MercadoLivreTokenResponse> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                request,
                MercadoLivreTokenResponse.class
        );

        // 6: Remover o code_verifier após usar (segurança: não deixar guardado)
        codeVerifiers.remove(usuarioId);

        // 7: Retornar a resposta (tokens)
        return response.getBody();
    }

    // SALVAR TOKENS NO BANCO
    // Após receber os tokens do MELI, salvamos eles no banco, vinculado ao usuário

    // @param usuarioId - ID do usuário no nosso banco
    // @param tokenResponse - Objeto com access_token e refresh_token
    public void salvarTokens(Long usuarioId, MercadoLivreTokenResponse tokenResponse) {

        // 1: Buscar o usuário no banco
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 2: Atualizar os tokens
        usuario.setMercadoLivreToken(tokenResponse.getAccessToken());
        usuario.setMercadoLivreRefreshToken(tokenResponse.getRefreshToken());

        // 3: Salvar no banco
        usuarioRepository.save(usuario);
    }
}