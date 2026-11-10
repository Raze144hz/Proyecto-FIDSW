package com.sgpi.ui;

import com.sgpi.data.DataService;
import com.sgpi.model.CorreoSaliente;
import com.sgpi.model.FrecuenciaCorreo;
import com.sgpi.model.User;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Preferencias de notificación del usuario (HU-015): frecuencia de los correos y lista de los correos que
 * el SGPI le generó, con su estado.
 */
public class VistaPreferencias {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DataService database;
    private final User usuario;

    private final TableView<CorreoSaliente> tabla = new TableView<>();
    private final TextArea cuerpo = new TextArea();

    public VistaPreferencias(DataService database, User usuario) {
        this.database = Objects.requireNonNull(database);
        this.usuario = Objects.requireNonNull(usuario);
    }

    /** Muestra la ventana y espera a que el usuario la cierre. */
    public void mostrar(Window propietario) {
        Stage ventana = new Stage();
        ventana.initOwner(propietario);
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.setTitle("Preferencias de notificación");

        Label titulo = new Label("Notificaciones por correo");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label correo = new Label("Los correos llegan a " + usuario.correo() + ". Las notificaciones dentro del "
                + "sistema (botón Notificaciones) llegan siempre, sin importar esta preferencia.");
        correo.setWrapText(true);
        correo.setMinHeight(Region.USE_PREF_SIZE);

        ComboBox<String> frecuencia = new ComboBox<>();
        frecuencia.setId("cmbFrecuenciaCorreo");
        frecuencia.getItems().setAll(FrecuenciaCorreo.TODAS);
        frecuencia.setCellFactory(lista -> new CeldaFrecuencia());
        frecuencia.setButtonCell(new CeldaFrecuencia());
        frecuencia.setValue(database.obtenerFrecuenciaCorreo(usuario));

        Button guardar = new Button("Guardar");
        guardar.setId("btnGuardarPreferencias");
        guardar.setOnAction(event -> {
            try {
                database.cambiarFrecuenciaCorreo(usuario, frecuencia.getValue());
                refrescar();
                Alert aviso = new Alert(Alert.AlertType.INFORMATION,
                        "Frecuencia de correo: " + FrecuenciaCorreo.descripcion(frecuencia.getValue()) + ".");
                aviso.setHeaderText("Preferencia guardada");
                aviso.showAndWait();
            } catch (RuntimeException exception) {
                Alert alerta = new Alert(Alert.AlertType.ERROR, exception.getMessage());
                alerta.setHeaderText("No fue posible guardar la preferencia");
                alerta.showAndWait();
            }
        });

        HBox filaFrecuencia = new HBox(10, new Label("Frecuencia:"), frecuencia, guardar);
        filaFrecuencia.setAlignment(Pos.CENTER_LEFT);

        VBox contenido = new VBox(12, titulo, correo, filaFrecuencia);

        if (!database.correoConfigurado()) {
            Label sinServidor = new Label("En este equipo no hay un servidor de correo configurado, así que los "
                    + "correos quedan pendientes en la lista de abajo. Para enviarlos, el administrador define las "
                    + "variables SGPI_SMTP_* (ver la guía de usuario).");
            sinServidor.setWrapText(true);
            sinServidor.setMinHeight(Region.USE_PREF_SIZE);
            sinServidor.setStyle("-fx-text-fill: #8a5a00;");
            contenido.getChildren().add(sinServidor);
        }

        configurarTabla();
        cuerpo.setEditable(false);
        cuerpo.setWrapText(true);
        cuerpo.setPrefRowCount(6);
        cuerpo.setId("txtCuerpoCorreo");

        Button cerrar = new Button("Cerrar");
        cerrar.setCancelButton(true);
        cerrar.setOnAction(event -> ventana.close());
        HBox pie = new HBox(cerrar);
        pie.setAlignment(Pos.CENTER_RIGHT);

        contenido.getChildren().addAll(new Label("Correos generados para usted"), tabla, cuerpo, pie);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        contenido.setPadding(new Insets(20));

        refrescar();

        ventana.setScene(new Scene(contenido, 780, 620));
        ventana.showAndWait();
    }

    private void configurarTabla() {
        tabla.setId("tblCorreos");

        TableColumn<CorreoSaliente, String> fecha = new TableColumn<>("Fecha");
        fecha.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().creadoEn().format(FORMATO_FECHA)));
        fecha.setPrefWidth(120);

        TableColumn<CorreoSaliente, String> asunto = new TableColumn<>("Asunto");
        asunto.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().asunto()));
        asunto.setPrefWidth(380);

        TableColumn<CorreoSaliente, String> estado = new TableColumn<>("Estado");
        estado.setCellValueFactory(celda -> new ReadOnlyStringWrapper(describirEstado(celda.getValue())));
        estado.setPrefWidth(220);

        tabla.getColumns().add(fecha);
        tabla.getColumns().add(asunto);
        tabla.getColumns().add(estado);
        tabla.setPlaceholder(new Label("Todavía no hay correos."));

        tabla.getSelectionModel().selectedItemProperty().addListener((observable, anterior, correo) ->
                cuerpo.setText(correo == null ? "" : correo.cuerpo()
                        + (correo.error() == null ? "" : "\n\nÚltimo error: " + correo.error())));
    }

    private static String describirEstado(CorreoSaliente correo) {
        if (correo.enviadoEn() != null) {
            return "Enviado el " + correo.enviadoEn().format(FORMATO_FECHA);
        }
        if (correo.error() != null) {
            return "Con error (se reintenta)";
        }
        if (correo.enviarDesde().isAfter(correo.creadoEn())) {
            return "En el resumen del " + correo.enviarDesde().format(FORMATO_FECHA);
        }
        return "Pendiente de envío";
    }

    private void refrescar() {
        tabla.getItems().setAll(database.listarCorreos(usuario));
        cuerpo.clear();
    }

    /** Muestra la descripción de cada frecuencia en la lista desplegable. */
    private static final class CeldaFrecuencia extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : FrecuenciaCorreo.descripcion(item));
        }
    }
}
