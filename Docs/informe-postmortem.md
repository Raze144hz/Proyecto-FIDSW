# Informe postmortem — Proyecto SGPI

**Gestión y Reportes**
SGPI: Sistema de Gestión de Procesos Institucionales
Grupo Los FIS
Pontificia Universidad Javeriana, Facultad de Ingeniería

> Borrador del 18 de noviembre de 2026 (David Álvarez). Se revisa con todo el equipo en la Retrospectiva final antes de la entrega del viernes 20. Fuentes: retrospectivas y reviews, actas de pruebas, [cronograma](Planning/cronograma-desarrollo.md), [trazabilidad](Planning/trazabilidad-hu-issues.md), historial de GitHub y [reporte gerencial final](reporte-gerencial-entrega-final.md).
>
> Formato sin culpables: se analizan prácticas y decisiones, no personas.

---

## 1. Resumen

| Aspecto | Resultado |
|---|---|
| Objetivo | Prototipo funcional del SGPI con su documentación de requisitos, diseño, Scrum y pruebas |
| Alcance logrado | 8 de 22 HU implementadas con pruebas, 2 en parte; ciclo completo de un plan de acción funcionando |
| Calidad | 76 pruebas JUnit en verde; SonarQube y JaCoCo desde el PR #77; casos de prueba ejecutados en interfaz y en Azure DevOps **[completar con el resultado]** |
| Tiempo | Entrega en la fecha (20 nov), con el alcance reducido frente al cronograma |

## 2. Línea de tiempo

| Fecha | Hecho |
|---|---|
| Septiembre | Entrega 1: propuesta, requisitos (22 HU), WBS, organigrama, tablero Kanban |
| 23 sep – 7 oct | Sprint 2: el diseño técnico empieza con unos 6 días de atraso; ninguna HU planeada se programa en el sprint |
| 12 oct | HU-017 se une a `main` (PR #40) con las primeras 12 pruebas JUnit |
| 18 oct | Se corrige el supuesto de stack (Spring y web → JavaFX y H2) y se define un issue canónico por HU |
| 8 – 21 oct | Sprint 3: 0 de 6 HU del objetivo con código; 24 PR unidos, sobre todo de documentación y diagramas |
| 22 oct – 4 nov | Sprint 4: refactor del prototipo, HU-001 y HU-004 (PR #76, 37 pruebas) |
| 5 – 18 nov | Sprint 5: SonarQube y JaCoCo, HU-003, HU-010 y HU-015 (68 pruebas), HU-005 (76 pruebas) |
| 19 – 20 nov | Cierre: unión a `main`, verificación en clon limpio, pruebas en Azure DevOps, entrega |

## 3. Qué salió bien

- **Pruebas automáticas desde temprano.** Cada HU programada trae sus pruebas JUnit en base H2 en memoria, con reloj controlado cuando hay fechas. Se pasó de 12 a 76 pruebas sin fallos.
- **Trazabilidad HU ↔ issue ↔ estado real.** El 18 de octubre se eliminaron issues duplicados y cerrados sin implementar. Desde entonces el estado de cada HU sale del código y no del tablero.
- **Decisiones técnicas documentadas.** Arquitectura objetivo frente al prototipo, estados del plan, evidencias con revisión del contenido del archivo y claves fuera del código (`SGPI_DB_PASSWORD`, `SGPI_SMTP_*`).
- **Calidad medible.** SonarQube y JaCoCo (PR #77) y consultas SQL constantes (PR #78).
- **Reutilización.** HU-005 se construyó sobre las notificaciones de HU-017 y el correo de HU-015, sin tablas nuevas.

## 4. Qué salió mal

| Problema | Evidencia | Impacto |
|---|---|---|
| Atraso inicial que no se recuperó | Sprint 2 con 6 días de atraso; Sprint 3 con 0 de 6 HU del objetivo con código | 12 HU quedaron sin programar, 4 de ellas de prioridad Alta |
| Supuesto de stack equivocado | El cronograma y las actas suponían Spring, web y PostgreSQL; el prototipo es JavaFX y H2 | Documentos y casos de prueba que se tuvieron que rehacer |
| PR unidos sin revisión | PR #29 a #36 directo a `main`; PR #40 sin revisión registrada | No se cumplía la DoD; las HU quedaban en *In review* |
| Ramas desincronizadas | Issue #42; en noviembre `main` tenía dailies que `develop` no tenía, y `develop` tenía HU-010 y HU-015 que `main` no tenía | Riesgo de presentar una versión incompleta |
| Registro Scrum tardío | Dailies subidas días después (las 7 a 14 el 19 y 20 de oct; la 18 con fecha 16/10 subida el 17/11); faltaban actas de Planning, Review y Retro de los Sprints 4 y 5 | Poca evidencia del seguimiento real |
| Documentación atrasada frente al código | El README decía 37 pruebas cuando había 68; el modelo relacional no tenía la tabla de correos | Riesgo de inconsistencias en la sustentación |
| Concentración de trabajo | Buena parte del código de noviembre la hizo una sola persona | Riesgo si esa persona no está disponible |

## 5. Causas raíz

1. **Planeación sin validar la capacidad ni el stack.** El cronograma se armó antes de tener el entorno funcionando, así que el atraso del Sprint 2 se arrastró a todos los sprints.
2. **Sin control automático en los PR.** No había un pipeline que corriera las pruebas ni una regla de revisión obligatoria, así que unir sin revisión era lo más fácil.
3. **El registro Scrum no estaba atado al trabajo diario.** Las dailies y las actas se escribían en lote al final, no el mismo día.
4. **La documentación se actualizaba aparte del código.** El README y los diagramas no estaban en la checklist del PR.

## 6. Lecciones aprendidas

| Lección | Cómo aplicarla en un próximo proyecto |
|---|---|
| Validar el stack con un "esqueleto" que compile y corra antes de planear los sprints | Sprint 0 con el entorno, el pipeline y una HU mínima de punta a punta |
| Lo que no está automatizado no se cumple | Pipeline con `mvnw clean test` en cada PR y protección de rama con 1 revisión obligatoria |
| Planear menos HU por sprint y terminarlas | Comprometer solo las HU que caben según la velocidad real del sprint anterior |
| La documentación es parte de la HU | Agregar a la DoD: README, trazabilidad, CHANGELOG y diagramas afectados actualizados en el mismo PR |
| Registrar el Scrum el mismo día | Daily de 15 minutos con el registro hecho al terminar; Review y Retro el último día del sprint |
| Repartir el código | Al menos una HU con código por integrante y por sprint, con revisión cruzada |

## 7. Métricas finales

| Métrica | Valor |
|---|---|
| HU implementadas / planeadas | 8 / 22 (+ 2 parciales) |
| Pruebas JUnit | 76, 0 fallos |
| Pull requests unidos a `develop` | 39 (hasta el #85) |
| Defectos registrados en actas | 12 (8 del Sprint 2 y 4 del Sprint 4) + **[completar]** bugs de Azure DevOps |
| Casos de prueba en Azure DevOps | **[completar]** diseñados / ejecutados / aprobados / fallidos |

## 8. Acciones pendientes después de la entrega

- Terminar HU-020, HU-018, HU-016 y HU-012a (prioridad Alta).
- Reintentos de correo con espera creciente y STARTTLS obligatorio.
- Separar `DataService` en servicios y repositorios y migrar a PostgreSQL 16.
- Pipeline de CI y protección de ramas.
