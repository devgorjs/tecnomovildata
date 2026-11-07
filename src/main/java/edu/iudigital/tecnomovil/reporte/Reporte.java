package edu.iudigital.tecnomovil.reporte;

import edu.iudigital.tecnomovil.modelo.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Presentación de resultados. Los efectos de consola quedan aislados aquí. */
public final class Reporte {
    private Reporte() {
    }

    public static void titulo(String texto) {
        System.out.println("\n=== " + texto.toUpperCase() + " ===");
    }

    public static void resumenGeneral(int usuarios, List<Registro> registros) {
        long entradas = registros.stream().filter(Registro::esEntrada).count();
        long salidas = registros.stream().filter(Registro::esSalida).count();
        System.out.printf("Usuarios simulados: %,d%n", usuarios);
        System.out.printf("Registros generados: %,d%n", registros.size());
        System.out.printf("Entradas: %,d | Salidas: %,d%n", entradas, salidas);
    }

    public static void afluencia(Map<String, Long> conteos) {
        conteos.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("%s: %,d ingresos%n", e.getKey(), e.getValue()));
    }

    public static void horasPico(List<FranjaHoraria> franjas) {
        franjas.forEach(f -> System.out.println(f.etiqueta()));
    }

    public static void rutas(List<UsoRuta> rutas) {
        rutas.forEach(r -> System.out.printf("%s: %,d validaciones%n",
                r.ruta(), r.validaciones()));
    }

    public static void patrones(Map<String, List<String>> patrones, int limite) {
        patrones.entrySet().stream().sorted(Map.Entry.comparingByKey()).limit(limite)
                .forEach(e -> System.out.printf("%s: %s%n",
                        e.getKey(), String.join(" -> ", e.getValue())));
    }

    public static void tramos(Map<Tramo, Duration> promedios, int limite) {
        promedios.entrySet().stream()
                .sorted(Map.Entry.<Tramo, Duration>comparingByValue().reversed()
                        .thenComparing(e -> e.getKey().etiqueta()))
                .limit(limite)
                .forEach(e -> System.out.printf("%s: %.2f minutos%n",
                        e.getKey().etiqueta(), e.getValue().toMillis() / 60_000.0));
    }

    public static void criticas(List<RutaCritica> rutas) {
        if (rutas.isEmpty()) {
            System.out.println("No se detectaron rutas por encima del umbral de ocupación.");
            return;
        }
        rutas.forEach(r -> System.out.printf(
                "ALERTA %s: ocupación pico %,d; umbral %,d; variación %.2f%%%n",
                r.ruta(), r.ocupacionPico(), r.umbral(), r.porcentajeSobreUmbral()));
    }
}
