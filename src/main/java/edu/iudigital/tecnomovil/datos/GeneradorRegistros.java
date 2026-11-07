package edu.iudigital.tecnomovil.datos;

import edu.iudigital.tecnomovil.modelo.Accion;
import edu.iudigital.tecnomovil.modelo.Registro;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Generador determinista de datos sintéticos.
 * La aleatoriedad queda aislada en esta capa; los análisis no dependen de ella.
 */
public final class GeneradorRegistros {
    private static final LocalDate FECHA_SIMULACION = LocalDate.of(2026, 11, 5);

    private GeneradorRegistros() {
    }

    public static List<Registro> generar(int totalUsuarios, long semilla) {
        if (totalUsuarios < 0) {
            throw new IllegalArgumentException("El total de usuarios no puede ser negativo");
        }

        return IntStream.rangeClosed(1, totalUsuarios)
                .boxed()
                .flatMap(indice -> viajeDeUsuario(indice, semilla))
                .toList();
    }

    private static Stream<Registro> viajeDeUsuario(int indiceUsuario, long semilla) {
        // Cada usuario tiene su propio Random: no se comparte estado mutable entre tareas.
        Random random = new Random(semilla ^ (indiceUsuario * 0x9E3779B97F4A7C15L));
        String idUsuario = "U%07d".formatted(indiceUsuario);
        String ruta = rutaSegunDemanda(random);
        List<String> estaciones = CatalogoRed.estacionesDe(ruta);

        int indiceOrigen = random.nextInt(estaciones.size() - 1);
        int indiceDestino = indiceOrigen + 1
                + random.nextInt(estaciones.size() - indiceOrigen - 1);

        String origen = estaciones.get(indiceOrigen);
        String destino = estaciones.get(indiceDestino);

        int hora = 5 + random.nextInt(15);
        int minuto = random.nextInt(60);
        LocalDateTime entrada = LocalDateTime.of(FECHA_SIMULACION, java.time.LocalTime.of(hora, minuto));
        LocalDateTime salida = entrada.plusMinutes(15 + random.nextInt(30));

        return Stream.of(
                new Registro(idUsuario, ruta, origen, Accion.ENTRADA, entrada),
                new Registro(idUsuario, ruta, destino, Accion.SALIDA, salida)
        );
    }

    private static String rutaSegunDemanda(Random random) {
        double valor = random.nextDouble();
        double acumulado = 0.0;

        for (String ruta : CatalogoRed.RUTAS) {
            acumulado += CatalogoRed.PESO_DE_DEMANDA.get(ruta);
            if (valor < acumulado) {
                return ruta;
            }
        }
        return CatalogoRed.RUTAS.get(CatalogoRed.RUTAS.size() - 1);
    }
}
