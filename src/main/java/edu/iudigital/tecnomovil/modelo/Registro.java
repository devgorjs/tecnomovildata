package edu.iudigital.tecnomovil.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento inmutable de validación de un usuario en una estación.
 */
public record Registro(
        String idUsuario,
        String ruta,
        String estacion,
        Accion accion,
        LocalDateTime timestamp
) {
    public Registro {
        Objects.requireNonNull(idUsuario, "idUsuario no puede ser null");
        Objects.requireNonNull(ruta, "ruta no puede ser null");
        Objects.requireNonNull(estacion, "estacion no puede ser null");
        Objects.requireNonNull(accion, "accion no puede ser null");
        Objects.requireNonNull(timestamp, "timestamp no puede ser null");
    }

    public boolean esEntrada() {
        return accion == Accion.ENTRADA;
    }

    public boolean esSalida() {
        return accion == Accion.SALIDA;
    }

    public int hora() {
        return timestamp.getHour();
    }
}
