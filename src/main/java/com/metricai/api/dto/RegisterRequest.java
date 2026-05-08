package com.metricai.api.dto;

import lombok.Data;

// DTO (Data Transfer Object) - Requisição de Cadastro
// Essa classe representa os dados que o FRONTEND envia quando o usuário se cadastra no sistema

// FLUXO:
// 1. Usuário preenche formulário no React (nome, email, senha)
// 2. React envia JSON para o Backend: POST /api/auth/register
// 3. Spring converte o JSON automaticamente para esta classe
// 4. Backend valida e salva no banco

@Data // Lombok gera getters e setters automaticamente
public class RegisterRequest {

    // NOME COMPLETO DO USUÁRIO
    private String nome;

    // EMAIL DO USUÁRIO (SERÁ USADO COMO LOGIN)
    private String email;

    // SENHA EM TEXTO PURO, o backend vai criptografar antes de salvar no banco.
    private String senha;
}