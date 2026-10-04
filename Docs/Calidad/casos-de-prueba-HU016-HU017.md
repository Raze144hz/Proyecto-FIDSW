# Casos de prueba — HU-016 y HU-017

Casos diseñados a partir de los criterios de aceptación y las reglas de negocio de las dos HU elegidas para el diseño. Siguen el [formato de caso de prueba](formato-caso-de-prueba.md). La sección **Ejecución** se llena cuando las HU estén programadas.

---

## HU-016 — Búsqueda avanzada y filtrado de evidencias

### CP-HU016-01 — Filtrar por tipo de archivo y rango de fechas

| Campo | Valor |
|---|---|
| Criterio de aceptación | Dado que accedo al módulo de repositorio documental, cuando aplico un filtro por tipo de archivo y rango de fechas, entonces el sistema despliega únicamente los documentos que cumplen los criterios. |
| Tipo / prioridad | Funcional / Media |
| Rol | Líder de calidad |
| Precondiciones | Existen evidencias PDF y DOCX cargadas dentro y fuera del rango de fechas, en áreas que el rol puede consultar. |
| Datos de prueba | Tipo: PDF · Desde: 01/01/2026 · Hasta: 30/06/2026 |

**Pasos:** 1. Entrar a Evidencias. 2. Marcar solo el tipo PDF. 3. Poner el rango de fechas. 4. Aplicar filtros.

**Resultado esperado:** solo aparecen evidencias PDF cargadas entre el 01/01/2026 y el 30/06/2026; ninguna DOCX y ninguna fuera del rango.

### CP-HU016-02 — Buscar por palabra clave en nombre o metadatos

| Campo | Valor |
|---|---|
| Criterio de aceptación | Dado que introduzco una palabra clave en el buscador general de evidencias, cuando confirmo la búsqueda, entonces el sistema muestra todos los archivos cuyos nombres o metadatos coincidan. |
| Tipo / prioridad | Funcional / Media |
| Rol | Líder de calidad |
| Precondiciones | Existe una evidencia con "comité" en el nombre y otra con "comité" solo en sus palabras clave. |
| Datos de prueba | Palabra clave: comité |

**Pasos:** 1. Entrar a Evidencias. 2. Escribir "comité" en el buscador. 3. Presionar Buscar.

**Resultado esperado:** aparecen las dos evidencias (la que coincide por nombre y la que coincide por palabras clave), y ninguna que no contenga el término.

### CP-HU016-03 — Los resultados respetan los permisos del rol (regla de negocio)

| Campo | Valor |
|---|---|
| Regla de negocio | Los resultados de búsqueda respetan el control de permisos y roles del usuario que realiza la consulta. |
| Tipo / prioridad | Funcional (seguridad, RNF-05) / Alta |
| Rol | Usuario con permiso solo sobre el área Calidad |
| Precondiciones | Existen evidencias con la palabra "acta" en las áreas Calidad y Financiera. |
| Datos de prueba | Palabra clave: acta |

**Pasos:** 1. Iniciar sesión con el usuario de Calidad. 2. Buscar "acta".

**Resultado esperado:** solo aparecen las evidencias del área Calidad; ninguna del área Financiera.

---

## HU-017 — Aprobación y retroalimentación de planes de acción

### CP-HU017-01 — Aprobar un plan enviado a revisión

| Campo | Valor |
|---|---|
| Criterio de aceptación | Dado que un coordinador envía un plan a revisión, cuando accedo al detalle y selecciono "Aprobar", entonces el sistema cambia el estado del plan a "Aprobado". |
| Tipo / prioridad | Funcional / Alta |
| Rol | Director de área |
| Precondiciones | Existe un plan de su área en estado "Pendiente de revisión". |
| Datos de prueba | Plan de prueba creado para el caso |

**Pasos:** 1. Entrar a Revisión. 2. Abrir el plan. 3. Presionar "Aprobar plan".

**Resultado esperado:** el plan queda en estado "Aprobado" y desaparece de la lista de pendientes.

### CP-HU017-02 — Devolver un plan con observaciones

| Campo | Valor |
|---|---|
| Criterio de aceptación | Dado que un plan presenta inconsistencias, cuando selecciono "Devolver" e ingreso observaciones obligatorias, entonces el sistema notifica al coordinador y cambia el estado a "En revisión". |
| Tipo / prioridad | Funcional / Alta |
| Rol | Director de área |
| Precondiciones | Existe un plan de su área en estado "Pendiente de revisión". |
| Datos de prueba | Observación: "Falta indicar qué procesos se auditarán." |

**Pasos:** 1. Abrir el plan. 2. Presionar "Devolver con observaciones". 3. Escribir la observación. 4. Confirmar.

**Resultado esperado:** el plan queda "En revisión" y el coordinador recibe una notificación con la observación.

### CP-HU017-03 — No se puede devolver sin observaciones

| Campo | Valor |
|---|---|
| Criterio de aceptación | Las observaciones son obligatorias al devolver un plan. |
| Tipo / prioridad | Funcional (validación) / Alta |
| Rol | Director de área |
| Precondiciones | Existe un plan en estado "Pendiente de revisión". |
| Datos de prueba | Observación vacía |

**Pasos:** 1. Abrir el plan. 2. Presionar "Devolver con observaciones". 3. Confirmar sin escribir nada.

**Resultado esperado:** el sistema no devuelve el plan, muestra un mensaje en español junto al campo (RNF-06) y el estado no cambia.

### CP-HU017-04 — Un plan devuelto no pasa a ejecución (regla de negocio)

| Campo | Valor |
|---|---|
| Regla de negocio | Un plan devuelto no puede pasar a ejecución hasta que las observaciones registradas sean atendidas y reevaluadas. |
| Tipo / prioridad | Funcional / Alta |
| Rol | Coordinador de procesos |
| Precondiciones | Existe un plan devuelto con una observación sin atender. |
| Datos de prueba | Plan devuelto en CP-HU017-02 |

**Pasos:** 1. Intentar iniciar la ejecución del plan devuelto. 2. Marcar la observación como atendida. 3. Intentar de nuevo sin que el director lo apruebe.

**Resultado esperado:** en los pasos 1 y 3 el sistema no deja iniciar la ejecución; solo se permite cuando las observaciones están atendidas **y** el director vuelve a aprobar el plan.

---

> Pendiente de decisión del equipo: el CU-01 de la Propuesta dice que un plan devuelto vuelve a "En borrador", mientras que el criterio de la HU-017 dice "En revisión". Los casos usan "En revisión" (el criterio de la HU); hay que unificarlo.
