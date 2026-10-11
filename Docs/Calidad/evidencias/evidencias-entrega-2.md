# Evidencias del prototipo — Entrega 2

**Responsable:** David Álvarez Rodríguez (Gestión y Reportes) · Issue #39
**Fecha de la ejecución:** 10 de octubre de 2026
**Versión:** rama `feature/HU-017-aprobacion-planes` @ `5fce05f` (PR #40). Incluye el prototipo del Sprint 2 (`main` @ `9390848`) más la HU-017.
**Entorno:** Ubuntu 24.04.4 LTS con pantalla virtual (Xvfb), OpenJDK 21.0.10 compilando con `--release 17`, JavaFX 11.0.11, H2 2.2.220, jBCrypt 0.4 y JUnit 5.10.1.

Las capturas salen del recorrido automático de la interfaz real del prototipo ([`Script/evidencias-prototipo`](../../../Script/evidencias-prototipo/README.md)). Lo que mostró la aplicación en cada paso está en el [registro de ejecución](registro-ejecucion-interfaz.txt). Los resultados por caso están en el [acta de pruebas del Sprint 2](../actas/acta-pruebas-sprint-2.md).

## 1. Capturas

Todas están en [`Graficas/entrega-2/`](../../../Graficas/entrega-2).

| # | Captura | Qué muestra | Caso |
|---|---|---|---|
| 01 | [01-login](../../../Graficas/entrega-2/01-login.png) | Pantalla de inicio de sesión | CP-PRO-03 |
| 02 | [02-login-error](../../../Graficas/entrega-2/02-login-error.png) | Contraseña incorrecta: "Usuario o contraseña incorrectos." | CP-PRO-04 |
| 03 | [03-coordinador-panel](../../../Graficas/entrega-2/03-coordinador-panel.png) | Panel del coordinador; un plan nuevo empieza en BORRADOR | CP-PRO-03 |
| 04 | [04-validacion-objetivo](../../../Graficas/entrega-2/04-validacion-objetivo.png) | Crear un plan sin objetivo: "Debe ingresar el objetivo del plan." | CP-PRO-05 |
| 05 | [05-plan-creado](../../../Graficas/entrega-2/05-plan-creado.png) | Plan PA-2026-0001 creado | CP-PRO-05 |
| 06 | [06-enviar-revision](../../../Graficas/entrega-2/06-enviar-revision.png) | El coordinador envía el plan; queda PENDIENTE DE REVISION | Preparación de CP-HU017-01 y 02 |
| 07 | [07-director-pendientes](../../../Graficas/entrega-2/07-director-pendientes.png) | Vista del director con los dos planes pendientes del área Calidad | CP-PRO-03 |
| 08 | [08-cp-hu017-03-sin-observaciones](../../../Graficas/entrega-2/08-cp-hu017-03-sin-observaciones.png) | Devolver sin observaciones: mensaje junto al campo y el plan sigue pendiente | CP-HU017-03 |
| 09 | [09-cp-hu017-02-confirmar-devolucion](../../../Graficas/entrega-2/09-cp-hu017-02-confirmar-devolucion.png) | Confirmación de la devolución con la observación escrita | CP-HU017-02 |
| 10 | [10-cp-hu017-02-plan-devuelto](../../../Graficas/entrega-2/10-cp-hu017-02-plan-devuelto.png) | PA-2026-0001 queda EN REVISION y sale de pendientes | CP-HU017-02 |
| 11 | [11-cp-hu017-01-confirmar-aprobacion](../../../Graficas/entrega-2/11-cp-hu017-01-confirmar-aprobacion.png) | Confirmación de la aprobación | CP-HU017-01 |
| 12 | [12-cp-hu017-01-plan-aprobado](../../../Graficas/entrega-2/12-cp-hu017-01-plan-aprobado.png) | PA-2026-0002 queda APROBADO | CP-HU017-01 |
| 13 | [13-cp-hu017-01-sin-pendientes](../../../Graficas/entrega-2/13-cp-hu017-01-sin-pendientes.png) | La lista de pendientes queda vacía | CP-HU017-01 |
| 14 | [14-director-planes-area](../../../Graficas/entrega-2/14-director-planes-area.png) | Planes del área: PA-2026-0002 APROBADO y PA-2026-0001 EN REVISION | CP-HU017-01 y 02 |
| 15 | [15-cp-hu017-02-notificaciones](../../../Graficas/entrega-2/15-cp-hu017-02-notificaciones.png) | El coordinador recibe las dos notificaciones, una con la observación | CP-HU017-02 |
| 16 | [16-cp-hu017-04-observacion-pendiente](../../../Graficas/entrega-2/16-cp-hu017-04-observacion-pendiente.png) | Plan devuelto con la observación pendiente | CP-HU017-04 |
| 17 | [17-cp-hu017-04-paso1-ejecucion-bloqueada](../../../Graficas/entrega-2/17-cp-hu017-04-paso1-ejecucion-bloqueada.png) | Paso 1: no deja iniciar la ejecución del plan devuelto | CP-HU017-04 |
| 18 | [18-cp-hu017-04-reenvio-bloqueado](../../../Graficas/entrega-2/18-cp-hu017-04-reenvio-bloqueado.png) | No deja reenviar el plan con observaciones sin atender | CP-HU017-04 |
| 19 | [19-cp-hu017-04-paso2-observacion-atendida](../../../Graficas/entrega-2/19-cp-hu017-04-paso2-observacion-atendida.png) | Paso 2: la observación queda atendida | CP-HU017-04 |
| 20 | [20-cp-hu017-04-paso3-ejecucion-bloqueada](../../../Graficas/entrega-2/20-cp-hu017-04-paso3-ejecucion-bloqueada.png) | Paso 3: sin nueva aprobación sigue sin poder ejecutarse | CP-HU017-04 |
| 21 | [21-director-reevalua](../../../Graficas/entrega-2/21-director-reevalua.png) | El director ve la observación atendida antes de aprobar de nuevo | CP-HU017-04 |
| 22 | [22-cp-hu017-04-ejecucion-permitida](../../../Graficas/entrega-2/22-cp-hu017-04-ejecucion-permitida.png) | Después de atender y reaprobar, el plan queda EN EJECUCION | CP-HU017-04 |
| 23 | [23-historial-plan](../../../Graficas/entrega-2/23-historial-plan.png) | Historial del plan con cada cambio de estado, quién y cuándo | Verificación adicional V-04 |
| 24 | [24-edicion-bloqueada](../../../Graficas/entrega-2/24-edicion-bloqueada.png) | No se puede modificar un plan APROBADO | Verificación adicional V-03 |
| 25 | [25-responsable-consulta](../../../Graficas/entrega-2/25-responsable-consulta.png) | El rol RESPONSABLE solo consulta | CP-PRO-06 |

## 2. Evidencias en texto

| Archivo | Qué contiene | Caso |
|---|---|---|
| [registro-ejecucion-interfaz.txt](registro-ejecucion-interfaz.txt) | Cada paso del recorrido y el mensaje que mostró la aplicación | Todos los de la sección 1 |
| [resultado-junit-hu017.txt](resultado-junit-hu017.txt) | Resultado de las 12 pruebas JUnit de HU-017: pasan las 12 | CP-HU017-01 a 04, V-01 a V-04 |
| [migracion-base-anterior.txt](migracion-base-anterior.txt) | Una base creada con el prototipo del Sprint 2 abre con la versión de HU-017 sin perder sus planes | CP-PRO-07 |
| [verificacion-contrasenas.txt](verificacion-contrasenas.txt) | Las contraseñas de los usuarios quedan guardadas con hash BCrypt | CP-PRO-02 (RNF-03) |

## 3. Evidencias que faltan

| Evidencia | Para qué | Responsable propuesto |
|---|---|---|
| Captura del prototipo corriendo en Windows con `mvn javafx:run` | CP-PRO-08 (RNF-08) | Quien revise el PR #40 |
| Salida de `mvn clean test` | CP-PRO-09 | Quien revise el PR #40 |
| `Graficas/diagrama-clases.png` y su fuente editable | Diagrama de clases de la Entrega 2 | David Andrés Galindo |
| Diagramas de secuencia y de procesos de HU-017 | Entrega 2 | Equipo (Galindo revisa) |
| Captura del tablero de GitHub Projects con el estado de las tareas de la Entrega 2 | Seguimiento del sprint | Miguel Ángel Gómez |
