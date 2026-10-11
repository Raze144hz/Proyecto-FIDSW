package com.sgpi;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Recorre los casos de prueba sobre la interfaz real del prototipo y guarda una captura de cada paso.
 * Se usó para generar las evidencias del acta de pruebas del Sprint 2 (ver README.md de esta carpeta).
 */
public class Evidencias {

    static Path salida;
    static PrintStream log;

    public static void main(String[] args) throws Exception {
        salida = Path.of(args[0]);
        Files.createDirectories(salida);
        log = new PrintStream(Files.newOutputStream(salida.resolve("registro-ejecucion.txt")), true, "UTF-8");

        new Thread(() -> Application.launch(Main.class), "lanzador").start();
        esperarVentana();

        try {
            recorrido();
            paso("FIN", "Recorrido completo sin errores del arnés.");
        } catch (Throwable t) {
            paso("ERROR DEL ARNÉS", t.toString());
            t.printStackTrace(log);
            throw t;
        } finally {
            Platform.exit();
        }
        System.exit(0);
    }

    static void recorrido() throws Exception {

        // ---------------------------------------------------------------- prototipo base
        captura("01-login", "Pantalla de inicio de sesión");

        login("coordinador@sgpi.local", "ClaveIncorrecta");
        paso("Login con contraseña incorrecta", "Mensaje: " + etiqueta(principalRoot(), "Usuario o contraseña incorrectos."));
        captura("02-login-error", "Login rechazado con contraseña incorrecta");

        login("coordinador@sgpi.local", "Coord123*");
        captura("03-coordinador-panel", "Panel del coordinador después de iniciar sesión");

        clic("btnGuardar");
        capturaDialogo("04-validacion-objetivo", "Crear plan sin objetivo");
        aceptarDialogo();

        texto("txtObjetivo", "Auditoría interna de los procesos del área de Calidad 2026");
        texto("txtArea", "Calidad");
        clic("btnGuardar");
        capturaDialogo("05-plan-creado", "Crear plan A (Calidad)");
        aceptarDialogo();

        texto("txtObjetivo", "Plan de mejora del proceso de compras institucionales");
        texto("txtArea", "Calidad");
        clic("btnGuardar");
        registrarDialogo("Crear plan B (Calidad)");
        aceptarDialogo();

        seleccionar("tblPlanes", "PA-2026-0001");
        clic("btnEnviarRevision");
        capturaDialogo("06-enviar-revision", "Enviar plan A a revisión");
        aceptarDialogo();

        seleccionar("tblPlanes", "PA-2026-0002");
        clic("btnEnviarRevision");
        registrarDialogo("Enviar plan B a revisión");
        aceptarDialogo();
        paso("Planes después de enviarlos", planes("tblPlanes"));

        // ---------------------------------------------------------------- director de área
        logout();
        login("director@sgpi.local", "Dir123*");
        paso("Pendientes del director", planes("tblPendientes"));
        seleccionar("tblPendientes", "PA-2026-0001");
        captura("07-director-pendientes", "Vista del director con los planes pendientes de su área");

        // CP-HU017-03
        clic("btnDevolver");
        paso("CP-HU017-03 · Devolver sin observaciones",
                "Mensaje junto al campo: " + textoDe("lblErrorObservacion")
                + " | ¿Abrió diálogo?: " + dialogo().isPresent()
                + " | Pendientes: " + planes("tblPendientes"));
        captura("08-cp-hu017-03-sin-observaciones", "Devolver sin escribir observaciones");

        // CP-HU017-02
        texto("txtObservacion", "Falta indicar qué procesos se auditarán.");
        clic("btnDevolver");
        capturaDialogo("09-cp-hu017-02-confirmar-devolucion", "Confirmar la devolución");
        aceptarDialogo();
        capturaDialogo("10-cp-hu017-02-plan-devuelto", "Resultado de la devolución");
        aceptarDialogo();

        // CP-HU017-01
        seleccionar("tblPendientes", "PA-2026-0002");
        clic("btnAprobar");
        capturaDialogo("11-cp-hu017-01-confirmar-aprobacion", "Confirmar la aprobación");
        aceptarDialogo();
        capturaDialogo("12-cp-hu017-01-plan-aprobado", "Resultado de la aprobación");
        aceptarDialogo();
        paso("CP-HU017-01 · Pendientes después de aprobar", planes("tblPendientes"));
        captura("13-cp-hu017-01-sin-pendientes", "Lista de pendientes después de aprobar y devolver");

        pestana(1);
        paso("Planes del área", planes("tblPlanesArea"));
        captura("14-director-planes-area", "Planes del área con su estado");

        // ---------------------------------------------------------------- coordinador: notificación y CP-HU017-04
        logout();
        login("coordinador@sgpi.local", "Coord123*");
        paso("Botón de notificaciones", textoDe("btnNotificaciones"));
        clic("btnNotificaciones");
        capturaDialogo("15-cp-hu017-02-notificaciones", "Notificaciones del coordinador");
        aceptarDialogo();

        seleccionar("tblPlanes", "PA-2026-0001");
        desplazarFormulario(1.0);
        paso("Observaciones del plan A", observaciones());
        captura("16-cp-hu017-04-observacion-pendiente", "Plan A devuelto con la observación pendiente");

        clic("btnIniciarEjecucion");
        capturaDialogo("17-cp-hu017-04-paso1-ejecucion-bloqueada", "CP-HU017-04 paso 1: iniciar ejecución del plan devuelto");
        aceptarDialogo();

        clic("btnEnviarRevision");
        capturaDialogo("18-cp-hu017-04-reenvio-bloqueado", "Reenviar con la observación sin atender");
        aceptarDialogo();

        seleccionarObservacion(0);
        clic("btnAtender");
        capturaDialogo("19-cp-hu017-04-paso2-observacion-atendida", "CP-HU017-04 paso 2: marcar la observación como atendida");
        aceptarDialogo();
        desplazarFormulario(1.0);
        paso("Observaciones del plan A", observaciones());

        clic("btnIniciarEjecucion");
        capturaDialogo("20-cp-hu017-04-paso3-ejecucion-bloqueada", "CP-HU017-04 paso 3: iniciar ejecución sin nueva aprobación");
        aceptarDialogo();

        clic("btnEnviarRevision");
        registrarDialogo("Reenviar plan A con la observación atendida");
        aceptarDialogo();

        logout();
        login("director@sgpi.local", "Dir123*");
        seleccionar("tblPendientes", "PA-2026-0001");
        desplazarPanelRevision();
        captura("21-director-reevalua", "El director ve la observación atendida antes de aprobar de nuevo");
        clic("btnAprobar");
        registrarDialogo("Confirmar la nueva aprobación del plan A");
        aceptarDialogo();
        registrarDialogo("Resultado de la nueva aprobación del plan A");
        aceptarDialogo();

        logout();
        login("coordinador@sgpi.local", "Coord123*");
        seleccionar("tblPlanes", "PA-2026-0001");
        desplazarFormulario(1.0);
        clic("btnIniciarEjecucion");
        capturaDialogo("22-cp-hu017-04-ejecucion-permitida", "Iniciar ejecución después de atender y reaprobar");
        aceptarDialogo();

        clic("btnHistorial");
        capturaDialogo("23-historial-plan", "Historial de cambios de estado del plan A");
        aceptarDialogo();

        seleccionar("tblPlanes", "PA-2026-0002");
        desplazarFormulario(0.0);
        texto("txtObjetivo", "Objetivo modificado después de aprobado");
        clic("btnGuardar");
        capturaDialogo("24-edicion-bloqueada", "Modificar un plan aprobado");
        aceptarDialogo();
        paso("Planes al final (coordinador)", planes("tblPlanes"));

        // ---------------------------------------------------------------- rol de consulta
        logout();
        login("responsable@sgpi.local", "Resp123*");
        captura("25-responsable-consulta", "Rol RESPONSABLE en modo consulta");
        paso("Rol RESPONSABLE", "¿Tiene formulario de edición?: " + (nodo("btnGuardar") != null)
                + " | ¿Tiene botón Aprobar?: " + (nodo("btnAprobar") != null));
    }

