package com.sgpi.data;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.function.UnaryOperator;

/**
 * Envío de correos por SMTP con Jakarta Mail (HU-015, RNF-04). Los datos del servidor se leen de
 * variables de entorno, para no guardar contraseñas en el código ni en el repositorio:
 *
 * <ul>
 *   <li>{@code SGPI_SMTP_HOST}: servidor; si no existe, el envío por correo queda desactivado.</li>
 *   <li>{@code SGPI_SMTP_PUERTO}: puerto (587 por defecto).</li>
 *   <li>{@code SGPI_SMTP_USUARIO} y {@code SGPI_SMTP_CLAVE}: credenciales, si el servidor las pide.</li>
 *   <li>{@code SGPI_SMTP_REMITENTE}: dirección que aparece como remitente (por defecto el usuario).</li>
 *   <li>{@code SGPI_SMTP_TLS}: {@code false} para no usar STARTTLS (por defecto se usa).</li>
 * </ul>
 */
public final class CorreoSmtp implements EnvioCorreo {

    static final String VARIABLE_HOST = "SGPI_SMTP_HOST";

    private final Session sesion;
    private final String remitente;

    private CorreoSmtp(Session sesion, String remitente) {
        this.sesion = sesion;
        this.remitente = remitente;
    }

    /** Crea el envío con las variables de entorno, o devuelve {@code null} si no hay servidor configurado. */
    static CorreoSmtp desdeEntorno(UnaryOperator<String> entorno) {
        String host = valor(entorno, VARIABLE_HOST);
        if (host == null) {
            return null;
        }

        String puerto = valor(entorno, "SGPI_SMTP_PUERTO");
        String usuario = valor(entorno, "SGPI_SMTP_USUARIO");
        String clave = valor(entorno, "SGPI_SMTP_CLAVE");
        String remitente = valor(entorno, "SGPI_SMTP_REMITENTE");
        boolean tls = !"false".equalsIgnoreCase(valor(entorno, "SGPI_SMTP_TLS"));

        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", host);
        propiedades.put("mail.smtp.port", puerto == null ? "587" : puerto);
        propiedades.put("mail.smtp.starttls.enable", String.valueOf(tls));
        propiedades.put("mail.smtp.connectiontimeout", "10000");
        propiedades.put("mail.smtp.timeout", "10000");

        Authenticator autenticacion = null;
        if (usuario != null) {
            propiedades.put("mail.smtp.auth", "true");
            autenticacion = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(usuario, clave == null ? "" : clave);
                }
            };
        }

        String desde = remitente != null ? remitente : usuario != null ? usuario : "sgpi@" + host;
        return new CorreoSmtp(Session.getInstance(propiedades, autenticacion), desde);
    }

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) throws MessagingException {
        MimeMessage mensaje = new MimeMessage(sesion);
        mensaje.setFrom(new InternetAddress(remitente));
        mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        mensaje.setSubject(asunto, "UTF-8");
        mensaje.setText(cuerpo, "UTF-8");
        Transport.send(mensaje);
    }

    private static String valor(UnaryOperator<String> entorno, String nombre) {
        String valor = entorno.apply(nombre);
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
