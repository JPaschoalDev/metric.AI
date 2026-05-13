package com.metricai.api.security;

import com.metricai.api.model.Usuario;
import com.metricai.api.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.util.ArrayList;

// JWT AUTHENTICATION FILTER
// Este filtro intercepta TODAS as requisições HTTP e valida o token JWT

// FLUXO:
// 1. Requisição chega no backend
// 2. É executado ANTES de chegar no Controller
// 3. Extrai o token do header "Authorization"
// 4. Valida o token usando "JwtUtil"
// 5. Se válido: coloca o usuário no contexto de segurança
// 6. Se inválido: requisição continua SEM autenticação
// 7. Controller verifica se o usuário está autenticado

// OncePerRequestFilter = Executado UMA VEZ por requisição
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // @Lazy = Carrega o UsuarioService só quando precisar (lazy loading)
    // Isso quebra o ciclo de dependências circular
    @Autowired
    @Lazy
    private UsuarioService usuarioService;

    // METODO PRINCIPAL DO FILTRO
    // Este metodo é executado em TODA requisição HTTP
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1: EXTRAIR O TOKEN DO HEADER
        // O frontend envia: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        // Precisamos pegar apenas a parte "eyJhbGciOiJIUzI1NiJ9..."
        String authHeader = request.getHeader("Authorization");

        // Verifica se o header existe e começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Não tem token ou formato errado
            // Continua a requisição SEM autenticação
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai o token (remove "Bearer " do início)
        String token = authHeader.substring(7);

        try {
            // 2: VALIDAR O TOKEN
            if (jwtUtil.validarToken(token)) {

                // 3: EXTRAIR O USER ID DO TOKEN
                Long userId = jwtUtil.extrairUserId(token);

                // 4: BUSCAR O USUÁRIO NO BANCO
                Usuario usuario = usuarioService.buscarPorId(userId);

                if (usuario != null) {
                    // 5: COLOCAR O USUÁRIO NO CONTEXTO DE SEGURANÇA
                    // Isso diz ao Spring Security: "Este usuário está autenticado!"

                    // Cria um objeto de autenticação
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    usuario,           // Principal (usuário autenticado)
                                    null,              // Credentials (não precisamos da senha aqui)
                                    new ArrayList<>()  // Authorities (permissões - vamos deixar vazio por enquanto)
                            );

                    // Adiciona detalhes da requisição (IP, sessão, etc)
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // COLOCA NO CONTEXTO DE SEGURANÇA
                    // Agora o Spring Security sabe que este usuário está autenticado!
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (Exception e) {
            // Token inválido, expirado ou erro ao processar
            // Ignora e continua SEM autenticação
            System.out.println("Erro ao validar token: " + e.getMessage());
        }

        // 6: CONTINUA A REQUISIÇÃO
        // Passa para o próximo filtro ou para o Controller
        filterChain.doFilter(request, response);
    }
}