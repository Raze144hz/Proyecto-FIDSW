package com.sgpi.model;

/** Estados del plan de acción (EstadoPlan en el diagrama de clases). */
public final class EstadoPlan {

    public static final String BORRADOR = "BORRADOR";
    public static final String PENDIENTE_REVISION = "PENDIENTE DE REVISION";
    public static final String EN_REVISION = "EN REVISION";
    public static final String APROBADO = "APROBADO";
    public static final String EN_EJECUCION = "EN EJECUCION";
    public static final String FINALIZADO = "FINALIZADO";

    /**
     * Estado al que vuelve un plan devuelto por el director. La HU-017 dice "En revisión"
     * y el CU-01 dice "En borrador"; si el equipo decide lo segundo, solo se cambia esta línea.
     */
    public static final String DEVUELTO = EN_REVISION;

    private EstadoPlan() {
    }

    /** El coordinador solo puede modificar, eliminar o enviar a revisión un plan que está en sus manos. */
    public static boolean editable(String estado) {
        return BORRADOR.equals(estado) || DEVUELTO.equals(estado);
    }
}
