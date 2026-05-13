package com.metricai.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// DTO (Data Transfer Object) - Respostas do token do MELI
// Essa classe representa a resposta do MELI para a troca do OAuth por um token de acesso

@Data
public class MercadoLivreTokenResponse {

    // Token de acesso (válido por 6 horas)
    // Usado para fazer requisições à API do Mercado Livre
    @JsonProperty("access_token")
    private String accessToken;

    // Tipo do token (sempre "bearer")
    @JsonProperty("token_type")
    private String tokenType;

    // Tempo de expiração em segundos (21600 = 6 horas)
    @JsonProperty("expires_in")
    private Integer expiresIn;

    // Permissões concedidas
    @JsonProperty("scope")
    private String scope;

    // ID do usuário no Mercado Livre
    @JsonProperty("user_id")
    private Long userId;

    // Refresh Token (usado para renovar o access_token)
    // Válido por 6 meses
    // Quando o access_token expirar, usamos este para pegar um novo
    @JsonProperty("refresh_token")
    private String refreshToken;
}