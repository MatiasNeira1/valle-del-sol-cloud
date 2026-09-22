package com.MunicipalidadDelValle.ms_alerta_incendios.Excepcion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class ExcepcionGlobal {

    @ExceptionHandler(ReporteInvalidoExcepcion.class)
    public ResponseEntity<Map<String, String>> manejarReporteInvalido(ReporteInvalidoExcepcion ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("error", "Validación fallida");
        respuesta.put("mensaje", ex.getMessage());
        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST); // Retorna un 400 Bad Request
    }

    // Atrapa cualquier otro error inesperado (como la base de datos caída)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErroresGenerales(Exception ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("error", "Error interno del servidor");
        respuesta.put("mensaje", "Ocurrió un problema inesperado. Por favor, intente más tarde.");
        return new ResponseEntity<>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }
}
