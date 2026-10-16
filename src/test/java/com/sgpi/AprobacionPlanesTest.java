package com.sgpi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sgpi.Main.EstadoPlan;
import com.sgpi.Main.Notificacion;
import com.sgpi.Main.Observacion;
import com.sgpi.Main.Plan;
import com.sgpi.Main.RegistroHistorial;
import com.sgpi.Main.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de las reglas de negocio de HU-017 (aprobación y retroalimentación de planes de acción).
 * Usan una base H2 en memoria, distinta para cada prueba.
 */
class AprobacionPlanesTest {

    private Main.DataService database;
    private User coordinador;
    private User director;
    private User responsable;

    @BeforeEach
    void prepararBaseDeDatos() {
        database = new Main.DataService("jdbc:h2:mem:sgpi-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        database.init();

        coordinador = database.authenticate("coordinador@sgpi.local", "Coord123*").orElseThrow();
        director = database.authenticate("director@sgpi.local", "Dir123*").orElseThrow();
        responsable = database.authenticate("responsable@sgpi.local", "Resp123*").orElseThrow();
    }

    @Test
    @DisplayName("Un plan nuevo empieza en BORRADOR")
    void planNuevoEmpiezaEnBorrador() {
        Plan plan = crearPlan("Calidad");

        assertEquals(EstadoPlan.BORRADOR, estadoDe(plan));
    }

    @Test
    @DisplayName("CP-HU017-01: el director aprueba un plan pendiente y sale de la lista de pendientes")
    void aprobarPlanPendiente() {
        Plan plan = crearPlanPendiente("Calidad");
        assertEquals(1, database.listarPendientesRevision(director).size());

        database.aprobarPlan(plan.id(), director);

        assertEquals(EstadoPlan.APROBADO, estadoDe(plan));
        assertTrue(database.listarPendientesRevision(director).isEmpty());
        assertEquals(1, database.contarNotificacionesSinLeer(coordinador));
    }

    @Test
    @DisplayName("CP-HU017-02: devolver con observaciones deja el plan EN REVISION y notifica al coordinador")
    void devolverConObservaciones() {
        Plan plan = crearPlanPendiente("Calidad");
        String texto = "Falta indicar qué procesos se auditarán.";

        database.devolverPlan(plan.id(), director, texto);

        assertEquals(EstadoPlan.DEVUELTO, estadoDe(plan));

        List<Observacion> observaciones = database.listarObservaciones(plan.id());
        assertEquals(1, observaciones.size());
        assertEquals(texto, observaciones.get(0).descripcion());
        assertEquals(director.nombre(), observaciones.get(0).autor());
        assertFalse(observaciones.get(0).atendida());

        List<Notificacion> notificaciones = database.listarNotificaciones(coordinador);
        assertEquals(1, notificaciones.size());
        assertTrue(notificaciones.get(0).mensaje().contains(texto));
    }

    @Test
    @DisplayName("CP-HU017-03: no se puede devolver un plan sin observaciones")
    void noSeDevuelveSinObservaciones() {
        Plan plan = crearPlanPendiente("Calidad");

        IllegalArgumentException vacia = assertThrows(IllegalArgumentException.class,
                () -> database.devolverPlan(plan.id(), director, ""));
        assertEquals("Debe escribir las observaciones para devolver el plan.", vacia.getMessage());

        assertThrows(IllegalArgumentException.class,
                () -> database.devolverPlan(plan.id(), director, "   "));

        assertEquals(EstadoPlan.PENDIENTE_REVISION, estadoDe(plan));
        assertTrue(database.listarObservaciones(plan.id()).isEmpty());
        assertEquals(0, database.contarNotificacionesSinLeer(coordinador));
    }

    @Test
    @DisplayName("CP-HU017-04: un plan devuelto no pasa a ejecución hasta atender las observaciones y aprobarlo de nuevo")
    void planDevueltoNoSeEjecutaHastaReaprobarlo() {
        Plan plan = crearPlanPendiente("Calidad");
        database.devolverPlan(plan.id(), director, "Ajustar las fechas del cronograma.");

        // Paso 1: intentar ejecutar el plan devuelto.
        assertThrows(IllegalStateException.class, () -> database.iniciarEjecucion(plan.id(), coordinador));
        assertFalse(database.puedeEjecutarse(plan.id()));

        // Tampoco se puede reenviar mientras la observación siga pendiente.
        assertThrows(IllegalStateException.class, () -> database.enviarARevision(plan.id(), coordinador));

        // Paso 2: marcar la observación como atendida.
        long observacion = database.listarObservaciones(plan.id()).get(0).id();
        database.marcarObservacionAtendida(observacion, coordinador);

        // Paso 3: intentar de nuevo sin que el director apruebe.
        assertThrows(IllegalStateException.class, () -> database.iniciarEjecucion(plan.id(), coordinador));
        assertFalse(database.puedeEjecutarse(plan.id()));

        // Solo después de reenviar y aprobar se puede ejecutar.
        database.enviarARevision(plan.id(), coordinador);
        database.aprobarPlan(plan.id(), director);
        assertTrue(database.puedeEjecutarse(plan.id()));

        database.iniciarEjecucion(plan.id(), coordinador);
        assertEquals(EstadoPlan.EN_EJECUCION, estadoDe(plan));
    }

    @Test
    @DisplayName("Solo el director de área puede aprobar o devolver planes")
    void soloElDirectorRevisa() {
        Plan plan = crearPlanPendiente("Calidad");

        assertThrows(IllegalStateException.class, () -> database.aprobarPlan(plan.id(), coordinador));
        assertThrows(IllegalStateException.class, () -> database.devolverPlan(plan.id(), responsable, "Texto"));
        assertThrows(IllegalStateException.class, () -> database.enviarARevision(plan.id(), responsable));

        assertEquals(EstadoPlan.PENDIENTE_REVISION, estadoDe(plan));
    }

    @Test
    @DisplayName("El director no revisa planes de otra área")
    void directorDeOtraArea() {
        Plan plan = crearPlanPendiente("Financiera");

        assertTrue(database.listarPendientesRevision(director).isEmpty());
        assertThrows(IllegalStateException.class, () -> database.aprobarPlan(plan.id(), director));
        assertThrows(IllegalStateException.class,
                () -> database.devolverPlan(plan.id(), director, "Revisar presupuesto."));

        assertEquals(EstadoPlan.PENDIENTE_REVISION, estadoDe(plan));
    }

    @Test
    @DisplayName("El área del director se compara sin importar mayúsculas ni espacios")
    void areaSinMayusculasNiEspacios() {
        Plan plan = crearPlanPendiente("  calidad ");

        assertEquals(1, database.listarPendientesRevision(director).size());
        database.aprobarPlan(plan.id(), director);

        assertEquals(EstadoPlan.APROBADO, estadoDe(plan));
    }

    @Test
    @DisplayName("Solo se aprueban o devuelven planes pendientes de revisión")
    void soloPlanesPendientes() {
        Plan plan = crearPlan("Calidad");

        assertThrows(IllegalStateException.class, () -> database.aprobarPlan(plan.id(), director));
        assertThrows(IllegalStateException.class,
                () -> database.devolverPlan(plan.id(), director, "Observación"));

        assertEquals(EstadoPlan.BORRADOR, estadoDe(plan));
    }

    @Test
    @DisplayName("Un plan en revisión del director no se puede modificar; uno devuelto sí")
    void edicionSegunEstado() {
        Plan plan = crearPlanPendiente("Calidad");

        assertThrows(IllegalStateException.class, () -> database.updatePlan(plan.id(), "Otro objetivo",
                "Calidad", plan.fechaInicio(), plan.fechaFin(), coordinador));

        database.devolverPlan(plan.id(), director, "Precisar el objetivo.");
        database.updatePlan(plan.id(), "Objetivo corregido", "Calidad",
                plan.fechaInicio(), plan.fechaFin(), coordinador);

        Plan actualizado = buscar(plan.id());
        assertEquals("Objetivo corregido", actualizado.objetivo());
        assertEquals(EstadoPlan.DEVUELTO, actualizado.estado());
    }

    @Test
    @DisplayName("Cada cambio de estado queda en el historial con el estado anterior y el nuevo")
    void historialDeCambios() {
        Plan plan = crearPlanPendiente("Calidad");
        database.devolverPlan(plan.id(), director, "Agregar responsables.");
        long observacion = database.listarObservaciones(plan.id()).get(0).id();
        database.marcarObservacionAtendida(observacion, coordinador);
        database.enviarARevision(plan.id(), coordinador);
        database.aprobarPlan(plan.id(), director);

        List<RegistroHistorial> historial = database.listarHistorial(plan.id());

        assertEquals(
                List.of("CREACION", "ENVIO_REVISION", "DEVOLUCION", "OBSERVACION_ATENDIDA",
                        "ENVIO_REVISION", "APROBACION"),
                historial.stream().map(RegistroHistorial::accion).toList());

        RegistroHistorial devolucion = historial.get(2);
        assertEquals(EstadoPlan.PENDIENTE_REVISION, devolucion.estadoAnterior());
        assertEquals(EstadoPlan.DEVUELTO, devolucion.estadoNuevo());
        assertEquals("Agregar responsables.", devolucion.detalle());
        assertEquals(director.nombre(), devolucion.usuario());
    }

    @Test
    @DisplayName("Una observación no se marca como atendida dos veces")
    void observacionAtendidaUnaVez() {
        Plan plan = crearPlanPendiente("Calidad");
        database.devolverPlan(plan.id(), director, "Corregir el área.");
        long observacion = database.listarObservaciones(plan.id()).get(0).id();

        database.marcarObservacionAtendida(observacion, coordinador);

        assertThrows(IllegalStateException.class,
                () -> database.marcarObservacionAtendida(observacion, coordinador));
    }

    // ------------------------------------------------------------------ apoyo

    private Plan crearPlan(String area) {
        return database.createPlan(
                "Auditoría interna de procesos 2026",
                area,
                LocalDate.of(2026, 10, 15),
                LocalDate.of(2026, 12, 15),
                coordinador);
    }

    private Plan crearPlanPendiente(String area) {
        Plan plan = crearPlan(area);
        database.enviarARevision(plan.id(), coordinador);
        return plan;
    }

    private Plan buscar(long id) {
        return database.listPlans().stream()
                .filter(plan -> plan.id() == id)
                .findFirst()
                .orElseThrow();
    }

    private String estadoDe(Plan plan) {
        return buscar(plan.id()).estado();
    }
}
