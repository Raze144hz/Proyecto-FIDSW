package com.sgpi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class Main extends Application {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DataService database = new DataService();
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        database.init();

        stage.setTitle(
                "SGPI - Sistema de Gestión de Procesos Institucionales"
        );

        mostrarLogin();

        stage.show();
    }

    private void mostrarLogin() {

        Label titulo = new Label("SGPI");

        titulo.setStyle(
                "-fx-font-size: 32px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitulo =
                new Label(
                        "Sistema de Gestión de Procesos Institucionales"
                );

        TextField correo =
                new TextField();

        correo.setPromptText(
                "Correo institucional"
        );

        correo.setId("txtCorreo");

        PasswordField clave =
                new PasswordField();

        clave.setPromptText(
                "Contraseña"
        );

        clave.setId("txtClave");

        Button ingresar =
                new Button(
                        "Iniciar sesión"
                );

        ingresar.setDefaultButton(true);

        ingresar.setId("btnIngresar");

        Label mensaje =
                new Label();

        ingresar.setOnAction(event -> {

            String email =
                    correo
                            .getText()
                            .trim();

            String password =
                    clave
                            .getText();

            if (
                    email.isBlank()
                    ||
                    password.isBlank()
            ) {

                mensaje.setText(
                        "Ingrese correo y contraseña."
                );

                return;
            }

            Optional<User> usuario =
                    database.authenticate(
                            email,
                            password
                    );

            if (
                    usuario.isPresent()
            ) {

                mostrarDashboard(
                        usuario.get()
                );

            } else {

                mensaje.setText(
                        "Usuario o contraseña incorrectos."
                );
            }
        });

        VBox formulario =
                new VBox(
                        12,
                        titulo,
                        subtitulo,
                        new Label("Correo"),
                        correo,
                        new Label("Contraseña"),
                        clave,
                        ingresar,
                        mensaje
                );

        formulario.setPadding(
                new Insets(30)
        );

        formulario.setMaxWidth(
                420
        );

        formulario.setAlignment(
                Pos.CENTER_LEFT
        );

        StackPane root =
                new StackPane(
                        formulario
                );

        root.setPadding(
                new Insets(40)
        );

        stage.setScene(
                new Scene(
                        root,
                        700,
                        500
                )
        );

        stage.centerOnScreen();
    }

    private void mostrarDashboard(
            User usuario
    ) {

        BorderPane root =
                new BorderPane();

        root.setPadding(
                new Insets(20)
        );

        Label titulo =
                new Label("SGPI");

        titulo.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;"
        );

        Label sesion =
                new Label(
                        "Sesión: "
                        + usuario.nombre()
                        + " | Rol: "
                        + usuario.rol()
                        + (usuario.area() == null
                                ? ""
                                : " | Área: " + usuario.area())
                );

        Button cerrarSesion =
                new Button(
                        "Cerrar sesión"
                );

        cerrarSesion.setOnAction(
                event ->
                        mostrarLogin()
        );

        cerrarSesion.setId("btnCerrarSesion");

        // HU-017: el coordinador recibe una notificación cuando el director aprueba o devuelve su plan.
        Button notificaciones = new Button();
        notificaciones.setId("btnNotificaciones");

        Runnable actualizarNotificaciones = () -> notificaciones.setText(
                "Notificaciones (" + database.contarNotificacionesSinLeer(usuario) + ")");

        actualizarNotificaciones.run();

        notificaciones.setOnAction(event -> {
            mostrarNotificaciones(usuario);
            actualizarNotificaciones.run();
        });

        Region espacio =
                new Region();

        HBox.setHgrow(
                espacio,
                Priority.ALWAYS
        );

        HBox topbar =
                new HBox(
                        15,
                        titulo,
                        espacio,
                        sesion,
                        notificaciones,
                        cerrarSesion
                );

        topbar.setAlignment(
                Pos.CENTER_LEFT
        );

        topbar.setPadding(
                new Insets(
                        0,
                        0,
                        20,
                        0
                )
        );

        root.setTop(
                topbar
        );

        if (Rol.DIRECTOR.equals(usuario.rol())) {

            // HU-017: el director de área revisa, aprueba o devuelve los planes de su área.
            root.setCenter(crearVistaRevision(usuario));

            stage.setScene(new Scene(root, 1200, 700));
            stage.centerOnScreen();
            return;
        }

        TableView<Plan> tabla =
                crearTablaPlanes();

        tabla.setId("tblPlanes");

        ObservableList<Plan> planes =
                FXCollections
                        .observableArrayList();

        tabla.setItems(
                planes
        );

        Runnable actualizarTabla =
                () -> planes.setAll(
                        database.listPlans()
                );

        actualizarTabla.run();

        Label tituloPlanes =
                new Label(
                        "Planes de acción"
                );

        tituloPlanes.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        Label cantidad =
                new Label();

        Runnable actualizarCantidad =
                () -> cantidad.setText(
                        "Total de planes: "
                        + planes.size()
                );

        actualizarCantidad.run();

        VBox centro =
                new VBox(
                        10,
                        tituloPlanes,
                        cantidad,
                        tabla
                );

        VBox.setVgrow(
                tabla,
                Priority.ALWAYS
        );

        root.setCenter(
                centro
        );

        boolean puedeEditar =
                usuario.rol().equals("ADMIN")
                ||
                usuario.rol().equals("COORDINADOR");

        if (
                puedeEditar
        ) {

            VBox formulario =
                    crearFormularioCrud(
                            tabla,
                            usuario,
                            () -> {

                                actualizarTabla.run();
                                actualizarCantidad.run();

                            }
                    );

            formulario.setPrefWidth(
                    330
            );

            formulario.setPadding(
                    new Insets(
                            0,
                            0,
                            0,
                            25
                    )
            );

            ScrollPane desplazamiento = new ScrollPane(formulario);
            desplazamiento.setFitToWidth(true);
            desplazamiento.setPrefWidth(375);
            desplazamiento.setStyle("-fx-background-color: transparent;");

            root.setRight(
                    desplazamiento
            );

        } else {

            Label permisos =
                    new Label(
                            "Modo consulta\n\n"
                            +
                            "El rol RESPONSABLE puede consultar "
                            +
                            "los planes registrados, pero no "
                            +
                            "crear, modificar ni eliminar."
                    );

            permisos.setWrapText(
                    true
            );

            permisos.setPrefWidth(
                    250
            );

            permisos.setPadding(
                    new Insets(20)
            );

            root.setRight(
                    permisos
            );
        }

        stage.setScene(
                new Scene(
                        root,
                        1200,
                        700
                )
        );

        stage.centerOnScreen();
    }

    private TableView<Plan> crearTablaPlanes() {

        TableView<Plan> tabla =
                new TableView<>();

        TableColumn<Plan, String> codigo =
                new TableColumn<>(
                        "Código"
                );

        codigo.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .codigo()
                        )
        );

        TableColumn<Plan, String> objetivo =
                new TableColumn<>(
                        "Objetivo"
                );

        objetivo.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .objetivo()
                        )
        );

        TableColumn<Plan, String> area =
                new TableColumn<>(
                        "Área"
                );

        area.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .area()
                        )
        );

        TableColumn<Plan, String> inicio =
                new TableColumn<>(
                        "Inicio"
                );

        inicio.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .fechaInicio()
                                        .toString()
                        )
        );

        TableColumn<Plan, String> fin =
                new TableColumn<>(
                        "Fin"
                );

        fin.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .fechaFin()
                                        .toString()
                        )
        );

        TableColumn<Plan, String> estado =
                new TableColumn<>(
                        "Estado"
                );

        estado.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .estado()
                        )
        );

        tabla.getColumns().addAll(
                codigo,
                objetivo,
                area,
                inicio,
                fin,
                estado
        );

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        // Más espacio para el objetivo y para los estados largos de HU-017 (PENDIENTE DE REVISION).
        codigo.setMinWidth(105);
        objetivo.setMinWidth(150);
        area.setMinWidth(90);
        inicio.setMinWidth(90);
        fin.setMinWidth(90);
        estado.setMinWidth(175);

        tabla.setPlaceholder(
                new Label(
                        "No existen planes registrados."
                )
        );

        return tabla;
    }

    private VBox crearFormularioCrud(
            TableView<Plan> tabla,
            User usuario,
            Runnable refrescar
    ) {

        SimpleObjectProperty<Plan> seleccionado =
                new SimpleObjectProperty<>();

        Label titulo =
                new Label(
                        "Gestión del plan"
                );

        titulo.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        Label modo =
                new Label(
                        "Nuevo plan"
                );

        modo.setStyle(
                "-fx-font-weight: bold;"
        );

        TextArea objetivo =
                new TextArea();

        objetivo.setPromptText(
                "Objetivo del plan"
        );

        objetivo.setPrefRowCount(
                4
        );

        objetivo.setId("txtObjetivo");

        TextField area =
                new TextField();

        area.setPromptText(
                "Área responsable"
        );

        area.setId("txtArea");

        DatePicker fechaInicio =
                new DatePicker(
                        LocalDate.now()
                );

        DatePicker fechaFin =
                new DatePicker(
                        LocalDate.now()
                                .plusDays(30)
                );

        // HU-017: el estado ya no se escoge a mano; cambia con las acciones del flujo de aprobación.
        Label estado = new Label();
        estado.setId("lblEstado");
        estado.setStyle("-fx-font-weight: bold;");

        Label tituloFlujo = new Label("Aprobación del plan (HU-017)");
        tituloFlujo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button enviarRevision = new Button("Enviar a revisión del director");
        enviarRevision.setId("btnEnviarRevision");

        Button iniciarEjecucion = new Button("Iniciar ejecución");
        iniciarEjecucion.setId("btnIniciarEjecucion");

        Button finalizar = new Button("Finalizar plan");
        finalizar.setId("btnFinalizar");

        Button historial = new Button("Ver historial del plan");
        historial.setId("btnHistorial");

        ListView<Observacion> observaciones = new ListView<>();
        observaciones.setId("lstObservaciones");
        configurarListaObservaciones(observaciones);

        Button atender = new Button("Marcar observación como atendida");
        atender.setId("btnAtender");

        for (Button boton : List.of(enviarRevision, iniciarEjecucion, finalizar, historial, atender)) {
            boton.setMaxWidth(Double.MAX_VALUE);
        }

        Runnable cargarObservaciones = () -> {
            Plan plan = seleccionado.get();
            observaciones.getItems().setAll(
                    plan == null ? List.<Observacion>of() : database.listarObservaciones(plan.id()));
        };

        Button guardar =
                new Button(
                        "Guardar"
                );

        guardar.setId("btnGuardar");

        Button nuevo =
                new Button(
                        "Nuevo"
                );

        nuevo.setId("btnNuevo");

        Button eliminar =
                new Button(
                        "Eliminar"
                );

        guardar.setMaxWidth(
                Double.MAX_VALUE
        );

        nuevo.setMaxWidth(
                Double.MAX_VALUE
        );

        eliminar.setMaxWidth(
                Double.MAX_VALUE
        );

        eliminar.setDisable(
                true
        );

        Runnable limpiarFormulario =
                () -> {

                    seleccionado.set(
                            null
                    );

                    tabla.getSelectionModel()
                            .clearSelection();

                    objetivo.clear();

                    area.clear();

                    fechaInicio.setValue(
                            LocalDate.now()
                    );

                    fechaFin.setValue(
                            LocalDate.now()
                                    .plusDays(30)
                    );

                    estado.setText(
                            "Estado: " + EstadoPlan.BORRADOR + " (al crear el plan)"
                    );

                    observaciones.getItems().clear();

                    deshabilitar(true, enviarRevision, iniciarEjecucion, finalizar, historial, atender);

                    modo.setText(
                            "Nuevo plan"
                    );

                    guardar.setText(
                            "Crear plan"
                    );

                    eliminar.setDisable(
                            true
                    );
                };

        limpiarFormulario.run();

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                anterior,
                                plan
                        ) -> {

                            if (
                                    plan == null
                            ) {
                                return;
                            }

                            seleccionado.set(
                                    plan
                            );

                            objetivo.setText(
                                    plan.objetivo()
                            );

                            area.setText(
                                    plan.area()
                            );

                            fechaInicio.setValue(
                                    plan.fechaInicio()
                            );

                            fechaFin.setValue(
                                    plan.fechaFin()
                            );

                            estado.setText(
                                    "Estado: " + plan.estado()
                            );

                            cargarObservaciones.run();

                            deshabilitar(false, enviarRevision, iniciarEjecucion, finalizar, historial, atender);

                            modo.setText(
                                    "Editando "
                                    + plan.codigo()
                            );

                            guardar.setText(
                                    "Actualizar plan"
                            );

                            eliminar.setDisable(
                                    false
                            );
                        }
                );

        nuevo.setOnAction(
                event ->
                        limpiarFormulario.run()
        );

        guardar.setOnAction(
                event -> {

                    try {

                        validarPlan(
                                objetivo.getText(),
                                area.getText(),
                                fechaInicio.getValue(),
                                fechaFin.getValue()
                        );

                        Plan actual =
                                seleccionado.get();

                        if (
                                actual == null
                        ) {

                            Plan creado =
                                    database.createPlan(
                                            objetivo
                                                    .getText()
                                                    .trim(),

                                            area
                                                    .getText()
                                                    .trim(),

                                            fechaInicio
                                                    .getValue(),

                                            fechaFin
                                                    .getValue(),

                                            usuario
                                    );

                            mostrarAlerta(
                                    Alert.AlertType.INFORMATION,
                                    "Plan creado",
                                    "Se creó correctamente "
                                    + creado.codigo()
                            );

                        } else {

                            database.updatePlan(
                                    actual.id(),

                                    objetivo
                                            .getText()
                                            .trim(),

                                    area
                                            .getText()
                                            .trim(),

                                    fechaInicio
                                            .getValue(),

                                    fechaFin
                                            .getValue(),

                                    usuario
                            );

                            mostrarAlerta(
                                    Alert.AlertType.INFORMATION,
                                    "Plan actualizado",
                                    "El plan "
                                    + actual.codigo()
                                    + " fue actualizado correctamente."
                            );
                        }

                        refrescar.run();
                        limpiarFormulario.run();

                    } catch (
                            Exception exception
                    ) {

                        mostrarAlerta(
                                Alert.AlertType.ERROR,
                                "Error",
                                obtenerMensaje(
                                        exception
                                )
                        );
                    }
                }
        );

        eliminar.setOnAction(
                event -> {

                    Plan actual =
                            seleccionado.get();

                    if (
                            actual == null
                    ) {

                        mostrarAlerta(
                                Alert.AlertType.WARNING,
                                "Seleccione un plan",
                                "Debe seleccionar un plan para eliminar."
                        );

                        return;
                    }

                    Alert confirmacion =
                            new Alert(
                                    Alert.AlertType.CONFIRMATION
                            );

                    confirmacion.setTitle(
                            "Eliminar plan"
                    );

                    confirmacion.setHeaderText(
                            "¿Eliminar "
                            + actual.codigo()
                            + "?"
                    );

                    confirmacion.setContentText(
                            "Esta acción eliminará permanentemente "
                            +
                            "el plan seleccionado."
                    );

                    Optional<ButtonType> respuesta =
                            confirmacion.showAndWait();

                    if (
                            respuesta.isPresent()
                            &&
                            respuesta.get()
                                    == ButtonType.OK
                    ) {

                        try {

                            database.deletePlan(
                                    actual.id(),
                                    usuario
                            );

                            refrescar.run();

                            limpiarFormulario.run();

                            mostrarAlerta(
                                    Alert.AlertType.INFORMATION,
                                    "Plan eliminado",
                                    "El plan fue eliminado correctamente."
                            );

                        } catch (
                                Exception exception
                        ) {

                            mostrarAlerta(
                                    Alert.AlertType.ERROR,
                                    "Error",
                                    obtenerMensaje(
                                            exception
                                    )
                            );
                        }
                    }
                }
        );

        enviarRevision.setOnAction(event -> ejecutarAccionPlan(
                seleccionado.get(), tabla, refrescar,
                plan -> database.enviarARevision(plan.id(), usuario),
                "Plan enviado a revisión",
                "El plan %s quedó " + EstadoPlan.PENDIENTE_REVISION + ". El director de área lo debe aprobar o devolver."));

        iniciarEjecucion.setOnAction(event -> ejecutarAccionPlan(
                seleccionado.get(), tabla, refrescar,
                plan -> database.iniciarEjecucion(plan.id(), usuario),
                "Ejecución iniciada",
                "El plan %s quedó " + EstadoPlan.EN_EJECUCION + "."));

        finalizar.setOnAction(event -> ejecutarAccionPlan(
                seleccionado.get(), tabla, refrescar,
                plan -> database.finalizarPlan(plan.id(), usuario),
                "Plan finalizado",
                "El plan %s quedó " + EstadoPlan.FINALIZADO + "."));

        historial.setOnAction(event -> mostrarHistorial(seleccionado.get()));

        atender.setOnAction(event -> {

            Observacion observacion = observaciones.getSelectionModel().getSelectedItem();

            if (observacion == null) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Seleccione una observación",
                        "Seleccione en la lista la observación que ya atendió.");
                return;
            }

            ejecutarAccionPlan(
                    seleccionado.get(), tabla, refrescar,
                    plan -> database.marcarObservacionAtendida(observacion.id(), usuario),
                    "Observación atendida",
                    "La observación del plan %s quedó marcada como atendida. "
                    + "Cuando termine los ajustes, envíe el plan de nuevo a revisión.");
        });

        HBox botones =
                new HBox(
                        10,
                        nuevo,
                        guardar
                );

        HBox.setHgrow(
                nuevo,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                guardar,
                Priority.ALWAYS
        );

        return new VBox(
                10,
                titulo,
                modo,

                new Label(
                        "Objetivo"
                ),

                objetivo,

                new Label(
                        "Área responsable"
                ),

                area,

                new Label(
                        "Fecha de inicio"
                ),

                fechaInicio,

                new Label(
                        "Fecha final"
                ),

                fechaFin,

                estado,

                botones,

                new Separator(),

                eliminar,

                new Separator(),

                tituloFlujo,

                enviarRevision,

                iniciarEjecucion,

                finalizar,

                historial,

                new Label(
                        "Observaciones del director"
                ),

                observaciones,

                atender
        );
    }

    /**
     * HU-017: vista del director de área. Muestra los planes pendientes de revisión de su área
     * y permite aprobarlos o devolverlos con observaciones obligatorias.
     */
    private Node crearVistaRevision(User director) {

        TableView<Plan> pendientes = crearTablaPlanes();
        pendientes.setId("tblPendientes");
        pendientes.setPlaceholder(new Label("No hay planes pendientes de revisión."));

        TableView<Plan> planesArea = crearTablaPlanes();
        planesArea.setId("tblPlanesArea");

        Runnable refrescar = () -> {
            pendientes.getItems().setAll(database.listarPendientesRevision(director));
            planesArea.getItems().setAll(database.listarPlanesArea(director));
        };

        Tab tabPendientes = new Tab("Pendientes de revisión", pendientes);
        tabPendientes.setClosable(false);

        Tab tabArea = new Tab("Planes del área", planesArea);
        tabArea.setClosable(false);

        TabPane pestanas = new TabPane(tabPendientes, tabArea);
        pestanas.setId("tabsRevision");

        Label titulo = new Label("Revisión de planes de acción");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label alcance = new Label(
                director.area() == null ? "Todas las áreas" : "Área: " + director.area());

        VBox centro = new VBox(10, titulo, alcance, pestanas);
        VBox.setVgrow(pestanas, Priority.ALWAYS);
        HBox.setHgrow(centro, Priority.ALWAYS);

        // Panel de revisión del plan seleccionado
        SimpleObjectProperty<Plan> seleccionado = new SimpleObjectProperty<>();

        Label tituloPanel = new Label("Detalle del plan");
        tituloPanel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label detalle = new Label();
        detalle.setId("lblDetalle");
        detalle.setWrapText(true);

        ListView<Observacion> anteriores = new ListView<>();
        anteriores.setId("lstObservacionesAnteriores");
        configurarListaObservaciones(anteriores);

        TextArea observacion = new TextArea();
        observacion.setId("txtObservacion");
        observacion.setPromptText("Escriba qué debe corregir el coordinador");
        observacion.setPrefRowCount(4);
        observacion.setWrapText(true);

        Label errorObservacion = new Label();
        errorObservacion.setId("lblErrorObservacion");
        errorObservacion.setWrapText(true);
        errorObservacion.setStyle("-fx-text-fill: #b00020;");

        Button aprobar = new Button("Aprobar plan");
        aprobar.setId("btnAprobar");

        Button devolver = new Button("Devolver con observaciones");
        devolver.setId("btnDevolver");

        Button historial = new Button("Ver historial del plan");
        historial.setId("btnHistorialRevision");

        for (Button boton : List.of(aprobar, devolver, historial)) {
            boton.setMaxWidth(Double.MAX_VALUE);
        }

        Runnable limpiar = () -> {
            seleccionado.set(null);
            detalle.setText("Seleccione un plan para revisarlo.");
            anteriores.getItems().clear();
            observacion.clear();
            errorObservacion.setText("");
            observacion.setStyle("");
            deshabilitar(true, aprobar, devolver, historial);
        };

        Consumer<Plan> mostrar = plan -> {

            if (plan == null) {
                return;
            }

            seleccionado.set(plan);
            detalle.setText(formatearDetalle(plan));
            anteriores.getItems().setAll(database.listarObservaciones(plan.id()));
            errorObservacion.setText("");
            observacion.setStyle("");

            boolean pendiente = EstadoPlan.PENDIENTE_REVISION.equals(plan.estado());
            deshabilitar(!pendiente, aprobar, devolver);
            historial.setDisable(false);
        };

        pendientes.getSelectionModel().selectedItemProperty()
                .addListener((observable, anterior, plan) -> mostrar.accept(plan));

        planesArea.getSelectionModel().selectedItemProperty()
                .addListener((observable, anterior, plan) -> mostrar.accept(plan));

        observacion.textProperty().addListener((observable, anterior, texto) -> {
            if (!texto.isBlank()) {
                errorObservacion.setText("");
                observacion.setStyle("");
            }
        });

        aprobar.setOnAction(event -> {

            Plan plan = seleccionado.get();

            if (plan == null
                    || !confirmar(
                            "Aprobar plan",
                            "¿Aprobar el plan " + plan.codigo() + "?",
                            "El plan quedará " + EstadoPlan.APROBADO
                            + " y el coordinador podrá iniciar su ejecución.")) {
                return;
            }

            try {
                database.aprobarPlan(plan.id(), director);
                refrescar.run();
                limpiar.run();
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Plan aprobado",
                        "El plan " + plan.codigo() + " quedó " + EstadoPlan.APROBADO
                        + ". Se notificó al coordinador.");

            } catch (Exception exception) {
                mostrarAlerta(Alert.AlertType.WARNING, "No se puede aprobar el plan", obtenerMensaje(exception));
            }
        });

        devolver.setOnAction(event -> {

            Plan plan = seleccionado.get();

            if (plan == null) {
                return;
            }

            // RNF-06: el mensaje de validación aparece junto al campo.
            if (observacion.getText().isBlank()) {
                errorObservacion.setText("Debe escribir las observaciones para devolver el plan.");
                observacion.setStyle("-fx-border-color: #b00020;");
                observacion.requestFocus();
                return;
            }

            if (!confirmar(
                    "Devolver plan",
                    "¿Devolver el plan " + plan.codigo() + " al coordinador?",
                    "El plan quedará " + EstadoPlan.DEVUELTO
                    + " y no podrá ejecutarse hasta que se atiendan las observaciones y se apruebe de nuevo.")) {
                return;
            }

            try {
                database.devolverPlan(plan.id(), director, observacion.getText().trim());
                refrescar.run();
                limpiar.run();
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Plan devuelto",
                        "El plan " + plan.codigo() + " quedó " + EstadoPlan.DEVUELTO
                        + ". Se notificó al coordinador con las observaciones.");

            } catch (Exception exception) {
                mostrarAlerta(Alert.AlertType.WARNING, "No se puede devolver el plan", obtenerMensaje(exception));
            }
        });

        historial.setOnAction(event -> mostrarHistorial(seleccionado.get()));

        VBox panel = new VBox(
                10,
                tituloPanel,
                detalle,
                new Label("Observaciones anteriores"),
                anteriores,
                new Separator(),
                new Label("Observaciones para el coordinador"),
                observacion,
                errorObservacion,
                aprobar,
                devolver,
                historial);

        panel.setPrefWidth(360);

        ScrollPane desplazamiento = new ScrollPane(panel);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setPrefWidth(390);
        desplazamiento.setMinWidth(390);
        desplazamiento.setStyle("-fx-background-color: transparent;");

        refrescar.run();
        limpiar.run();

        return new HBox(25, centro, desplazamiento);
    }

    /** Ejecuta una acción del flujo de aprobación sobre el plan seleccionado y muestra el resultado. */
    private void ejecutarAccionPlan(
            Plan plan,
            TableView<Plan> tabla,
            Runnable refrescar,
            Consumer<Plan> accion,
            String titulo,
            String mensaje
    ) {

        if (plan == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Seleccione un plan",
                    "Debe seleccionar un plan de la tabla.");
            return;
        }

        try {
            accion.accept(plan);
            refrescar.run();
            seleccionarPlan(tabla, plan.id());
            mostrarAlerta(Alert.AlertType.INFORMATION, titulo, String.format(mensaje, plan.codigo()));

        } catch (Exception exception) {
            mostrarAlerta(Alert.AlertType.WARNING, "No se puede completar la acción", obtenerMensaje(exception));
        }
    }

    private void seleccionarPlan(TableView<Plan> tabla, long id) {
        tabla.getItems().stream()
                .filter(plan -> plan.id() == id)
                .findFirst()
                .ifPresent(plan -> tabla.getSelectionModel().select(plan));
    }

    private void configurarListaObservaciones(ListView<Observacion> lista) {

        lista.setPrefHeight(120);
        lista.setPlaceholder(new Label("El plan no tiene observaciones."));

        lista.setCellFactory(vista -> {

            ListCell<Observacion> celda = new ListCell<>() {
                @Override
                protected void updateItem(Observacion item, boolean vacia) {
                    super.updateItem(item, vacia);
                    setText(vacia || item == null ? null : formatearObservacion(item));
                }
            };

            celda.setWrapText(true);
            celda.prefWidthProperty().bind(vista.widthProperty().subtract(20));
            return celda;
        });
    }

    private void mostrarNotificaciones(User usuario) {

        List<Notificacion> lista = database.listarNotificaciones(usuario);

        String texto = lista.isEmpty()
                ? "No tiene notificaciones."
                : lista.stream()
                        .map(notificacion -> (notificacion.leida() ? "" : "[Nueva] ")
                                + notificacion.fecha().format(FORMATO_FECHA)
                                + "\n" + notificacion.mensaje())
                        .collect(Collectors.joining("\n\n"));

        mostrarTexto("Notificaciones", "Notificaciones de " + usuario.nombre(), texto);

        database.marcarNotificacionesLeidas(usuario);
    }

    private void mostrarHistorial(Plan plan) {

        if (plan == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Seleccione un plan",
                    "Debe seleccionar un plan para ver su historial.");
            return;
        }

        List<RegistroHistorial> registros = database.listarHistorial(plan.id());

        String texto = registros.isEmpty()
                ? "El plan no tiene cambios de estado registrados."
                : registros.stream()
                        .map(registro -> registro.fecha().format(FORMATO_FECHA)
                                + " · " + registro.usuario()
                                + " · " + registro.accion()
                                + ": " + (registro.estadoAnterior() == null ? "—" : registro.estadoAnterior())
                                + " → " + registro.estadoNuevo()
                                + (registro.detalle() == null ? "" : "\n    " + registro.detalle()))
                        .collect(Collectors.joining("\n"));

        mostrarTexto("Historial del plan", "Historial de " + plan.codigo(), texto);
    }

    private void mostrarTexto(String titulo, String encabezado, String texto) {

        TextArea contenido = new TextArea(texto);
        contenido.setId("txtDialogo");
        contenido.setEditable(false);
        contenido.setWrapText(true);
        contenido.setPrefSize(680, 320);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.getDialogPane().setContent(contenido);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private boolean confirmar(String titulo, String encabezado, String contenido) {

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle(titulo);
        confirmacion.setHeaderText(encabezado);
        confirmacion.setContentText(contenido);
        confirmacion.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    private static String formatearDetalle(Plan plan) {
        return "Código: " + plan.codigo()
                + "\nObjetivo: " + plan.objetivo()
                + "\nÁrea: " + plan.area()
                + "\nFechas: " + plan.fechaInicio() + " a " + plan.fechaFin()
                + "\nEstado: " + plan.estado();
    }

    private static String formatearObservacion(Observacion observacion) {
        return (observacion.atendida() ? "[Atendida] " : "[Pendiente] ")
                + observacion.fecha().format(FORMATO_FECHA)
                + " · " + observacion.autor()
                + ": " + observacion.descripcion();
    }

    private static void deshabilitar(boolean valor, Control... controles) {
        for (Control control : controles) {
            control.setDisable(valor);
        }
    }

    private void validarPlan(
            String objetivo,
            String area,
            LocalDate inicio,
            LocalDate fin
    ) {

        if (
                objetivo == null
                ||
                objetivo.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Debe ingresar el objetivo del plan."
            );
        }

        if (
                area == null
                ||
                area.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Debe ingresar el área responsable."
            );
        }

        if (
                inicio == null
                ||
                fin == null
        ) {

            throw new IllegalArgumentException(
                    "Debe seleccionar las fechas."
            );
        }

        if (
                fin.isBefore(
                        inicio
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha final no puede ser anterior "
                    +
                    "a la fecha de inicio."
            );
        }
    }

    private String obtenerMensaje(
            Exception exception
    ) {

        if (
                exception.getMessage() != null
                &&
                !exception
                        .getMessage()
                        .isBlank()
        ) {

            return exception.getMessage();
        }

        return "Ocurrió un error inesperado.";
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje
    ) {

        Alert alert =
                new Alert(
                        tipo
                );

        alert.setTitle(
                titulo
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                mensaje
        );

        // Evita que los mensajes largos se corten en algunos sistemas (por ejemplo, Linux).
        alert.getDialogPane().setMinHeight(
                Region.USE_PREF_SIZE
        );

        alert.showAndWait();
    }

    public static void main(
            String[] args
    ) {

        launch(args);
    }

    public record User(
            long id,
            String nombre,
            String correo,
            String rol,
            String area
    ) {}

    /** Observación que el director registra al devolver un plan (HU-017). */
    public record Observacion(
            long id,
            long planId,
            String autor,
            String descripcion,
            LocalDateTime fecha,
            boolean atendida
    ) {}

    public record Notificacion(
            long id,
            String mensaje,
            LocalDateTime fecha,
            boolean leida
    ) {}

    public record RegistroHistorial(
            String accion,
            String estadoAnterior,
            String estadoNuevo,
            String usuario,
            String detalle,
            LocalDateTime fecha
    ) {}

    /** Roles del prototipo. */
    static final class Rol {

        static final String ADMIN = "ADMIN";
        static final String COORDINADOR = "COORDINADOR";
        static final String RESPONSABLE = "RESPONSABLE";
        static final String DIRECTOR = "DIRECTOR";

        private Rol() {
        }
    }

    /** Estados del plan de acción (EstadoPlan en el diagrama de clases). */
    static final class EstadoPlan {

        static final String BORRADOR = "BORRADOR";
        static final String PENDIENTE_REVISION = "PENDIENTE DE REVISION";
        static final String EN_REVISION = "EN REVISION";
        static final String APROBADO = "APROBADO";
        static final String EN_EJECUCION = "EN EJECUCION";
        static final String FINALIZADO = "FINALIZADO";

        /**
         * Estado al que vuelve un plan devuelto por el director. La HU-017 dice "En revisión"
         * y el CU-01 dice "En borrador"; si el equipo decide lo segundo, solo se cambia esta línea.
         */
        static final String DEVUELTO = EN_REVISION;

        private EstadoPlan() {
        }

        /** El coordinador solo puede modificar o enviar a revisión un plan que está en sus manos. */
        static boolean editable(String estado) {
            return BORRADOR.equals(estado) || DEVUELTO.equals(estado);
        }
    }

    public record Plan(
            long id,
            String codigo,
            String objetivo,
            String area,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String estado
    ) {}

    static class DataService {

        private static final String URL_POR_DEFECTO =
                "jdbc:h2:./data/sgpi";

        private final String url;

        private static final String USER =
                "sa";

        private static final String PASSWORD =
                "";

        DataService() {
            this(URL_POR_DEFECTO);
        }

        /** Permite usar otra base de datos, por ejemplo una en memoria para las pruebas. */
        DataService(String url) {
            this.url = url;
        }

        public void init() {

            try {

                if (url.startsWith("jdbc:h2:./data/")) {
                    Files.createDirectories(
                            Path.of("data")
                    );
                }

                try (
                        Connection connection =
                                connect();

                        Statement statement =
                                connection.createStatement()
                ) {

                    statement.executeUpdate(
                            """
                            CREATE TABLE IF NOT EXISTS usuarios (
                                id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                nombre VARCHAR(120) NOT NULL,
                                correo VARCHAR(180) NOT NULL UNIQUE,
                                password_hash VARCHAR(100) NOT NULL,
                                rol VARCHAR(30) NOT NULL,
                                activo BOOLEAN NOT NULL DEFAULT TRUE
                            )
                            """
                    );

                    statement.executeUpdate(
                            """
                            CREATE TABLE IF NOT EXISTS planes (
                                id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                codigo VARCHAR(50) UNIQUE,
                                objetivo VARCHAR(500) NOT NULL,
                                area VARCHAR(150) NOT NULL,
                                fecha_inicio DATE NOT NULL,
                                fecha_fin DATE NOT NULL,
                                estado VARCHAR(30) NOT NULL,
                                creado_por BIGINT NOT NULL,
                                FOREIGN KEY (creado_por)
                                    REFERENCES usuarios(id)
                            )
                            """
                    );

                    crearTablasRevision(
                            statement
                    );

                    seedUser(
                            connection,
                            "Administrador SGPI",
                            "admin@sgpi.local",
                            "Admin123*",
                            "ADMIN"
                    );

                    seedUser(
                            connection,
                            "Coordinador Demo",
                            "coordinador@sgpi.local",
                            "Coord123*",
                            "COORDINADOR"
                    );

                    seedUser(
                            connection,
                            "Responsable Demo",
                            "responsable@sgpi.local",
                            "Resp123*",
                            "RESPONSABLE"
                    );

                    seedUser(
                            connection,
                            "Director Demo",
                            "director@sgpi.local",
                            "Dir123*",
                            Rol.DIRECTOR,
                            "Calidad"
                    );
                }

            } catch (
                    Exception exception
            ) {

                throw new RuntimeException(
                        "No fue posible iniciar la base de datos.",
                        exception
                );
            }
        }

        private Connection connect()
                throws SQLException {

            return DriverManager
                    .getConnection(
                            url,
                            USER,
                            PASSWORD
                    );
        }

        /**
         * HU-017: columnas y tablas para el flujo de aprobación. Usa IF NOT EXISTS para que las bases
         * creadas con la versión anterior del prototipo se actualicen sin perder datos.
         */
        private void crearTablasRevision(
                Statement statement
        )
                throws SQLException {

            statement.executeUpdate("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS area VARCHAR(150)");
            statement.executeUpdate("ALTER TABLE planes ADD COLUMN IF NOT EXISTS fecha_envio TIMESTAMP");
            statement.executeUpdate("ALTER TABLE planes ADD COLUMN IF NOT EXISTS revisado_por BIGINT");
            statement.executeUpdate("ALTER TABLE planes ADD COLUMN IF NOT EXISTS fecha_revision TIMESTAMP");

            statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS observaciones (
                        id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        plan_id BIGINT NOT NULL,
                        autor_id BIGINT NOT NULL,
                        descripcion VARCHAR(1000) NOT NULL,
                        fecha TIMESTAMP NOT NULL,
                        atendida BOOLEAN NOT NULL DEFAULT FALSE,
                        fecha_atencion TIMESTAMP,
                        FOREIGN KEY (plan_id) REFERENCES planes(id) ON DELETE CASCADE,
                        FOREIGN KEY (autor_id) REFERENCES usuarios(id)
                    )
                    """
            );

            statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS notificaciones (
                        id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        usuario_id BIGINT NOT NULL,
                        plan_id BIGINT,
                        mensaje VARCHAR(1200) NOT NULL,
                        fecha TIMESTAMP NOT NULL,
                        leida BOOLEAN NOT NULL DEFAULT FALSE,
                        FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
                        FOREIGN KEY (plan_id) REFERENCES planes(id) ON DELETE CASCADE
                    )
                    """
            );

            // Sin llave foránea hacia planes: el historial se conserva aunque el plan se elimine.
            statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS historial_planes (
                        id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        plan_id BIGINT NOT NULL,
                        codigo_plan VARCHAR(50),
                        usuario_id BIGINT NOT NULL,
                        accion VARCHAR(40) NOT NULL,
                        estado_anterior VARCHAR(30),
                        estado_nuevo VARCHAR(30),
                        detalle VARCHAR(1000),
                        fecha TIMESTAMP NOT NULL,
                        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
                    )
                    """
            );
        }

        private void seedUser(
                Connection connection,
                String nombre,
                String correo,
                String clave,
                String rol
        )
                throws SQLException {

            seedUser(connection, nombre, correo, clave, rol, null);
        }

        private void seedUser(
                Connection connection,
                String nombre,
                String correo,
                String clave,
                String rol,
                String area
        )
                throws SQLException {

            try (
                    PreparedStatement consulta =
                            connection.prepareStatement(
                                    "SELECT id FROM usuarios "
                                    +
                                    "WHERE LOWER(correo)=LOWER(?)"
                            )
            ) {

                consulta.setString(
                        1,
                        correo
                );

                try (
                        ResultSet result =
                                consulta.executeQuery()
                ) {

                    if (
                            result.next()
                    ) {
                        return;
                    }
                }
            }

            try (
                    PreparedStatement insert =
                            connection.prepareStatement(
                                    """
                                    INSERT INTO usuarios
                                    (
                                        nombre,
                                        correo,
                                        password_hash,
                                        rol,
                                        area
                                    )
                                    VALUES (?, ?, ?, ?, ?)
                                    """
                            )
            ) {

                insert.setString(
                        1,
                        nombre
                );

                insert.setString(
                        2,
                        correo
                );

                insert.setString(
                        3,
                        BCrypt.hashpw(
                                clave,
                                BCrypt.gensalt()
                        )
                );

                insert.setString(
                        4,
                        rol
                );

                insert.setString(
                        5,
                        area
                );

                insert.executeUpdate();
            }
        }

        public Optional<User> authenticate(
                String correo,
                String clave
        ) {

            String sql =
                    """
                    SELECT
                        id,
                        nombre,
                        correo,
                        password_hash,
                        rol,
                        area
                    FROM usuarios
                    WHERE LOWER(correo)=LOWER(?)
                      AND activo=TRUE
                    """;

            try (
                    Connection connection =
                            connect();

                    PreparedStatement statement =
                            connection.prepareStatement(
                                    sql
                            )
            ) {

                statement.setString(
                        1,
                        correo
                );

                try (
                        ResultSet result =
                                statement.executeQuery()
                ) {

                    if (
                            result.next()
                            &&
                            BCrypt.checkpw(
                                    clave,
                                    result.getString(
                                            "password_hash"
                                    )
                            )
                    ) {

                        return Optional.of(
                                new User(
                                        result.getLong(
                                                "id"
                                        ),

                                        result.getString(
                                                "nombre"
                                        ),

                                        result.getString(
                                                "correo"
                                        ),

                                        result.getString(
                                                "rol"
                                        ),

                                        result.getString(
                                                "area"
                                        )
                                )
                        );
                    }
                }

                return Optional.empty();

            } catch (
                    SQLException exception
            ) {

                throw new RuntimeException(
                        exception
                );
            }
        }

        public Plan createPlan(
                String objetivo,
                String area,
                LocalDate inicio,
                LocalDate fin,
                User usuario
        ) {

            validarPermiso(
                    usuario
            );

            // HU-017: todo plan nuevo empieza en borrador; los demás estados se alcanzan con el flujo de aprobación.
            String estado = EstadoPlan.BORRADOR;

            String sql =
                    """
                    INSERT INTO planes
                    (
                        objetivo,
                        area,
                        fecha_inicio,
                        fecha_fin,
                        estado,
                        creado_por
                    )
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            try (
                    Connection connection =
                            connect()
            ) {

                connection.setAutoCommit(
                        false
                );

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        sql,
                                        Statement.RETURN_GENERATED_KEYS
                                )
                ) {

                    statement.setString(
                            1,
                            objetivo
                    );

                    statement.setString(
                            2,
                            area
                    );

                    statement.setDate(
                            3,
                            Date.valueOf(
                                    inicio
                            )
                    );

                    statement.setDate(
                            4,
                            Date.valueOf(
                                    fin
                            )
                    );

                    statement.setString(
                            5,
                            estado
                    );

                    statement.setLong(
                            6,
                            usuario.id()
                    );

                    statement.executeUpdate();

                    long id;

                    try (
                            ResultSet keys =
                                    statement.getGeneratedKeys()
                    ) {

                        if (
                                !keys.next()
                        ) {

                            throw new SQLException(
                                    "No fue posible obtener el ID."
                            );
                        }

                        id =
                                keys.getLong(
                                        1
                                );
                    }

                    String codigo =
                            "PA-"
                            +
                            LocalDate.now()
                                    .getYear()
                            +
                            "-"
                            +
                            String.format(
                                    "%04d",
                                    id
                            );

                    try (
                            PreparedStatement update =
                                    connection.prepareStatement(
                                            """
                                            UPDATE planes
                                            SET codigo=?
                                            WHERE id=?
                                            """
                                    )
                    ) {

                        update.setString(
                                1,
                                codigo
                        );

                        update.setLong(
                                2,
                                id
                        );

                        update.executeUpdate();
                    }

                    registrarHistorial(
                            connection,
                            new PlanInfo(id, codigo, area, null, usuario.id()),
                            usuario,
                            "CREACION",
                            estado,
                            null
                    );

                    connection.commit();

                    return new Plan(
                            id,
                            codigo,
                            objetivo,
                            area,
                            inicio,
                            fin,
                            estado
                    );

                } catch (
                        Exception exception
                ) {

                    connection.rollback();

                    throw exception;
                }

            } catch (
                    Exception exception
            ) {

                throw new RuntimeException(
                        "No fue posible crear el plan: "
                        +
                        exception.getMessage(),
                        exception
                );
            }
        }

        public void updatePlan(
                long id,
                String objetivo,
                String area,
                LocalDate inicio,
                LocalDate fin,
                User usuario
        ) {

            validarPermiso(usuario);

            enTransaccion(connection -> {

                PlanInfo plan = leerPlan(connection, id);

                // HU-017: un plan en revisión, aprobado o en ejecución ya no se modifica.
                if (!EstadoPlan.editable(plan.estado())) {
                    throw new IllegalStateException(
                            "El plan " + plan.codigo() + " está en estado " + plan.estado()
                            + ". Solo se pueden modificar planes en estado " + EstadoPlan.BORRADOR
                            + " o " + EstadoPlan.DEVUELTO + ".");
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        """
                        UPDATE planes
                        SET objetivo=?, area=?, fecha_inicio=?, fecha_fin=?
                        WHERE id=?
                        """)) {

                    statement.setString(1, objetivo);
                    statement.setString(2, area);
                    statement.setDate(3, Date.valueOf(inicio));
                    statement.setDate(4, Date.valueOf(fin));
                    statement.setLong(5, id);
                    statement.executeUpdate();
                }
            });
        }

        public void deletePlan(
                long id,
                User usuario
        ) {

            validarPermiso(usuario);

            String sql =
                    """
                    DELETE FROM planes
                    WHERE id=?
                    """;

            try (
                    Connection connection =
                            connect();

                    PreparedStatement statement =
                            connection.prepareStatement(
                                    sql
                            )
            ) {

                statement.setLong(
                        1,
                        id
                );

                int filas =
                        statement.executeUpdate();

                if (
                        filas == 0
                ) {

                    throw new RuntimeException(
                            "El plan no existe."
                    );
                }

            } catch (
                    SQLException exception
            ) {

                throw new RuntimeException(
                        "No fue posible eliminar el plan: "
                        +
                        exception.getMessage(),
                        exception
                );
            }
        }

        public List<Plan> listPlans() {

            List<Plan> planes =
                    new ArrayList<>();

            String sql =
                    """
                    SELECT
                        id,
                        codigo,
                        objetivo,
                        area,
                        fecha_inicio,
                        fecha_fin,
                        estado
                    FROM planes
                    ORDER BY id DESC
                    """;

            try (
                    Connection connection =
                            connect();

                    PreparedStatement statement =
                            connection.prepareStatement(
                                    sql
                            );

                    ResultSet result =
                            statement.executeQuery()
            ) {

                while (
                        result.next()
                ) {

                    planes.add(
                            new Plan(
                                    result.getLong(
                                            "id"
                                    ),

                                    result.getString(
                                            "codigo"
                                    ),

                                    result.getString(
                                            "objetivo"
                                    ),

                                    result.getString(
                                            "area"
                                    ),

                                    result.getDate(
                                            "fecha_inicio"
                                    )
                                            .toLocalDate(),

                                    result.getDate(
                                            "fecha_fin"
                                    )
                                            .toLocalDate(),

                                    result.getString(
                                            "estado"
                                    )
                            )
                    );
                }

                return planes;

            } catch (
                    SQLException exception
            ) {

                throw new RuntimeException(
                        exception
                );
            }
        }

        // ------------------------------------------------------------------
        // HU-017: aprobación y retroalimentación de planes de acción
        // ------------------------------------------------------------------

        /** El coordinador envía el plan al director de área (enviarRevision en el diagrama de clases). */
        public void enviarARevision(long planId, User usuario) {

            validarPermiso(usuario);

            enTransaccion(connection -> {

                PlanInfo plan = leerPlan(connection, planId);

                if (!EstadoPlan.editable(plan.estado())) {
                    throw new IllegalStateException(
                            "Solo se pueden enviar a revisión planes en estado " + EstadoPlan.BORRADOR
                            + " o " + EstadoPlan.DEVUELTO + ". El plan " + plan.codigo()
                            + " está en estado " + plan.estado() + ".");
                }

                if (contarObservacionesPendientes(connection, planId) > 0) {
                    throw new IllegalStateException(
                            "El plan " + plan.codigo() + " tiene observaciones sin atender. "
                            + "Márquelas como atendidas antes de enviarlo de nuevo a revisión.");
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE planes SET estado=?, fecha_envio=? WHERE id=?")) {
                    statement.setString(1, EstadoPlan.PENDIENTE_REVISION);
                    statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                    statement.setLong(3, planId);
                    statement.executeUpdate();
                }

                registrarHistorial(connection, plan, usuario, "ENVIO_REVISION",
                        EstadoPlan.PENDIENTE_REVISION, null);
            });
        }

        /** El director aprueba un plan pendiente de revisión (aprobar en el diagrama de clases). */
        public void aprobarPlan(long planId, User director) {

            validarDirector(director);

            enTransaccion(connection -> {

                PlanInfo plan = leerPlan(connection, planId);

                validarAreaDirector(director, plan);

                if (!EstadoPlan.PENDIENTE_REVISION.equals(plan.estado())) {
                    throw new IllegalStateException(
                            "Solo se pueden aprobar planes en estado " + EstadoPlan.PENDIENTE_REVISION
                            + ". El plan " + plan.codigo() + " está en estado " + plan.estado() + ".");
                }

                if (contarObservacionesPendientes(connection, planId) > 0) {
                    throw new IllegalStateException(
                            "El plan " + plan.codigo() + " tiene observaciones sin atender.");
                }

                registrarRevision(connection, planId, EstadoPlan.APROBADO, director);

                registrarHistorial(connection, plan, director, "APROBACION", EstadoPlan.APROBADO, null);

                notificar(connection, plan.creadoPor(), planId,
                        "El plan " + plan.codigo() + " fue aprobado por " + director.nombre()
                        + ". Ya puede iniciar su ejecución.");
            });
        }

        /**
         * El director devuelve un plan con observaciones obligatorias (devolver en el diagrama de clases).
         * El plan pasa a EstadoPlan.DEVUELTO y se notifica al coordinador que lo creó.
         */
        public void devolverPlan(long planId, User director, String observacion) {

            validarDirector(director);

            if (observacion == null || observacion.isBlank()) {
                throw new IllegalArgumentException("Debe escribir las observaciones para devolver el plan.");
            }

            String texto = observacion.trim();

            if (texto.length() > 1000) {
                throw new IllegalArgumentException("Las observaciones no pueden superar los 1000 caracteres.");
            }

            enTransaccion(connection -> {

                PlanInfo plan = leerPlan(connection, planId);

                validarAreaDirector(director, plan);

                if (!EstadoPlan.PENDIENTE_REVISION.equals(plan.estado())) {
                    throw new IllegalStateException(
                            "Solo se pueden devolver planes en estado " + EstadoPlan.PENDIENTE_REVISION
                            + ". El plan " + plan.codigo() + " está en estado " + plan.estado() + ".");
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        """
                        INSERT INTO observaciones (plan_id, autor_id, descripcion, fecha)
                        VALUES (?, ?, ?, ?)
                        """)) {
                    statement.setLong(1, planId);
                    statement.setLong(2, director.id());
                    statement.setString(3, texto);
                    statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    statement.executeUpdate();
                }

                registrarRevision(connection, planId, EstadoPlan.DEVUELTO, director);

                registrarHistorial(connection, plan, director, "DEVOLUCION", EstadoPlan.DEVUELTO, texto);

                notificar(connection, plan.creadoPor(), planId,
                        "El plan " + plan.codigo() + " fue devuelto por " + director.nombre()
                        + " con observaciones: " + texto);
            });
        }

        /** El coordinador marca una observación como atendida (marcarAtendida en el diagrama de clases). */
        public void marcarObservacionAtendida(long observacionId, User usuario) {

            validarPermiso(usuario);

            enTransaccion(connection -> {

                long planId;
                boolean atendida;
                String descripcion;

                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT plan_id, atendida, descripcion FROM observaciones WHERE id=?")) {

                    statement.setLong(1, observacionId);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            throw new IllegalArgumentException("La observación no existe.");
                        }

                        planId = result.getLong("plan_id");
                        atendida = result.getBoolean("atendida");
                        descripcion = result.getString("descripcion");
                    }
                }

                if (atendida) {
                    throw new IllegalStateException("La observación ya está marcada como atendida.");
                }

                PlanInfo plan = leerPlan(connection, planId);

                if (!EstadoPlan.editable(plan.estado())) {
                    throw new IllegalStateException(
                            "Solo se pueden atender observaciones de planes devueltos al coordinador. El plan "
                            + plan.codigo() + " está en estado " + plan.estado() + ".");
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE observaciones SET atendida=TRUE, fecha_atencion=? WHERE id=?")) {
                    statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    statement.setLong(2, observacionId);
                    statement.executeUpdate();
                }

                registrarHistorial(connection, plan, usuario, "OBSERVACION_ATENDIDA", plan.estado(), descripcion);
            });
        }

        /**
         * Regla de negocio de HU-017: un plan solo se ejecuta si el director lo aprobó y no tiene
         * observaciones sin atender (puedeEjecutarse en el diagrama de clases).
         */
        public boolean puedeEjecutarse(long planId) {

            try (Connection connection = connect()) {

                PlanInfo plan = leerPlan(connection, planId);

                return EstadoPlan.APROBADO.equals(plan.estado())
                        && contarObservacionesPendientes(connection, planId) == 0;

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }

        public void iniciarEjecucion(long planId, User usuario) {

            validarPermiso(usuario);

            enTransaccion(connection -> {

                PlanInfo plan = leerPlan(connection, planId);

                if (!EstadoPlan.APROBADO.equals(plan.estado())) {
                    throw new IllegalStateException(
                            "El plan " + plan.codigo() + " no se puede ejecutar: el director de área debe aprobarlo primero. "
                            + "Estado actual: " + plan.estado() + ".");
                }

                if (contarObservacionesPendientes(connection, planId) > 0) {
                    throw new IllegalStateException(
                            "El plan " + plan.codigo() + " tiene observaciones sin atender y no se puede ejecutar.");
                }

                actualizarEstado(connection, planId, EstadoPlan.EN_EJECUCION);

                registrarHistorial(connection, plan, usuario, "INICIO_EJECUCION", EstadoPlan.EN_EJECUCION, null);
            });
        }

        public void finalizarPlan(long planId, User usuario) {

            validarPermiso(usuario);

            enTransaccion(connection -> {

                PlanInfo plan = leerPlan(connection, planId);

                if (!EstadoPlan.EN_EJECUCION.equals(plan.estado())) {
                    throw new IllegalStateException(
                            "Solo se pueden finalizar planes en estado " + EstadoPlan.EN_EJECUCION
                            + ". El plan " + plan.codigo() + " está en estado " + plan.estado() + ".");
                }

                actualizarEstado(connection, planId, EstadoPlan.FINALIZADO);

                registrarHistorial(connection, plan, usuario, "FINALIZACION", EstadoPlan.FINALIZADO, null);
            });
        }

        public List<Plan> listarPendientesRevision(User director) {

            validarDirector(director);

            if (director.area() == null) {
                return consultarPlanes("estado=?", EstadoPlan.PENDIENTE_REVISION);
            }

            return consultarPlanes(
                    "estado=? AND LOWER(TRIM(area))=LOWER(TRIM(?))",
                    EstadoPlan.PENDIENTE_REVISION,
                    director.area());
        }

        public List<Plan> listarPlanesArea(User director) {

            validarDirector(director);

            if (director.area() == null) {
                return consultarPlanes("1=1");
            }

            return consultarPlanes("LOWER(TRIM(area))=LOWER(TRIM(?))", director.area());
        }

        public List<Observacion> listarObservaciones(long planId) {

            List<Observacion> lista = new ArrayList<>();

            String sql =
                    """
                    SELECT o.id, o.plan_id, u.nombre AS autor, o.descripcion, o.fecha, o.atendida
                    FROM observaciones o
                    JOIN usuarios u ON u.id=o.autor_id
                    WHERE o.plan_id=?
                    ORDER BY o.fecha DESC, o.id DESC
                    """;

            try (Connection connection = connect();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setLong(1, planId);

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        lista.add(new Observacion(
                                result.getLong("id"),
                                result.getLong("plan_id"),
                                result.getString("autor"),
                                result.getString("descripcion"),
                                result.getTimestamp("fecha").toLocalDateTime(),
                                result.getBoolean("atendida")));
                    }
                }

                return lista;

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }

        public List<Notificacion> listarNotificaciones(User usuario) {

            List<Notificacion> lista = new ArrayList<>();

            try (Connection connection = connect();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT id, mensaje, fecha, leida FROM notificaciones "
                         + "WHERE usuario_id=? ORDER BY fecha DESC, id DESC")) {

                statement.setLong(1, usuario.id());

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        lista.add(new Notificacion(
                                result.getLong("id"),
                                result.getString("mensaje"),
                                result.getTimestamp("fecha").toLocalDateTime(),
                                result.getBoolean("leida")));
                    }
                }

                return lista;

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }

        public int contarNotificacionesSinLeer(User usuario) {

            try (Connection connection = connect();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT COUNT(*) FROM notificaciones WHERE usuario_id=? AND leida=FALSE")) {

                statement.setLong(1, usuario.id());

                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    return result.getInt(1);
                }

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }

        public void marcarNotificacionesLeidas(User usuario) {

            try (Connection connection = connect();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE notificaciones SET leida=TRUE WHERE usuario_id=? AND leida=FALSE")) {

                statement.setLong(1, usuario.id());
                statement.executeUpdate();

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }

        public List<RegistroHistorial> listarHistorial(long planId) {

            List<RegistroHistorial> lista = new ArrayList<>();

            String sql =
                    """
                    SELECT h.accion, h.estado_anterior, h.estado_nuevo, u.nombre AS usuario, h.detalle, h.fecha
                    FROM historial_planes h
                    JOIN usuarios u ON u.id=h.usuario_id
                    WHERE h.plan_id=?
                    ORDER BY h.fecha, h.id
                    """;

            try (Connection connection = connect();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setLong(1, planId);

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        lista.add(new RegistroHistorial(
                                result.getString("accion"),
                                result.getString("estado_anterior"),
                                result.getString("estado_nuevo"),
                                result.getString("usuario"),
                                result.getString("detalle"),
                                result.getTimestamp("fecha").toLocalDateTime()));
                    }
                }

                return lista;

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }

        // ---------------------------------------------------------------- apoyo

        /** Datos del plan que necesitan las reglas del flujo de aprobación. */
        private record PlanInfo(
                long id,
                String codigo,
                String area,
                String estado,
                long creadoPor
        ) {}

        @FunctionalInterface
        private interface OperacionBD {
            void ejecutar(Connection connection) throws SQLException;
        }

        /** Ejecuta la operación en una transacción: si algo falla, no queda ningún cambio a medias. */
        private void enTransaccion(OperacionBD operacion) {

            try (Connection connection = connect()) {

                connection.setAutoCommit(false);

                try {
                    operacion.ejecutar(connection);
                    connection.commit();

                } catch (SQLException | RuntimeException exception) {
                    connection.rollback();
                    throw exception;
                }

            } catch (SQLException exception) {
                throw new RuntimeException(
                        "Error en la base de datos: " + exception.getMessage(), exception);
            }
        }

        private PlanInfo leerPlan(Connection connection, long planId) throws SQLException {

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, codigo, area, estado, creado_por FROM planes WHERE id=?")) {

                statement.setLong(1, planId);

                try (ResultSet result = statement.executeQuery()) {

                    if (!result.next()) {
                        throw new IllegalArgumentException("El plan no existe.");
                    }

                    return new PlanInfo(
                            result.getLong("id"),
                            result.getString("codigo"),
                            result.getString("area"),
                            result.getString("estado"),
                            result.getLong("creado_por"));
                }
            }
        }

        private int contarObservacionesPendientes(Connection connection, long planId) throws SQLException {

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM observaciones WHERE plan_id=? AND atendida=FALSE")) {

                statement.setLong(1, planId);

                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    return result.getInt(1);
                }
            }
        }

        private void actualizarEstado(Connection connection, long planId, String estado) throws SQLException {

            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE planes SET estado=? WHERE id=?")) {
                statement.setString(1, estado);
                statement.setLong(2, planId);
                statement.executeUpdate();
            }
        }

        private void registrarRevision(Connection connection, long planId, String estado, User director)
                throws SQLException {

            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE planes SET estado=?, revisado_por=?, fecha_revision=? WHERE id=?")) {
                statement.setString(1, estado);
                statement.setLong(2, director.id());
                statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                statement.setLong(4, planId);
                statement.executeUpdate();
            }
        }

        /** Deja el cambio de estado en el historial con el estado anterior y el nuevo (RNF-10). */
        private void registrarHistorial(
                Connection connection,
                PlanInfo plan,
                User usuario,
                String accion,
                String estadoNuevo,
                String detalle
        )
                throws SQLException {

            try (PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO historial_planes
                    (plan_id, codigo_plan, usuario_id, accion, estado_anterior, estado_nuevo, detalle, fecha)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """)) {

                statement.setLong(1, plan.id());
                statement.setString(2, plan.codigo());
                statement.setLong(3, usuario.id());
                statement.setString(4, accion);
                statement.setString(5, plan.estado());
                statement.setString(6, estadoNuevo);
                statement.setString(7, detalle);
                statement.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                statement.executeUpdate();
            }
        }

        private void notificar(Connection connection, long usuarioId, long planId, String mensaje)
                throws SQLException {

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO notificaciones (usuario_id, plan_id, mensaje, fecha) VALUES (?, ?, ?, ?)")) {
                statement.setLong(1, usuarioId);
                statement.setLong(2, planId);
                statement.setString(3, mensaje);
                statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                statement.executeUpdate();
            }
        }

        private List<Plan> consultarPlanes(String condicion, Object... parametros) {

            List<Plan> planes = new ArrayList<>();

            String sql = "SELECT id, codigo, objetivo, area, fecha_inicio, fecha_fin, estado "
                    + "FROM planes WHERE " + condicion + " ORDER BY id DESC";

            try (Connection connection = connect();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                for (int i = 0; i < parametros.length; i++) {
                    statement.setObject(i + 1, parametros[i]);
                }

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        planes.add(new Plan(
                                result.getLong("id"),
                                result.getString("codigo"),
                                result.getString("objetivo"),
                                result.getString("area"),
                                result.getDate("fecha_inicio").toLocalDate(),
                                result.getDate("fecha_fin").toLocalDate(),
                                result.getString("estado")));
                    }
                }

                return planes;

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }

        private void validarDirector(User usuario) {

            if (!Rol.DIRECTOR.equals(usuario.rol())) {
                throw new IllegalStateException(
                        "Solo el director de área puede aprobar o devolver planes de acción.");
            }
        }

        private void validarAreaDirector(User director, PlanInfo plan) {

            if (director.area() != null && !normalizar(director.area()).equals(normalizar(plan.area()))) {
                throw new IllegalStateException(
                        "El plan " + plan.codigo() + " es del área " + plan.area()
                        + ". Solo lo puede revisar el director de esa área.");
            }
        }

        private static String normalizar(String texto) {
            return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        }

        private void validarPermiso(
                User usuario
        ) {

            if (
                    !usuario.rol()
                            .equals("ADMIN")
                    &&
                    !usuario.rol()
                            .equals("COORDINADOR")
            ) {

                throw new IllegalStateException(
                        "El usuario no tiene permisos "
                        +
                        "para modificar planes."
                );
            }
        }
    }
}
