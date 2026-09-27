-- O número da venda passou a ser calculado preenchendo lacunas (menor número
-- livre), então a sequence monotônica não é mais usada.
DROP SEQUENCE IF EXISTS venda_numero_seq;
