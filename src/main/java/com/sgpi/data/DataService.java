package com.sgpi.data;

import com.sgpi.model.Actividad;
import com.sgpi.model.EstadoActividad;
import com.sgpi.model.EstadoPlan;
import com.sgpi.model.Notificacion;
import com.sgpi.model.Observacion;
import com.sgpi.model.Plan;
import com.sgpi.model.RegistroHistorial;
import com.sgpi.model.ResultadoLogin;
import com.sgpi.model.Rol;
import com.sgpi.model.User;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Acceso a datos y reglas de negocio del prototipo (base H2 embebida).
 *
 * <p>Cada operación que modifica varias tablas se ejecuta en una transacción, para que no queden
 * cambios a medias si algo falla.
 */
public class DataService {

    /** HU-001: número de intentos fallidos seguidos que bloquean la cuenta. */
    public static final int MAX_INTENTOS_FALLIDOS = 3;

    /** HU-001: tiempo que la cuenta permanece bloqueada. */
    public static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    public static final int MAX_OBJETIVO = 500;
    public static final int MAX_AREA = 150;
    public static final int MAX_OBSERVACION = 1000;
    public static final int MAX_DESCRIPCION_ACTIVIDAD = 300;

    private static final String URL_POR_DEFECTO = "jdbc:h2:./data/sgpi";
    private static final String PREFIJO_BASE_LOCAL = "jdbc:h2:./data/";
    private static final String USER = "sa";

    /** Variable de entorno con la contraseña de la base; si no existe se usa la del archivo local. */
    static final String VARIABLE_CLAVE = "SGPI_DB_PASSWORD";

    /**
     * Contraseña de las bases creadas por versiones anteriores del prototipo, que no tenían. Solo se usa
     * una vez, para asignarles la contraseña nueva (ver {@link #prepararClave()}).
     */
    private static final String CLAVE_VERSION_ANTERIOR = "";

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final String url;
    private final Clock reloj;

    /** Contraseña de la base. Las bases en memoria usan una aleatoria, distinta en cada instancia. */
    private String claveBaseDatos = generarClave();

    public DataService() {
        this(URL_POR_DEFECTO);
    }

    /** Permite usar otra base de datos, por ejemplo una en memoria para las pruebas. */
    public DataService(String url) {
        this(url, Clock.systemDefaultZone());
    }

    /** Permite además fijar el reloj, para probar reglas que dependen del tiempo (bloqueo de cuentas). */
    public DataService(String url, Clock reloj) {
        this.url = url;
        this.reloj = reloj;
    }

    // ------------------------------------------------------------------ inicialización

    public void init() {
        try {
            if (url.startsWith(PREFIJO_BASE_LOCAL)) {
                Files.createDirectories(Path.of("data"));
            }
            claveBaseDatos = prepararClave();

            try (Connection connection = connect(); Statement statement = connection.createStatement()) {
                crearTablas(statement);

                seedUser(connection, "Administrador SGPI", "admin@sgpi.local", "Admin123*", Rol.ADMIN, null);
                seedUser(connection, "Coordinador Demo", "coordinador@sgpi.local", "Coord123*", Rol.COORDINADOR, null);
                seedUser(connection, "Responsable Demo", "responsable@sgpi.local", "Resp123*", Rol.RESPONSABLE, null);
                seedUser(connection, "Director Demo", "director@sgpi.local", "Dir123*", Rol.DIRECTOR, "Calidad");
            }
        } catch (Exception exception) {
            throw new RuntimeException("No fue posible iniciar la base de datos.", exception);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, USER, claveBaseDatos);
    }

    /**
     * Define la contraseña de la base (regla java:S2115: una base de datos no debe quedar sin
     * contraseña).
     *
     * <ol>
     *   <li>Si existe la variable de entorno {@value #VARIABLE_CLAVE}, se usa esa.</li>
     *   <li>En la base local ({@code data/}), la contraseña se genera al azar la primera vez y se guarda
     *       en {@code data/sgpi.clave}, que no se sube al repositorio.</li>
     *   <li>Si la base local la creó una versión anterior, sin contraseña, se le asigna la nueva sin
     *       perder los datos.</li>
     * </ol>
     */
    private String prepararClave() throws IOException, SQLException {
        String desdeEntorno = System.getenv(VARIABLE_CLAVE);
        if (desdeEntorno != null && !desdeEntorno.isBlank()) {
            return desdeEntorno;
        }

        if (!url.startsWith(PREFIJO_BASE_LOCAL)) {
            return claveBaseDatos;
        }

        String base = url.substring("jdbc:h2:".length()).split(";")[0];
        Path archivoClave = Path.of(base + ".clave");
        if (Files.exists(archivoClave)) {
            return Files.readString(archivoClave, StandardCharsets.UTF_8).trim();
        }

        String nueva = generarClave();
        if (Files.exists(Path.of(base + ".mv.db"))) {
            asignarClaveABaseAnterior(nueva);
        }
        Files.writeString(archivoClave, nueva, StandardCharsets.UTF_8);
        return nueva;
    }

