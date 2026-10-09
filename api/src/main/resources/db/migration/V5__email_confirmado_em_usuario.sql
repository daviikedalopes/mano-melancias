-- Confirmação de e-mail: usuários novos nascem com e-mail NÃO confirmado e só
-- entram no sistema depois de clicar no link enviado para o endereço deles.
-- Os usuários que já existem ficam como confirmados (DEFAULT TRUE).
ALTER TABLE usuario ADD COLUMN email_confirmado BOOLEAN NOT NULL DEFAULT TRUE;
