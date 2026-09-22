package com.MunicipalidadDelValle.ms_monitoreo_geo.dto;
import lombok.Data;

@Data
public class ReporteDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private Double latitud;
    private Double longitud;
}