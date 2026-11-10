package com.sgpi.model;

import java.time.LocalDate;

/**
 * Actividad de un plan de acción (HU-002 y HU-004). El avance del plan se calcula como
 * actividades finalizadas sobre el total.
 *
 * @param responsableId usuario responsable, o {@code null} si aún no tiene responsable
 * @param responsable nombre del responsable, o {@code null}
 */
public record Actividad(
        long id,
        long planId,
        String descripcion,
        Long responsableId,
        String responsable,
        LocalDate fechaLimite,
        String estado) {

    public boolean finalizada() {
        return EstadoActividad.FINALIZADA.equals(estado);
    }
}
