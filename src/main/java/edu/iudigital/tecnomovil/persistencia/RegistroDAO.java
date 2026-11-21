package edu.iudigital.tecnomovil.persistencia;

import edu.iudigital.tecnomovil.conexion.ConfiguracionConexion;
import edu.iudigital.tecnomovil.conexion.ConexionMySQL;
import edu.iudigital.tecnomovil.modelo.Accion;
import edu.iudigital.tecnomovil.modelo.Registro;
import edu.iudigital.tecnomovil.modelo.RegistroPersistido;
import edu.iudigital.tecnomovil.modelo.Simulacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Acceso a datos: concentra las consultas e inserciones SQL del proyecto. */
public final class RegistroDAO {
    private static final int TAMANO_LOTE = 500;
    private final ConexionMySQL conexionMySQL;

    public RegistroDAO(ConexionMySQL conexionMySQL) {
        this.conexionMySQL = Objects.requireNonNull(conexionMySQL);
    }

    /** Guarda la simulación y todos sus registros en una sola transacción. */
    public long guardarSimulacion(
            ConfiguracionConexion configuracion,
            int totalUsuarios,
            long semilla,
            List<Registro> registros
    ) throws ClassNotFoundException, SQLException {
        Objects.requireNonNull(registros, "La lista de registros es obligatoria");
        if (totalUsuarios < 1 || registros.isEmpty()) {
            throw new IllegalArgumentException("La simulación debe contener usuarios y registros.");
        }

        try (Connection conexion = conexionMySQL.abrirConexion(configuracion)) {
            conexion.setAutoCommit(false);
            try {
                long idSimulacion = insertarSimulacion(
                        conexion, totalUsuarios, registros.size(), semilla);
                insertarRegistros(conexion, idSimulacion, registros);
                conexion.commit();
                return idSimulacion;
            } catch (SQLException | RuntimeException excepcion) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    excepcion.addSuppressed(errorRollback);
                }
                throw excepcion;
            }
        }
    }

    private long insertarSimulacion(
            Connection conexion, int usuarios, int cantidadRegistros, long semilla
    ) throws SQLException {
        String sql = "INSERT INTO simulaciones "
                + "(total_usuarios, total_registros, semilla) VALUES (?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, usuarios);
            sentencia.setInt(2, cantidadRegistros);
            sentencia.setLong(3, semilla);
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getLong(1);
                }
                throw new SQLException("MySQL no devolvió el identificador de la simulación.");
            }
        }
    }

    private void insertarRegistros(
            Connection conexion, long idSimulacion, List<Registro> registros
    ) throws SQLException {
        String sql = "INSERT INTO registros "
                + "(id_simulacion, id_usuario, ruta, estacion, accion, fecha_evento) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            int pendientes = 0;
            for (Registro registro : registros) {
                sentencia.setLong(1, idSimulacion);
                sentencia.setString(2, registro.idUsuario());
                sentencia.setString(3, registro.ruta());
                sentencia.setString(4, registro.estacion());
                sentencia.setString(5, registro.accion().name());
                sentencia.setTimestamp(6, Timestamp.valueOf(registro.timestamp()));
                sentencia.addBatch();
                pendientes++;

                if (pendientes == TAMANO_LOTE) {
                    sentencia.executeBatch();
                    sentencia.clearBatch();
                    pendientes = 0;
                }
            }
            if (pendientes > 0) {
                sentencia.executeBatch();
            }
        }
    }

    public List<Simulacion> listarSimulaciones(ConfiguracionConexion configuracion)
            throws ClassNotFoundException, SQLException {
        String sql = "SELECT id_simulacion, fecha_creacion, total_usuarios, "
                + "total_registros, semilla FROM simulaciones "
                + "ORDER BY id_simulacion DESC LIMIT 100";
        List<Simulacion> simulaciones = new ArrayList<>();

        try (Connection conexion = conexionMySQL.abrirConexion(configuracion);
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                Timestamp fecha = resultado.getTimestamp("fecha_creacion");
                simulaciones.add(new Simulacion(
                        resultado.getLong("id_simulacion"),
                        fecha.toLocalDateTime(),
                        resultado.getInt("total_usuarios"),
                        resultado.getInt("total_registros"),
                        resultado.getLong("semilla")
                ));
            }
        }
        return List.copyOf(simulaciones);
    }

    /** Consulta hasta el límite indicado; los filtros opcionales se enlazan como parámetros. */
    public List<RegistroPersistido> consultarRegistros(
            ConfiguracionConexion configuracion,
            long idSimulacion,
            String idUsuario,
            String ruta,
            Accion accion,
            int limite
    ) throws ClassNotFoundException, SQLException {
        if (idSimulacion <= 0) {
            throw new IllegalArgumentException("Seleccione una simulación válida.");
        }
        int limiteSeguro = Math.max(1, Math.min(limite, 2_000));
        StringBuilder sql = new StringBuilder("SELECT id_registro, id_simulacion, "
                + "id_usuario, ruta, estacion, accion, fecha_evento "
                + "FROM registros WHERE id_simulacion = ?");
        List<Object> parametros = new ArrayList<>();
        parametros.add(idSimulacion);

        if (idUsuario != null && !idUsuario.isBlank()) {
            sql.append(" AND id_usuario LIKE ?");
            parametros.add("%" + idUsuario.trim() + "%");
        }
        if (ruta != null && !ruta.isBlank()) {
            sql.append(" AND ruta = ?");
            parametros.add(ruta);
        }
        if (accion != null) {
            sql.append(" AND accion = ?");
            parametros.add(accion.name());
        }
        sql.append(" ORDER BY fecha_evento, id_registro LIMIT ?");
        parametros.add(limiteSeguro);

        List<RegistroPersistido> registros = new ArrayList<>();
        try (Connection conexion = conexionMySQL.abrirConexion(configuracion);
             PreparedStatement sentencia = conexion.prepareStatement(sql.toString())) {
            for (int indice = 0; indice < parametros.size(); indice++) {
                Object valor = parametros.get(indice);
                if (valor instanceof Long numero) {
                    sentencia.setLong(indice + 1, numero);
                } else {
                    sentencia.setString(indice + 1, valor.toString());
                }
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    registros.add(mapearRegistro(resultado));
                }
            }
        }
        return List.copyOf(registros);
    }

    /** Carga todos los eventos de una simulación para ejecutar los análisis funcionales. */
    public List<Registro> obtenerRegistrosDeSimulacion(
            ConfiguracionConexion configuracion, long idSimulacion
    ) throws ClassNotFoundException, SQLException {
        String sql = "SELECT id_usuario, ruta, estacion, accion, fecha_evento "
                + "FROM registros WHERE id_simulacion = ? "
                + "ORDER BY fecha_evento, id_registro";
        List<Registro> registros = new ArrayList<>();

        try (Connection conexion = conexionMySQL.abrirConexion(configuracion);
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idSimulacion);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    registros.add(new Registro(
                            resultado.getString("id_usuario"),
                            resultado.getString("ruta"),
                            resultado.getString("estacion"),
                            Accion.valueOf(resultado.getString("accion")),
                            resultado.getTimestamp("fecha_evento").toLocalDateTime()
                    ));
                }
            }
        }
        return List.copyOf(registros);
    }

    private RegistroPersistido mapearRegistro(ResultSet resultado) throws SQLException {
        Registro registro = new Registro(
                resultado.getString("id_usuario"),
                resultado.getString("ruta"),
                resultado.getString("estacion"),
                Accion.valueOf(resultado.getString("accion")),
                resultado.getTimestamp("fecha_evento").toLocalDateTime()
        );
        return new RegistroPersistido(
                resultado.getLong("id_registro"),
                resultado.getLong("id_simulacion"),
                registro
        );
    }
}
