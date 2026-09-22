package com.MunicipalidadDelValle.ms_alerta_incendios.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
@Entity
@Table(name = "reportes")
public class ReporteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descripcion;
    private Double latitud;
    private Double longitud;
    private String estado; // Ej: "NUEVO", "EN PROGRESO", "CONTROLADO"
    private LocalDateTime fechaCreacion;

    //esto crea automatimente la fecha y el estado del incendio al momento de crear el reporte
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "NUEVO";
    }

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvidenciaMultimedia> evidencias = new ArrayList<>();

    public List<EvidenciaMultimedia> getEvidencias() { return evidencias; }
    public void setEvidencias(List<EvidenciaMultimedia> evidencias) { this.evidencias = evidencias; }

}