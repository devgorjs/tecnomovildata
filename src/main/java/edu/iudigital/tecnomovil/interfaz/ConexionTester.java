package edu.iudigital.tecnomovil.interfaz;

import edu.iudigital.tecnomovil.conexion.ConfiguracionConexion;
import edu.iudigital.tecnomovil.conexion.ConexionMySQL;
import edu.iudigital.tecnomovil.persistencia.RegistroDAO;
import edu.iudigital.tecnomovil.servicio.ConsultaService;
import edu.iudigital.tecnomovil.servicio.EstadisticasService;
import edu.iudigital.tecnomovil.servicio.SimulacionService;
import edu.iudigital.tecnomovil.modelo.Simulacion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/** Ventana principal de Swing. Incluye el formulario exigido para probar JDBC. */
@SuppressWarnings("serial")
public final class ConexionTester extends JFrame {
    private static final Color VERDE_EXITO = new Color(0, 128, 64);
    private static final Color ROJO_ERROR = new Color(180, 35, 35);
    private static final Color AZUL_TITULO = new Color(22, 105, 150);

    private final JTextField campoUrl = new JTextField(
            "jdbc:mysql://localhost:3306/tecnomovil_data", 30);
    private final JTextField campoUsuario = new JTextField("root", 18);
    private final JPasswordField campoContrasena = new JPasswordField(18);
    private final JButton botonProbar = new JButton("Probar conexión");
    private final JTextArea areaResultado = new JTextArea(4, 50);

    private final ConexionMySQL conexionMySQL = new ConexionMySQL();
    private final SimulacionService simulacionService;
    private final ConsultaService consultaService;
    private final PanelConsultas panelConsultas;
    private final PanelEstadisticas panelEstadisticas;

