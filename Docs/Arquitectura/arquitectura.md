# Arquitectura del SGPI — Diseño de HU-016, HU-017, HU-004 y HU-020

Diagramas de diseño para los dos casos de uso elegidos en el ejercicio de la Sesión 15 (Análisis y diseño de software):

- **HU-016** — Búsqueda avanzada y filtrado de evidencias (líder de calidad).
- **HU-017** — Aprobación y retroalimentación de planes de acción (director de área).

Desde la revisión con el profesor del 16 de octubre, los diagramas también incluyen el seguimiento del plan de acción (**HU-004**, seguimiento de avance) y, desde el 18 de octubre, su cierre (**HU-020**, cierre y evaluación final).

Todos los diagramas están en PNG en la carpeta [`/Graficas`](../../Graficas). Su fuente editable está en [`Graficas/fuentes`](../../Graficas/fuentes): en PlantUML (`.puml`) los de clases, modelo relacional, secuencia y procesos, y en SVG los de componentes y de despliegue con componentes. El diagrama de despliegue todavía no tiene fuente.

## 0. Arquitectura objetivo y prototipo implementado

Los diagramas describen la **arquitectura objetivo** (propuesta) del SGPI. El **prototipo** de la Entrega 2 es más pequeño y usa otra distribución:

| Aspecto | Arquitectura objetivo (diagramas) | Prototipo implementado (`main`) |
|---|---|---|
| Cliente | Aplicación JavaFX que llama a una API por HTTPS | Aplicación JavaFX de escritorio que hace todo en un solo programa (`Main.java`) |
| Servidor de aplicaciones | API SGPI en Java 17 con los componentes del diagrama | No existe |
| Base de datos | PostgreSQL 16 en un servidor aparte, con el [modelo relacional](#2-modelo-relacional-de-la-base-de-datos) | H2 local en la carpeta `data/`, con el esquema de [`Database/prototipo-h2.sql`](../../Database/prototipo-h2.sql) |
| Archivos de evidencia | Carpeta `evidencias/` del servidor, a cargo del Gestor de Archivos | No implementado |
| Historias cubiertas | HU-016, HU-017, HU-004 y HU-020 | HU-017 completa; partes de HU-001, HU-002 y HU-006 |
| Seguimiento por actividades (HU-004) y cierre con evaluación (HU-020) | Diseñados en los diagramas de clases, modelo relacional, componentes y procesos | No implementados: el prototipo no tiene actividades, avance ni evaluación; su botón **Finalizar** deja el plan en FINALIZADO sin esas reglas |

El diagrama de secuencia (punto 6) muestra HU-017, que el prototipo sí implementa, pero con los componentes de la arquitectura objetivo.

## Decisiones del 18 de octubre

Tomadas para que requisitos, diagramas, base de datos y pruebas digan lo mismo:

| Tema | Decisión | Dónde se refleja |
|---|---|---|
| Regla de avance (HU-004) | El avance del plan es **actividades en FINALIZADA / total de actividades**, como dicen la propuesta, el issue #4 y el CU-03. Nadie lo digita. | Clase `Actividad`, `calcularAvance()`, tabla `ACTIVIDAD`, procesos, casos de prueba |
| Seguimiento del plan (pedido del profesor) | `SeguimientoPlan` es un **corte de seguimiento**: el coordinador escribe un comentario y el sistema guarda el avance calculado en esa fecha | Clase `SeguimientoPlan`, tabla `SEGUIMIENTO_PLAN` (`avance_calculado`) |
| Evidencias | Cada evidencia pertenece a **una actividad** (CU-02). Las evidencias de un plan se consultan por sus actividades. Una actividad solo pasa a FINALIZADA si tiene evidencia (CU-03). | `EVIDENCIA.id_actividad`, procesos |
| Cierre (HU-020) | Lo hace el **director de área** cuando el avance es 100 %, con calificación y observaciones. El estado final se llama **FINALIZADO**, como en el código y los diagramas. Después el plan no se modifica. | `finalizar(calificacion, observaciones)`, columnas `*_cierre` de `PLAN_ACCION`, procesos |
| Historial (RNF-10) | El historial **se conserva aunque el plan se elimine**, como en el prototipo: `HISTORIAL_PLAN` guarda `id_plan` y `codigo_plan` sin llave foránea | Modelo relacional, script del modelo objetivo |

## 1. Diagrama de clases

![Diagrama de clases](../../Graficas/diagrama-clases.png)

- Clases del dominio: `Usuario`, `Rol`, `Area`, `Busqueda`, `Evidencia`, `PlanAccion`, `EstadoPlan`, `Actividad`, `EstadoActividad`, `Observacion`, `Notificacion`, `HistorialPlan` y `SeguimientoPlan`.
- Un `Usuario` tiene un `Rol` y pertenece a un `Area`. Es responsable de evidencias y de actividades, coordina o dirige planes de acción, escribe observaciones, registra seguimientos y recibe notificaciones.
- `PlanAccion` es el centro del diseño:
  - Pertenece a un área, que es la que revisa su director.
  - Contiene sus `Observacion` y sus `Actividad` (composición: no existen sin su plan).
  - Genera `Notificacion` para el coordinador.
  - Registra cada cambio en `HistorialPlan` (RNF-10).
  - Contiene sus `SeguimientoPlan` (composición), los cortes de seguimiento durante la ejecución (HU-004).
  - Sus métodos siguen el ciclo de vida: `enviarRevision()`, `aprobar()`, `devolver(observacion)`, `iniciarEjecucion()`, `agregarActividad(actividad)`, `calcularAvance()`, `registrarSeguimiento(comentario)`, `puedeFinalizarse()` y `finalizar(calificacion, observaciones)`.
  - `puedeEjecutarse()` aplica la regla de HU-017: un plan devuelto no pasa a ejecución hasta que sus observaciones queden atendidas y el director lo apruebe de nuevo.
  - `calcularAvance()` aplica la regla de HU-004: actividades en `FINALIZADA` × 100 / total de actividades.
  - `puedeFinalizarse()` y `finalizar()` aplican HU-020: solo el director del área, con avance 100 %, registra la calificación y las observaciones de cierre (`calificacionCierre`, `observacionesCierre`, `fechaCierre`).
- `Actividad` tiene descripción, fecha límite, un `Usuario` responsable y un `EstadoActividad`, cuyo atributo `estado` toma los valores `POR_HACER`, `EN_PROCESO` o `FINALIZADA` (las columnas del Kanban del CU-03). `cambiarEstado()` no deja pasar a `FINALIZADA` una actividad sin evidencia (`tieneEvidencia()`).
- `EstadoPlan` tiene un solo atributo, `estado`, y los estados del ciclo de vida son los valores que toma: `BORRADOR` → `PENDIENTE_REVISION` → `APROBADO` o `EN_REVISION` (devuelto) → `EN_EJECUCION` → `FINALIZADO`. `getEstado()` devuelve el texto que se muestra y se guarda en la base de datos (por ejemplo, `PENDIENTE_REVISION` se guarda como "PENDIENTE DE REVISION"), y `esEditable()` indica si el coordinador puede modificar el plan (solo en `BORRADOR` o `EN_REVISION`).
- `SeguimientoPlan` guarda la fecha, el avance calculado en ese momento y un comentario. Lo registra un `Usuario`.
- `HistorialPlan` y `SeguimientoPlan` son distintos: el historial lo escribe el sistema en cada cambio (auditoría); el seguimiento lo registra una persona para reportar cómo va el plan. `HistorialPlan` guarda el código del plan para que la auditoría siga legible si el plan se elimina.
- `Busqueda` depende de `Usuario` («use»): `buscar(palabraClave, usuario)` recibe a quien busca, porque los resultados dependen de sus permisos y de su área. También usa `Evidencia` («use») para buscar y filtrar, pero no la guarda. `filtrar()` recibe tipo, fechas, área y responsable, como pide HU-016. Cada `Evidencia` está clasificada en un `Area` y soporta una `Actividad`.

## 2. Modelo relacional de la base de datos

![Modelo relacional de la base de datos](../../Graficas/modelo-relacional-bd.png)

El script equivalente para PostgreSQL está en [`Database/modelo-objetivo-postgresql.sql`](../../Database/modelo-objetivo-postgresql.sql).

- Diez tablas: `AREA`, `ROL`, `USUARIO`, `EVIDENCIA`, `PLAN_ACCION`, `ACTIVIDAD`, `OBSERVACION`, `NOTIFICACION`, `HISTORIAL_PLAN` y `SEGUIMIENTO_PLAN`. Cada clase del diagrama es una tabla, salvo `Busqueda`, que no se guarda, y `EstadoPlan` y `EstadoActividad`, cuyo atributo `estado` es la columna `estado` de su tabla.
- `USUARIO` referencia a `ROL` y `AREA`; `correo` es único porque con él se inicia sesión.
- `PLAN_ACCION` referencia a su área y dos veces a `USUARIO`: al coordinador y al director. `codigo` es único. `id_director` y `fecha_envio` quedan vacíos mientras el plan no se ha enviado ni revisado; `calificacion_cierre`, `observaciones_cierre` y `fecha_cierre` quedan vacíos hasta que el director lo finaliza (HU-020).
- `ACTIVIDAD` referencia al plan y al usuario responsable. El avance del plan no se guarda: se calcula con las actividades en `FINALIZADA` sobre el total.
- `EVIDENCIA` referencia a la actividad que soporta (obligatorio), al usuario responsable y al área.
- `OBSERVACION` referencia al plan y a su autor. `NOTIFICACION` referencia al destinatario y, cuando el aviso es de un plan, al plan.
- `HISTORIAL_PLAN` guarda `id_plan` y `codigo_plan` **sin llave foránea** hacia `PLAN_ACCION`, para que la auditoría se conserve si el plan se elimina (igual que `historial_planes` en el prototipo). Sí referencia al usuario que hizo el cambio.
- `SEGUIMIENTO_PLAN` referencia al plan y al usuario que registró el seguimiento; `avance_calculado` es el avance del plan en esa fecha.
- Las fechas con hora son `TIMESTAMP`, el tipo de PostgreSQL, que es la base de datos del diagrama de despliegue.

El prototipo implementa la parte de HU-017 de este modelo en H2, con cinco tablas: el rol y el área quedan como texto en `usuarios`, y no hay tablas de evidencias, actividades ni seguimiento porque HU-016, HU-003 y HU-004 no están programadas. Ver [`Database/prototipo-h2.sql`](../../Database/prototipo-h2.sql).

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

### Correcciones de la revisión con el profesor (16 de octubre)

| Diagrama | Observación | Cambio |
|---|---|---|
| Clases | `EstadoPlan` debe tener un atributo `estado` | `EstadoPlan` tiene un solo atributo, `estado`, y los estados (`BORRADOR`, `PENDIENTE_REVISION`, `EN_REVISION`, `APROBADO`, `EN_EJECUCION`, `FINALIZADO`) son los valores que toma; también `getEstado()` y `esEditable()`. `EstadoActividad` sigue la misma forma. *Ajustado el 18 de octubre:* la primera versión listaba los estados aparte del atributo. |
| Clases | La búsqueda debe depender del usuario | La asociación `Usuario`–`Busqueda` pasa a ser una dependencia «use» de `Busqueda` hacia `Usuario`; `buscar()` recibe el usuario |
| Clases | Integrar un seguimiento del plan de acción | Nueva clase `SeguimientoPlan` (HU-004), métodos `registrarSeguimiento()` y `calcularAvance()` en `PlanAccion`, y tabla `SEGUIMIENTO_PLAN` |
| Componentes | No poner "Almacén de Archivos", sino el componente específico | El componente **Gestor de Archivos** de la API provee `IArchivos` y usa `java.nio.file` para guardar los archivos en la carpeta `evidencias/` del servidor |
| Componentes | Especificar la herramienta que usa cada componente | Cada componente indica entre paréntesis la herramienta con la que se construye, como `BD SGPI (PostgreSQL 16)`; ver la [tabla del punto 3](#3-diagrama-de-componentes). *Ajustado el 18 de octubre:* la primera versión agregaba recuadros punteados de terceros solo para algunos componentes; se quitaron porque ahora cada componente indica su herramienta. |
| Despliegue | Sin cambios pedidos | Solo se actualizaron la lista de tablas, las HU del cliente y los usuarios de la nota, para que coincidan con los demás diagramas |
| Procesos | La decisión va del lado del "no" y las decisiones deben ser rombos | Las decisiones son rombos; el ciclo de devolución sale por el "no" y vuelve a la decisión; se agregó el seguimiento hasta el 100 % antes de finalizar |

En la misma revisión se corrigieron cuatro diferencias entre diagramas: faltaba `HISTORIAL_PLAN` en las tablas del despliegue y `HistorialPlan` en el componente Gestión de Planes; `listarPendientes(area)`, que usa el diagrama de secuencia, no estaba en `IAprobacionPlanes`; y `filtrar()` tenía menos parámetros en el diagrama de clases que en la interfaz. Además, la columna `estado` pasó de `VARCHAR(20)` a `VARCHAR(30)`.

### Alineación con los requisitos (18 de octubre)

La primera versión del seguimiento registraba un porcentaje digitado por una persona, regla distinta a la de HU-004 (actividades completadas / total). Con las [decisiones del 18 de octubre](#decisiones-del-18-de-octubre):

| Diagrama | Cambio |
|---|---|
| Clases | Nuevas `Actividad` y `EstadoActividad`. `PlanAccion` cambia `registrarSeguimiento(porcentaje, comentario)` por `agregarActividad()`, `registrarSeguimiento(comentario)` y `puedeFinalizarse()`, y `finalizar()` recibe la calificación y las observaciones. `SeguimientoPlan` guarda el avance calculado. `Evidencia` se asocia con `Actividad`, no con `SeguimientoPlan`. `HistorialPlan` guarda el código del plan. |
| Modelo relacional | Nueva tabla `ACTIVIDAD`; `EVIDENCIA.id_actividad` reemplaza a `id_seguimiento`; `PLAN_ACCION` tiene los datos del cierre; `SEGUIMIENTO_PLAN.avance_calculado` reemplaza a `porcentaje_avance`; `HISTORIAL_PLAN` sin llave foránea al plan. |
| Componentes | Vista Seguimiento de Planes y Gestión de Planes atienden HU-020; Gestión de Planes incluye `Actividad`; nuevas operaciones de `ISeguimientoPlanes`. |
| Despliegue (ambos) | La tabla `ACTIVIDAD` en la lista de tablas y HU-020 en el subtítulo. |
| Procesos | Carril del responsable de actividad, avance por actividades con la validación de evidencia, y cierre por el director con la evaluación final. |

## 3. Diagrama de componentes

![Diagrama de componentes](../../Graficas/diagrama-componentes.png)

- Notación de la Sesión 15: componente con ícono, interfaz provista (bola) e interfaz requerida (media luna).
- Cliente SGPI (JavaFX 17) con tres vistas: Revisión de Planes (HU-017), Seguimiento de Planes (HU-004 y HU-020) y Búsqueda de Evidencias (HU-016).
- API SGPI (Java 17) con Gestión de Planes de Acción, Repositorio Documental, Notificaciones, Seguridad y Roles, Estructura Organizacional, Gestor de Archivos y Persistencia.
- Cada componente indica entre paréntesis la herramienta con la que se construye, las clases del diagrama de clases que lo implementan y la historia de usuario que atiende.
- El recuadro inferior define las operaciones de cada interfaz a partir de los métodos del diagrama de clases. `ISeguimientoPlanes` agrupa el seguimiento y el cierre: `agregarActividad`, `cambiarEstado`, `calcularAvance`, `registrarSeguimiento`, `puedeFinalizarse` y `finalizar`.

| Componente | Herramienta | Para qué |
|---|---|---|
| Vista Revisión de Planes, Vista Seguimiento de Planes y Vista Búsqueda de Evidencias | JavaFX 17 | Pantallas del cliente de escritorio, como en el prototipo |
| Gestión de Planes de Acción | Java 17 + Javalin 6 | Reglas del plan en Java; Javalin publica `IAprobacionPlanes` e `ISeguimientoPlanes` como API REST sobre HTTPS |
| Repositorio Documental | Java 17 + Javalin 6 | Búsqueda y filtrado de evidencias; Javalin publica `IBusquedaEvidencias` |
| Notificaciones | Jakarta Mail 2.1 | Envía los correos por SMTP (vencimientos de HU-005, alertas de HU-015 y el enlace de HU-018); los avisos internos se guardan en `NOTIFICACION` |
| Seguridad y Roles | jBCrypt 0.4 | Hash de contraseñas (RNF-03), la misma librería del prototipo |
| Estructura Organizacional | Java 17 | Consulta de áreas |
| Gestor de Archivos | `java.nio.file` (JDK 17) | Guarda y lee los archivos en la carpeta `evidencias/` del servidor |
| Persistencia | JDBC + driver PostgreSQL 42.7 | Consultas SQL a la base de datos |
| BD SGPI | PostgreSQL 16 | Base de datos relacional |

Javalin 6 y Jakarta Mail 2.1 son propuestas del 18 de octubre (ver [supuestos](#supuestos-pendientes-de-confirmar-por-el-equipo)); las demás herramientas ya estaban en los diagramas o en el prototipo.

## 4. Diagrama de despliegue

![Diagrama de despliegue](../../Graficas/diagrama-despliegue.png)

- Tres nodos: «Cliente» PC del usuario, «Servidor» de aplicaciones y «Servidor» de base de datos.
- Artefactos desplegados en cada nodo (incluida la carpeta `evidencias/`, donde el Gestor de Archivos guarda los archivos) y conectores «TCP/IP» con su protocolo (HTTPS y JDBC) y multiplicidad.
- Notas con las propiedades mínimas de software y hardware de cada nodo.
- Es el despliegue **objetivo**: el prototipo actual corre completo en el PC del usuario, con H2 local.

## 5. Diagrama de despliegue unido con componentes

![Diagrama de despliegue con componentes](../../Graficas/diagrama-despliegue-con-componentes.png)

Mismos nodos y conectores del punto 4, con los componentes del punto 3 y su herramienta, ubicados dentro del nodo donde se ejecutan (formato de la diapositiva "Uniendo diagrama de despliegue con componentes").

## 6. Diagrama de secuencia (HU-017)

![Diagrama de secuencia de HU-017](../../Graficas/diagrama-secuencia-hu017.png)

- Participantes: el director de área, el coordinador y los componentes del punto 3 que intervienen en HU-017: Vista Revisión de Planes, Gestión de Planes de Acción, Seguridad y Roles, Notificaciones, Persistencia y BD SGPI.
- Los mensajes son las operaciones de las interfaces del punto 3 y los métodos del diagrama de clases: `aprobar()`, `devolver(observacion)`, `puedeEjecutarse()`, `tienePermiso(accion)`, `enviar(destinatario, mensaje)`, `guardar(entidad)`, `consultar(criterios)` y `buscarPorId(id)`. `listarPendientes(area)` es la operación de `IAprobacionPlanes` con la que la vista del director carga los planes de su área.
- Tiene tres partes:
  - El director consulta los planes pendientes de su área.
  - Revisa un plan: el bloque `alt` separa aprobar de devolver. Dentro de devolver, otro `alt` muestra que con las observaciones vacías la vista muestra el error y no llama a Gestión de Planes.
  - La regla de negocio: si el coordinador intenta ejecutar un plan devuelto, `puedeEjecutarse()` responde `false`.

## 7. Diagrama de procesos (HU-017, HU-004 y HU-020)

![Diagrama de procesos de HU-017, HU-004 y HU-020](../../Graficas/diagrama-procesos-hu017.png)

- Diagrama de actividades con cuatro carriles: Coordinador, Sistema SGPI, Director de área y Responsable de actividad. Las decisiones son rombos.
- Sigue el ciclo de vida del plan: BORRADOR → PENDIENTE DE REVISION → APROBADO → EN EJECUCION → FINALIZADO.
- El coordinador registra el plan junto con sus actividades (responsable y fecha límite).
- Decisión "¿El plan es correcto?": por el "no" el director devuelve el plan con observaciones obligatorias, el sistema lo deja EN REVISION y notifica, y el coordinador lo corrige, marca las observaciones como atendidas y lo reenvía. El director lo revisa otra vez y se vuelve a la decisión. Por el "sí", el director lo aprueba. Así no hay forma de llegar a la ejecución sin aprobación.
- Decisión "¿Todas las actividades están FINALIZADA?": por el "no", el responsable ejecuta una actividad y carga su evidencia (HU-003), el coordinador la mueve a FINALIZADA y el sistema rechaza el cambio si la actividad no tiene evidencia (CU-03); si la tiene, recalcula el avance (finalizadas / total). Después el coordinador registra un seguimiento y el sistema lo guarda con el avance calculado.
- Por el "sí" (avance 100 %), el sistema avisa al director, que registra la evaluación final y finaliza el plan (HU-020). El sistema lo deja FINALIZADO y bloquea las modificaciones.
- Un plan devuelto queda EN REVISION, como dice HU-017. El CU-01 dice "En borrador"; esa diferencia está pendiente de decisión del equipo.

## Supuestos pendientes de confirmar por el equipo

| Tema | Supuesto usado en los diagramas | Fuente |
|---|---|---|
| Cliente | Aplicación de escritorio JavaFX sobre JRE 17 | Propuesta, RNF-06 y RNF-08 |
| Comunicación cliente–servidor | API REST sobre HTTPS (puerto 443), publicada con Javalin 6 | Propuesto en estos diagramas; no definido en otro documento |
| Correo | Jakarta Mail 2.1 con el servidor SMTP de la institución | HU-005, HU-015 y HU-018 (RNF-04); propuesto el 18 de octubre |
| Base de datos | PostgreSQL 16, acceso por JDBC (puerto 5432) | RNF-09 (PostgreSQL o MySQL) |
| Hardware mínimo | 2 núcleos, 4 GB de RAM, discos de 50 GB (aplicaciones) y 100 GB (BD) | Propuesto en estos diagramas; no definido en otro documento |
| Filtro de evidencias | `filtrar(tipo, inicio, fin, area, responsable)` | HU-016 (filtrar por tipo, fecha, área o responsable) |

Si el equipo decide una aplicación web o MySQL, hay que actualizar los diagramas. Están pendientes de la revisión técnica del líder técnico.

## Cómo regenerar las imágenes desde las fuentes

Las fuentes de los diagramas de clases, modelo relacional, secuencia y procesos están en [`Graficas/fuentes`](../../Graficas/fuentes). Se abren en cualquier editor de PlantUML (por ejemplo, la extensión de PlantUML de VS Code o el servidor web de PlantUML), o se generan las imágenes desde la carpeta `Graficas` con:

```
java -jar plantuml.jar -tpng -o .. fuentes/*.puml
```

Los diagramas de componentes y de despliegue con componentes tienen su fuente en SVG (`diagrama-componentes.svg` y `diagrama-despliegue-con-componentes.svg`). Se abren en el navegador y se editan con Inkscape o con un editor de texto (cada texto del diagrama es una etiqueta `<text>`). Para exportar el PNG al mismo tamaño que el de `Graficas`, desde esa carpeta:

```
inkscape fuentes/diagrama-componentes.svg --export-type=png --export-width=3440 --export-filename=diagrama-componentes.png
```

Usan la fuente DejaVu Sans; si no está instalada, el texto sale en Arial y puede cambiar un poco de ancho.

El diagrama de despliegue no tiene fuente editable en el repositorio: se generó con un script que no se subió, y sus cambios del 18 de octubre se hicieron directamente sobre el PNG.
