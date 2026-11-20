# Reporte Gerencial — Entrega final

**Gestión y Reportes**
SGPI: Sistema de Gestión de Procesos Institucionales
Grupo Los FIS
Pontificia Universidad Javeriana, Facultad de Ingeniería

> Versión del 19 de noviembre de 2026 para la entrega del viernes 20. Fuentes: [cronograma](Planning/cronograma-desarrollo.md), [trazabilidad](Planning/trazabilidad-hu-issues.md), [registro de cambios](../CHANGELOG.md), historial de pull requests de GitHub, resultado de `mvnw clean test` y [reportes anteriores](reporte-gerencial-entrega-2.md).

---

## 1. Identificación

| Campo | Valor |
|---|---|
| Proyecto | SGPI: Sistema de Gestión de Procesos Institucionales |
| Entrega | Final, viernes 20 de noviembre de 2026 |
| Periodo cubierto | Sprint 1 a Sprint 5 y sprint de Cierre (septiembre a 20 de noviembre de 2026) |
| Equipo | David Andrés Galindo Rojas (líder técnico), Miguel Ángel Gómez López (Scrum Master), Juan Sebastián Martínez (requisitos), David Álvarez Rodríguez (gestión y reportes) |
| Entregables | Prototipo final, reporte de pruebas, repositorio de código actualizado, informe postmortem y este reporte gerencial |

## 2. Resumen ejecutivo

- El prototipo de escritorio (Java 17, JavaFX, H2, BCrypt, JUnit 5) cubre el ciclo principal de un plan de acción: registro con actividades, envío a revisión, aprobación o devolución por el director de área, ejecución con evidencias, avance por actividades, avisos de vencimiento y notificaciones por correo.
- **8 de las 22 HU están implementadas con pruebas** (HU-001, HU-002, HU-003, HU-004, HU-005, HU-010, HU-015 y HU-017) y 2 están implementadas en parte (HU-006 y HU-013).
- **76 pruebas automáticas, todas en verde** (18 de noviembre). Al cierre de la Entrega 2 eran 12.
- Las HU que faltan, entre ellas HU-018, HU-020, HU-016 y HU-012a (prioridad Alta), quedan en el backlog priorizado. La razón principal es el atraso acumulado desde el Sprint 2 (ver §7 y el [informe postmortem](informe-postmortem.md)).

## 3. Estado de las historias de usuario

| HU | Historia | Prioridad | Estado al 18 nov | Pruebas JUnit |
|---|---|---|---|---|
| HU-001 | Login en el sistema | Alta | Implementada: BCrypt y bloqueo de 15 min tras 3 intentos | 8 |
| HU-002 | Registro de un nuevo plan de acción | Media | Implementada (el formulario pide objetivo, área y fechas; el issue pide nombre, responsable y periodo) | 7 |
| HU-003 | Carga de evidencias documentales | Media | Implementada: PDF, DOCX, JPG y PNG de hasta 10 MB; RN-03 | 9 |
| HU-004 | Seguimiento de avance de actividades | Media | Implementada: avance = finalizadas / total (RN-01) | 10 |
| HU-005 | Notificaciones de vencimiento de actividades | Alta | Implementada el 18 nov: aviso a 5 días y a 1 día al responsable | 8 |
| HU-006 | Autenticación y control de roles | Media | Parcial: 4 roles con permisos; los nombres no coinciden con el issue | (en las demás) |
| HU-010 | Gestión de la estructura organizacional | Media | Implementada: áreas activas o inactivas | 10 |
| HU-013 | Historial de auditoría | Media | Parcial: falta guardar el valor anterior y nuevo de cada campo | (en las demás) |
| HU-015 | Notificaciones por correo y alertas | Baja | Implementada: SMTP con frecuencia inmediata, diaria o desactivada | 8 |
| HU-017 | Aprobación y retroalimentación de planes | Media | Implementada | 12 |
| HU-007, HU-008, HU-009, HU-011, HU-012a, HU-012b, HU-014a, HU-014b, HU-016, HU-018, HU-019, HU-020 | — | — | Sin programar; en el backlog | — |

Las 4 pruebas restantes (`SeguridadBaseDatosTest`) cubren la clave de la base de datos.

## 4. Indicadores

