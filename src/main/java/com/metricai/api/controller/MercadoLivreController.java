package com.metricai.api.controller;

import com.metricai.api.dto.MercadoLivreTokenResponse;
import com.metricai.api.model.Usuario;
import com.metricai.api.service.MercadoLivreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// CONTROLLER - INTEGRAÇÃO COM MERCADO LIVRE
// Endpoints para autenticação OAuth2 com PKCE

@RestController
@RequestMapping("/api/mercadolivre")
@CrossOrigin(origins = "*")
public class MercadoLivreController {

    @Autowired
    private MercadoLivreService mercadoLivreService;

    // ENDPOINT 1: INICIAR AUTENTICAÇÃO COM PKCE
    // GET /api/mercadolivre/auth

    // ROTA PROTEGIDA: Precisa estar logado!

    // FLUXO:
    // 1. Usuário autenticado clica "Conectar Mercado Livre" no frontend
    // 2. Frontend chama: GET /api/mercadolivre/auth (com token JWT)
    // 3. Backend gera code_verifier e code_challenge (PKCE)
    // 4. Backend salva code_verifier vinculado ao usuário
    // 5. Backend retorna URL com code_challenge incluído
    // 6. Frontend redireciona usuário para essa URL
    // 7. Usuário faz login e autoriza no Mercado Livre
    // 8. Mercado Livre redireciona para /callback

    // @param usuario - Usuário autenticado (injetado pelo JWT)
    // @return URL de autenticação do Mercado Livre com PKCE
    @GetMapping("/auth")
    public ResponseEntity<?> iniciarAutenticacao(
            @AuthenticationPrincipal Usuario usuario
    ) {
        // Verifica se está autenticado
        if (usuario == null) {
            return ResponseEntity.status(401).body(
                    Map.of("erro", "Você precisa estar logado para conectar ao Mercado Livre")
            );
        }

        try {
            // Gera URL com PKCE vinculado ao ID do usuário
            String authUrl = mercadoLivreService.gerarUrlAutenticacao(usuario.getId());

            // Retorna a URL para o frontend redirecionar
            Map<String, String> response = new HashMap<>();
            response.put("authUrl", authUrl);
            response.put("mensagem", "Redirecione o usuário para esta URL");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("erro", "Erro ao gerar URL: " + e.getMessage())
            );
        }
    }

    // ENDPOINT 2: CALLBACK COM PKCE (Mercado Livre redireciona aqui)
    // GET /api/mercadolivre/callback?code=TG-123456789

    // ROTA PÚBLICA: Mercado Livre chama diretamente!

    // FLUXO:
    // 1. Usuário autoriza no Mercado Livre
    // 2. Mercado Livre redireciona: GET /callback?code=TG-123456789
    // 3. Backend recupera o code_verifier salvo para o usuário
    // 4. Backend troca code + code_verifier por token (PKCE!)
    // 5. Mercado Livre valida: SHA256(verifier) == challenge? Se sim, libera!
    // 6. Backend salva tokens no banco
    // 7. Retorna JSON com status (frontend consome depois)

    // @param code - Código de autorização enviado pelo Mercado Livre
    // @return JSON com status e token
    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {

        try {
            System.out.println("=== CALLBACK RECEBIDO ===");
            System.out.println("Código: " + code);

            // ID do usuário que iniciou o fluxo
            // ATENÇÃO: Por enquanto fixo em 1 (melhorar com state param futuramente)
            Long usuarioId = 1L;

            // 1: Trocar código por token USANDO o code_verifier (PKCE)
            MercadoLivreTokenResponse tokenResponse =
                    mercadoLivreService.trocarCodigoPorToken(code, usuarioId);

            System.out.println("Token recebido: " + tokenResponse.getAccessToken());

            // 2: Salvar tokens no banco
            mercadoLivreService.salvarTokens(usuarioId, tokenResponse);

            System.out.println("=== SUCESSO! TOKEN SALVO COM PKCE ===");

            // 3: Retornar JSON com sucesso
            // Quando tivermos frontend, trocaremos por RedirectView
            return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "mensagem", "Mercado Livre conectado com segurança PKCE!",
                    "token", tokenResponse.getAccessToken()
            ));

        } catch (Exception e) {
            System.out.println("=== ERRO NO CALLBACK: " + e.getMessage() + " ===");
            return ResponseEntity.status(400).body(Map.of(
                    "status", "erro",
                    "mensagem", e.getMessage()
            ));
        }
    }

    // ENDPOINT 3: VERIFICAR SE ESTÁ CONECTADO
    // GET /api/mercadolivre/status

    // ROTA PROTEGIDA: Verifica se o usuário tem token do Mercado Livre

    // @param usuario - Usuário autenticado
    // @return Status da conexão com Mercado Livre
    @GetMapping("/status")
    public ResponseEntity<?> verificarStatus(
            @AuthenticationPrincipal Usuario usuario
    ) {
        if (usuario == null) {
            return ResponseEntity.status(401).body(
                    Map.of("erro", "Não autenticado")
            );
        }

        // Verifica se tem token do Mercado Livre salvo no banco
        boolean conectado = usuario.getMercadoLivreToken() != null;

        Map<String, Object> response = new HashMap<>();
        response.put("conectado", conectado);

        if (conectado) {
            response.put("mensagem", "Conectado ao Mercado Livre");
        } else {
            response.put("mensagem", "Não conectado ao Mercado Livre");
        }

        return ResponseEntity.ok(response);
    }
}