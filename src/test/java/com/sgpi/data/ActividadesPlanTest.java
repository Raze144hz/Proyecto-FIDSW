package com.sgpi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sgpi.model.Actividad;
import com.sgpi.model.EstadoActividad;
import com.sgpi.model.EstadoPlan;
import com.sgpi.model.Plan;
import com.sgpi.model.RegistroHistorial;
import com.sgpi.model.User;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas de HU-002 (actividades del plan) y HU-004 (avance por actividades). */
class ActividadesPlanTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 10, 15);
    private static final LocalDate FIN = LocalDate.of(2026, 12, 15);

    private DataService database;
    private User coordinador;
    private User director;
    private User responsable;

    @BeforeEach
    void prepararBaseDeDatos() {
        database = new DataService("jdbc:h2:mem:sgpi-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        database.init();

        coordinador = database.authenticate("coordinador@sgpi.local", "Coord123*").orElseThrow();
        director = database.authenticate("director@sgpi.local", "Dir123*").orElseThrow();
        responsable = database.authenticate("responsable@sgpi.local", "Resp123*").orElseThrow();
    }

    @Test
    @DisplayName("El coordinador agrega actividades a un plan en borrador")
    void agregarActividad() {
        Plan plan = crearPlan();

        Actividad actividad = database.agregarActividad(plan.id(), "  Levantar inventario de procesos  ",
                responsable.id(), LocalDate.of(2026, 11, 1), coordinador);

        assertEquals("Levantar inventario de procesos", actividad.descripcion());
        assertEquals(EstadoActividad.PENDIENTE, actividad.estado());
        assertEquals(responsable.nombre(), actividad.responsable());
        assertEquals(1, database.listarActividades(plan.id()).size());
    }

    @Test
    @DisplayName("Una actividad puede quedar sin responsable")
    void actividadSinResponsable() {
        Plan plan = crearPlan();

        Actividad actividad = database.agregarActividad(plan.id(), "Definir cronograma", null, FIN, coordinador);

        assertNull(database.listarActividades(plan.id()).get(0).responsableId());
        assertNull(actividad.responsable());
    }

    @Test
    @DisplayName("Se validan descripción, fecha dentro del plan y rol del responsable")
    void validacionesActividad() {
        Plan plan = crearPlan();

        assertThrows(IllegalArgumentException.class,
                () -> database.agregarActividad(plan.id(), "  ", null, FIN, coordinador));
        assertThrows(IllegalArgumentException.class,
                () -> database.agregarActividad(plan.id(), "Antes del plan", null, INICIO.minusDays(1), coordinador));
        assertThrows(IllegalArgumentException.class,
                () -> database.agregarActividad(plan.id(), "Después del plan", null, FIN.plusDays(1), coordinador));
        assertThrows(IllegalArgumentException.class,
                () -> database.agregarActividad(plan.id(), "Director como responsable", director.id(), FIN,
                        coordinador));
        assertThrows(IllegalStateException.class,
                () -> database.agregarActividad(plan.id(), "Sin permiso", null, FIN, responsable));
    }

    @Test
    @DisplayName("No se agregan ni eliminan actividades cuando el plan ya está en revisión")
    void actividadesBloqueadasEnRevision() {
        Plan plan = crearPlan();
        Actividad actividad = database.agregarActividad(plan.id(), "Actividad 1", null, FIN, coordinador);
        database.enviarARevision(plan.id(), coordinador);

        assertThrows(IllegalStateException.class,
                () -> database.agregarActividad(plan.id(), "Actividad 2", null, FIN, coordinador));
        assertThrows(IllegalStateException.class, () -> database.eliminarActividad(actividad.id(), coordinador));
    }

    @Test
    @DisplayName("CP-HU004: el avance es actividades finalizadas sobre el total")
    void calculoDeAvance() {
        Plan plan = crearPlan();
        assertEquals(0, database.calcularAvance(plan.id()));

        Actividad a1 = database.agregarActividad(plan.id(), "Actividad 1", responsable.id(), FIN, coordinador);
        Actividad a2 = database.agregarActividad(plan.id(), "Actividad 2", responsable.id(), FIN, coordinador);
        Actividad a3 = database.agregarActividad(plan.id(), "Actividad 3", null, FIN, coordinador);
        database.agregarActividad(plan.id(), "Actividad 4", null, FIN, coordinador);
        ponerEnEjecucion(plan);
        conEvidencia(a1);
        conEvidencia(a3);

        database.cambiarEstadoActividad(a1.id(), EstadoActividad.FINALIZADA, responsable);
        assertEquals(25, database.calcularAvance(plan.id()));

        database.cambiarEstadoActividad(a2.id(), EstadoActividad.EN_PROGRESO, responsable);
        assertEquals(25, database.calcularAvance(plan.id()), "Una actividad en progreso no cuenta como finalizada");

        database.cambiarEstadoActividad(a3.id(), EstadoActividad.FINALIZADA, coordinador);
        assertEquals(50, database.calcularAvance(plan.id()));
    }

    @Test
    @DisplayName("El avance solo se registra con el plan en ejecución")
    void avanceSoloEnEjecucion() {
        Plan plan = crearPlan();
        Actividad actividad = database.agregarActividad(plan.id(), "Actividad 1", responsable.id(), FIN, coordinador);

        assertThrows(IllegalStateException.class,
                () -> database.cambiarEstadoActividad(actividad.id(), EstadoActividad.FINALIZADA, coordinador));
    }

    @Test
    @DisplayName("Solo el coordinador o el responsable asignado actualizan una actividad")
    void permisosDeAvance() {
        Plan plan = crearPlan();
        Actividad sinAsignar = database.agregarActividad(plan.id(), "Sin asignar", null, FIN, coordinador);
        ponerEnEjecucion(plan);

        assertThrows(IllegalStateException.class,
                () -> database.cambiarEstadoActividad(sinAsignar.id(), EstadoActividad.FINALIZADA, responsable));
        assertThrows(IllegalStateException.class,
                () -> database.cambiarEstadoActividad(sinAsignar.id(), EstadoActividad.FINALIZADA, director));
        assertThrows(IllegalArgumentException.class,
                () -> database.cambiarEstadoActividad(sinAsignar.id(), "TERMINADA", coordinador));
    }

    @Test
    @DisplayName("Un plan con actividades pendientes no se finaliza; con el 100 % sí")
    void finalizarRequiereAvanceCompleto() {
        Plan plan = crearPlan();
        Actividad actividad = database.agregarActividad(plan.id(), "Actividad 1", responsable.id(), FIN, coordinador);
        ponerEnEjecucion(plan);

        assertThrows(IllegalStateException.class, () -> database.finalizarPlan(plan.id(), coordinador));

        conEvidencia(actividad);
        database.cambiarEstadoActividad(actividad.id(), EstadoActividad.FINALIZADA, responsable);
        database.finalizarPlan(plan.id(), coordinador);

        assertEquals(EstadoPlan.FINALIZADO, estadoDe(plan));
        assertEquals(100, database.calcularAvance(plan.id()));
    }

    @Test
    @DisplayName("Los cambios de actividades quedan en el historial del plan")
    void historialDeActividades() {
        Plan plan = crearPlan();
        Actividad actividad = database.agregarActividad(plan.id(), "Actividad 1", responsable.id(), FIN, coordinador);
        Actividad otra = database.agregarActividad(plan.id(), "Actividad 2", null, FIN, coordinador);
        database.eliminarActividad(otra.id(), coordinador);
        ponerEnEjecucion(plan);
        conEvidencia(actividad);
        database.cambiarEstadoActividad(actividad.id(), EstadoActividad.FINALIZADA, responsable);

        List<String> acciones = database.listarHistorial(plan.id()).stream().map(RegistroHistorial::accion).toList();

        assertTrue(acciones.contains("ACTIVIDAD_AGREGADA"));
        assertTrue(acciones.contains("ACTIVIDAD_ELIMINADA"));
        assertTrue(acciones.contains("AVANCE_ACTIVIDAD"));
    }

    @Test
    @DisplayName("Al eliminar un plan se eliminan sus actividades")
    void eliminarPlanEliminaActividades() {
        Plan plan = crearPlan();
        database.agregarActividad(plan.id(), "Actividad 1", null, FIN, coordinador);

        database.deletePlan(plan.id(), coordinador);

        assertTrue(database.listarActividades(plan.id()).isEmpty());
    }

    // ------------------------------------------------------------------ apoyo

    private Plan crearPlan() {
        return database.createPlan("Auditoría interna de procesos 2026", "Calidad", INICIO, FIN, coordinador);
    }

    private void ponerEnEjecucion(Plan plan) {
        database.enviarARevision(plan.id(), coordinador);
        database.aprobarPlan(plan.id(), director);
        database.iniciarEjecucion(plan.id(), coordinador);
    }

    /** RN-03: para finalizar una actividad hace falta al menos una evidencia (HU-003). */
    private void conEvidencia(Actividad actividad) {
        database.cargarEvidencia(actividad.id(), "soporte.pdf",
                "%PDF-1.4 soporte".getBytes(StandardCharsets.US_ASCII), coordinador);
    }

    private String estadoDe(Plan plan) {
        return database.listPlans().stream()
                .filter(p -> p.id() == plan.id())
                .findFirst()
                .orElseThrow()
                .estado();
    }
}