| Indicador | Resultado | Observación |
|---|---|---|
| HU implementadas con pruebas | 8 de 22 (36 %) | 2 más en parte |
| HU de prioridad Alta implementadas | 2 de 6 (HU-001 y HU-005) | Pendientes: HU-012a, HU-016, HU-018 y HU-020 |
| Pruebas automáticas | 76, 0 fallos | 12 (Entrega 2) → 37 (3 nov) → 68 (10 nov) → 76 (18 nov) |
| Pull requests unidos a `develop` | 39 | 1 en septiembre, 31 en octubre, 8 en noviembre (hasta el #85) |
| Casos de prueba en Azure DevOps | 12 diseñados y 12 ejecutados (CP-001 a CP-012) | 9 pasaron y 3 fallaron (75 %); ver el [reporte de pruebas](Calidad/reporte-pruebas-entrega-final.md) |
| Defectos registrados en actas | 8 (Sprint 2) + 4 (Sprint 4) + 3 bugs en Azure DevOps (#272, #273 y #274) | Los 3 bugs quedan abiertos en el backlog |
| Dailies registradas | Día 1 a 18 al 19 nov | Varias se subieron días después (ver postmortem) |
| Análisis estático | SonarQube corregido y JaCoCo agregado (PR #77) | Evidencias en `Docs/Calidad/sonarqube` |

## 5. Trabajo de la entrega final (Sprint 5 y Cierre)

| Fecha | Trabajo | Responsable |
|---|---|---|
| 6 nov | Refactor del prototipo en paquetes `data`, `model` y `ui`; HU-001 (bloqueo) y HU-004 (PR #76) | Juan Sebastián Martínez |
| 9–10 nov | Correcciones de SonarQube y cobertura con JaCoCo (PR #77); consultas SQL constantes (PR #78); pruebas independientes de la clave de la base (PR #80) | Juan Sebastián Martínez, David Álvarez |
| 10 nov | HU-003 (PR #81), HU-010 (PR #84) y HU-015 (PR #85) | Juan Sebastián Martínez |
| 18 nov | HU-005, modelo relacional con la tabla de correos, README, trazabilidad y cronograma | David Álvarez |
| 18–19 nov | Diagramas en draw.io (PR #87 y #89), organización del repositorio, unión `develop` → `main` (PR #88 y #90), verificación en un clon limpio (76 de 76 pruebas) y ejecución de los 12 casos en Azure DevOps | David Álvarez; revisión y aprobación de los PR: David Galindo |

## 6. Riesgos y limitaciones conocidas al cierre

| Riesgo o limitación | Impacto | Tratamiento |
|---|---|---|
| Los reintentos de correo se agotan en unos 15 minutos si el servidor SMTP está caído | Ese correo no se vuelve a intentar; la notificación interna sí queda | En el backlog: reintento con espera creciente |
| STARTTLS no es obligatorio en el envío SMTP | Un intermediario podría forzar el envío sin cifrar | Pendiente: STARTTLS está activo por defecto, pero se puede desactivar con `SGPI_SMTP_TLS=false`; en el backlog: exigirlo siempre |
| El diagrama de clases es el diseño objetivo, no el código (`DataService` concentra la lógica) | Puede confundir a quien compare diagrama y código | Diagrama *as-built* en la entrega final |
| El prototipo usa H2 local; la arquitectura objetivo usa PostgreSQL 16 | La migración no está probada | DDL objetivo en `Database/modelo-objetivo-postgresql.sql` |
| Cuentas de prueba sembradas con contraseñas conocidas | Aceptable en un prototipo local; no lo es en un despliegue real | Documentado en el README |

## 7. Problemas de gestión y lecciones

El detalle está en el [informe postmortem](informe-postmortem.md). En resumen:
- El Sprint 2 empezó con 6 días de atraso, y en el Sprint 3 ninguna de las 6 HU del objetivo tuvo código. El trabajo se recuperó en noviembre.
- Varios PR se unieron sin revisión de otro integrante, y `main` y `develop` divergieron más de una vez.
- La documentación (README, conteo de pruebas, modelo relacional) se quedó atrás del código hasta el cierre.

## 8. Recomendaciones para continuar el proyecto

1. Terminar las HU Alta pendientes en este orden: HU-020 (cierre con evaluación), HU-018 (recuperación de contraseña, que reutiliza el correo de HU-015), HU-016 y HU-012a.
2. Separar `DataService` en servicios y repositorios, como en el diagrama de clases objetivo, antes de migrar a PostgreSQL.
3. Correr `mvnw clean test` en un pipeline (GitHub Actions o Azure Pipelines) en cada PR y exigir una revisión para unir.

## 9. Evidencias

- Pruebas automáticas: [salida de `mvnw clean test`](Calidad/evidencias/resultado-maven-entrega-final.txt) del 19 nov, 76 pruebas y 0 fallos.
- Pruebas en Azure DevOps: [reporte de pruebas](Calidad/reporte-pruebas-entrega-final.md), con la lista de bugs y las capturas de las fallas; en Azure, el Test Plan #158 con una captura por paso.
- Prototipo: las capturas de cada paso de los 12 casos están adjuntas en Azure DevOps (Test Runs 1000030 a 1000041).
