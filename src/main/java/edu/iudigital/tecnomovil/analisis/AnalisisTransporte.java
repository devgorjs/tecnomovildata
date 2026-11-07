package edu.iudigital.tecnomovil.analisis;

import edu.iudigital.tecnomovil.modelo.*;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Operaciones de análisis. No modifica la lista recibida ni mantiene estado
 * mutable entre invocaciones.
 */
public final class AnalisisTransporte {
    private AnalisisTransporte() {
    }

    /** Cuenta ingresos por estación. */
    public static Map<String, Long> afluenciaPorEstacion(List<Registro> registros) {
        return Operadores.contarPor(
                Operadores.filtrar(registros, Operadores.esEntrada()),
                Registro::estacion
        );
    }

    /**
     * Cuenta todas las validaciones por hora (entradas y salidas), tal como
     * solicita el requerimiento de flujo por hora.
     */
    public static Map<Integer, Long> flujoPorHora(List<Registro> registros) {
        return Operadores.contarPor(registros, Registro::hora);
    }

    public static List<FranjaHoraria> horasPico(List<Registro> registros, int limite) {
        return flujoPorHora(registros).entrySet().stream()
                .map(e -> new FranjaHoraria(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(FranjaHoraria::flujo).reversed()
                        .thenComparingInt(FranjaHoraria::hora))
                .limit(limite)
                .toList();
    }

    /** Ordena rutas por volumen total de validaciones. */
    public static List<UsoRuta> rutasMasUtilizadas(List<Registro> registros, int limite) {
        return Operadores.contarPor(registros, Registro::ruta).entrySet().stream()
                .map(e -> new UsoRuta(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(UsoRuta::validaciones).reversed()
                        .thenComparing(UsoRuta::ruta))
                .limit(limite)
                .toList();
    }

    /** Devuelve las estaciones visitadas por usuario en orden cronológico. */
    public static Map<String, List<String>> patronesDeViaje(List<Registro> registros) {
        Map<String, List<String>> patrones = registros.stream()
                .sorted(Comparator.comparing(Registro::timestamp)
                        .thenComparing(Registro::idUsuario))
                .collect(Collectors.groupingBy(
                        Registro::idUsuario,
                        Collectors.mapping(Registro::estacion, Collectors.toList())
                ));

        return patrones.entrySet().stream().collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> List.copyOf(e.getValue())
        ));
    }

    /**
     * Calcula duraciones de pares consecutivos ENTRADA -> SALIDA del mismo
     * usuario y ruta. No incluye esperas entre viajes diferentes.
     */
    public static Map<Tramo, Duration> tiempoPromedioEntreEstaciones(List<Registro> registros) {
        record ViajeTemporal(Tramo tramo, long minutos) {
        }

        List<ViajeTemporal> viajes = registros.stream()
                .collect(Collectors.groupingBy(Registro::idUsuario))
                .values().stream()
                .flatMap(listaUsuario -> {
                    List<Registro> ordenados = listaUsuario.stream()
                            .sorted(Comparator.comparing(Registro::timestamp))
                            .toList();

                    return IntStream.range(0, Math.max(0, ordenados.size() - 1))
                            .mapToObj(i -> {
                                Registro origen = ordenados.get(i);
                                Registro destino = ordenados.get(i + 1);
                                return (origen.esEntrada()
                                        && destino.esSalida()
                                        && origen.ruta().equals(destino.ruta())
                                        && !destino.timestamp().isBefore(origen.timestamp()))
                                        ? new ViajeTemporal(
                                                new Tramo(origen.estacion(), destino.estacion()),
                                                Duration.between(origen.timestamp(), destino.timestamp()).toMinutes())
                                        : null;
                            })
                            .filter(java.util.Objects::nonNull);
                })
                .toList();

        return viajes.stream().collect(Collectors.collectingAndThen(
                Collectors.groupingBy(ViajeTemporal::tramo,
                        Collectors.averagingLong(ViajeTemporal::minutos)),
                promedios -> promedios.entrySet().stream().collect(
                        Collectors.toUnmodifiableMap(
                                Map.Entry::getKey,
                                e -> Duration.ofMillis(Math.round(e.getValue() * 60_000.0))
                        )
                )
        ));
    }

    /**
     * Calcula la ocupación simultánea máxima por ruta. ENTRADA incrementa
     * ocupación y SALIDA la reduce. El umbral se compara con el pico observado.
     */
    public static List<RutaCritica> rutasCriticas(
            List<Registro> registros,
            long umbralOcupacion
    ) {
        if (umbralOcupacion <= 0) {
            throw new IllegalArgumentException("El umbral debe ser mayor que cero");
        }

        record Estado(long ocupacionActual, long ocupacionPico) {
            Estado aplicar(Registro registro) {
                long nuevaOcupacion = ocupacionActual + (registro.esEntrada() ? 1 : -1);
                if (nuevaOcupacion < 0) {
                    throw new IllegalArgumentException(
                            "Secuencia de eventos inconsistente: salida sin entrada previa en ruta "
                                    + registro.ruta());
                }
                return new Estado(nuevaOcupacion, Math.max(ocupacionPico, nuevaOcupacion));
            }
        }

        return registros.stream()
                .collect(Collectors.groupingBy(Registro::ruta))
                .entrySet().stream()
                .map(entrada -> {
                    long pico = entrada.getValue().stream()
                            .sorted(Comparator.comparing(Registro::timestamp)
                                    .thenComparing(r -> r.esEntrada() ? 0 : 1))
                            .reduce(new Estado(0, 0), Estado::aplicar, (a, b) ->
                                    new Estado(b.ocupacionActual(),
                                            Math.max(a.ocupacionPico(), b.ocupacionPico())))
                            .ocupacionPico();

                    return new RutaCritica(entrada.getKey(), pico, umbralOcupacion);
                })
                .filter(ruta -> ruta.ocupacionPico() > umbralOcupacion)
                .sorted(Comparator.comparingLong(RutaCritica::ocupacionPico).reversed()
                        .thenComparing(RutaCritica::ruta))
                .toList();
    }

    /** Ejemplo de composición de predicados para un análisis específico. */
    public static List<Registro> entradasDeRutaEnFranja(
            List<Registro> registros, String ruta, int horaInicio, int horaFin
    ) {
        return Operadores.filtrar(
                registros,
                Operadores.esEntrada()
                        .and(Operadores.deRuta(ruta))
                        .and(Operadores.enFranja(horaInicio, horaFin))
        );
    }

    /** Variante paralela para contrastar la agregación de afluencia. */
    public static Map<String, Long> afluenciaPorEstacionParalela(List<Registro> registros) {
        return registros.parallelStream()
                .filter(Registro::esEntrada)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(Registro::estacion, Collectors.counting()),
                        Map::copyOf
                ));
    }
}
