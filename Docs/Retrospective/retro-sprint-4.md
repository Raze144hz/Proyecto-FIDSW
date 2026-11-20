# Retrospectiva - Sprint 4

**Fecha:** 04/11/2026
**Scrum Master:** Miguel Ángel Gómez López
**Asistentes:** David Galindo, Miguel Gómez, Juan Martínez, David Álvarez

## 1. ¿Qué salió bien?
* **Cumplimiento total del alcance:** Se implementaron y validaron al 100 % las historias comprometidas (HU-003 carga de evidencias y HU-004 seguimiento de avance de actividades) dentro de las fechas del Sprint.
* **Cobertura de pruebas automatizadas:** Se agregaron 19 pruebas nuevas (`EvidenciasTest` con 9 pruebas y `ActividadesPlanTest` con 10 pruebas) pasando sin fallos con `mvnw clean test`.
* **Control de calidad documentado:** Se consolidó oportunamente el acta de pruebas del Sprint 4 y se mantuvo la disciplina de integración mediante Pull Requests hacia `develop`.

## 2. ¿Qué no salió tan bien? (Oportunidades de mejora)
* **Rezago en documentación Scrum y general:** Por priorizar el desarrollo funcional, quedaron pendientes de subir al repositorio las actas de Planning/Review del ciclo y se empezó a desactualizar el conteo de pruebas en `README.md` y `README-PROTOTIPO.md`.
* **Concentración de lógica en `DataService`:** La clase principal de datos siguió creciendo al absorber nuevas validaciones, alejándose del diseño desacoplado planteado en el diagrama de clases objetivo.
* **Trazabilidad de ceremonias:** Faltó mantener al día el registro continuo de Dailies y la sincronización con el tablero de seguimiento.

## 3. Plan de Acción para el Sprint 5
* **Desacoplar integraciones externas:** Para el módulo de correo electrónico (HU-015), introducir una interfaz dedicada (`EnvioCorreo`) que permita aislar la lógica de negocio del proveedor SMTP y facilitar las pruebas unitarias.
* **Mantener el estándar de calidad:** Exigir pruebas automatizadas en verde antes de aprobar los PRs de HU-010 y HU-015.
* **Regularizar la documentación:** Reservar tiempo al cierre del Sprint 5 para actualizar actas, diagramas y README de cara a la entrega final.
