package com.sgpi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sgpi.model.ResultadoLogin;
import com.sgpi.model.User;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas de HU-001: inicio de sesión y bloqueo tras intentos fallidos. */
class InicioSesionTest {

    private static final String CORREO = "coordinador@sgpi.local";
    private static final String CLAVE = "Coord123*";

    /** Reloj que la prueba puede adelantar para simular el paso del tiempo. */
    private static final class RelojAjustable extends Clock {
        private Instant ahora = Instant.parse("2026-11-03T10:00:00Z");

        void adelantar(Duration tiempo) {
            ahora = ahora.plus(tiempo);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private RelojAjustable reloj;
    private DataService database;

    @BeforeEach
    void prepararBaseDeDatos() {
        reloj = new RelojAjustable();
        database = new DataService("jdbc:h2:mem:sgpi-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", reloj);
        database.init();
    }

    @Test
    @DisplayName("Con credenciales correctas el inicio de sesión es exitoso (sin importar mayúsculas en el correo)")
    void credencialesCorrectas() {
        ResultadoLogin resultado = database.iniciarSesion("  Coordinador@SGPI.local ", CLAVE);

        assertTrue(resultado.exitoso());
        assertEquals("COORDINADOR", resultado.usuario().orElseThrow().rol());
    }

    @Test
    @DisplayName("Cada intento fallido informa cuántos intentos quedan")
    void intentosRestantes() {
        assertEquals(2, database.iniciarSesion(CORREO, "mala").intentosRestantes());
        assertEquals(1, database.iniciarSesion(CORREO, "mala").intentosRestantes());
    }

    @Test
    @DisplayName("CP-HU001: tras 3 intentos fallidos la cuenta se bloquea, incluso con la contraseña correcta")
    void bloqueoTrasTresIntentos() {
        database.iniciarSesion(CORREO, "mala");
        database.iniciarSesion(CORREO, "mala");
        ResultadoLogin tercero = database.iniciarSesion(CORREO, "mala");

        assertEquals(ResultadoLogin.Estado.BLOQUEADO, tercero.estado());
        assertEquals(15, tercero.minutosBloqueo());
        assertEquals(ResultadoLogin.Estado.BLOQUEADO, database.iniciarSesion(CORREO, CLAVE).estado());
        assertTrue(database.authenticate(CORREO, CLAVE).isEmpty());
    }

    @Test
    @DisplayName("El bloqueo vence a los 15 minutos y la cuenta recupera sus intentos")
    void bloqueoVence() {
        for (int i = 0; i < 3; i++) {
            database.iniciarSesion(CORREO, "mala");
        }

        reloj.adelantar(Duration.ofMinutes(14));
        assertEquals(ResultadoLogin.Estado.BLOQUEADO, database.iniciarSesion(CORREO, CLAVE).estado());

        reloj.adelantar(Duration.ofMinutes(2));
        assertTrue(database.iniciarSesion(CORREO, CLAVE).exitoso());
    }

    @Test
    @DisplayName("Un inicio de sesión exitoso reinicia el contador de intentos fallidos")
    void exitoReiniciaContador() {
        database.iniciarSesion(CORREO, "mala");
        database.iniciarSesion(CORREO, "mala");
        assertTrue(database.iniciarSesion(CORREO, CLAVE).exitoso());

        assertEquals(2, database.iniciarSesion(CORREO, "mala").intentosRestantes());
    }

    @Test
    @DisplayName("Los intentos fallidos de un usuario no afectan a otro")
    void bloqueoPorUsuario() {
        for (int i = 0; i < 3; i++) {
            database.iniciarSesion(CORREO, "mala");
        }

        assertTrue(database.iniciarSesion("director@sgpi.local", "Dir123*").exitoso());
    }

    @Test
    @DisplayName("El administrador puede desbloquear una cuenta; otros roles no")
    void desbloqueoPorAdministrador() {
        for (int i = 0; i < 3; i++) {
            database.iniciarSesion(CORREO, "mala");
        }
        User admin = database.authenticate("admin@sgpi.local", "Admin123*").orElseThrow();
        User director = database.authenticate("director@sgpi.local", "Dir123*").orElseThrow();

        assertThrows(IllegalStateException.class, () -> database.desbloquearUsuario(CORREO, director));

        database.desbloquearUsuario(CORREO, admin);
        assertTrue(database.iniciarSesion(CORREO, CLAVE).exitoso());
    }

    @Test
    @DisplayName("Un correo inexistente no revela si el usuario existe")
    void correoInexistente() {
        ResultadoLogin resultado = database.iniciarSesion("nadie@sgpi.local", "x");

        assertEquals(ResultadoLogin.Estado.CREDENCIALES_INVALIDAS, resultado.estado());
    }
}
