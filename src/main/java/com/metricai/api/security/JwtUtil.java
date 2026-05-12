package com.metricai.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// JWT UTIL - GERADOR E VALIDADOR DE TOKENS
// Classe responsável por gerar e validar tokens JWT (JSON Web Token)
// JWT é um padrão de token usado em APIs REST para autenticação STATELESS
// "STATELESS" significa que o servidor NÃO guarda sessões em memória

// FLUXO:
// 1. Frontend envia email e senha
// 2. Backend valida as credenciais
// 3. Backend gera um token JWT
// 4. Backend retorna o token para o frontend
// 5. Frontend salva o token no "localStorage"

// A estrutura de um JWT tem 3 partes separadas por pontos (.)
// eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImVtYWlsIjoiam9hbyJ9.assinatura
// └────────┬─────────┘ └──────────────┬──────────────┘ └────┬────┘
//       HEADER                     PAYLOAD              SIGNATURE

// @Component = Spring cria uma instância única dessa classe
@Component
public class JwtUtil {

    // CHAVE SECRETA (SECRET KEY)
    // Essa chave é usada para assinar o token JWT

    // Atenção para pontos de segurança crítica:
    // 1. NUNCA versione esta chave no Git
    // 2. NUNCA exponha esta chave publicamente
    // 3. Deve ter no mínimo 256 bits (32 caracteres)
    // 4. Em produção, use variáveis de ambiente

    // @Value = Lê o valor do arquivo "application-secrets.properties"
    // Se não encontrar, usa o valor padrão
    @Value("${jwt.secret:minha-chave-secreta-super-segura-com-mais-de-256-bits-para-garantir-seguranca-maxima}")
    private String secret;

    // TEMPO DE EXPIRAÇÃO DO TOKEN
    // 24 horas = 86.400.000 milissegundos
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    // GERAR TOKEN JWT
    // Gera um token JWT que contém o ID e o e-mail do usuário

    // FLUXO:
    // 1. Cria um Map com os dados (claims) que vão no token
    // 2. Define o "subject" (assunto) como o ID do usuário
    // 3. Define a data de criação (agora)
    // 4. Define a data de expiração (agora + 24 horas)
    // 5. Assina o token com a chave secreta usando HS256
    // 6. Retorna o token como String
    public String gerarToken(Long userId, String email) {

        // 1: Criar os CLAIMS (dados dentro do token JWT)
        // Claims = Declarações/Afirmações sobre o usuário
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);    // Adiciona o ID
        claims.put("email", email);      // Adiciona o email

        // 2: Construir o token
        return Jwts.builder()
                .claims(claims)

                // Define o SUBJECT, que normalmente é o identificador único (ID)
                .subject(userId.toString())

                // Define quando o token foi CRIADO (agora)
                .issuedAt(new Date())

                // Define quando o token EXPIRA (agora + 24 horas)
                // Após esta data, o token é considerado inválido
                .expiration(new Date(System.currentTimeMillis() + expiration))

                // ASSINA o token com a chave secreta
                // O algoritmo é detectado automaticamente pela chave
                .signWith(getSigningKey())

                // Converte tudo em String (o token final)
                .compact();
    }

    // VALIDAR TOKEN JWT
    // Verifica se um token é válido
    // FLUXO:
    // 1. Token tem formato correto (3 partes separadas por ponto)
    // 2. Assinatura é válida (não foi alterado)
    // 3. Token não expirou
    // 4. Caso alguma das validações falhar, retorna false

    // @param token - Token JWT a ser validado
    // @return true se válido, false se inválido
    public boolean validarToken(String token) {
        try {
            // Executa a análise do token
            // Se conseguir, o token é válido
            // Se falhar, lança exceção e cai no catch

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;  // Token válido

        } catch (Exception e) {
            // Token inválido por algum motivo:
            // - Assinatura incorreta (token foi alterado)
            // - Token expirado
            // - Formato inválido
            return false;
        }
    }

    // EXTRAIR USER ID DO TOKEN
    // Extrai o ID do usuário que está dentro do token
    // Útil para saber QUEM é o usuário logado sem consultar o banco

    // @param token = token JWT
    // @return ID do usuário
    public Long extrairUserId(String token) {
        // Extrai todos os claims (dados) do token
        Claims claims = extrairClaims(token);

        // Pega o claim "userId" e converte para "Long"
        // "get()" retorna Object, então fazemos casting para "Integer" primeiro, depois converte para "Long"
        return ((Integer) claims.get("userId")).longValue();
    }

    // EXTRAI O EMAIL DO TOKEN
    // Extrai o e-mail do usuário que está dentro do token

    // @param token - Token JWT
    // @return Email do usuário
    public String extrairEmail(String token) {
        Claims claims = extrairClaims(token);
        return (String) claims.get("email");
    }

    // EXTRAI TODOS OS CLAIMS DO TOKEN
    // Claims = Dados que estão dentro do token (payload)
    // Este metodo interno extrai todos os dados internos do token JWT

    // @param token - Token JWT
    // @return Claims (objeto com todos os dados)
    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // OBTER CHAVE DE ASSINATURA
    // Converte a string da chave secreta em um objeto "SecretKey"
    // HMAC-SHA256 requer uma chave de no mínimo 256 bits (32 bytes)
    private SecretKey getSigningKey() {
        // Converte a string em bytes UTF-8
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // Cria uma SecretKey para HMAC-SHA256
        return Keys.hmacShaKeyFor(keyBytes);
    }
}