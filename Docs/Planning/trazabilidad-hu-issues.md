# Trazabilidad: historias de usuario ↔ issues ↔ estado real

**Responsable:** David Álvarez Rodríguez (Gestión y Reportes)
**Corte:** 18 de octubre de 2026, rama `main` en `56b1eb7`

Hasta esta fecha varias HU tenían más de un issue, con títulos, códigos o prioridades distintos a los de la [propuesta](../propuesta-y-requisitos.md), y muchos issues estaban cerrados aunque la HU no se había programado. Esta tabla define **un issue canónico por HU**: el trabajo, los PR y el tablero se vinculan a ese issue. Los repetidos se cerraron como *duplicate* con un comentario que apunta al canónico, y los 22 issues canónicos quedaron abiertos y con la prioridad de la propuesta.

**Estado real** es lo que hay en el código y los documentos. Según la [Definition of Done](../Calidad/definition-of-done.md), ninguna HU está en *Done* todavía, así que todos los issues de HU siguen abiertos; un issue se cierra solo cuando su HU cumple la DoD.

## Mapeo

| HU | Historia (propuesta) | Issue canónico | Issues cerrados que apuntan a él | Prioridad | Estado real | Sprint del cronograma |
|---|---|---|---|---|---|---|
| HU-001 | Login en el sistema | [#1](https://github.com/Raze144hz/Proyecto-FIDSW/issues/1) | — | Alta | Avance en el prototipo: inicio de sesión con BCrypt. Falta el bloqueo tras 3 intentos fallidos del criterio de aceptación. | 3 |
| HU-002 | Registro de un nuevo plan de acción | [#19](https://github.com/Raze144hz/Proyecto-FIDSW/issues/19) | #2 | Media | Avance en el prototipo: crear, editar, eliminar y consultar planes. El formulario pide objetivo, área y fechas; el issue pide nombre, responsable y periodo. | 3 |
| HU-003 | Carga de evidencias documentales | [#18](https://github.com/Raze144hz/Proyecto-FIDSW/issues/18) | #3 | Media | Sin iniciar. Diseñada en el modelo: cada evidencia pertenece a una actividad (RN-03). El issue y el CU-02 no coinciden en el actor ni en el tamaño máximo (10 MB frente a 20 MB). | 4 |
| HU-004 | Seguimiento de avance de actividades | [#4](https://github.com/Raze144hz/Proyecto-FIDSW/issues/4) | — | Media | Diseñada el 18 oct (avance = actividades finalizadas / total, RN-01) en los diagramas, el modelo objetivo y los [casos de prueba](../Calidad/casos-de-prueba-HU004-HU020.md). Sin programar. | 4 |
| HU-005 | Notificaciones de vencimiento de actividades | [#20](https://github.com/Raze144hz/Proyecto-FIDSW/issues/20) | — | Alta | Sin iniciar. Las notificaciones internas de HU-017 sirven de base. | 5 |
| HU-006 | Autenticación y control de roles | [#6](https://github.com/Raze144hz/Proyecto-FIDSW/issues/6) | — | Media | Avance en el prototipo: roles ADMIN, COORDINADOR, RESPONSABLE y DIRECTOR. El issue nombra administrador, coordinador, directivo y consulta. | 3 |
| HU-007 | Pruebas de validación del módulo de informes | [#7](https://github.com/Raze144hz/Proyecto-FIDSW/issues/7) | — | Media | Sin iniciar; depende de HU-014a. | Cierre |
| HU-008 | Administración y asignación de roles de usuario | [#8](https://github.com/Raze144hz/Proyecto-FIDSW/issues/8) | — | Media | Sin iniciar; sin responsable asignado. | 3 |
| HU-009 | Configuración general del sistema | [#9](https://github.com/Raze144hz/Proyecto-FIDSW/issues/9) | — | Media | Sin iniciar. | 5 |
| HU-010 | Gestión de la estructura organizacional | [#10](https://github.com/Raze144hz/Proyecto-FIDSW/issues/10) | — | Media | Sin iniciar; sin responsable asignado. | 3 |
| HU-011 | Exportación de planes de acción a PDF y Excel | [#22](https://github.com/Raze144hz/Proyecto-FIDSW/issues/22) | #11, #21 | Baja | Sin iniciar. | Cierre |
| HU-012a | Visualización del tablero Kanban | [#56](https://github.com/Raze144hz/Proyecto-FIDSW/issues/56) | #12 (dividida) | Alta | Sin iniciar. Las columnas son los estados de actividad de RN-02. | 4 |
| HU-012b | Actualización de estado por arrastre en el Kanban | [#57](https://github.com/Raze144hz/Proyecto-FIDSW/issues/57) | #12 (dividida) | Media | Sin iniciar. | 4 |
| HU-013 | Historial de auditoría y trazabilidad de cambios | [#23](https://github.com/Raze144hz/Proyecto-FIDSW/issues/23) | #13 | Media | Parcial: el prototipo registra los cambios de **estado** del plan. Falta auditar los cambios de actividades y evidencias con valor anterior y nuevo, como piden el issue y RNF-10. | 5 |
| HU-014a | Informe consolidado de cumplimiento | [#58](https://github.com/Raze144hz/Proyecto-FIDSW/issues/58) | #14 (dividida), #5 | Media | Sin iniciar. Falta definir qué estado es "publicado" en la regla de negocio. | 5 |
| HU-014b | Drill-down del informe gerencial | [#59](https://github.com/Raze144hz/Proyecto-FIDSW/issues/59) | #14 (dividida) | Baja | Sin iniciar. | Cierre |
| HU-015 | Notificaciones por correo electrónico y alertas | [#15](https://github.com/Raze144hz/Proyecto-FIDSW/issues/15) | — | Baja | Sin iniciar. | Cierre |
| HU-016 | Búsqueda avanzada y filtrado de evidencias | [#24](https://github.com/Raze144hz/Proyecto-FIDSW/issues/24) | #16 | Alta | Diseñada (diagramas y [casos de prueba](../Calidad/casos-de-prueba-HU016-HU017.md)). Sin programar; depende de HU-003. | 4 |
| HU-017 | Aprobación y retroalimentación de planes de acción | [#25](https://github.com/Raze144hz/Proyecto-FIDSW/issues/25) | #17 | Media | Implementada en el PR #40, con 12 pruebas JUnit que pasan en Windows. Queda *In review* hasta que otro integrante revise el PR #40. | 2 (Entrega 2) |
| HU-018 | Recuperación y restablecimiento de contraseña | [#26](https://github.com/Raze144hz/Proyecto-FIDSW/issues/26) | — | Alta | Sin iniciar. | 3 |
| HU-019 | Gestión de versiones y actualización de evidencias | [#27](https://github.com/Raze144hz/Proyecto-FIDSW/issues/27) | — | Media | Sin iniciar. | 5 |
| HU-020 | Cierre y evaluación final de planes de acción | [#28](https://github.com/Raze144hz/Proyecto-FIDSW/issues/28) | — | Alta | Diseñada el 18 oct (cierre del director con evaluación, estado FINALIZADO, RN-05). Sin programar: el botón **Finalizar** del prototipo no pide evaluación. | 5 |

Issues que no son HU: [#37](https://github.com/Raze144hz/Proyecto-FIDSW/issues/37) (reporte gerencial del Sprint 2), [#38](https://github.com/Raze144hz/Proyecto-FIDSW/issues/38) (acta de pruebas del Sprint 2), [#39](https://github.com/Raze144hz/Proyecto-FIDSW/issues/39) (evidencias de la Entrega 2) y [#41](https://github.com/Raze144hz/Proyecto-FIDSW/issues/41) (limpieza del repositorio) están cerrados y terminados. [#42](https://github.com/Raze144hz/Proyecto-FIDSW/issues/42) (rama `develop` desactualizada) se cerró el 18 de octubre: el PR #53 puso `develop` al día y los PR van a `develop`.

## Cambios hechos en GitHub el 18 de octubre

- Cerrados como *duplicate*, con un comentario que apunta al canónico: #2 → #19, #3 → #18, #5 → #14, #11 → #22, #21 → #22, #13 → #23, #16 → #24 y #17 → #25.
- Reabiertos, como pide el [Planning del Sprint 3](Acta-Planning-Sprint3.md): #1, #6, #8, #10 y #19, con un comentario sobre lo que falta en cada uno.
- Cerrado como terminado: #42.
- Corregido en #28 (HU-020) el nombre del estado final: "Finalizado" en lugar de "Cerrado".
- Reabiertos porque se habían cerrado sin implementar: #4, #7, #9, #15 y #18. También #25 (HU-017), que sigue *In review* hasta la revisión del PR #40.
- HU-012 y HU-014 se dividieron como en la propuesta: #12 → #56 (HU-012a) y #57 (HU-012b); #14 → #58 (HU-014a) y #59 (HU-014b). #12 y #14 quedaron cerrados con un comentario que apunta a los nuevos.
- La prioridad de cada issue se alineó con la de la propuesta; en cada issue cambiado quedó una nota con la prioridad anterior.

## Pendiente para el Scrum Master y el equipo

- Crear el milestone del Sprint 3 y mover ahí #1, #6, #8, #10, #19 y #26; después, los milestones de los sprints 4 y 5. Este documento no verifica el tablero de GitHub Projects ni los milestones; las capturas de `Graficas/entrega-1` son de la Entrega 1.
- Estimar en story points HU-012a, HU-012b, HU-014a y HU-014b en su planning.
- Resolver las diferencias entre los criterios y el prototipo anotadas en la tabla (HU-001, HU-002, HU-003, HU-006, HU-013 y HU-014a) antes de cerrar esas HU.
- Cerrar #25 (HU-017) cuando la revisión del PR #40 quede registrada.
