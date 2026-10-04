# Arquitectura del SGPI — Diseño de HU-016 y HU-017

Diagramas de diseño para los dos casos de uso elegidos en el ejercicio de la Sesión 15 (Análisis y diseño de software):

- **HU-016** — Búsqueda avanzada y filtrado de evidencias (líder de calidad).
- **HU-017** — Aprobación y retroalimentación de planes de acción (director de área).

Las imágenes están en la carpeta [`/Graficas`](../../Graficas). Cada diagrama tiene versión `.png` y `.svg` (el `.svg` se puede ampliar sin perder calidad).

## 1. Diagrama de componentes

![Diagrama de componentes](../../Graficas/diagrama-componentes.png)

- Notación de la Sesión 15: componente con ícono, interfaz provista (bola) e interfaz requerida (media luna).
- Cada componente indica las clases del diagrama de clases que lo implementan y la historia de usuario que atiende.
- El recuadro inferior define las operaciones de cada interfaz a partir de los métodos del diagrama de clases.

## 2. Diagrama de despliegue

![Diagrama de despliegue](../../Graficas/diagrama-despliegue.png)

- Tres nodos: «Cliente» PC del usuario, «Servidor» de aplicaciones y «Servidor» de base de datos.
- Artefactos desplegados en cada nodo y conectores «TCP/IP» con su protocolo (HTTPS y JDBC) y multiplicidad.
- Notas con las propiedades mínimas de software y hardware de cada nodo.

## 3. Diagrama de despliegue unido con componentes

![Diagrama de despliegue con componentes](../../Graficas/diagrama-despliegue-con-componentes.png)

Mismos nodos y conectores del punto 2, con los componentes del punto 1 ubicados dentro de cada nodo (formato de la diapositiva "Uniendo diagrama de despliegue con componentes").

## Supuestos pendientes de confirmar por el equipo

| Tema | Supuesto usado en los diagramas | Fuente |
|---|---|---|
| Cliente | Aplicación de escritorio JavaFX sobre JRE 17 | Propuesta, RNF-06 y RNF-08 |
| Comunicación cliente–servidor | API REST sobre HTTPS (puerto 443) | Propuesto en estos diagramas; no definido en otro documento |
| Base de datos | PostgreSQL 16, acceso por JDBC (puerto 5432) | RNF-09 (PostgreSQL o MySQL) |
| Hardware mínimo | 2 núcleos, 4 GB de RAM, discos de 50 GB (aplicaciones) y 100 GB (BD) | Propuesto en estos diagramas; no definido en otro documento |
| Filtro de evidencias | `filtrar(tipo, inicio, fin, area, responsable)` | HU-016 (filtrar por tipo, fecha, área o responsable) |

Si el equipo decide una aplicación web o MySQL, hay que actualizar los tres diagramas y el modelo de base de datos (por ejemplo, `DATETIME` es de MySQL y en PostgreSQL es `TIMESTAMP`).
