# Prototipo funcional del SGPI

Aplicación de escritorio en Java para registrar planes de acción y aprobarlos o devolverlos con observaciones (HU-017). Cómo se usa: [guía de usuario](Docs/guia-de-usuario.md).

## Tecnologías

- Java 17 y JavaFX 17
- H2 (base de datos local) con JDBC
- BCrypt para las contraseñas
- Maven, con el Maven Wrapper incluido (`mvnw`)
- JUnit 5 para las pruebas automáticas

## Funcionalidades

- Inicio y cierre de sesión.
- Roles ADMIN, COORDINADOR, RESPONSABLE y DIRECTOR, cada uno con sus permisos.
- Registro, modificación y consulta de planes de acción, con código único y validaciones.
- Envío del plan a revisión; aprobación o devolución con observaciones obligatorias por el director de área (HU-017).
- Notificaciones al coordinador.
- Historial de cambios de estado de cada plan.
- **Finalizar plan** deja un plan en ejecución en FINALIZADO. Es una simplificación: el prototipo no tiene actividades, cálculo de avance ni la evaluación de cierre del director que define HU-020 (ver [arquitectura](Docs/Arquitectura/arquitectura.md)).
- Persistencia local en H2. Una base creada con una versión anterior del prototipo se actualiza sola al abrir la aplicación, sin borrar datos.

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

Las reglas de HU-017 tienen 12 pruebas JUnit 5 en `src/test/java`, que usan una base H2 en memoria:

```
.\mvnw.cmd clean test      (Windows)
sh mvnw clean test         (Linux o macOS)
```

Las pruebas se corren localmente con estos comandos, y el resultado se registra en el acta de pruebas del sprint.

## Base de datos

El esquema que crea la aplicación está documentado en [`Database/prototipo-h2.sql`](Database/prototipo-h2.sql).
