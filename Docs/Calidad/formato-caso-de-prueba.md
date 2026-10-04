# Formato de caso de prueba — SGPI

Copia esta plantilla por cada caso de prueba. Cada criterio de aceptación de una HU debe tener al menos un caso. Los casos ejecutados se registran en el [acta de pruebas](formato-acta-de-pruebas.md) del sprint.

**Identificador:** `CP-HU0xx-NN` (ejemplo: `CP-HU016-01`)

---

### CP-HU0xx-NN — [Nombre corto del caso]

| Campo | Valor |
|---|---|
| Historia de usuario | HU-0xx — [título] |
| Criterio de aceptación o requisito | [Copia el criterio Dado / Cuando / Entonces o el RNF que se prueba] |
| Tipo de prueba | Funcional · No funcional · Regresión |
| Prioridad | Alta · Media · Baja |
| Rol con el que se prueba | [Ej.: Líder de calidad] |
| Precondiciones | [Qué debe existir antes: usuarios, datos, estado del plan…] |
| Datos de prueba | [Valores concretos que se usan] |

**Pasos**

1. [Acción]
2. [Acción]
3. [Acción]

**Resultado esperado:** [Qué debe pasar si el sistema funciona bien]

**Ejecución** (se llena el día de pruebas)

| Campo | Valor |
|---|---|
| Resultado obtenido | |
| Estado | Pasa · Falla · Bloqueado · No ejecutado |
| Evidencia | [Enlace a la captura o al video] |
| Defecto asociado | [Issue #, si falló] |
| Ejecutado por | |
| Fecha | |
| Versión probada | [Rama y commit, ej.: `develop` @ `a1b2c3d`] |
