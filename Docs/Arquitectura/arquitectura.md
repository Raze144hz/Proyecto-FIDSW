# Arquitectura del SGPI — Diseño de HU-016 y HU-017

Diagramas de diseño para los dos casos de uso elegidos en el ejercicio de la Sesión 15 (Análisis y diseño de software):

- **HU-016** — Búsqueda avanzada y filtrado de evidencias (líder de calidad).
- **HU-017** — Aprobación y retroalimentación de planes de acción (director de área).

Todos los diagramas están en PNG en la carpeta [`/Graficas`](../../Graficas). Los de clases, modelo relacional, secuencia y procesos tienen su fuente editable en PlantUML (`.puml`) en [`Graficas/fuentes`](../../Graficas/fuentes).

## 1. Diagrama de clases

![Diagrama de clases](../../Graficas/diagrama-clases.png)

- Clases del dominio de las dos HU: `Usuario`, `Rol`, `Area`, `Busqueda`, `Evidencia`, `PlanAccion`, `EstadoPlan` (enumeración), `Observacion`, `Notificacion` y `HistorialPlan`.
- Un `Usuario` tiene un `Rol` y pertenece a un `Area`. Es responsable de evidencias, hace búsquedas, coordina o dirige planes de acción, escribe observaciones y recibe notificaciones.
- `PlanAccion` es el centro de HU-017:
  - Pertenece a un área, que es la que revisa su director.
  - Contiene sus `Observacion` (composición: una observación no existe sin su plan).
  - Genera `Notificacion` para el coordinador.
  - Registra cada cambio de estado en `HistorialPlan` (RNF-10).
  - Sus métodos siguen el flujo de la HU: `enviarRevision()`, `aprobar()`, `devolver(observacion)`, `iniciarEjecucion()` y `finalizar()`. `puedeEjecutarse()` aplica la regla de negocio: un plan devuelto no pasa a ejecución hasta que sus observaciones queden atendidas y el director lo apruebe de nuevo.
- `EstadoPlan` tiene los estados del ciclo de vida: `BORRADOR` → `PENDIENTE_REVISION` → `APROBADO` o `EN_REVISION` (devuelto) → `EN_EJECUCION` → `FINALIZADO`.
- `Busqueda` usa `Evidencia` («use») para buscar y filtrar, pero no la guarda. Cada `Evidencia` está clasificada en un `Area`.

## 2. Modelo relacional de la base de datos

![Modelo relacional de la base de datos](../../Graficas/modelo-relacional-bd.png)

- Ocho tablas: `AREA`, `ROL`, `USUARIO`, `EVIDENCIA`, `PLAN_ACCION`, `OBSERVACION`, `NOTIFICACION` y `HISTORIAL_PLAN`. Cada clase del diagrama es una tabla, salvo `Busqueda`, que no se guarda, y `EstadoPlan`, que es la columna `estado`.
- `USUARIO` referencia a `ROL` y `AREA`; `correo` es único porque con él se inicia sesión.
- `PLAN_ACCION` referencia a su área y dos veces a `USUARIO`: al coordinador y al director. `codigo` es único. `id_director` y `fecha_envio` quedan vacíos mientras el plan no se ha enviado ni revisado.
- `OBSERVACION` referencia al plan y a su autor. `NOTIFICACION` referencia al destinatario y, cuando el aviso es de un plan, al plan. `HISTORIAL_PLAN` referencia al plan y al usuario que hizo el cambio.
- Las fechas con hora son `TIMESTAMP`, el tipo de PostgreSQL, que es la base de datos del diagrama de despliegue.

El prototipo de la Entrega 2 implementa la parte de HU-017 de este modelo en H2, con cinco tablas: el rol y el área quedan como texto en `usuarios`, y no hay tabla de evidencias porque HU-016 no está programada. Ver [`Database/prototipo-h2.sql`](../../Database/prototipo-h2.sql).

### Cambios frente a la primera versión

| Cambio | Motivo |
|---|---|
| `EstadoPlan` incluye `BORRADOR` y `FINALIZADO` | Todo plan nace en borrador (CU-01) y el ciclo termina en finalizado, como en el prototipo |
| Nueva clase `HistorialPlan` y tabla `HISTORIAL_PLAN` | RNF-10: cada cambio de estado queda con el valor anterior y el nuevo |
| `PlanAccion` tiene `codigo`, `objetivo`, `fechaInicio` y `fechaFin`, y se asocia con su `Area` | Son los datos del formulario de CU-01, y el director revisa los planes de su área |
| Dos relaciones entre `USUARIO` y `PLAN_ACCION` (coordinador y director) | La tabla tiene dos llaves foráneas a `USUARIO`; antes se dibujaba una sola línea |
| Asociación `autor` entre `Usuario` y `Observacion`, y atributo `leida` en `Notificacion` | Ya estaban en el modelo relacional y faltaban en el de clases |
| El director de un plan es `0..1`; `id_director` y `fecha_envio` pueden ser nulos | Un plan en borrador todavía no tiene director ni fecha de envío |
| Un rol o un área pueden tener `0..*` usuarios | Coincide con el modelo relacional |
| `DATETIME` cambia a `TIMESTAMP` | `DATETIME` es de MySQL; el despliegue usa PostgreSQL 16 |

