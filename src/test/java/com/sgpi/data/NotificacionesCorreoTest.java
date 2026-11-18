package com.sgpi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sgpi.model.CorreoSaliente;
import com.sgpi.model.FrecuenciaCorreo;
import com.sgpi.model.Plan;
import com.sgpi.model.User;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas de HU-015 (notificaciones por correo electrónico y alertas en el sistema). */
class NotificacionesCorreoTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDate INICIO = LocalDate.of(2026, 11, 20);
    private static final LocalDate FIN = LocalDate.of(2026, 12, 15);

    /** Correo que la prueba "envió". */
    private record Enviado(String destinatario, String asunto, String cuerpo) {}

    /** Reloj que la prueba puede adelantar. */
    private static final class RelojAjustable extends Clock {
        private Instant ahora;

        RelojAjustable(LocalDateTime inicio) {
            this.ahora = inicio.atZone(ZONA).toInstant();
        }

        void adelantar(Duration duracion) {
            ahora = ahora.plus(duracion);
        }

        @Override
        public ZoneId getZone() {
            return ZONA;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private final List<Enviado> enviados = new ArrayList<>();
    private RelojAjustable reloj;
    private DataService database;
    private User coordinador;
    private User responsable;
    private User director;

    @BeforeEach
    void prepararBaseDeDatos() {
        crearBase(Map.of());
    }

    private void crearBase(Map<String, String> variables) {
        reloj = new RelojAjustable(LocalDateTime.of(2026, 11, 20, 9, 30));
        database = new DataService("jdbc:h2:mem:sgpi-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", reloj,
                variables::get);
        database.init();
        enviados.clear();
        database.usarEnvioCorreo((destinatario, asunto, cuerpo) ->
                enviados.add(new Enviado(destinatario, asunto, cuerpo)), Runnable::run);

        coordinador = database.authenticate("coordinador@sgpi.local", "Coord123*").orElseThrow();
        responsable = database.authenticate("responsable@sgpi.local", "Resp123*").orElseThrow();
        director = database.authenticate("director@sgpi.local", "Dir123*").orElseThrow();
    }

    @Test
    @DisplayName("Criterio 1: al asignar una actividad, el responsable recibe de inmediato un correo con el resumen")
    void correoAlAsignarActividad() {
        Plan plan = crearPlan();

        database.agregarActividad(plan.id(), "Auditar el proceso de compras", responsable.id(),
                LocalDate.of(2026, 11, 30), coordinador);

        assertEquals(1, enviados.size());
        Enviado correo = enviados.get(0);
        assertEquals("responsable@sgpi.local", correo.destinatario());
        assertTrue(correo.asunto().contains(plan.codigo()));
        assertTrue(correo.cuerpo().contains("Auditar el proceso de compras"));
        assertTrue(correo.cuerpo().contains("2026-11-30"));
        assertTrue(correo.cuerpo().contains("Preferencias"));
        assertEquals(1, database.contarNotificacionesSinLeer(responsable));
        assertEquals("Enviado", database.listarCorreos(responsable).get(0).estado());
    }

    @Test
    @DisplayName("Al enviar un plan a revisión, el director del área recibe el correo")
    void correoAlDirector() {
        Plan plan = crearPlan();

        database.enviarARevision(plan.id(), coordinador);

        assertEquals(1, enviados.size());
        assertEquals("director@sgpi.local", enviados.get(0).destinatario());
        assertTrue(enviados.get(0).asunto().contains("pendiente de su revisión"));
    }

    @Test
    @DisplayName("Criterio 1: con la dirección del SGPI configurada, el correo trae el enlace del plan")
    void enlaceDelPlan() {
        crearBase(Map.of(DataService.VARIABLE_URL_BASE, "https://sgpi.ejemplo.edu.co"));
        Plan plan = crearPlan();

        database.enviarARevision(plan.id(), coordinador);

        assertTrue(enviados.get(0).cuerpo().contains("https://sgpi.ejemplo.edu.co/planes/" + plan.codigo()));
    }

    @Test
    @DisplayName("Regla: con los correos desactivados no llega correo, pero sí la notificación en el sistema")
    void correosDesactivados() {
        database.cambiarFrecuenciaCorreo(responsable, FrecuenciaCorreo.DESACTIVADA);
        Plan plan = crearPlan();

        database.agregarActividad(plan.id(), "Auditar compras", responsable.id(), LocalDate.of(2026, 11, 30),
                coordinador);

        assertTrue(enviados.isEmpty());
        assertTrue(database.listarCorreos(responsable).isEmpty());
        assertEquals(1, database.contarNotificacionesSinLeer(responsable));
        assertEquals(FrecuenciaCorreo.DESACTIVADA, database.obtenerFrecuenciaCorreo(responsable));
    }

    @Test
    @DisplayName("Regla: con el resumen diario, las notificaciones del día llegan juntas a las 7:00 del día siguiente")
    void resumenDiario() {
        database.cambiarFrecuenciaCorreo(responsable, FrecuenciaCorreo.DIARIA);
        Plan plan = crearPlan();

        database.agregarActividad(plan.id(), "Auditar compras", responsable.id(), LocalDate.of(2026, 11, 30),
                coordinador);
        database.agregarActividad(plan.id(), "Auditar contratación", responsable.id(), LocalDate.of(2026, 12, 5),
                coordinador);

        assertTrue(enviados.isEmpty());
        List<CorreoSaliente> pendientes = database.listarCorreos(responsable);
        assertEquals(1, pendientes.size());
        assertEquals(LocalDateTime.of(2026, 11, 21, 7, 0), pendientes.get(0).enviarDesde());

        assertEquals(0, database.enviarCorreosPendientes());

        reloj.adelantar(Duration.ofHours(22));
        assertEquals(1, database.enviarCorreosPendientes());
        assertEquals(1, enviados.size());
        assertTrue(enviados.get(0).cuerpo().contains("Auditar compras"));
        assertTrue(enviados.get(0).cuerpo().contains("Auditar contratación"));
    }

    @Test
    @DisplayName("Solo se aceptan las frecuencias definidas")
    void frecuenciaNoValida() {
        assertThrows(IllegalArgumentException.class, () -> database.cambiarFrecuenciaCorreo(responsable, "SEMANAL"));
        assertEquals(FrecuenciaCorreo.INMEDIATA, database.obtenerFrecuenciaCorreo(responsable));
    }

    @Test
    @DisplayName("Si el servidor de correo falla, la operación se completa y el correo queda para reintentar")
    void fallaDelServidor() {
        database.usarEnvioCorreo((destinatario, asunto, cuerpo) -> {
            throw new IllegalStateException("Servidor no disponible");
        }, Runnable::run);
        Plan plan = crearPlan();

        database.enviarARevision(plan.id(), coordinador);

        CorreoSaliente correo = database.listarCorreos(director).get(0);
        assertNull(correo.enviadoEn());
        assertEquals("Servidor no disponible", correo.error());
        assertEquals("Con error", correo.estado());
        assertEquals(1, database.listarPendientesRevision(director).size());

        database.usarEnvioCorreo((destinatario, asunto, cuerpo) ->
                enviados.add(new Enviado(destinatario, asunto, cuerpo)), Runnable::run);
        assertEquals(1, database.enviarCorreosPendientes());
        assertEquals("Enviado", database.listarCorreos(director).get(0).estado());
    }

    @Test
    @DisplayName("Sin servidor de correo configurado, el correo queda pendiente")
    void sinServidor() {
        database.usarEnvioCorreo(null, Runnable::run);
        Plan plan = crearPlan();

        database.enviarARevision(plan.id(), coordinador);

        assertFalse(database.correoConfigurado());
        assertEquals(0, database.enviarCorreosPendientes());
        assertEquals("Pendiente", database.listarCorreos(director).get(0).estado());
    }

    private Plan crearPlan() {
        return database.createPlan("Auditoría interna de procesos 2026", "Calidad", INICIO, FIN, coordinador);
    }
}
