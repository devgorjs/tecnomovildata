package edu.iudigital.tecnomovil.interfaz;

import edu.iudigital.tecnomovil.conexion.ConfiguracionConexion;
import edu.iudigital.tecnomovil.modelo.Simulacion;
import edu.iudigital.tecnomovil.servicio.SimulacionService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Pantalla para generar datos sintéticos y guardarlos en MySQL. */
@SuppressWarnings("serial")
public final class PanelGeneracion extends JPanel {
    private final Supplier<ConfiguracionConexion> configuracionProveedor;
    private final SimulacionService simulacionService;
    private final Consumer<String> notificador;
    private final Consumer<Simulacion> simulacionCreada;
    private final JSpinner controlUsuarios = new JSpinner(
            new SpinnerNumberModel(5_000, 1, 100_000, 500));
    private final JTextField campoSemilla = new JTextField("12345", 16);
    private final JButton botonGenerar = new JButton("Generar y guardar");
    private final JLabel etiquetaResultado = new JLabel("Aún no se ha generado una simulación.");

    public PanelGeneracion(
            Supplier<ConfiguracionConexion> configuracionProveedor,
            SimulacionService simulacionService,
            Consumer<String> notificador,
            Consumer<Simulacion> simulacionCreada
    ) {
        this.configuracionProveedor = Objects.requireNonNull(configuracionProveedor);
        this.simulacionService = Objects.requireNonNull(simulacionService);
        this.notificador = Objects.requireNonNull(notificador);
        this.simulacionCreada = Objects.requireNonNull(simulacionCreada);
        construirInterfaz();
        botonGenerar.addActionListener(evento -> generarYGuardar());
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Parámetros de la simulación"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0;
        formulario.add(new JLabel("Cantidad de usuarios:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(controlUsuarios, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        formulario.add(new JLabel("Semilla de generación:"), c);
        c.gridx = 1; c.weightx = 1;
        formulario.add(campoSemilla, c);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        formulario.add(new JLabel("Cada usuario genera una entrada y una salida."), c);
        add(formulario, BorderLayout.NORTH);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(botonGenerar);
        add(acciones, BorderLayout.CENTER);

        etiquetaResultado.setBorder(BorderFactory.createTitledBorder("Resultado"));
        add(etiquetaResultado, BorderLayout.SOUTH);
    }

    private void generarYGuardar() {
        int usuarios = (Integer) controlUsuarios.getValue();
        final long semilla;
        try {
            semilla = Long.parseLong(campoSemilla.getText().trim());
        } catch (NumberFormatException excepcion) {
            notificador.accept("La semilla debe ser un número entero.");
            return;
        }

        ConfiguracionConexion configuracion;
        try {
            configuracion = configuracionProveedor.get();
        } catch (IllegalArgumentException excepcion) {
            notificador.accept(excepcion.getMessage());
            return;
        }

        botonGenerar.setEnabled(false);
        etiquetaResultado.setText("Generando registros y guardando en MySQL...");
        notificador.accept("Iniciando simulación para " + usuarios + " usuarios...");

        new SwingWorker<Simulacion, Void>() {
            @Override
            protected Simulacion doInBackground() throws Exception {
                return simulacionService.generarYGuardar(configuracion, usuarios, semilla);
            }

            @Override
            protected void done() {
                botonGenerar.setEnabled(true);
                configuracion.limpiarClave();
                try {
                    Simulacion simulacion = get();
                    etiquetaResultado.setText("Simulación #" + simulacion.idSimulacion()
                            + " guardada: " + simulacion.totalUsuarios() + " usuarios / "
                            + simulacion.totalRegistros() + " registros.");
                    notificador.accept("Guardado completado. ID de simulación: "
                            + simulacion.idSimulacion());
                    simulacionCreada.accept(simulacion);
                } catch (Exception excepcion) {
                    Throwable causa = excepcion.getCause() == null
                            ? excepcion : excepcion.getCause();
                    etiquetaResultado.setText("No fue posible guardar la simulación.");
                    notificador.accept("Error al generar o guardar: " + causa.getMessage());
                }
            }
        }.execute();
    }
}
