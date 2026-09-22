package com.MunicipalidadDelValle.ms_alerta_incendios.Excepcion;

public class ReporteInvalidoExcepcion extends RuntimeException {
    public ReporteInvalidoExcepcion(String mensaje) {
        super(mensaje);
    }

}
