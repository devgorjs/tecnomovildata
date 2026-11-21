package edu.iudigital.tecnomovil.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

/** Centraliza la apertura y la comprobación de conexiones JDBC con MySQL. */
public final class ConexionMySQL {
    private static final String CLASE_DRIVER = "com.mysql.cj.jdbc.Driver";

    public Connection abrirConexion(ConfiguracionConexion configuracion)
            throws ClassNotFoundException, SQLException {
        Objects.requireNonNull(configuracion, "La configuración es obligatoria");

        String url = configuracion.url();
        String usuario = configuracion.usuario();
        char[] clave = configuracion.clave();

        if (url.isBlank() || !url.startsWith("jdbc:mysql://")) {
            Arrays.fill(clave, '\0');
            throw new IllegalArgumentException(
                    "La URL debe comenzar con jdbc:mysql:// y contener la dirección del servidor."
            );
        }
        if (usuario.isBlank()) {
            Arrays.fill(clave, '\0');
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }

        try {
            Class.forName(CLASE_DRIVER);
            return DriverManager.getConnection(url, usuario, new String(clave));
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

    /** Prueba la conexión y la cierra automáticamente al salir del método. */
    public void probarConexion(ConfiguracionConexion configuracion)
            throws ClassNotFoundException, SQLException {
        try (Connection conexion = abrirConexion(configuracion)) {
            if (!conexion.isValid(5)) {
                throw new SQLException("MySQL no confirmó que la conexión esté activa.");
            }
        }
    }
}
