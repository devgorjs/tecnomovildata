package edu.iudigital.tecnomovil.servicio;

import edu.iudigital.tecnomovil.conexion.ConfiguracionConexion;
import edu.iudigital.tecnomovil.modelo.Accion;
import edu.iudigital.tecnomovil.modelo.Registro;
import edu.iudigital.tecnomovil.modelo.RegistroPersistido;
import edu.iudigital.tecnomovil.modelo.Simulacion;
import edu.iudigital.tecnomovil.persistencia.RegistroDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Servicio de consultas para las pantallas de registros y simulaciones. */
public final class ConsultaService {
    private final RegistroDAO registroDAO;

    public ConsultaService(RegistroDAO registroDAO) {
        this.registroDAO = Objects.requireNonNull(registroDAO);
    }

    public List<Simulacion> listarSimulaciones(ConfiguracionConexion configuracion)
            throws ClassNotFoundException, SQLException {
        return registroDAO.listarSimulaciones(configuracion);
    }

    public List<RegistroPersistido> consultarRegistros(
            ConfiguracionConexion configuracion,
            long idSimulacion,
            String idUsuario,
            String ruta,
            Accion accion,
            int limite
    ) throws ClassNotFoundException, SQLException {
        return registroDAO.consultarRegistros(
                configuracion, idSimulacion, idUsuario, ruta, accion, limite);
    }

    public List<Registro> obtenerRegistrosDeSimulacion(
            ConfiguracionConexion configuracion, long idSimulacion
    ) throws ClassNotFoundException, SQLException {
        return registroDAO.obtenerRegistrosDeSimulacion(configuracion, idSimulacion);
    }
}
