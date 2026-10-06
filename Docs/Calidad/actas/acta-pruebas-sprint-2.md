# Acta de pruebas — Sprint 2

> Preparada el 3 de octubre con el [formato de acta de pruebas](../formato-acta-de-pruebas.md). Las secciones de resultados se llenan durante la sesión de pruebas del **6 de octubre** (congelamiento de código).

## 1. Datos generales

| Campo | Valor |
|---|---|
| Proyecto | SGPI — Sistema de Gestión de Procesos Institucionales |
| Sprint | 2 (23 sep – 7 oct de 2026) |
| Fecha de la sesión de pruebas | 6 de octubre de 2026 |
| Versión probada | `develop` @ [commit] |
| Entorno | [Sistema operativo, navegador y versión, versión de Java, base de datos y versión] |
| Responsable del acta | David Álvarez Rodríguez (Gestión y Reportes) |

## 2. Participantes

| Nombre | Rol en las pruebas |
|---|---|
| David Álvarez Rodríguez | Registra el acta y los defectos |
| David Andrés Galindo Rojas | |
| Miguel Ángel Gómez López | |
| Juan Sebastián Martínez | |

## 3. Alcance

- **HU incluidas:** ninguna. En el Sprint 2 no se programaron HU (ver [cronograma](../../Planning/cronograma-desarrollo.md)).
- **HU no incluidas y motivo:** HU-001, HU-002, HU-003, HU-005, HU-006 y HU-018 pasan al Sprint 3 o a sprints posteriores por el atraso del diseño técnico.
- **Qué se prueba:** que el entorno de desarrollo configurado el 5 de octubre funciona y está listo para empezar a programar en el Sprint 3.
- **Requisitos no funcionales verificados:** RNF-08 (ejecución en Windows y Linux).

## 4. Casos de prueba del entorno

| Caso | Qué se prueba | Resultado esperado |
|---|---|---|
| CP-ENT-01 | Compilar y arrancar el backend (Spring) desde `develop` | Compila sin errores y la aplicación queda arriba sin excepciones en el log. |
| CP-ENT-02 | Conexión del backend con la base de datos | La aplicación se conecta a la BD; las credenciales se leen de la configuración del entorno, no están escritas en el código. |
| CP-ENT-03 | Ejecutar el script del modelo relacional de `Database/` sobre una base vacía | Se crean las 7 tablas (AREA, ROL, USUARIO, EVIDENCIA, PLAN_ACCION, OBSERVACION, NOTIFICACION) con sus llaves, sin errores. |
| CP-ENT-04 | Abrir el frontend web en el navegador | Carga la página inicial sin errores en la consola del navegador. |
| CP-ENT-05 | Arrancar el proyecto en Windows y en Linux (RNF-08) | Arranca en los dos sistemas sin cambiar el código. |
| CP-ENT-06 | Revisar que `develop` está al día con `main` | `develop` contiene las carpetas `Docs/` y `Graficas/` actuales. |

## 5. Resultados

| Caso | Estado (Pasa · Falla · Bloqueado · No ejecutado) | Ejecutado por | Evidencia | Defecto |
|---|---|---|---|---|
| CP-ENT-01 | | | | |
| CP-ENT-02 | | | | |
| CP-ENT-03 | | | | |
| CP-ENT-04 | | | | |
| CP-ENT-05 | | | | |
| CP-ENT-06 | | | | |

## 6. Resumen

| Indicador | Valor |
|---|---|
| Casos planeados | 6 |
| Casos ejecutados | |
| Casos que pasan | |
| Casos que fallan | |
| Casos bloqueados | |
| Porcentaje de aprobación (pasan / ejecutados) | |

## 7. Defectos encontrados

| Issue | Descripción | Severidad (Crítica · Alta · Media · Baja) | Responsable | Estado |
|---|---|---|---|---|
| # | | | | |

## 8. Decisión

| Pregunta | Respuesta |
|---|---|
| ¿El entorno queda listo para empezar el Sprint 3? | Sí · No |
| Si no, ¿qué falta y quién lo resuelve? | |

## 9. Compromisos

| Acción | Responsable | Fecha límite |
|---|---|---|
| | | |

## 10. Aprobación

| Nombre | Rol | Fecha |
|---|---|---|
| David Álvarez Rodríguez | Responsable de Gestión y Reportes | |
| David Andrés Galindo Rojas | Líder técnico | |
| Miguel Ángel Gómez López | Scrum Master | |
