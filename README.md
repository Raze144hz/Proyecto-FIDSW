# Proyecto-FIDSW
# 🚀 SGPI: Sistema de Gestión de Procesos Institucionales

![GitHub repo size](https://img.shields.io/github/repo-size/Raze144hz/Proyecto-FIDSW?style=for-the-badge)
![GitHub contributors](https://img.shields.io/github/contributors/Raze144hz/Proyecto-FIDSW?style=for-the-badge&color=orange)
![GitHub last commit](https://img.shields.io/github/last-commit/Raze144hz/Proyecto-FIDSW?style=for-the-badge&color=green)
![Status](https://img.shields.io/badge/Estado-En%20Desarrollo-blue?style=for-the-badge)


## 📋 Tabla de Contenidos
- [Descripción del Proyecto](#-descripción-del-proyecto)
- [Estado actual del prototipo](#-estado-actual-del-prototipo)
- [Equipo de Trabajo](#-equipo-de-trabajo)
- [Tecnologías Utilizadas](#️-tecnologías-utilizadas)
- [Cómo ejecutar el prototipo](#️-cómo-ejecutar-el-prototipo)
- [Documentación](#-documentación)
- [Estructura del Repositorio](#-estructura-del-repositorio)
- [Estrategia de Ramas](#-estrategia-de-ramas-git-flow)

## 📌 Descripción del Proyecto

Este proyecto consiste en el diseño y desarrollo de un Sistema de Información de Gestión Institucional, creado con el propósito de centralizar, automatizar y optimizar la administración de los planes de acción vinculados a los diferentes procesos de una institución.

Frente a la necesidad de mantener un control riguroso sobre las metas y proyectos internos, esta plataforma integral busca transformar la planificación institucional, pasando de métodos manuales o descentralizados a un ecosistema digital colaborativo, seguro y enfocado en el cumplimiento.

La **visión** del sistema se fundamenta en cuatro pilares. Describen el producto completo del backlog, no lo que ya está construido; lo implementado está en [Estado actual del prototipo](#-estado-actual-del-prototipo).

1. Formulación de Planes de Acción: Permite el registro estructurado y la planificación de nuevos planes, definiendo tareas específicas, asignando responsables según la estructura organizacional y estableciendo cronogramas claros.

2. Seguimiento y Control: Facilita el monitoreo del avance de las actividades, calculado como actividades finalizadas sobre el total (HU-004). Cuenta con herramientas proactivas como notificaciones de vencimiento, historiales de auditoría (trazabilidad de cambios) y control de estados, garantizando que los objetivos se cumplan en los tiempos estipulados.

3. Gestión y Cargue Documental: Proporciona un repositorio seguro para adjuntar, organizar y gestionar evidencias y documentos de soporte asociados a cada actividad del plan. Incluye funciones de búsqueda avanzada y filtrado para un acceso rápido a la información.

4. Generación de Reportes: Integra un motor de reportería capaz de generar informes gerenciales de cumplimiento y exportar los planes de acción consolidados en formatos estándar (PDF y Excel), apoyando de manera directa la toma de decisiones estratégicas.

A nivel de seguridad, el sistema contempla autenticación de usuarios y control de acceso basado en roles, asegurando que cada miembro de la organización interactúe únicamente con la información y las acciones correspondientes a su perfil.

## 🧪 Estado actual del prototipo

Corte: 18 de octubre de 2026, rama `main`.

| Funcionalidad | Historias | Estado |
|---|---|---|
| Inicio de sesión con contraseñas cifradas (BCrypt) | HU-001 | Implementado en parte (sin bloqueo tras intentos fallidos) |
| Roles ADMIN, COORDINADOR, RESPONSABLE y DIRECTOR con permisos distintos | HU-006 | Implementado en parte |
| Registro, edición, consulta y eliminación de planes con código único | HU-002 | Implementado en parte (sin actividades ni responsables por actividad) |
| Envío a revisión, aprobación o devolución con observaciones, notificaciones internas e historial de estados | HU-017 | Implementado, con 12 pruebas JUnit; pendiente la revisión del PR por otro integrante |
| Seguimiento del avance por actividades y tablero Kanban | HU-004, HU-012a, HU-012b | Diseñado (diagramas y modelo de datos); sin programar |
| Cierre del plan con evaluación del director | HU-020 | Diseñado; sin programar. El botón **Finalizar** del prototipo no pide evaluación |
| Carga, versiones, búsqueda y filtrado de evidencias | HU-003, HU-019, HU-016 | Sin programar |
| Alertas de vencimiento y correo | HU-005, HU-015 | Sin programar |
| Reportes, informes y exportación a PDF y Excel | HU-011, HU-014a, HU-014b | Sin programar |

El mapeo completo entre historias, issues y estado real está en [`Docs/Planning/trazabilidad-hu-issues.md`](Docs/Planning/trazabilidad-hu-issues.md).

## 👥 Equipo de Trabajo

*   **David Andrés Galindo Rojas** - *Líder Técnico / Responsable de Repositorio y Arquitectura*
*   **Miguel Ángel Gómez López** - *Scrum Master / Responsable de Planeación Ágil*
*   **Juan Sebastián Martínez** - *Responsable de Requisitos y Propuesta*
*   **David Álvarez Rodríguez** - *Responsable de Gestión y Reportes*

## 🛠️ Tecnologías Utilizadas

*   **Lenguaje de Programación:** Java 17
*   **Interfaz:** JavaFX (aplicación de escritorio)
*   **Base de Datos:** H2 con JDBC en el prototipo
*   **Seguridad:** contraseñas con hash BCrypt
*   **Construcción y pruebas:** Maven (con Maven Wrapper), JUnit 5 y GitHub Actions. Ninguna de las ejecuciones de Actions ha arrancado desde que se creó el flujo (12 de octubre): GitHub informa que la cuenta dueña del repositorio está bloqueada por un problema de facturación. Mientras se resuelve, las pruebas se corren localmente con `mvnw clean test`.
*   **Herramientas:** GitHub Projects, Git

## ▶️ Cómo ejecutar el prototipo

Con JDK 17 o superior, desde la carpeta del proyecto:

```
.\mvnw.cmd javafx:run "-Djavafx.platform=win"      (Windows)
sh mvnw javafx:run -Djavafx.platform=linux        (Linux; en macOS, mac)
```

Requisitos, usuarios de prueba y pruebas automáticas: [`README-PROTOTIPO.md`](README-PROTOTIPO.md). Uso de la aplicación: [guía de usuario](Docs/guia-de-usuario.md).

## 📚 Documentación

| Tema | Documento |
|---|---|
| Propuesta, requisitos e historias de usuario | [`Docs/propuesta-y-requisitos.md`](Docs/propuesta-y-requisitos.md) |
| Entrega 1 | [Actas de reunión](Docs/actas-de-reunion-entrega-1.md), [informe de resultados](Docs/informe-de-resultados-entrega-1.md), [reporte gerencial](Docs/reporte-gerencial-entrega-1.md), [WBS y organigrama](Docs/wbs-y-organigrama-entrega-1.md) |
| Entrega 2 | [Reporte gerencial del Sprint 2](Docs/reporte-gerencial-entrega-2.md), [acta de pruebas](Docs/Calidad/actas/acta-pruebas-sprint-2.md), [evidencias del prototipo](Docs/Calidad/evidencias/evidencias-entrega-2.md) |
| Arquitectura | [`Docs/Arquitectura/arquitectura.md`](Docs/Arquitectura/arquitectura.md) (arquitectura objetivo y diferencias con el prototipo) |
| Base de datos | [Esquema del prototipo (H2)](Database/prototipo-h2.sql), [modelo objetivo (PostgreSQL)](Database/modelo-objetivo-postgresql.sql) |
| Calidad | [Definition of Done](Docs/Calidad/definition-of-done.md), [formato de caso de prueba](Docs/Calidad/formato-caso-de-prueba.md), [formato de acta de pruebas](Docs/Calidad/formato-acta-de-pruebas.md), [casos de HU-016 y HU-017](Docs/Calidad/casos-de-prueba-HU016-HU017.md), [casos de HU-004 y HU-020](Docs/Calidad/casos-de-prueba-HU004-HU020.md) |
| Planeación | Actas de Sprint Planning del [Sprint 2](Docs/Planning/Acta-Planning-Sprint2.md) y del [Sprint 3](Docs/Planning/Acta-Planning-Sprint3.md), [cronograma de desarrollo](Docs/Planning/cronograma-desarrollo.md), [trazabilidad HU ↔ issues](Docs/Planning/trazabilidad-hu-issues.md) |
| Scrum | [Dailies](Docs/Dailys) |
| Uso del prototipo | [Guía de usuario](Docs/guia-de-usuario.md) |

## 📂 Estructura del Repositorio

El repositorio está organizado de la siguiente manera para mantener el orden y la escalabilidad:

*   `/Docs`: Documentación formal (propuesta, requisitos, historias de usuario, actas, reportes, calidad, arquitectura y registros Scrum).
*   `/src`: Código fuente del prototipo (`src/main/java`) y pruebas automáticas (`src/test/java`).
*   `/Database`: Modelos de base de datos y scripts DDL/DML.
*   `/Script`: Scripts de ejecución, configuración y pruebas.
*   `/Graficas`: Recursos visuales, capturas de pantalla, diagramas y organigramas; `/Graficas/fuentes` tiene las fuentes PlantUML de los diagramas.
*   `/Wiki`: Respaldo de la documentación de la Wiki (ver su [README](Wiki/README.md)).
*   `pom.xml`, `mvnw`, `mvnw.cmd` y `.mvn/`: Configuración de Maven y Maven Wrapper.
*   `.github/workflows`: Compilación y pruebas automáticas en GitHub Actions.

## 🌿 Estrategia de Ramas (Git Flow)

Para este proyecto utilizamos el siguiente estándar de ramas:
*   `main`: Contiene la versión estable de cada sprint. No se hace push directo; solo recibe `develop` al cerrar el sprint, con su etiqueta (`v0.3`, `v0.4`…).
*   `develop`: Rama principal de integración. Todas las `features` nacen y se fusionan aquí por pull request.
*   `feature/HU-0xx-descripcion`: Ramas temporales para desarrollar una historia de usuario.
*   `docs/descripcion`: Ramas temporales para cambios de documentación; siguen el mismo camino que las `feature/`.

Cada pull request va hacia `develop`, enlaza su issue y necesita la aprobación de un integrante distinto al autor ([Definition of Done](Docs/Calidad/definition-of-done.md)).

> Hasta el Sprint 2 los pull requests se unieron directo a `main` y `develop` se quedó atrás (issue #42). El 18 de octubre `develop` quedó al día con `main` (PR #53) y desde entonces se sigue este flujo.
