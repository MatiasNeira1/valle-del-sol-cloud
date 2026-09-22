package com.MunicipalidadDelValle.ms_monitoreo_geo.model;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "reportes_geo")
@Data
public class ReporteGeoModel {
@Id
    private Long idOriginal; // Usaremos el mismo ID del reporte original
    private String titulo;
    private Double latitud;
    private Double longitud;
}
