# Sprint Review - Sprint 3

**Fecha:** 21/10/2026
**Scrum Master:** Miguel Ángel Gómez López
**Asistentes:** David Galindo, Miguel Gómez, Juan Martínez, David Álvarez
**Producto:** SGPI (Sistema de Gestión de Procesos Institucionales)

## 1. Objetivo del Sprint
Consolidar el acceso seguro al prototipo SGPI (autenticación, bloqueo por intentos fallidos y control de roles) y habilitar el registro de nuevos planes de acción con sus actividades iniciales.

## 2. Incremento Presentado (Demostración)
El equipo presentó el prototipo en JavaFX + H2 operando con las siguientes funcionalidades integradas:
* **HU-001 (Login en el sistema):** Inicio de sesión con contraseñas cifradas (BCrypt) y bloqueo temporal de 15 minutos tras 3 intentos fallidos consecutivos.
* **HU-006 (Autenticación y control de roles — parcial):** Validación de permisos por rol y área directamente en la capa de datos (`DataService`), evitando que el control dependa únicamente de la interfaz.
* **HU-002 (Registro de un nuevo plan de acción):** Creación de planes con validación de campos obligatorios, asignación de área, responsables y fechas.

## 3. Aseguramiento de Calidad
* **InicioSesionTest:** 8 pruebas automatizadas aprobadas (login válido, credenciales incorrectas y bloqueo de cuenta).
* **GestionPlanesTest:** 7 pruebas automatizadas aprobadas (creación y validación de planes).
* **SeguridadBaseDatosTest:** 4 pruebas automatizadas aprobadas (protección de acceso y credenciales de BD).

## 4. Pendientes y Próximos Pasos
* **Observación sobre HU-006:** Se identificó que algunos nombres de roles en el prototipo difieren ligeramente de los enunciados en el issue original; queda documentado como ajuste menor.
* **Siguiente Sprint (Sprint 4):** Implementar la carga de evidencias documentales (HU-003) y el seguimiento de avance de actividades (HU-004).
