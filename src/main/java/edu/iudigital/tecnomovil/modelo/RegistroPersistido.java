package edu.iudigital.tecnomovil.modelo;

/** Registro del dominio acompañado por sus identificadores en la base de datos. */
public record RegistroPersistido(
        long idRegistro,
        long idSimulacion,
        Registro registro
) {
}
