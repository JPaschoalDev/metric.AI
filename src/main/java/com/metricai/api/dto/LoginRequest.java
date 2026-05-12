package com.metricai.api.dto;

import lombok.Data;

// DTO (Data Transfer Object) - Login Request
// Classe que transporta dados entre o frontend (REACT) e o backend (SPING BOOT)

// FLUXO:
// 1. RECEBE OS DADOS DO FRONTEND
// 2. LEVA ESSES DADOS PARA O "CONTROLLER"
// 3. FACILITA O ACESSO AOS DADOS VIA GETTERS

// Lombook responsável por gerars os getteres, setteres, toString, equals, hashCode
@Data
public class LoginRequest {
    // EMAIL DO USUÁRIO
    private String email;

    // SENHA DO USUÁRIO (SALVA TEMPORIARIAMENTE EM TEXTO PURO PARA CHECAR SE BATE COM A SENHA JÁ CRIPTOGRAFADA OU NÃO)
    private String senha;
}
