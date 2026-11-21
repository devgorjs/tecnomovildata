-- Consultas sencillas para verificar la información guardada por TecnoMovil Data.
USE tecnomovil_data;

-- Resumen de las simulaciones guardadas.
SELECT id_simulacion, fecha_creacion, total_usuarios, total_registros, semilla
FROM simulaciones
ORDER BY id_simulacion DESC;

-- Cantidad real de registros por simulación.
SELECT id_simulacion, COUNT(*) AS registros_guardados
FROM registros
GROUP BY id_simulacion
ORDER BY id_simulacion DESC;

-- Muestra de registros recientes de la simulación indicada.
-- Reemplace 1 por el ID que muestre la aplicación.
SELECT id_registro, id_simulacion, id_usuario, ruta, estacion, accion, fecha_evento
FROM registros
WHERE id_simulacion = 1
ORDER BY fecha_evento, id_registro
LIMIT 50;
