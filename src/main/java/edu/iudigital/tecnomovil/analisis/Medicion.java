package edu.iudigital.tecnomovil.analisis;

import edu.iudigital.tecnomovil.modelo.Registro;
import edu.iudigital.tecnomovil.modelo.ResultadoMedicion;

import java.util.List;
import java.util.function.Supplier;

/** Utilidades para calentar y medir operaciones sin alterar los datos. */
public final class Medicion {
    private Medicion() {
    }

    public static void calentar(List<Registro> registros, int iteraciones) {
        for (int i = 0; i < iteraciones; i++) {
            AnalisisTransporte.afluenciaPorEstacion(registros);
            AnalisisTransporte.afluenciaPorEstacionParalela(registros);
        }
    }

    public static <T> ResultadoMedicion<T> medir(Supplier<T> operacion) {
        long inicio = System.nanoTime();
        T resultado = operacion.get();
        long duracion = System.nanoTime() - inicio;
        return new ResultadoMedicion<>(resultado, duracion);
    }

    public static double milisegundos(long nanosegundos) {
        return nanosegundos / 1_000_000.0;
    }
}