    // ------------------------------------------------------------------ acciones

    static void login(String correo, String clave) throws Exception {
        texto("txtCorreo", correo);
        texto("txtClave", clave);
        clic("btnIngresar");
        pausa(900);
    }

    static void logout() throws Exception {
        clic("btnCerrarSesion");
        pausa(700);
    }

    static void texto(String id, String valor) throws Exception {
        fx(() -> {
            ((TextInputControl) buscar(id)).setText(valor);
            return null;
        });
        pausa(200);
    }

    static void clic(String id) throws Exception {
        Node nodo = nodo(id);
        if (nodo == null) {
            throw new IllegalStateException("No existe el control " + id);
        }
        Platform.runLater(() -> ((ButtonBase) nodo).fire());
        pausa(900);
    }

    @SuppressWarnings("unchecked")
    static void seleccionar(String tablaId, String codigo) throws Exception {
        boolean ok = fx(() -> {
            TableView<Main.Plan> tabla = (TableView<Main.Plan>) buscar(tablaId);
            Optional<Main.Plan> plan = tabla.getItems().stream().filter(p -> codigo.equals(p.codigo())).findFirst();
            plan.ifPresent(p -> {
                tabla.getSelectionModel().select(p);
                tabla.scrollTo(p);
            });
            return plan.isPresent();
        });
        if (!ok) {
            throw new IllegalStateException("No se encontró " + codigo + " en " + tablaId);
        }
        pausa(400);
    }

