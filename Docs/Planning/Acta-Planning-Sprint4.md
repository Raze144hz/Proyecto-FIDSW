# Acta de Sprint Planning - Sprint 4

**Fecha:** 22/10/2026
**Scrum Master:** Miguel Ángel Gómez López
**Asistentes:** David Galindo, Miguel Gómez, Juan Martínez, David Álvarez
**Duración del Sprint:** 22 de octubre al 4 de noviembre de 2026

## 1. Objetivo del Sprint
Implementar el seguimiento operativo de las actividades dentro de los planes de acción aprobados, habilitando el cargue de evidencias documentales (HU-003) y el control de porcentaje de avance (HU-004), manteniendo la cobertura de pruebas automatizadas en la capa de datos.

## 2. Alcance Seleccionado (Backlog del Sprint)
* **HU-003:** Carga de evidencias documentales asociadas a actividades de un plan.
* **HU-004:** Seguimiento de avance de actividades (progreso porcentual y validación de cierre con evidencia).

## 3. Asignación de Responsabilidades
* **Juan Sebastián Martínez:** Implementación de HU-003 y HU-004 (`VistaActividades`, lógica en `DataService` y validaciones de negocio) y actualización de casos de prueba.
* **David Andrés Galindo:** Revisión técnica de Pull Requests, integración en `develop` y validación de integridad de la base de datos H2.
* **David Álvarez Rodríguez:** Ejecución y documentación del acta de pruebas del Sprint 4 y seguimiento del cronograma.
* **Miguel Ángel Gómez:** Gestión del tablero Kanban, seguimiento de Issues/PRs, registro de ceremonias y control del cumplimiento del Definition of Done (DoD).

## 4. Compromisos y Criterios de Cierre
* Toda funcionalidad debe entrar por rama `feature/*` hacia `develop` mediante Pull Request revisado.
* El conjunto de pruebas con `mvnw clean test` debe pasar al 100 % antes del cierre del Sprint el 4 de noviembre.
