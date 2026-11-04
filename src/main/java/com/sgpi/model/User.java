package com.sgpi.model;

/** Usuario autenticado. El área solo aplica a los directores (revisan los planes de su área). */
public record User(long id, String nombre, String correo, String rol, String area) {}
