package com.MunicipalidadDelValle.ms_alerta_incendios.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
@Entity
@Table(name = "evidencias_multimedia")
public class EvidenciaMultimedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Admite URLs largas generadas por almacenamiento de objetos.
    @Column(length = 1000) 
    private String urlArchivo;  
    
    private String tipoArchivo; // IMAGEN o VIDEO
    private LocalDateTime fechaSubida;

    // La relación: Muchas evidencias pertenecen a UN reporte
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id", nullable = false)
    @JsonIgnore // Evita que al pedir el reporte, se cree un bucle infinito
    private ReporteModel reporte;

    @PrePersist
    protected void onCreate() {
        this.fechaSubida = LocalDateTime.now();
    }
    


}
