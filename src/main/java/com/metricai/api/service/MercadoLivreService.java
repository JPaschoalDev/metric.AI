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

// SERVICE - INTEGRAÇÃO COM MERCADO LIVRE
// Esta classe gerencia toda a comunicação com a API do Mercado Livre

// Responsabilidades:
// - Gerar URL de autenticação OAuth2
// - Trocar código por token de acesso
// - Renovar tokens expirados
// - Salvar tokens no banco
// - Fazer requisições à API do Mercado Livre

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

    // GERAR URL DE AUTENTICAÇÃO
    // Retorna a URL que o usuário deve acessar para autorizar a aplicação

    // @return URL de autenticação do Mercado Livre
    public String gerarUrlAutenticacao() {
        return AUTH_URL
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;
    }

    // TROCAR CÓDIGO POR TOKEN
    // Após o usuário autorizar no Mercado Livre, recebemos um "code"
    // Este metodo troca esse código por um access_token

    // FLUXO:
    // 1. Monta os parâmetros da requisição
    // 2. Faz POST para https://api.mercadolibre.com/oauth/token
    // 3. Recebe access_token e refresh_token
    // 4. Retorna o objeto com os tokens

    // @param code - Código recebido do Mercado Livre
    // @return Objeto com access_token e refresh_token
    public MercadoLivreTokenResponse trocarCodigoPorToken(String code) {

        // 1: Preparar os parâmetros da requisição
        // Mercado Livre espera um formulário (application/x-www-form-urlencoded)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        // 2: Configurar headers da requisição
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 3: Criar a requisição HTTP
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        // 4: Fazer POST para API do Mercado Livre
        ResponseEntity<MercadoLivreTokenResponse> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                request,
                MercadoLivreTokenResponse.class
        );

        // 5: Retornar a resposta (tokens)
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