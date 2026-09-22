package com.MunicipalidadDelValle.ms_monitoreo_geo.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import feign.RequestInterceptor;
import feign.RequestTemplate;

class BearerTokenForwardingInterceptorTest {
    private final RequestInterceptor interceptor = new SecurityConfig().bearerTokenForwardingInterceptor();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void forwardsOnlyBearerTokenFromValidatedJwtAuthentication() {
        Instant now = Instant.now();
        Jwt jwt = new Jwt("validated-access-token", now, now.plusSeconds(300),
                Map.of("alg", "none"), Map.of("sub", "user-id"));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers().get(HttpHeaders.AUTHORIZATION))
                .containsExactly("Bearer validated-access-token");
    }

    @Test
    void doesNotForwardCredentialsFromNonJwtOrMissingAuthentication() {
        RequestTemplate missing = new RequestTemplate();
        interceptor.apply(missing);
        assertThat(missing.headers()).doesNotContainKey(HttpHeaders.AUTHORIZATION);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user", "unvalidated-secret"));
        RequestTemplate nonJwt = new RequestTemplate();
        interceptor.apply(nonJwt);
        assertThat(nonJwt.headers()).doesNotContainKey(HttpHeaders.AUTHORIZATION);
    }
}