    static void seleccionarObservacion(int indice) throws Exception {
        fx(() -> {
            ((ListView<?>) buscar("lstObservaciones")).getSelectionModel().select(indice);
            return null;
        });
        pausa(300);
    }

    static void pestana(int indice) throws Exception {
        fx(() -> {
            ((TabPane) buscar("tabsRevision")).getSelectionModel().select(indice);
            return null;
        });
        pausa(500);
    }

    static void desplazarFormulario(double valor) throws Exception {
        fx(() -> {
            BorderPane root = (BorderPane) principal().getScene().getRoot();
            ((ScrollPane) root.getRight()).setVvalue(valor);
            return null;
        });
        pausa(400);
    }

    static void desplazarPanelRevision() throws Exception {
        fx(() -> {
            for (Node n : principal().getScene().getRoot().lookupAll(".scroll-pane")) {
                if (n instanceof ScrollPane sp && sp.getContent() != null
                        && sp.getContent().lookup("#btnAprobar") != null) {
                    sp.setVvalue(0.0);
                }
            }
            return null;
        });
        pausa(300);
    }

    @SuppressWarnings("unchecked")
    static String planes(String tablaId) throws Exception {
        return fx(() -> {
            TableView<Main.Plan> tabla = (TableView<Main.Plan>) buscar(tablaId);
            if (tabla.getItems().isEmpty()) {
                return "(sin planes)";
            }
            StringBuilder sb = new StringBuilder();
            for (Main.Plan p : tabla.getItems()) {
                sb.append(p.codigo()).append("=").append(p.estado()).append("; ");
            }
            return sb.toString();
        });
    }

    static String observaciones() throws Exception {
        return fx(() -> {
            ListView<?> lista = (ListView<?>) buscar("lstObservaciones");
            StringBuilder sb = new StringBuilder();
            for (Object o : lista.getItems()) {
                Main.Observacion obs = (Main.Observacion) o;
                sb.append(obs.atendida() ? "[Atendida] " : "[Pendiente] ").append(obs.descripcion()).append("; ");
            }
            return sb.length() == 0 ? "(sin observaciones)" : sb.toString();
        });
    }

    static String textoDe(String id) throws Exception {
        return fx(() -> {
            Node n = buscar(id);
            if (n instanceof Label l) {
                return l.getText();
            }
            if (n instanceof Button b) {
                return b.getText();
            }
            return String.valueOf(n);
        });
    }

    static String etiqueta(Node raiz, String esperado) throws Exception {
        return fx(() -> {
            for (Node n : raiz.lookupAll(".label")) {
                if (n instanceof Label l && esperado.equals(l.getText())) {
                    return l.getText();
                }
            }
            return "(no se encontró el mensaje)";
        });
    }

    static Node principalRoot() throws Exception {
        return fx(() -> principal().getScene().getRoot());
    }

    // ------------------------------------------------------------------ diálogos

    static Optional<DialogPane> dialogo() throws Exception {
        return fx(() -> {
            for (Window w : Window.getWindows()) {
                if (w.isShowing() && w.getScene() != null && w.getScene().getRoot() instanceof DialogPane dp) {
                    return Optional.of(dp);
                }
            }
            return Optional.empty();
        });
    }

    static DialogPane esperarDialogo() throws Exception {
        for (int i = 0; i < 60; i++) {
            Optional<DialogPane> d = dialogo();
            if (d.isPresent()) {
                return d.get();
            }
            pausa(200);
        }
        throw new IllegalStateException("No apareció el diálogo esperado.");
    }

