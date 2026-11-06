package com.sgpi.model;

/** Roles del prototipo (HU-006). */
public final class Rol {

    public static final String ADMIN = "ADMIN";
    public static final String COORDINADOR = "COORDINADOR";
    public static final String RESPONSABLE = "RESPONSABLE";
    public static final String DIRECTOR = "DIRECTOR";

    private Rol() {
    }

    /** ADMIN y COORDINADOR son los únicos que crean, modifican o eliminan planes. */
    public static boolean gestionaPlanes(String rol) {
        return ADMIN.equals(rol) || COORDINADOR.equals(rol);
    }
}
