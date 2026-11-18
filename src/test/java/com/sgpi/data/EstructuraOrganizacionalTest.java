package com.sgpi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sgpi.model.Area;
import com.sgpi.model.Plan;
import com.sgpi.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas de HU-010 (gestión de la estructura organizacional). */
class EstructuraOrganizacionalTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 10);
    private static final LocalDate FIN = LocalDate.of(2026, 12, 15);

    private DataService database;
    private User administrador;
    private User coordinador;

    @BeforeEach
    void prepararBaseDeDatos() {
        database = new DataService("jdbc:h2:mem:sgpi-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        database.init();

        administrador = database.authenticate("admin@sgpi.local", "Admin123*").orElseThrow();
        coordinador = database.authenticate("coordinador@sgpi.local", "Coord123*").orElseThrow();
    }

    @Test
    @DisplayName("Una base nueva trae las áreas iniciales activas")
    void areasIniciales() {
        List<String> nombres = database.listarAreasActivas().stream().map(Area::nombre).toList();

        assertTrue(nombres.contains("Calidad"));
        assertTrue(nombres.contains("Sistemas"));
    }

    @Test
    @DisplayName("Criterio 1: un área nueva con nombre y código queda habilitada para asignar planes")
    void areaNuevaHabilitaPlanes() {
        Area area = database.crearArea("fin", "  Gestión   Financiera ", administrador);

        assertEquals("FIN", area.codigo());
        assertEquals("Gestión Financiera", area.nombre());
        assertTrue(area.activa());

        Plan plan = database.createPlan("Reducir los tiempos de pago", "gestión financiera", INICIO, FIN,
                coordinador);
        assertEquals("Gestión Financiera", plan.area());
    }

    @Test
    @DisplayName("Un plan solo se crea con un área registrada")
    void areaNoRegistrada() {
        assertThrows(IllegalArgumentException.class,
                () -> database.createPlan("Objetivo", "Jurídica", INICIO, FIN, coordinador));
    }

    @Test
    @DisplayName("Solo el administrador gestiona las áreas")
    void soloAdministrador() {
        long calidad = buscar("Calidad").id();

        assertThrows(IllegalStateException.class, () -> database.crearArea("JUR", "Jurídica", coordinador));
        assertThrows(IllegalStateException.class,
                () -> database.actualizarArea(calidad, "CAL", "Otra", coordinador));
        assertThrows(IllegalStateException.class, () -> database.cambiarEstadoArea(calidad, false, coordinador));
        assertThrows(IllegalStateException.class, () -> database.eliminarArea(calidad, coordinador));
    }

    @Test
    @DisplayName("Se validan el código y el nombre, y no se repiten")
    void validaciones() {
        assertThrows(IllegalArgumentException.class, () -> database.crearArea(" ", "Jurídica", administrador));
        assertThrows(IllegalArgumentException.class, () -> database.crearArea("JU R", "Jurídica", administrador));
        assertThrows(IllegalArgumentException.class,
                () -> database.crearArea("CODIGOMUYLARGO", "Jurídica", administrador));
        assertThrows(IllegalArgumentException.class, () -> database.crearArea("JUR", " ", administrador));
        assertThrows(IllegalArgumentException.class, () -> database.crearArea("cal", "Jurídica", administrador));
        assertThrows(IllegalArgumentException.class, () -> database.crearArea("JUR", "calidad", administrador));
    }

    @Test
    @DisplayName("Criterio 2: un área con planes no se elimina y se sugiere desactivarla")
    void areaConPlanesNoSeElimina() {
        Area area = database.crearArea("JUR", "Jurídica", administrador);
        database.createPlan("Actualizar el normograma", "Jurídica", INICIO, FIN, coordinador);

        IllegalStateException error =
                assertThrows(IllegalStateException.class, () -> database.eliminarArea(area.id(), administrador));

        assertTrue(error.getMessage().contains("desactivarla"));
        assertEquals(1, buscar("Jurídica").planes());
    }

    @Test
    @DisplayName("Un área con usuarios activos no se elimina")
    void areaConUsuariosNoSeElimina() {
        Area calidad = buscar("Calidad");

        assertEquals(1, calidad.usuarios());
        assertThrows(IllegalStateException.class, () -> database.eliminarArea(calidad.id(), administrador));
    }

    @Test
    @DisplayName("Un área sin planes ni usuarios se elimina")
    void areaSinAsociadosSeElimina() {
        Area area = database.crearArea("JUR", "Jurídica", administrador);

        database.eliminarArea(area.id(), administrador);

        assertFalse(database.listarAreas().stream().anyMatch(a -> a.id() == area.id()));
    }

    @Test
    @DisplayName("Un área desactivada no se asigna a planes nuevos, pero sus planes la conservan")
    void areaDesactivada() {
        Area area = database.crearArea("JUR", "Jurídica", administrador);
        Plan plan = database.createPlan("Actualizar el normograma", "Jurídica", INICIO, FIN, coordinador);

        database.cambiarEstadoArea(area.id(), false, administrador);

        assertFalse(database.listarAreasActivas().stream().anyMatch(a -> a.id() == area.id()));
        assertThrows(IllegalArgumentException.class,
                () -> database.createPlan("Otro plan", "Jurídica", INICIO, FIN, coordinador));
        database.updatePlan(plan.id(), "Actualizar el normograma 2026", "Jurídica", INICIO, FIN, coordinador);

        Plan otro = database.createPlan("Otro plan", "Calidad", INICIO, FIN, coordinador);
        assertThrows(IllegalArgumentException.class,
                () -> database.updatePlan(otro.id(), "Otro plan", "Jurídica", INICIO, FIN, coordinador));

        database.cambiarEstadoArea(area.id(), true, administrador);
        database.createPlan("Otro plan", "Jurídica", INICIO, FIN, coordinador);
    }

    @Test
    @DisplayName("Al cambiar el nombre de un área, sus planes y su director la siguen teniendo")
    void renombrarArea() {
        Plan plan = database.createPlan("Auditoría interna", "Calidad", INICIO, FIN, coordinador);
        database.enviarARevision(plan.id(), coordinador);

        database.actualizarArea(buscar("Calidad").id(), "GCA", "Gestión de Calidad", administrador);

        assertEquals("Gestión de Calidad", database.listPlans().get(0).area());
        User director = database.authenticate("director@sgpi.local", "Dir123*").orElseThrow();
        assertEquals("Gestión de Calidad", director.area());
        assertEquals(1, database.listarPendientesRevision(director).size());
        assertEquals("GCA", buscar("Gestión de Calidad").codigo());
    }

    private Area buscar(String nombre) {
        return database.listarAreas().stream()
                .filter(area -> area.nombre().equals(nombre))
                .findFirst()
                .orElseThrow();
    }
}
