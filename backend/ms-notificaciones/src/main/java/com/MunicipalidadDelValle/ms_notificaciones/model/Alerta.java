package com.MunicipalidadDelValle.ms_notificaciones.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "alertas")
public class Alerta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String mensaje;
    private String nivelRiesgo;
    private String zonaAfectada;
}