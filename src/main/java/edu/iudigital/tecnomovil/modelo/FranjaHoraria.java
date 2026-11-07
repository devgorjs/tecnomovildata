package edu.iudigital.tecnomovil.modelo;

/** Resumen del número de validaciones registradas durante una hora. */
public record FranjaHoraria(int hora, long flujo) {
    public String etiqueta() {
        return "%02d:00 - %02d:59 (validaciones: %d)"
                .formatted(hora, hora, flujo);
    }
}
