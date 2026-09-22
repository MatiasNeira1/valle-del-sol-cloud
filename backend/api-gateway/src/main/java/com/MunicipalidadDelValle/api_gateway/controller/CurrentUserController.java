package com.MunicipalidadDelValle.api_gateway.controller;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class CurrentUserController {
    private static final List<String> UI_ROLES = List.of("ADMIN", "STAFF", "EMERGENCY_ENTITY", "USER");

    @GetMapping
    CurrentUserResponse currentUser(JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();
        List<String> tokenRoles = jwt.getClaimAsStringList("roles");
        List<String> roles = tokenRoles == null ? List.of() : tokenRoles.stream()
                .map(String::toUpperCase)
                .filter(UI_ROLES::contains)
                .distinct()
                .toList();
        return new CurrentUserResponse(
                firstNonBlank(jwt.getClaimAsString("oid"), jwt.getSubject()),
                firstNonBlank(jwt.getClaimAsString("name"), jwt.getClaimAsString("preferred_username")),
                firstNonBlank(jwt.getClaimAsString("email"), jwt.getClaimAsString("preferred_username")),
                roles);
    }

    private String firstNonBlank(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }

    record CurrentUserResponse(String id, String name, String email, List<String> roles) {}
}
