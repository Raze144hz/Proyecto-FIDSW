# Guion de la demostración — Entrega final (20 de noviembre de 2026)

Recorrido de unos 10 minutos que muestra el prototipo completo con una base nueva. Detalle de cada pantalla: [guía de usuario](guia-de-usuario.md).

## 1. Antes de la sustentación

En PowerShell, con la aplicación cerrada:

```
cd C:\Users\David\Documents\Proyecto-FIDSW
git checkout main
git pull
.\mvnw.cmd clean test
```

Debe terminar en `BUILD SUCCESS` con 76 pruebas y 0 fallos.

**Base de datos nueva.** La carpeta `data\` guarda la base local. Para empezar sin los planes de pruebas anteriores, muévala a otro lugar (no se borra nada):

```
Move-Item data ..\data-antes-de-la-demo
```

La aplicación crea una base nueva con los usuarios de prueba al abrirse. Para volver a la base anterior: cerrar la aplicación, borrar `data\` y mover `..\data-antes-de-la-demo` de vuelta como `data`.

**Abrir la aplicación** (dejar PowerShell abierto mientras se usa):

```
.\mvnw.cmd javafx:run "-Djavafx.platform=win"
```

Tener a mano un PDF para las evidencias, por ejemplo cualquiera de `Descargas`.

## 2. Usuarios

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@sgpi.local` | `Admin123*` |
| Coordinador | `coordinador@sgpi.local` | `Coord123*` |
| Responsable | `responsable@sgpi.local` | `Resp123*` |
| Director de Calidad | `director@sgpi.local` | `Dir123*` |

## 3. Datos del plan de la demostración

| Campo | Valor |
|---|---|
| Objetivo | Mejorar la atención a los estudiantes en la oficina de Calidad |
| Área responsable | Calidad |
| Fechas | 20/11/2026 a 31/12/2026 |
| Actividad 1 | "Aplicar encuesta de satisfacción", responsable: Responsable Demo, fecha límite **24/11/2026** (vence en 4 días, genera el aviso de HU-005) |
| Actividad 2 | "Publicar el informe de resultados", responsable: Responsable Demo, fecha límite 15/12/2026 |
| Actividad 3 (al corregir) | "Socializar los resultados con los estudiantes", responsable: Responsable Demo, fecha límite 10/12/2026 |
| Observación del director | "Agregar una actividad para socializar los resultados con los estudiantes." |

Al escribir una fecha, confírmela con **Enter** antes de salir del campo; con Tab puede quedar otra fecha.

## 4. Recorrido

| # | Usuario | Qué hacer | Qué se muestra |
|---|---|---|---|
| 1 | — | Iniciar sesión con `coordinador@sgpi.local` y una contraseña errada, una sola vez | HU-001: "Usuario o contraseña incorrectos" con los intentos restantes. No repetir 3 veces: bloquea la cuenta 15 minutos |
| 2 | Administrador | **Áreas** | HU-010: Calidad, Sistemas, Planeación y Talento Humano, activas. Cerrar sesión |
| 3 | Coordinador | **Nuevo**, llenar objetivo, área y fechas, **Crear plan** | HU-002: código `PA-2026-0001` en BORRADOR |
| 4 | Coordinador | **Actividades del plan**, agregar las actividades 1 y 2 | Actividades con responsable y fecha límite |
| 5 | Coordinador | **Enviar a revisión del director**. Cerrar sesión | PENDIENTE DE REVISION |
| 6 | Director | Pestaña **Pendientes de revisión**, seleccionar el plan, intentar **Devolver** sin observación y luego con la observación | HU-017: la observación es obligatoria; el plan queda EN REVISION |
| 7 | Coordinador | **Notificaciones**: ver la observación. Agregar la actividad 3, **Marcar observación como atendida**, **Enviar a revisión del director** | El plan vuelve a PENDIENTE DE REVISION |
| 8 | Director | **Aprobar plan**. Cerrar sesión | APROBADO; el coordinador recibe la notificación |
| 9 | — | Cerrar la ventana del SGPI y volver a abrirla con el comando de la sección 1 (la revisión de vencimientos corre al abrir) | |
| 10 | Responsable | **Notificaciones** | HU-005: aviso de vencimiento de "Aplicar encuesta de satisfacción" (24/11/2026), además de los avisos de asignación |
| 11 | Responsable | **Preferencias** | HU-015: frecuencia de correo y correos generados (quedan pendientes porque no hay servidor SMTP configurado) |
| 12 | Coordinador | Seleccionar el plan, **Iniciar ejecución** | EN EJECUCION |
| 13 | Coordinador | **Actividades del plan**: marcar la actividad 1 como FINALIZADA sin evidencia | HU-003: no deja, pide cargar una evidencia |
| 14 | Coordinador | **Cargar evidencia...** con el PDF y marcar FINALIZADA. Repetir con las actividades 2 y 3 | HU-004: el avance sube a 33 %, 67 % y 100 % |
| 15 | Coordinador | **Ver historial del plan** | Creación, envío, devolución, aprobación, ejecución y actividades |
| 16 | Coordinador | **Finalizar plan** | FINALIZADO. No pide la evaluación del director: es el bug #274 (HU-020) |

## 5. Azure DevOps y repositorio

1. Azure DevOps → **Test Plans** → plan **SGPI - Pruebas entrega final** (#158): las 7 suites, un caso abierto con sus pasos y las capturas adjuntas en el resultado.
2. **Progress report**: muestra 17 puntos porque CP-001 a CP-005 estaban duplicados en la suite raíz por error. Los casos reales son 12, todos ejecutados: 9 pasaron y 3 fallaron.
3. **Boards → Queries** con Work Item Type = Bug: los bugs #272, #273 y #274.
4. GitHub: `main` con el [reporte de pruebas](Calidad/reporte-pruebas-entrega-final.md), el [reporte gerencial](reporte-gerencial-entrega-final.md) y el [informe postmortem](informe-postmortem.md).
