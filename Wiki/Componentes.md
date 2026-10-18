# 🧩 Componentes y Módulos

El sistema está dividido en los siguientes módulos funcionales, basados en las Historias de Usuario (HU) de la [propuesta](https://github.com/Raze144hz/Proyecto-FIDSW/blob/main/Docs/propuesta-y-requisitos.md). El estado de cada HU está en la [trazabilidad HU ↔ issues](https://github.com/Raze144hz/Proyecto-FIDSW/blob/main/Docs/Planning/trazabilidad-hu-issues.md).

### 🔒 1. Módulo de Seguridad y Accesos
* **Autenticación:** Login seguro y recuperación de contraseña (`HU-001`, `HU-018`).
* **Roles:** Control de permisos y administración de usuarios (`HU-006`, `HU-008`).
* **Configuración:** Parámetros generales y estructura organizacional (`HU-009`, `HU-010`).

### 📝 2. Módulo de Formulación y Aprobación
* **Registro:** Creación de planes de acción y de sus actividades (`HU-002`).
* **Aprobación:** Revisión del director de área, que aprueba o devuelve con observaciones (`HU-017`).

### ⏱️ 3. Módulo de Seguimiento y Control
* **Avance:** Avance del plan calculado por actividades finalizadas y tablero Kanban (`HU-004`, `HU-012a`, `HU-012b`).
* **Auditoría:** Historial de auditoría y trazabilidad de cambios (`HU-013`).
* **Notificaciones:** Alertas de vencimiento y correo (`HU-005`, `HU-015`).
* **Cierre:** Evaluación final del director y cierre del plan (`HU-020`).

### 📂 4. Módulo de Gestión Documental
* **Evidencias:** Cargue, versiones, búsqueda avanzada y filtrado de documentos de soporte (`HU-003`, `HU-019`, `HU-016`).

### 📊 5. Módulo de Reportes
* **Informes:** Informes de cumplimiento, drill-down y su validación (`HU-014a`, `HU-014b`, `HU-007`).
* **Exportación:** Planes de acción en PDF y Excel (`HU-011`).
