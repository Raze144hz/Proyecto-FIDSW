# Trazabilidad: historias de usuario ↔ issues ↔ estado real

**Responsable:** David Álvarez Rodríguez (Gestión y Reportes)
**Corte:** 18 de octubre de 2026, rama `main` en `56b1eb7`

Hasta esta fecha varias HU tenían más de un issue, con títulos o códigos distintos a los de la [propuesta](../propuesta-y-requisitos.md), y muchos issues estaban cerrados aunque la HU no se había programado. Esta tabla define **un issue canónico por HU**: el trabajo, los PR y el tablero se vinculan a ese issue. Los repetidos se cerraron como *duplicate* con un comentario que apunta al canónico.

**Estado del issue** es lo que muestra GitHub. **Estado real** es lo que hay en el código y los documentos. Que un issue esté cerrado no quiere decir que la HU esté terminada: según la [Definition of Done](../Calidad/definition-of-done.md), ninguna HU está en *Done* todavía.

## Mapeo

| HU | Historia (propuesta) | Issue canónico | Estado del issue | Duplicados cerrados | Prioridad propuesta / issue | Estado real | Sprint del cronograma |
|---|---|---|---|---|---|---|---|
| HU-001 | Login en el sistema | [#1](https://github.com/Raze144hz/Proyecto-FIDSW/issues/1) | Abierto (reabierto el 18 oct) | — | Alta / Alta | Avance en el prototipo: inicio de sesión con BCrypt. Falta el bloqueo tras 3 intentos fallidos del criterio de aceptación. | 3 |
| HU-002 | Registro de un nuevo plan de acción | [#19](https://github.com/Raze144hz/Proyecto-FIDSW/issues/19) | Abierto (reabierto el 18 oct) | #2 | Media / Alta | Avance en el prototipo: crear, editar, eliminar y consultar planes. El formulario pide objetivo, área y fechas; el issue pide nombre, responsable y periodo. | 3 |
| HU-003 | Carga de evidencias documentales | [#18](https://github.com/Raze144hz/Proyecto-FIDSW/issues/18) | **Cerrado sin implementar** | #3 | Media / Alta | Sin iniciar. Diseñada en el modelo: cada evidencia pertenece a una actividad (RN-03). | 4 |
| HU-004 | Seguimiento de avance de actividades | [#4](https://github.com/Raze144hz/Proyecto-FIDSW/issues/4) | **Cerrado sin implementar** | — | Media / Alta | Diseñada el 18 oct (avance = actividades finalizadas / total, RN-01) en los diagramas, el modelo objetivo y los [casos de prueba](../Calidad/casos-de-prueba-HU004-HU020.md). Sin programar. | 4 |
| HU-005 | Notificaciones de vencimiento de actividades | [#20](https://github.com/Raze144hz/Proyecto-FIDSW/issues/20) | Abierto | #5 (era de reportes, con la numeración anterior) | Alta / Media | Sin iniciar. Las notificaciones internas de HU-017 sirven de base. | 5 |
| HU-006 | Autenticación y control de roles | [#6](https://github.com/Raze144hz/Proyecto-FIDSW/issues/6) | Abierto (reabierto el 18 oct) | — | Media / Alta | Avance en el prototipo: roles ADMIN, COORDINADOR, RESPONSABLE y DIRECTOR. El issue nombra administrador, coordinador, directivo y consulta. | 3 |
| HU-007 | Pruebas de validación del módulo de informes | [#7](https://github.com/Raze144hz/Proyecto-FIDSW/issues/7) | **Cerrado sin implementar** | — | Media / Media | Sin iniciar; depende de HU-014a. | Cierre |
| HU-008 | Administración y asignación de roles de usuario | [#8](https://github.com/Raze144hz/Proyecto-FIDSW/issues/8) | Abierto (reabierto el 18 oct) | — | Media / Alta | Sin iniciar; sin responsable asignado. | 3 |
| HU-009 | Configuración general del sistema | [#9](https://github.com/Raze144hz/Proyecto-FIDSW/issues/9) | **Cerrado sin implementar** | — | Media / Media | Sin iniciar. | 5 |
| HU-010 | Gestión de la estructura organizacional | [#10](https://github.com/Raze144hz/Proyecto-FIDSW/issues/10) | Abierto (reabierto el 18 oct) | — | Media / Media | Sin iniciar; sin responsable asignado. | 3 |
| HU-011 | Exportación de planes de acción a PDF y Excel | [#22](https://github.com/Raze144hz/Proyecto-FIDSW/issues/22) | Abierto | #11, #21 | Baja / Media | Sin iniciar. | Cierre |
| HU-012a | Visualización del tablero Kanban | [#12](https://github.com/Raze144hz/Proyecto-FIDSW/issues/12) (sin dividir) | **Cerrado sin implementar** | — | Alta / Media | Sin iniciar. Las columnas son los estados de actividad de RN-02. | 4 |
| HU-012b | Actualización de estado por arrastre en el Kanban | [#12](https://github.com/Raze144hz/Proyecto-FIDSW/issues/12) (sin dividir) | **Cerrado sin implementar** | — | Media / Media | Sin iniciar. | 4 |
| HU-013 | Historial de auditoría y trazabilidad de cambios | [#23](https://github.com/Raze144hz/Proyecto-FIDSW/issues/23) | Abierto | #13 | Media / Media | Parcial: el prototipo registra los cambios de **estado** del plan. Falta auditar los cambios de actividades y evidencias con valor anterior y nuevo, como piden el issue y RNF-10. | 5 |
| HU-014a | Informe consolidado de cumplimiento | [#14](https://github.com/Raze144hz/Proyecto-FIDSW/issues/14) (sin dividir) | **Cerrado sin implementar** | — | Media / Media | Sin iniciar. | 5 |
| HU-014b | Drill-down del informe gerencial | [#14](https://github.com/Raze144hz/Proyecto-FIDSW/issues/14) (sin dividir) | **Cerrado sin implementar** | — | Baja / Media | Sin iniciar. | Cierre |
| HU-015 | Notificaciones por correo electrónico y alertas | [#15](https://github.com/Raze144hz/Proyecto-FIDSW/issues/15) | **Cerrado sin implementar** | — | Baja / Baja | Sin iniciar. | Cierre |
| HU-016 | Búsqueda avanzada y filtrado de evidencias | [#24](https://github.com/Raze144hz/Proyecto-FIDSW/issues/24) | Abierto | #16 | Alta / Media | Diseñada (diagramas y [casos de prueba](../Calidad/casos-de-prueba-HU016-HU017.md)). Sin programar; depende de HU-003. | 4 |
| HU-017 | Aprobación y retroalimentación de planes de acción | [#25](https://github.com/Raze144hz/Proyecto-FIDSW/issues/25) | Cerrado | #17 (título de la numeración anterior) | Media / Alta | Implementada en el PR #40, con 12 pruebas JUnit que pasan en Windows. Queda *In review*: el PR se unió sin la revisión de otro integrante. | 2 (Entrega 2) |
| HU-018 | Recuperación y restablecimiento de contraseña | [#26](https://github.com/Raze144hz/Proyecto-FIDSW/issues/26) | Abierto | — | Alta / Media | Sin iniciar. | 3 |
| HU-019 | Gestión de versiones y actualización de evidencias | [#27](https://github.com/Raze144hz/Proyecto-FIDSW/issues/27) | Abierto | — | Media / Media | Sin iniciar. | 5 |
| HU-020 | Cierre y evaluación final de planes de acción | [#28](https://github.com/Raze144hz/Proyecto-FIDSW/issues/28) | Abierto | — | Alta / Alta | Diseñada el 18 oct (cierre del director con evaluación, estado FINALIZADO, RN-05). Sin programar: el botón **Finalizar** del prototipo no pide evaluación. | 5 |

Issues que no son HU: [#37](https://github.com/Raze144hz/Proyecto-FIDSW/issues/37) (reporte gerencial del Sprint 2), [#38](https://github.com/Raze144hz/Proyecto-FIDSW/issues/38) (acta de pruebas del Sprint 2), [#39](https://github.com/Raze144hz/Proyecto-FIDSW/issues/39) (evidencias de la Entrega 2) y [#41](https://github.com/Raze144hz/Proyecto-FIDSW/issues/41) (limpieza del repositorio) están cerrados y terminados. [#42](https://github.com/Raze144hz/Proyecto-FIDSW/issues/42) (rama `develop` desactualizada) se cerró el 18 de octubre: el PR #53 puso `develop` al día y los PR van a `develop`.

## Cambios hechos en GitHub el 18 de octubre

- Cerrados como *duplicate*, con un comentario que apunta al canónico: #2 → #19, #3 → #18, #5 → #14, #11 → #22, #21 → #22, #13 → #23, #16 → #24 y #17 → #25.
- Reabiertos, como pide el [Planning del Sprint 3](Acta-Planning-Sprint3.md): #1, #6, #8, #10 y #19, con un comentario sobre lo que falta en cada uno.
- Cerrado como terminado: #42.
- Corregido en #28 (HU-020) el nombre del estado final: "Finalizado" en lugar de "Cerrado".

## Pendiente para el Scrum Master y el equipo

- Reabrir o devolver al backlog los issues **cerrados sin implementar**: #4, #7, #9, #12, #14, #15 y #18. Mientras estén cerrados, el tablero muestra como hechas HU que no existen en el código.
- Decidir si #25 (HU-017) se reabre hasta que otro integrante revise el PR #40, como pide la DoD.
- Dividir #12 en HU-012a y HU-012b, y #14 en HU-014a y HU-014b, como en la propuesta.
- Alinear la prioridad de cada issue con la de la propuesta (columna "Prioridad propuesta / issue"): hoy no coinciden en 12 HU.
- Mover los issues del Sprint 3 al milestone del Sprint 3 y revisar las columnas de GitHub Projects. Este documento no verifica el tablero ni los milestones; las capturas de `Graficas/entrega-1` son de la Entrega 1.
- Resolver las diferencias entre criterios y prototipo anotadas en la tabla (HU-001, HU-002, HU-006 y HU-013) antes de cerrar esas HU.
