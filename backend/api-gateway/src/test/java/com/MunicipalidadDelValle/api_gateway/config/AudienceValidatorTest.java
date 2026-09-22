package com.MunicipalidadDelValle.api_gateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class AudienceValidatorTest {
    private static final String API_CLIENT_ID = "11111111-1111-1111-1111-111111111111";

    @Test
    void acceptsApiClientIdAndRejectsAppIdUriForV2Audience() {
        AudienceValidator validator = new AudienceValidator(API_CLIENT_ID);

        assertThat(validator.validate(jwt(List.of(API_CLIENT_ID))).hasErrors()).isFalse();
        assertThat(validator.validate(jwt(List.of("api://" + API_CLIENT_ID))).hasErrors()).isTrue();
        assertThat(validator.validate(jwt(List.of("22222222-2222-2222-2222-222222222222"))).hasErrors()).isTrue();
    }

    @Test
    void rejectsAppIdUriConfigurationAtStartup() {
        assertThatThrownBy(() -> new AudienceValidator("api://" + API_CLIENT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("without api://");
    }

    private Jwt jwt(List<String> audience) {
        Instant now = Instant.now();
        return new Jwt("test-token", now, now.plusSeconds(300),
                java.util.Map.of("alg", "none"),
                java.util.Map.of("sub", "test-user", "aud", audience));
    }
}
