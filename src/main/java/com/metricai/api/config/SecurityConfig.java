package com.metricai.api.config;

// Import das ferramentas necessárias para fomentar a segurança do projeto
import com.metricai.api.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// CONFIGURAÇÃO DE SEGURANÇA DO SPRING SECURITY
// Esta classe configura TODA a segurança da aplicação.

// @Configuration: Diz ao Spring que esta classe contém configurações
// @EnableWebSecurity: Ativa o Spring Security
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // NOVA INJEÇÃO: FILTRO JWT
    // Este filtro intercepta TODAS as requisições HTTP
    // Valida o token JWT antes da requisição chegar no Controller
    // Foi criado em JwtAuthenticationFilter.java
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // BEAN DE CRIPTOGRAFIA DE SENHAS (BCrypt)
    // Será injetado automaticamente quando precisar criptografar senhas.
    // BCrypt é um HASH seguro para senhas
    // Funciona extraindo o "salt" do hash e replica na senha digitada, caso seja igual SENHA CORRETA
    // @return PasswordEncoder = objeto para criptografar/verificar senhas.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // BEAN DE CONFIGURAÇÃO DE ROTAS
    // Define quais URL's são públicas e quais são privadas
    // "@param http" = Objeto de configuração do Spring Security
    // "@return SecurityFilterChain" = Cadeia de filtros de segurança
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF (Cross-Site Request Forgery)
                // APIs REST não precisam de CSRF porque usam tokens JWT
                .csrf(csrf -> csrf.disable())

                // CONFIGURAÇÃO DE AUTORIZAÇÃO DE REQUISIÇÕES
                .authorizeHttpRequests(auth -> auth
                        // ROTAS PÚBLICAS (qualquer um pode acessar SEM token)
                        // /api/auth/** = /api/auth/register, /api/auth/login, etc
                        .requestMatchers("/api/auth/**").permitAll()

                        // ROTA PÚBLICA: CALLBACK DO MERCADO LIVRE
                        // O Mercado Livre chama esta rota diretamente (sem token JWT)
                        .requestMatchers("/api/mercadolivre/callback").permitAll()

                        // TODAS AS OUTRAS ROTAS SÃO PROTEGIDAS!
                        .anyRequest().authenticated()
                )

                // STATELESS: Não guarda sessão no servidor
                // Cada requisição precisa enviar o token JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ADICIONA O FILTRO JWT
                // Este filtro é executado ANTES do filtro padrão de autenticação

                // Ele intercepta TODA requisição e:
                // 1. Extrai o token do header "Authorization: Bearer {token}"
                // 2. Valida o token usando JwtUtil
                // 3. Se válido: coloca o usuário no contexto de segurança
                // 4. Se inválido: requisição continua SEM autenticação

                // addFilterBefore = Adiciona ANTES de outro filtro
                // UsernamePasswordAuthenticationFilter = Filtro padrão do Spring Security
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}