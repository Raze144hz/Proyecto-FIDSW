package com.sgpi.model;

import java.util.Optional;

/**
 * Resultado de un intento de inicio de sesión (HU-001).
 *
 * @param usuario el usuario, solo si el inicio de sesión fue exitoso
 * @param minutosBloqueo minutos que faltan para desbloquear la cuenta, si está bloqueada
 * @param intentosRestantes intentos que quedan antes del bloqueo, si las credenciales fallaron
 */
public record ResultadoLogin(Estado estado, Optional<User> usuario, long minutosBloqueo, int intentosRestantes) {

    public enum Estado { EXITOSO, CREDENCIALES_INVALIDAS, BLOQUEADO }

    public static ResultadoLogin exitoso(User usuario) {
        return new ResultadoLogin(Estado.EXITOSO, Optional.of(usuario), 0, 0);
    }

    public static ResultadoLogin invalido(int intentosRestantes) {
        return new ResultadoLogin(Estado.CREDENCIALES_INVALIDAS, Optional.empty(), 0, intentosRestantes);
    }

    public static ResultadoLogin bloqueado(long minutos) {
        return new ResultadoLogin(Estado.BLOQUEADO, Optional.empty(), minutos, 0);
    }

    public boolean exitoso() {
        return estado == Estado.EXITOSO;
    }
}
