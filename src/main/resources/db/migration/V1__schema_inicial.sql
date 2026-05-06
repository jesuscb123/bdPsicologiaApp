-- Esquema inicial PostgreSQL alineado con las entidades JPA del proyecto.
-- Convención de nombres físicos en minúsculas (Spring Boot / Hibernate por defecto).
-- Aplica esta migración solo contra una base vacía (nuevo Postgres en Render).

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    firebase_uid VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    foto_perfil TEXT
);

CREATE TABLE psicologos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuarios (id),
    numero_colegiado VARCHAR(255) NOT NULL UNIQUE,
    especialidad VARCHAR(255) NOT NULL,
    descripcion VARCHAR(1000)
);

CREATE TABLE pacientes_v2 (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES usuarios (id),
    psicologo_id BIGINT REFERENCES psicologos (id)
);

CREATE TABLE notas (
    id BIGSERIAL PRIMARY KEY,
    asunto VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    paciente_id BIGINT NOT NULL REFERENCES pacientes_v2 (id),
    psicologo_id BIGINT NOT NULL REFERENCES psicologos (id),
    ultima_modificacion TIMESTAMP(6) NOT NULL
);

CREATE TABLE tareas (
    id BIGSERIAL PRIMARY KEY,
    titulo_tarea VARCHAR(255) NOT NULL,
    descripcion_tarea VARCHAR(255) NOT NULL,
    hora_envio TIMESTAMP(6) NOT NULL,
    realizada BOOLEAN NOT NULL DEFAULT FALSE,
    aceptada_por_paciente BOOLEAN NOT NULL DEFAULT FALSE,
    psicologo_id BIGINT NOT NULL REFERENCES psicologos (id),
    paciente_id BIGINT NOT NULL REFERENCES pacientes_v2 (id),
    ultima_modificacion TIMESTAMP(6) NOT NULL
);

CREATE TABLE citas (
    id BIGSERIAL PRIMARY KEY,
    psicologo_id BIGINT NOT NULL REFERENCES psicologos (id),
    paciente_id BIGINT NOT NULL REFERENCES pacientes_v2 (id),
    inicio TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    duracion_minutos INTEGER NOT NULL,
    estado VARCHAR(255) NOT NULL,
    ultima_modificacion TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_citas_psicologo_inicio UNIQUE (psicologo_id, inicio)
);

CREATE TABLE fcm_tokens (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios (id),
    token VARCHAR(4096) NOT NULL UNIQUE,
    instalacion_id VARCHAR(256),
    plataforma VARCHAR(32) NOT NULL DEFAULT 'ANDROID',
    creado_en TIMESTAMP(6) NOT NULL,
    actualizado_en TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_fcm_token_usuario ON fcm_tokens (usuario_id);
