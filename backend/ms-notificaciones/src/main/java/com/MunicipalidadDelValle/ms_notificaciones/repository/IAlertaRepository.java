package com.MunicipalidadDelValle.ms_notificaciones.repository;

import com.MunicipalidadDelValle.ms_notificaciones.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IAlertaRepository extends JpaRepository<Alerta, Long> {
    
    // Este es el método que pide tu diagrama de clases para filtrar 
    List<Alerta> findByZonaAfectada(String zonaAfectada);
}