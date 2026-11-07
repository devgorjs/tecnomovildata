package edu.iudigital.tecnomovil.datos;

import java.util.List;
import java.util.Map;

/** Catálogo inmutable de rutas, estaciones y distribución de demanda. */
public final class CatalogoRed {
    private CatalogoRed() {
    }

    public static final List<String> RUTAS = List.of("T1", "T2", "T3");

    public static final Map<String, List<String>> ESTACIONES_POR_RUTA = Map.of(
            "T1", List.of("Norte", "Universidad", "Centro", "Sur"),
            "T2", List.of("Occidente", "Estadio", "Centro", "Oriente"),
            "T3", List.of("Terminal", "Sur", "Poblado")
    );

    public static final Map<String, Double> PESO_DE_DEMANDA = Map.of(
            "T1", 0.50,
            "T2", 0.30,
            "T3", 0.20
    );

    public static List<String> estacionesDe(String ruta) {
        return ESTACIONES_POR_RUTA.getOrDefault(ruta, List.of());
    }
}
