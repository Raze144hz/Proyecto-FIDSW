# Retrospectiva - Sprint 2
**Fecha:** 19/10/2026
**Scrum Master:** Miguel Ángel Gómez López
**Asistentes:** David Galindo, Miguel Gómez, Juan Martínez, David Álvarez

## 1. ¿Qué salió bien?
* **Desarrollo técnico exitoso:** El equipo logró implementar la funcionalidad más compleja (HU-017 - Aprobación y retroalimentación de planes) directamente sobre el stack tecnológico real (JavaFX y base de datos H2).
* **Aseguramiento de Calidad:** Se estructuraron y ejecutaron 12 pruebas de validación sobre la capa de datos, pasando el 100% de ellas de forma exitosa.
* **Distribución de roles:** La planeación del cierre del Sprint fue excelente, repartiendo de manera equilibrada la arquitectura, las pruebas, los reportes y la gestión metodológica.

## 2. ¿Qué no salió tan bien? (Oportunidades de mejora)
* **Inconsistencias en la Arquitectura (SDD):** Se generó confusión técnica al detectar que la documentación oficial mencionaba un entorno (Spring, Web, 7 tablas) que no correspondía con el código real (JavaFX y JDBC).
* **Inconsistencias de Requisitos:** Se detectaron contradicciones documentales entre los Casos de Uso y las Historias de Usuario (ej. el CU-01 indicaba que un plan devuelto volvía a "Borrador", mientras la HU-017 decía "En revisión").
* **Uso del Tablero Kanban:** Múltiples Historias de Usuario fueron movidas a la columna "Done" prematuramente durante la fase de diseño, lo que generó un falso progreso antes de iniciar la programación real.

## 3. Plan de Acción para el Sprint 3
* **Alineación estricta Código/Documentación:** El Líder Técnico garantizará que los diagramas de clases y el SDD reflejen únicamente la arquitectura real en JavaFX antes de avanzar con nuevas funcionalidades.
* **Metodología Ágil Realista:** Como Scrum Master, aseguraré que las Historias de Usuario solo pasen a "In Progress" cuando se empiece a escribir código, y a "Done" cuando pasen las pruebas.
* **Revisión cruzada antes de programar:** El responsable de Requisitos unificará los estados y flujos funcionales para evitar inconsistencias antes de que las tareas pasen a la columna "Ready".
