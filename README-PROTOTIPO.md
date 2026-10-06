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

Comandos de ejecucion y compilacion:


mvn clean compile

mvn javafx:run -Djavafx.platform=win

