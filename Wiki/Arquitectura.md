# 🏗️ Arquitectura del Sistema

Esta sección resume los estándares técnicos del proyecto. La documentación completa, con los diagramas, está en [`Docs/Arquitectura/arquitectura.md`](https://github.com/Raze144hz/Proyecto-FIDSW/blob/main/Docs/Arquitectura/arquitectura.md).

## ⚙️ Patrón Arquitectónico
La arquitectura objetivo es **cliente-servidor**: un cliente de escritorio JavaFX que llama a una API en Java 17, la cual accede a una base de datos relacional. El prototipo actual todavía no tiene servidor: es una sola aplicación JavaFX con una base de datos local.

## 🛠️ Stack Tecnológico
| Capa | Prototipo (implementado) | Arquitectura objetivo |
|---|---|---|
| Interfaz | JavaFX 17 (escritorio) | JavaFX 17 (escritorio) |
| Lógica | Java 17 en la misma aplicación | API SGPI en Java 17 (HTTPS) |
| Base de datos | H2 local con JDBC | PostgreSQL 16 con JDBC |
| Seguridad | Contraseñas con hash BCrypt | Contraseñas con hash BCrypt y control de roles |
| Construcción y pruebas | Maven (Maven Wrapper), JUnit 5, GitHub Actions | Igual |
| Control de versiones | Git y GitHub (Git Flow con `main`, `develop` y `feature/`) | Igual |

## 📂 Estructura de Directorios (Repositorio)
El código fuente y la documentación se organizan en las siguientes carpetas:
* `/src` - Código del prototipo y pruebas automáticas.
* `/Database` - Esquema del prototipo (H2) y modelo objetivo (PostgreSQL).
* `/Docs` - Documentación formal (requisitos, arquitectura, calidad, planeación, actas y reportes).
* `/Graficas` - Diagramas, capturas y evidencias; `/Graficas/fuentes` tiene las fuentes PlantUML.
* `/Script` - Scripts de apoyo, como el recorrido automático para las evidencias.
* `/Wiki` - Respaldo de esta documentación.
