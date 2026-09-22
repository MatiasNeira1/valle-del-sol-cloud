package com.MunicipalidadDelValle.ms_alerta_incendios.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.MunicipalidadDelValle.ms_alerta_incendios.model.ReporteModel;
import com.MunicipalidadDelValle.ms_alerta_incendios.service.ReporteService;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    // Mapa para llevar el registro de peticiones por IP (Rate Limiting en memoria)
    // Almacena la IP como llave y una lista de fechas (timestamps) de sus reportes
    private final Map<String, List<LocalDateTime>> requestCountsPerIp = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_LIMIT = 5;

    @PostMapping("/crear")
    public ResponseEntity<?> crearReporte(@RequestBody ReporteModel nuevoReporte, HttpServletRequest request) {
        // 1. Obtener la IP del cliente
        String clientIp = obtenerIpCliente(request);

        // 2. Aplicar limitación de tasa (Rate Limiting)
        if (!puedeCrearReporte(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Has superado el límite de " + MAX_REQUESTS_LIMIT + " reportes cada 10 horas. Intenta de nuevo más tarde.");
        }

        // 3. Crear el reporte si pasó la validación
        ReporteModel reporteCreado = reporteService.crearNuevoReporte(nuevoReporte);
        return new ResponseEntity<>(reporteCreado, HttpStatus.CREATED);
    }

    // ── Métodos auxiliares para Rate Limiting ────────────────────────────────────

    private boolean puedeCrearReporte(String ip) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime hace10Horas = ahora.minusHours(10);

        // Obtenemos o inicializamos la lista de peticiones para esta IP
        List<LocalDateTime> peticiones = requestCountsPerIp.computeIfAbsent(ip, k -> new ArrayList<>());

        synchronized (peticiones) {
            // Limpiar peticiones antiguas (mayores a 10 horas) para no saturar la memoria
            peticiones.removeIf(tiempo -> tiempo.isBefore(hace10Horas));

            // Comprobar si ya alcanzó el límite
            if (peticiones.size() >= MAX_REQUESTS_LIMIT) {
                return false; // Límite superado
            }

            // Registrar la nueva petición
            peticiones.add(ahora);
            return true;
        }
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @GetMapping
    public ResponseEntity<List<ReporteModel>> obtenerTodosLosReportes() {
        List<ReporteModel> reportes = reporteService.obtenerTodos();
        return new ResponseEntity<>(reportes, HttpStatus.OK);
    }

    @GetMapping("/sincronizar")
    public ResponseEntity<List<ReporteModel>> sincronizarReportes() {
        List<ReporteModel> reportes = reporteService.obtenerTodos();
        return new ResponseEntity<>(reportes, HttpStatus.OK);
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<ReporteModel> editarReporte(@PathVariable Long id,
            @RequestBody ReporteModel reporteDetalles) {
        ReporteModel reporteActualizado = reporteService.actualizarReporte(id, reporteDetalles);
        return new ResponseEntity<>(reporteActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarReporte(@PathVariable Long id) {
        reporteService.eliminarReporte(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
