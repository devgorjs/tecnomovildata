package edu.iudigital.tecnomovil;

import edu.iudigital.tecnomovil.interfaz.ConexionTester;

import javax.swing.SwingUtilities;

/** Punto de entrada gráfico de TecnoMovil Data. */
public class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConexionTester ventana = new ConexionTester();
            ventana.setVisible(true);
        });
    }
}
