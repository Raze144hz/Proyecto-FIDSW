package com.sgpi.ui;

import com.sgpi.data.DataService;
import com.sgpi.model.Actividad;
import com.sgpi.model.EstadoActividad;
import com.sgpi.model.EstadoPlan;
import com.sgpi.model.Plan;
import com.sgpi.model.Rol;
import com.sgpi.model.User;
import java.time.LocalDate;
import java.util.Objects;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
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
 * Ventana de actividades de un plan (HU-002 y HU-004).
 *
 * <ul>
 *   <li>El coordinador agrega o elimina actividades mientras el plan está en borrador o devuelto.</li>
 *   <li>Con el plan en ejecución, el coordinador o el responsable asignado registran el avance.</li>
 *   <li>La barra muestra el avance: actividades finalizadas sobre el total.</li>
 * </ul>
 */
public class VistaActividades {

    private final DataService database;
    private final User usuario;
    private final Plan plan;

    private final TableView<Actividad> tabla = new TableView<>();
    private final ProgressBar barra = new ProgressBar(0);
    private final Label avance = new Label();

    public VistaActividades(DataService database, User usuario, Plan plan) {
        this.database = Objects.requireNonNull(database);
        this.usuario = Objects.requireNonNull(usuario);
        this.plan = Objects.requireNonNull(plan);
    }

