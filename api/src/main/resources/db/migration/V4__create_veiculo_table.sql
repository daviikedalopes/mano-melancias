CREATE TABLE veiculo (
    id UUID PRIMARY KEY,
    placa VARCHAR(8) NOT NULL UNIQUE,
    cidade VARCHAR(255) NOT NULL,
    motorista_id UUID REFERENCES motorista (id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_veiculo_motorista ON veiculo (motorista_id);
