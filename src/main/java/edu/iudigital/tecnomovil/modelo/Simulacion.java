package edu.iudigital.tecnomovil.modelo;

import java.time.LocalDateTime;

/** Representa una ejecución de generación de datos almacenada en MySQL. */
public record Simulacion(
        long idSimulacion,
        LocalDateTime fechaCreacion,
        int totalUsuarios,
        int totalRegistros,
        long semilla
) {
    @Override
    public String toString() {
        return "Simulación #" + idSimulacion + " - " + totalRegistros + " registros";
    }
}
