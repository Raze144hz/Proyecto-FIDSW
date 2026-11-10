package com.sgpi.model;

import java.time.LocalDateTime;

/**
 * Evidencia documental cargada sobre una actividad (HU-003). El contenido del archivo no viaja en este
 * registro; se descarga aparte con {@code DataService.descargarEvidencia}.
 *
 * @param tamano tamaño del archivo en bytes
 * @param cargadoPor nombre de quien cargó el archivo
 */
public record Evidencia(
        long id,
        long actividadId,
        String nombreArchivo,
        String formato,
        long tamano,
        String cargadoPor,
        LocalDateTime cargadoEn) {
}
