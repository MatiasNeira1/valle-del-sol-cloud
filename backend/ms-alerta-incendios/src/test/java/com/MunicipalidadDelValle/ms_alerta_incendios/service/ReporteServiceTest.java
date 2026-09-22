package com.MunicipalidadDelValle.ms_alerta_incendios.service;

import com.MunicipalidadDelValle.ms_alerta_incendios.Excepcion.ReporteInvalidoExcepcion;
import com.MunicipalidadDelValle.ms_alerta_incendios.model.EvidenciaMultimedia;
import com.MunicipalidadDelValle.ms_alerta_incendios.model.ReporteModel;
import com.MunicipalidadDelValle.ms_alerta_incendios.repository.ReporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ReporteService.
 * Se usa Mockito puro (sin levantar Spring Context) para independencia de la BD.
 */
@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @InjectMocks
    private ReporteService reporteService;

    private ReporteModel reporteValido;

    @BeforeEach
    void setUp() {
        reporteValido = new ReporteModel();
        reporteValido.setTitulo("Incendio forestal sector alto");
        reporteValido.setDescripcion("Se detecta humo en la zona boscosa");
        reporteValido.setLatitud(-33.4489);
        reporteValido.setLongitud(-70.6693);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TESTS PARA crearNuevoReporte()
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("crearNuevoReporte - Casos exitosos")
    class CrearReporteExitoso {

        @Test
        @DisplayName("Debe crear un reporte válido correctamente")
        void crearReporteValido() {
            // Arrange
            when(reporteRepository.save(any(ReporteModel.class))).thenReturn(reporteValido);

            // Act
            ReporteModel resultado = reporteService.crearNuevoReporte(reporteValido);

            // Assert
            assertNotNull(resultado);
            assertEquals("Incendio forestal sector alto", resultado.getTitulo());
            assertEquals(-33.4489, resultado.getLatitud());
            assertEquals(-70.6693, resultado.getLongitud());
            verify(reporteRepository, times(1)).save(reporteValido);
        }

        @Test
        @DisplayName("Debe crear un reporte sin evidencias (lista null)")
        void crearReporteSinEvidencias() {
            // Arrange
            reporteValido.setEvidencias(null);
            when(reporteRepository.save(any(ReporteModel.class))).thenReturn(reporteValido);

            // Act
            ReporteModel resultado = reporteService.crearNuevoReporte(reporteValido);

            // Assert
            assertNotNull(resultado);
            verify(reporteRepository, times(1)).save(reporteValido);
        }

        @Test
        @DisplayName("Debe asociar las evidencias al reporte padre al crear")
        void crearReporteConEvidencias() {
            // Arrange
            EvidenciaMultimedia evidencia1 = new EvidenciaMultimedia();
            evidencia1.setUrlArchivo("https://storage.example/imagen1.jpg");
            evidencia1.setTipoArchivo("IMAGEN");

            EvidenciaMultimedia evidencia2 = new EvidenciaMultimedia();
            evidencia2.setUrlArchivo("https://storage.example/video1.mp4");
            evidencia2.setTipoArchivo("VIDEO");

            List<EvidenciaMultimedia> evidencias = new ArrayList<>(Arrays.asList(evidencia1, evidencia2));
            reporteValido.setEvidencias(evidencias);

            when(reporteRepository.save(any(ReporteModel.class))).thenReturn(reporteValido);

            // Act
            ReporteModel resultado = reporteService.crearNuevoReporte(reporteValido);

            // Assert
            assertNotNull(resultado);
            // Verificar que cada evidencia tiene su reporte padre asignado
            for (EvidenciaMultimedia ev : reporteValido.getEvidencias()) {
                assertEquals(reporteValido, ev.getReporte());
            }
            verify(reporteRepository, times(1)).save(reporteValido);
        }

        @Test
        @DisplayName("Debe crear un reporte con coordenadas en el límite válido")
        void crearReporteConCoordenadasLimite() {
            // Arrange
            reporteValido.setLatitud(90.0);
            reporteValido.setLongitud(180.0);
            when(reporteRepository.save(any(ReporteModel.class))).thenReturn(reporteValido);

            // Act
            ReporteModel resultado = reporteService.crearNuevoReporte(reporteValido);

            // Assert
            assertNotNull(resultado);
            assertEquals(90.0, resultado.getLatitud());
            assertEquals(180.0, resultado.getLongitud());
        }

        @Test
        @DisplayName("Debe crear un reporte con coordenadas negativas en el límite")
        void crearReporteConCoordenadasNegativasLimite() {
            // Arrange
            reporteValido.setLatitud(-90.0);
            reporteValido.setLongitud(-180.0);
            when(reporteRepository.save(any(ReporteModel.class))).thenReturn(reporteValido);

            // Act
            ReporteModel resultado = reporteService.crearNuevoReporte(reporteValido);

            // Assert
            assertNotNull(resultado);
            assertEquals(-90.0, resultado.getLatitud());
            assertEquals(-180.0, resultado.getLongitud());
        }
    }

    @Nested
    @DisplayName("crearNuevoReporte - Validaciones de título")
    class CrearReporteValidacionTitulo {

        @Test
        @DisplayName("Debe lanzar excepción si el título es null")
        void crearReporteSinTitulo() {
            // Arrange
            reporteValido.setTitulo(null);

            // Act & Assert
            ReporteInvalidoExcepcion excepcion = assertThrows(
                    ReporteInvalidoExcepcion.class,
                    () -> reporteService.crearNuevoReporte(reporteValido)
            );
            assertEquals("El reporte debe tener un título descriptivo.", excepcion.getMessage());
            verify(reporteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si el título está vacío")
        void crearReporteConTituloVacio() {
            // Arrange
            reporteValido.setTitulo("");

            // Act & Assert
            ReporteInvalidoExcepcion excepcion = assertThrows(
                    ReporteInvalidoExcepcion.class,
                    () -> reporteService.crearNuevoReporte(reporteValido)
            );
            assertEquals("El reporte debe tener un título descriptivo.", excepcion.getMessage());
            verify(reporteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si el título tiene solo espacios en blanco")
        void crearReporteConTituloEnBlanco() {
            // Arrange
            reporteValido.setTitulo("   ");

            // Act & Assert
            assertThrows(ReporteInvalidoExcepcion.class,
                    () -> reporteService.crearNuevoReporte(reporteValido));
            verify(reporteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("crearNuevoReporte - Validaciones de coordenadas")
    class CrearReporteValidacionCoordenadas {

        @Test
        @DisplayName("Debe lanzar excepción si la latitud es null")
        void crearReporteSinLatitud() {
            // Arrange
            reporteValido.setLatitud(null);

            // Act & Assert
            ReporteInvalidoExcepcion excepcion = assertThrows(
                    ReporteInvalidoExcepcion.class,
                    () -> reporteService.crearNuevoReporte(reporteValido)
            );
            assertEquals("Las coordenadas (latitud y longitud) son obligatorias para el mapa.", excepcion.getMessage());
            verify(reporteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si la longitud es null")
        void crearReporteSinLongitud() {
            // Arrange
            reporteValido.setLongitud(null);

            // Act & Assert
            ReporteInvalidoExcepcion excepcion = assertThrows(
                    ReporteInvalidoExcepcion.class,
                    () -> reporteService.crearNuevoReporte(reporteValido)
            );
            assertEquals("Las coordenadas (latitud y longitud) son obligatorias para el mapa.", excepcion.getMessage());
            verify(reporteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si ambas coordenadas son null")
        void crearReporteSinAmbasCoordenadas() {
            // Arrange
            reporteValido.setLatitud(null);
            reporteValido.setLongitud(null);

            // Act & Assert
            assertThrows(ReporteInvalidoExcepcion.class,
                    () -> reporteService.crearNuevoReporte(reporteValido));
            verify(reporteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si la latitud excede 90")
        void crearReporteConLatitudInvalida() {
            // Arrange
            reporteValido.setLatitud(91.0);

            // Act & Assert
            ReporteInvalidoExcepcion excepcion = assertThrows(
                    ReporteInvalidoExcepcion.class,
                    () -> reporteService.crearNuevoReporte(reporteValido)
            );
            assertEquals("Las coordenadas geográficas ingresadas no son válidas.", excepcion.getMessage());
            verify(reporteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si la latitud es menor a -90")
        void crearReporteConLatitudNegativaInvalida() {
            // Arrange
            reporteValido.setLatitud(-91.0);

            // Act & Assert
            assertThrows(ReporteInvalidoExcepcion.class,
                    () -> reporteService.crearNuevoReporte(reporteValido));
            verify(reporteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si la longitud excede 180")
        void crearReporteConLongitudInvalida() {
            // Arrange
            reporteValido.setLongitud(181.0);

            // Act & Assert
            assertThrows(ReporteInvalidoExcepcion.class,
                    () -> reporteService.crearNuevoReporte(reporteValido));
            verify(reporteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si la longitud es menor a -180")
        void crearReporteConLongitudNegativaInvalida() {
            // Arrange
            reporteValido.setLongitud(-181.0);

            // Act & Assert
            assertThrows(ReporteInvalidoExcepcion.class,
                    () -> reporteService.crearNuevoReporte(reporteValido));
            verify(reporteRepository, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TESTS PARA obtenerTodos()
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("obtenerTodos")
    class ObtenerTodos {

        @Test
        @DisplayName("Debe retornar una lista con reportes")
        void obtenerTodosConDatos() {
            // Arrange
            ReporteModel reporte2 = new ReporteModel();
            reporte2.setTitulo("Incendio en cerro");
            List<ReporteModel> reportes = Arrays.asList(reporteValido, reporte2);
            when(reporteRepository.findAll()).thenReturn(reportes);

            // Act
            List<ReporteModel> resultado = reporteService.obtenerTodos();

            // Assert
            assertEquals(2, resultado.size());
            verify(reporteRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Debe retornar una lista vacía cuando no hay reportes")
        void obtenerTodosSinDatos() {
            // Arrange
            when(reporteRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<ReporteModel> resultado = reporteService.obtenerTodos();

            // Assert
            assertTrue(resultado.isEmpty());
            verify(reporteRepository, times(1)).findAll();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TESTS PARA actualizarReporte()
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("actualizarReporte")
    class ActualizarReporte {

        @Test
        @DisplayName("Debe actualizar el título del reporte")
        void actualizarTitulo() {
            // Arrange
            Long id = 1L;
            reporteValido.setId(id);

            ReporteModel detalles = new ReporteModel();
            detalles.setTitulo("Título actualizado");

            when(reporteRepository.findById(id)).thenReturn(Optional.of(reporteValido));
            when(reporteRepository.save(any(ReporteModel.class))).thenReturn(reporteValido);

            // Act
            ReporteModel resultado = reporteService.actualizarReporte(id, detalles);

            // Assert
            assertEquals("Título actualizado", resultado.getTitulo());
            verify(reporteRepository).findById(id);
            verify(reporteRepository).save(reporteValido);
        }

        @Test
        @DisplayName("Debe actualizar la descripción del reporte")
        void actualizarDescripcion() {
            // Arrange
            Long id = 1L;
            reporteValido.setId(id);

            ReporteModel detalles = new ReporteModel();
            detalles.setDescripcion("Nueva descripción detallada");

            when(reporteRepository.findById(id)).thenReturn(Optional.of(reporteValido));
            when(reporteRepository.save(any(ReporteModel.class))).thenReturn(reporteValido);

            // Act
            ReporteModel resultado = reporteService.actualizarReporte(id, detalles);

            // Assert
            assertEquals("Nueva descripción detallada", resultado.getDescripcion());
        }

        @Test
        @DisplayName("Debe actualizar el estado del reporte")
        void actualizarEstado() {
            // Arrange
            Long id = 1L;
            reporteValido.setId(id);
            reporteValido.setEstado("NUEVO");

            ReporteModel detalles = new ReporteModel();
            detalles.setEstado("CONTROLADO");

            when(reporteRepository.findById(id)).thenReturn(Optional.of(reporteValido));
            when(reporteRepository.save(any(ReporteModel.class))).thenReturn(reporteValido);

            // Act
            ReporteModel resultado = reporteService.actualizarReporte(id, detalles);

            // Assert
            assertEquals("CONTROLADO", resultado.getEstado());
        }

        @Test
        @DisplayName("Debe actualizar las coordenadas del reporte")
        void actualizarCoordenadas() {
            // Arrange
            Long id = 1L;
            reporteValido.setId(id);

            ReporteModel detalles = new ReporteModel();
            detalles.setLatitud(-34.0);
            detalles.setLongitud(-71.0);

            when(reporteRepository.findById(id)).thenReturn(Optional.of(reporteValido));
            when(reporteRepository.save(any(ReporteModel.class))).thenReturn(reporteValido);

            // Act
            ReporteModel resultado = reporteService.actualizarReporte(id, detalles);

            // Assert
            assertEquals(-34.0, resultado.getLatitud());
            assertEquals(-71.0, resultado.getLongitud());
        }

        @Test
        @DisplayName("No debe modificar campos cuando los detalles son null")
        void actualizarSinCambios() {
            // Arrange
            Long id = 1L;
            reporteValido.setId(id);
            String tituloOriginal = reporteValido.getTitulo();
            String descripcionOriginal = reporteValido.getDescripcion();

            ReporteModel detalles = new ReporteModel();
            // Todos los campos en null

            when(reporteRepository.findById(id)).thenReturn(Optional.of(reporteValido));
            when(reporteRepository.save(any(ReporteModel.class))).thenReturn(reporteValido);

            // Act
            ReporteModel resultado = reporteService.actualizarReporte(id, detalles);

            // Assert
            assertEquals(tituloOriginal, resultado.getTitulo());
            assertEquals(descripcionOriginal, resultado.getDescripcion());
        }

        @Test
        @DisplayName("No debe modificar el título si viene vacío")
        void actualizarConTituloVacio() {
            // Arrange
            Long id = 1L;
            reporteValido.setId(id);
            String tituloOriginal = reporteValido.getTitulo();

            ReporteModel detalles = new ReporteModel();
            detalles.setTitulo("");

            when(reporteRepository.findById(id)).thenReturn(Optional.of(reporteValido));
            when(reporteRepository.save(any(ReporteModel.class))).thenReturn(reporteValido);

            // Act
            ReporteModel resultado = reporteService.actualizarReporte(id, detalles);

            // Assert
            assertEquals(tituloOriginal, resultado.getTitulo());
        }

        @Test
        @DisplayName("Debe lanzar excepción si el reporte no existe")
        void actualizarReporteNoExistente() {
            // Arrange
            Long id = 999L;
            ReporteModel detalles = new ReporteModel();
            when(reporteRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            ReporteInvalidoExcepcion excepcion = assertThrows(
                    ReporteInvalidoExcepcion.class,
                    () -> reporteService.actualizarReporte(id, detalles)
            );
            assertEquals("Reporte no encontrado con ID: 999", excepcion.getMessage());
            verify(reporteRepository, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TESTS PARA eliminarReporte()
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("eliminarReporte")
    class EliminarReporte {

        @Test
        @DisplayName("Debe eliminar un reporte existente correctamente")
        void eliminarReporteExistente() {
            // Arrange
            Long id = 1L;
            reporteValido.setId(id);
            when(reporteRepository.findById(id)).thenReturn(Optional.of(reporteValido));
            doNothing().when(reporteRepository).delete(reporteValido);

            // Act
            reporteService.eliminarReporte(id);

            // Assert
            verify(reporteRepository, times(1)).findById(id);
            verify(reporteRepository, times(1)).delete(reporteValido);
        }

        @Test
        @DisplayName("Debe lanzar excepción al intentar eliminar un reporte que no existe")
        void eliminarReporteNoExistente() {
            // Arrange
            Long id = 999L;
            when(reporteRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            ReporteInvalidoExcepcion excepcion = assertThrows(
                    ReporteInvalidoExcepcion.class,
                    () -> reporteService.eliminarReporte(id)
            );
            assertEquals("Reporte no encontrado con ID: 999", excepcion.getMessage());
            verify(reporteRepository, never()).delete(any());
        }
    }
}
