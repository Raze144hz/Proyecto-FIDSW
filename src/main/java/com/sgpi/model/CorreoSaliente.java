package com.sgpi.model;

import java.time.LocalDateTime;

/**
 * Correo de notificación generado para un usuario (HU-015).
 *
 * @param enviarDesde fecha desde la que se puede enviar; en el resumen diario es la siguiente 7:00
 * @param enviadoEn fecha de envío, o {@code null} si aún no se envió
 * @param error último error del servidor de correo, o {@code null}
 */
public record CorreoSaliente(
        long id,
        String destinatario,
        String asunto,
        String cuerpo,
        LocalDateTime creadoEn,
        LocalDateTime enviarDesde,
        LocalDateTime enviadoEn,
        String error) {

    /** Estado para mostrar en la interfaz. */
    public String estado() {
        if (enviadoEn != null) {
            return "Enviado";
        }
        return error == null ? "Pendiente" : "Con error";
    }
}
