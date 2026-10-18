# Acta de Sprint Planning - Sprint 3

> **Borrador para completar.** Las HU, los issues, los responsables y las tareas salen del [cronograma de desarrollo](cronograma-desarrollo.md), del [reporte gerencial del Sprint 2](../reporte-gerencial-entrega-2.md) (sección 11) y del [acta de pruebas del Sprint 2](../Calidad/actas/acta-pruebas-sprint-2.md). Lo marcado con **[completar]** es lo que se decide en la reunión y lo registra el Scrum Master.

**Fecha:** [completar]
**Scrum Master:** Miguel Ángel Gómez López
**Asistentes:** [completar]
**Duración del Sprint:** 8 al 21 de octubre de 2026

| Fecha | Hito |
|---|---|
| 20 oct | Congelamiento de código, pruebas y acta de pruebas |
| 21 oct | Sprint Review y Retrospectiva |

## Objetivo del Sprint

Acceso, roles y registro de planes: completar el inicio de sesión, el control de roles, la recuperación de contraseña, la administración de roles, la estructura organizacional y el registro de planes de acción. En paralelo, cerrar las tareas de la Entrega 2.

[completar: confirmar o ajustar el objetivo en la reunión]

## Historias de usuario del Sprint

| HU | Historia | Prioridad | Esfuerzo | Responsable | Issue | Punto de partida | Estimación |
|---|---|---|---|---|---|---|---|
| HU-001 | Login en el sistema | Alta | Bajo | David Andrés Galindo | [#1](https://github.com/Raze144hz/Proyecto-FIDSW/issues/1) | Avance en el prototipo: inicio de sesión con contraseñas cifradas | [completar] |
| HU-006 | Autenticación y control de roles | Media | Medio | David Andrés Galindo | [#6](https://github.com/Raze144hz/Proyecto-FIDSW/issues/6) | Avance en el prototipo: roles con permisos distintos | [completar] |
| HU-018 | Recuperación y restablecimiento de contraseña | Alta | Bajo | David Álvarez | [#26](https://github.com/Raze144hz/Proyecto-FIDSW/issues/26) | Sin iniciar | [completar] |
| HU-008 | Administración y asignación de roles de usuario | Media | Medio | [completar] | [#8](https://github.com/Raze144hz/Proyecto-FIDSW/issues/8) | Sin iniciar | [completar] |
| HU-010 | Gestión de la estructura organizacional | Media | Medio | [completar] | [#10](https://github.com/Raze144hz/Proyecto-FIDSW/issues/10) | Sin iniciar | [completar] |
| HU-002 | Registro de un nuevo plan de acción | Media | Medio | Juan Sebastián Martínez | [#19](https://github.com/Raze144hz/Proyecto-FIDSW/issues/19) | Avance en el prototipo: crear, editar y consultar planes | [completar] |

La prioridad y el esfuerzo son los de la [propuesta](../propuesta-y-requisitos.md). Los issues #1, #6, #8, #10 y #19 se cerraron en la Entrega 1 sin estar programados, así que hay que reabrirlos y moverlos al milestone del Sprint 3.

## Tareas de la Entrega 2 ligadas a las HU

| Tarea | HU | Issue o PR | Responsable |
|---|---|---|---|
| Implementar la aprobación y retroalimentación de planes, con pruebas JUnit | HU-017 | [#25](https://github.com/Raze144hz/Proyecto-FIDSW/issues/25), PR #40 | David Álvarez |
| Reporte gerencial del Sprint 2 con el estado de cada HU | HU del Sprint 2 | [#37](https://github.com/Raze144hz/Proyecto-FIDSW/issues/37) | David Álvarez |
| Acta de pruebas del Sprint 2 | HU-016, HU-017 | [#38](https://github.com/Raze144hz/Proyecto-FIDSW/issues/38) | David Álvarez |
| Evidencias del prototipo | HU-017 | [#39](https://github.com/Raze144hz/Proyecto-FIDSW/issues/39) | David Álvarez |
| Diagrama de clases y modelo relacional, con su fuente editable | HU-016, HU-017 | [#24](https://github.com/Raze144hz/Proyecto-FIDSW/issues/24), [#25](https://github.com/Raze144hz/Proyecto-FIDSW/issues/25) | David Andrés Galindo |
| Diagrama de secuencia y diagrama de procesos | HU-017 | [#25](https://github.com/Raze144hz/Proyecto-FIDSW/issues/25) | Todo el equipo (Galindo revisa) |
| Revisión técnica del SDD frente al código e identificación de patrones | HU-017 | — | David Andrés Galindo |
| Revisar el código del PR #40 | HU-017 | [#25](https://github.com/Raze144hz/Proyecto-FIDSW/issues/25) | David Andrés Galindo |
| Decidir el estado de un plan devuelto (EN REVISION o BORRADOR) y unificar HU-017 y CU-01 | HU-017, HU-002 | — | Juan Sebastián Martínez |
| Trazabilidad entre requisitos y diseño; revisión de los casos de prueba | HU-016, HU-017 | [#24](https://github.com/Raze144hz/Proyecto-FIDSW/issues/24), [#25](https://github.com/Raze144hz/Proyecto-FIDSW/issues/25) | Juan Sebastián Martínez |
| Escribir los casos de prueba de las HU con avance en el prototipo | HU-001, HU-002, HU-006 | [#1](https://github.com/Raze144hz/Proyecto-FIDSW/issues/1), [#19](https://github.com/Raze144hz/Proyecto-FIDSW/issues/19), [#6](https://github.com/Raze144hz/Proyecto-FIDSW/issues/6) | Responsable de cada HU |
| Arrancar el prototipo en Windows (CP-PRO-08, RNF-08) | HU-017 | [#25](https://github.com/Raze144hz/Proyecto-FIDSW/issues/25) | David Álvarez |
| Reabrir los issues de las HU del sprint, asignarlos y vincularlos al milestone; mantener el tablero al día | HU-001, HU-002, HU-006, HU-008, HU-010, HU-018 | #1, #19, #6, #8, #10, #26 | Miguel Ángel Gómez |
| Sprint Review y Retrospectiva | Todas las HU del sprint | — | Miguel Ángel Gómez |

## Acuerdos

[completar: qué se decidió en la reunión, por ejemplo a qué rama van los PR (issue #42) y quién toma HU-008 y HU-010]

## Compromisos

[completar]

## Cambios durante el sprint

- **16 de octubre:** revisión de los diagramas con el profesor. Las correcciones (clases, componentes y procesos, que incluyen el seguimiento del plan de HU-004) quedaron en el PR #52 y en [`Docs/Arquitectura/arquitectura.md`](../Arquitectura/arquitectura.md).
