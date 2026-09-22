package com.MunicipalidadDelValle.ms_monitoreo_geo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.MunicipalidadDelValle.ms_monitoreo_geo.model.ReporteGeoModel;

public interface MapaRepository extends JpaRepository<ReporteGeoModel, Long> {

}
