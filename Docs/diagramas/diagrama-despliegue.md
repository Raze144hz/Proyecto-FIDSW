# Diagrama de despliegue

| Campo | Detalle |
|---|---|
| Historias de usuario | HU-017 (principal), HU-004 y HU-016 |
| Issue | #69 (sub-issue de HU-017, #25) |
| Fuente editable | [`diagrama-despliegue.svg`](diagrama-despliegue.svg) (se edita con Inkscape o un editor de texto) |

![Diagrama de despliegue](diagrama-despliegue.svg)

## Nodos y artefactos

| Nodo «device» | Entorno de ejecución | Artefactos | Mínimos propuestos |
|---|---|---|---|
| PC del usuario | JRE 17 + JavaFX 17 | `sgpi-cliente.jar` | Windows 10/11 o Linux (RNF-08), 2 núcleos, 4 GB de RAM, pantalla ≥ 1366×768 |
| Servidor de aplicaciones | JRE 17 sobre Ubuntu Server 22.04 LTS | `sgpi-api.jar`, `application.properties`, carpeta `evidencias/` | 2 núcleos, 4 GB de RAM, disco de 50 GB, disponibilidad ≥ 99 % en horario laboral (RNF-11) |
| Servidor de base de datos | PostgreSQL 16 | `modelo-objetivo-postgresql.sql` (esquema de 10 tablas) | 2 núcleos, 4 GB de RAM, disco SSD de 100 GB, respaldo diario |
| Servidor de correo institucional (servicio externo) | — | Servicio SMTP | Lo administra la institución |

## Comunicaciones

| Desde | Hacia | Protocolo | Puerto | Multiplicidad |
|---|---|---|---|---|
| PC del usuario | Servidor de aplicaciones | HTTPS (REST / JSON) | 443 | 1..* a 1 |
| Servidor de aplicaciones | Servidor de base de datos | JDBC sobre TCP | 5432 | 1 a 1 |
| Servidor de aplicaciones | Servidor de correo | SMTP con STARTTLS | 587 | 1 a 1 |

Dentro del servidor de aplicaciones, `sgpi-api.jar` guarda y lee los archivos de evidencia en la carpeta `evidencias/` con `java.nio.file`.
