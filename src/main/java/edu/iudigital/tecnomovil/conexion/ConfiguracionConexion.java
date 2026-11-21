package edu.iudigital.tecnomovil.conexion;

import java.util.Arrays;
import java.util.Objects;

/** Datos de conexión recibidos desde los campos de la interfaz. */
public final class ConfiguracionConexion {
    private final String url;
    private final String usuario;
    private final char[] clave;

    public ConfiguracionConexion(String url, String usuario, char[] clave) {
        this.url = Objects.requireNonNull(url, "La URL es obligatoria").trim();
        this.usuario = Objects.requireNonNull(usuario, "El usuario es obligatorio").trim();
        this.clave = clave == null ? new char[0] : clave.clone();
    }

    public String url() {
        return url;
    }

    public String usuario() {
        return usuario;
    }

    /** Devuelve una copia para no exponer el arreglo interno. */
    public char[] clave() {
        return clave.clone();
    }

    public void limpiarClave() {
        Arrays.fill(clave, '\0');
    }
}
