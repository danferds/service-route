CREATE TYPE rota_status AS ENUM (
    'CRIADA',
    'PLANEJANDO',
    'PLANEJADA',
    'ERRO'
);

CREATE TYPE rota_waypoint_tipo AS ENUM (
    'PICKUP',
    'DELIVERY',
    'PARADA',
    'ORIGEM',
    'DESTINO'
);

CREATE TYPE alerta_tipo AS ENUM (
    'ATRASO',
    'INFO',
    'ERRO'
);

CREATE TABLE cliente (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);

CREATE TABLE rota (
    id UUID PRIMARY KEY,
    id_cliente UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP NOT NULL DEFAULT NOW(),
    status rota_status NOT NULL DEFAULT 'CRIADA',
    tempo_estimado_total INTEGER,

    CONSTRAINT fk_rota_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES cliente(id)
);

CREATE TABLE waypoint (
    id UUID PRIMARY KEY,
    longitude DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    endereco VARCHAR(500)
);

CREATE TABLE rota_waypoint (
    id UUID PRIMARY KEY,
    id_rota UUID NOT NULL,
    id_waypoint UUID NOT NULL,
    tipo rota_waypoint_tipo NOT NULL,
    seq INTEGER,
    eta_previsto TIMESTAMP,

    CONSTRAINT fk_rota_waypoint_rota
        FOREIGN KEY (id_rota)
        REFERENCES rota(id),

    CONSTRAINT fk_rota_waypoint_waypoint
        FOREIGN KEY (id_waypoint)
        REFERENCES waypoint(id),

    CONSTRAINT uq_rota_seq
        UNIQUE (id_rota, seq)
);

CREATE TABLE viagem (
    id UUID PRIMARY KEY,
    id_rota UUID NOT NULL,
    veiculo_ref VARCHAR(255),
    tempo_real_total INTEGER,
    data_inicio TIMESTAMP,
    data_fim TIMESTAMP,

    CONSTRAINT fk_viagem_rota
        FOREIGN KEY (id_rota)
        REFERENCES rota(id)
);

CREATE TABLE visitas (
    id UUID PRIMARY KEY,
    id_viagem UUID NOT NULL,
    id_rota_waypoint UUID NOT NULL,
    eta_real TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_visita_viagem
        FOREIGN KEY (id_viagem)
        REFERENCES viagem(id),

    CONSTRAINT fk_visita_rota_waypoint
        FOREIGN KEY (id_rota_waypoint)
        REFERENCES rota_waypoint(id)
);

CREATE TABLE alertas (
    id UUID PRIMARY KEY,
    id_visita UUID NOT NULL,
    texto TEXT NOT NULL,
    tipo alerta_tipo NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_alerta_visita
        FOREIGN KEY (id_visita)
        REFERENCES visitas(id)
);

CREATE INDEX idx_rota_id_cliente ON rota(id_cliente);
CREATE INDEX idx_rota_waypoint_rota ON rota_waypoint(id_rota);
CREATE INDEX idx_visitas_viagem ON visitas(id_viagem);
CREATE INDEX idx_alertas_tipo ON alertas(tipo);
