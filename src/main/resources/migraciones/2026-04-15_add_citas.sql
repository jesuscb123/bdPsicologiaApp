-- Migración manual para Postgres (Render/producción).
-- Crea tabla CITAS con constraint único (psicologo_id, inicio).

CREATE TABLE IF NOT EXISTS CITAS (
    id BIGSERIAL PRIMARY KEY,
    psicologo_id BIGINT NOT NULL,
    paciente_id BIGINT NOT NULL,
    inicio TIMESTAMPTZ NOT NULL,
    duracion_minutos INT NOT NULL DEFAULT 60,
    estado VARCHAR(30) NOT NULL DEFAULT 'RESERVADA',
    ultima_modificacion TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_citas_psicologo_inicio UNIQUE (psicologo_id, inicio),
    CONSTRAINT fk_citas_psicologo FOREIGN KEY (psicologo_id) REFERENCES PSICOLOGOS(id),
    CONSTRAINT fk_citas_paciente FOREIGN KEY (paciente_id) REFERENCES PACIENTES_v2(id)
);

-- Índice de apoyo para listados por paciente/psicólogo.
CREATE INDEX IF NOT EXISTS idx_citas_paciente_id ON CITAS(paciente_id);
CREATE INDEX IF NOT EXISTS idx_citas_psicologo_id ON CITAS(psicologo_id);

