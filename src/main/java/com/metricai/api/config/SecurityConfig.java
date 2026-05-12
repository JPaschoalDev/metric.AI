package com.metricai.api.config;

// Import das ferramentas necessaárias para fomentar a segurança do projeto
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Criptografar senhas
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


// CONFIGURAÇÃO DE SEGURANÇA DO SPRING SECURITY
// Esta classe configura TODA a segurança da aplicação.

// @Configuration: Diz ao Spring que esta classe contém configurações
// @EnableWebSecurity: Ativa o Spring Security
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // BEAN DE CRIPTOGRAFIA DE SENHAS (ByCrypt)
    // Será injetado automaticamente quando precisar criptografar senahs.
    // ByCrypt é um HASH seguro para senhas
    // Funciona extraindo o "salt" do hash e replica na senha digitada, caso seja igual SENHA CORRETA
    // @return PasswordEncoder = objeto para criptografar/verificar senhas.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // BEAN DE CONFIGURAÇÃO DE ROTAS
    // Define quais URL´s são públicas e quais são privadas
    // "@param http" = Objeto de configuração do Spring Security
    // "@return SecurityFilterChain" = Cadeia de filtros de segurança
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF (Cross-Site Request Forgery)
                // APIs REST não precisam de CSRF porque usam tokens JWT
                .csrf(csrf -> csrf.disable())
                // Configuração de autorização de requisições
                .authorizeHttpRequests(auth -> auth
                        // TODAS as rotas estão públicas (sem autenticação)
                        // Necessário mudar após testes de segurançar e funcionalidade
                        .anyRequest().permitAll()
                )
                // STATELESS: Não guarda sessão no servidor
                // Cada requisição precisa enviar o token JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
        return http.build();
    }
}