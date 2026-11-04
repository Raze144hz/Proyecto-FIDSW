# Acta de pruebas — Sprint 4

> Hecha con el [formato de acta de pruebas](../formato-acta-de-pruebas.md). Los casos están en [casos-de-prueba-HU004-HU020.md](../casos-de-prueba-HU004-HU020.md) y las capturas en [`Graficas/sprint-4`](../../../Graficas/sprint-4). Cambios probados: PR #76.

## 1. Datos generales

| Campo | Valor |
|---|---|
| Proyecto | SGPI — Sistema de Gestión de Procesos Institucionales |
| Sprint | 4 (22 oct – 4 nov de 2026) |
| Fecha de la sesión de pruebas | 4 de noviembre de 2026 |
| Versión probada | Rama `mejoras/refactor-hu001-hu004` (PR #76), commit `7f7697b` más el ajuste del detalle del plan que se sube junto con esta acta |
| Entorno | Windows Server 2022, JDK 17.0.21, Maven 3.9.9, JavaFX 17.0.10, H2 2.2.224 (versiones del `pom.xml`) |
| Forma de ejecución | Manual sobre la interfaz (`mvn javafx:run -Djavafx.platform=win`), con una captura por paso, y pruebas JUnit (`mvn clean test`) |
| Responsable del acta | |

## 2. Participantes

| Nombre | Rol en las pruebas |
|---|---|
| | Preparó los datos, ejecutó los casos y registra el acta |
| David Álvarez Rodríguez | Revisa el PR #76 y valida los resultados (pendiente) |
| Miguel Ángel Gómez López | Aprueba el acta como Scrum Master (pendiente) |

## 3. Alcance

- **HU incluidas:** HU-004 (seguimiento del avance), implementada en el PR #76 junto con las actividades del plan de HU-002 y el bloqueo de HU-001.
- **HU no incluidas y motivo:**
  - HU-020 (cierre y evaluación final): no está programada; va en el Sprint 5 según el [cronograma](../../Planning/cronograma-desarrollo.md). Sus casos se registran como no ejecutados.
  - HU-003 (evidencias) y HU-016: no se programaron en este sprint. Por eso los casos de HU-004 que dependen de evidencias no se pueden ejecutar completos.
- **Reglas de negocio verificadas:** RN-01 (avance = actividades finalizadas / total, y 0 % sin actividades). Además, un plan con actividades solo se finaliza al 100 %.

### Datos de prueba

| Plan | Estado | Actividades |
|---|---|---|
| PA-2026-0001 "Auditoría interna de procesos 2026" (Calidad) | EN EJECUCION | 4: compras, contratación, archivo e informe. Las dos primeras asignadas a Responsable Demo. |
| PA-2026-0002 "Capacitación en gestión documental" (Calidad) | APROBADO | Ninguna |

Los dos planes recorrieron el flujo real: el coordinador los envió a revisión y `director@sgpi.local` los aprobó. Después el coordinador inició la ejecución de PA-2026-0001.

> **Nombres de los estados.** Los casos se escribieron con los estados POR_HACER y EN_PROCESO. La aplicación usa PENDIENTE y EN PROGRESO, que son los de la [guía de usuario](../../guia-de-usuario.md). Se ejecutó con estos últimos y hay que actualizar el documento de casos.

## 4. Casos de prueba

Los casos de HU-004 (CP-HU004-01 a 05) y de HU-020 (CP-HU020-01 a 05) están en [casos-de-prueba-HU004-HU020.md](../casos-de-prueba-HU004-HU020.md).

### Verificaciones adicionales

No están en el documento de casos.

| Verificación | Qué se prueba |
|---|---|
| V-01 | Un plan con actividades sin finalizar no se puede finalizar |
| V-02 | Cada cambio de estado de una actividad queda en el historial del plan con el estado anterior y el nuevo |
| V-03 | Bloqueo de la cuenta tras 3 intentos fallidos (HU-001) |
| V-04 | `mvn clean test` |

## 5. Resultados

| Caso | HU | Estado | Ejecutado por | Evidencia | Defecto |
|---|---|---|---|---|---|
| CP-HU004-01 | HU-004 | Pasa | | [coordinador](../../../Graficas/sprint-4/cp-hu004-01-avance-25.png), [director](../../../Graficas/sprint-4/cp-hu004-01-director-25.png) | D-01 (corregido) |
| CP-HU004-02 | HU-004 | Pasa con observación | | [avance 50 %](../../../Graficas/sprint-4/cp-hu004-02-avance-50.png), [historial](../../../Graficas/sprint-4/cp-hu004-02-historial.png) | D-02 |
| CP-HU004-03 | HU-004 | No ejecutado | — | Necesita la carga de evidencias (HU-003), que no está programada | D-02 |
| CP-HU004-04 | HU-004 | Pasa | | [sin actividades](../../../Graficas/sprint-4/cp-hu004-04-sin-actividades-0.png) | — |
| CP-HU004-05 | HU-004 | No ejecutado | — | Los cortes de seguimiento con comentario (RN-04) no están programados | D-03 |
| CP-HU020-01 | HU-020 | No ejecutado | — | HU no programada (Sprint 5) | — |
| CP-HU020-02 | HU-020 | No ejecutado | — | HU no programada (Sprint 5) | — |
| CP-HU020-03 | HU-020 | No ejecutado | — | HU no programada. La regla del 100 % sí existe, pero el cierre lo hace el coordinador y no el director; ver V-01 | — |
| CP-HU020-04 | HU-020 | No ejecutado | — | HU no programada (Sprint 5) | — |
| CP-HU020-05 | HU-020 | No ejecutado | — | HU no programada (Sprint 5) | — |

### Resultado obtenido en los casos de HU-004

- **CP-HU004-01:** con 1 actividad FINALIZADA, 1 EN PROGRESO y 2 PENDIENTE, la ventana *Actividades del plan* muestra "25 % (1 de 4 finalizadas)". La columna *Avance* de la tabla del coordinador y la del director muestran 25 %. El detalle del plan del director muestra "Avance: 25 % (1 de 4 actividades finalizadas)". No hay ningún campo para escribir el porcentaje: la actividad EN PROGRESO no suma.
- **CP-HU004-02:** al pasar "Auditar proceso de contratación" de EN PROGRESO a FINALIZADA, la barra cambia a "50 % (2 de 4 finalizadas)" sin recargar nada. Al cerrar la ventana, la columna *Avance* ya muestra 50 %. El historial registra `AVANCE_ACTIVIDAD` con "Auditar proceso de contratación: EN PROGRESO -> FINALIZADA". Observación: la precondición pide que la actividad tenga una evidencia cargada, y eso no se pudo preparar (D-02).
- **CP-HU004-04:** el plan PA-2026-0002, aprobado y sin actividades, muestra 0 % en la tabla y "Avance: 0 % (sin actividades registradas)" en el detalle del director. No hubo error.

### Verificaciones adicionales

| Verificación | Estado | Evidencia |
|---|---|---|
| V-01 | Pasa | Con el plan al 50 %, *Finalizar plan* muestra "El plan PA-2026-0001 tiene 2 actividad(es) sin finalizar. Su avance es 50 %." y el plan sigue EN EJECUCION: [captura](../../../Graficas/sprint-4/cp-hu004-cierre-rechazado-50.png). JUnit: `ActividadesPlanTest` |
| V-02 | Pasa | [historial](../../../Graficas/sprint-4/cp-hu004-02-historial.png): tres registros `AVANCE_ACTIVIDAD`, cada uno con el estado anterior y el nuevo |
| V-03 | Pasa | Prueba manual del 3 de noviembre: al tercer intento fallido con `coordinador@sgpi.local` aparece "Cuenta bloqueada… 15 minuto(s)". JUnit: `InicioSesionTest` (8 pruebas) |
| V-04 | Pasa | `mvn clean test` en Windows: 37 pruebas, 0 fallos, BUILD SUCCESS |

## 6. Resumen

| Indicador | Valor |
|---|---|
| Casos planeados | 10 (5 de HU-004 y 5 de HU-020) |
| Casos ejecutados | 3 |
| Casos que pasan | 3 (uno con observación) |
| Casos que fallan | 0 |
| Casos bloqueados | 0 |
| Casos no ejecutados | 7 (CP-HU004-03, CP-HU004-05 y los 5 de HU-020) |
| Porcentaje de aprobación (pasan / ejecutados) | 100 % (3 de 3) |
| Verificaciones adicionales | 4 de 4 pasan |
| Pruebas JUnit | 37 de 37 pasan |

## 7. Defectos y pendientes encontrados

| Defecto | Issue | Descripción | Severidad | Responsable | Estado |
|---|---|---|---|---|---|
| D-01 | PR #76 | El detalle del plan del director no mostraba el porcentaje de avance; solo aparecía en la columna de la tabla. CP-HU004-01 pide verlo en el detalle. | Baja | | Corregido en el PR #76: el detalle muestra "Avance: N % (x de y actividades finalizadas)" |
| D-02 | #4 | Una actividad pasa a FINALIZADA sin evidencias. La regla RN-03 pide al menos una, pero depende de HU-003. | Media | Responsable de HU-003 | Abierto |
| D-03 | #4 | No existen los cortes de seguimiento con comentario (RN-04). | Media | Por asignar | Abierto |
| D-04 | — | El documento de casos usa los estados POR_HACER y EN_PROCESO; la aplicación usa PENDIENTE y EN PROGRESO. | Baja | | Abierto |

## 8. Decisión

### Por historia de usuario

| HU | ¿Cumple la Definition of Done? | Decisión |
|---|---|---|
| HU-004 | Todavía no. Cumple: compila, tiene pruebas JUnit de cada regla, los casos ejecutados pasan y la documentación está al día. Falta: la revisión del PR #76 por otro integrante y los criterios que dependen de evidencias (RN-03) y de los cortes de seguimiento (RN-04). | Queda en *In review*. Lo que falta se maneja como D-02 y D-03. |
| HU-001 | El criterio que faltaba (bloqueo tras 3 intentos) ya está implementado y probado (V-03). Falta la revisión del PR #76. | Queda en *In review*. |
| HU-020 | No | No se programó; va en el Sprint 5. |

## 9. Compromisos

Son propuestas; quedan firmes cuando el equipo apruebe el acta.

| Acción | Responsable | Fecha límite |
|---|---|---|
| Revisar el PR #76 y dejar los comentarios | David Álvarez Rodríguez | Planning del Sprint 5 |
| Cambiar POR_HACER y EN_PROCESO por PENDIENTE y EN PROGRESO en los casos de HU-004 (D-04) | | Planning del Sprint 5 |
| Programar la carga de evidencias (HU-003) y conectarla con el paso a FINALIZADA (D-02) | Responsable de HU-003 | Sprint 5 |
| Ejecutar CP-HU004-03 y CP-HU004-05 cuando existan las evidencias y los cortes de seguimiento | | Sprint 5 |

## 10. Aprobación

| Nombre | Rol | Fecha |
|---|---|---|
| | Ejecutó las pruebas | |
| David Álvarez Rodríguez | Revisor del PR #76 | |
| Miguel Ángel Gómez López | Scrum Master | |
