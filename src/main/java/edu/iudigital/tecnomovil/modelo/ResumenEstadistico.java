package edu.iudigital.tecnomovil.modelo;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Resultados inmutables de los análisis realizados sobre una simulación. */
public record ResumenEstadistico(
        long idSimulacion,
        int totalRegistros,
        Map<String, Long> afluenciaPorEstacion,
        List<FranjaHoraria> horasPico,
        List<UsoRuta> rutasMasUtilizadas,
        Map<String, List<String>> patronesDeViaje,
        Map<Tramo, Duration> tiemposPromedio,
        List<RutaCritica> rutasCriticas,
        long umbralOcupacion
) {
    public ResumenEstadistico {
        afluenciaPorEstacion = Map.copyOf(afluenciaPorEstacion);
        horasPico = List.copyOf(horasPico);
        rutasMasUtilizadas = List.copyOf(rutasMasUtilizadas);
        patronesDeViaje = patronesDeViaje.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())
                ));
        tiemposPromedio = Map.copyOf(tiemposPromedio);
        rutasCriticas = List.copyOf(rutasCriticas);
    }
}
