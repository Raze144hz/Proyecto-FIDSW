package com.sgpi.model;

import java.time.LocalDate;

/** Plan de acción (HU-002). */
public record Plan(
        long id,
        String codigo,
        String objetivo,
        String area,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estado) {}
