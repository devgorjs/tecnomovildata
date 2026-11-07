package edu.iudigital.tecnomovil.analisis;

import edu.iudigital.tecnomovil.modelo.Registro;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Operadores funcionales reutilizables sobre registros. */
public final class Operadores {
    private Operadores() {
    }

    public static <K> Map<K, Long> contarPor(
            List<Registro> registros,
            Function<Registro, K> clasificador
    ) {
        return registros.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(clasificador, Collectors.counting()),
                        Map::copyOf
                ));
    }

    public static Predicate<Registro> enFranja(int horaInicio, int horaFin) {
        if (horaInicio < 0 || horaFin > 23 || horaInicio > horaFin) {
            throw new IllegalArgumentException("Franja horaria no válida");
        }
        return registro -> registro.hora() >= horaInicio && registro.hora() <= horaFin;
    }

    public static Predicate<Registro> deRuta(String nombreRuta) {
        return registro -> registro.ruta().equals(nombreRuta);
    }

    public static Predicate<Registro> esEntrada() {
        return Registro::esEntrada;
    }

    public static List<Registro> filtrar(
            List<Registro> registros,
            Predicate<Registro> criterio
    ) {
        return registros.stream().filter(criterio).toList();
    }

    public static Function<Map<String, Long>, List<String>> rankingDeEstaciones(int limite) {
        if (limite < 0) {
            throw new IllegalArgumentException("El límite no puede ser negativo");
        }
        return conteos -> conteos.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limite)
                .map(Map.Entry::getKey)
                .toList();
    }
}
