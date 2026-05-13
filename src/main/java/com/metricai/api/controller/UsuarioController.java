package com.metricai.api.controller;

import com.metricai.api.model.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// CONTROLLER DE USUÁRIOS
// Endpoints protegidos que requerem autenticação

// @RestController = Marca como controller REST
// @RequestMapping = Todas as rotas começam com /api/usuarios
// @CrossOrigin = Permite requisições do frontend
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    // ENDPOINT: GET /api/usuarios/me
    // Retorna os dados do usuário autenticado

    // ROTA PROTEGIDA: Precisa enviar token JWT!

    // @AuthenticationPrincipal = Injeta o usuário autenticado
    // Este usuário foi colocado no contexto pelo JwtAuthenticationFilter
    @GetMapping("/me")
    public ResponseEntity<?> obterUsuarioAutenticado(
            @AuthenticationPrincipal Usuario usuario
    ) {
        // Se o usuário for null, significa que não está autenticado
        if (usuario == null) {
            return ResponseEntity.status(401).body(
                    Map.of("erro", "Não autenticado")
            );
        }

        // Cria um Map com os dados do usuário
        // NÃO retornamos a senha!
        Map<String, Object> dadosUsuario = new HashMap<>();
        dadosUsuario.put("id", usuario.getId());
        dadosUsuario.put("nome", usuario.getNome());
        dadosUsuario.put("email", usuario.getEmail());
        dadosUsuario.put("criadoEm", usuario.getCriadoEm());

        // Retorna 200 OK com os dados
        return ResponseEntity.ok(dadosUsuario);
    }
}
