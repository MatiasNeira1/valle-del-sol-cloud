package com.MunicipalidadDelValle.ms_notificaciones.service;

import com.MunicipalidadDelValle.ms_notificaciones.model.Alerta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para NotificacionFactory.
 * Como es un componente puro sin dependencias externas,
 * no se necesita Mockito. Se verifica la salida de consola.
 */
class NotificacionFactoryTest {

    private NotificacionFactory notificacionFactory;
    private Alerta alerta;

    // Para capturar la salida de System.out
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        notificacionFactory = new NotificacionFactory();

        alerta = new Alerta();
        alerta.setId(1L);
        alerta.setMensaje("Alerta de incendio forestal en zona sur");
        alerta.setNivelRiesgo("ALTO");
        alerta.setZonaAfectada("Valle Central");

        // Capturar System.out para verificar mensajes
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    // Restaurar System.out después de cada test
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TESTS PARA crearCanal() - Canal SMS
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("crearCanal - Canal SMS")
    class CanalSMS {

        @Test
        @DisplayName("Debe enviar notificación SMS con la zona afectada")
        void enviarSMS() {
            // Act
            notificacionFactory.crearCanal("SMS", alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Enviando SMS a la zona: Valle Central"));
            assertTrue(salida.contains("Mensaje: Alerta de incendio forestal en zona sur"));
        }

        @Test
        @DisplayName("Debe aceptar tipo SMS en minúsculas")
        void enviarSMSMinusculas() {
            // Act
            notificacionFactory.crearCanal("sms", alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Enviando SMS a la zona: Valle Central"));
        }

        @Test
        @DisplayName("Debe aceptar tipo SMS en mayúsculas y minúsculas mezcladas")
        void enviarSMSMixCase() {
            // Act
            notificacionFactory.crearCanal("Sms", alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Enviando SMS a la zona: Valle Central"));
        }

        @Test
        @DisplayName("Debe incluir el mensaje de la alerta en el SMS")
        void smsContieneElMensaje() {
            // Arrange
            alerta.setMensaje("Evacuación inmediata sector oriente");

            // Act
            notificacionFactory.crearCanal("SMS", alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Evacuación inmediata sector oriente"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TESTS PARA crearCanal() - Canal EMAIL
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("crearCanal - Canal EMAIL")
    class CanalEMAIL {

        @Test
        @DisplayName("Debe enviar notificación por Email con el nivel de riesgo")
        void enviarEmail() {
            // Act
            notificacionFactory.crearCanal("EMAIL", alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Enviando Email de alerta - Nivel: ALTO"));
            assertTrue(salida.contains("Cuerpo: Alerta de incendio forestal en zona sur"));
        }

        @Test
        @DisplayName("Debe aceptar tipo EMAIL en minúsculas")
        void enviarEmailMinusculas() {
            // Act
            notificacionFactory.crearCanal("email", alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Enviando Email de alerta - Nivel: ALTO"));
        }

        @Test
        @DisplayName("Debe aceptar tipo EMAIL en formato mixto")
        void enviarEmailMixCase() {
            // Act
            notificacionFactory.crearCanal("Email", alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Enviando Email de alerta - Nivel: ALTO"));
        }

        @Test
        @DisplayName("Debe mostrar el nivel de riesgo correcto en el email")
        void emailConNivelDeRiesgo() {
            // Arrange
            alerta.setNivelRiesgo("CRITICO");

            // Act
            notificacionFactory.crearCanal("EMAIL", alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Nivel: CRITICO"));
        }

        @Test
        @DisplayName("Debe incluir el cuerpo del mensaje en el email")
        void emailConCuerpoMensaje() {
            // Arrange
            alerta.setMensaje("Se requiere apoyo aéreo urgente");

            // Act
            notificacionFactory.crearCanal("EMAIL", alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Cuerpo: Se requiere apoyo aéreo urgente"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TESTS PARA crearCanal() - Canal no reconocido
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("crearCanal - Canal no reconocido")
    class CanalNoReconocido {

        @Test
        @DisplayName("Debe indicar canal no reconocido para tipo desconocido")
        void canalDesconocido() {
            // Act
            notificacionFactory.crearCanal("TELEGRAM", alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Canal de notificación no reconocido."));
        }

        @Test
        @DisplayName("Debe indicar canal no reconocido para tipo null")
        void canalNull() {
            // Act
            notificacionFactory.crearCanal(null, alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Canal de notificación no reconocido."));
        }

        @Test
        @DisplayName("Debe indicar canal no reconocido para tipo vacío")
        void canalVacio() {
            // Act
            notificacionFactory.crearCanal("", alerta);

            // Assert
            String salida = outputStream.toString();
            assertTrue(salida.contains("Canal de notificación no reconocido."));
        }

        @Test
        @DisplayName("Debe indicar canal no reconocido para tipos similares pero incorrectos")
        void canalesSimilaresIncorrectos() {
            // Act & Assert
            notificacionFactory.crearCanal("WHATSAPP", alerta);
            String salida1 = outputStream.toString();
            assertTrue(salida1.contains("Canal de notificación no reconocido."));

            // Limpiar y probar otro
            outputStream.reset();
            notificacionFactory.crearCanal("PUSH", alerta);
            String salida2 = outputStream.toString();
            assertTrue(salida2.contains("Canal de notificación no reconocido."));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TESTS PARA verificar que no lanza excepciones
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("crearCanal - Robustez")
    class Robustez {

        @Test
        @DisplayName("No debe lanzar excepción con alerta sin zona afectada")
        void alertaSinZona() {
            // Arrange
            alerta.setZonaAfectada(null);

            // Act & Assert - No debe lanzar excepción
            assertDoesNotThrow(() -> notificacionFactory.crearCanal("SMS", alerta));
        }

        @Test
        @DisplayName("No debe lanzar excepción con alerta sin mensaje")
        void alertaSinMensaje() {
            // Arrange
            alerta.setMensaje(null);

            // Act & Assert
            assertDoesNotThrow(() -> notificacionFactory.crearCanal("EMAIL", alerta));
        }

        @Test
        @DisplayName("No debe lanzar excepción con alerta sin nivel de riesgo")
        void alertaSinNivelRiesgo() {
            // Arrange
            alerta.setNivelRiesgo(null);

            // Act & Assert
            assertDoesNotThrow(() -> notificacionFactory.crearCanal("EMAIL", alerta));
        }
    }
}
