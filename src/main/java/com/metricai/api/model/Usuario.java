package com.metricai.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// ENTIDADE USUARIO - Representa um usuário do sistema Metric.AI

// Esta classe cria a tabela 'usuarios' no PostgreSQL.
// Cada usuário pode se cadastrar, fazer login e conectar contas de marketplaces.

// Anotações importantes:
// @Entity: Diz ao Spring que esta classe é uma entidade do banco de dados
// @Table: Define o nome da tabela (se não colocar, usa o nome da classe em minúsculo)
// @Data: Lombok gera automaticamente getters, setters, toString, equals, hashCode

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    // Cria o iD do usuário como chave primária
    // O PostgreSQL gera os iD automaticamente de forma incremental ("IDENTITY")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NOME DO USUÁRIO: Este campo é obrigatório, com tamanho máximo de 100 caracteres
    @Column(nullable = false, length = 100)
    private String nome;

    // EMAIL DO USUÁRIO: Este campo é obrigatório, unico e com tamanho máximo de 100 caracteres
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // SENHA CRIPTOGRAFADA: Senha não é armazenada com texto puro no banco, antes de salvar
    // no banco de dados o "Spring Security" criptografa a senha.
    @Column(nullable = false)
    private String senha;

    // TOKEN OAuth2
    // 1. Usuário clica em "Conectar Mercado Livre"
    // 2. Mercado Livre autoriza e retorna um token
    // 3. Salvamos esse token aqui
    // 4. Usamos ele para fazer requisições à API do Mercado Livre

    @Column(length = 500)
    private String mercadoLivreToken;

    /**
     * Token de REFRESH do Mercado Livre
     * <p>
     * Tokens de acesso expiram (geralmente em 6 horas).
     * O refresh token serve para pegar um novo token sem o usuário fazer login de novo.
     * <p>
     * Analogia:
     * - Access Token = ingresso de cinema (expira depois do filme)
     * - Refresh Token = carteirinha de sócio (renova o ingresso quando expira)
     */
    @Column(length = 500)
    private String mercadoLivreRefreshToken;

    /**
     * Data e hora em que o usuário foi criado
     * <p>
     * updatable = false: Depois que é definido, nunca muda
     * Útil para relatórios, analytics, etc.
     * <p>
     * Exemplo: "2026-05-07T18:30:00"
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /**
     * Método executado AUTOMATICAMENTE ANTES de salvar no banco pela primeira vez
     *
     * @PrePersist é um "hook" (gancho) do JPA
     * Quando você faz: usuarioRepository.save(usuario)
     * Antes de inserir no banco, o JPA executa este método
     * <p>
     * Resultado: criadoEm é sempre preenchido automaticamente!
     */
    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }
}