package com.sgpi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas de la corrección del hallazgo java:S2115 de SonarQube (base de datos sin contraseña). */
class SeguridadBaseDatosTest {

    private static final String SIN_CLAVE = "";

    private final String nombre = "prueba-" + UUID.randomUUID();
    private final String urlLocal = "jdbc:h2:./data/" + nombre;
    private final Path archivoClave = Path.of("data", nombre + ".clave");

    @AfterEach
    void borrarBaseLocal() throws IOException {
        if (!Files.isDirectory(Path.of("data"))) {
            return;
        }
        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(Path.of("data"), nombre + ".*")) {
            for (Path archivo : archivos) {
                Files.deleteIfExists(archivo);
            }
        }
    }

    @Test
    @DisplayName("La base de datos rechaza conexiones sin contraseña")
    void laBaseRechazaConexionesSinClave() {
        String url = "jdbc:h2:mem:sgpi-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        new DataService(url).init();

        assertThrows(SQLException.class, () -> DriverManager.getConnection(url, "sa", SIN_CLAVE));
    }

    @Test
    @DisplayName("La base local nueva recibe una contraseña aleatoria guardada en data/")
    void laBaseLocalNuevaGuardaSuClave() throws IOException {
        new DataService(urlLocal).init();

        assertTrue(Files.exists(archivoClave));
        assertTrue(Files.readString(archivoClave, StandardCharsets.UTF_8).length() >= 32);
        assertThrows(SQLException.class, () -> DriverManager.getConnection(urlLocal, "sa", SIN_CLAVE));

        // Al abrir la aplicación otra vez se lee la misma clave y los usuarios siguen ahí.
        DataService otraVez = new DataService(urlLocal);
        otraVez.init();
        assertTrue(otraVez.authenticate("coordinador@sgpi.local", "Coord123*").isPresent());
    }

    @Test
    @DisplayName("Una base creada sin contraseña por una versión anterior recibe la nueva sin perder datos")
    void laBaseAnteriorSeProtegeSinPerderDatos() throws Exception {
        Files.createDirectories(Path.of("data"));
        try (Connection anterior = DriverManager.getConnection(urlLocal, "sa", SIN_CLAVE);
                Statement statement = anterior.createStatement()) {
            statement.execute("CREATE TABLE dato_anterior (valor INT)");
            statement.execute("INSERT INTO dato_anterior VALUES (7)");
        }

        DataService database = new DataService(urlLocal);
        database.init();

        assertThrows(SQLException.class, () -> DriverManager.getConnection(urlLocal, "sa", SIN_CLAVE));
        assertTrue(database.authenticate("coordinador@sgpi.local", "Coord123*").isPresent());

        String clave = Files.readString(archivoClave, StandardCharsets.UTF_8).trim();
        try (Connection conexion = DriverManager.getConnection(urlLocal, "sa", clave);
                Statement statement = conexion.createStatement();
                ResultSet result = statement.executeQuery("SELECT valor FROM dato_anterior")) {
            assertTrue(result.next());
            assertEquals(7, result.getInt(1));
        }
    }
}
