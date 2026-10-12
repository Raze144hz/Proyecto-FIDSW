# Proyecto-FIDSW
# 🚀 SGPI: Sistema de Gestión de Procesos Institucionales

![GitHub repo size](https://img.shields.io/github/repo-size/Raze144hz/Proyecto-FIDSW?style=for-the-badge)
![GitHub contributors](https://img.shields.io/github/contributors/Raze144hz/Proyecto-FIDSW?style=for-the-badge&color=orange)
![GitHub last commit](https://img.shields.io/github/last-commit/Raze144hz/Proyecto-FIDSW?style=for-the-badge&color=green)
![Status](https://img.shields.io/badge/Estado-En%20Desarrollo-blue?style=for-the-badge)


## 📋 Tabla de Contenidos
- [Descripción del Proyecto](#-descripción-del-proyecto)
- [Equipo de Trabajo](#-equipo-de-trabajo)
- [Tecnologías Utilizadas](#️-tecnologías-utilizadas)
- [Cómo ejecutar el prototipo](#️-cómo-ejecutar-el-prototipo)
- [Documentación](#-documentación)
- [Estructura del Repositorio](#-estructura-del-repositorio)
- [Estrategia de Ramas](#-estrategia-de-ramas-git-flow)

## 📌 Descripción del Proyecto

Este proyecto consiste en el diseño y desarrollo de un Sistema de Información de Gestión Institucional, creado con el propósito de centralizar, automatizar y optimizar la administración de los planes de acción vinculados a los diferentes procesos de una institución.

Frente a la necesidad de mantener un control riguroso sobre las metas y proyectos internos, esta plataforma integral busca transformar la planificación institucional, pasando de métodos manuales o descentralizados a un ecosistema digital colaborativo, seguro y enfocado en el cumplimiento.

El sistema se fundamenta en cuatro pilares principales:

1. Formulación de Planes de Acción: Permite el registro estructurado y la planificación de nuevos planes, definiendo tareas específicas, asignando responsables según la estructura organizacional y estableciendo cronogramas claros.

2. Seguimiento y Control: Facilita el monitoreo en tiempo real del avance de las actividades. Cuenta con herramientas proactivas como notificaciones de vencimiento, historiales de auditoría (trazabilidad de cambios) y control de estados, garantizando que los objetivos se cumplan en los tiempos estipulados.

3. Gestión y Cargue Documental: Proporciona un repositorio seguro para adjuntar, organizar y gestionar evidencias y documentos de soporte asociados a cada actividad del plan. Incluye funciones de búsqueda avanzada y filtrado para un acceso rápido a la información.

4. Generación de Reportes: Integra un motor de reportería capaz de generar informes gerenciales de cumplimiento y exportar los planes de acción consolidados en formatos estándar (PDF y Excel), apoyando de manera directa la toma de decisiones estratégicas.

A nivel de seguridad, el sistema cuenta con autenticación de usuarios y control de acceso basado en roles, asegurando que cada miembro de la organización interactúe únicamente con la información y las acciones correspondientes a su perfil.

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
*   **Construcción y pruebas:** Maven (con Maven Wrapper), JUnit 5 y GitHub Actions
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
| Arquitectura | [`Docs/Arquitectura/arquitectura.md`](Docs/Arquitectura/arquitectura.md) |
| Calidad | [Definition of Done](Docs/Calidad/definition-of-done.md), [formato de caso de prueba](Docs/Calidad/formato-caso-de-prueba.md), [formato de acta de pruebas](Docs/Calidad/formato-acta-de-pruebas.md), [casos de HU-016 y HU-017](Docs/Calidad/casos-de-prueba-HU016-HU017.md) |
| Planeación | [Acta de Sprint Planning del Sprint 2](Docs/Planning/Acta-Planning-Sprint2.md), [cronograma de desarrollo](Docs/Planning/cronograma-desarrollo.md) |
| Scrum | [Dailies](Docs/Dailys), [Sprint Review](Docs/Review/review.md), [Retrospectiva](Docs/Retrospective/retro.md) |
| Uso del prototipo | [Guía de usuario](Docs/guia-de-usuario.md) |

## 📂 Estructura del Repositorio

El repositorio está organizado de la siguiente manera para mantener el orden y la escalabilidad:

*   `/Docs`: Documentación formal (propuesta, requisitos, historias de usuario, actas, reportes, calidad, arquitectura y registros Scrum).
*   `/src`: Código fuente del prototipo (`src/main/java`) y pruebas automáticas (`src/test/java`).
*   `/Database`: Modelos de base de datos y scripts DDL/DML.
*   `/Script`: Scripts de ejecución, configuración y pruebas.
*   `/Graficas`: Recursos visuales, capturas de pantalla, diagramas y organigramas.
*   `/Wiki`: Respaldo de la documentación de la Wiki.
*   `pom.xml`, `mvnw`, `mvnw.cmd` y `.mvn/`: Configuración de Maven y Maven Wrapper.
*   `.github/workflows`: Compilación y pruebas automáticas en GitHub Actions.

## 🌿 Estrategia de Ramas (Git Flow)

Para este proyecto utilizamos el siguiente estándar de ramas:
*   `main`: Contiene el código de producción estable. No se hace push directo.
*   `develop`: Rama principal de integración. Todas las `features` nacen y se fusionan aquí.
*   `feature/nombre-de-tarea`: Ramas temporales para desarrollar nuevas funcionalidades.
