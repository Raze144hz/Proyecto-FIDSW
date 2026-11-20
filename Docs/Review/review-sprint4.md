# Sprint Review - Sprint 4

**Fecha:** 04/11/2026
**Scrum Master:** Miguel Ángel Gómez López
**Asistentes:** David Galindo, Miguel Gómez, Juan Martínez, David Álvarez
**Producto:** SGPI (Sistema de Gestión de Procesos Institucionales)

## 1. Objetivo del Sprint
Habilitar la ejecución operativa de los planes de acción aprobados mediante el cargue de evidencias documentales y el seguimiento porcentual del avance de sus actividades.

## 2. Incremento Presentado (Demostración)
Se demostró el flujo completo desde un plan aprobado hasta el cierre de actividades:
* **HU-003 (Carga de evidencias documentales):** Adjuntar soportes (como archivos PDF) vinculados a cada actividad dentro de la interfaz `VistaActividades`.
* **HU-004 (Seguimiento de avance de actividades):** Actualización de progreso porcentual (0 % a 100 %) y aplicación de la regla de negocio que exige evidencia cargada para finalizar una actividad.

## 3. Aseguramiento de Calidad
* **EvidenciasTest:** 9 pruebas automatizadas aprobadas.
* **ActividadesPlanTest:** 10 pruebas automatizadas aprobadas.
* Se documentó el resultado completo en el acta de pruebas del Sprint 4 (`Docs/Calidad/actas/`), sin fallos bloqueantes en la compilación con `mvnw clean test`.

## 4. Pendientes y Próximos Pasos
* Se completó el 100 % del alcance funcional comprometido para el Sprint 4.
* **Siguiente Sprint (Sprint 5):** Implementar la gestión de la estructura organizacional por áreas (HU-010) y el sistema de notificaciones por correo electrónico con preferencias de usuario (HU-015).
