CREATE SEQUENCE venda_numero_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE venda (
    id UUID PRIMARY KEY,
    numero INTEGER NOT NULL UNIQUE,
    data_venda DATE NOT NULL,
    cliente_id UUID NOT NULL REFERENCES cliente (id),
    produtor_id UUID NOT NULL REFERENCES produtor (id),
    motorista_id UUID NOT NULL REFERENCES motorista (id),
    veiculo_id UUID NOT NULL REFERENCES veiculo (id),
    peso_bruto NUMERIC(10,2) NOT NULL,
    desc_tara NUMERIC(10,2) NOT NULL,
    desc_palha NUMERIC(10,2) NOT NULL DEFAULT 0,
    peso_liquido NUMERIC(10,2) NOT NULL,
    total_frutas INTEGER NOT NULL,
    media_peso NUMERIC(10,2) NOT NULL,
    preco_kg NUMERIC(10,4) NOT NULL,
    valor_mercadoria NUMERIC(12,2) NOT NULL,
    tipo_frete VARCHAR(20) NOT NULL,
    preco_frete_kg NUMERIC(10,4),
    valor_frete NUMERIC(12,2) NOT NULL,
    restante_pagar NUMERIC(12,2) NOT NULL,
    vencimento DATE,
    nf VARCHAR(50),
    status_pagamento VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    observacoes TEXT,
    created_by UUID REFERENCES usuario (id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_venda_data_venda ON venda (data_venda);
CREATE INDEX idx_venda_cliente ON venda (cliente_id);
CREATE INDEX idx_venda_produtor ON venda (produtor_id);
CREATE INDEX idx_venda_motorista ON venda (motorista_id);
CREATE INDEX idx_venda_status_pagamento ON venda (status_pagamento);
