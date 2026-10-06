#!/usr/bin/env bash
set -e

echo "======================================"
echo " Generando prototipo funcional SGPI"
echo "======================================"

mkdir -p src/main/java/com/sgpi
mkdir -p data

cat > pom.xml <<'POM'
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>edu.javeriana</groupId>
    <artifactId>sgpi</artifactId>
    <version>0.1.0-SNAPSHOT</version>

    <name>SGPI</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.release>17</maven.compiler.release>
        <javafx.version>17.0.10</javafx.version>
    </properties>

    <dependencies>

        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>

        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>

    </dependencies>

    <build>
        <plugins>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>

                <configuration>
                    <release>17</release>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>

                <configuration>
                    <mainClass>com.sgpi.Main</mainClass>
                </configuration>
            </plugin>

        </plugins>
    </build>

</project>
POM


cat > .gitignore <<'GITIGNORE'
target/
.idea/
.vscode/
*.iml

data/*.mv.db
data/*.trace.db
GITIGNORE


cat > src/main/java/com/sgpi/Main.java <<'JAVA'
package com.sgpi;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.file.Files;
import java.nio.file.Path;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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


        Label subtitulo = new Label(
                "Sistema de Gestión de Procesos Institucionales"
        );


        TextField correo = new TextField();

        correo.setPromptText(
                "Correo institucional"
        );


        PasswordField clave = new PasswordField();

        clave.setPromptText(
                "Contraseña"
        );


        Button ingresar = new Button(
                "Iniciar sesión"
        );

        ingresar.setDefaultButton(true);


        Label mensaje = new Label();


        ingresar.setOnAction(event -> {

            String email = correo
                    .getText()
                    .trim();

            String password =
                    clave.getText();


            if (email.isBlank() ||
                    password.isBlank()) {

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


            if (usuario.isPresent()) {

                mostrarDashboard(
                        usuario.get()
                );

            } else {

                mensaje.setText(
                        "Usuario o contraseña incorrectos."
                );
            }

        });


        VBox formulario = new VBox(
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

        formulario.setMaxWidth(420);

        formulario.setAlignment(
                Pos.CENTER_LEFT
        );


        StackPane root =
                new StackPane(formulario);

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
                event -> mostrarLogin()
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


        root.setTop(topbar);


        TableView<Plan> tabla =
                new TableView<>();


        TableColumn<Plan, String>
                columnaCodigo =
                new TableColumn<>(
                        "Código"
                );

        columnaCodigo.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .codigo()
                        )
        );


        TableColumn<Plan, String>
                columnaObjetivo =
                new TableColumn<>(
                        "Objetivo"
                );

        columnaObjetivo.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .objetivo()
                        )
        );


        TableColumn<Plan, String>
                columnaArea =
                new TableColumn<>(
                        "Área"
                );

        columnaArea.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .area()
                        )
        );


        TableColumn<Plan, String>
                columnaInicio =
                new TableColumn<>(
                        "Inicio"
                );

        columnaInicio.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .fechaInicio()
                                        .toString()
                        )
        );


        TableColumn<Plan, String>
                columnaFin =
                new TableColumn<>(
                        "Fin"
                );

        columnaFin.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .fechaFin()
                                        .toString()
                        )
        );


        TableColumn<Plan, String>
                columnaEstado =
                new TableColumn<>(
                        "Estado"
                );

        columnaEstado.setCellValueFactory(
                dato ->
                        new ReadOnlyStringWrapper(
                                dato
                                        .getValue()
                                        .estado()
                        )
        );


        tabla.getColumns().addAll(
                columnaCodigo,
                columnaObjetivo,
                columnaArea,
                columnaInicio,
                columnaFin,
                columnaEstado
        );


        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );


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


        VBox centro =
                new VBox(
                        15,
                        tituloPlanes,
                        tabla
                );


        VBox.setVgrow(
                tabla,
                Priority.ALWAYS
        );


        root.setCenter(
                centro
        );


        if (
                usuario.rol()
                        .equals("ADMIN")
                        ||
                usuario.rol()
                        .equals("COORDINADOR")
        ) {

            VBox formulario =
                    crearFormularioPlan(
                            usuario,
                            actualizarTabla
                    );

            formulario.setPrefWidth(
                    310
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
                            "Rol de consulta.\n"
                                    +
                            "No tiene permisos para crear planes."
                    );

            permisos.setPadding(
                    new Insets(
                            20
                    )
            );

            root.setRight(
                    permisos
            );
        }


        stage.setScene(
                new Scene(
                        root,
                        1150,
                        700
                )
        );

        stage.centerOnScreen();
    }


    private VBox crearFormularioPlan(
            User usuario,
            Runnable actualizarTabla
    ) {

        Label titulo =
                new Label(
                        "Nuevo plan"
                );

        titulo.setStyle(
                "-fx-font-size: 18px;" +
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


        Button crear =
                new Button(
                        "Crear plan"
                );


        crear.setMaxWidth(
                Double.MAX_VALUE
        );


        crear.setOnAction(event -> {

            try {

                if (
                        objetivo
                                .getText()
                                .isBlank()
                        ||
                        area
                                .getText()
                                .isBlank()
                ) {

                    mostrarAlerta(
                            "Validación",
                            "Debe ingresar objetivo y área."
                    );

                    return;
                }


                Plan plan =
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


                objetivo.clear();

                area.clear();

                fechaInicio.setValue(
                        LocalDate.now()
                );

                fechaFin.setValue(
                        LocalDate.now()
                                .plusDays(30)
                );


                actualizarTabla.run();


                mostrarAlerta(
                        "Plan creado",
                        "Se creó correctamente "
                                + plan.codigo()
                );


            } catch (
                    Exception exception
            ) {

                mostrarAlerta(
                        "Error",
                        exception.getMessage()
                );
            }

        });


        return new VBox(
                10,

                titulo,

                new Label(
                        "Objetivo"
                ),

                objetivo,

                new Label(
                        "Área"
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

                crear
        );
    }


    private void mostrarAlerta(
            String titulo,
            String mensaje
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
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

            } catch (Exception exception) {

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
                                    "SELECT id FROM usuarios WHERE LOWER(correo)=LOWER(?)"
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

                    if (result.next()) {
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
                User usuario
        ) {

            if (
                    !usuario
                            .rol()
                            .equals("ADMIN")
                    &&
                    !usuario
                            .rol()
                            .equals("COORDINADOR")
            ) {

                throw new IllegalStateException(
                        "No tiene permisos para crear planes."
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
                        "La fecha final no puede ser anterior a la inicial."
                );
            }


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
                    VALUES (?, ?, ?, ?, 'BORRADOR', ?)
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

                    statement.setLong(
                            5,
                            usuario.id()
                    );


                    statement.executeUpdate();


                    long id;


                    try (
                            ResultSet generatedKeys =
                                    statement
                                            .getGeneratedKeys()
                    ) {

                        if (
                                !generatedKeys.next()
                        ) {

                            throw new SQLException(
                                    "No fue posible obtener el ID del plan."
                            );
                        }


                        id =
                                generatedKeys
                                        .getLong(1);
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
                            "BORRADOR"
                    );


                } catch (
                        Exception exception
                ) {

                    connection.rollback();

                    throw exception;


                } finally {

                    connection.setAutoCommit(
                            true
                    );
                }


            } catch (
                    Exception exception
            ) {

                throw new RuntimeException(
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
    }
}
JAVA


cat > README-PROTOTIPO.md <<'README'
# Primer prototipo funcional SGPI

## Tecnologías

- Java
- JavaFX
- Maven
- H2
- JDBC
- BCrypt

## Funcionalidades

- Inicio de sesión.
- Roles ADMIN, COORDINADOR y RESPONSABLE.
- Dashboard.
- Creación de planes de acción.
- Consulta de planes registrados.
- Persistencia local mediante H2.
- Restricción de creación según rol.
- Cierre de sesión.

## Usuarios de prueba

### Administrador

Correo:

admin@sgpi.local

Contraseña:

Admin123*

### Coordinador

Correo:

coordinador@sgpi.local

Contraseña:

Coord123*

### Responsable

Correo:

responsable@sgpi.local

Contraseña:

Resp123*
README


echo ""
echo "======================================"
echo " Prototipo SGPI generado correctamente"
echo "======================================"
echo ""
echo "Archivos principales:"
echo "  pom.xml"
echo "  src/main/java/com/sgpi/Main.java"
echo "  README-PROTOTIPO.md"
echo ""
echo "Siguiente comando:"
echo "  mvn clean compile"
echo ""
