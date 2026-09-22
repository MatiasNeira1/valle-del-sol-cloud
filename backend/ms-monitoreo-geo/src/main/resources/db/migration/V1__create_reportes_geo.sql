CREATE TABLE IF NOT EXISTS reportes_geo (
    id_original BIGINT PRIMARY KEY,
    titulo VARCHAR(255),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION
);
