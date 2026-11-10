package com.sgpi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sgpi.model.EstadoPlan;
import com.sgpi.model.Plan;
import com.sgpi.model.RegistroHistorial;
import com.sgpi.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas de HU-002 (registro, edición y eliminación de planes) y de las correcciones de la capa de datos. */
class GestionPlanesTest {

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
    @DisplayName("El plan recibe un código único con el formato PA-año-consecutivo")
    void codigoUnico() {
        Plan primero = crear();
        Plan segundo = crear();

        assertTrue(primero.codigo().matches("PA-\\d{4}-\\d{4}"));
        assertTrue(!primero.codigo().equals(segundo.codigo()));
    }

    @Test
    @DisplayName("La capa de datos rechaza objetivo vacío, área vacía y fechas invertidas")
    void validacionesDelServidor() {
        assertThrows(IllegalArgumentException.class,
                () -> database.createPlan("  ", "Calidad", INICIO, FIN, coordinador));
        assertThrows(IllegalArgumentException.class,
                () -> database.createPlan("Objetivo", " ", INICIO, FIN, coordinador));
        assertThrows(IllegalArgumentException.class,
                () -> database.createPlan("Objetivo", "Calidad", FIN, INICIO, coordinador));
        assertThrows(IllegalArgumentException.class,
                () -> database.createPlan("x".repeat(501), "Calidad", INICIO, FIN, coordinador));
        assertTrue(database.listPlans().isEmpty());
    }

    @Test
    @DisplayName("El rol RESPONSABLE no crea, modifica ni elimina planes")
    void responsableSinPermisos() {
        Plan plan = crear();

        assertThrows(IllegalStateException.class,
                () -> database.createPlan("Objetivo", "Calidad", INICIO, FIN, responsable));
        assertThrows(IllegalStateException.class,
                () -> database.updatePlan(plan.id(), "Otro", "Calidad", INICIO, FIN, responsable));
        assertThrows(IllegalStateException.class, () -> database.deletePlan(plan.id(), responsable));
    }

    @Test
    @DisplayName("Corrección: un plan en revisión o aprobado no se puede eliminar")
    void noEliminarPlanEnRevision() {
        Plan plan = crear();
        database.enviarARevision(plan.id(), coordinador);

        assertThrows(IllegalStateException.class, () -> database.deletePlan(plan.id(), coordinador));

        database.aprobarPlan(plan.id(), director);
        assertThrows(IllegalStateException.class, () -> database.deletePlan(plan.id(), coordinador));
        assertEquals(1, database.listPlans().size());
    }

    @Test
    @DisplayName("Corrección: la edición y la eliminación quedan en el historial")
    void historialDeEdicionYEliminacion() {
        Plan plan = crear();
        database.updatePlan(plan.id(), "Objetivo corregido", "Calidad", INICIO, FIN, coordinador);
        database.deletePlan(plan.id(), coordinador);

        List<String> acciones = database.listarHistorial(plan.id()).stream().map(RegistroHistorial::accion).toList();

        assertEquals(List.of("CREACION", "MODIFICACION", "ELIMINACION"), acciones);
        assertTrue(database.listPlans().isEmpty());
    }

    @Test
    @DisplayName("Mejora: al enviar a revisión se notifica al director del área")
    void notificaAlDirector() {
        Plan plan = crear();

        database.enviarARevision(plan.id(), coordinador);

        assertEquals(1, database.contarNotificacionesSinLeer(director));
        assertTrue(database.listarNotificaciones(director).get(0).mensaje().contains(plan.codigo()));
    }

    @Test
    @DisplayName("Un plan de otra área no notifica al director de Calidad")
    void noNotificaDirectorDeOtraArea() {
        Plan plan = database.createPlan("Objetivo", "Sistemas", INICIO, FIN, coordinador);

        database.enviarARevision(plan.id(), coordinador);

        assertEquals(0, database.contarNotificacionesSinLeer(director));
        assertEquals(EstadoPlan.PENDIENTE_REVISION, database.listPlans().get(0).estado());
    }

    private Plan crear() {
        return database.createPlan("Auditoría interna de procesos 2026", "Calidad", INICIO, FIN, coordinador);
    }
}
