package com.sgpi.model;

import java.time.LocalDateTime;

/** Entrada del historial de cambios de un plan (RNF-10, trazabilidad). */
public record RegistroHistorial(
        String accion,
        String estadoAnterior,
        String estadoNuevo,
        String usuario,
        String detalle,
        LocalDateTime fecha) {}
