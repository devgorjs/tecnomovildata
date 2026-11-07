package edu.iudigital.tecnomovil.modelo;

/**
 * Ocupación pico simultánea estimada para una ruta y umbral configurado.
 */
public record RutaCritica(String ruta, long ocupacionPico, long umbral) {
    public RutaCritica {
        if (umbral <= 0) {
            throw new IllegalArgumentException("El umbral debe ser mayor que cero");
        }
    }

    public double porcentajeSobreUmbral() {
        return ((double) (ocupacionPico - umbral) / umbral) * 100.0;
    }
}
