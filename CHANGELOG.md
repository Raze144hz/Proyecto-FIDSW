# Registro de cambios

## HU-003: carga de evidencias (10 de noviembre de 2026)

- Cada actividad puede tener evidencias en PDF, DOCX, JPG o PNG, de hasta 10 MB. Además de la extensión se revisa la firma del archivo, para no aceptar, por ejemplo, un ejecutable renombrado a `.pdf`.
- Las cargan el coordinador o el responsable asignado, con el plan en ejecución. La carga queda en el historial (`EVIDENCIA_CARGADA`).
- RN-03: una actividad solo pasa a FINALIZADA si tiene al menos una evidencia. Con esto queda corregido el defecto D-02 del acta del Sprint 4.
- En la ventana de actividades: columna **Evidencias**, botón **Cargar evidencia...** y lista de evidencias con opción de guardar una copia.
- Nueva tabla `evidencias` (se crea sola al abrir la app) y 9 pruebas en `EvidenciasTest`.

## Rama `mejoras/refactor-hu001-hu004` (3 de noviembre de 2026)

### Reorganización del código

- `Main.java` pasó de 2.990 a unas 1.600 líneas. Las clases que estaban anidadas se movieron a paquetes propios:
  - `com.sgpi.model`: `User`, `Plan`, `Observacion`, `Notificacion`, `RegistroHistorial`, `Rol`, `EstadoPlan` y los nuevos `Actividad`, `EstadoActividad` y `ResultadoLogin`.
  - `com.sgpi.data`: `DataService`, reescrito con menos código repetido (un método genérico `consultar` y `enTransaccion` que devuelve resultados).
  - `com.sgpi.ui`: `VistaActividades`, la nueva ventana de actividades.
- Las pruebas se movieron a `src/test/java/com/sgpi/data`. Las 12 pruebas de HU-017 siguen igual y pasan.

### Historias de usuario

- **HU-001 (login):** bloqueo de 15 minutos tras 3 intentos fallidos seguidos, que era el criterio de aceptación pendiente. La pantalla de inicio de sesión muestra los intentos que quedan y los minutos de bloqueo. El administrador puede desbloquear una cuenta (`DataService.desbloquearUsuario`).
- **HU-002 (registro del plan):** actividades del plan con descripción, responsable (rol RESPONSABLE, opcional) y fecha límite dentro de las fechas del plan. Se agregan o eliminan mientras el plan está en borrador o devuelto.
- **HU-004 (seguimiento del avance):** estados de actividad PENDIENTE, EN PROGRESO y FINALIZADA; el avance es actividades finalizadas / total (RN-01). Lo registran el coordinador o el responsable asignado, solo con el plan en ejecución. Nueva columna **Avance** en la tabla de planes.

### Correcciones

- Un plan en revisión, aprobado o en ejecución ya no se puede eliminar (antes se podía borrar en cualquier estado).
- La edición y la eliminación del plan quedan en el historial (`MODIFICACION`, `ELIMINACION`).
- La capa de datos valida objetivo, área, longitud de los campos y que la fecha de fin no sea anterior a la de inicio; antes solo lo validaba el formulario.
- Un plan con actividades pendientes no se puede finalizar.
- Al enviar un plan a revisión, el director del área recibe una notificación.
- El detalle del plan que ve el director muestra el avance ("Avance: 25 % (1 de 4 actividades finalizadas)"); antes solo aparecía en la columna de la tabla.

### Pruebas

- De 12 a 37 pruebas JUnit: `InicioSesionTest` (8), `ActividadesPlanTest` (10) y `GestionPlanesTest` (7), además de `AprobacionPlanesTest` (12).

### Pruebas en la interfaz

- [Acta de pruebas del Sprint 4](Docs/Calidad/actas/acta-pruebas-sprint-4.md): CP-HU004-01, 02 y 04 pasan; capturas en `Graficas/sprint-4`.

### Documentación

- Actualizados: `README.md`, `README-PROTOTIPO.md`, la [guía de usuario](Docs/guia-de-usuario.md), el [esquema H2](Database/prototipo-h2.sql), la [trazabilidad HU ↔ issues](Docs/Planning/trazabilidad-hu-issues.md) y los documentos de [arquitectura](Docs/Arquitectura/arquitectura.md) y [modelos arquitectónicos](Docs/Arquitectura/modelos-arquitectonicos.md).
