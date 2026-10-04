# Cronograma de la fase de desarrollo — SGPI

**Responsable:** David Álvarez Rodríguez (Gestión y Reportes)
**Actualizado:** 3 de octubre de 2026
**Estado:** propuesta para aprobar en el Planning del Sprint 3

## Supuestos

- Sprints de **dos semanas**, de jueves a miércoles, igual que el Sprint 2 (23 sep – 7 oct).
- Cada sprint sigue la misma estructura del Sprint 2: Planning el primer día, desarrollo, **congelamiento de código y pruebas** el penúltimo día, y **Sprint Review + Retrospectiva** el último día.
- Stack: backend **Java 17 + Spring**, frontend **web** y base de datos relacional (**PostgreSQL**, por confirmar frente a MySQL según el RNF-09).
- **Fecha de entrega final: [POR CONFIRMAR CON EL PROFESOR].** El cronograma asume que el proyecto cierra a finales de noviembre; si la fecha es otra, se ajusta el sprint de cierre.
- Festivos en Colombia dentro del periodo: **12 de octubre**, **2 de noviembre** y **16 de noviembre** (lunes).

## Estado real del Sprint 2 al 3 de octubre

| Hito planeado | Fecha planeada | Estado al 3 oct |
|---|---|---|
| Diseño técnico: mockups, modelo de BD, diagramas UML y DoD | 23 – 27 sep | Diagramas de componentes y despliegue en el repo desde el 3 oct. DoD y formatos de pruebas en este cambio. El modelo de BD y los mockups existen como borradores, pero aún no están en el repo. |
| Configuración del entorno (Java/Spring, BD, repos) | 28 – 29 sep | Sin empezar: no hay código ni scripts de BD en el repo. |
| Desarrollo inicial de las HU en *Ready* | 30 sep – 5 oct | Sin empezar. |
| Congelamiento de código y actas de prueba | 6 oct | Pendiente. |
| Sprint Review | 7 oct | Pendiente. |

El Sprint 2 va con unos **6 días de atraso**. Las HU que estaban para programar en este sprint (HU-001, HU-002, HU-003, HU-005, HU-006 y HU-018) pasan a los siguientes sprints.

## Sprint 2 reajustado (3 – 7 oct)

| Fecha | Hito | Objetivo |
|---|---|---|
| 3 – 4 oct (sáb – dom) | Cierre del diseño técnico | Aprobar la DoD, el formato de actas de prueba y este cronograma. Subir los mockups (`Docs/Mockups`) y el modelo de BD (`Database/`). Decidir los estados del plan de acción. |
| 5 oct (lun) | Configuración del entorno | Esqueleto del proyecto Spring + frontend web en `develop`, PostgreSQL con el script del modelo relacional, ramas y plantilla de PR funcionando. Crear el milestone y los issues del Sprint 3. |
| 6 oct (mar) | Congelamiento y pruebas | Verificar que el entorno arranca y conecta con la base de datos. Llenar la primera acta de pruebas (prueba del entorno). |
| 7 oct (mié) | Sprint Review y Retrospectiva | Presentar el diseño técnico y el entorno funcionando. Registrar la Review y la Retro en `Docs/Review` y `Docs/Retrospective`. |

## Sprints de desarrollo

La distribución de HU sigue la prioridad de la Propuesta y las dependencias de cada issue. Primero va la base (acceso, roles y estructura); después, lo que depende de ella.

### Sprint 3 (8 – 21 oct) — Acceso, roles y registro de planes

| Fecha | Hito |
|---|---|
| 8 oct (jue) | Sprint Planning |
| 8 – 19 oct | Desarrollo (festivo: lunes 12 oct) |
| 20 oct (mar) | Congelamiento de código, pruebas y acta de pruebas |
| 21 oct (mié) | Sprint Review y Retrospectiva |

