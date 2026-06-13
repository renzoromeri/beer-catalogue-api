package com.renzo.beercatalogue.security.infrastructure.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.renzo.beercatalogue.security.domain.Role;
import com.renzo.beercatalogue.security.infrastructure.config.JwtProperties;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(new JwtProperties(
            "test-secret-key-that-is-long-enough-for-hs256",
            60
    ));

    private final UserPrincipal user = new UserPrincipal(
            1L,
            "admin",
            "hashed-password",
            Role.ADMIN,
            true
    );

    @Test
    void shouldGenerateValidTokenAndExtractUsername() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtService.isValid(token, user)).isTrue();
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        String token = jwtService.generateToken(user);
        UserPrincipal otherUser = new UserPrincipal(
                2L,
                "other",
                "hashed-password",
                Role.MANUFACTURER,
                true
        );

        assertThat(jwtService.isValid(token, otherUser)).isFalse();
    }
}
