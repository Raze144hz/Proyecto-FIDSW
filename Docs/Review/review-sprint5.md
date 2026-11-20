# Sprint Review - Sprint 5

**Fecha:** 18/11/2026
**Scrum Master:** Miguel Ángel Gómez López
**Asistentes:** David Galindo, Miguel Gómez, Juan Martínez, David Álvarez
**Producto:** SGPI (Sistema de Gestión de Procesos Institucionales)

## 1. Objetivo del Sprint
Incorporar la administración de áreas organizacionales y el módulo de notificaciones por correo electrónico con cola asíncrona y preferencias configurables por usuario.

## 2. Incremento Presentado (Demostración)
Se presentaron los incrementos integrados en la rama `develop` (PR #84 y PR #85):
* **HU-010 (Gestión de la estructura organizacional — PR #84):** Administración de áreas en `VistaAreas`, impidiendo eliminar áreas con planes asociados y sugiriendo su desactivación.
* **HU-015 (Notificaciones por correo electrónico y alertas — PR #85):** Cola de correos en base de datos, envío asíncrono mediante `EnvioCorreo`/`CorreoSmtp` con credenciales leídas únicamente de variables de entorno (`SGPI_SMTP_*`), y pantalla `VistaPreferencias` (frecuencia inmediata, resumen diario a las 7:00 a. m. o desactivada).

## 3. Aseguramiento de Calidad
* **EstructuraOrganizacionalTest:** 10 pruebas automatizadas aprobadas.
* **NotificacionesCorreoTest:** 8 pruebas automatizadas aprobadas (usando reloj controlado y doble de prueba sin dependencia de servidor SMTP externo).
* **Consolidado en `develop`:** **68 pruebas ejecutadas, 0 fallos (`BUILD SUCCESS`)** con `mvnw clean test`, y arranque verificado del prototipo en JavaFX.

## 4. Hallazgos, Pendientes y Próximos Pasos
* **Sincronización de ramas:** `develop` contiene HU-010 y HU-015, mientras que `main` tiene los dailies 15–18; se programó su unificación antes de la entrega final.
* **Endurecimiento técnico:** Se acordó ajustar timeouts/STARTTLS en `CorreoSmtp`, aislar las pruebas de `SGPI_SMTP_*` y actualizar documentación (`README`, modelo relacional y diagramas).
* **Sprint de Cierre (19–20 nov):** Implementar **HU-005** (notificaciones de vencimiento a 5 días y 1 día) y dejar las demás historias pendientes en el backlog priorizado del producto.
