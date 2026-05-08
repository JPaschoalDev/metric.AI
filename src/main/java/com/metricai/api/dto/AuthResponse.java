package com.metricai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// DTO - Resposta de Autenticação (Login ou cadastro bem sucessido)
// Quando o usuário faz login ou se cadastra com sucesso o beckend retorna essa class como JSON

// Fluxo:
// 1. Usuário faz login com sucesso
// 2. Backend gera um TOKEN JWT
// 3. Backend retorna este objeto como resposta
// 4. React salva o token no localStorage
// 5. Em TODAS as próximas requisições, React envia: Authorization: Bearer {token}

@Data
@AllArgsConstructor // Lombok cria um construtor com todos os campos
public class AuthResponse {

    // Token JWT gerado pelo beckend
    // Este token prova que o usuário está autenticado e tem validade de 24hrs (configurável)
    private String token;

    // Tipo "Bearer" é o padrão para enviar tokens em requisições HTTP
    private String tipo = "Bearer";

    // iD do usuário no banco de dados
    private Long id;

    // Nome do usuário, útil para mensagens de boas vinda e etc.
    private String nome;

    // Email do usuário
    private String email;

    // Contrutor personalizado (sem o "tipo" pois já tem valor padrão)
    public AuthResponse(String token, Long id, String nome, String email) {
        this.token = token;
        this.tipo = "Bearer";
        this.id = id;
        this.nome = nome;
        this.email = email;
    }
}