-- Tabela de compromissos da agenda.
-- As regras do domínio também ficam protegidas no banco, como última barreira.

CREATE TABLE compromisso (
    id        uuid         PRIMARY KEY,
    titulo    varchar(200) NOT NULL,
    descricao text,
    inicio    timestamptz  NOT NULL,
    fim       timestamptz  NOT NULL,
    status    varchar(20)  NOT NULL,

    CONSTRAINT compromisso_fim_posterior_ao_inicio CHECK (fim > inicio),
    CONSTRAINT compromisso_status_valido CHECK (status IN ('ATIVO', 'CANCELADO')),

    -- Dois compromissos ativos não podem se sobrepor. O intervalo '[)' inclui o
    -- início e exclui o fim: terminar exatamente quando outro começa não conflita.
    -- Protege contra duas requisições simultâneas que passariam juntas pela
    -- verificação da aplicação.
    CONSTRAINT compromisso_sem_sobreposicao_ativa
        EXCLUDE USING gist (tstzrange(inicio, fim, '[)') WITH &&)
        WHERE (status = 'ATIVO')
);

CREATE INDEX compromisso_inicio_idx ON compromisso (inicio);
