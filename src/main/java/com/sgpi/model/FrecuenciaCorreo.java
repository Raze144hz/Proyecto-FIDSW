package com.sgpi.model;

import java.util.List;

/**
 * Frecuencia con la que un usuario recibe por correo sus notificaciones (HU-015). Las notificaciones
 * dentro del sistema llegan siempre; esta preferencia solo cambia los correos.
 */
public final class FrecuenciaCorreo {

    /** Un correo por cada notificación, apenas ocurre. Es la opción por defecto. */
    public static final String INMEDIATA = "INMEDIATA";

    /** Un solo correo al día, a las 7:00, con todas las notificaciones pendientes. */
    public static final String DIARIA = "DIARIA";

    /** No se envían correos. */
    public static final String DESACTIVADA = "DESACTIVADA";

    public static final List<String> TODAS = List.of(INMEDIATA, DIARIA, DESACTIVADA);

    private FrecuenciaCorreo() {
    }

    /** Texto para mostrar en la interfaz. */
    public static String descripcion(String frecuencia) {
        return switch (frecuencia) {
            case INMEDIATA -> "Inmediata (un correo por notificación)";
            case DIARIA -> "Resumen diario (a las 7:00)";
            case DESACTIVADA -> "Desactivada (solo notificaciones en el sistema)";
            default -> frecuencia;
        };
    }
}
