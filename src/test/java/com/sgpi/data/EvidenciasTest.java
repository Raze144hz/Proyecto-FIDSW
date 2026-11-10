package com.sgpi.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sgpi.model.Actividad;
import com.sgpi.model.EstadoActividad;
import com.sgpi.model.Evidencia;
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

/** Pruebas de HU-003 (carga de evidencias documentales) y de la regla RN-03. */
class EvidenciasTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 10, 15);
    private static final LocalDate FIN = LocalDate.of(2026, 12, 15);

    private static final byte[] PDF = "%PDF-1.4 acta de auditoria".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x01};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};
    private static final byte[] DOCX = {0x50, 0x4B, 0x03, 0x04, 0x14, 0x00};

    private DataService database;
    private User coordinador;
    private User director;
    private User responsable;
    private Plan plan;
    private Actividad asignada;
    private Actividad sinAsignar;

    @BeforeEach
    void prepararPlanEnEjecucion() {
        database = new DataService("jdbc:h2:mem:sgpi-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        database.init();

        coordinador = database.authenticate("coordinador@sgpi.local", "Coord123*").orElseThrow();
        director = database.authenticate("director@sgpi.local", "Dir123*").orElseThrow();
        responsable = database.authenticate("responsable@sgpi.local", "Resp123*").orElseThrow();

        plan = database.createPlan("Auditoría interna de procesos 2026", "Calidad", INICIO, FIN, coordinador);
        asignada = database.agregarActividad(plan.id(), "Auditar proceso de compras", responsable.id(), FIN,
                coordinador);
        sinAsignar = database.agregarActividad(plan.id(), "Consolidar informe", null, FIN, coordinador);
        database.enviarARevision(plan.id(), coordinador);
        database.aprobarPlan(plan.id(), director);
        database.iniciarEjecucion(plan.id(), coordinador);
    }

    @Test
    @DisplayName("HU-003: un archivo válido queda asociado a la actividad, que pasa a tener evidencia")
    void cargaValida() {
        Evidencia evidencia = database.cargarEvidencia(asignada.id(), "acta-compras.pdf", PDF, responsable);

        assertEquals("PDF", evidencia.formato());
        assertEquals(PDF.length, evidencia.tamano());
        assertEquals(responsable.nombre(), evidencia.cargadoPor());

        List<Evidencia> evidencias = database.listarEvidencias(asignada.id());
        assertEquals(1, evidencias.size());
        assertEquals("acta-compras.pdf", evidencias.get(0).nombreArchivo());
        assertTrue(actividad(asignada).conEvidencia());
        assertFalse(actividad(sinAsignar).conEvidencia());
        assertArrayEquals(PDF, database.descargarEvidencia(evidencia.id()));
    }

    @Test
    @DisplayName("HU-003: se aceptan PDF, DOCX, JPG y PNG")
    void formatosPermitidos() {
        database.cargarEvidencia(asignada.id(), "informe.docx", DOCX, coordinador);
        database.cargarEvidencia(asignada.id(), "foto.JPG", JPG, coordinador);
        database.cargarEvidencia(asignada.id(), "foto2.jpeg", JPG, coordinador);
        database.cargarEvidencia(asignada.id(), "captura.png", PNG, coordinador);

        assertEquals(4, actividad(asignada).evidencias());
    }

    @Test
    @DisplayName("HU-003: un formato no permitido se rechaza y el mensaje dice cuáles son válidos")
    void formatoNoPermitido() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> database.cargarEvidencia(asignada.id(), "notas.txt", PDF, coordinador));

        assertTrue(error.getMessage().contains(DataService.FORMATOS_VALIDOS));
        assertThrows(IllegalArgumentException.class,
                () -> database.cargarEvidencia(asignada.id(), "sin-extension", PDF, coordinador));
        assertTrue(database.listarEvidencias(asignada.id()).isEmpty());
    }

    @Test
    @DisplayName("HU-003: un archivo cuyo contenido no corresponde a la extensión se rechaza")
    void contenidoQueNoCorresponde() {
        byte[] ejecutable = {0x4D, 0x5A, (byte) 0x90, 0x00};

        assertThrows(IllegalArgumentException.class,
                () -> database.cargarEvidencia(asignada.id(), "acta.pdf", ejecutable, coordinador));
        assertThrows(IllegalArgumentException.class,
                () -> database.cargarEvidencia(asignada.id(), "foto.png", PDF, coordinador));
    }

    @Test
    @DisplayName("HU-003: los archivos vacíos o de más de 10 MB se rechazan")
    void tamano() {
        byte[] grande = new byte[DataService.TAMANO_MAXIMO_EVIDENCIA + 1];
        System.arraycopy(PDF, 0, grande, 0, PDF.length);
        byte[] limite = new byte[DataService.TAMANO_MAXIMO_EVIDENCIA];
        System.arraycopy(PDF, 0, limite, 0, PDF.length);

        assertThrows(IllegalArgumentException.class,
                () -> database.cargarEvidencia(asignada.id(), "grande.pdf", grande, coordinador));
        assertThrows(IllegalArgumentException.class,
                () -> database.cargarEvidencia(asignada.id(), "vacio.pdf", new byte[0], coordinador));

        database.cargarEvidencia(asignada.id(), "justo-10-mb.pdf", limite, coordinador);
        assertEquals(1, actividad(asignada).evidencias());
    }

    @Test
    @DisplayName("HU-003: solo el coordinador o el responsable asignado cargan evidencias")
    void permisos() {
        assertThrows(IllegalStateException.class,
                () -> database.cargarEvidencia(sinAsignar.id(), "acta.pdf", PDF, responsable));
        assertThrows(IllegalStateException.class,
                () -> database.cargarEvidencia(asignada.id(), "acta.pdf", PDF, director));
    }

    @Test
    @DisplayName("HU-003: las evidencias solo se cargan con el plan en ejecución")
    void soloEnEjecucion() {
        Plan borrador = database.createPlan("Capacitación en gestión documental", "Calidad", INICIO, FIN,
                coordinador);
        Actividad actividad = database.agregarActividad(borrador.id(), "Preparar material", null, FIN,
                coordinador);

        assertThrows(IllegalStateException.class,
                () -> database.cargarEvidencia(actividad.id(), "material.pdf", PDF, coordinador));
    }

    @Test
    @DisplayName("RN-03: una actividad sin evidencia no pasa a FINALIZADA; con evidencia sí")
    void finalizarRequiereEvidencia() {
        database.cambiarEstadoActividad(asignada.id(), EstadoActividad.EN_PROGRESO, responsable);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> database.cambiarEstadoActividad(asignada.id(), EstadoActividad.FINALIZADA, responsable));
        assertTrue(error.getMessage().contains("no tiene evidencias"));
        assertEquals(EstadoActividad.EN_PROGRESO, actividad(asignada).estado());

        database.cargarEvidencia(asignada.id(), "acta-compras.pdf", PDF, responsable);
        database.cambiarEstadoActividad(asignada.id(), EstadoActividad.FINALIZADA, responsable);

        assertEquals(EstadoActividad.FINALIZADA, actividad(asignada).estado());
        assertEquals(50, database.calcularAvance(plan.id()));
    }

    @Test
    @DisplayName("La carga de una evidencia queda en el historial del plan")
    void historial() {
        database.cargarEvidencia(asignada.id(), "acta-compras.pdf", PDF, responsable);

        List<RegistroHistorial> historial = database.listarHistorial(plan.id());

        assertTrue(historial.stream().anyMatch(registro -> "EVIDENCIA_CARGADA".equals(registro.accion())
                && registro.detalle().contains("acta-compras.pdf")));
    }

    private Actividad actividad(Actividad original) {
        return database.listarActividades(plan.id()).stream()
                .filter(actividad -> actividad.id() == original.id())
                .findFirst()
                .orElseThrow();
    }
}
