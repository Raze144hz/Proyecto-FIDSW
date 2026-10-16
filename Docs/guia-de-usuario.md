# Guía de usuario del prototipo SGPI

Cómo se usa el prototipo de escritorio del SGPI. Para instalarlo y ejecutarlo, ver [`README-PROTOTIPO.md`](../README-PROTOTIPO.md).

## 1. Iniciar sesión

1. Abra la aplicación. Aparece la pantalla de inicio de sesión.
2. Escriba el **Correo** y la **Contraseña** y pulse **Iniciar sesión** (o Enter).
3. Si los datos no coinciden, la aplicación muestra "Usuario o contraseña incorrectos." y no deja entrar.

Los usuarios de prueba están en [`README-PROTOTIPO.md`](../README-PROTOTIPO.md#usuarios-de-prueba).

En la barra superior están el botón **Notificaciones (n)**, con el número de notificaciones sin leer, y **Cerrar sesión**.

## 2. Qué puede hacer cada rol

| Rol | Qué ve | Qué puede hacer |
|---|---|---|
| ADMIN | Tabla de planes y formulario | Crear, modificar y eliminar planes; enviarlos a revisión y llevar su ejecución |
| COORDINADOR | Tabla de planes y formulario | Lo mismo que ADMIN |
| RESPONSABLE | Tabla de planes | Solo consulta: no tiene formulario ni botones de cambio |
| DIRECTOR | Vista de revisión de planes de su área | Aprobar o devolver con observaciones los planes pendientes de su área |

## 3. Registrar un plan (coordinador o administrador)

1. Pulse **Nuevo** para limpiar el formulario.
2. Escriba el objetivo y el área responsable y elija las fechas de inicio y de fin.
3. Pulse **Crear plan**. El plan recibe un código único (por ejemplo `PA-2026-0001`) y queda en **BORRADOR**.

Si falta un dato, la aplicación lo indica, por ejemplo "Debe ingresar el objetivo del plan.".

Para modificar un plan, selecciónelo en la tabla, cambie los datos y pulse **Actualizar plan**. Solo se pueden modificar planes en **BORRADOR** o **EN REVISION**. **Eliminar** borra el plan seleccionado, después de confirmar.

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

## 7. Ejecutar y finalizar un plan

- **Iniciar ejecución:** solo para un plan **APROBADO** y sin observaciones pendientes. El plan queda **EN EJECUCION**.
- **Finalizar plan:** el plan en ejecución queda **FINALIZADO**.

## 8. Historial del plan

**Ver historial del plan** muestra cada cambio de estado con la fecha, quién lo hizo, el estado anterior y el nuevo. En las devoluciones también muestra la observación.

## Estados del plan

```
BORRADOR → PENDIENTE DE REVISION → APROBADO → EN EJECUCION → FINALIZADO
                    ↓        ↑
                 EN REVISION (devuelto con observaciones)
```
