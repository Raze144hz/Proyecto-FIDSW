# Prototipo funcional del SGPI

Aplicación de escritorio en Java para registrar planes de acción con sus actividades, aprobarlos o devolverlos con observaciones (HU-017) y seguir su avance (HU-004). Cómo se usa: [guía de usuario](Docs/guia-de-usuario.md).

## Tecnologías

- Java 17 y JavaFX 17
- H2 (base de datos local) con JDBC
- BCrypt para las contraseñas
- Maven, con el Maven Wrapper incluido (`mvnw`)
- JUnit 5 para las pruebas automáticas

## Funcionalidades

- Inicio y cierre de sesión. Tras 3 intentos fallidos seguidos la cuenta se bloquea 15 minutos (HU-001).
- Roles ADMIN, COORDINADOR, RESPONSABLE y DIRECTOR, cada uno con sus permisos.
- Registro, modificación, eliminación y consulta de planes de acción, con código único y validaciones en el formulario y en la capa de datos. Solo se modifican o eliminan planes en borrador o devueltos.
- Actividades del plan con responsable y fecha límite (HU-002), y registro de su avance durante la ejecución (HU-004).
- Avance del plan = actividades finalizadas / total, en la tabla de planes y en la ventana de actividades.
- Envío del plan a revisión; aprobación o devolución con observaciones obligatorias por el director de área (HU-017).
- Notificaciones al coordinador (aprobación o devolución) y al director del área (plan pendiente de revisión).
- Historial de cada plan: creación, edición, eliminación, cambios de estado y de actividades.
- **Finalizar plan** deja un plan en ejecución en FINALIZADO; si tiene actividades, exige el 100 % de avance. Todavía no pide la evaluación de cierre del director que define HU-020 (ver [arquitectura](Docs/Arquitectura/arquitectura.md)).
- Persistencia local en H2. Una base creada con una versión anterior del prototipo se actualiza sola al abrir la aplicación, sin borrar datos.

## Estructura del código

| Paquete | Contenido |
|---|---|
| `com.sgpi` | `Main`: arranque de JavaFX, inicio de sesión, tablero, formulario de planes y vista del director |
| `com.sgpi.ui` | `VistaActividades`: ventana de actividades y avance |
| `com.sgpi.data` | `DataService`: reglas de negocio, transacciones y SQL |
| `com.sgpi.model` | `User`, `Plan`, `Actividad`, `Observacion`, `Notificacion`, `RegistroHistorial`, `ResultadoLogin`, `Rol`, `EstadoPlan`, `EstadoActividad` |

## Requisitos

- JDK 17 o superior.
- Conexión a internet la primera vez: el Maven Wrapper descarga Maven 3.9.16 y Maven descarga las librerías. No hace falta instalar Maven.

## Cómo ejecutarlo

Desde la carpeta del proyecto.

Windows (PowerShell o CMD):

```
.\mvnw.cmd clean compile
.\mvnw.cmd javafx:run "-Djavafx.platform=win"
```

Linux o macOS:

```
sh mvnw clean compile
sh mvnw javafx:run -Djavafx.platform=linux
```

En macOS se cambia `linux` por `mac`. Si Maven ya está instalado, se puede usar `mvn` en lugar de `mvnw`.

La base de datos se crea en la carpeta `data/` la primera vez que se abre la aplicación, con los usuarios de prueba.

## Usuarios de prueba

| Rol | Correo | Contraseña | Área |
|---|---|---|---|
| Administrador | `admin@sgpi.local` | `Admin123*` | — |
| Coordinador | `coordinador@sgpi.local` | `Coord123*` | — |
| Responsable | `responsable@sgpi.local` | `Resp123*` | — |
| Director de área | `director@sgpi.local` | `Dir123*` | Calidad |

Para que un plan le aparezca al director, el plan debe tener "Calidad" como área responsable.

## Pruebas automáticas

Hay 37 pruebas JUnit 5 en `src/test/java/com/sgpi/data`, que usan una base H2 en memoria distinta para cada prueba:

| Clase | Qué prueba | Pruebas |
|---|---|---|
| `AprobacionPlanesTest` | Flujo de aprobación y retroalimentación (HU-017) | 12 |
| `InicioSesionTest` | Inicio de sesión, bloqueo y desbloqueo (HU-001) | 8 |
| `ActividadesPlanTest` | Actividades, permisos, avance y cierre al 100 % (HU-002 y HU-004) | 10 |
| `GestionPlanesTest` | Código del plan, validaciones, permisos, eliminación e historial (HU-002) | 7 |

```
.\mvnw.cmd clean test      (Windows)
sh mvnw clean test         (Linux o macOS)
```

Las pruebas se corren localmente con estos comandos, y el resultado se registra en el acta de pruebas del sprint.

## Base de datos

El esquema que crea la aplicación está documentado en [`Database/prototipo-h2.sql`](Database/prototipo-h2.sql).
