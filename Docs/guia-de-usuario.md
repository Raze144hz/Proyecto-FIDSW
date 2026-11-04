# Guía de usuario del prototipo SGPI

Cómo se usa el prototipo de escritorio del SGPI. Para instalarlo y ejecutarlo, ver [`README-PROTOTIPO.md`](../README-PROTOTIPO.md).

## 1. Iniciar sesión

1. Abra la aplicación. Aparece la pantalla de inicio de sesión.
2. Escriba el **Correo** y la **Contraseña** y pulse **Iniciar sesión** (o Enter).
3. Si los datos no coinciden, la aplicación muestra "Usuario o contraseña incorrectos." con los intentos que quedan, y no deja entrar.
4. Después de **3 intentos fallidos seguidos** la cuenta queda **bloqueada 15 minutos** (HU-001), aunque luego se escriba la contraseña correcta. La aplicación indica cuántos minutos faltan. Un inicio de sesión exitoso reinicia el contador.

Los usuarios de prueba están en [`README-PROTOTIPO.md`](../README-PROTOTIPO.md#usuarios-de-prueba).

En la barra superior están el botón **Notificaciones (n)**, con el número de notificaciones sin leer, y **Cerrar sesión**.

## 2. Qué puede hacer cada rol

| Rol | Qué ve | Qué puede hacer |
|---|---|---|
| ADMIN | Tabla de planes y formulario | Crear, modificar y eliminar planes; enviarlos a revisión y llevar su ejecución |
| COORDINADOR | Tabla de planes y formulario | Lo mismo que ADMIN |
| RESPONSABLE | Tabla de planes | Consulta los planes; registra el avance de las actividades que tiene asignadas cuando el plan está en ejecución |
| DIRECTOR | Vista de revisión de planes de su área | Aprobar o devolver con observaciones los planes pendientes de su área |

## 3. Registrar un plan (coordinador o administrador)

1. Pulse **Nuevo** para limpiar el formulario.
2. Escriba el objetivo y el área responsable y elija las fechas de inicio y de fin.
3. Pulse **Crear plan**. El plan recibe un código único (por ejemplo `PA-2026-0001`) y queda en **BORRADOR**.

Si falta un dato, la aplicación lo indica, por ejemplo "Debe ingresar el objetivo del plan.".

Para modificar un plan, selecciónelo en la tabla, cambie los datos y pulse **Actualizar plan**. Solo se pueden modificar o eliminar planes en **BORRADOR** o **EN REVISION** (devueltos). **Eliminar** borra el plan seleccionado, después de confirmar; un plan que ya está en revisión, aprobado o en ejecución no se puede eliminar. La edición y la eliminación quedan en el historial.

### Actividades del plan (HU-002)

1. Seleccione el plan y pulse **Actividades del plan** (encima de la tabla).
2. Escriba la actividad, elija un responsable (o déjela **Sin asignar**) y la fecha límite, que debe estar entre las fechas del plan.
3. Pulse **Agregar actividad**. Para quitar una, selecciónela y pulse **Eliminar seleccionada**.

Las actividades se agregan o eliminan mientras el plan está en **BORRADOR** o **EN REVISION**.

## 4. Enviar un plan a revisión

1. Seleccione el plan en la tabla.
2. Pulse **Enviar a revisión del director**. El plan queda **PENDIENTE DE REVISION** y ya no se puede modificar.

El plan le aparece al director cuyo área coincide con el área responsable del plan. El área se compara sin importar mayúsculas ni espacios.

## 5. Revisar un plan (director de área)

La vista del director tiene dos pestañas:

- **Pendientes de revisión:** los planes de su área que esperan su decisión.
- **Planes del área:** todos los planes de su área con su estado.

Al seleccionar un plan pendiente se ven el **Detalle del plan** y las **Observaciones anteriores**, si las hay. Luego:

- **Aprobar plan:** después de confirmar, el plan queda **APROBADO**.
- **Devolver con observaciones:** escriba en **Observaciones para el coordinador** qué debe corregir y confirme. El plan queda **EN REVISION**. Si el campo está vacío, la aplicación muestra "Debe escribir las observaciones para devolver el plan." junto al campo y no devuelve el plan.

En los dos casos el coordinador recibe una notificación. Si el plan se devolvió, la notificación incluye las observaciones.

## 6. Atender un plan devuelto (coordinador)

1. Abra **Notificaciones** para ver qué pidió el director.
2. Seleccione el plan, corrija los datos y pulse **Actualizar plan**.
3. Seleccione cada observación y pulse **Marcar observación como atendida**.
4. Pulse **Enviar a revisión del director**. El plan vuelve a **PENDIENTE DE REVISION**.

No se puede reenviar un plan con observaciones sin atender, y un plan devuelto no se puede ejecutar hasta que el director lo apruebe de nuevo.

## 7. Ejecutar, registrar avance y finalizar un plan

- **Iniciar ejecución:** solo para un plan **APROBADO** y sin observaciones pendientes. El plan queda **EN EJECUCION**.
- **Registrar avance (HU-004):** con el plan en ejecución, abra **Actividades del plan**, seleccione una actividad, elija el nuevo estado (**PENDIENTE**, **EN PROGRESO** o **FINALIZADA**) y pulse **Actualizar estado de la seleccionada**. Lo pueden hacer el coordinador, el administrador o el responsable asignado a esa actividad.
- **Avance:** la barra de la ventana y la columna **Avance** de la tabla muestran actividades **FINALIZADA** sobre el total. Una actividad en progreso no suma.
- **Finalizar plan:** el plan en ejecución queda **FINALIZADO**. Si el plan tiene actividades, todas deben estar finalizadas (avance 100 %).

> En el prototipo, **Finalizar plan** lo hace el coordinador y no pide evaluación. Cuando se programe HU-020, el cierre lo hará el director de área con la calificación y las observaciones de cierre.

## 8. Historial del plan

**Ver historial del plan** muestra cada cambio con la fecha, quién lo hizo, el estado anterior y el nuevo: creación, edición, eliminación, cambios de estado y cambios de actividades. En las devoluciones también muestra la observación.

Cuando un plan se envía a revisión, el director del área recibe una notificación.

## Estados del plan

```
BORRADOR → PENDIENTE DE REVISION → APROBADO → EN EJECUCION → FINALIZADO
                    ↓        ↑
                 EN REVISION (devuelto con observaciones)
```
