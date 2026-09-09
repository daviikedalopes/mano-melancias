package com.manomelancias.api.usuario.dto;

import lombok.Getter;

@Getter
public class LoginResponse {

    private final String token;
    private final String tipo = "Bearer";

    public LoginResponse(String token) {
        this.token = token;
    }
}
