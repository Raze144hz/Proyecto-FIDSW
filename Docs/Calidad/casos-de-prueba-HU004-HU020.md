# Casos de prueba — HU-004 y HU-020

Casos diseñados a partir de los criterios de aceptación del issue [#4](https://github.com/Raze144hz/Proyecto-FIDSW/issues/4) (HU-004), del issue [#28](https://github.com/Raze144hz/Proyecto-FIDSW/issues/28) (HU-020) y de las [reglas de negocio precisadas el 18 de octubre](../propuesta-y-requisitos.md#23-reglas-de-negocio-precisadas-18-de-octubre-de-2026) (RN-01 a RN-05). Siguen el [formato de caso de prueba](formato-caso-de-prueba.md).

> **Estado: diseñados, no ejecutados.** El prototipo actual no tiene actividades, cálculo de avance ni evaluación de cierre, así que estos casos no se pueden ejecutar todavía. La sección **Ejecución** se llena cuando HU-004 y HU-020 estén programadas (Sprint 4 y Sprint 5 del [cronograma](../Planning/cronograma-desarrollo.md)).

Datos comunes: un plan "Auditoría interna de procesos 2026" del área Calidad, en estado EN EJECUCION, con el coordinador `coordinador@sgpi.local`, la directora del área `director@sgpi.local` y el responsable `responsable@sgpi.local`.

---

## HU-004 — Seguimiento de avance de actividades

### CP-HU004-01 — El detalle del plan muestra el avance calculado

| Campo | Valor |
|---|---|
| Criterio de aceptación | Dado que un plan tiene actividades registradas, cuando accedo a su detalle, entonces el sistema muestra el porcentaje de avance calculado. |
| Tipo / prioridad | Funcional / Alta |
| Rol | Director de área |
| Precondiciones | El plan tiene 4 actividades: 1 en FINALIZADA, 1 en EN_PROCESO y 2 en POR_HACER. |
| Datos de prueba | 1 de 4 actividades finalizadas |

**Pasos:** 1. Iniciar sesión como director. 2. Abrir el detalle del plan.

**Resultado esperado:** el avance mostrado es 25 % (1 / 4). No hay ningún campo para escribir el porcentaje a mano.

### CP-HU004-02 — Al finalizar una actividad el avance se recalcula

| Campo | Valor |
|---|---|
| Criterio de aceptación | Dado que se completa una actividad, cuando se actualiza su estado, entonces el porcentaje de avance del plan se recalcula automáticamente. |
| Tipo / prioridad | Funcional / Alta |
| Rol | Coordinador |
| Precondiciones | El mismo plan de CP-HU004-01 (25 %). La actividad en EN_PROCESO tiene una evidencia cargada. |
| Datos de prueba | Actividad en EN_PROCESO con evidencia |

**Pasos:** 1. Iniciar sesión como coordinador. 2. Pasar la actividad en EN_PROCESO a FINALIZADA. 3. Volver al detalle del plan.

**Resultado esperado:** el avance pasa de 25 % a 50 % sin recargar datos a mano, y el historial registra el cambio de estado de la actividad.

### CP-HU004-03 — Una actividad sin evidencia no pasa a FINALIZADA (RN-03, CU-03)

| Campo | Valor |
|---|---|
| Regla de negocio | Una actividad solo pasa a *Finalizada* si tiene al menos una evidencia. |
| Tipo / prioridad | Funcional / Alta |
| Rol | Coordinador |
| Precondiciones | El plan tiene una actividad en POR_HACER sin evidencias. |
| Datos de prueba | Actividad sin evidencia |

**Pasos:** 1. Iniciar sesión como coordinador. 2. Intentar mover la actividad a FINALIZADA.

**Resultado esperado:** el sistema rechaza el cambio y explica que falta la evidencia; la actividad sigue en su estado y el avance no cambia.

### CP-HU004-04 — Un plan sin actividades tiene avance 0 % (RN-01)

| Campo | Valor |
|---|---|
| Regla de negocio | Un plan sin actividades tiene avance 0 %. |
| Tipo / prioridad | Funcional / Media |
| Rol | Director de área |
| Precondiciones | Existe un plan aprobado sin actividades. |
| Datos de prueba | 0 actividades |

**Pasos:** 1. Abrir el detalle del plan.

**Resultado esperado:** el avance es 0 % y no se produce ningún error de división.

### CP-HU004-05 — El seguimiento guarda el avance calculado (RN-04)

| Campo | Valor |
|---|---|
| Regla de negocio | El coordinador registra cortes de seguimiento con un comentario; el sistema guarda el avance calculado en esa fecha. |
| Tipo / prioridad | Funcional / Media |
| Rol | Coordinador |
| Precondiciones | El plan tiene 50 % de avance (2 de 4 actividades finalizadas). |
| Datos de prueba | Comentario: "Se cerraron las auditorías de los procesos 1 y 2." |

**Pasos:** 1. Registrar un seguimiento con el comentario. 2. Consultar los seguimientos del plan.

**Resultado esperado:** aparece el seguimiento con la fecha, el comentario y un avance de 50 %. El formulario no deja escribir el porcentaje.

---

## HU-020 — Cierre y evaluación final de planes de acción

### CP-HU020-01 — Con avance 100 % el director accede al formulario de evaluación

| Campo | Valor |
|---|---|
| Criterio de aceptación | Dado que un plan alcanza el 100 % de avance en sus actividades, cuando accedo a la opción de cierre, entonces el sistema me habilita el formulario de evaluación final. |
| Tipo / prioridad | Funcional / Alta |
| Rol | Director de área |
| Precondiciones | Las 4 actividades del plan están en FINALIZADA (avance 100 %). |
| Datos de prueba | Avance 100 % |

**Pasos:** 1. Iniciar sesión como director. 2. Abrir el plan. 3. Elegir **Finalizar plan**.

**Resultado esperado:** se abre el formulario de evaluación final con la calificación y las observaciones de cierre.

### CP-HU020-02 — Finalizar con la evaluación deja el plan FINALIZADO y bloqueado

| Campo | Valor |
|---|---|
| Criterio de aceptación | Dado que completo el formulario con la calificación y observaciones de cierre, cuando confirmo la acción, entonces el sistema cambia el estado del plan a FINALIZADO ("Cerrado" en el issue) y bloquea futuras modificaciones. |
| Tipo / prioridad | Funcional / Alta |
| Rol | Director de área, luego coordinador |
| Precondiciones | Las del caso CP-HU020-01. |
| Datos de prueba | Calificación: 4 · Observaciones: "Se cumplieron los objetivos de la auditoría." |

**Pasos:** 1. Llenar el formulario y confirmar. 2. Revisar el estado y el historial del plan. 3. Iniciar sesión como coordinador e intentar modificar el plan o una actividad.

**Resultado esperado:** el plan queda FINALIZADO con la calificación, las observaciones y la fecha de cierre; el historial registra el cierre; el coordinador no puede modificar el plan ni sus actividades.

### CP-HU020-03 — No se finaliza un plan con avance menor a 100 %

| Campo | Valor |
|---|---|
| Regla de negocio | El director solo cierra el plan cuando su avance es 100 % (RN-05). |
| Tipo / prioridad | Funcional / Alta |
| Rol | Director de área |
| Precondiciones | El plan tiene 75 % de avance (3 de 4 actividades finalizadas). |
| Datos de prueba | Avance 75 % |

**Pasos:** 1. Abrir el plan. 2. Intentar finalizarlo.

**Resultado esperado:** la opción de cierre no está disponible o el sistema la rechaza indicando que el avance no es 100 %; el plan sigue EN EJECUCION.

### CP-HU020-04 — Solo el director del área finaliza el plan (RNF-05)

| Campo | Valor |
|---|---|
| Regla de negocio | El cierre lo hace el director de área (RN-05); lo no autorizado se niega por defecto (RNF-05). |
| Tipo / prioridad | Funcional (seguridad) / Alta |
| Rol | Coordinador, y un director de otra área |
| Precondiciones | Plan del área Calidad con avance 100 %; existe un director del área Financiera. |
| Datos de prueba | Avance 100 % |

**Pasos:** 1. Como coordinador, intentar finalizar el plan. 2. Como director de Financiera, intentar finalizarlo.

**Resultado esperado:** en los dos casos el sistema no permite la acción y el plan sigue EN EJECUCION.

### CP-HU020-05 — La evaluación es obligatoria

| Campo | Valor |
|---|---|
| Criterio de aceptación | El formulario de cierre registra la calificación y las observaciones (issue #28). |
| Tipo / prioridad | Funcional / Media |
| Rol | Director de área |
| Precondiciones | Las del caso CP-HU020-01. |
| Datos de prueba | Calificación vacía |

**Pasos:** 1. Abrir el formulario de evaluación. 2. Confirmar sin calificación.

**Resultado esperado:** el sistema muestra el mensaje junto al campo (RNF-06) y el plan no cambia de estado.
