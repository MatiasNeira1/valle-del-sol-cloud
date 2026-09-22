package com.MunicipalidadDelValle.ms_alerta_incendios.service;

import org.springframework.stereotype.Service;
import com.MunicipalidadDelValle.ms_alerta_incendios.model.ReporteModel;
// 👇 ¡IMPORTANTE! Agregamos la importación de la Evidencia aquí:
import com.MunicipalidadDelValle.ms_alerta_incendios.model.EvidenciaMultimedia; 
import com.MunicipalidadDelValle.ms_alerta_incendios.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.MunicipalidadDelValle.ms_alerta_incendios.Excepcion.*;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {
    @Autowired
    private ReporteRepository reporteRepository;

    @Transactional
    public ReporteModel crearNuevoReporte(ReporteModel reporte) {
        
        
        if (reporte.getTitulo() == null || reporte.getTitulo().trim().isEmpty()) {
            throw new ReporteInvalidoExcepcion("El reporte debe tener un título descriptivo.");
        }
        
        if (reporte.getLatitud() == null || reporte.getLongitud() == null) {
            throw new ReporteInvalidoExcepcion("Las coordenadas (latitud y longitud) son obligatorias para el mapa.");
        }

        //sdasd
        if (reporte.getLatitud() < -90 || reporte.getLatitud() > 90 || 
            reporte.getLongitud() < -180 || reporte.getLongitud() > 180) {
            throw new ReporteInvalidoExcepcion("Las coordenadas geográficas ingresadas no son válidas.");
        }

        
        // Le decimos a cada evidencia (hijo) quién es su reporte (padre)
        if (reporte.getEvidencias() != null) {
            for (EvidenciaMultimedia evidencia : reporte.getEvidencias()) {
                evidencia.setReporte(reporte); 
            }
        }
      

        // 2. Guardar en la base de datis 
        ReporteModel reporteGuardado = reporteRepository.save(reporte);

        
        return reporteGuardado;
    }

    public List<ReporteModel> obtenerTodos() {
        return reporteRepository.findAll(); 
    }

    @Transactional
    public ReporteModel actualizarReporte(Long id, ReporteModel reporteDetalles) {
        ReporteModel reporte = reporteRepository.findById(id)
            .orElseThrow(() -> new ReporteInvalidoExcepcion("Reporte no encontrado con ID: " + id));
        
        if (reporteDetalles.getTitulo() != null && !reporteDetalles.getTitulo().trim().isEmpty()) {
            reporte.setTitulo(reporteDetalles.getTitulo());
        }
        if (reporteDetalles.getDescripcion() != null && !reporteDetalles.getDescripcion().trim().isEmpty()) {
            reporte.setDescripcion(reporteDetalles.getDescripcion());
        }
        if (reporteDetalles.getEstado() != null) {
            reporte.setEstado(reporteDetalles.getEstado());
        }
        if (reporteDetalles.getLatitud() != null) {
            reporte.setLatitud(reporteDetalles.getLatitud());
        }
        if (reporteDetalles.getLongitud() != null) {
            reporte.setLongitud(reporteDetalles.getLongitud());
        }
        
        return reporteRepository.save(reporte);
    }

    @Transactional
    public void eliminarReporte(Long id) {
        ReporteModel reporte = reporteRepository.findById(id)
            .orElseThrow(() -> new ReporteInvalidoExcepcion("Reporte no encontrado con ID: " + id));
        reporteRepository.delete(reporte);
    }
}