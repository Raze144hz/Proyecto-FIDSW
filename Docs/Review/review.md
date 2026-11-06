# Sprint Review - Sprint 2
**Fecha:** 19/10/2026
**Scrum Master:** Miguel Ángel Gómez López
**Producto:** SGPI (Sistema de Gestión de Procesos Institucionales)

## 1. Objetivo del Sprint
Aterrizar el diseño técnico, corregir inconsistencias documentales e implementar el prototipo funcional de la Historia de Usuario de mayor complejidad técnica (HU-017).

## 2. Incremento Presentado (Demostración)
El equipo presentó el prototipo funcional operando bajo los siguientes parámetros técnicos:
* **Funcionalidad Integrada:** Implementación exitosa de la **HU-017 (Aprobación y retroalimentación de planes)**.
* **Stack Tecnológico Real:** Desarrollo ejecutado en **JavaFX** para la interfaz de usuario, integrado con una base de datos **H2** en la capa de persistencia.
* **Trazabilidad de Código:** El incremento fue desarrollado de forma aislada y segura en la rama `feature/HU-017-aprobacion-planes`.

## 3. Cumplimiento de Reglas de Negocio
Se validó la lógica de la capa de datos demostrando el flujo estricto de transiciones de estado de un Plan de Acción:
1. Borrador
2. Pendiente revisión
3. Aprobado
4. Ejecución
5. Finalizado

**Aseguramiento de Calidad:** Se diseñaron y ejecutaron **12 pruebas de validación** sobre la capa de datos para blindar las reglas de negocio. El 100% de las pruebas pasaron exitosamente.

## 4. Conclusiones y Próximos Pasos
* El incremento funcional presentado cumple con los requerimientos técnicos y fue aceptado.
* **Siguiente acción:** Realizar la integración (Merge) de la rama `feature/HU-017-aprobacion-planes` hacia la rama principal de desarrollo y dar inicio a las actividades del Sprint 3.