    public ConexionTester() {
        super("TecnoMovil Data - Gestión de simulaciones");
        RegistroDAO registroDAO = new RegistroDAO(conexionMySQL);
        this.simulacionService = new SimulacionService(registroDAO);
        this.consultaService = new ConsultaService(registroDAO);
        EstadisticasService estadisticasService = new EstadisticasService(consultaService);
        this.panelConsultas = new PanelConsultas(
                this::obtenerConfiguracion, consultaService, this::mostrarMensaje);
        this.panelEstadisticas = new PanelEstadisticas(
                this::obtenerConfiguracion, estadisticasService, this::mostrarMensaje);

        construirInterfaz();
        configurarEventos();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 680));
        setLayout(new BorderLayout(10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout(8, 8));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(12, 14, 4, 14));
        JLabel titulo = new JLabel("TecnoMovil Data | Gestión de transporte urbano");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 19));
        titulo.setForeground(AZUL_TITULO);
        panelSuperior.add(titulo, BorderLayout.NORTH);
        panelSuperior.add(crearFormularioConexion(), BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        PanelGeneracion panelGeneracion = new PanelGeneracion(
                this::obtenerConfiguracion,
                simulacionService,
                this::mostrarMensaje,
                this::seleccionarSimulacion
        );

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Generar y guardar", panelGeneracion);
        pestanas.addTab("Consultar registros", panelConsultas);
        pestanas.addTab("Estadísticas", panelEstadisticas);
        add(pestanas, BorderLayout.CENTER);

        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        areaResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        areaResultado.setText("Ingrese los datos de MySQL y pruebe la conexión. "
                + "Después podrá generar y consultar simulaciones.\n");
        JScrollPane desplazamiento = new JScrollPane(areaResultado);
        desplazamiento.setBorder(BorderFactory.createTitledBorder("Mensajes de la aplicación"));
        desplazamiento.setPreferredSize(new Dimension(900, 105));
        JPanel panelMensajes = new JPanel(new BorderLayout());
        panelMensajes.setBorder(BorderFactory.createEmptyBorder(0, 14, 12, 14));
        panelMensajes.add(desplazamiento, BorderLayout.CENTER);
        add(panelMensajes, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel crearFormularioConexion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Conexión a MySQL"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("URL:"), c);
        c.gridx = 1; c.weightx = 1; c.gridwidth = 5; panel.add(campoUrl, c);
        c.gridwidth = 1; c.weightx = 0;
        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Usuario:"), c);
        c.gridx = 1; c.weightx = 1; panel.add(campoUsuario, c);
        c.gridx = 2; c.weightx = 0; panel.add(new JLabel("Contraseña:"), c);
        c.gridx = 3; c.weightx = 1; panel.add(campoContrasena, c);
        c.gridx = 4; c.weightx = 0; panel.add(botonProbar, c);
        return panel;
    }

    private void configurarEventos() {
        botonProbar.addActionListener(evento -> probarConexion());
    }

    private ConfiguracionConexion obtenerConfiguracion() {
        return new ConfiguracionConexion(
                campoUrl.getText(),
                campoUsuario.getText(),
                campoContrasena.getPassword()
        );
    }

    private void probarConexion() {
        ConfiguracionConexion configuracion;
        try {
            configuracion = obtenerConfiguracion();
        } catch (IllegalArgumentException excepcion) {
            mostrarMensaje(excepcion.getMessage(), ROJO_ERROR);
            return;
        }

        botonProbar.setEnabled(false);
        mostrarMensaje("Intentando conectar con MySQL...", Color.DARK_GRAY);
        new SwingWorker<ResultadoPruebaConexion, Void>() {
            @Override
            protected ResultadoPruebaConexion doInBackground() {
                try {
                    conexionMySQL.probarConexion(configuracion);
                    return new ResultadoPruebaConexion(true,
                            "CONEXIÓN EXITOSA. La conexión de prueba se cerró correctamente.");
                } catch (ClassNotFoundException excepcion) {
                    return new ResultadoPruebaConexion(false,
                            "FALLO DE CONEXIÓN: No se encontró el controlador JDBC de MySQL. "
                                    + excepcion.getMessage());
                } catch (SQLException excepcion) {
                    return new ResultadoPruebaConexion(false,
                            "FALLO DE CONEXIÓN: " + excepcion.getMessage());
                } catch (IllegalArgumentException excepcion) {
                    return new ResultadoPruebaConexion(false,
                            "DATOS DE CONEXIÓN NO VÁLIDOS: " + excepcion.getMessage());
                }
            }

            @Override
            protected void done() {
                botonProbar.setEnabled(true);
                configuracion.limpiarClave();
                try {
                    ResultadoPruebaConexion resultado = get();
                    mostrarMensaje(resultado.mensaje(),
                            resultado.exitosa() ? VERDE_EXITO : ROJO_ERROR);
                } catch (InterruptedException excepcion) {
                    Thread.currentThread().interrupt();
                    mostrarMensaje("La prueba de conexión fue interrumpida.", ROJO_ERROR);
                } catch (ExecutionException excepcion) {
                    Throwable causa = excepcion.getCause() == null
                            ? excepcion : excepcion.getCause();
                    mostrarMensaje("Error inesperado al probar la conexión: "
                            + causa.getMessage(), ROJO_ERROR);
                }
            }
        }.execute();
    }

    private void seleccionarSimulacion(Simulacion simulacion) {
        panelConsultas.seleccionarSimulacion(simulacion.idSimulacion());
        panelEstadisticas.seleccionarSimulacion(simulacion.idSimulacion());
    }

    private void mostrarMensaje(String mensaje) {
        mostrarMensaje(mensaje, Color.DARK_GRAY);
    }

    private void mostrarMensaje(String mensaje, Color color) {
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(() -> mostrarMensaje(mensaje, color));
            return;
        }
        areaResultado.setForeground(color);
        areaResultado.setText(mensaje);
        areaResultado.setCaretPosition(0);
    }
    private record ResultadoPruebaConexion(boolean exitosa, String mensaje) {
    }

}
