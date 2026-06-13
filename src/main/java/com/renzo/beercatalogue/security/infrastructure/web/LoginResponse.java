package com.renzo.beercatalogue.security.infrastructure.web;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}
