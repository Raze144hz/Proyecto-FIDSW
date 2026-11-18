package com.sgpi.ui;

import com.sgpi.data.DataService;
import com.sgpi.model.Area;
import com.sgpi.model.User;
import java.util.Objects;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Ventana de la estructura organizacional (HU-010). El administrador registra áreas con su código y su
 * nombre, las modifica, las activa o desactiva y elimina las que no tienen planes ni usuarios.
 */
public class VistaAreas {

    private final DataService database;
    private final User administrador;

    private final TableView<Area> tabla = new TableView<>();
    private final TextField codigo = new TextField();
    private final TextField nombre = new TextField();
    private final Label modo = new Label();
    private final Button guardar = new Button();
    private final Button cambiarEstado = new Button();
    private final Button eliminar = new Button("Eliminar");

    public VistaAreas(DataService database, User administrador) {
        this.database = Objects.requireNonNull(database);
        this.administrador = Objects.requireNonNull(administrador);
    }

    /** Muestra la ventana y espera a que el administrador la cierre. */
    public void mostrar(Window propietario) {
        Stage ventana = new Stage();
        ventana.initOwner(propietario);
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.setTitle("Estructura organizacional");

        Label titulo = new Label("Áreas y dependencias");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label ayuda = new Label("Solo las áreas activas se pueden asignar a planes nuevos. Un área con planes "
                + "o usuarios activos no se elimina: se desactiva.");
        ayuda.setWrapText(true);
        ayuda.setMinHeight(Region.USE_PREF_SIZE);

        configurarTabla();

        VBox contenido = new VBox(12, titulo, ayuda, tabla, crearFormulario());
        VBox.setVgrow(tabla, Priority.ALWAYS);

        Button cerrar = new Button("Cerrar");
        cerrar.setCancelButton(true);
        cerrar.setOnAction(event -> ventana.close());
        HBox pie = new HBox(cerrar);
        pie.setAlignment(Pos.CENTER_RIGHT);
        contenido.getChildren().add(pie);
        contenido.setPadding(new Insets(20));

        limpiar();
        refrescar();

        ventana.setScene(new Scene(contenido, 760, 560));
        ventana.showAndWait();
    }

    private void configurarTabla() {
        tabla.setId("tblAreas");

        TableColumn<Area, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().codigo()));

        TableColumn<Area, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().nombre()));
        colNombre.setPrefWidth(260);

        TableColumn<Area, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(
                celda -> new ReadOnlyStringWrapper(celda.getValue().activa() ? "Activa" : "Inactiva"));

        TableColumn<Area, Integer> colPlanes = new TableColumn<>("Planes");
        colPlanes.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().planes()));

        TableColumn<Area, Integer> colUsuarios = new TableColumn<>("Usuarios activos");
        colUsuarios.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().usuarios()));
        colUsuarios.setPrefWidth(120);

        tabla.getColumns().add(colCodigo);
        tabla.getColumns().add(colNombre);
        tabla.getColumns().add(colEstado);
        tabla.getColumns().add(colPlanes);
        tabla.getColumns().add(colUsuarios);
        tabla.setPlaceholder(new Label("No hay áreas registradas."));

        tabla.getSelectionModel().selectedItemProperty().addListener((observable, anterior, area) -> {
            if (area == null) {
                return;
            }
            codigo.setText(area.codigo());
            nombre.setText(area.nombre());
            modo.setText("Editando el área " + area.codigo());
            guardar.setText("Actualizar área");
            cambiarEstado.setText(area.activa() ? "Desactivar" : "Activar");
            cambiarEstado.setDisable(false);
            eliminar.setDisable(false);
        });
    }

    private VBox crearFormulario() {
        codigo.setId("txtCodigoArea");
        codigo.setPromptText("Ej.: CAL");
        codigo.setPrefColumnCount(8);
        nombre.setId("txtNombreArea");
        nombre.setPromptText("Ej.: Calidad");
        nombre.setPrefColumnCount(28);

        GridPane campos = new GridPane();
        campos.setHgap(10);
        campos.setVgap(8);
        campos.addRow(0, new Label("Código"), codigo);
        campos.addRow(1, new Label("Nombre"), nombre);

        modo.setStyle("-fx-font-weight: bold;");

        Button nueva = new Button("Nueva");
        nueva.setId("btnNuevaArea");
        nueva.setOnAction(event -> limpiar());

        guardar.setId("btnGuardarArea");
        guardar.setDefaultButton(true);
        guardar.setOnAction(event -> guardar());

        cambiarEstado.setId("btnEstadoArea");
        cambiarEstado.setOnAction(event -> {
            Area area = tabla.getSelectionModel().getSelectedItem();
            ejecutar(() -> database.cambiarEstadoArea(area.id(), !area.activa(), administrador));
        });

        eliminar.setId("btnEliminarArea");
        eliminar.setOnAction(event -> {
            Area area = tabla.getSelectionModel().getSelectedItem();
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Eliminar el área " + area.nombre() + "?", ButtonType.YES, ButtonType.NO);
            confirmacion.setHeaderText("Eliminar área");
            if (confirmacion.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                ejecutar(() -> database.eliminarArea(area.id(), administrador));
            }
        });

        HBox botones = new HBox(10, nueva, guardar, cambiarEstado, eliminar);
        botones.setAlignment(Pos.CENTER_LEFT);

        return new VBox(8, modo, campos, botones);
    }

    private void guardar() {
        Area seleccionada = tabla.getSelectionModel().getSelectedItem();
        ejecutar(() -> {
            if (seleccionada == null) {
                database.crearArea(codigo.getText(), nombre.getText(), administrador);
            } else {
                database.actualizarArea(seleccionada.id(), codigo.getText(), nombre.getText(), administrador);
            }
        });
    }

    private void limpiar() {
        tabla.getSelectionModel().clearSelection();
        codigo.clear();
        nombre.clear();
        modo.setText("Nueva área");
        guardar.setText("Crear área");
        cambiarEstado.setText("Desactivar");
        cambiarEstado.setDisable(true);
        eliminar.setDisable(true);
    }

    private void ejecutar(Runnable accion) {
        try {
            accion.run();
            refrescar();
            limpiar();
        } catch (RuntimeException exception) {
            String mensaje = exception.getMessage();
            if (mensaje == null && exception.getCause() != null) {
                mensaje = exception.getCause().getMessage();
            }
            Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje == null ? "Ocurrió un error." : mensaje);
            alerta.setHeaderText("No fue posible completar la acción");
            alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alerta.showAndWait();
        }
    }

    private void refrescar() {
        tabla.getItems().setAll(database.listarAreas());
    }
}
