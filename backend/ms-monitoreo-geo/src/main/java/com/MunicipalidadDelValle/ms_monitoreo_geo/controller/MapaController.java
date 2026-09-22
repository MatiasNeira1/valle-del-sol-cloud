package com.MunicipalidadDelValle.ms_monitoreo_geo.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.MunicipalidadDelValle.ms_monitoreo_geo.service.MapaService;

import com.MunicipalidadDelValle.ms_monitoreo_geo.model.ReporteGeoModel;

@RestController
@RequestMapping("/api/mapa")
@RequiredArgsConstructor

public class MapaController {
    private final MapaService mapaService;

    @GetMapping("/sincronizar")
    public ResponseEntity<List<ReporteGeoModel>> sincronizarYObtenerMapa() {
        // Llama al servicio que se comunica con el otro MS y guarda en BD
        List<ReporteGeoModel> puntosEnMapa = mapaService.sincronizarMapa();
        return ResponseEntity.ok(puntosEnMapa);
}
}