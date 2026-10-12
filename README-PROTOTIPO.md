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
- Roles ADMIN, COORDINADOR, RESPONSABLE y DIRECTOR.
- Dashboard.
- Creación de planes de acción.
- Consulta de planes registrados.
- Persistencia local mediante H2.
- Restricción de creación según rol.
- Aprobación y devolución de planes con observaciones (HU-017).
- Notificaciones al coordinador e historial de cambios de estado del plan.
- Cierre de sesión.

## Flujo de aprobación de planes (HU-017)

1. El coordinador crea el plan. Todo plan nuevo queda en **BORRADOR**.
2. El coordinador lo envía a revisión. El plan queda **PENDIENTE DE REVISION**.
3. El director de área revisa los planes pendientes de su área:
   - **Aprobar plan:** el plan queda **APROBADO**.
   - **Devolver con observaciones:** las observaciones son obligatorias y el plan queda **EN REVISION**.
4. En los dos casos el coordinador recibe una notificación (botón **Notificaciones** de la barra superior).
5. Si el plan fue devuelto, el coordinador lo corrige, marca cada observación como atendida y lo envía de nuevo a revisión.
6. Solo un plan **APROBADO** y sin observaciones pendientes puede pasar a **EN EJECUCION**. Después se puede marcar como **FINALIZADO**.

Reglas:

- El coordinador solo puede modificar un plan en **BORRADOR** o **EN REVISION**.
- El director solo revisa los planes de su área. El área se compara sin importar mayúsculas ni espacios.
- Cada cambio de estado queda en el historial del plan (botón **Ver historial del plan**) con el estado anterior, el nuevo, quién lo hizo y cuándo.
- Las bases de datos creadas con la versión anterior del prototipo se actualizan solas al abrir la aplicación: se agregan las columnas y tablas nuevas sin borrar datos.

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

### Director de área (HU-017)

Correo:

director@sgpi.local

Contraseña:

Dir123*

Área: Calidad. Para que un plan le aparezca al director, el plan debe tener "Calidad" como área responsable.

Comandos de ejecucion y compilacion:


mvn clean compile

mvn javafx:run -Djavafx.platform=win

En Linux o macOS se cambia `win` por `linux` o `mac`.

Pruebas automáticas (JUnit 5) de las reglas de HU-017:

mvn test

