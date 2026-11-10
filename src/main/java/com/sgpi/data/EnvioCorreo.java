package com.sgpi.data;

/**
 * Envía un correo (HU-015). La implementación real es {@link CorreoSmtp}; las pruebas usan una que solo
 * guarda los mensajes.
 */
@FunctionalInterface
public interface EnvioCorreo {

    void enviar(String destinatario, String asunto, String cuerpo) throws Exception;
}
