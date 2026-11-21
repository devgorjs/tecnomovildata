package edu.iudigital.tecnomovil.interfaz;

import edu.iudigital.tecnomovil.conexion.ConfiguracionConexion;
import edu.iudigital.tecnomovil.modelo.ResumenEstadistico;
import edu.iudigital.tecnomovil.modelo.Tramo;
import edu.iudigital.tecnomovil.servicio.EstadisticasService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Pantalla que vuelve a calcular estadísticas usando registros recuperados de MySQL. */
@SuppressWarnings("serial")
public final class PanelEstadisticas extends JPanel {
    private static final long UMBRAL_OCUPACION = 80L;
    private final Supplier<ConfiguracionConexion> configuracionProveedor;
    private final EstadisticasService estadisticasService;
    private final Consumer<String> notificador;
    private final JTextField campoIdSimulacion = new JTextField(8);
    private final JButton botonCalcular = new JButton("Calcular estadísticas");
    private final JTextArea areaResultados = new JTextArea();

    public PanelEstadisticas(
            Supplier<ConfiguracionConexion> configuracionProveedor,
            EstadisticasService estadisticasService,
            Consumer<String> notificador
    ) {
        this.configuracionProveedor = Objects.requireNonNull(configuracionProveedor);
        this.estadisticasService = Objects.requireNonNull(estadisticasService);
        this.notificador = Objects.requireNonNull(notificador);
        construirInterfaz();
        botonCalcular.addActionListener(evento -> calcular());
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.setBorder(BorderFactory.createTitledBorder("Simulación para analizar"));
        filtros.add(new JLabel("ID simulación:"));
        filtros.add(campoIdSimulacion);
        filtros.add(botonCalcular);

        areaResultados.setEditable(false);
        areaResultados.setLineWrap(true);
        areaResultados.setWrapStyleWord(true);
        areaResultados.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        areaResultados.setText("Ingrese un ID de simulación para consultar sus estadísticas.");

        add(filtros, BorderLayout.NORTH);
        add(new JScrollPane(areaResultados), BorderLayout.CENTER);
    }

    public void seleccionarSimulacion(long idSimulacion) {
        campoIdSimulacion.setText(Long.toString(idSimulacion));
    }

    private void calcular() {
        final long idSimulacion;
        try {
            idSimulacion = Long.parseLong(campoIdSimulacion.getText().trim());
            if (idSimulacion <= 0) throw new NumberFormatException();
        } catch (NumberFormatException excepcion) {
            notificador.accept("Ingrese un ID de simulación válido.");
            return;
        }

        ConfiguracionConexion configuracion;
        try {
            configuracion = configuracionProveedor.get();
        } catch (IllegalArgumentException excepcion) {
            notificador.accept(excepcion.getMessage());
            return;
        }

        botonCalcular.setEnabled(false);
        areaResultados.setText("Consultando datos y calculando estadísticas...");

        new SwingWorker<ResumenEstadistico, Void>() {
            @Override
            protected ResumenEstadistico doInBackground() throws Exception {
                return estadisticasService.generarResumen(
                        configuracion, idSimulacion, UMBRAL_OCUPACION);
            }

            @Override
            protected void done() {
                botonCalcular.setEnabled(true);
                configuracion.limpiarClave();
                try {
                    areaResultados.setText(formatear(get()));
                    notificador.accept("Estadísticas calculadas con los registros recuperados de MySQL.");
                } catch (Exception excepcion) {
                    Throwable causa = excepcion.getCause() == null ? excepcion : excepcion.getCause();
                    areaResultados.setText("No fue posible calcular las estadísticas.\n\n"
                            + causa.getMessage());
                    notificador.accept("Error en estadísticas: " + causa.getMessage());
                }
            }
        }.execute();
    }

    private String formatear(ResumenEstadistico resumen) {
        StringBuilder texto = new StringBuilder();
        texto.append("RESUMEN DE LA SIMULACIÓN #").append(resumen.idSimulacion()).append("\n");
        texto.append("Registros analizados: ").append(resumen.totalRegistros()).append("\n\n");

        texto.append("1. AFLUENCIA POR ESTACIÓN\n");
        resumen.afluenciaPorEstacion().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> texto.append("   ").append(e.getKey()).append(": ")
                        .append(e.getValue()).append(" ingresos\n"));

        texto.append("\n2. HORAS DE MAYOR FLUJO\n");
        resumen.horasPico().forEach(f -> texto.append("   ")
                .append(f.etiqueta()).append("\n"));

        texto.append("\n3. RUTAS MÁS UTILIZADAS\n");
        resumen.rutasMasUtilizadas().forEach(r -> texto.append("   ")
                .append(r.ruta()).append(": ").append(r.validaciones())
                .append(" validaciones\n"));

        texto.append("\n4. MUESTRA DE PATRONES DE VIAJE\n");
        resumen.patronesDeViaje().entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).limit(5)
                .forEach(e -> texto.append("   ").append(e.getKey()).append(": ")
                        .append(String.join(" -> ", e.getValue())).append("\n"));

        texto.append("\n5. TIEMPOS PROMEDIO ENTRE ESTACIONES\n");
        resumen.tiemposPromedio().entrySet().stream()
                .sorted(Map.Entry.<Tramo, Duration>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> texto.append("   ").append(e.getKey().etiqueta())
                        .append(": ").append(String.format("%.2f", e.getValue().toMillis() / 60_000.0))
                        .append(" minutos\n"));

        texto.append("\n6. RUTAS CRÍTICAS (UMBRAL: ")
                .append(resumen.umbralOcupacion()).append(")\n");
        if (resumen.rutasCriticas().isEmpty()) {
            texto.append("   No se superó el umbral configurado.\n");
        } else {
            resumen.rutasCriticas().forEach(r -> texto.append("   ")
                    .append(r.ruta()).append(": ocupación pico ")
                    .append(r.ocupacionPico()).append(" (sobre umbral ")
                    .append(String.format("%.2f", r.porcentajeSobreUmbral()))
                    .append("%)\n"));
        }
        return texto.toString();
    }
}
