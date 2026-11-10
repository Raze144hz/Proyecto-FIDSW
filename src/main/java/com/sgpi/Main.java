package com.sgpi;

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
import com.sgpi.data.DataService;
import com.sgpi.model.Actividad;
import com.sgpi.model.Area;
import com.sgpi.model.EstadoPlan;
import com.sgpi.model.Notificacion;
import com.sgpi.model.Observacion;
import com.sgpi.model.Plan;
import com.sgpi.model.RegistroHistorial;
import com.sgpi.model.ResultadoLogin;
import com.sgpi.model.Rol;
import com.sgpi.model.User;
import com.sgpi.ui.VistaActividades;
import com.sgpi.ui.VistaAreas;
import com.sgpi.ui.VistaPreferencias;

public class Main extends Application {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DataService database = new DataService();
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        database.init();

        // HU-015: envía los correos pendientes, como el resumen diario, mientras la aplicación está abierta.
        database.iniciarRevisionDeCorreos(5);

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

            // HU-001: tras varios intentos fallidos la cuenta se bloquea por un tiempo.
            ResultadoLogin resultado = database.iniciarSesion(email, password);

            switch (resultado.estado()) {
                case EXITOSO -> mostrarDashboard(resultado.usuario().orElseThrow());
                case BLOQUEADO -> {
                    clave.clear();
                    mensaje.setText("Cuenta bloqueada por intentos fallidos. Intente de nuevo en "
                            + resultado.minutosBloqueo() + " minuto(s).");
                }
                case CREDENCIALES_INVALIDAS -> {
                    clave.clear();
                    mensaje.setText("Usuario o contraseña incorrectos. Intentos restantes antes del bloqueo: "
                            + resultado.intentosRestantes() + ".");
                }
            }
        });

        mensaje.setId("lblMensajeLogin");
        mensaje.setWrapText(true);

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

        // HU-015: cada usuario elige la frecuencia de sus correos y ve los que le llegaron.
        Button preferencias = new Button("Preferencias");
        preferencias.setId("btnPreferencias");
        preferencias.setOnAction(event -> new VistaPreferencias(database, usuario).mostrar(stage));
        topbar.getChildren().add(topbar.getChildren().indexOf(cerrarSesion), preferencias);

        // HU-010: el administrador gestiona las áreas de la estructura organizacional.
        if (Rol.ADMIN.equals(usuario.rol())) {
            Button areas = new Button("Áreas");
            areas.setId("btnAreas");
            areas.setOnAction(event -> new VistaAreas(database, usuario).mostrar(stage));
            topbar.getChildren().add(topbar.getChildren().indexOf(notificaciones), areas);
        }

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

        // HU-002 y HU-004: actividades del plan seleccionado y registro de su avance.
        Button actividades = new Button("Actividades del plan");
        actividades.setId("btnActividades");
        actividades.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        actividades.setOnAction(event -> {
            Plan plan = tabla.getSelectionModel().getSelectedItem();
            new VistaActividades(database, usuario, plan).mostrar(stage);
            long id = plan.id();
            actualizarTabla.run();
            seleccionarPlan(tabla, id);
            tabla.refresh();
        });

        HBox encabezado = new HBox(15, cantidad, actividades);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        VBox centro =
                new VBox(
                        10,
                        tituloPlanes,
                        encabezado,
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

        // HU-004: avance = actividades finalizadas / total de actividades.
        TableColumn<Plan, String> avance = new TableColumn<>("Avance");
        avance.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(database.calcularAvance(dato.getValue().id()) + " %"));

        tabla.getColumns().addAll(
                codigo,
                objetivo,
                area,
                inicio,
                fin,
                estado,
                avance
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
        avance.setMinWidth(70);

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

        // HU-010: el área se elige entre las áreas activas de la estructura organizacional.
        ComboBox<String> area =
                new ComboBox<>();

        area.setPromptText(
                "Área responsable"
        );

        area.setMaxWidth(Double.MAX_VALUE);

        area.setId("cmbArea");

        Runnable cargarAreas = () -> area.getItems().setAll(
                database.listarAreasActivas().stream().map(Area::nombre).toList());

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

                    cargarAreas.run();

                    area.setValue(null);

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

                            area.setValue(
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
                                area.getValue(),
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
                                                    .getValue(),

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
                                            .getValue(),

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

    private String formatearDetalle(Plan plan) {
        List<Actividad> actividades = database.listarActividades(plan.id());
        long finalizadas = actividades.stream().filter(Actividad::finalizada).count();
        String avance = actividades.isEmpty()
                ? "0 % (sin actividades registradas)"
                : database.calcularAvance(plan.id()) + " % (" + finalizadas + " de " + actividades.size()
                        + " actividades finalizadas)";

        return "Código: " + plan.codigo()
                + "\nObjetivo: " + plan.objetivo()
                + "\nÁrea: " + plan.area()
                + "\nFechas: " + plan.fechaInicio() + " a " + plan.fechaFin()
                + "\nEstado: " + plan.estado()
                + "\nAvance: " + avance;
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
                    "Debe seleccionar el área responsable."
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
}
