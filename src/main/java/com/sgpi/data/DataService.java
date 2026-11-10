package com.sgpi.data;

import com.sgpi.model.Actividad;
import com.sgpi.model.Area;
import com.sgpi.model.CorreoSaliente;
import com.sgpi.model.EstadoActividad;
import com.sgpi.model.Evidencia;
import com.sgpi.model.FrecuenciaCorreo;
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
import java.text.Normalizer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
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

    /** HU-010: largo máximo del código de un área (letras mayúsculas, números y guion). */
    public static final int MAX_CODIGO_AREA = 10;
    public static final int MAX_OBSERVACION = 1000;
    public static final int MAX_DESCRIPCION_ACTIVIDAD = 300;

    /** HU-003: tamaño máximo de una evidencia (10 MB, según el issue #18) y formatos permitidos. */
    public static final int TAMANO_MAXIMO_EVIDENCIA = 10 * 1024 * 1024;
    public static final String FORMATOS_VALIDOS = "PDF, DOCX, JPG y PNG";
    public static final int MAX_NOMBRE_EVIDENCIA = 255;

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

    /** HU-015: hora a la que sale el resumen diario de notificaciones. */
    public static final LocalTime HORA_RESUMEN_DIARIO = LocalTime.of(7, 0);

    /** HU-015: intentos de envío de un correo antes de dejarlo con error. */
    public static final int MAX_INTENTOS_CORREO = 3;

    /** HU-015: variable de entorno con la dirección web del SGPI, para poner el enlace del plan en el correo. */
    static final String VARIABLE_URL_BASE = "SGPI_URL_BASE";

    private static final String ASUNTO_RESUMEN = "SGPI: resumen diario de notificaciones";
    private static final int MAX_CUERPO_CORREO = 8000;
    private static final String SEPARADOR_RESUMEN = "\n\n----------\n\n";

    /** HU-010: áreas que se crean en una base nueva, con su código. */
    private static final String[][] AREAS_INICIALES = {
        {"CAL", "Calidad"},
        {"SIS", "Sistemas"},
        {"PLA", "Planeación"},
        {"THU", "Talento Humano"}
    };

    private static final String SQL_AREAS = """
            SELECT a.id, a.codigo, a.nombre, a.activa,
                   (SELECT COUNT(*) FROM planes p WHERE LOWER(TRIM(p.area))=LOWER(a.nombre)) AS planes,
                   (SELECT COUNT(*) FROM usuarios u
                    WHERE u.activo=TRUE AND LOWER(TRIM(u.area))=LOWER(a.nombre)) AS usuarios
            FROM areas a
            """;

    private static final String SELECT_PLANES =
            "SELECT id, codigo, objetivo, area, fecha_inicio, fecha_fin, estado FROM planes ";
    private static final String SQL_TODOS_LOS_PLANES = SELECT_PLANES + "ORDER BY id DESC";
    private static final String SQL_PLANES_PENDIENTES = SELECT_PLANES + "WHERE estado=? ORDER BY id DESC";
    private static final String SQL_PLANES_PENDIENTES_AREA = SELECT_PLANES
            + "WHERE estado=? AND LOWER(TRIM(area))=LOWER(TRIM(?)) ORDER BY id DESC";
    private static final String SQL_PLANES_AREA = SELECT_PLANES
            + "WHERE LOWER(TRIM(area))=LOWER(TRIM(?)) ORDER BY id DESC";

    private final String url;
    private final Clock reloj;
    private final UnaryOperator<String> entorno;

    /** Contraseña de la base. Las bases en memoria usan una aleatoria, distinta en cada instancia. */
    private String claveBaseDatos = generarClave();

    /** HU-015: envío de correos ({@code null} si no hay servidor configurado) y dónde se ejecuta. */
    private EnvioCorreo envioCorreo;
    private Executor ejecutorCorreo;
    private boolean hayCorreosNuevos;
    private ScheduledExecutorService revisionPeriodica;

    public DataService() {
        this(URL_POR_DEFECTO);
    }

    /** Permite usar otra base de datos, por ejemplo una en memoria para las pruebas. */
    public DataService(String url) {
        this(url, Clock.systemDefaultZone());
    }

    /** Permite además fijar el reloj, para probar reglas que dependen del tiempo (bloqueo de cuentas). */
    public DataService(String url, Clock reloj) {
        this(url, reloj, System::getenv);
    }

    /**
     * Permite además fijar de dónde se leen las variables de entorno, para que las pruebas no dependan de
     * las variables que tenga definidas el equipo donde corren (por ejemplo {@value #VARIABLE_CLAVE}).
     */
    DataService(String url, Clock reloj, UnaryOperator<String> entorno) {
        this.url = url;
        this.reloj = reloj;
        this.entorno = entorno;
        this.envioCorreo = CorreoSmtp.desdeEntorno(entorno);
        // Los correos se envían en un hilo aparte, para que un servidor lento no congele la ventana.
        this.ejecutorCorreo = Executors.newSingleThreadExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "sgpi-correo");
            hilo.setDaemon(true);
            return hilo;
        });
    }

    /** Permite a las pruebas cambiar el envío de correos y enviarlos en el mismo hilo. */
    void usarEnvioCorreo(EnvioCorreo envio, Executor ejecutor) {
        this.envioCorreo = envio;
        this.ejecutorCorreo = ejecutor;
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

                registrarAreasIniciales(connection);
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
        String desdeEntorno = entorno.apply(VARIABLE_CLAVE);
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

        // HU-010: estructura organizacional. Los planes y los usuarios guardan el nombre del área.
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS areas (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    codigo VARCHAR(10) NOT NULL UNIQUE,
                    nombre VARCHAR(150) NOT NULL UNIQUE,
                    activa BOOLEAN NOT NULL DEFAULT TRUE
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

        // HU-003: evidencias documentales de cada actividad (RN-03). El archivo se guarda en la base.
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS evidencias (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    actividad_id BIGINT NOT NULL,
                    nombre_archivo VARCHAR(255) NOT NULL,
                    formato VARCHAR(10) NOT NULL,
                    tamano BIGINT NOT NULL,
                    contenido BLOB NOT NULL,
                    cargado_por BIGINT NOT NULL,
                    cargado_en TIMESTAMP NOT NULL,
                    FOREIGN KEY (actividad_id) REFERENCES actividades(id) ON DELETE CASCADE,
                    FOREIGN KEY (cargado_por) REFERENCES usuarios(id)
                )
                """);

        // HU-015: preferencia de correo de cada usuario y correos de notificación por enviar.
        statement.executeUpdate("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS frecuencia_correo VARCHAR(20) "
                + "NOT NULL DEFAULT '" + FrecuenciaCorreo.INMEDIATA + "'");
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS correos (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    usuario_id BIGINT NOT NULL,
                    destinatario VARCHAR(180) NOT NULL,
                    asunto VARCHAR(200) NOT NULL,
                    cuerpo VARCHAR(8000) NOT NULL,
                    creado_en TIMESTAMP NOT NULL,
                    enviar_desde TIMESTAMP NOT NULL,
                    enviado_en TIMESTAMP,
                    intentos INT NOT NULL DEFAULT 0,
                    ultimo_error VARCHAR(500),
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
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
        String estado = EstadoPlan.BORRADOR; // HU-017: todo plan nuevo empieza en borrador.

        return enTransaccion(connection -> {
            // HU-010: el área debe estar registrada y activa.
            String areaLimpia = resolverArea(connection, area, null);
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

            // HU-010: un plan puede conservar su área aunque se haya desactivado, pero no pasar a otra inactiva.
            String areaLimpia = resolverArea(connection, area, plan.area());

            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE planes SET objetivo=?, area=?, fecha_inicio=?, fecha_fin=? WHERE id=?")) {
                statement.setString(1, objetivo.trim());
                statement.setString(2, areaLimpia);
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
        return consultarPlanes(SQL_TODOS_LOS_PLANES);
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

    // ------------------------------------------------------------------ HU-010: estructura organizacional

    /** Todas las áreas, con la cantidad de planes y de usuarios activos de cada una. */
    public List<Area> listarAreas() {
        return consultar(SQL_AREAS + "ORDER BY a.nombre", DataService::mapearArea);
    }

    /** Áreas que se pueden asignar a un plan. */
    public List<Area> listarAreasActivas() {
        return consultar(SQL_AREAS + "WHERE a.activa=TRUE ORDER BY a.nombre", DataService::mapearArea);
    }

    /** El administrador registra un área con su código y su nombre; queda activa para asignar planes. */
    public Area crearArea(String codigo, String nombre, User administrador) {
        validarAdministrador(administrador);
        String codigoLimpio = validarCodigoArea(codigo);
        String nombreLimpio = validarNombreArea(nombre);

        return enTransaccion(connection -> {
            validarAreaUnica(connection, codigoLimpio, nombreLimpio, null);

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO areas (codigo, nombre, activa) VALUES (?, ?, TRUE)",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, codigoLimpio);
                statement.setString(2, nombreLimpio);
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    return new Area(keys.getLong(1), codigoLimpio, nombreLimpio, true, 0, 0);
                }
            }
        });
    }

    /**
     * Cambia el código o el nombre de un área. Si cambia el nombre, los planes y los usuarios del área
     * quedan con el nombre nuevo, para que no pierdan la relación.
     */
    public void actualizarArea(long areaId, String codigo, String nombre, User administrador) {
        validarAdministrador(administrador);
        String codigoLimpio = validarCodigoArea(codigo);
        String nombreLimpio = validarNombreArea(nombre);

        ejecutarEnTransaccion(connection -> {
            Area actual = leerArea(connection, areaId);
            validarAreaUnica(connection, codigoLimpio, nombreLimpio, areaId);

            try (PreparedStatement statement =
                    connection.prepareStatement("UPDATE areas SET codigo=?, nombre=? WHERE id=?")) {
                statement.setString(1, codigoLimpio);
                statement.setString(2, nombreLimpio);
                statement.setLong(3, areaId);
                statement.executeUpdate();
            }

            if (!actual.nombre().equals(nombreLimpio)) {
                for (String tabla : new String[] {"planes", "usuarios"}) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE " + tabla + " SET area=? WHERE LOWER(TRIM(area))=LOWER(?)")) {
                        statement.setString(1, nombreLimpio);
                        statement.setString(2, actual.nombre());
                        statement.executeUpdate();
                    }
                }
            }
        });
    }

    /**
     * Activa o desactiva un área. Un área desactivada no se ofrece para planes nuevos, pero sus planes y
     * usuarios la conservan.
     */
    public void cambiarEstadoArea(long areaId, boolean activa, User administrador) {
        validarAdministrador(administrador);

        ejecutarEnTransaccion(connection -> {
            leerArea(connection, areaId);
            try (PreparedStatement statement = connection.prepareStatement("UPDATE areas SET activa=? WHERE id=?")) {
                statement.setBoolean(1, activa);
                statement.setLong(2, areaId);
                statement.executeUpdate();
            }
        });
    }

    /**
     * Elimina un área. Regla de HU-010: no se elimina un área con planes o usuarios activos asociados; en
     * ese caso se sugiere desactivarla.
     */
    public void eliminarArea(long areaId, User administrador) {
        validarAdministrador(administrador);

        ejecutarEnTransaccion(connection -> {
            Area area = leerArea(connection, areaId);

            if (area.tieneAsociados()) {
                throw new IllegalStateException("El área " + area.nombre() + " tiene " + area.planes()
                        + " plan(es) y " + area.usuarios() + " usuario(s) activo(s) asociados, así que no se "
                        + "puede eliminar. Puede desactivarla para que no se asigne a planes nuevos.");
            }

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM areas WHERE id=?")) {
                statement.setLong(1, areaId);
                statement.executeUpdate();
            }
        });
    }

    /**
     * Crea las áreas iniciales en una base nueva y registra las áreas que ya usaban los planes y los
     * usuarios de una base anterior, para que sigan siendo válidas.
     */
    private void registrarAreasIniciales(Connection connection) throws SQLException {
        boolean vacia;
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM areas")) {
            result.next();
            vacia = result.getInt(1) == 0;
        }

        if (vacia) {
            for (String[] area : AREAS_INICIALES) {
                insertarAreaSiFalta(connection, area[0], area[1]);
            }
        }

        List<String> enUso = new ArrayList<>();
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("""
                        SELECT DISTINCT TRIM(area) FROM planes WHERE TRIM(area)<>''
                        UNION
                        SELECT DISTINCT TRIM(area) FROM usuarios WHERE area IS NOT NULL AND TRIM(area)<>''
                        """)) {
            while (result.next()) {
                enUso.add(result.getString(1));
            }
        }

        for (String nombre : enUso) {
            insertarAreaSiFalta(connection, codigoSugerido(connection, nombre), nombre);
        }
    }

    private void insertarAreaSiFalta(Connection connection, String codigo, String nombre) throws SQLException {
        try (PreparedStatement consulta =
                connection.prepareStatement("SELECT 1 FROM areas WHERE LOWER(nombre)=LOWER(?)")) {
            consulta.setString(1, nombre);
            try (ResultSet result = consulta.executeQuery()) {
                if (result.next()) {
                    return;
                }
            }
        }

        try (PreparedStatement insert =
                connection.prepareStatement("INSERT INTO areas (codigo, nombre, activa) VALUES (?, ?, TRUE)")) {
            insert.setString(1, codigo);
            insert.setString(2, nombre);
            insert.executeUpdate();
        }
    }

    /** Código a partir de las tres primeras letras del nombre, sin tildes; si ya existe, se le agrega un número. */
    private static String codigoSugerido(Connection connection, String nombre) throws SQLException {
        String letras = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
        String base = letras.isEmpty() ? "AREA" : letras.substring(0, Math.min(3, letras.length()));

        String codigo = base;
        for (int numero = 2; existeCodigoArea(connection, codigo); numero++) {
            codigo = base + numero;
        }
        return codigo;
    }

    private static boolean existeCodigoArea(Connection connection, String codigo) throws SQLException {
        try (PreparedStatement statement =
                connection.prepareStatement("SELECT 1 FROM areas WHERE UPPER(codigo)=UPPER(?)")) {
            statement.setString(1, codigo);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /**
     * Devuelve el nombre registrado del área (sin espacios de más y con sus mayúsculas). El área debe
     * existir y estar activa, salvo que sea la que el plan ya tenía.
     */
    private String resolverArea(Connection connection, String area, String areaActual) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT nombre, activa FROM areas WHERE LOWER(nombre)=LOWER(TRIM(?))")) {
            statement.setString(1, area);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("El área \"" + area.trim() + "\" no está registrada. "
                            + "El administrador puede crearla en la opción Áreas.");
                }

                String nombre = result.getString("nombre");
                boolean mismaArea = normalizar(nombre).equals(normalizar(areaActual));
                if (!result.getBoolean("activa") && !mismaArea) {
                    throw new IllegalArgumentException(
                            "El área " + nombre + " está desactivada y no se puede asignar a planes.");
                }
                return nombre;
            }
        }
    }

    private Area leerArea(Connection connection, long areaId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_AREAS + "WHERE a.id=?")) {
            statement.setLong(1, areaId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("El área no existe.");
                }
                return mapearArea(result);
            }
        }
    }

    private static void validarAreaUnica(Connection connection, String codigo, String nombre, Long excepto)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT codigo, nombre FROM areas WHERE (UPPER(codigo)=UPPER(?) OR LOWER(nombre)=LOWER(?)) "
                + "AND id<>?")) {
            statement.setString(1, codigo);
            statement.setString(2, nombre);
            statement.setLong(3, excepto == null ? -1 : excepto);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    boolean mismoCodigo = result.getString("codigo").equalsIgnoreCase(codigo);
                    throw new IllegalArgumentException(mismoCodigo
                            ? "Ya existe un área con el código " + codigo + "."
                            : "Ya existe un área con el nombre " + result.getString("nombre") + ".");
                }
            }
        }
    }

    private static String validarCodigoArea(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del área es obligatorio.");
        }
        String limpio = codigo.trim().toUpperCase(Locale.ROOT);
        if (limpio.length() > MAX_CODIGO_AREA || !limpio.matches("[A-Z0-9-]+")) {
            throw new IllegalArgumentException("El código del área debe tener hasta " + MAX_CODIGO_AREA
                    + " caracteres, solo letras sin tilde, números y guion.");
        }
        return limpio;
    }

    private static String validarNombreArea(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del área es obligatorio.");
        }
        String limpio = nombre.trim().replaceAll("\\s+", " ");
        if (limpio.length() > MAX_AREA) {
            throw new IllegalArgumentException("El nombre del área no puede superar los " + MAX_AREA + " caracteres.");
        }
        return limpio;
    }

    private static Area mapearArea(ResultSet result) throws SQLException {
        return new Area(
                result.getLong("id"),
                result.getString("codigo"),
                result.getString("nombre"),
                result.getBoolean("activa"),
                result.getInt("planes"),
                result.getInt("usuarios"));
    }

    private static void validarAdministrador(User usuario) {
        if (!Rol.ADMIN.equals(usuario.rol())) {
            throw new IllegalStateException("Solo el administrador puede gestionar la estructura organizacional.");
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
                notificar(connection, directorId, plan, "SGPI: plan " + plan.codigo() + " pendiente de su revisión",
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
            notificar(connection, plan.creadoPor(), plan, "SGPI: plan " + plan.codigo() + " aprobado",
                    "El plan " + plan.codigo() + " fue aprobado por "
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
            notificar(connection, plan.creadoPor(), plan, "SGPI: plan " + plan.codigo() + " devuelto con observaciones",
                    "El plan " + plan.codigo() + " fue devuelto por "
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
            return consultarPlanes(SQL_PLANES_PENDIENTES, EstadoPlan.PENDIENTE_REVISION);
        }

        return consultarPlanes(SQL_PLANES_PENDIENTES_AREA, EstadoPlan.PENDIENTE_REVISION,
                director.area());
    }

    public List<Plan> listarPlanesArea(User director) {
        validarDirector(director);

        if (director.area() == null) {
            return consultarPlanes(SQL_TODOS_LOS_PLANES);
        }

        return consultarPlanes(SQL_PLANES_AREA, director.area());
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

    // ------------------------------------------------------------------ HU-015: correos de notificación

    /** Indica si hay un servidor de correo configurado; si no, los correos quedan pendientes. */
    public boolean correoConfigurado() {
        return envioCorreo != null;
    }

    /** Frecuencia con la que el usuario recibe sus notificaciones por correo. */
    public String obtenerFrecuenciaCorreo(User usuario) {
        List<String> valor = consultar("SELECT frecuencia_correo FROM usuarios WHERE id=?",
                result -> result.getString(1), usuario.id());
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        return valor.get(0);
    }

    /**
     * Regla de negocio de HU-015: cada usuario elige la frecuencia de sus correos o los desactiva. Las
     * notificaciones dentro del sistema no cambian.
     */
    public void cambiarFrecuenciaCorreo(User usuario, String frecuencia) {
        if (!FrecuenciaCorreo.TODAS.contains(frecuencia)) {
            throw new IllegalArgumentException("Frecuencia de correo no válida: " + frecuencia + ".");
        }

        ejecutarEnTransaccion(connection -> {
            try (PreparedStatement statement =
                    connection.prepareStatement("UPDATE usuarios SET frecuencia_correo=? WHERE id=?")) {
                statement.setString(1, frecuencia);
                statement.setLong(2, usuario.id());
                statement.executeUpdate();
            }

            // Si desactiva los correos, los que estaban esperando el resumen diario ya no se envían.
            if (FrecuenciaCorreo.DESACTIVADA.equals(frecuencia)) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM correos WHERE usuario_id=? AND enviado_en IS NULL")) {
                    statement.setLong(1, usuario.id());
                    statement.executeUpdate();
                }
            }
        });
    }

    /** Correos de notificación del usuario, del más reciente al más antiguo. */
    public List<CorreoSaliente> listarCorreos(User usuario) {
        return consultar("""
                SELECT id, destinatario, asunto, cuerpo, creado_en, enviar_desde, enviado_en, ultimo_error
                FROM correos WHERE usuario_id=? ORDER BY creado_en DESC, id DESC
                """, result -> {
                    Timestamp enviado = result.getTimestamp("enviado_en");
                    return new CorreoSaliente(
                            result.getLong("id"),
                            result.getString("destinatario"),
                            result.getString("asunto"),
                            result.getString("cuerpo"),
                            result.getTimestamp("creado_en").toLocalDateTime(),
                            result.getTimestamp("enviar_desde").toLocalDateTime(),
                            enviado == null ? null : enviado.toLocalDateTime(),
                            result.getString("ultimo_error"));
                }, usuario.id());
    }

    /**
     * Envía los correos que ya están en su hora (los inmediatos y los resúmenes diarios vencidos). Un
     * correo que falla se reintenta hasta {@value #MAX_INTENTOS_CORREO} veces. Devuelve cuántos se
     * enviaron. Sin servidor configurado no hace nada y los correos quedan pendientes.
     */
    public synchronized int enviarCorreosPendientes() {
        EnvioCorreo envio = envioCorreo;
        if (envio == null) {
            return 0;
        }

        List<CorreoSaliente> pendientes = consultar("""
                SELECT id, destinatario, asunto, cuerpo, creado_en, enviar_desde, enviado_en, ultimo_error
                FROM correos WHERE enviado_en IS NULL AND enviar_desde<=? AND intentos<? ORDER BY id
                """, result -> new CorreoSaliente(
                        result.getLong("id"),
                        result.getString("destinatario"),
                        result.getString("asunto"),
                        result.getString("cuerpo"),
                        result.getTimestamp("creado_en").toLocalDateTime(),
                        result.getTimestamp("enviar_desde").toLocalDateTime(),
                        null,
                        null), Timestamp.valueOf(LocalDateTime.now(reloj)), MAX_INTENTOS_CORREO);

        int enviados = 0;
        for (CorreoSaliente correo : pendientes) {
            String error = null;
            try {
                envio.enviar(correo.destinatario(), correo.asunto(), correo.cuerpo() + pieDeCorreo());
                enviados++;
            } catch (Exception exception) {
                error = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            }
            registrarResultadoEnvio(correo.id(), error);
        }
        return enviados;
    }

    private void registrarResultadoEnvio(long correoId, String error) {
        String sql = error == null
                ? "UPDATE correos SET enviado_en=?, intentos=intentos+1, ultimo_error=NULL WHERE id=?"
                : "UPDATE correos SET intentos=intentos+1, ultimo_error=? WHERE id=?";

        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (error == null) {
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now(reloj)));
            } else {
                statement.setString(1, error.length() > 500 ? error.substring(0, 500) : error);
            }
            statement.setLong(2, correoId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Deja el correo listo según la preferencia del usuario: inmediato, sumado al resumen del día o
     * ninguno si los desactivó.
     */
    private void encolarCorreo(Connection connection, long usuarioId, PlanInfo plan, String asunto, String mensaje)
            throws SQLException {
        String destinatario;
        String frecuencia;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT correo, frecuencia_correo FROM usuarios WHERE id=? AND activo=TRUE")) {
            statement.setLong(1, usuarioId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return;
                }
                destinatario = result.getString("correo");
                frecuencia = result.getString("frecuencia_correo");
            }
        }

        if (FrecuenciaCorreo.DESACTIVADA.equals(frecuencia)) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now(reloj);
        String detalle = mensaje + "\n\nPlan: " + plan.codigo() + " (área " + plan.area() + ")\n" + enlaceDelPlan(plan);

        if (FrecuenciaCorreo.DIARIA.equals(frecuencia)) {
            LocalDateTime proximoResumen = ahora.toLocalDate().atTime(HORA_RESUMEN_DIARIO);
            if (!ahora.isBefore(proximoResumen)) {
                proximoResumen = proximoResumen.plusDays(1);
            }
            if (sumarAlResumen(connection, usuarioId, proximoResumen, detalle)) {
                return;
            }
            insertarCorreo(connection, usuarioId, destinatario, ASUNTO_RESUMEN,
                    "Resumen de sus notificaciones del SGPI:" + SEPARADOR_RESUMEN + detalle, ahora, proximoResumen);
            return;
        }

        insertarCorreo(connection, usuarioId, destinatario, asunto, detalle, ahora, ahora);
        hayCorreosNuevos = true;
    }

    /** Agrega la notificación al resumen diario que ya está esperando, si cabe. */
    private boolean sumarAlResumen(Connection connection, long usuarioId, LocalDateTime enviarDesde, String detalle)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT id, cuerpo FROM correos
                WHERE usuario_id=? AND enviado_en IS NULL AND asunto=? AND enviar_desde=?
                """)) {
            statement.setLong(1, usuarioId);
            statement.setString(2, ASUNTO_RESUMEN);
            statement.setTimestamp(3, Timestamp.valueOf(enviarDesde));
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return false;
                }
                String cuerpo = result.getString("cuerpo") + SEPARADOR_RESUMEN + detalle;
                if (cuerpo.length() > MAX_CUERPO_CORREO) {
                    return false;
                }
                try (PreparedStatement update =
                        connection.prepareStatement("UPDATE correos SET cuerpo=? WHERE id=?")) {
                    update.setString(1, cuerpo);
                    update.setLong(2, result.getLong("id"));
                    update.executeUpdate();
                }
                return true;
            }
        }
    }

    private void insertarCorreo(Connection connection, long usuarioId, String destinatario, String asunto,
            String cuerpo, LocalDateTime creadoEn, LocalDateTime enviarDesde) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO correos (usuario_id, destinatario, asunto, cuerpo, creado_en, enviar_desde)
                VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            statement.setLong(1, usuarioId);
            statement.setString(2, destinatario);
            statement.setString(3, asunto.length() > 200 ? asunto.substring(0, 200) : asunto);
            statement.setString(4, cuerpo.length() > MAX_CUERPO_CORREO ? cuerpo.substring(0, MAX_CUERPO_CORREO) : cuerpo);
            statement.setTimestamp(5, Timestamp.valueOf(creadoEn));
            statement.setTimestamp(6, Timestamp.valueOf(enviarDesde));
            statement.executeUpdate();
        }
    }

    /**
     * Enlace al plan si está configurada la dirección web del SGPI ({@value #VARIABLE_URL_BASE}). El
     * prototipo es de escritorio, así que sin esa dirección el correo indica dónde verlo en la aplicación.
     */
    private String enlaceDelPlan(PlanInfo plan) {
        String base = entorno.apply(VARIABLE_URL_BASE);
        if (base == null || base.isBlank()) {
            return "Puede verlo en el SGPI, en el botón Notificaciones.";
        }
        String limpia = base.trim().endsWith("/") ? base.trim() : base.trim() + "/";
        return "Enlace: " + limpia + "planes/" + plan.codigo();
    }

    private static String pieDeCorreo() {
        return "\n\n--\nSGPI - Sistema de Gestión de Procesos Institucionales.\n"
                + "Puede cambiar la frecuencia de estos correos o desactivarlos en Preferencias.";
    }

    /**
     * Revisa cada {@code minutos} si hay correos en su hora, por ejemplo el resumen diario de las 7:00.
     * Sin servidor de correo configurado no hace nada.
     */
    public synchronized void iniciarRevisionDeCorreos(long minutos) {
        if (envioCorreo == null || revisionPeriodica != null) {
            return;
        }
        revisionPeriodica = Executors.newSingleThreadScheduledExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "sgpi-correo-periodico");
            hilo.setDaemon(true);
            return hilo;
        });
        revisionPeriodica.scheduleWithFixedDelay(() -> {
            try {
                enviarCorreosPendientes();
            } catch (RuntimeException exception) {
                // Si falla la base, se vuelve a intentar en la siguiente revisión.
                System.getLogger(DataService.class.getName()).log(System.Logger.Level.WARNING,
                        "No fue posible revisar los correos pendientes: " + exception.getMessage());
            }
        }, 0, minutos, TimeUnit.MINUTES);
    }

    /** Después de confirmar una transacción que generó correos inmediatos, los envía sin bloquear la ventana. */
    private void programarEnvioCorreos() {
        if (!hayCorreosNuevos) {
            return;
        }
        hayCorreosNuevos = false;
        if (envioCorreo != null) {
            ejecutorCorreo.execute(this::enviarCorreosPendientes);
        }
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

            // HU-015: el responsable recibe la notificación y el correo de la actividad asignada.
            if (responsableId != null) {
                notificar(connection, responsableId, plan, "SGPI: nueva actividad asignada en el plan " + plan.codigo(),
                        "Se le asignó la actividad \"" + texto + "\" del plan " + plan.codigo() + " (área "
                        + plan.area() + "), con fecha límite " + fechaLimite + ".");
            }

            return new Actividad(id, planId, texto, responsableId, responsable, fechaLimite,
                    EstadoActividad.PENDIENTE, 0);
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
            validarTrabajoEnActividad(actividad, plan, usuario, "El avance solo se registra");

            if (nuevoEstado.equals(actividad.estado())) {
                return;
            }

            // RN-03: una actividad solo pasa a FINALIZADA si tiene al menos una evidencia.
            if (EstadoActividad.FINALIZADA.equals(nuevoEstado) && !actividad.conEvidencia()) {
                throw new IllegalStateException("La actividad \"" + actividad.descripcion()
                        + "\" no tiene evidencias. Carga al menos una antes de marcarla como "
                        + EstadoActividad.FINALIZADA + ".");
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
                       a.fecha_limite, a.estado,
                       (SELECT COUNT(*) FROM evidencias e WHERE e.actividad_id=a.id) AS evidencias
                FROM actividades a
                LEFT JOIN usuarios u ON u.id=a.responsable_id
                WHERE a.plan_id=?
                ORDER BY a.fecha_limite, a.id
                """;

        return consultar(sql, DataService::mapearActividad, planId);
    }

    // ------------------------------------------------------------------ HU-003: evidencias

    /**
     * HU-003: carga un archivo como evidencia de una actividad. Lo pueden hacer el coordinador o el
     * responsable asignado, con el plan en ejecución. Se aceptan PDF, DOCX, JPG y PNG de hasta 10 MB, y
     * se revisa que el contenido corresponda al formato, no solo la extensión.
     */
    public Evidencia cargarEvidencia(long actividadId, String nombreArchivo, byte[] contenido, User usuario) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new IllegalArgumentException("El archivo no tiene nombre.");
        }
        String nombre = Path.of(nombreArchivo.trim()).getFileName().toString();
        if (nombre.length() > MAX_NOMBRE_EVIDENCIA) {
            throw new IllegalArgumentException("El nombre del archivo no puede superar los "
                    + MAX_NOMBRE_EVIDENCIA + " caracteres.");
        }

        String formato = formatoDe(nombre);
        if (formato == null) {
            throw new IllegalArgumentException("Formato no permitido. Formatos válidos: " + FORMATOS_VALIDOS + ".");
        }
        if (contenido == null || contenido.length == 0) {
            throw new IllegalArgumentException("El archivo está vacío.");
        }
        if (contenido.length > TAMANO_MAXIMO_EVIDENCIA) {
            throw new IllegalArgumentException("El archivo pesa " + (contenido.length / (1024 * 1024))
                    + " MB. El máximo es " + (TAMANO_MAXIMO_EVIDENCIA / (1024 * 1024)) + " MB.");
        }
        if (!contenidoCoincide(formato, contenido)) {
            throw new IllegalArgumentException("El contenido de \"" + nombre + "\" no corresponde a un archivo "
                    + formato + ". Formatos válidos: " + FORMATOS_VALIDOS + ".");
        }

        return enTransaccion(connection -> {
            Actividad actividad = leerActividad(connection, actividadId);
            PlanInfo plan = leerPlan(connection, actividad.planId());
            validarTrabajoEnActividad(actividad, plan, usuario, "Las evidencias solo se cargan");

            LocalDateTime ahora = LocalDateTime.now(reloj);
            long id;
            try (PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO evidencias
                    (actividad_id, nombre_archivo, formato, tamano, contenido, cargado_por, cargado_en)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, actividadId);
                statement.setString(2, nombre);
                statement.setString(3, formato);
                statement.setLong(4, contenido.length);
                statement.setBytes(5, contenido);
                statement.setLong(6, usuario.id());
                statement.setTimestamp(7, Timestamp.valueOf(ahora));
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getLong(1);
                }
            }

            registrarHistorial(connection, plan, usuario, "EVIDENCIA_CARGADA", plan.estado(),
                    actividad.descripcion() + ": " + nombre);

            return new Evidencia(id, actividadId, nombre, formato, contenido.length, usuario.nombre(), ahora);
        });
    }

    /** Evidencias de una actividad, de la más reciente a la más antigua. */
    public List<Evidencia> listarEvidencias(long actividadId) {
        String sql = """
                SELECT e.id, e.actividad_id, e.nombre_archivo, e.formato, e.tamano, u.nombre AS cargado_por,
                       e.cargado_en
                FROM evidencias e
                JOIN usuarios u ON u.id=e.cargado_por
                WHERE e.actividad_id=?
                ORDER BY e.cargado_en DESC, e.id DESC
                """;

        return consultar(sql, result -> new Evidencia(
                result.getLong("id"),
                result.getLong("actividad_id"),
                result.getString("nombre_archivo"),
                result.getString("formato"),
                result.getLong("tamano"),
                result.getString("cargado_por"),
                result.getTimestamp("cargado_en").toLocalDateTime()), actividadId);
    }

    /** Contenido del archivo de una evidencia, para descargarlo. */
    public byte[] descargarEvidencia(long evidenciaId) {
        List<byte[]> contenido = consultar("SELECT contenido FROM evidencias WHERE id=?",
                result -> result.getBytes("contenido"), evidenciaId);
        if (contenido.isEmpty()) {
            throw new IllegalArgumentException("La evidencia no existe.");
        }
        return contenido.get(0);
    }

    /** Devuelve PDF, DOCX, JPG o PNG según la extensión, o {@code null} si no es un formato permitido. */
    private static String formatoDe(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto < 0) {
            return null;
        }
        return switch (nombre.substring(punto + 1).toLowerCase(Locale.ROOT)) {
            case "pdf" -> "PDF";
            case "docx" -> "DOCX";
            case "jpg", "jpeg" -> "JPG";
            case "png" -> "PNG";
            default -> null;
        };
    }

    /** Revisa la firma de los primeros bytes, para no aceptar, por ejemplo, un .exe renombrado a .pdf. */
    private static boolean contenidoCoincide(String formato, byte[] contenido) {
        return switch (formato) {
            case "PDF" -> empiezaCon(contenido, 0x25, 0x50, 0x44, 0x46);           // %PDF
            case "DOCX" -> empiezaCon(contenido, 0x50, 0x4B, 0x03, 0x04);          // ZIP (PK)
            case "JPG" -> empiezaCon(contenido, 0xFF, 0xD8, 0xFF);
            case "PNG" -> empiezaCon(contenido, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            default -> false;
        };
    }

    private static boolean empiezaCon(byte[] contenido, int... firma) {
        if (contenido.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((contenido[i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * El avance y las evidencias de una actividad solo los registran el coordinador o el responsable
     * asignado, y solo con el plan en ejecución.
     */
    private static void validarTrabajoEnActividad(Actividad actividad, PlanInfo plan, User usuario,
            String que) {
        boolean esResponsable = actividad.responsableId() != null && actividad.responsableId() == usuario.id();
        if (!Rol.gestionaPlanes(usuario.rol()) && !esResponsable) {
            throw new IllegalStateException(
                    "Solo el coordinador o el responsable asignado pueden actualizar esta actividad.");
        }

        if (!EstadoPlan.EN_EJECUCION.equals(plan.estado())) {
            throw new IllegalStateException(que + " en planes en estado " + EstadoPlan.EN_EJECUCION
                    + ". El plan " + plan.codigo() + " está en estado " + plan.estado() + ".");
        }
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
                result.getString("estado"),
                result.getInt("evidencias"));
    }

    private Actividad leerActividad(Connection connection, long actividadId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT a.id, a.plan_id, a.descripcion, a.responsable_id, u.nombre AS responsable,
                       a.fecha_limite, a.estado,
                       (SELECT COUNT(*) FROM evidencias e WHERE e.actividad_id=a.id) AS evidencias
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
                programarEnvioCorreos();
                return resultado;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                hayCorreosNuevos = false;
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

    private List<Plan> consultarPlanes(String sql, Object... parametros) {
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

    /**
     * Registra la notificación dentro del sistema y, según la preferencia del usuario, deja listo el
     * correo (HU-015). El correo se envía después de confirmar la transacción.
     */
    private void notificar(Connection connection, long usuarioId, PlanInfo plan, String asunto, String mensaje)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO notificaciones (usuario_id, plan_id, mensaje, fecha) VALUES (?, ?, ?, ?)")) {
            statement.setLong(1, usuarioId);
            statement.setLong(2, plan.id());
            statement.setString(3, mensaje);
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now(reloj)));
            statement.executeUpdate();
        }

        encolarCorreo(connection, usuarioId, plan, asunto, mensaje);
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
