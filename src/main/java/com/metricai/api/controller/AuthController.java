package com.metricai.api.controller;

import com.metricai.api.dto.AuthResponse;
import com.metricai.api.dto.LoginRequest;
import com.metricai.api.dto.RegisterRequest;
import com.metricai.api.model.Usuario;
import com.metricai.api.security.JwtUtil;
import com.metricai.api.service.UsuarioService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// CONTROLLER DE AUTENTICAÇÃO
// Recebe requisições HTTP do frontend (React)
// Chama o Service para executar a lógica
// Retorna uma resposta HTTP

// Este "controller" expõe os ENDPOINTS (rotas HTTP) de autenticação:
// - POST /api/auth/register -> Cadastrar novo usuário
// - POST /api/auth/login -> Fazer login

// @RestController = Marca esta classe como um controller REST
// @RequestMapping = Define o prefixo de todas as rotas desta classe
// @CrossOrigin = Permite requisições do frontend
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permite requisições de qualquer origem (React em localhost:3000)
public class AuthController {

    // INJEÇÃO DE DEPENDÊNCIAS
    // O Spring injeta automaticamente esses objetos
    // Não precisamos fazer "new UsuarioService()" ou "new JwtUtil()"
    @Autowired
    private UsuarioService usuarioService;

    // NOVA DEPENDÊNCIA: JwtUtil
    // Agora temos acesso à classe que GERA e VALIDA tokens JWT reais
    // Antes usávamos "fake-jwt-token-temporario"
    // Agora usamos tokens JWT de verdade!
    @Autowired
    private JwtUtil jwtUtil;

    // ENDPOINT DE CADASTRO
    // ROTA: POST http://localhost:8080/api/auth/register
    // FLUXO ENDPOINT:
    // 1. Recebe dados de cadastro do frontend (nome, email, senha)
    // 2. Chama o Service para validar e salvar
    // 3. Gera um TOKEN JWT REAL com os dados do usuário
    // 4. Se sucesso: retorna 201 Created + dados do usuário + token JWT
    // 5. Se erro: retorna 400 Bad Request + mensagem de erro

    // @PostMapping = Define que é uma requisição POST
    // @RequestBody = Diz ao Spring para converter o JSON em RegisterRequest
    @PostMapping("/register")
    public ResponseEntity<?> cadastrar(@RequestBody RegisterRequest request) {
        // "ResponseEntity<?>"
        // "<?>" = Pode retornar qualquer tipo
        // Permite controlar o STATUS HTTP (200,201,400, etc)
        // Permite adicionar headers customizados
        // Flexibilidade ao retornar o objeto

        try {
            // PASSO 1: Chamar o Service para cadastrar
            Usuario usuarioSalvo = usuarioService.cadastrar(request);

            // PASSO 2: GERAR TOKEN JWT REAL
            // Antes: "fake-jwt-token-temporario"
            // Agora: Token JWT de verdade gerado pela classe JwtUtil!
            //
            // O token contém:
            // - ID do usuário
            // - Email do usuário
            // - Data de criação
            // - Data de expiração (24 horas)
            // - Assinatura criptográfica (impossível falsificar)
            //
            // Exemplo de token gerado:
            // "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImVtYWlsIjoiam9hb0BtZXRyaWNhaS5jb20ifQ.xyz123"
            String tokenJwt = jwtUtil.gerarToken(
                    usuarioSalvo.getId(),      // ID do usuário salvo no banco
                    usuarioSalvo.getEmail()    // Email do usuário
            );

            // PASSO 3: Criar a resposta com os dados do usuário + TOKEN JWT REAL
            AuthResponse response = new AuthResponse(
                    tokenJwt,                  // ← TOKEN JWT REAL (não é mais fake!)
                    usuarioSalvo.getId(),
                    usuarioSalvo.getNome(),
                    usuarioSalvo.getEmail()
            );

            // PASSO 4: Retornar 201 Created + JSON
            // HttpStatus.CREATED = 201 (padrão para recursos criados)
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Se o Service lançar erro (ex: email duplicado), cai aqui

            // Retorna 400 Bad Request + mensagem de erro em JSON
            return ResponseEntity
                    .badRequest()
                    .body(new ErroResponse(e.getMessage()));
        }
    }

    // ENDPOINT DE LOGIN
    // ROTA: POST http://localhost:8080/api/auth/login
    // FLUXO:
    // 1. Recebe email e senha do frontend
    // 2. Chama o Service para validar as credenciais
    // 3. Se válido: gera TOKEN JWT REAL e retorna 200 OK
    // 4. Se inválido: retorna 401 Unauthorized + mensagem de erro
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // PASSO 1: Chamar o Service para autenticar
            Usuario usuarioAutenticado = usuarioService.autenticar(
                    request.getEmail(),
                    request.getSenha()
            );

            // PASSO 2: GERAR TOKEN JWT REAL
            // Agora que o usuário foi autenticado com sucesso,
            // geramos um token JWT que o frontend vai usar
            // em TODAS as próximas requisições
            //
            // O frontend vai guardar esse token no localStorage:
            // localStorage.setItem('token', tokenJwt)
            //
            // E enviar em toda requisição:
            // Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
            String tokenJwt = jwtUtil.gerarToken(
                    usuarioAutenticado.getId(),
                    usuarioAutenticado.getEmail()
            );

            // PASSO 3: Criar a resposta com TOKEN JWT REAL
            AuthResponse response = new AuthResponse(
                    tokenJwt,                        // ← TOKEN JWT REAL!
                    usuarioAutenticado.getId(),
                    usuarioAutenticado.getNome(),
                    usuarioAutenticado.getEmail()
            );

            // PASSO 4: Retornar 200 OK + JSON
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Se email não existir ou senha estiver errada, cai aqui

            // Retorna 401 (Credenciais inválidas) Unauthorized + mensagem de erro
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErroResponse(e.getMessage()));
        }
    }

    // CLASSE INTERNA PARA RESPOSTA DE ERRO
    // Essa classe retorna erros em formato JSON padronizado
    // Ajuda na padronização da formatação de erros
    // Frontend sempre sabe onde buscar a mensagem de erro
    // Facilita o tratamento de erros no React
    @Data
    @AllArgsConstructor
    public static class ErroResponse {
        private String erro;
    }
}