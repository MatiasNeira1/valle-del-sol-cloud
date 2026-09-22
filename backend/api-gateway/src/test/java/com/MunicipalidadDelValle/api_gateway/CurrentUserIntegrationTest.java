package com.MunicipalidadDelValle.api_gateway;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
class CurrentUserIntegrationTest {
    @Autowired ApplicationContext context;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity()).configureClient()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Test
    void adminProfileComesFromValidatedAccessToken() {
        assertProfile("admin-id", "Admin Demo", "admin@example.test", List.of("ADMIN"));
    }

    @Test
    void userProfileComesFromValidatedAccessToken() {
        assertProfile("user-id", "User Demo", "user@example.test", List.of("USER"));
    }

    @Test
    void profileWithoutRoleDoesNotInventUserRole() {
        client.mutateWith(mockJwt().jwt(jwt -> jwt
                        .subject("no-role-id")
                        .claim("oid", "no-role-id")
                        .claim("name", "No Role")
                        .claim("preferred_username", "norole@example.test"))
                        .authorities(new SimpleGrantedAuthority("SCOPE_api.access")))
                .get().uri("/api/me").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("no-role-id")
                .jsonPath("$.roles.length()").isEqualTo(0);
    }

    @Test
    void profileWithoutTokenIsRejected() {
        client.get().uri("/api/me").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void mePreflightAllowsConfiguredFrontendOrigin() {
        client.options().uri("/api/me")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173")
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    }

    private void assertProfile(String id, String name, String email, List<String> roles) {
        client.mutateWith(mockJwt().jwt(jwt -> jwt
                        .subject(id)
                        .claim("oid", id)
                        .claim("name", name)
                        .claim("preferred_username", email)
                        .claim("roles", roles))
                        .authorities(new SimpleGrantedAuthority("SCOPE_api.access")))
                .get().uri("/api/me").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.email").isEqualTo(email)
                .jsonPath("$.roles[0]").isEqualTo(roles.get(0));
    }
}
