package com.MunicipalidadDelValle.ms_notificaciones.service;

import com.MunicipalidadDelValle.ms_notificaciones.model.Alerta;
import org.springframework.stereotype.Component;

@Component
public class NotificacionFactory {

    /**
     * Implementación del método crearCanal según el diagrama de clases.
     * Aquí se decide si la alerta sale por SMS o Email.
     */
    public void crearCanal(String tipo, Alerta alerta) {
        if ("SMS".equalsIgnoreCase(tipo)) {
            // Lógica simulada para envío de SMS
            System.out.println("Enviando SMS a la zona: " + alerta.getZonaAfectada());
            System.out.println("Mensaje: " + alerta.getMensaje());
        } 
        else if ("EMAIL".equalsIgnoreCase(tipo)) {
            // Lógica simulada para envío de correo electrónico
            System.out.println("Enviando Email de alerta - Nivel: " + alerta.getNivelRiesgo());
            System.out.println("Cuerpo: " + alerta.getMensaje());
        }
        else {
            System.out.println("Canal de notificación no reconocido.");
        }
    }
}