    static String textoDialogo(DialogPane dp) throws Exception {
        return fx(() -> {
            String contenido = dp.getContent() instanceof TextArea ta ? ta.getText() : dp.getContentText();
            return "[" + ((Stage) dp.getScene().getWindow()).getTitle() + "] "
                    + (dp.getHeaderText() == null ? "" : dp.getHeaderText() + " | ")
                    + contenido;
        });
    }

    static void registrarDialogo(String descripcion) throws Exception {
        DialogPane dp = esperarDialogo();
        paso(descripcion, textoDialogo(dp));
    }

    static void capturaDialogo(String nombre, String descripcion) throws Exception {
        DialogPane dp = esperarDialogo();
        paso(descripcion, textoDialogo(dp));
        pausa(500);
        capturar(nombre, true);
    }

    static void aceptarDialogo() throws Exception {
        DialogPane dp = esperarDialogo();
        Window ventana = fx(() -> dp.getScene().getWindow());
        Platform.runLater(() -> {
            Node ok = dp.lookupButton(ButtonType.OK);
            ((Button) ok).fire();
        });
        for (int i = 0; i < 50; i++) {
            pausa(150);
            if (fx(() -> !ventana.isShowing())) {
                pausa(600);
                return;
            }
        }
        throw new IllegalStateException("El diálogo no se cerró.");
    }

    // ------------------------------------------------------------------ capturas

    static void captura(String nombre, String descripcion) throws Exception {
        paso(descripcion, "captura " + nombre + ".png");
        pausa(500);
        capturar(nombre, false);
    }

    static void capturar(String nombre, boolean conDialogo) throws Exception {
        double[] b = fx(() -> {
            Stage s = principal();
            double x1 = s.getX(), y1 = s.getY(), x2 = s.getX() + s.getWidth(), y2 = s.getY() + s.getHeight();
            if (conDialogo) {
                for (Window w : Window.getWindows()) {
                    if (w.isShowing() && w != s) {
                        x1 = Math.min(x1, w.getX());
                        y1 = Math.min(y1, w.getY());
                        x2 = Math.max(x2, w.getX() + w.getWidth());
                        y2 = Math.max(y2, w.getY() + w.getHeight());
                    }
                }
            }
            return new double[] {x1, y1, x2 - x1, y2 - y1};
        });
        String geometria = String.format("%dx%d+%d+%d",
                Math.round(b[2]), Math.round(b[3]), Math.round(b[0]), Math.round(b[1]));
        Process p = new ProcessBuilder("import", "-window", "root", "-crop", geometria, "+repage",
                salida.resolve(nombre + ".png").toString()).inheritIO().start();
        if (!p.waitFor(20, TimeUnit.SECONDS) || p.exitValue() != 0) {
            throw new IllegalStateException("Falló la captura " + nombre);
        }
        log.println("    -> " + nombre + ".png (" + geometria + ")");
    }

    // ------------------------------------------------------------------ utilidades FX

    static Stage principal() {
        for (Window w : Window.getWindows()) {
            if (w instanceof Stage s && s.isShowing() && s.getScene() != null
                    && !(s.getScene().getRoot() instanceof DialogPane)) {
                return s;
            }
        }
        throw new IllegalStateException("No hay ventana principal.");
    }

    static Node buscar(String id) {
        Node n = principal().getScene().lookup("#" + id);
        if (n == null) {
            throw new IllegalStateException("No existe el control " + id);
        }
        return n;
    }

    static Node nodo(String id) throws Exception {
        return fx(() -> principal().getScene().lookup("#" + id));
    }

    static <T> T fx(Callable<T> tarea) throws Exception {
        CompletableFuture<T> futuro = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                futuro.complete(tarea.call());
            } catch (Throwable t) {
                futuro.completeExceptionally(t);
            }
        });
        return futuro.get(20, TimeUnit.SECONDS);
    }

    static void esperarVentana() throws Exception {
        for (int i = 0; i < 100; i++) {
            pausa(200);
            try {
                if (fx(() -> {
                    principal();
                    return true;
                })) {
                    pausa(1000);
                    return;
                }
            } catch (Exception ignorada) {
                // la plataforma aún no arranca
            }
        }
        throw new IllegalStateException("La aplicación no abrió.");
    }

    static void paso(String descripcion, String resultado) {
        log.println("- " + descripcion + ": " + resultado);
        System.out.println("- " + descripcion + ": " + resultado);
    }

    static void pausa(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}