    /** Muestra la ventana y espera a que el usuario la cierre. */
    public void mostrar(Window propietario) {
        Stage ventana = new Stage();
        ventana.initOwner(propietario);
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.setTitle("Actividades del plan " + plan.codigo());

        Label titulo = new Label("Actividades del plan " + plan.codigo());
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label detalle = new Label(plan.objetivo() + "\nEstado del plan: " + plan.estado()
                + " | Vigencia: " + plan.fechaInicio() + " a " + plan.fechaFin());
        detalle.setWrapText(true);
        detalle.setMinHeight(Region.USE_PREF_SIZE);

        barra.setPrefWidth(300);
        barra.setId("barAvance");
        avance.setId("lblAvance");
        HBox filaAvance = new HBox(10, new Label("Avance:"), barra, avance);
        filaAvance.setAlignment(Pos.CENTER_LEFT);

        configurarTabla();

        VBox contenido = new VBox(12, titulo, detalle, filaAvance, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        boolean planeacion = Rol.gestionaPlanes(usuario.rol()) && EstadoPlan.editable(plan.estado());
        boolean ejecucion = EstadoPlan.EN_EJECUCION.equals(plan.estado());

        if (planeacion) {
            contenido.getChildren().add(crearFormularioNueva());
        }

        if (ejecucion) {
            contenido.getChildren().add(crearControlesAvance());
        }

        if (!planeacion && !ejecucion) {
            Label ayuda = new Label("Las actividades se agregan mientras el plan está en "
                    + EstadoPlan.BORRADOR + " o " + EstadoPlan.DEVUELTO
                    + ", y su avance se registra cuando el plan está " + EstadoPlan.EN_EJECUCION + ".");
            ayuda.setWrapText(true);
            contenido.getChildren().add(ayuda);
        }

        Button cerrar = new Button("Cerrar");
        cerrar.setCancelButton(true);
        cerrar.setOnAction(event -> ventana.close());
        HBox pie = new HBox(cerrar);
        pie.setAlignment(Pos.CENTER_RIGHT);
        contenido.getChildren().add(pie);

        contenido.setPadding(new Insets(20));

        refrescar();

        ventana.setScene(new Scene(contenido, 860, 600));
        ventana.showAndWait();
    }

    private void configurarTabla() {
        tabla.setId("tblActividades");

        TableColumn<Actividad, String> descripcion = new TableColumn<>("Actividad");
        descripcion.setCellValueFactory(dato -> new ReadOnlyStringWrapper(dato.getValue().descripcion()));

        TableColumn<Actividad, String> responsable = new TableColumn<>("Responsable");
        responsable.setCellValueFactory(dato -> new ReadOnlyStringWrapper(
                dato.getValue().responsable() == null ? "Sin asignar" : dato.getValue().responsable()));

        TableColumn<Actividad, String> fecha = new TableColumn<>("Fecha límite");
        fecha.setCellValueFactory(dato -> new ReadOnlyStringWrapper(dato.getValue().fechaLimite().toString()));

        TableColumn<Actividad, String> estado = new TableColumn<>("Estado");
        estado.setCellValueFactory(dato -> new ReadOnlyStringWrapper(dato.getValue().estado()));

        tabla.getColumns().addAll(descripcion, responsable, fecha, estado);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        descripcion.setMinWidth(280);
        tabla.setPlaceholder(new Label("El plan no tiene actividades registradas."));
    }

    private GridPane crearFormularioNueva() {
        TextField descripcion = new TextField();
        descripcion.setPromptText("Descripción de la actividad");
        descripcion.setId("txtActividad");

        ComboBox<User> responsable = new ComboBox<>();
        responsable.getItems().add(null);
        responsable.getItems().addAll(database.listarResponsables());
        responsable.setButtonCell(new CeldaUsuario());
        responsable.setCellFactory(lista -> new CeldaUsuario());
        responsable.setId("cmbResponsable");

        DatePicker fecha = new DatePicker(plan.fechaFin());
        fecha.setId("dpFechaLimite");

        Button agregar = new Button("Agregar actividad");
        agregar.setId("btnAgregarActividad");
        agregar.setOnAction(event -> ejecutar(() -> {
            User seleccionado = responsable.getValue();
            LocalDate limite = fecha.getValue();
            database.agregarActividad(plan.id(), descripcion.getText(),
                    seleccionado == null ? null : seleccionado.id(), limite, usuario);
            descripcion.clear();
            responsable.setValue(null);
        }));

        Button eliminar = new Button("Eliminar seleccionada");
        eliminar.setId("btnEliminarActividad");
        eliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        eliminar.setOnAction(event -> {
            Actividad actividad = tabla.getSelectionModel().getSelectedItem();
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Eliminar la actividad \"" + actividad.descripcion() + "\"?", ButtonType.OK, ButtonType.CANCEL);
            alerta.setHeaderText("Eliminar actividad");
            if (alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                ejecutar(() -> database.eliminarActividad(actividad.id(), usuario));
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Actividad"), descripcion);
        grid.addRow(1, new Label("Responsable"), responsable, new Label("Fecha límite"), fecha);
        grid.add(new HBox(10, agregar, eliminar), 1, 2, 3, 1);
        GridPane.setHgrow(descripcion, Priority.ALWAYS);
        GridPane.setColumnSpan(descripcion, 3);
        return grid;
    }

    private HBox crearControlesAvance() {
        ComboBox<String> estado = new ComboBox<>();
        estado.getItems().addAll(EstadoActividad.TODOS);
        estado.setValue(EstadoActividad.FINALIZADA);
        estado.setId("cmbEstadoActividad");

        Button actualizar = new Button("Actualizar estado de la seleccionada");
        actualizar.setId("btnActualizarActividad");
        actualizar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        actualizar.setOnAction(event -> {
            Actividad actividad = tabla.getSelectionModel().getSelectedItem();
            ejecutar(() -> database.cambiarEstadoActividad(actividad.id(), estado.getValue(), usuario));
        });

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox fila = new HBox(10, new Label("Nuevo estado"), estado, actualizar, espacio);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private void ejecutar(Runnable accion) {
        try {
            accion.run();
            refrescar();
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
        tabla.getItems().setAll(database.listarActividades(plan.id()));
        int porcentaje = database.calcularAvance(plan.id());
        long finalizadas = tabla.getItems().stream().filter(Actividad::finalizada).count();
        barra.setProgress(porcentaje / 100.0);
        avance.setText(porcentaje + " % (" + finalizadas + " de " + tabla.getItems().size() + " finalizadas)");
    }

    /** Muestra el nombre del usuario en las listas desplegables. */
    private static final class CeldaUsuario extends ListCell<User> {
        @Override
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : item == null ? "Sin asignar" : item.nombre());
        }
    }
}
