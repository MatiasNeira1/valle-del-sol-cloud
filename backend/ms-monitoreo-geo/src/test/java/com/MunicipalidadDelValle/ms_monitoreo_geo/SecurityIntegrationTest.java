package com.MunicipalidadDelValle.ms_monitoreo_geo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.MunicipalidadDelValle.ms_monitoreo_geo.service.MapaService;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean MapaService mapaService;

    @Test
    void synchronizationWithoutTokenIsRejectedBeforeInternalCall() throws Exception {
        mvc.perform(get("/api/mapa/sincronizar")).andExpect(status().isUnauthorized());
    }

    @Test
    void synchronizationWithValidatedTokenIsAllowed() throws Exception {
        mvc.perform(get("/api/mapa/sincronizar")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_api.access"))))
                .andExpect(status().isOk());
    }
}
