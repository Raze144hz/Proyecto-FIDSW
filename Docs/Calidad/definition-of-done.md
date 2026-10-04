# Definition of Done (DoD) — SGPI

**Responsable:** David Álvarez Rodríguez (Gestión y Reportes)
**Aplica desde:** Sprint 2, cuando empiece la programación
**Estado:** propuesta para aprobar en equipo

La DoD es el acuerdo del equipo sobre qué significa que algo está **terminado**. Una historia de usuario (HU) solo pasa a la columna **Done** del tablero cuando cumple **todos** los criterios de la sección 1. Si falta uno, la HU sigue en *In progress* o *In review*.

---

## 1. DoD de una historia de usuario

### Código
- [ ] El trabajo se hizo en una rama `feature/HU-0xx-descripcion` creada desde `develop`, como define el README.
- [ ] El proyecto compila sin errores.
- [ ] No hay código comentado sin usar, credenciales ni contraseñas en el código.

### Revisión
- [ ] Se abrió un pull request hacia `develop` que enlaza el issue de la HU (`Closes #número`).
- [ ] Al menos **un integrante distinto al autor** revisó y aprobó el pull request.
- [ ] Todos los comentarios de la revisión quedaron resueltos.

### Pruebas
- [ ] Cada regla de negocio de la HU tiene al menos una prueba unitaria (JUnit), y todas las pruebas pasan.
- [ ] Cada criterio de aceptación (Dado / Cuando / Entonces) tiene un caso de prueba en el [formato de caso de prueba](formato-caso-de-prueba.md), ejecutado con resultado **Pasa**.
- [ ] Los casos ejecutados quedaron registrados en el [acta de pruebas](formato-acta-de-pruebas.md) del sprint.

### Requisitos no funcionales que apliquen a la HU
- [ ] **Seguridad (RNF-05):** solo los roles autorizados pueden usar la funcionalidad; lo no autorizado se niega por defecto.
- [ ] **Auditoría (RNF-10):** si la HU modifica un plan o una evidencia, el cambio queda en el historial con valor anterior y nuevo.
- [ ] **Usabilidad (RNF-06):** los mensajes de error y validación están en español, junto al campo correspondiente.
- [ ] Otros RNF propios de la HU, por ejemplo contraseñas con hash y salt (RNF-03) o el tiempo de carga del Kanban (RNF-01).

### Base de datos
- [ ] Si la HU cambia tablas o columnas, el script SQL está en `/Database` y coincide con el modelo relacional.

### Integración y documentación
- [ ] El pull request se unió a `develop` y la aplicación arranca sin errores en esa rama.
- [ ] Si cambió la forma de usar el sistema, se actualizó la guía de usuario (`Docs/user guide/guide.md`).
- [ ] El issue tiene enlazados el pull request y el acta de pruebas, y solo entonces se mueve a **Done**.

---

## 2. DoD de un sprint

Un sprint está terminado cuando:

- [ ] Todas las HU marcadas como **Done** cumplen la sección 1.
- [ ] `develop` se unió a `main` con la versión del sprint y se marcó con una etiqueta (`v0.2`, `v0.3`…).
- [ ] El acta de pruebas del sprint está completa y aprobada.
- [ ] La Sprint Review quedó registrada en `Docs/Review/` y la Retrospectiva en `Docs/Retrospective/`.
- [ ] Las dailies del sprint están en `Docs/Dailys/`, cada una registrada el mismo día.
- [ ] Las HU no terminadas volvieron al backlog con su estado real.

---

## 3. Lo que NO cuenta como terminado

| Situación | Dónde va la HU |
|---|---|
| La HU está redactada con sus criterios de aceptación, pero no hay código | *Backlog* o *Ready* |
| El código funciona solo en el computador de quien lo hizo | *In progress* |
| Hay un pull request abierto sin revisión | *In review* |
| El pull request está aprobado, pero faltan las pruebas o el acta | *In review* |

> Nota: en el tablero actual hay HU en *Done* que solo están redactadas (por ejemplo HU-002, HU-003 y HU-006). Al adoptar esta DoD, deben volver a *Ready* hasta que se programen y prueben.

---

## 4. Cómo se aplica

- Quien abre el pull request revisa que la HU cumpla la sección 1 antes de pedir la revisión.
- Quien revisa el PR comprueba la sección 1 antes de aprobar.
- Si el equipo cambia un criterio, se actualiza este documento por pull request y se avisa en la siguiente daily.
