# Datos para el acta de pruebas — Sprint 2

**Sesión de pruebas:** 10 de octubre de 2026. La sesión planeada para el 6 de octubre no quedó registrada.
**Acta que se llena con esto:** [`acta-pruebas-sprint-2.md`](acta-pruebas-sprint-2.md)

Estos son los datos recopilados en la sesión de pruebas. Con ellos se completó el acta y se calculó el resumen.

---

## 1. Versión probada

- Prototipo del Sprint 2: `main` @ `9390848`
- Prototipo con HU-017: `feature/HU-017-aprobacion-planes` @ `5fce05f` (PR #40)

## 2. Entorno

| Dato | Valor |
|---|---|
| Sistema operativo | Ubuntu 24.04.4 LTS con pantalla virtual (Xvfb) |
| Interfaz | Aplicación de escritorio JavaFX 11.0.11 (no aplica navegador) |
| Versión de Java | OpenJDK 21.0.10, compilando con `--release 17` |
| Base de datos y versión | H2 2.2.220, base local en `data/sgpi` |
| Otras librerías | jBCrypt 0.4 y JUnit 5.10.1 |
| Forma de ejecución | Recorrido automático de la interfaz (`Script/evidencias-prototipo`) y pruebas JUnit |

> El `pom.xml` usa JavaFX 17.0.10 y H2 2.2.224. No hubo acceso a Maven Central en el entorno de prueba, por eso falta el caso CP-PRO-09.

## 3. Participantes

| Nombre | Rol en las pruebas |
|---|---|
| David Álvarez Rodríguez | Ejecuta los casos; registra el acta y los defectos |
| David Andrés Galindo Rojas | Valida (pendiente) |
| Miguel Ángel Gómez López | Aprueba el acta (pendiente) |
| Juan Sebastián Martínez | Revisa los casos de HU-016 y HU-017 |

## 4. Resultados por caso

Estado: **Pasa** · **Falla** · **Bloqueado** · **No ejecutado**

| Caso | Qué se prueba | Estado | Ejecutado por | Evidencia | Si falló o no se ejecutó, qué pasó |
|---|---|---|---|---|---|
| CP-PRO-01 | Compilar el prototipo | Pasa | David Álvarez | Compilación con `javac` sin errores | |
| CP-PRO-02 | Abrir la aplicación y crear la base H2, con contraseñas con hash | Pasa | David Álvarez | `01-login.png`, `verificacion-contrasenas.txt` | |
| CP-PRO-03 | Iniciar sesión con cada rol | Pasa | David Álvarez | `03`, `07`, `25` | |
| CP-PRO-04 | Contraseña incorrecta | Pasa | David Álvarez | `02-login-error.png` | |
| CP-PRO-05 | Crear un plan con validación | Pasa | David Álvarez | `04`, `05` | |
| CP-PRO-06 | El rol RESPONSABLE solo consulta | Pasa | David Álvarez | `25-responsable-consulta.png` | |
| CP-PRO-07 | Abrir una base de la versión anterior | Pasa | David Álvarez | `migracion-base-anterior.txt` | |
| CP-PRO-08 | Arrancar en Windows (RNF-08) | No ejecutado | — | — | Solo se probó en Linux |
| CP-PRO-09 | `mvn clean test` | No ejecutado | — | — | Sin acceso a Maven Central |
| CP-PRO-10 | `develop` al día con `main` | Falla | David Álvarez | `develop` @ `35ca77b`, `main` @ `9390848` | `develop` solo tiene la estructura de carpetas del 16 de septiembre |
| CP-HU017-01 | Aprobar un plan pendiente | Pasa | David Álvarez | `11`, `12`, `13`, JUnit | |
| CP-HU017-02 | Devolver con observaciones y notificar | Pasa | David Álvarez | `09`, `10`, `15`, JUnit | |
| CP-HU017-03 | No devolver sin observaciones | Pasa | David Álvarez | `08`, JUnit | |
| CP-HU017-04 | Un plan devuelto no pasa a ejecución | Pasa | David Álvarez | `16` a `22`, JUnit | |
| CP-HU016-01 a 03 | Búsqueda y filtrado de evidencias | No ejecutado | — | — | HU-016 no está programada |

Las capturas están en `Graficas/entrega-2/` y el detalle de cada una en el [índice de evidencias](../evidencias/evidencias-entrega-2.md).

## 5. Defectos encontrados

| Defecto | Descripción | Severidad | Responsable de corregirlo |
|---|---|---|---|
| D-01 | El estado del plan se escogía a mano (se podía crear EN EJECUCION) | Alta | David Álvarez: corregido en el PR #40 |
| D-02 | `updatePlan` y `deletePlan` no validaban el rol | Media | David Álvarez: corregido en el PR #40 |
| D-03 | Las alertas cortaban los mensajes largos en Linux | Baja | David Álvarez: corregido en el PR #40 |
| D-04 | `bootstrap_prototipo.sh` sobrescribe el código actual con una versión anterior | Media | Juan Sebastián Martínez (propuesto), #41 |
| D-05 | Copia de Maven en `tools/` dentro del repositorio | Baja | Juan Sebastián Martínez (propuesto), #41 |
| D-06 | El README del prototipo solo traía el comando para Windows | Baja | David Álvarez: corregido en el PR #40 |
| D-07 | Contraseñas de prueba y usuario de H2 escritos en el código | Baja | David Andrés Galindo (propuesto) |
| D-08 | `develop` desactualizada respecto a `main` | Media | David Andrés Galindo (propuesto), #42 |

## 6. Decisión

- ¿HU-017 pasa a *Done*? **No todavía.** Queda en *In review* hasta que otro integrante revise el PR #40 y se ejecuten CP-PRO-08 y CP-PRO-09.
- ¿El Sprint 2 cumple la DoD de sprint? **No.** Se propone cerrarlo con el prototipo como resultado parcial y pasar las HU pendientes al Sprint 3 (ver la sección 8 del acta).

## 7. Compromisos

| Acción | Responsable | Fecha límite |
|---|---|---|
| Revisar el PR #40 y ejecutar CP-PRO-08 y CP-PRO-09 | David Andrés Galindo | 20 oct |
| Decidir el estado de un plan devuelto y unificar HU-017, CU-01 y los casos | Juan Sebastián Martínez | 20 oct |
| Resolver #41 | Juan Sebastián Martínez | 20 oct |
| Resolver #42 | David Andrés Galindo | 20 oct |
| Escribir los casos de prueba de HU-001, HU-002 y HU-006 | Responsable de cada HU | 20 oct |
| Registrar la Sprint Review y la Retrospectiva del Sprint 2 | Miguel Ángel Gómez | 20 oct |

## 8. Aprobación

| Nombre | Rol | Fecha en que aprueba |
|---|---|---|
| David Álvarez Rodríguez | Responsable de Gestión y Reportes | |
| David Andrés Galindo Rojas | Líder técnico | |
| Miguel Ángel Gómez López | Scrum Master | |
