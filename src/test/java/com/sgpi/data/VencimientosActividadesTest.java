package com.sgpi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sgpi.model.Actividad;
import com.sgpi.model.CorreoSaliente;
import com.sgpi.model.EstadoActividad;
import com.sgpi.model.FrecuenciaCorreo;
import com.sgpi.model.Notificacion;
import com.sgpi.model.Plan;
import com.sgpi.model.User;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas de HU-005 (notificaciones de vencimiento de actividades). */
class VencimientosActividadesTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 20);
    private static final byte[] PDF = "%PDF-1.4 acta".getBytes(StandardCharsets.US_ASCII);

    private record Enviado(String destinatario, String asunto) {}

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
        reloj = new RelojAjustable(HOY.atTime(9, 30));
        database = new DataService("jdbc:h2:mem:sgpi-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", reloj,
                nombre -> null);
        database.init();
        database.usarEnvioCorreo((destinatario, asunto, cuerpo) -> enviados.add(new Enviado(destinatario, asunto)),
                Runnable::run);

        coordinador = database.authenticate("coordinador@sgpi.local", "Coord123*").orElseThrow();
        responsable = database.authenticate("responsable@sgpi.local", "Resp123*").orElseThrow();
        director = database.authenticate("director@sgpi.local", "Dir123*").orElseThrow();
    }

    @Test
    @DisplayName("Criterio 1: una actividad que vence en 5 días genera un aviso al responsable, también por correo")
    void avisoACincoDias() {
        Plan plan = planAprobadoConActividad(HOY.plusDays(5));

        assertEquals(1, database.notificarVencimientos());

        List<String> avisos = avisosDe(responsable);
        assertEquals(1, avisos.size());
        assertTrue(avisos.get(0).startsWith("Aviso de vencimiento"));
        assertTrue(avisos.get(0).contains(plan.codigo()));
        assertTrue(avisos.get(0).contains(HOY.plusDays(5).toString()));
        assertEquals(List.of(new Enviado("responsable@sgpi.local",
                "SGPI: actividad por vencer en el plan " + plan.codigo())), enviados);
    }

    @Test
    @DisplayName("La verificación puede correr muchas veces al día sin repetir el aviso")
    void sinAvisosDuplicados() {
        planAprobadoConActividad(HOY.plusDays(3));

        assertEquals(1, database.notificarVencimientos());
        assertEquals(0, database.notificarVencimientos());
        reloj.adelantar(Duration.ofHours(6));
        assertEquals(0, database.notificarVencimientos());

        assertEquals(1, avisosDe(responsable).size());
        assertEquals(1, enviados.size());
    }

    @Test
    @DisplayName("Regla: el aviso se repite una vez cuando falta 1 día y la actividad sigue pendiente")
    void avisoRepetidoAUnDia() {
        planAprobadoConActividad(HOY.plusDays(5));
        database.notificarVencimientos();

        reloj.adelantar(Duration.ofDays(3));
        assertEquals(0, database.notificarVencimientos());

        reloj.adelantar(Duration.ofDays(1));
        assertEquals(1, database.notificarVencimientos());
        reloj.adelantar(Duration.ofDays(1));
        assertEquals(0, database.notificarVencimientos());

        List<String> avisos = avisosDe(responsable);
        assertEquals(2, avisos.size());
        assertTrue(avisos.get(0).startsWith("Último aviso de vencimiento"));
    }

    @Test
    @DisplayName("Una actividad que vence en más de 5 días no genera aviso")
    void sinAvisoAntesDeTiempo() {
        planAprobadoConActividad(HOY.plusDays(6));

        assertEquals(0, database.notificarVencimientos());
        assertTrue(avisosDe(responsable).isEmpty());
    }

    @Test
    @DisplayName("Criterio 2: una actividad finalizada no genera aviso aunque su fecha límite se acerque")
    void actividadFinalizadaSinAviso() {
        Plan plan = planAprobadoConActividad(HOY.plusDays(2));
        database.iniciarEjecucion(plan.id(), coordinador);
        Actividad actividad = database.listarActividades(plan.id()).get(0);
        database.cargarEvidencia(actividad.id(), "acta.pdf", PDF, responsable);
        database.cambiarEstadoActividad(actividad.id(), EstadoActividad.FINALIZADA, responsable);

        assertEquals(0, database.notificarVencimientos());
        assertTrue(avisosDe(responsable).isEmpty());
    }

    @Test
    @DisplayName("Un plan que todavía no está aprobado no genera avisos")
    void planEnBorradorSinAviso() {
        Plan plan = database.createPlan("Auditoría interna de procesos 2026", "Calidad", HOY, HOY.plusDays(30),
                coordinador);
        database.agregarActividad(plan.id(), "Auditar compras", responsable.id(), HOY.plusDays(2), coordinador);

        assertEquals(0, database.notificarVencimientos());
    }

    @Test
    @DisplayName("HU-015: con los correos desactivados el aviso llega solo en el sistema")
    void avisoSinCorreo() {
        database.cambiarFrecuenciaCorreo(responsable, FrecuenciaCorreo.DESACTIVADA);
        planAprobadoConActividad(HOY.plusDays(4));

        assertEquals(1, database.notificarVencimientos());

        assertEquals(1, avisosDe(responsable).size());
        assertTrue(enviados.isEmpty());
    }

    @Test
    @DisplayName("HU-015: con resumen diario el aviso espera al correo de las 7:00 del día siguiente")
    void avisoEnResumenDiario() {
        database.cambiarFrecuenciaCorreo(responsable, FrecuenciaCorreo.DIARIA);
        planAprobadoConActividad(HOY.plusDays(4));

        database.notificarVencimientos();

        assertTrue(enviados.isEmpty());
        CorreoSaliente resumen = database.listarCorreos(responsable).get(0);
        assertEquals(HOY.plusDays(1).atTime(DataService.HORA_RESUMEN_DIARIO), resumen.enviarDesde());
        assertTrue(resumen.cuerpo().contains("Aviso de vencimiento"));
    }

    /** Plan del área Calidad con una actividad del responsable, enviado a revisión y aprobado. */
    private Plan planAprobadoConActividad(LocalDate fechaLimite) {
        Plan plan = database.createPlan("Auditoría interna de procesos 2026", "Calidad", HOY, HOY.plusDays(30),
                coordinador);
        database.agregarActividad(plan.id(), "Auditar compras", responsable.id(), fechaLimite, coordinador);
        database.enviarARevision(plan.id(), coordinador);
        database.aprobarPlan(plan.id(), director);
        enviados.clear();
        return plan;
    }

    /** Avisos de vencimiento del usuario, del más reciente al más antiguo. */
    private List<String> avisosDe(User usuario) {
        return database.listarNotificaciones(usuario).stream()
                .map(Notificacion::mensaje)
                .filter(mensaje -> mensaje.contains("viso de vencimiento"))
                .toList();
    }
}
