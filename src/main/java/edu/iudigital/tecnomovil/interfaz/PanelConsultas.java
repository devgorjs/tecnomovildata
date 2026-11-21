package edu.iudigital.tecnomovil.interfaz;

import edu.iudigital.tecnomovil.conexion.ConfiguracionConexion;
import edu.iudigital.tecnomovil.modelo.Accion;
import edu.iudigital.tecnomovil.modelo.RegistroPersistido;
import edu.iudigital.tecnomovil.modelo.Simulacion;
import edu.iudigital.tecnomovil.servicio.ConsultaService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Pantalla para consultar y mostrar los registros guardados. */
@SuppressWarnings("serial")
public final class PanelConsultas extends JPanel {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Supplier<ConfiguracionConexion> configuracionProveedor;
    private final ConsultaService consultaService;
    private final Consumer<String> notificador;

    private final JTextField campoIdSimulacion = new JTextField(8);
    private final JTextField campoUsuario = new JTextField(10);
    private final JComboBox<String> comboRuta = new JComboBox<>(new String[]{"Todas", "T1", "T2", "T3"});
    private final JComboBox<String> comboAccion = new JComboBox<>(new String[]{"Todas", "ENTRADA", "SALIDA"});
    private final JButton botonConsultar = new JButton("Consultar");
    private final JButton botonSimulaciones = new JButton("Ver simulaciones");
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Simulación", "Usuario", "Ruta", "Estación", "Acción", "Fecha y hora"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public PanelConsultas(
            Supplier<ConfiguracionConexion> configuracionProveedor,
            ConsultaService consultaService,
            Consumer<String> notificador
    ) {
        this.configuracionProveedor = Objects.requireNonNull(configuracionProveedor);
        this.consultaService = Objects.requireNonNull(consultaService);
        this.notificador = Objects.requireNonNull(notificador);
        construirInterfaz();
        botonConsultar.addActionListener(evento -> consultar());
        botonSimulaciones.addActionListener(evento -> listarSimulaciones());
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setBorder(BorderFactory.createTitledBorder("Filtros de consulta"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; filtros.add(new JLabel("ID simulación:"), c);
        c.gridx = 1; filtros.add(campoIdSimulacion, c);
        c.gridx = 2; filtros.add(new JLabel("Usuario:"), c);
        c.gridx = 3; filtros.add(campoUsuario, c);
        c.gridx = 0; c.gridy = 1; filtros.add(new JLabel("Ruta:"), c);
        c.gridx = 1; filtros.add(comboRuta, c);
        c.gridx = 2; filtros.add(new JLabel("Acción:"), c);
        c.gridx = 3; filtros.add(comboAccion, c);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(botonSimulaciones);
        acciones.add(botonConsultar);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 4;
        filtros.add(acciones, c);

        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        add(filtros, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    public void seleccionarSimulacion(long idSimulacion) {
        campoIdSimulacion.setText(Long.toString(idSimulacion));
    }

    private ConfiguracionConexion obtenerConfiguracion() {
        return configuracionProveedor.get();
    }

    private void listarSimulaciones() {
        ConfiguracionConexion configuracion;
        try {
            configuracion = obtenerConfiguracion();
        } catch (IllegalArgumentException excepcion) {
            notificador.accept(excepcion.getMessage());
            return;
        }
        botonSimulaciones.setEnabled(false);
        new SwingWorker<List<Simulacion>, Void>() {
            @Override
            protected List<Simulacion> doInBackground() throws Exception {
                return consultaService.listarSimulaciones(configuracion);
            }

            @Override
            protected void done() {
                botonSimulaciones.setEnabled(true);
                configuracion.limpiarClave();
                try {
                    List<Simulacion> simulaciones = get();
                    if (simulaciones.isEmpty()) {
                        notificador.accept("No hay simulaciones guardadas.");
                    } else {
                        StringBuilder texto = new StringBuilder("Simulaciones disponibles:\n");
                        simulaciones.forEach(simulacion -> texto.append("#")
                                .append(simulacion.idSimulacion())
                                .append(" | ").append(simulacion.fechaCreacion())
                                .append(" | ").append(simulacion.totalRegistros())
                                .append(" registros\n"));
                        notificador.accept(texto.toString());
                        campoIdSimulacion.setText(Long.toString(simulaciones.get(0).idSimulacion()));
                    }
                } catch (Exception excepcion) {
                    Throwable causa = excepcion.getCause() == null ? excepcion : excepcion.getCause();
                    notificador.accept("Error consultando simulaciones: " + causa.getMessage());
                }
            }
        }.execute();
    }

    private void consultar() {
        final long idSimulacion;
        try {
            idSimulacion = Long.parseLong(campoIdSimulacion.getText().trim());
            if (idSimulacion <= 0) throw new NumberFormatException();
        } catch (NumberFormatException excepcion) {
            notificador.accept("Ingrese un ID de simulación válido o seleccione 'Ver simulaciones'.");
            return;
        }

        String usuario = campoUsuario.getText().trim();
        String ruta = "Todas".equals(comboRuta.getSelectedItem()) ? null : (String) comboRuta.getSelectedItem();
        String accionSeleccionada = (String) comboAccion.getSelectedItem();
        Accion accion = "Todas".equals(accionSeleccionada) ? null : Accion.valueOf(accionSeleccionada);

        ConfiguracionConexion configuracion;
        try {
            configuracion = obtenerConfiguracion();
        } catch (IllegalArgumentException excepcion) {
            notificador.accept(excepcion.getMessage());
            return;
        }
        botonConsultar.setEnabled(false);
        notificador.accept("Consultando registros en MySQL...");

        new SwingWorker<List<RegistroPersistido>, Void>() {
            @Override
            protected List<RegistroPersistido> doInBackground() throws Exception {
                return consultaService.consultarRegistros(
                        configuracion, idSimulacion, usuario, ruta, accion, 500);
            }

            @Override
            protected void done() {
                botonConsultar.setEnabled(true);
                configuracion.limpiarClave();
                try {
                    List<RegistroPersistido> registros = get();
                    modeloTabla.setRowCount(0);
                    registros.forEach(fila -> {
                        var registro = fila.registro();
                        modeloTabla.addRow(new Object[]{
                                fila.idRegistro(), fila.idSimulacion(), registro.idUsuario(),
                                registro.ruta(), registro.estacion(), registro.accion(),
                                registro.timestamp().format(FORMATO_FECHA)
                        });
                    });
                    notificador.accept("Consulta terminada. Se muestran " + registros.size()
                            + " registros (máximo 500 por consulta).");
                } catch (Exception excepcion) {
                    Throwable causa = excepcion.getCause() == null ? excepcion : excepcion.getCause();
                    notificador.accept("Error consultando registros: " + causa.getMessage());
                }
            }
        }.execute();
    }
}
