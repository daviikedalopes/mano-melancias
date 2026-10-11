-- Peso líquido da carga na chegada ao cliente (as melancias podem se danificar na viagem).
-- Campo livre e opcional: só é registrado/exibido, não entra em nenhum cálculo nem validação.
ALTER TABLE venda ADD COLUMN peso_liquido_chegada NUMERIC(10,2);