| HU | Historia | Prioridad | Responsable |
|---|---|---|---|
| HU-001 | Login en el sistema | Alta | David Galindo |
| HU-006 | Autenticación y control de roles | Media | David Galindo |
| HU-018 | Recuperación y restablecimiento de contraseña | Alta | David Álvarez |
| HU-008 | Administración y asignación de roles de usuario | Media | Por asignar |
| HU-010 | Gestión de la estructura organizacional | Media | Por asignar |
| HU-002 | Registro de un nuevo plan de acción | Media | Juan Sebastián Martínez |

### Sprint 4 (22 oct – 4 nov) — Evidencias, seguimiento y aprobación

| Fecha | Hito |
|---|---|
| 22 oct (jue) | Sprint Planning |
| 22 oct – 2 nov | Desarrollo (festivo: lunes 2 nov) |
| 3 nov (mar) | Congelamiento de código, pruebas y acta de pruebas |
| 4 nov (mié) | Sprint Review y Retrospectiva |

| HU | Historia | Prioridad | Responsable |
|---|---|---|---|
| HU-003 | Carga de evidencias documentales | Media | Juan Sebastián Martínez |
| HU-016 | Búsqueda avanzada y filtrado de evidencias | Alta | Por asignar |
| HU-017 | Aprobación y retroalimentación de planes de acción | Media | Por asignar |
| HU-012a | Visualización del tablero Kanban | Alta | Por asignar |
| HU-012b | Actualización de estado por arrastre en el Kanban | Media | Por asignar |
| HU-004 | Seguimiento de avance de actividades | Media | Por asignar |

### Sprint 5 (5 – 18 nov) — Notificaciones, auditoría, cierre e informes

| Fecha | Hito |
|---|---|
| 5 nov (jue) | Sprint Planning |
| 5 – 16 nov | Desarrollo (festivo: lunes 16 nov) |
| 17 nov (mar) | Congelamiento de código, pruebas y acta de pruebas |
| 18 nov (mié) | Sprint Review y Retrospectiva |

| HU | Historia | Prioridad | Responsable |
|---|---|---|---|
| HU-005 | Notificaciones de vencimiento de actividades | Alta | David Álvarez |
| HU-013 | Historial de auditoría y trazabilidad de cambios | Media | Por asignar |
| HU-019 | Gestión de versiones y actualización de evidencias | Media | Por asignar |
| HU-020 | Cierre y evaluación final de planes de acción | Alta | Por asignar |
| HU-014a | Informe consolidado de cumplimiento | Media | Por asignar |
| HU-009 | Configuración general del sistema | Media | Por asignar |

### Cierre (19 nov – [entrega final]) — Prioridad baja, estabilización y entrega

| Fecha | Hito |
|---|---|
| 19 nov (jue) | Planning de cierre |
| 19 nov – [entrega − 2 días] | HU de prioridad baja (solo si las anteriores están en *Done*), corrección de defectos y pruebas de regresión |
| [entrega − 1 día] | Congelamiento final, acta de pruebas final y guía de usuario |
| [entrega final] | Entrega al profesor, Review y Retrospectiva final |

| HU | Historia | Prioridad | Responsable |
|---|---|---|---|
| HU-011 | Exportación de planes de acción a PDF y Excel | Baja | Por asignar |
| HU-014b | Drill-down del informe gerencial | Baja | Por asignar |
| HU-015 | Notificaciones por correo electrónico y alertas | Baja | Por asignar |
| HU-007 | Pruebas de validación del módulo de informes | Media | Por asignar |

## Reglas para mantener el cronograma al día

- Una HU solo cuenta como avance cuando cumple la [Definition of Done](../Calidad/definition-of-done.md).
- Cada sprint tiene su propio milestone en GitHub (Sprint 3, Sprint 4…) con la fecha de su Review, y sus issues asignados.
- Si al cerrar un sprint quedan HU sin terminar, pasan al siguiente sprint y este documento se actualiza en la Retrospectiva.
- El avance se mide en HU terminadas por sprint; ese dato alimenta el Reporte Gerencial.
