package com.MunicipalidadDelValle.ms_notificaciones.controller;

import com.MunicipalidadDelValle.ms_notificaciones.model.Alerta;
import com.MunicipalidadDelValle.ms_notificaciones.repository.IAlertaRepository;
import com.MunicipalidadDelValle.ms_notificaciones.service.NotificacionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class AlertaController {

    @Autowired
    private IAlertaRepository repository;

    @Autowired
    private NotificacionFactory factory;

    // POST: Recibe una alerta y decide el canal (SMS o EMAIL)
    @PostMapping("/enviar")
    public Alerta enviarAlerta(@RequestBody Alerta alerta, @RequestParam String canal) {
        // 1. Guardamos la alerta en la base de datos
        Alerta alertaGuardada = repository.save(alerta);

        // 2. Procesamos el envío a través de la Factory según el canal
        factory.crearCanal(canal, alertaGuardada);

        return alertaGuardada;
    }

    // GET: Permite consultar el historial de alertas enviadas
    @GetMapping("/historial")
    public List<Alerta> obtenerHistorial() {
        return repository.findAll();
    }
}