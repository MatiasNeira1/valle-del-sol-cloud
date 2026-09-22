package com.MunicipalidadDelValle.ms_monitoreo_geo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsMonitoreoGeoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsMonitoreoGeoApplication.class, args);
    }
}
