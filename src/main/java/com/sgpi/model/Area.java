package com.sgpi.model;

/**
 * Área o dependencia de la estructura organizacional (HU-010). Los planes de acción y los directores
 * se asocian a un área por su nombre.
 *
 * @param activa solo las áreas activas se pueden asignar a planes nuevos
 * @param planes cantidad de planes de acción del área
 * @param usuarios cantidad de usuarios activos del área
 */
public record Area(long id, String codigo, String nombre, boolean activa, int planes, int usuarios) {

    /** Un área con planes o usuarios activos no se elimina; se desactiva. */
    public boolean tieneAsociados() {
        return planes > 0 || usuarios > 0;
    }
}
