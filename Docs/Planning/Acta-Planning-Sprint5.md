# Acta de Sprint Planning - Sprint 5

**Fecha:** 05/11/2026
**Scrum Master:** Miguel Ángel Gómez López
**Asistentes:** David Galindo, Miguel Gómez, Juan Martínez, David Álvarez
**Duración del Sprint:** 5 de noviembre al 18 de noviembre de 2026

## 1. Objetivo del Sprint
Ampliar las capacidades administrativas y de comunicación del prototipo SGPI mediante la gestión de la estructura organizacional por áreas (HU-010) y el sistema de notificaciones por correo electrónico con preferencias de envío (HU-015).

## 2. Alcance Seleccionado (Backlog del Sprint)
* **HU-010:** Gestión de la estructura organizacional (administración de áreas y prevención de borrado con planes asociados).
* **HU-015:** Notificaciones por correo electrónico y alertas (cola de correos, envío SMTP asíncrono, preferencias inmediata / resumen diario 7:00 / desactivada).

## 3. Asignación de Responsabilidades
* **Juan Sebastián Martínez:** Implementación de HU-010 (`VistaAreas`, PR #84) y HU-015 (`VistaPreferencias`, `EnvioCorreo`, `CorreoSmtp`, PR #85), junto con sus pruebas automatizadas.
* **David Andrés Galindo:** Revisión de arquitectura y seguridad en el manejo de credenciales SMTP por variables de entorno, revisión de PRs y soporte técnico.
* **David Álvarez Rodríguez:** Configuración inicial del proyecto en Azure DevOps (`Proyecto-SGPI`), creación del Test Plan y seguimiento de métricas.
* **Miguel Ángel Gómez:** Facilitación de ceremonias Scrum, auditoría de trazabilidad y preparación del cierre del proyecto.

## 4. Compromisos y Criterios de Cierre
* Las credenciales SMTP no deben almacenarse en el repositorio; se leerán únicamente de variables de entorno (`SGPI_SMTP_*`).
* Las pruebas de correo deben ejecutarse con reloj controlado y doble de prueba sin depender de un servidor SMTP externo.
