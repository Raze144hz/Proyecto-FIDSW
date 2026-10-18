-- Modelo relacional objetivo del SGPI (PostgreSQL 16).
-- Equivale a Graficas/modelo-relacional-bd.png (fuente: Graficas/fuentes/modelo-relacional-bd.puml)
-- y cubre HU-016, HU-017, HU-004 y HU-020 según las decisiones del 18 de octubre de 2026
-- (ver Docs/Arquitectura/arquitectura.md).
--
-- El prototipo NO usa este script: usa H2 con el esquema de Database/prototipo-h2.sql,
-- que solo cubre HU-017.

CREATE TABLE area (
    id_area SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE rol (
    id_rol SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);

CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,            -- hash BCrypt (RNF-03)
    id_rol INT NOT NULL REFERENCES rol(id_rol),
    id_area INT NOT NULL REFERENCES area(id_area)
);

CREATE TABLE plan_accion (
    id_plan SERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    objetivo TEXT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(30) NOT NULL
        CHECK (estado IN ('BORRADOR', 'PENDIENTE DE REVISION', 'EN REVISION',
                          'APROBADO', 'EN EJECUCION', 'FINALIZADO')),
    fecha_envio TIMESTAMP,                       -- último envío a revisión (HU-017)
    calificacion_cierre INT,                     -- HU-020: evaluación final del director
    observaciones_cierre TEXT,                   -- HU-020
    fecha_cierre TIMESTAMP,                      -- HU-020
    id_area INT NOT NULL REFERENCES area(id_area),
    id_coordinador INT NOT NULL REFERENCES usuario(id_usuario),
    id_director INT REFERENCES usuario(id_usuario),
    CHECK (fecha_fin >= fecha_inicio),
    -- HU-020: un plan FINALIZADO tiene su evaluación; uno sin finalizar no la tiene.
    CHECK ((estado = 'FINALIZADO') =
           (calificacion_cierre IS NOT NULL AND fecha_cierre IS NOT NULL))
);

-- HU-004: el avance del plan = actividades en FINALIZADA * 100 / total de actividades.
-- Se calcula (ver la vista avance_plan); no se guarda en plan_accion.
CREATE TABLE actividad (
    id_actividad SERIAL PRIMARY KEY,
    descripcion VARCHAR(500) NOT NULL,
    fecha_limite DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'POR_HACER'
        CHECK (estado IN ('POR_HACER', 'EN_PROCESO', 'FINALIZADA')),
    id_plan INT NOT NULL REFERENCES plan_accion(id_plan) ON DELETE CASCADE,
    id_responsable INT NOT NULL REFERENCES usuario(id_usuario)
);

-- CU-02: cada evidencia soporta una actividad. Las evidencias de un plan se consultan
-- uniendo evidencia -> actividad -> plan_accion.
CREATE TABLE evidencia (
    id_evidencia SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    tipo_archivo VARCHAR(10) NOT NULL,
    fecha_carga DATE NOT NULL,
    palabras_clave VARCHAR(255),
    ruta VARCHAR(255) NOT NULL,                  -- archivo en la carpeta evidencias/ del servidor
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario),
    id_area INT NOT NULL REFERENCES area(id_area),
    id_actividad INT NOT NULL REFERENCES actividad(id_actividad) ON DELETE CASCADE
);

CREATE TABLE observacion (
    id_observacion SERIAL PRIMARY KEY,
    descripcion TEXT NOT NULL,
    fecha TIMESTAMP NOT NULL,
    atendida BOOLEAN NOT NULL DEFAULT FALSE,
    id_plan INT NOT NULL REFERENCES plan_accion(id_plan) ON DELETE CASCADE,
    id_autor INT NOT NULL REFERENCES usuario(id_usuario)
);

CREATE TABLE notificacion (
    id_notificacion SERIAL PRIMARY KEY,
    mensaje VARCHAR(255) NOT NULL,
    fecha TIMESTAMP NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario),
    id_plan INT REFERENCES plan_accion(id_plan) ON DELETE CASCADE
);

-- Corte de seguimiento (HU-004): comentario del coordinador y avance calculado en esa fecha.
CREATE TABLE seguimiento_plan (
    id_seguimiento SERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    avance_calculado INT NOT NULL CHECK (avance_calculado BETWEEN 0 AND 100),
    comentario TEXT,
    id_plan INT NOT NULL REFERENCES plan_accion(id_plan) ON DELETE CASCADE,
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario)
);

-- RNF-10: id_plan y codigo_plan no son llave foránea, para que el historial
-- se conserve si el plan se elimina (mismo criterio que historial_planes en el prototipo).
CREATE TABLE historial_plan (
    id_historial SERIAL PRIMARY KEY,
    id_plan INT NOT NULL,
    codigo_plan VARCHAR(20) NOT NULL,
    accion VARCHAR(30) NOT NULL,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    detalle TEXT,
    fecha TIMESTAMP NOT NULL,
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario)
);

-- HU-004: avance de cada plan. Un plan sin actividades tiene avance 0.
CREATE VIEW avance_plan AS
SELECT p.id_plan,
       COUNT(a.id_actividad) AS total_actividades,
       COUNT(a.id_actividad) FILTER (WHERE a.estado = 'FINALIZADA') AS actividades_finalizadas,
       COALESCE(
           COUNT(a.id_actividad) FILTER (WHERE a.estado = 'FINALIZADA') * 100
           / NULLIF(COUNT(a.id_actividad), 0),
           0) AS avance
FROM plan_accion p
LEFT JOIN actividad a ON a.id_plan = p.id_plan
GROUP BY p.id_plan;
