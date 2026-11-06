package com.sgpi.model;

import java.time.LocalDateTime;

/** Observación que el director registra al devolver un plan (HU-017). */
public record Observacion(
        long id,
        long planId,
        String autor,
        String descripcion,
        LocalDateTime fecha,
        boolean atendida) {}
