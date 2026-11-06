package com.sgpi.model;

import java.time.LocalDateTime;

/** Notificación interna para un usuario. */
public record Notificacion(long id, String mensaje, LocalDateTime fecha, boolean leida) {}
