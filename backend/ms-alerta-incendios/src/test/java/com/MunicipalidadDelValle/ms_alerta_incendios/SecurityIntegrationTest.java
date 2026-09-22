package com.MunicipalidadDelValle.ms_alerta_incendios;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void endpointWithoutTokenReturns401() throws Exception {
        mvc.perform(get("/api/reportes")).andExpect(status().isUnauthorized());
    }

    @Test
    void malformedTokenReturns401() throws Exception {
        mvc.perform(get("/api/reportes").header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenWithScopeCanRead() throws Exception {
        mvc.perform(get("/api/reportes").with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_api.access"))))
                .andExpect(status().isOk());
    }

    @Test
    void validTokenWithoutRequiredRoleReturns403() throws Exception {
        mvc.perform(post("/api/reportes/crear")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_api.access")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Prueba\",\"latitud\":-33.4,\"longitud\":-70.6}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userRoleCanCreateReport() throws Exception {
        mvc.perform(post("/api/reportes/crear")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_api.access"),
                                new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Prueba autorizada\",\"latitud\":-33.4,\"longitud\":-70.6}"))
                .andExpect(status().isCreated());
    }

    @Test
    void allowedOriginReceivesCorsHeaders() throws Exception {
        mvc.perform(options("/api/reportes")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }
}
