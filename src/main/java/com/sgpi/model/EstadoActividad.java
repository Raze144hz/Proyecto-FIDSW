package com.sgpi.model;

import java.util.List;

/** Estados de una actividad (HU-004). */
public final class EstadoActividad {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String EN_PROGRESO = "EN PROGRESO";
    public static final String FINALIZADA = "FINALIZADA";

    public static final List<String> TODOS = List.of(PENDIENTE, EN_PROGRESO, FINALIZADA);

    private EstadoActividad() {
    }
}