## 3. Diagrama de componentes

![Diagrama de componentes](../../Graficas/diagrama-componentes.png)

- Notación de la Sesión 15: componente con ícono, interfaz provista (bola) e interfaz requerida (media luna).
- Cada componente indica las clases del diagrama de clases que lo implementan y la historia de usuario que atiende.
- El recuadro inferior define las operaciones de cada interfaz a partir de los métodos del diagrama de clases.

## 4. Diagrama de despliegue

![Diagrama de despliegue](../../Graficas/diagrama-despliegue.png)

- Tres nodos: «Cliente» PC del usuario, «Servidor» de aplicaciones y «Servidor» de base de datos.
- Artefactos desplegados en cada nodo y conectores «TCP/IP» con su protocolo (HTTPS y JDBC) y multiplicidad.
- Notas con las propiedades mínimas de software y hardware de cada nodo.

## 5. Diagrama de despliegue unido con componentes

![Diagrama de despliegue con componentes](../../Graficas/diagrama-despliegue-con-componentes.png)

Mismos nodos y conectores del punto 4, con los componentes del punto 3 ubicados dentro de cada nodo (formato de la diapositiva "Uniendo diagrama de despliegue con componentes").

## 6. Diagrama de secuencia (HU-017)

![Diagrama de secuencia de HU-017](../../Graficas/diagrama-secuencia-hu017.png)

- Participantes: el director de área, el coordinador y los componentes del punto 3 que intervienen en HU-017: Vista Revisión de Planes, Gestión de Planes de Acción, Seguridad y Roles, Notificaciones, Persistencia y BD SGPI.
- Los mensajes son las operaciones de las interfaces del punto 3 y los métodos del diagrama de clases: `aprobar()`, `devolver(observacion)`, `puedeEjecutarse()`, `tienePermiso(accion)`, `enviar(destinatario, mensaje)`, `guardar(entidad)`, `consultar(criterios)` y `buscarPorId(id)`. `listarPendientes(area)` es la consulta con la que la vista del director carga los planes de su área.
- Tiene tres partes:
  - El director consulta los planes pendientes de su área.
  - Revisa un plan: el bloque `alt` separa aprobar de devolver. Dentro de devolver, otro `alt` muestra que con las observaciones vacías la vista muestra el error y no llama a Gestión de Planes.
  - La regla de negocio: si el coordinador intenta ejecutar un plan devuelto, `puedeEjecutarse()` responde `false`.

## 7. Diagrama de procesos (HU-017)

![Diagrama de procesos de HU-017](../../Graficas/diagrama-procesos-hu017.png)

- Diagrama de actividades con tres carriles: Coordinador, Sistema SGPI y Director de área.
- Sigue el ciclo de vida del plan: BORRADOR → PENDIENTE DE REVISION → APROBADO → EN EJECUCION → FINALIZADO.
- El ciclo de revisión se repite mientras el director devuelva el plan. Cada devolución deja el plan EN REVISION, con observaciones obligatorias; el coordinador lo corrige, marca las observaciones como atendidas y lo reenvía.
- Un plan devuelto queda EN REVISION, como dice HU-017. El CU-01 dice "En borrador"; esa diferencia está pendiente de decisión del equipo.

## Supuestos pendientes de confirmar por el equipo

| Tema | Supuesto usado en los diagramas | Fuente |
|---|---|---|
| Cliente | Aplicación de escritorio JavaFX sobre JRE 17 | Propuesta, RNF-06 y RNF-08 |
| Comunicación cliente–servidor | API REST sobre HTTPS (puerto 443) | Propuesto en estos diagramas; no definido en otro documento |
| Base de datos | PostgreSQL 16, acceso por JDBC (puerto 5432) | RNF-09 (PostgreSQL o MySQL) |
| Hardware mínimo | 2 núcleos, 4 GB de RAM, discos de 50 GB (aplicaciones) y 100 GB (BD) | Propuesto en estos diagramas; no definido en otro documento |
| Filtro de evidencias | `filtrar(tipo, inicio, fin, area, responsable)` | HU-016 (filtrar por tipo, fecha, área o responsable) |

Si el equipo decide una aplicación web o MySQL, hay que actualizar los diagramas. Los de secuencia y procesos están pendientes de la revisión técnica del líder técnico.

## Cómo regenerar las imágenes desde las fuentes

Las fuentes de los diagramas de clases, modelo relacional, secuencia y procesos están en [`Graficas/fuentes`](../../Graficas/fuentes). Se abren en cualquier editor de PlantUML (por ejemplo, la extensión de PlantUML de VS Code o el servidor web de PlantUML), o se generan las imágenes desde la carpeta `Graficas` con:

```
java -jar plantuml.jar -tpng -o .. fuentes/*.puml
```
