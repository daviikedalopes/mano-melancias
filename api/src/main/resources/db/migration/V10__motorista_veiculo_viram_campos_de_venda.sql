-- Motorista e Veículo deixam de ser cadastros próprios: passam a ser só
-- campos da venda (nome+CPF do motorista, placa+cidade do veículo).

ALTER TABLE venda ADD COLUMN motorista_nome VARCHAR(255);
ALTER TABLE venda ADD COLUMN motorista_cpf VARCHAR(11);
ALTER TABLE venda ADD COLUMN veiculo_placa VARCHAR(8);
ALTER TABLE venda ADD COLUMN veiculo_cidade VARCHAR(255);

-- Preserva o histórico: copia os dados dos cadastros antigos para as vendas
-- que os referenciavam, antes de apagar as tabelas.
UPDATE venda v
SET motorista_nome = m.nome,
    motorista_cpf = m.cpf
FROM motorista m
WHERE v.motorista_id = m.id;

UPDATE venda v
SET veiculo_placa = ve.placa,
    veiculo_cidade = ve.cidade
FROM veiculo ve
WHERE v.veiculo_id = ve.id;

ALTER TABLE venda ALTER COLUMN motorista_nome SET NOT NULL;
ALTER TABLE venda ALTER COLUMN veiculo_placa SET NOT NULL;
ALTER TABLE venda ALTER COLUMN veiculo_cidade SET NOT NULL;

ALTER TABLE venda DROP COLUMN motorista_id;
ALTER TABLE venda DROP COLUMN veiculo_id;

DROP TABLE veiculo;
DROP TABLE motorista;