    private void asignarClaveABaseAnterior(String nueva) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, USER, CLAVE_VERSION_ANTERIOR);
                PreparedStatement statement = connection.prepareStatement("ALTER USER SA SET PASSWORD ?")) {
            statement.setString(1, nueva);
            statement.execute();
        }
    }

    private static String generarClave() {
        byte[] bytes = new byte[24];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Crea las tablas. Usa IF NOT EXISTS para que las bases creadas con versiones anteriores del
     * prototipo se actualicen sin perder datos.
     */
    private void crearTablas(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    nombre VARCHAR(120) NOT NULL,
                    correo VARCHAR(180) NOT NULL UNIQUE,
                    password_hash VARCHAR(100) NOT NULL,
                    rol VARCHAR(30) NOT NULL,
                    activo BOOLEAN NOT NULL DEFAULT TRUE
                )
                """);

        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS planes (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    codigo VARCHAR(50) UNIQUE,
                    objetivo VARCHAR(500) NOT NULL,
                    area VARCHAR(150) NOT NULL,
                    fecha_inicio DATE NOT NULL,
                    fecha_fin DATE NOT NULL,
                    estado VARCHAR(30) NOT NULL,
                    creado_por BIGINT NOT NULL,
                    FOREIGN KEY (creado_por) REFERENCES usuarios(id)
                )
                """);

        // HU-017: aprobación y retroalimentación.
        statement.executeUpdate("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS area VARCHAR(150)");
        statement.executeUpdate("ALTER TABLE planes ADD COLUMN IF NOT EXISTS fecha_envio TIMESTAMP");
        statement.executeUpdate("ALTER TABLE planes ADD COLUMN IF NOT EXISTS revisado_por BIGINT");
        statement.executeUpdate("ALTER TABLE planes ADD COLUMN IF NOT EXISTS fecha_revision TIMESTAMP");

        // HU-001: bloqueo de la cuenta tras varios intentos fallidos.
        statement.executeUpdate(
                "ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS intentos_fallidos INT NOT NULL DEFAULT 0");
        statement.executeUpdate("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS bloqueado_hasta TIMESTAMP");

        statement.executeUpdate("""
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
                """);

        statement.executeUpdate("""
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
                """);

        // Sin llave foránea hacia planes: el historial se conserva aunque el plan se elimine.
        statement.executeUpdate("""
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
                """);

        // HU-002 y HU-004: actividades del plan y cálculo del avance.
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS actividades (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    plan_id BIGINT NOT NULL,
                    descripcion VARCHAR(300) NOT NULL,
                    responsable_id BIGINT,
                    fecha_limite DATE NOT NULL,
                    estado VARCHAR(20) NOT NULL,
                    FOREIGN KEY (plan_id) REFERENCES planes(id) ON DELETE CASCADE,
                    FOREIGN KEY (responsable_id) REFERENCES usuarios(id)
                )
                """);
    }

    private void seedUser(Connection connection, String nombre, String correo, String clave, String rol,
            String area) throws SQLException {

        try (PreparedStatement consulta =
                connection.prepareStatement("SELECT id FROM usuarios WHERE LOWER(correo)=LOWER(?)")) {
            consulta.setString(1, correo);
            try (ResultSet result = consulta.executeQuery()) {
                if (result.next()) {
                    return;
                }
            }
        }

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO usuarios (nombre, correo, password_hash, rol, area) VALUES (?, ?, ?, ?, ?)")) {
            insert.setString(1, nombre);
            insert.setString(2, correo);
            insert.setString(3, BCrypt.hashpw(clave, BCrypt.gensalt()));
            insert.setString(4, rol);
            insert.setString(5, area);
            insert.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ HU-001: inicio de sesión

    /** Compatibilidad: devuelve el usuario solo si el inicio de sesión fue exitoso. */
    public Optional<User> authenticate(String correo, String clave) {
        return iniciarSesion(correo, clave).usuario();
    }

    /**
     * Valida las credenciales. Tras {@link #MAX_INTENTOS_FALLIDOS} intentos fallidos seguidos la
     * cuenta queda bloqueada durante {@link #DURACION_BLOQUEO}; mientras dure el bloqueo se rechaza
     * incluso la contraseña correcta. Un inicio de sesión exitoso reinicia el contador.
     */
    public ResultadoLogin iniciarSesion(String correo, String clave) {
        if (correo == null || clave == null) {
            return ResultadoLogin.invalido(MAX_INTENTOS_FALLIDOS);
        }

        String sql = """
                SELECT id, nombre, correo, password_hash, rol, area, intentos_fallidos, bloqueado_hasta
                FROM usuarios
                WHERE LOWER(correo)=LOWER(?) AND activo=TRUE
                """;

        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, correo.trim());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    // No se revela si el correo existe.
                    return ResultadoLogin.invalido(MAX_INTENTOS_FALLIDOS);
                }

                long id = result.getLong("id");
                // Se compara con instantes (Instant) y no con horas locales, para que un cambio de hora
                // o de zona horaria no alargue ni acorte el bloqueo (regla java:S8700).
                Instant ahora = reloj.instant();
                Timestamp bloqueadoHasta = result.getTimestamp("bloqueado_hasta");

                if (bloqueadoHasta != null && bloqueadoHasta.toInstant().isAfter(ahora)) {
                    long segundos = Duration.between(ahora, bloqueadoHasta.toInstant()).toSeconds();
                    return ResultadoLogin.bloqueado(Math.max(1, (segundos + 59) / 60));
                }

                // Si el bloqueo ya venció, el usuario empieza de nuevo con todos sus intentos.
                int intentos = bloqueadoHasta == null ? result.getInt("intentos_fallidos") : 0;

                if (BCrypt.checkpw(clave, result.getString("password_hash"))) {
                    actualizarIntentos(connection, id, 0, null);
                    return ResultadoLogin.exitoso(new User(
                            id,
                            result.getString("nombre"),
                            result.getString("correo"),
                            result.getString("rol"),
                            result.getString("area")));
                }

                intentos++;

                if (intentos >= MAX_INTENTOS_FALLIDOS) {
                    actualizarIntentos(connection, id, 0, ahora.plus(DURACION_BLOQUEO));
                    return ResultadoLogin.bloqueado(DURACION_BLOQUEO.toMinutes());
                }

                actualizarIntentos(connection, id, intentos, null);
                return ResultadoLogin.invalido(MAX_INTENTOS_FALLIDOS - intentos);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error en la base de datos: " + exception.getMessage(), exception);
        }
    }

    /** El administrador desbloquea una cuenta antes de que venza el bloqueo. */
    public void desbloquearUsuario(String correo, User administrador) {
        if (!Rol.ADMIN.equals(administrador.rol())) {
            throw new IllegalStateException("Solo el administrador puede desbloquear cuentas.");
        }

        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
                "UPDATE usuarios SET intentos_fallidos=0, bloqueado_hasta=NULL WHERE LOWER(correo)=LOWER(?)")) {
            statement.setString(1, correo.trim());
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe un usuario con el correo " + correo + ".");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error en la base de datos: " + exception.getMessage(), exception);
        }
    }

    private void actualizarIntentos(Connection connection, long usuarioId, int intentos, Instant hasta)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE usuarios SET intentos_fallidos=?, bloqueado_hasta=? WHERE id=?")) {
            statement.setInt(1, intentos);
            statement.setTimestamp(2, hasta == null ? null : Timestamp.from(hasta));
            statement.setLong(3, usuarioId);
            statement.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ HU-002: planes

    public Plan createPlan(String objetivo, String area, LocalDate inicio, LocalDate fin, User usuario) {
        validarPermiso(usuario);
        validarDatosPlan(objetivo, area, inicio, fin);

        String objetivoLimpio = objetivo.trim();
        String areaLimpia = area.trim();
        String estado = EstadoPlan.BORRADOR; // HU-017: todo plan nuevo empieza en borrador.

        return enTransaccion(connection -> {
            long id;

            try (PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO planes (objetivo, area, fecha_inicio, fecha_fin, estado, creado_por)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, objetivoLimpio);
                statement.setString(2, areaLimpia);
                statement.setDate(3, Date.valueOf(inicio));
                statement.setDate(4, Date.valueOf(fin));
                statement.setString(5, estado);
                statement.setLong(6, usuario.id());
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No fue posible obtener el ID.");
                    }
                    id = keys.getLong(1);
                }
            }

            String codigo = "PA-" + LocalDate.now(reloj).getYear() + "-" + String.format("%04d", id);

            try (PreparedStatement update = connection.prepareStatement("UPDATE planes SET codigo=? WHERE id=?")) {
                update.setString(1, codigo);
                update.setLong(2, id);
                update.executeUpdate();
            }

            registrarHistorial(connection, new PlanInfo(id, codigo, areaLimpia, null, usuario.id()), usuario,
                    "CREACION", estado, null);

            return new Plan(id, codigo, objetivoLimpio, areaLimpia, inicio, fin, estado);
        });
    }

    public void updatePlan(long id, String objetivo, String area, LocalDate inicio, LocalDate fin, User usuario) {
        validarPermiso(usuario);
        validarDatosPlan(objetivo, area, inicio, fin);

        ejecutarEnTransaccion(connection -> {
            PlanInfo plan = leerPlan(connection, id);

            // HU-017: un plan en revisión, aprobado o en ejecución ya no se modifica.
            if (!EstadoPlan.editable(plan.estado())) {
                throw new IllegalStateException("El plan " + plan.codigo() + " está en estado " + plan.estado()
                        + ". Solo se pueden modificar planes en estado " + EstadoPlan.BORRADOR + " o "
                        + EstadoPlan.DEVUELTO + ".");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE planes SET objetivo=?, area=?, fecha_inicio=?, fecha_fin=? WHERE id=?")) {
                statement.setString(1, objetivo.trim());
                statement.setString(2, area.trim());
                statement.setDate(3, Date.valueOf(inicio));
                statement.setDate(4, Date.valueOf(fin));
                statement.setLong(5, id);
                statement.executeUpdate();
            }

            registrarHistorial(connection, plan, usuario, "MODIFICACION", plan.estado(), null);
        });
    }

    /**
     * Elimina un plan. Solo se permite mientras el plan está en manos del coordinador (borrador o
     * devuelto): un plan en revisión, aprobado o en ejecución ya tiene trazabilidad que conservar.
     */
    public void deletePlan(long id, User usuario) {
        validarPermiso(usuario);

        ejecutarEnTransaccion(connection -> {
            PlanInfo plan = leerPlan(connection, id);

            if (!EstadoPlan.editable(plan.estado())) {
                throw new IllegalStateException("El plan " + plan.codigo() + " está en estado " + plan.estado()
                        + ". Solo se pueden eliminar planes en estado " + EstadoPlan.BORRADOR + " o "
                        + EstadoPlan.DEVUELTO + ".");
            }

            // El historial no tiene llave foránea hacia planes, así que esta entrada se conserva.
            registrarHistorial(connection, plan, usuario, "ELIMINACION", null, null);

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM planes WHERE id=?")) {
                statement.setLong(1, id);
                statement.executeUpdate();
            }
        });
    }

    public List<Plan> listPlans() {
        return consultarPlanes("1=1");
    }

    /** Valida los datos del plan también en el servidor, no solo en el formulario. */
    private static void validarDatosPlan(String objetivo, String area, LocalDate inicio, LocalDate fin) {
        if (objetivo == null || objetivo.isBlank()) {
            throw new IllegalArgumentException("El objetivo es obligatorio.");
        }
        if (objetivo.trim().length() > MAX_OBJETIVO) {
            throw new IllegalArgumentException("El objetivo no puede superar los " + MAX_OBJETIVO + " caracteres.");
        }
        if (area == null || area.isBlank()) {
            throw new IllegalArgumentException("El área es obligatoria.");
        }
        if (area.trim().length() > MAX_AREA) {
            throw new IllegalArgumentException("El área no puede superar los " + MAX_AREA + " caracteres.");
        }
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
    }

    // ------------------------------------------------------------------ HU-017: aprobación

    /** El coordinador envía el plan al director de área (enviarRevision en el diagrama de clases). */
    public void enviarARevision(long planId, User usuario) {
        validarPermiso(usuario);

        ejecutarEnTransaccion(connection -> {
            PlanInfo plan = leerPlan(connection, planId);

            if (!EstadoPlan.editable(plan.estado())) {
                throw new IllegalStateException("Solo se pueden enviar a revisión planes en estado "
                        + EstadoPlan.BORRADOR + " o " + EstadoPlan.DEVUELTO + ". El plan " + plan.codigo()
                        + " está en estado " + plan.estado() + ".");
            }

            if (contarObservacionesPendientes(connection, planId) > 0) {
                throw new IllegalStateException("El plan " + plan.codigo() + " tiene observaciones sin atender. "
                        + "Márquelas como atendidas antes de enviarlo de nuevo a revisión.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement("UPDATE planes SET estado=?, fecha_envio=? WHERE id=?")) {
                statement.setString(1, EstadoPlan.PENDIENTE_REVISION);
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now(reloj)));
                statement.setLong(3, planId);
                statement.executeUpdate();
            }

            registrarHistorial(connection, plan, usuario, "ENVIO_REVISION", EstadoPlan.PENDIENTE_REVISION, null);

            // Avisa a los directores que pueden revisar el plan (los de su área o sin área asignada).
            for (long directorId : directoresDelArea(connection, plan.area())) {
                notificar(connection, directorId, planId,
                        "El plan " + plan.codigo() + " del área " + plan.area() + " está pendiente de su revisión.");
            }
        });
    }

    /** El director aprueba un plan pendiente de revisión (aprobar en el diagrama de clases). */
    public void aprobarPlan(long planId, User director) {
        validarDirector(director);

        ejecutarEnTransaccion(connection -> {
            PlanInfo plan = leerPlan(connection, planId);
            validarAreaDirector(director, plan);

            if (!EstadoPlan.PENDIENTE_REVISION.equals(plan.estado())) {
                throw new IllegalStateException("Solo se pueden aprobar planes en estado "
                        + EstadoPlan.PENDIENTE_REVISION + ". El plan " + plan.codigo() + " está en estado "
                        + plan.estado() + ".");
            }

            if (contarObservacionesPendientes(connection, planId) > 0) {
                throw new IllegalStateException("El plan " + plan.codigo() + " tiene observaciones sin atender.");
            }

            registrarRevision(connection, planId, EstadoPlan.APROBADO, director);
            registrarHistorial(connection, plan, director, "APROBACION", EstadoPlan.APROBADO, null);
            notificar(connection, plan.creadoPor(), planId, "El plan " + plan.codigo() + " fue aprobado por "
                    + director.nombre() + ". Ya puede iniciar su ejecución.");
        });
    }

    /**
     * El director devuelve un plan con observaciones obligatorias (devolver en el diagrama de clases).
     * El plan pasa a {@link EstadoPlan#DEVUELTO} y se notifica al coordinador que lo creó.
     */
    public void devolverPlan(long planId, User director, String observacion) {
        validarDirector(director);

        if (observacion == null || observacion.isBlank()) {
            throw new IllegalArgumentException("Debe escribir las observaciones para devolver el plan.");
        }

        String texto = observacion.trim();

        if (texto.length() > MAX_OBSERVACION) {
            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar los " + MAX_OBSERVACION + " caracteres.");
        }

        ejecutarEnTransaccion(connection -> {
            PlanInfo plan = leerPlan(connection, planId);
            validarAreaDirector(director, plan);

            if (!EstadoPlan.PENDIENTE_REVISION.equals(plan.estado())) {
                throw new IllegalStateException("Solo se pueden devolver planes en estado "
                        + EstadoPlan.PENDIENTE_REVISION + ". El plan " + plan.codigo() + " está en estado "
                        + plan.estado() + ".");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO observaciones (plan_id, autor_id, descripcion, fecha) VALUES (?, ?, ?, ?)")) {
                statement.setLong(1, planId);
                statement.setLong(2, director.id());
                statement.setString(3, texto);
                statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now(reloj)));
                statement.executeUpdate();
            }

            registrarRevision(connection, planId, EstadoPlan.DEVUELTO, director);
            registrarHistorial(connection, plan, director, "DEVOLUCION", EstadoPlan.DEVUELTO, texto);
            notificar(connection, plan.creadoPor(), planId, "El plan " + plan.codigo() + " fue devuelto por "
                    + director.nombre() + " con observaciones: " + texto);
        });
    }

    /** El coordinador marca una observación como atendida (marcarAtendida en el diagrama de clases). */
    public void marcarObservacionAtendida(long observacionId, User usuario) {
        validarPermiso(usuario);

        ejecutarEnTransaccion(connection -> {
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
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now(reloj)));
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
            return EstadoPlan.APROBADO.equals(plan.estado()) && contarObservacionesPendientes(connection, planId) == 0;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void iniciarEjecucion(long planId, User usuario) {
        validarPermiso(usuario);

        ejecutarEnTransaccion(connection -> {
            PlanInfo plan = leerPlan(connection, planId);

            if (!EstadoPlan.APROBADO.equals(plan.estado())) {
                throw new IllegalStateException("El plan " + plan.codigo()
                        + " no se puede ejecutar: el director de área debe aprobarlo primero. Estado actual: "
                        + plan.estado() + ".");
            }

            if (contarObservacionesPendientes(connection, planId) > 0) {
                throw new IllegalStateException(
                        "El plan " + plan.codigo() + " tiene observaciones sin atender y no se puede ejecutar.");
            }

            actualizarEstado(connection, planId, EstadoPlan.EN_EJECUCION);
            registrarHistorial(connection, plan, usuario, "INICIO_EJECUCION", EstadoPlan.EN_EJECUCION, null);
        });
    }

    /**
     * Finaliza un plan en ejecución. Si el plan tiene actividades, todas deben estar finalizadas
     * (avance del 100 %, HU-004).
     */
    public void finalizarPlan(long planId, User usuario) {
        validarPermiso(usuario);

        ejecutarEnTransaccion(connection -> {
            PlanInfo plan = leerPlan(connection, planId);

            if (!EstadoPlan.EN_EJECUCION.equals(plan.estado())) {
                throw new IllegalStateException("Solo se pueden finalizar planes en estado "
                        + EstadoPlan.EN_EJECUCION + ". El plan " + plan.codigo() + " está en estado "
                        + plan.estado() + ".");
            }

            int[] conteo = contarActividades(connection, planId);
            if (conteo[0] > 0 && conteo[1] < conteo[0]) {
                throw new IllegalStateException("El plan " + plan.codigo() + " tiene " + (conteo[0] - conteo[1])
                        + " actividad(es) sin finalizar. Su avance es " + porcentaje(conteo) + " %.");
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

        return consultarPlanes("estado=? AND LOWER(TRIM(area))=LOWER(TRIM(?))", EstadoPlan.PENDIENTE_REVISION,
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
        String sql = """
                SELECT o.id, o.plan_id, u.nombre AS autor, o.descripcion, o.fecha, o.atendida
                FROM observaciones o
                JOIN usuarios u ON u.id=o.autor_id
                WHERE o.plan_id=?
                ORDER BY o.fecha DESC, o.id DESC
                """;

        return consultar(sql, result -> new Observacion(
                result.getLong("id"),
                result.getLong("plan_id"),
                result.getString("autor"),
                result.getString("descripcion"),
                result.getTimestamp("fecha").toLocalDateTime(),
                result.getBoolean("atendida")), planId);
    }

    // ------------------------------------------------------------------ notificaciones e historial

    public List<Notificacion> listarNotificaciones(User usuario) {
        return consultar(
                "SELECT id, mensaje, fecha, leida FROM notificaciones WHERE usuario_id=? ORDER BY fecha DESC, id DESC",
                result -> new Notificacion(
                        result.getLong("id"),
                        result.getString("mensaje"),
                        result.getTimestamp("fecha").toLocalDateTime(),
                        result.getBoolean("leida")),
                usuario.id());
    }

    public int contarNotificacionesSinLeer(User usuario) {
        return consultar("SELECT COUNT(*) FROM notificaciones WHERE usuario_id=? AND leida=FALSE",
                result -> result.getInt(1), usuario.id()).get(0);
    }

    public void marcarNotificacionesLeidas(User usuario) {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
                "UPDATE notificaciones SET leida=TRUE WHERE usuario_id=? AND leida=FALSE")) {
            statement.setLong(1, usuario.id());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public List<RegistroHistorial> listarHistorial(long planId) {
        String sql = """
                SELECT h.accion, h.estado_anterior, h.estado_nuevo, u.nombre AS usuario, h.detalle, h.fecha
                FROM historial_planes h
                JOIN usuarios u ON u.id=h.usuario_id
                WHERE h.plan_id=?
                ORDER BY h.fecha, h.id
                """;

        return consultar(sql, result -> new RegistroHistorial(
                result.getString("accion"),
                result.getString("estado_anterior"),
                result.getString("estado_nuevo"),
                result.getString("usuario"),
                result.getString("detalle"),
                result.getTimestamp("fecha").toLocalDateTime()), planId);
    }

    // ------------------------------------------------------------------ HU-002 y HU-004: actividades

    /** Usuarios activos con rol RESPONSABLE, para asignarlos a actividades. */
    public List<User> listarResponsables() {
        return consultar("SELECT id, nombre, correo, rol, area FROM usuarios WHERE rol=? AND activo=TRUE ORDER BY nombre",
                result -> new User(
                        result.getLong("id"),
                        result.getString("nombre"),
                        result.getString("correo"),
                        result.getString("rol"),
                        result.getString("area")),
                Rol.RESPONSABLE);
    }

    /**
     * Agrega una actividad a un plan. Solo mientras el plan está en manos del coordinador (borrador o
     * devuelto), igual que el resto de la planeación.
     */
    public Actividad agregarActividad(long planId, String descripcion, Long responsableId, LocalDate fechaLimite,
            User usuario) {
        validarPermiso(usuario);

        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción de la actividad es obligatoria.");
        }
        if (descripcion.trim().length() > MAX_DESCRIPCION_ACTIVIDAD) {
            throw new IllegalArgumentException("La descripción no puede superar los "
                    + MAX_DESCRIPCION_ACTIVIDAD + " caracteres.");
        }
        if (fechaLimite == null) {
            throw new IllegalArgumentException("La fecha límite de la actividad es obligatoria.");
        }

        String texto = descripcion.trim();

        return enTransaccion(connection -> {
            PlanInfo plan = leerPlan(connection, planId);
            validarPlanEditableParaActividades(plan);

            LocalDate[] fechas = leerFechasPlan(connection, planId);
            if (fechaLimite.isBefore(fechas[0]) || fechaLimite.isAfter(fechas[1])) {
                throw new IllegalArgumentException("La fecha límite debe estar entre " + fechas[0] + " y "
                        + fechas[1] + ", las fechas del plan.");
            }

            String responsable = null;
            if (responsableId != null) {
                responsable = leerNombreResponsable(connection, responsableId);
            }

            long id;
            try (PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO actividades (plan_id, descripcion, responsable_id, fecha_limite, estado)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, planId);
                statement.setString(2, texto);
                if (responsableId == null) {
                    statement.setNull(3, Types.BIGINT);
                } else {
                    statement.setLong(3, responsableId);
                }
                statement.setDate(4, Date.valueOf(fechaLimite));
                statement.setString(5, EstadoActividad.PENDIENTE);
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getLong(1);
                }
            }

            registrarHistorial(connection, plan, usuario, "ACTIVIDAD_AGREGADA", plan.estado(), texto);

            return new Actividad(id, planId, texto, responsableId, responsable, fechaLimite,
                    EstadoActividad.PENDIENTE);
        });
    }

    public void eliminarActividad(long actividadId, User usuario) {
        validarPermiso(usuario);

        ejecutarEnTransaccion(connection -> {
            Actividad actividad = leerActividad(connection, actividadId);
            PlanInfo plan = leerPlan(connection, actividad.planId());
            validarPlanEditableParaActividades(plan);

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM actividades WHERE id=?")) {
                statement.setLong(1, actividadId);
                statement.executeUpdate();
            }

            registrarHistorial(connection, plan, usuario, "ACTIVIDAD_ELIMINADA", plan.estado(),
                    actividad.descripcion());
        });
    }

    /**
     * Cambia el estado de una actividad mientras el plan está en ejecución. Lo pueden hacer el
     * administrador, el coordinador o el responsable asignado a la actividad.
     */
    public void cambiarEstadoActividad(long actividadId, String nuevoEstado, User usuario) {
        if (!EstadoActividad.TODOS.contains(nuevoEstado)) {
            throw new IllegalArgumentException("Estado de actividad no válido: " + nuevoEstado + ".");
        }

        ejecutarEnTransaccion(connection -> {
            Actividad actividad = leerActividad(connection, actividadId);
            PlanInfo plan = leerPlan(connection, actividad.planId());

            boolean esResponsable = actividad.responsableId() != null && actividad.responsableId() == usuario.id();
            if (!Rol.gestionaPlanes(usuario.rol()) && !esResponsable) {
                throw new IllegalStateException(
                        "Solo el coordinador o el responsable asignado pueden actualizar esta actividad.");
            }

            if (!EstadoPlan.EN_EJECUCION.equals(plan.estado())) {
                throw new IllegalStateException("El avance solo se registra en planes en estado "
                        + EstadoPlan.EN_EJECUCION + ". El plan " + plan.codigo() + " está en estado "
                        + plan.estado() + ".");
            }

            if (nuevoEstado.equals(actividad.estado())) {
                return;
            }

            try (PreparedStatement statement =
                    connection.prepareStatement("UPDATE actividades SET estado=? WHERE id=?")) {
                statement.setString(1, nuevoEstado);
                statement.setLong(2, actividadId);
                statement.executeUpdate();
            }

            registrarHistorial(connection, plan, usuario, "AVANCE_ACTIVIDAD", plan.estado(),
                    actividad.descripcion() + ": " + actividad.estado() + " -> " + nuevoEstado);
        });
    }

    public List<Actividad> listarActividades(long planId) {
        String sql = """
                SELECT a.id, a.plan_id, a.descripcion, a.responsable_id, u.nombre AS responsable,
                       a.fecha_limite, a.estado
                FROM actividades a
                LEFT JOIN usuarios u ON u.id=a.responsable_id
                WHERE a.plan_id=?
                ORDER BY a.fecha_limite, a.id
                """;

        return consultar(sql, DataService::mapearActividad, planId);
    }

    /** HU-004: avance del plan = actividades finalizadas / total de actividades, en porcentaje entero. */
    public int calcularAvance(long planId) {
        try (Connection connection = connect()) {
            return porcentaje(contarActividades(connection, planId));
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static Actividad mapearActividad(ResultSet result) throws SQLException {
        // wasNull() se refiere a la última columna leída, así que se consulta justo después.
        long valor = result.getLong("responsable_id");
        Long responsableId = result.wasNull() ? null : valor;
        return new Actividad(
                result.getLong("id"),
                result.getLong("plan_id"),
                result.getString("descripcion"),
                responsableId,
                result.getString("responsable"),
                result.getDate("fecha_limite").toLocalDate(),
                result.getString("estado"));
    }

    private Actividad leerActividad(Connection connection, long actividadId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT a.id, a.plan_id, a.descripcion, a.responsable_id, u.nombre AS responsable,
                       a.fecha_limite, a.estado
                FROM actividades a
                LEFT JOIN usuarios u ON u.id=a.responsable_id
                WHERE a.id=?
                """)) {
            statement.setLong(1, actividadId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("La actividad no existe.");
                }
                return mapearActividad(result);
            }
        }
    }

    private String leerNombreResponsable(Connection connection, long usuarioId) throws SQLException {
        try (PreparedStatement statement =
                connection.prepareStatement("SELECT nombre, rol FROM usuarios WHERE id=? AND activo=TRUE")) {
            statement.setLong(1, usuarioId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("El responsable seleccionado no existe.");
                }
                if (!Rol.RESPONSABLE.equals(result.getString("rol"))) {
                    throw new IllegalArgumentException("Solo se pueden asignar usuarios con rol RESPONSABLE.");
                }
                return result.getString("nombre");
            }
        }
    }

    private LocalDate[] leerFechasPlan(Connection connection, long planId) throws SQLException {
        try (PreparedStatement statement =
                connection.prepareStatement("SELECT fecha_inicio, fecha_fin FROM planes WHERE id=?")) {
            statement.setLong(1, planId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return new LocalDate[] {
                    result.getDate("fecha_inicio").toLocalDate(), result.getDate("fecha_fin").toLocalDate()
                };
            }
        }
    }

    /** Devuelve {total, finalizadas}. */
    private int[] contarActividades(Connection connection, long planId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*), COUNT(CASE WHEN estado=? THEN 1 END) FROM actividades WHERE plan_id=?")) {
            statement.setString(1, EstadoActividad.FINALIZADA);
            statement.setLong(2, planId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return new int[] {result.getInt(1), result.getInt(2)};
            }
        }
    }

    private static int porcentaje(int[] conteo) {
        return conteo[0] == 0 ? 0 : conteo[1] * 100 / conteo[0];
    }

    private static void validarPlanEditableParaActividades(PlanInfo plan) {
        if (!EstadoPlan.editable(plan.estado())) {
            throw new IllegalStateException("Las actividades solo se agregan o eliminan en planes en estado "
                    + EstadoPlan.BORRADOR + " o " + EstadoPlan.DEVUELTO + ". El plan " + plan.codigo()
                    + " está en estado " + plan.estado() + ".");
        }
    }

    // ------------------------------------------------------------------ apoyo

    /** Datos del plan que necesitan las reglas de negocio. */
    private record PlanInfo(long id, String codigo, String area, String estado, long creadoPor) {}

    @FunctionalInterface
    private interface OperacionBD<T> {
        T ejecutar(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    private interface OperacionSinResultado {
        void ejecutar(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    private interface Mapeador<T> {
        T mapear(ResultSet result) throws SQLException;
    }

    /** Ejecuta la operación en una transacción: si algo falla, no queda ningún cambio a medias. */
    private <T> T enTransaccion(OperacionBD<T> operacion) {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(connection);
                connection.commit();
                return resultado;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error en la base de datos: " + exception.getMessage(), exception);
        }
    }

    private void ejecutarEnTransaccion(OperacionSinResultado operacion) {
        enTransaccion(connection -> {
            operacion.ejecutar(connection);
            return null;
        });
    }

    private <T> List<T> consultar(String sql, Mapeador<T> mapeador, Object... parametros) {
        List<T> lista = new ArrayList<>();

        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                statement.setObject(i + 1, parametros[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    lista.add(mapeador.mapear(result));
                }
            }
            return lista;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    private List<Plan> consultarPlanes(String condicion, Object... parametros) {
        String sql = "SELECT id, codigo, objetivo, area, fecha_inicio, fecha_fin, estado FROM planes WHERE "
                + condicion + " ORDER BY id DESC";

        return consultar(sql, result -> new Plan(
                result.getLong("id"),
                result.getString("codigo"),
                result.getString("objetivo"),
                result.getString("area"),
                result.getDate("fecha_inicio").toLocalDate(),
                result.getDate("fecha_fin").toLocalDate(),
                result.getString("estado")), parametros);
    }

    private PlanInfo leerPlan(Connection connection, long planId) throws SQLException {
        try (PreparedStatement statement =
                connection.prepareStatement("SELECT id, codigo, area, estado, creado_por FROM planes WHERE id=?")) {
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

    private List<Long> directoresDelArea(Connection connection, String area) throws SQLException {
        List<Long> ids = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM usuarios WHERE rol=? AND activo=TRUE "
                + "AND (area IS NULL OR LOWER(TRIM(area))=LOWER(TRIM(?)))")) {
            statement.setString(1, Rol.DIRECTOR);
            statement.setString(2, area);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ids.add(result.getLong(1));
                }
            }
        }
        return ids;
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
        try (PreparedStatement statement = connection.prepareStatement("UPDATE planes SET estado=? WHERE id=?")) {
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
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now(reloj)));
            statement.setLong(4, planId);
            statement.executeUpdate();
        }
    }

    /** Deja el cambio en el historial con el estado anterior y el nuevo (RNF-10). */
    private void registrarHistorial(Connection connection, PlanInfo plan, User usuario, String accion,
            String estadoNuevo, String detalle) throws SQLException {
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
            statement.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now(reloj)));
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
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now(reloj)));
            statement.executeUpdate();
        }
    }

    private void validarDirector(User usuario) {
        if (!Rol.DIRECTOR.equals(usuario.rol())) {
            throw new IllegalStateException("Solo el director de área puede aprobar o devolver planes de acción.");
        }
    }

    private void validarAreaDirector(User director, PlanInfo plan) {
        if (director.area() != null && !normalizar(director.area()).equals(normalizar(plan.area()))) {
            throw new IllegalStateException("El plan " + plan.codigo() + " es del área " + plan.area()
                    + ". Solo lo puede revisar el director de esa área.");
        }
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
    }

    private void validarPermiso(User usuario) {
        if (!Rol.gestionaPlanes(usuario.rol())) {
            throw new IllegalStateException("El usuario no tiene permisos para modificar planes.");
        }
    }
}
