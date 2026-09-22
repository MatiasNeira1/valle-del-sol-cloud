package com.MunicipalidadDelValle.ms_monitoreo_geo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.MunicipalidadDelValle.ms_monitoreo_geo.cliente.AlertaCliente;
import com.MunicipalidadDelValle.ms_monitoreo_geo.dto.ReporteDTO;
import com.MunicipalidadDelValle.ms_monitoreo_geo.model.ReporteGeoModel;
import com.MunicipalidadDelValle.ms_monitoreo_geo.repository.MapaRepository;

import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapaService {
    private final MapaRepository mapaRepository;
    private final AlertaCliente alertaCliente;

    @CircuitBreaker(name = "alertaCB", fallbackMethod = "fallbackSincronizarMapa")
    public List<ReporteGeoModel> sincronizarMapa() {
        log.info("Iniciando sincronización del mapa");
        List<ReporteDTO> reportes = alertaCliente.obtenerTodosLosReportes();
        List<ReporteGeoModel> puntos = reportes.stream().map(dto -> {
            ReporteGeoModel punto = new ReporteGeoModel();
            punto.setIdOriginal(dto.getId());
            punto.setTitulo(dto.getTitulo());
            punto.setLatitud(dto.getLatitud());
            punto.setLongitud(dto.getLongitud());
            return punto;
        }).toList();

        log.info("Guardando {} reportes en la base de datos de monitoreo", puntos.size());
        return mapaRepository.saveAll(puntos);
    }

    public List<ReporteGeoModel> fallbackSincronizarMapa(Throwable error) {
        if (error instanceof FeignException feignError
                && (feignError.status() == HttpStatus.UNAUTHORIZED.value()
                    || feignError.status() == HttpStatus.FORBIDDEN.value())) {
            throw new ResponseStatusException(HttpStatus.valueOf(feignError.status()),
                    "El servicio de reportes rechazó la autorización interna");
        }

        boolean unavailable = error instanceof RetryableException
                || error instanceof CallNotPermittedException
                || (error instanceof FeignException feignError && feignError.status() >= 500);
        if (!unavailable) {
            if (error instanceof RuntimeException runtimeException) throw runtimeException;
            throw new IllegalStateException("Falló la sincronización del mapa", error);
        }

        log.warn("Servicio de reportes no disponible; se devolverán los últimos datos locales (tipo={})",
                error.getClass().getSimpleName());
        return mapaRepository.findAll();
    }
}
