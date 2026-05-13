package com.metricai.api.controller;

import com.metricai.api.dto.MercadoLivreTokenResponse;
import com.metricai.api.model.Usuario;
import com.metricai.api.service.MercadoLivreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

// CONTROLLER - INTEGRAÇÃO COM MERCADO LIVRE
// Endpoints para autenticação OAuth2 com Mercado Livre

@RestController
@RequestMapping("/api/mercadolivre")
@CrossOrigin(origins = "*")
public class MercadoLivreController {

    @Autowired
    private MercadoLivreService mercadoLivreService;

    // ENDPOINT 1: INICIAR AUTENTICAÇÃO
    // GET /api/mercadolivre/auth

    // ROTA PROTEGIDA: Precisa estar logado!

    // FLUXO:
    // 1. Usuário autenticado clica "Conectar Mercado Livre" no frontend
    // 2. Frontend chama: GET /api/mercadolivre/auth
    // 3. Backend gera URL do Mercado Livre
    // 4. Frontend redireciona usuário para essa URL
    // 5. Usuário faz login e autoriza no Mercado Livre
    // 6. Mercado Livre redireciona para /callback

    // @param usuario - Usuário autenticado (injetado pelo JWT)
    // @return URL de autenticação do Mercado Livre
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

        // Gera a URL de autenticação
        String authUrl = mercadoLivreService.gerarUrlAutenticacao();

        // Retorna a URL para o frontend redirecionar
        Map<String, String> response = new HashMap<>();
        response.put("authUrl", authUrl);
        response.put("mensagem", "Redirecione o usuário para esta URL");

        return ResponseEntity.ok(response);
    }

    // ENDPOINT 2: CALLBACK (Mercado Livre redireciona aqui)
    // GET /api/mercadolivre/callback?code=TG-123456789

    // ROTA PÚBLICA: Mercado Livre chama diretamente!

    // FLUXO:
    // 1. Usuário autoriza no Mercado Livre
    // 2. Mercado Livre redireciona: GET /callback?code=TG-123456789
    // 3. Backend troca o código por token
    // 4. Backend salva tokens no banco
    // 5. Backend redireciona usuário para o frontend

    // @param code - Código de autorização enviado pelo Mercado Livre
    // @return Redireciona para o frontend
    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        try {
            System.out.println("=== CALLBACK RECEBIDO ===");
            System.out.println("Código: " + code);

            MercadoLivreTokenResponse tokenResponse =
                    mercadoLivreService.trocarCodigoPorToken(code);

            System.out.println("Token: " + tokenResponse.getAccessToken());

            mercadoLivreService.salvarTokens(1L, tokenResponse);

            System.out.println("=== SUCESSO! ===");

            // Retorna JSON em vez de redirect
            // Assim não precisamos do frontend!
            return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "mensagem", "Mercado Livre conectado!",
                    "token", tokenResponse.getAccessToken()
            ));

        } catch (Exception e) {
            System.out.println("=== ERRO: " + e.getMessage() + " ===");
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
    // @return Status da conexão
    @GetMapping("/status")
    public ResponseEntity<?> verificarStatus(
            @AuthenticationPrincipal Usuario usuario
    ) {
        if (usuario == null) {
            return ResponseEntity.status(401).body(
                    Map.of("erro", "Não autenticado")
            );
        }

        // Verifica se tem token do Mercado Livre
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