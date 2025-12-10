-- =====================================================================
--  V-1 - PostgreSQL
-- Nome do banco: servico-de-rotas
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    criado_em TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE rotas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID REFERENCES usuarios(id),
    nome TEXT NOT NULL,
    descricao TEXT,
    criado_em TIMESTAMPTZ DEFAULT now(),
    atualizado_em TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_rotas_owner ON rotas(owner_id);

CREATE TABLE waypoints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_rota UUID REFERENCES rotas(id) ON DELETE CASCADE,
    seq INTEGER NOT NULL,
    nome TEXT,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    eta TIMESTAMPTZ,
    criado_em TIMESTAMPTZ DEFAULT now(),

    UNIQUE(id_rota, seq)
);

CREATE INDEX idx_waypoints_rota ON waypoints(id_rota);
CREATE INDEX idx_waypoints_lat ON waypoints(latitude);
CREATE INDEX idx_waypoints_lon ON waypoints(longitude);

CREATE TABLE instancias_rota (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_rota UUID REFERENCES rotas(id),
    id_veiculo TEXT,
    id_atual_waypoint UUID,
    iniciado_em TIMESTAMPTZ,
    finalizado_em TIMESTAMPTZ,
    status TEXT CHECK (status IN ('planned','running','finished','cancelled')) DEFAULT 'planned',
    criado_em TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_instancias_rota_rota ON instancias_rota(id_rota);
CREATE INDEX idx_instancias_rota_status ON instancias_rota(status);

CREATE TABLE eventos_rastreamento (
    id BIGSERIAL PRIMARY KEY,
    instancia_rota_id UUID REFERENCES instancias_rota(id) ON DELETE CASCADE,
    gravado_em TIMESTAMPTZ NOT NULL,
    id_dispositivo TEXT,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    velocidade REAL,
    heading REAL,
    metadata JSONB,
    criado_em TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_hora_rastreio_rota ON eventos_rastreamento(instancia_rota_id, gravado_em DESC);
CREATE INDEX idx_rastreio_lat ON eventos_rastreamento(latitude);
CREATE INDEX idx_rastreio_lon ON eventos_rastreamento(longitude);

CREATE TABLE waypoint_visits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instancia_rota_id UUID REFERENCES instancias_rota(id) ON DELETE CASCADE,
    id_waypoint UUID REFERENCES waypoints(id) ON DELETE CASCADE,
    chegou_em TIMESTAMPTZ NOT NULL,
    criado_por TEXT,
    distancia_metros REAL,
    criado_em TIMESTAMPTZ DEFAULT now(),

    UNIQUE(instancia_rota_id, id_waypoint)
);

CREATE INDEX idx_visits_route_instance ON waypoint_visits(instancia_rota_id);
CREATE INDEX idx_visits_waypoint ON waypoint_visits(id_waypoint);

CREATE TABLE alertas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instancia_rota_id UUID REFERENCES instancias_rota(id),
    id_waypoint UUID REFERENCES waypoints(id),
    tipo TEXT CHECK (tipo IN ('delay','deviation','missed','other')),
    message TEXT,
    criado_em TIMESTAMPTZ DEFAULT now(),
    resolvido BOOLEAN DEFAULT FALSE,
    resolvido_em TIMESTAMPTZ
);

CREATE INDEX idx_alertas_instancia_rota ON alertas(instancia_rota_id);
CREATE INDEX idx_alertas_waypoint ON alertas(id_waypoint);
CREATE INDEX idx_alertas_tipo ON alertas(tipo);

CREATE OR REPLACE VIEW v_atraso_de_rotas AS
SELECT 
    r.id AS id_rota,
    ri.id AS instancia_rota_id,
    wp.id AS id_waypoint,
    wp.seq,
    wp.nome,
    wp.eta,
    wv.chegou_em,
    (wv.chegou_em - wp.eta) AS delay_interval,
    a.id AS alert_id
FROM instancias_rota ri
JOIN rotas r ON ri.id_rota = r.id
JOIN waypoints wp ON wp.id_rota = r.id
LEFT JOIN waypoint_visits wv 
    ON wv.instancia_rota_id = ri.id AND wv.id_waypoint = wp.id
LEFT JOIN alertas a 
    ON a.instancia_rota_id = ri.id 
   AND a.id_waypoint = wp.id 
   AND a.tipo = 'delay';

CREATE TABLE route_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_rota UUID REFERENCES rotas(id),
    instancia_rota_id UUID REFERENCES instancias_rota(id),
    status TEXT CHECK (status IN ('pending','running','done','failed','applied')) DEFAULT 'pending',
    solver TEXT,
    objective TEXT,
    params JSONB,
    total_distance_m REAL,
    total_time_seconds INT,
    plan_json JSONB,
    error_message TEXT,
    criado_em TIMESTAMPTZ DEFAULT now(),
    atualizado_em TIMESTAMPTZ DEFAULT now(),
    applied_at TIMESTAMPTZ
);

CREATE INDEX idx_route_plans_instancia ON route_plans(instancia_rota_id);
CREATE INDEX idx_route_plans_rota ON route_plans(id_rota);

CREATE TABLE plan_waypoints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_plan_id UUID REFERENCES route_plans(id) ON DELETE CASCADE,
    seq INTEGER,
    ref TEXT,
    type TEXT CHECK (type IN ('existing','inline')) NOT NULL,
    nome TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    eta TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_plan_waypoints_plan ON plan_waypoints(route_plan_id);
CREATE INDEX idx_plan_waypoints_plan_seq ON plan_waypoints(route_plan_id, seq);
