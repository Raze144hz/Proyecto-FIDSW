# Retrospectiva - Sprint 5

**Fecha:** 18/11/2026
**Scrum Master:** Miguel Ángel Gómez López
**Asistentes:** David Galindo, Miguel Gómez, Juan Martínez, David Álvarez

## 1. ¿Qué salió bien?
* **Entrega de funcionalidades clave:** Se completaron e integraron en `develop` las historias HU-010 (gestión de áreas, PR #84) y HU-015 (notificaciones por correo y preferencias, PR #85).
* **Buen diseño y seguridad en HU-015:** Se aplicó inversión de dependencias (`EnvioCorreo` / `CorreoSmtp`), hilos *daemon* para envío asíncrono post-*commit* y lectura de credenciales SMTP únicamente por variables de entorno (`SGPI_SMTP_*`), sin exponer secretos en el repositorio.
* **Estabilidad del prototipo:** La suite alcanzó **68 pruebas automatizadas con 0 fallos (`BUILD SUCCESS`)** y arranque verificado en JavaFX + H2.
* **Inicio de gestión en Azure DevOps:** Se configuró el proyecto `Proyecto-SGPI` y el primer Test Plan con casos de prueba base.

## 2. ¿Qué no salió tan bien? (Oportunidades de mejora)
* **Divergencia entre `main` y `develop`:** Se subieron Dailies directamente a `main` mientras los PRs #84 y #85 quedaron únicamente en `develop`, generando riesgo de presentar una rama incompleta.
* **Detalles de endurecimiento en correo y pruebas:** Se detectaron pendientes técnicos en la revisión: falta de `writetimeout` y `starttls.required` en `CorreoSmtp`, pruebas que pueden intentar enviar correos reales si `SGPI_SMTP_HOST` está definido en el entorno, y que `iniciarRevisionDeCorreos` se detiene si no hay SMTP configurado.
* **Brecha documental para la sustentación:** Faltan diagramas clave (casos de uso, estados del plan, secuencia HU-015/HU-005 y clases *as-built*), el modelo relacional no incluye `CORREO` ni `frecuencia_correo`, y las fechas de los Dailies 15–18 quedaron desfasadas.

## 3. Plan de Acción para el Sprint de Cierre (19–20 nov)
* **Proteger el alcance:** Programar únicamente **HU-005** (notificaciones de vencimiento, 3 SP) reutilizando `notificar` de HU-015, y documentar el resto de historias pendientes en el backlog priorizado del producto.
* **Sincronizar ramas y endurecer código:** Traer `main` a `develop`, aplicar los ajustes de SMTP/aislamiento de pruebas, integrar HU-005 y hacer el PR definitivo `develop -> main`, validando en clon limpio en 2 PCs.
* **Cerrar documentación y ensayar:** Completar las actas Scrum, los diagramas faltantes y el tablero de Azure DevOps, y realizar un ensayo cronometrado de la presentación y la demo.
