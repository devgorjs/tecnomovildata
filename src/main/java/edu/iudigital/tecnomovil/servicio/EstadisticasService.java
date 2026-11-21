package edu.iudigital.tecnomovil.servicio;

import edu.iudigital.tecnomovil.analisis.AnalisisTransporte;
import edu.iudigital.tecnomovil.conexion.ConfiguracionConexion;
import edu.iudigital.tecnomovil.modelo.Registro;
import edu.iudigital.tecnomovil.modelo.ResumenEstadistico;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Recupera datos de MySQL y reutiliza las operaciones funcionales existentes. */
public final class EstadisticasService {
    private final ConsultaService consultaService;

    public EstadisticasService(ConsultaService consultaService) {
        this.consultaService = Objects.requireNonNull(consultaService);
    }

    public ResumenEstadistico generarResumen(
            ConfiguracionConexion configuracion,
            long idSimulacion,
            long umbralOcupacion
    ) throws ClassNotFoundException, SQLException {
        List<Registro> registros = consultaService.obtenerRegistrosDeSimulacion(
                configuracion, idSimulacion);
        if (registros.isEmpty()) {
            throw new IllegalArgumentException(
                    "La simulación no tiene registros o el identificador no existe."
            );
        }

        return new ResumenEstadistico(
                idSimulacion,
                registros.size(),
                AnalisisTransporte.afluenciaPorEstacion(registros),
                AnalisisTransporte.horasPico(registros, 5),
                AnalisisTransporte.rutasMasUtilizadas(registros, 3),
                AnalisisTransporte.patronesDeViaje(registros),
                AnalisisTransporte.tiempoPromedioEntreEstaciones(registros),
                AnalisisTransporte.rutasCriticas(registros, umbralOcupacion),
                umbralOcupacion
        );
    }
}
