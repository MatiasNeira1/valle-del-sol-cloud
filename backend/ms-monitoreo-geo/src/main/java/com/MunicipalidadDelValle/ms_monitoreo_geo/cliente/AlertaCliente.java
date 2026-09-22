package com.MunicipalidadDelValle.ms_monitoreo_geo.cliente;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import com.MunicipalidadDelValle.ms_monitoreo_geo.dto.ReporteDTO;
import java.util.List;

@FeignClient(name = "ms-alerta-incendios", url = "${alerta.url}")
public interface AlertaCliente {

    @GetMapping("/api/reportes") 
    List<ReporteDTO> obtenerTodosLosReportes();
}
