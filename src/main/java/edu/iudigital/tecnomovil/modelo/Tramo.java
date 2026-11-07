package edu.iudigital.tecnomovil.modelo;

/** Par ordenado de estaciones que delimita un viaje observado. */
public record Tramo(String origen, String destino) {
    public String etiqueta() {
        return origen + " -> " + destino;
    }
}
