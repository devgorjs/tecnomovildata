package edu.iudigital.tecnomovil;

import edu.iudigital.tecnomovil.analisis.AnalisisTransporte;
import edu.iudigital.tecnomovil.analisis.Medicion;
import edu.iudigital.tecnomovil.datos.GeneradorRegistros;
import edu.iudigital.tecnomovil.modelo.Registro;
import edu.iudigital.tecnomovil.reporte.Reporte;

import java.util.List;

/** Punto de entrada de la aplicación TecnoMovil Data. */
public class MainConsola {
    private static final long SEMILLA = 12345L;
    private static final long UMBRAL_OCUPACION = 80L;

    private MainConsola() {
    }

    public static void main(String[] args) {
        int totalUsuarios = args.length > 0 ? Integer.parseInt(args[0]) : 5_000;
        if (totalUsuarios < 1) {
            throw new IllegalArgumentException("Indique al menos un usuario");
        }

        List<Registro> registros = GeneradorRegistros.generar(totalUsuarios, SEMILLA);

        Reporte.titulo("TecnoMovil Data - resumen de simulación");
        Reporte.resumenGeneral(totalUsuarios, registros);

        Medicion.calentar(registros, 5);
        var secuencial = Medicion.medir(
                () -> AnalisisTransporte.afluenciaPorEstacion(registros));
        var paralelo = Medicion.medir(
                () -> AnalisisTransporte.afluenciaPorEstacionParalela(registros));

        boolean resultadosEquivalentes =
                secuencial.resultado().equals(paralelo.resultado());

        Reporte.titulo("Comparación de procesamiento");
        System.out.printf("Secuencial: %.3f ms%n",
                Medicion.milisegundos(secuencial.nanosegundos()));
        System.out.printf("Paralelo:   %.3f ms%n",
                Medicion.milisegundos(paralelo.nanosegundos()));
        System.out.println("Resultados equivalentes: " + resultadosEquivalentes);

        if (!resultadosEquivalentes) {
            throw new IllegalStateException("El procesamiento secuencial y paralelo difiere");
        }

        Reporte.titulo("Afluencia por estación");
        Reporte.afluencia(secuencial.resultado());

        Reporte.titulo("Top 5 horas de mayor flujo de validaciones");
        Reporte.horasPico(AnalisisTransporte.horasPico(registros, 5));

        Reporte.titulo("Rutas más utilizadas");
        Reporte.rutas(AnalisisTransporte.rutasMasUtilizadas(registros, 3));

        Reporte.titulo("Patrones de viaje (muestra de 5 usuarios)");
        Reporte.patrones(AnalisisTransporte.patronesDeViaje(registros), 5);

        Reporte.titulo("Tiempo promedio entre estaciones (top 10)");
        Reporte.tramos(AnalisisTransporte.tiempoPromedioEntreEstaciones(registros), 10);

        Reporte.titulo("Rutas críticas por ocupación simultánea");
        Reporte.criticas(AnalisisTransporte.rutasCriticas(registros, UMBRAL_OCUPACION));

        Reporte.titulo("Composición de funciones");
        List<Registro> ejemplo = AnalisisTransporte.entradasDeRutaEnFranja(
                registros, "T1", 6, 9);
        System.out.printf("Entradas de T1 entre 06:00 y 09:59: %,d%n", ejemplo.size());
    }
}
