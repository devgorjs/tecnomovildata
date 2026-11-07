package edu.iudigital.tecnomovil.modelo;

/** Resultado de una ejecución cronometrada. */
public record ResultadoMedicion<T>(T resultado, long nanosegundos) {
}
