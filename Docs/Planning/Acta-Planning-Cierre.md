# Acta de Sprint Planning - Sprint de Cierre (Entrega Final)

**Fecha:** 19/11/2026
**Scrum Master:** Miguel Ángel Gómez López
**Asistentes:** David Galindo, Miguel Gómez, Juan Martínez, David Álvarez
**Duración del Sprint:** 19 de noviembre al 20 de noviembre de 2026 (Entrega final: viernes 20 nov)

## 1. Objetivo del Sprint
Cerrar el alcance comprometido para la sustentación final implementando la historia de prioridad Alta restante (HU-005), sincronizando `develop` con `main`, completando los diagramas y documentación Scrum faltantes, y dejando verificada la demostración en vivo.

## 2. Decisiones de Alcance
* Se incluye **únicamente HU-005** (Notificaciones de vencimiento de actividades, 3 SP, prioridad Alta), aprovechando la infraestructura de correo ya implementada en HU-015.
* Las demás historias pendientes (HU-018, HU-020, HU-016, HU-012a y prioridades Media/Baja) se documentan en el **Backlog priorizado del producto** y no se intentan en el cierre para proteger la estabilidad del prototipo.

## 3. Distribución de Tareas
* **David Álvarez Rodríguez:** Implementar HU-005 (`notificarVencimientos` a 5 días y 1 día) + pruebas `VencimientosActividadesTest`; completar tablero de Azure DevOps; actualizar modelo relacional (`CORREO` y `frecuencia_correo`), `README.md`, cronograma y reporte gerencial final; preparar datos de la demo.
* **David Andrés Galindo:** Sincronizar dailies de `main` a `develop`; endurecer SMTP (`writetimeout`, `starttls.required`, `ORDER BY`); aislar pruebas de variables `SGPI_SMTP_*`; actualizar `README-PROTOTIPO.md`; revisar PR de HU-005; ejecutar PR `develop -> main`; validar clon limpio en 2 equipos; generar diagrama de clases *as-built*.
* **Juan Sebastián Martínez:** Confirmar destinatarios de HU-005 (issue #20); crear casos de prueba HU-005/HU-015 y ejecutarlos en interfaz; elaborar diagramas de casos de uso y secuencia HU-015/HU-005; cerrar acta de pruebas final, trazabilidad y guía de usuario.
* **Miguel Ángel Gómez:** Regularizar fechas de dailies 15–18 y subir dailies 19–21; completar actas de Planning (4, 5 y Cierre), Review (3, 4 y 5) y Retrospectivas (4 y 5); elaborar diagrama de estados del plan (`EstadoPlan`); armar diapositivas y coordinar ensayo cronometrado.

## 4. Definición de Listo (DoD de Cierre — Jueves 19, 8:00 p. m.)
* `main` igualado con `develop` incluyendo HU-005, HU-010 y HU-015.
* `mvnw clean test` en verde desde un clon limpio y prototipo ejecutando en al menos 2 PCs.
* Todos los diagramas, actas Scrum, acta de pruebas final y tablero de Azure DevOps completos.
