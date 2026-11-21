package edu.iudigital.tecnomovil.servicio;

import edu.iudigital.tecnomovil.conexion.ConfiguracionConexion;
import edu.iudigital.tecnomovil.datos.GeneradorRegistros;
import edu.iudigital.tecnomovil.modelo.Registro;
import edu.iudigital.tecnomovil.modelo.Simulacion;
import edu.iudigital.tecnomovil.persistencia.RegistroDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/** Coordina la generación de datos sintéticos y su almacenamiento en MySQL. */
public final class SimulacionService {
    private final RegistroDAO registroDAO;

    public SimulacionService(RegistroDAO registroDAO) {
        this.registroDAO = Objects.requireNonNull(registroDAO);
    }

    public Simulacion generarYGuardar(
            ConfiguracionConexion configuracion, int totalUsuarios, long semilla
    ) throws ClassNotFoundException, SQLException {
        if (totalUsuarios < 1 || totalUsuarios > 100_000) {
            throw new IllegalArgumentException(
                    "La cantidad debe estar entre 1 y 100.000 usuarios."
            );
        }

        List<Registro> registros = GeneradorRegistros.generar(totalUsuarios, semilla);
        long idSimulacion = registroDAO.guardarSimulacion(
                configuracion, totalUsuarios, semilla, registros);

        return new Simulacion(
                idSimulacion,
                LocalDateTime.now(),
                totalUsuarios,
                registros.size(),
                semilla
        );
    }
}
