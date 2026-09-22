package com.MunicipalidadDelValle.ms_monitoreo_geo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.MunicipalidadDelValle.ms_monitoreo_geo.cliente.AlertaCliente;
import com.MunicipalidadDelValle.ms_monitoreo_geo.dto.ReporteDTO;
import com.MunicipalidadDelValle.ms_monitoreo_geo.model.ReporteGeoModel;
import com.MunicipalidadDelValle.ms_monitoreo_geo.repository.MapaRepository;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MapaServiceTest {
    @Mock MapaRepository mapaRepository;
    @Mock AlertaCliente alertaCliente;
    private MapaService service;

    @BeforeEach
    void setUp() {
        service = new MapaService(mapaRepository, alertaCliente);
    }

    @Test
    void internalCallReturnsAndPersistsCurrentReports() {
        ReporteDTO dto = new ReporteDTO();
        dto.setId(7L);
        dto.setTitulo("Reporte actual");
        dto.setLatitud(-33.4);
        dto.setLongitud(-70.6);
        when(alertaCliente.obtenerTodosLosReportes()).thenReturn(List.of(dto));
        when(mapaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ReporteGeoModel> result = service.sincronizarMapa();

        assertThat(result).singleElement().satisfies(point -> {
            assertThat(point.getIdOriginal()).isEqualTo(7L);
            assertThat(point.getTitulo()).isEqualTo("Reporte actual");
        });
        verify(alertaCliente).obtenerTodosLosReportes();
    }

    @Test
    void authorizationErrorNeverReturnsStaleDataAsSuccess() {
        assertThatThrownBy(() -> service.fallbackSincronizarMapa(feignException(403)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        verify(mapaRepository, never()).findAll();
    }

    @Test
    void realAvailabilityErrorUsesLocalFallback() {
        ReporteGeoModel cached = new ReporteGeoModel();
        cached.setIdOriginal(7L);
        when(mapaRepository.findAll()).thenReturn(List.of(cached));

        assertThat(service.fallbackSincronizarMapa(feignException(503)))
                .containsExactly(cached);
    }

    @Test
    void unrelatedFailureDoesNotUseAvailabilityFallback() {
        IllegalStateException failure = new IllegalStateException("mapping failed");
        assertThatThrownBy(() -> service.fallbackSincronizarMapa(failure)).isSameAs(failure);
        verify(mapaRepository, never()).findAll();
    }

    private static FeignException feignException(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "/api/reportes",
                Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        Response response = Response.builder().status(status).reason("test")
                .request(request).headers(Collections.emptyMap()).build();
        return FeignException.errorStatus("AlertaCliente#obtenerTodosLosReportes", response);
    }
}
