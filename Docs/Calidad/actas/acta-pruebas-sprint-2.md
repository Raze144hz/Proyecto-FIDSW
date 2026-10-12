# Acta de pruebas — Sprint 2

> Hecha con el [formato de acta de pruebas](../formato-acta-de-pruebas.md). Los datos de la sesión están en [datos-acta-pruebas-sprint-2.md](datos-acta-pruebas-sprint-2.md) y las capturas en el [índice de evidencias](../evidencias/evidencias-entrega-2.md). Issue #38.

## 1. Datos generales

| Campo | Valor |
|---|---|
| Proyecto | SGPI — Sistema de Gestión de Procesos Institucionales |
| Sprint | 2 (23 sep – 7 oct de 2026) |
| Fecha de la sesión de pruebas | 10 de octubre de 2026. La sesión planeada para el 6 de octubre no quedó registrada, por eso las pruebas se ejecutaron después del cierre del sprint. CP-PRO-09 se ejecutó el 12 de octubre. |
| Versión probada | `main` @ `9390848` (prototipo del Sprint 2) y `feature/HU-017-aprobacion-planes` @ `5fce05f` (PR #40: prototipo + HU-017) |
| Entorno | Ubuntu 24.04.4 LTS con pantalla virtual (Xvfb), OpenJDK 21.0.10 compilando con `--release 17`, JavaFX 11.0.11, H2 2.2.220, jBCrypt 0.4, JUnit 5.10.1. CP-PRO-09: Windows, OpenJDK 25.0.4 (Microsoft) y Maven 3.9.16 con las versiones del `pom.xml` |
| Forma de ejecución | Recorrido automático de la interfaz real ([`Script/evidencias-prototipo`](../../../Script/evidencias-prototipo/README.md)) y pruebas JUnit, con una captura por paso |
| Responsable del acta | David Álvarez Rodríguez (Gestión y Reportes) |

> El `pom.xml` usa JavaFX 17.0.10 y H2 2.2.224. En el entorno de la sesión del 10 de octubre no había acceso a Maven Central, así que se compiló con `javac` y librerías equivalentes. La prueba con Maven (CP-PRO-09) se hizo el 12 de octubre en Windows, con las versiones exactas del `pom.xml`.

## 2. Participantes

| Nombre | Rol en las pruebas |
|---|---|
| David Álvarez Rodríguez | Preparó y ejecutó los casos; registra el acta y los defectos |
| David Andrés Galindo Rojas | Valida los resultados como líder técnico (pendiente) |
| Miguel Ángel Gómez López | Aprueba el acta como Scrum Master (pendiente) |
| Juan Sebastián Martínez | Revisa los casos de HU-016 y HU-017 (tarea de la Entrega 2) |

## 3. Alcance

- **HU incluidas:** HU-017, implementada en el PR #40. También se prueba el prototipo entregado en el Sprint 2: inicio de sesión, roles y registro de planes, que son avance de HU-001, HU-002 y HU-006.
- **HU no incluidas y motivo:**
  - HU-016: no está programada y depende de HU-003.
  - HU-003, HU-005 y HU-018: no se han programado.
  - HU-001, HU-002 y HU-006: tienen avance en el prototipo, pero no se evaluaron contra sus criterios de aceptación porque todavía no tienen casos de prueba escritos.
- **Requisitos no funcionales verificados:**
  - RNF-03: contraseñas con hash y salt.
  - RNF-05: acceso según el rol.
  - RNF-06: mensajes en español junto al campo.
  - RNF-10: historial de cambios de estado con valor anterior y nuevo.
  - RNF-08 solo en Linux; falta Windows.

### Cambio frente al acta preparada el 3 de octubre

Los casos CP-ENT-01 a CP-ENT-06 suponían un backend en Spring, un frontend web y PostgreSQL con el script de 7 tablas. El equipo construyó en su lugar un prototipo de escritorio con JavaFX y H2, así que esos casos se reemplazan por los de la sección 4:

| Caso anterior | Reemplazo |
|---|---|
| CP-ENT-01: compilar y arrancar el backend | CP-PRO-01 y CP-PRO-02 |
| CP-ENT-02: conexión con la base de datos | CP-PRO-02 (H2 local, creada por la aplicación) |
| CP-ENT-03: script de 7 tablas | No aplica. El prototipo crea 5 tablas, documentadas en `Database/prototipo-h2.sql`. |
| CP-ENT-04: frontend web | No aplica |
| CP-ENT-05: Windows y Linux | CP-PRO-02 (Linux) y CP-PRO-08 (Windows) |
| CP-ENT-06: `develop` al día con `main` | CP-PRO-10 |

## 4. Casos de prueba

### Prototipo del Sprint 2

| Caso | Qué se prueba | Resultado esperado |
|---|---|---|
| CP-PRO-01 | Compilar el prototipo (`main` @ `9390848` y PR #40 @ `5fce05f`) | Compila sin errores |
| CP-PRO-02 | Abrir la aplicación en una carpeta vacía | Abre en Linux, crea la base H2 con los usuarios de prueba y guarda las contraseñas con hash (RNF-03) |
| CP-PRO-03 | Iniciar sesión con coordinador, director y responsable | Cada rol entra a su vista |
| CP-PRO-04 | Iniciar sesión con contraseña incorrecta | Mensaje "Usuario o contraseña incorrectos." y no entra |
| CP-PRO-05 | Crear un plan sin objetivo y luego uno completo | Primero un mensaje de validación; después el plan queda con código único en BORRADOR |
| CP-PRO-06 | Entrar con el rol RESPONSABLE | Solo consulta: no tiene formulario de edición ni botones de revisión |
| CP-PRO-07 | Abrir con la versión de HU-017 una base creada con el prototipo del Sprint 2 | Conserva los planes y el flujo nuevo funciona |
| CP-PRO-08 | Arrancar en Windows con `mvn javafx:run` (RNF-08) | Arranca sin cambiar el código |
| CP-PRO-09 | `mvn clean test` | Compila y pasan las pruebas JUnit |
| CP-PRO-10 | `develop` al día con `main` | `develop` contiene el prototipo y los documentos actuales |

### HU-017 y HU-016

Los casos están en [casos-de-prueba-HU016-HU017.md](../casos-de-prueba-HU016-HU017.md): CP-HU017-01 a 04 y CP-HU016-01 a 03.

### Verificaciones adicionales de HU-017

No están en el documento de casos. Se comprobaron con las pruebas JUnit y con capturas.

| Verificación | Qué se prueba |
|---|---|
| V-01 | Solo el director de área aprueba o devuelve; el coordinador y el responsable no (RNF-05) |
| V-02 | El director no ve ni revisa planes de otra área |
| V-03 | Un plan pendiente de revisión o aprobado no se puede modificar; uno devuelto sí |
| V-04 | Cada cambio de estado queda en el historial con el estado anterior, el nuevo, el usuario y la fecha (RNF-10) |

## 5. Resultados

| Caso | HU | Estado | Ejecutado por | Evidencia | Defecto |
|---|---|---|---|---|---|
| CP-PRO-01 | — | Pasa | David Álvarez | Compilación sin errores con `javac` en las dos versiones | — |
| CP-PRO-02 | — | Pasa | David Álvarez (automatizado) | [01](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/01-login.png), [verificación de contraseñas](../evidencias/verificacion-contrasenas.txt) | — |
| CP-PRO-03 | HU-001, HU-006 | Pasa | David Álvarez (automatizado) | [03](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/03-coordinador-panel.png), [07](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/07-director-pendientes.png), [25](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/25-responsable-consulta.png) | — |
| CP-PRO-04 | HU-001 | Pasa | David Álvarez (automatizado) | [02](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/02-login-error.png) | — |
| CP-PRO-05 | HU-002 | Pasa | David Álvarez (automatizado) | [04](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/04-validacion-objetivo.png), [05](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/05-plan-creado.png) | — |
| CP-PRO-06 | HU-006 | Pasa | David Álvarez (automatizado) | [25](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/25-responsable-consulta.png) | — |
| CP-PRO-07 | HU-017 | Pasa | David Álvarez | [migracion-base-anterior.txt](../evidencias/migracion-base-anterior.txt) | — |
| CP-PRO-08 | — | No ejecutado | — | Falta probar en Windows | — |
| CP-PRO-09 | HU-017 | Pasa | David Álvarez (Windows, 12 oct) | [resultado-maven-windows.txt](../evidencias/resultado-maven-windows.txt): BUILD SUCCESS, 12 pruebas sin fallas | — |
| CP-PRO-10 | — | Falla | David Álvarez | `develop` está en `35ca77b` (16 sep) y `main` en `9390848` | D-08 (#42) |
| CP-HU017-01 | HU-017 | Pasa | David Álvarez (automatizado) | [11](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/11-cp-hu017-01-confirmar-aprobacion.png), [12](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/12-cp-hu017-01-plan-aprobado.png), [13](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/13-cp-hu017-01-sin-pendientes.png), [JUnit](../evidencias/resultado-junit-hu017.txt) | — |
| CP-HU017-02 | HU-017 | Pasa | David Álvarez (automatizado) | [09](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/09-cp-hu017-02-confirmar-devolucion.png), [10](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/10-cp-hu017-02-plan-devuelto.png), [15](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/15-cp-hu017-02-notificaciones.png), [JUnit](../evidencias/resultado-junit-hu017.txt) | — |
| CP-HU017-03 | HU-017 | Pasa | David Álvarez (automatizado) | [08](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/08-cp-hu017-03-sin-observaciones.png), [JUnit](../evidencias/resultado-junit-hu017.txt) | — |
| CP-HU017-04 | HU-017 | Pasa | David Álvarez (automatizado) | [16](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/16-cp-hu017-04-observacion-pendiente.png) a [22](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/22-cp-hu017-04-ejecucion-permitida.png), [JUnit](../evidencias/resultado-junit-hu017.txt) | — |
| CP-HU016-01 | HU-016 | No ejecutado | — | HU no programada | — |
| CP-HU016-02 | HU-016 | No ejecutado | — | HU no programada | — |
| CP-HU016-03 | HU-016 | No ejecutado | — | HU no programada | — |

### Resultado obtenido en los casos de HU-017

- **CP-HU017-01:** el plan PA-2026-0002 quedó APROBADO y salió de la lista de pendientes. El coordinador recibió la notificación "El plan PA-2026-0002 fue aprobado por Director Demo. Ya puede iniciar su ejecución."
- **CP-HU017-02:** con la observación "Falta indicar qué procesos se auditarán.", el plan PA-2026-0001 quedó EN REVISION. El coordinador recibió una notificación con el texto de la observación.
- **CP-HU017-03:** al devolver sin observaciones apareció "Debe escribir las observaciones para devolver el plan." debajo del campo, marcado en rojo. No se abrió ningún diálogo y los dos planes siguieron PENDIENTE DE REVISION.
- **CP-HU017-04:**
  - En el paso 1 la aplicación no dejó iniciar la ejecución: "El plan PA-2026-0001 no se puede ejecutar: el director de área debe aprobarlo primero. Estado actual: EN REVISION."
  - Tampoco dejó reenviar el plan con la observación sin atender.
  - En el paso 2 la observación quedó atendida.
  - En el paso 3 siguió sin dejar ejecutar.
  - Solo después de reenviar el plan y de que el director lo aprobó de nuevo, el plan pasó a EN EJECUCION.

### Verificaciones adicionales

| Verificación | Estado | Evidencia |
|---|---|---|
| V-01 | Pasa | JUnit: "Solo el director de área puede aprobar o devolver planes"; [25](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/25-responsable-consulta.png) |
| V-02 | Pasa | JUnit: "El director no revisa planes de otra área" |
| V-03 | Pasa | JUnit: "Un plan en revisión del director no se puede modificar; uno devuelto sí"; [24](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/24-edicion-bloqueada.png) |
| V-04 | Pasa | JUnit: "Cada cambio de estado queda en el historial…"; [23](../../../Graficas/entrega-2/SGPI_evidencias_entrega_2/23-historial-plan.png) |

## 6. Resumen

| Indicador | Valor |
|---|---|
| Casos planeados | 17 (10 del prototipo, 4 de HU-017 y 3 de HU-016) |
| Casos ejecutados | 13 |
| Casos que pasan | 12 |
| Casos que fallan | 1 (CP-PRO-10) |
| Casos bloqueados | 0 |
| Casos no ejecutados | 4 (CP-PRO-08 y los 3 de HU-016) |
| Porcentaje de aprobación (pasan / ejecutados) | 92,3 % (12 de 13) |
| Verificaciones adicionales de HU-017 | 4 de 4 pasan |
| Pruebas JUnit de HU-017 | 12 de 12 pasan, en Linux y con Maven en Windows |

## 7. Defectos encontrados

| Defecto | Issue | Descripción | Severidad | Responsable | Estado |
|---|---|---|---|---|---|
| D-01 | PR #40 | En el prototipo del Sprint 2 el estado del plan se escogía a mano: se podía crear un plan directamente EN EJECUCION, sin aprobación. | Alta | David Álvarez | Corregido en el PR #40 (unido a `main` el 12 oct) |
| D-02 | PR #40 | `updatePlan` y `deletePlan` no validaban el rol en la capa de datos; solo la interfaz ocultaba el formulario (RNF-05). | Media | David Álvarez | Corregido en el PR #40 (unido a `main` el 12 oct) |
| D-03 | PR #40 | Las alertas con mensajes largos se cortaban en Linux. Se vio en la primera ejecución del recorrido. | Baja | David Álvarez | Corregido en el PR #40 (unido a `main` el 12 oct) |
| D-04 | #41 | `bootstrap_prototipo.sh` genera el proyecto con una versión anterior de `Main.java` y sobrescribe archivos; si alguien lo ejecuta, pierde el código actual. | Media | Juan Sebastián Martínez (propuesto) | Abierto |
| D-05 | #41 | La carpeta `tools/apache-maven-3.9.16` (97 archivos, unos 19 MB) está en el repositorio. | Baja | Juan Sebastián Martínez (propuesto) | Abierto |
| D-06 | PR #40 | El README del prototipo solo traía el comando de ejecución para Windows. | Baja | David Álvarez | Corregido en el README del PR #40; falta probarlo en Linux y macOS con Maven |
| D-07 | — | Las contraseñas de los usuarios de prueba y el usuario de la base H2 están escritos en el código, lo que va contra la DoD. Sirve para el prototipo, pero hay que sacarlos antes de una versión real. El PR #40 agrega uno más, el del director de prueba. | Baja | David Andrés Galindo (propuesto) | Abierto |
| D-08 | #42 | `develop` está desactualizada: los PR se unen directo a `main`. | Media | David Andrés Galindo (propuesto) | Abierto |

## 8. Decisión

### Por historia de usuario

| HU | ¿Cumple la Definition of Done? | Decisión |
|---|---|---|
| HU-017 | Todavía no. Cumple: compila, tiene pruebas JUnit de cada regla, sus casos pasan (también con `mvn clean test` en Windows), los mensajes están en español, valida roles, guarda el historial y tiene el script de la base de datos. Falta: que otro integrante revise el código (el PR #40 se unió a `main` el 12 de octubre sin esa revisión), arrancarlo en Windows con `mvn javafx:run` (CP-PRO-08) y llevarlo a `develop` (ver D-08). | Queda en *In review* hasta que otro integrante revise el código del PR #40. |
| HU-001, HU-002 y HU-006 | No se evaluaron: tienen avance en el prototipo, pero no tienen casos de prueba escritos. | No pasan a *Done*. Hay que escribir y ejecutar sus casos. |
| HU-016 | No | No se programó. |

### Del sprint (propuesta para aprobar en equipo)

El Sprint 2 no cumple la DoD de sprint:
- Ninguna HU queda en *Done*.
- `develop` no se unió a `main`.
- La Sprint Review y la Retrospectiva no están registradas.
- La daily del día 7 está vacía.

Se propone cerrar el Sprint 2 con el prototipo como resultado parcial y pasar las HU pendientes al Sprint 3, con los compromisos de la sección 9.

## 9. Compromisos

Son propuestas; quedan firmes cuando el equipo apruebe el acta.

| Acción | Responsable | Fecha límite |
|---|---|---|
| Revisar el código de HU-017 (PR #40, ya unido a `main`) y dejar los comentarios en el PR | David Andrés Galindo | 20 oct (congelamiento del Sprint 3) |
| Arrancar el prototipo en Windows con `mvn javafx:run` y guardar la captura (CP-PRO-08) | David Álvarez | 20 oct |
| Decidir el estado de un plan devuelto (EN REVISION o BORRADOR) y unificar HU-017, CU-01 y los casos de prueba | Juan Sebastián Martínez | 20 oct |
| Resolver D-04 y D-05 (#41) | Juan Sebastián Martínez | 20 oct |
| Actualizar `develop` con `main` o acordar que los PR van a `main` (#42) | David Andrés Galindo | 20 oct |
| Escribir los casos de prueba de HU-001, HU-002 y HU-006 | Responsable de cada HU | 20 oct |
| Registrar la Sprint Review y la Retrospectiva del Sprint 2 | Miguel Ángel Gómez | 20 oct |

## 10. Aprobación

| Nombre | Rol | Fecha |
|---|---|---|
| David Álvarez Rodríguez | Responsable de Gestión y Reportes | |
| David Andrés Galindo Rojas | Líder técnico | |
| Miguel Ángel Gómez López | Scrum Master | |
