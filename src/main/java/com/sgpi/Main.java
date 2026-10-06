package com.sgpi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class Main extends Application {

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

        PasswordField clave =
                new PasswordField();

        clave.setPromptText(
                "Contraseña"
        );

        Button ingresar =
                new Button(
                        "Iniciar sesión"
                );

        ingresar.setDefaultButton(true);

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
                );

        Button cerrarSesion =
                new Button(
                        "Cerrar sesión"
                );

        cerrarSesion.setOnAction(
                event ->
                        mostrarLogin()
        );

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

        TableView<Plan> tabla =
                crearTablaPlanes();

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

            root.setRight(
                    formulario
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

        TextField area =
                new TextField();

        area.setPromptText(
                "Área responsable"
        );

        DatePicker fechaInicio =
                new DatePicker(
                        LocalDate.now()
                );

        DatePicker fechaFin =
                new DatePicker(
                        LocalDate.now()
                                .plusDays(30)
                );

        ComboBox<String> estado =
                new ComboBox<>();

        estado.getItems().addAll(
                "BORRADOR",
                "EN EJECUCION",
                "FINALIZADO"
        );

        estado.setValue(
                "BORRADOR"
        );

        estado.setMaxWidth(
                Double.MAX_VALUE
        );

        Button guardar =
                new Button(
                        "Guardar"
                );

        Button nuevo =
                new Button(
                        "Nuevo"
                );

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

                    estado.setValue(
                            "BORRADOR"
                    );

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

                            estado.setValue(
                                    plan.estado()
                            );

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

                                            estado
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

                                    estado
                                            .getValue()
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
                                    actual.id()
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

                new Label(
                        "Estado"
                ),

                estado,

                botones,

                new Separator(),

                eliminar
        );
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
            String rol
    ) {}

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

        private static final String URL =
                "jdbc:h2:./data/sgpi";

        private static final String USER =
                "sa";

        private static final String PASSWORD =
                "";

        public void init() {

            try {

                Files.createDirectories(
                        Path.of("data")
                );

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
                            URL,
                            USER,
                            PASSWORD
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
                                        rol
                                    )
                                    VALUES (?, ?, ?, ?)
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
                        rol
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
                String estado,
                User usuario
        ) {

            validarPermiso(
                    usuario
            );

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
                String estado
        ) {

            String sql =
                    """
                    UPDATE planes
                    SET
                        objetivo=?,
                        area=?,
                        fecha_inicio=?,
                        fecha_fin=?,
                        estado=?
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
                        "No fue posible actualizar el plan: "
                        +
                        exception.getMessage(),
                        exception
                );
            }
        }

        public void deletePlan(
                long id
        ) {

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
