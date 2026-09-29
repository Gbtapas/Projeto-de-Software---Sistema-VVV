SET @OLD_UNIQUE_CHECKS      = @@UNIQUE_CHECKS,      UNIQUE_CHECKS      = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE           = @@SQL_MODE,           SQL_MODE           = 'TRADITIONAL';
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP DATABASE IF EXISTS vvv;

CREATE DATABASE vvv
    CHARACTER SET  utf8mb4
    COLLATE        utf8mb4_unicode_ci;

USE vvv;

SET time_zone = '-03:00';


CREATE TABLE perfis (
    id_perfil   TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome        ENUM('CLIENTE','FUNCIONARIO','GERENTE_PDV','GERENTE_VIRTUAL','ADMIN') NOT NULL,
    descricao   VARCHAR(200) NULL,
    PRIMARY KEY (id_perfil),
    UNIQUE KEY uq_perfis_nome (nome)
) ENGINE = InnoDB;


CREATE TABLE usuarios (
    id_usuario        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email             VARCHAR(150)    NOT NULL,
    senha_hash        VARCHAR(255)    NOT NULL,
    ativo             TINYINT(1)      NOT NULL DEFAULT 1,
    criado_em         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acesso     DATETIME        NULL,
    tentativas_falhas INT             NOT NULL DEFAULT 0,
    bloqueado_ate     DATETIME        NULL,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_usuarios_email (email)
) ENGINE = InnoDB;


CREATE TABLE usuarios_perfis (
    id_usuario   BIGINT UNSIGNED  NOT NULL,
    id_perfil    TINYINT UNSIGNED NOT NULL,
    concedido_em DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_perfil),
    CONSTRAINT fk_up_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_up_perfil  FOREIGN KEY (id_perfil)  REFERENCES perfis   (id_perfil)  ON DELETE RESTRICT
) ENGINE = InnoDB;


CREATE TABLE clientes (
    id_cliente  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo      VARCHAR(20)     NOT NULL,
    nome        VARCHAR(150)    NOT NULL,
    cpf         CHAR(11)        NOT NULL,
    email       VARCHAR(150)    NULL,
    telefone    VARCHAR(20)     NULL,
    ativo       TINYINT(1)      NOT NULL DEFAULT 1,
    criado_em   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario  BIGINT UNSIGNED NULL,
    PRIMARY KEY (id_cliente),
    UNIQUE KEY uq_clientes_codigo  (codigo),
    UNIQUE KEY uq_clientes_cpf     (cpf),
    UNIQUE KEY uq_clientes_usuario (id_usuario),
    CONSTRAINT fk_clientes_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE SET NULL
) ENGINE = InnoDB;


CREATE TABLE cidades (
    id_cidade     INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome          VARCHAR(100) NOT NULL,
    estado        VARCHAR(100) NOT NULL,
    pais          VARCHAR(100) NOT NULL DEFAULT 'Brasil',
    identificador CHAR(3)      NOT NULL,
    PRIMARY KEY (id_cidade),
    UNIQUE KEY uq_cidades_identificador (identificador),
    CONSTRAINT chk_cidades_identificador CHECK (identificador REGEXP '^[A-Z]{3}$')
) ENGINE = InnoDB;


CREATE TABLE aeroportos (
    id_aeroporto INT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo_iata  CHAR(3)      NOT NULL,
    nome         VARCHAR(150) NOT NULL,
    id_cidade    INT UNSIGNED NOT NULL,
    PRIMARY KEY (id_aeroporto),
    UNIQUE KEY uq_aeroportos_iata (codigo_iata),
    CONSTRAINT fk_aeroportos_cidade FOREIGN KEY (id_cidade) REFERENCES cidades (id_cidade) ON DELETE RESTRICT
) ENGINE = InnoDB;


CREATE TABLE transportadoras (
    id_transportadora INT UNSIGNED NOT NULL AUTO_INCREMENT,
    cnpj              CHAR(14)     NOT NULL,
    nome              VARCHAR(150) NOT NULL,
    telefone          VARCHAR(20)  NULL,
    email             VARCHAR(150) NULL,
    ativo             TINYINT(1)   NOT NULL DEFAULT 1,
    criado_em         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_transportadora),
    UNIQUE KEY uq_transportadoras_cnpj (cnpj)
) ENGINE = InnoDB;


CREATE TABLE funcionarios (
    id_funcionario  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo          VARCHAR(20)     NOT NULL,
    cpf             CHAR(11)        NOT NULL,
    nome            VARCHAR(150)    NOT NULL,
    rua             VARCHAR(150)    NULL,
    numero          VARCHAR(20)     NULL,
    complemento     VARCHAR(100)    NULL,
    bairro          VARCHAR(100)    NULL,
    cep             CHAR(8)         NULL,
    cidade_endereco VARCHAR(100)    NULL,
    estado_endereco CHAR(2)         NULL,
    tipo            ENUM('FUNCIONARIO','GERENTE_PDV','GERENTE_VIRTUAL') NOT NULL DEFAULT 'FUNCIONARIO',
    ativo           TINYINT(1)      NOT NULL DEFAULT 1,
    criado_em       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario      BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id_funcionario),
    UNIQUE KEY uq_funcionarios_codigo  (codigo),
    UNIQUE KEY uq_funcionarios_cpf     (cpf),
    UNIQUE KEY uq_funcionarios_usuario (id_usuario),
    CONSTRAINT fk_funcionarios_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT
) ENGINE = InnoDB;


CREATE TABLE pontos_de_venda (
    id_ponto        INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    codigo          VARCHAR(20)   NOT NULL,
    cnpj            CHAR(14)      NOT NULL,
    nome            VARCHAR(150)  NOT NULL,
    rua             VARCHAR(150)  NOT NULL,
    numero          VARCHAR(20)   NOT NULL,
    complemento     VARCHAR(100)  NULL,
    bairro          VARCHAR(100)  NOT NULL,
    cep             CHAR(8)       NOT NULL,
    cidade_endereco VARCHAR(100)  NOT NULL,
    estado_endereco CHAR(2)       NOT NULL,
    telefone        VARCHAR(20)   NULL,
    ativo           TINYINT(1)    NOT NULL DEFAULT 1,
    criado_em       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_gerente      BIGINT UNSIGNED NULL,
    PRIMARY KEY (id_ponto),
    UNIQUE KEY uq_pdv_codigo (codigo),
    UNIQUE KEY uq_pdv_cnpj   (cnpj),
    CONSTRAINT fk_pdv_gerente FOREIGN KEY (id_gerente) REFERENCES funcionarios (id_funcionario) ON DELETE SET NULL
) ENGINE = InnoDB;


CREATE TABLE funcionarios_pontos_de_venda (
    id_fpv         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_funcionario BIGINT UNSIGNED NOT NULL,
    id_ponto       INT UNSIGNED    NOT NULL,
    data_inicio    DATE            NOT NULL,
    data_fim       DATE            NULL,
    ativo          TINYINT(1)      NOT NULL DEFAULT 1,
    PRIMARY KEY (id_fpv),
    CONSTRAINT fk_fpv_funcionario FOREIGN KEY (id_funcionario) REFERENCES funcionarios    (id_funcionario) ON DELETE CASCADE,
    CONSTRAINT fk_fpv_ponto       FOREIGN KEY (id_ponto)       REFERENCES pontos_de_venda (id_ponto)       ON DELETE CASCADE
) ENGINE = InnoDB;


CREATE TABLE modais (
    id_modal          INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    codigo            VARCHAR(20)       NOT NULL,
    tipo              ENUM('AVIAO','TREM','ONIBUS','NAVIO') NOT NULL,
    modelo            VARCHAR(100)      NOT NULL,
    ano               YEAR              NOT NULL,
    capacidade        SMALLINT UNSIGNED NOT NULL,
    status            ENUM('DISPONIVEL','MANUTENCAO','INATIVO') NOT NULL DEFAULT 'DISPONIVEL',
    id_transportadora INT UNSIGNED      NOT NULL,
    id_aeroporto_base INT UNSIGNED      NULL,
    ativo             TINYINT(1)        NOT NULL DEFAULT 1,
    criado_em         DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_modal),
    UNIQUE KEY uq_modais_codigo (codigo),
    CONSTRAINT fk_modais_transportadora  FOREIGN KEY (id_transportadora) REFERENCES transportadoras (id_transportadora) ON DELETE RESTRICT,
    CONSTRAINT fk_modais_aeroporto       FOREIGN KEY (id_aeroporto_base) REFERENCES aeroportos      (id_aeroporto)       ON DELETE RESTRICT,
    CONSTRAINT chk_modais_aviao_aeroporto CHECK (NOT (tipo = 'AVIAO' AND id_aeroporto_base IS NULL))
) ENGINE = InnoDB;


CREATE TABLE manutencoes (
    id_manutencao INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_modal      INT UNSIGNED NOT NULL,
    data_inicio   DATE         NOT NULL,
    data_fim      DATE         NOT NULL,
    descricao     TEXT         NULL,
    status        ENUM('AGENDADA','EM_ANDAMENTO','CONCLUIDA','CANCELADA') NOT NULL DEFAULT 'AGENDADA',
    criado_em     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_manutencao),
    CONSTRAINT fk_manutencoes_modal  FOREIGN KEY (id_modal) REFERENCES modais (id_modal) ON DELETE RESTRICT,
    CONSTRAINT chk_manutencoes_datas CHECK (data_fim >= data_inicio)
) ENGINE = InnoDB;


CREATE TABLE rotas (
    id_rota           INT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo            VARCHAR(20)  NOT NULL,
    descricao         VARCHAR(200) NULL,
    id_cidade_origem  INT UNSIGNED NOT NULL,
    id_cidade_destino INT UNSIGNED NOT NULL,
    tipo              ENUM('DIRETA','COM_ESCALA') NOT NULL DEFAULT 'DIRETA',
    ativo             TINYINT(1)   NOT NULL DEFAULT 1,
    criado_em         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_rota),
    UNIQUE KEY uq_rotas_codigo (codigo),
    CONSTRAINT fk_rotas_origem  FOREIGN KEY (id_cidade_origem)  REFERENCES cidades (id_cidade) ON DELETE RESTRICT,
    CONSTRAINT fk_rotas_destino FOREIGN KEY (id_cidade_destino) REFERENCES cidades (id_cidade) ON DELETE RESTRICT,
    CONSTRAINT chk_rotas_cidades CHECK (id_cidade_origem <> id_cidade_destino)
) ENGINE = InnoDB;


CREATE TABLE trechos_rota (
    id_trecho            INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    id_rota              INT UNSIGNED      NOT NULL,
    ordem                TINYINT UNSIGNED  NOT NULL,
    id_cidade_origem     INT UNSIGNED      NOT NULL,
    id_cidade_destino    INT UNSIGNED      NOT NULL,
    id_aeroporto_origem  INT UNSIGNED      NULL,
    id_aeroporto_destino INT UNSIGNED      NULL,
    hora_partida         TIME              NOT NULL,
    hora_chegada         TIME              NOT NULL,
    tempo_estimado_min   SMALLINT UNSIGNED NOT NULL,
    PRIMARY KEY (id_trecho),
    UNIQUE KEY uq_trechos_rota_ordem (id_rota, ordem),
    CONSTRAINT fk_trechos_rota           FOREIGN KEY (id_rota)              REFERENCES rotas      (id_rota)      ON DELETE CASCADE,
    CONSTRAINT fk_trechos_cidade_orig    FOREIGN KEY (id_cidade_origem)     REFERENCES cidades    (id_cidade)    ON DELETE RESTRICT,
    CONSTRAINT fk_trechos_cidade_dest    FOREIGN KEY (id_cidade_destino)    REFERENCES cidades    (id_cidade)    ON DELETE RESTRICT,
    CONSTRAINT fk_trechos_aeroporto_orig FOREIGN KEY (id_aeroporto_origem)  REFERENCES aeroportos (id_aeroporto) ON DELETE SET NULL,
    CONSTRAINT fk_trechos_aeroporto_dest FOREIGN KEY (id_aeroporto_destino) REFERENCES aeroportos (id_aeroporto) ON DELETE SET NULL
) ENGINE = InnoDB;


CREATE TABLE programacoes_viagem (
    id_programacao    INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    id_rota           INT UNSIGNED      NOT NULL,
    id_modal          INT UNSIGNED      NOT NULL,
    data_viagem       DATE              NOT NULL,
    vagas_disponiveis SMALLINT UNSIGNED NOT NULL,
    valor_base        DECIMAL(10,2)     NOT NULL,
    status            ENUM('ATIVO','CANCELADO','ENCERRADO') NOT NULL DEFAULT 'ATIVO',
    criado_em         DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_programacao),
    UNIQUE KEY uq_prog_rota_modal_data (id_rota, id_modal, data_viagem),
    CONSTRAINT fk_prog_rota  FOREIGN KEY (id_rota)  REFERENCES rotas  (id_rota)  ON DELETE RESTRICT,
    CONSTRAINT fk_prog_modal FOREIGN KEY (id_modal) REFERENCES modais (id_modal) ON DELETE RESTRICT,
    CONSTRAINT chk_prog_vagas CHECK (vagas_disponiveis >= 0),
    CONSTRAINT chk_prog_valor CHECK (valor_base > 0)
) ENGINE = InnoDB;


CREATE TABLE passageiros (
    id_passageiro   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo          VARCHAR(20)     NOT NULL,
    cpf             CHAR(11)        NOT NULL,
    nome            VARCHAR(150)    NOT NULL,
    data_nascimento DATE            NOT NULL,
    telefone        VARCHAR(20)     NULL,
    profissao       VARCHAR(100)    NULL,
    rua             VARCHAR(150)    NULL,
    numero          VARCHAR(20)     NULL,
    complemento     VARCHAR(100)    NULL,
    bairro          VARCHAR(100)    NULL,
    cep             CHAR(8)         NULL,
    cidade_endereco VARCHAR(100)    NULL,
    estado_endereco CHAR(2)         NULL,
    ativo           TINYINT(1)      NOT NULL DEFAULT 1,
    criado_em       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario      BIGINT UNSIGNED NULL,
    id_cliente      BIGINT UNSIGNED NULL,
    PRIMARY KEY (id_passageiro),
    UNIQUE KEY uq_passageiros_codigo (codigo),
    UNIQUE KEY uq_passageiros_cpf    (cpf),
    CONSTRAINT fk_passageiros_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE SET NULL,
    CONSTRAINT fk_pass_cliente        FOREIGN KEY (id_cliente) REFERENCES clientes (id_cliente) ON DELETE SET NULL
) ENGINE = InnoDB;


CREATE TABLE reservas (
    id_reserva      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo          VARCHAR(20)     NOT NULL,
    data_criacao    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status          ENUM('PENDENTE','CONFIRMADA','CANCELADA') NOT NULL DEFAULT 'PENDENTE',
    canal           ENUM('ONLINE','PRESENCIAL')               NOT NULL,
    valor_bruto     DECIMAL(10,2)   NOT NULL,
    valor_desconto  DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    valor_total     DECIMAL(10,2)   NOT NULL,
    id_passageiro   BIGINT UNSIGNED NOT NULL,
    id_programacao  INT UNSIGNED    NOT NULL,
    id_acompanhante BIGINT UNSIGNED NULL,
    id_cliente      BIGINT UNSIGNED NULL,
    PRIMARY KEY (id_reserva),
    UNIQUE KEY uq_reservas_codigo (codigo),
    CONSTRAINT fk_res_passageiro   FOREIGN KEY (id_passageiro)   REFERENCES passageiros        (id_passageiro)  ON DELETE RESTRICT,
    CONSTRAINT fk_res_programacao  FOREIGN KEY (id_programacao)  REFERENCES programacoes_viagem (id_programacao) ON DELETE RESTRICT,
    CONSTRAINT fk_res_acompanhante FOREIGN KEY (id_acompanhante) REFERENCES passageiros        (id_passageiro)  ON DELETE RESTRICT,
    CONSTRAINT fk_res_cliente      FOREIGN KEY (id_cliente)      REFERENCES clientes            (id_cliente)     ON DELETE SET NULL,
    CONSTRAINT chk_res_valores     CHECK (valor_bruto >= 0 AND valor_desconto >= 0 AND valor_total >= 0)
) ENGINE = InnoDB;


CREATE TABLE pagamentos (
    id_pagamento       BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    id_reserva         BIGINT UNSIGNED  NOT NULL,
    tipo               ENUM('CREDITO','DEBITO') NOT NULL,
    parcelas           TINYINT UNSIGNED NOT NULL DEFAULT 1,
    valor_bruto        DECIMAL(10,2)    NOT NULL,
    valor_juros        DECIMAL(10,2)    NOT NULL DEFAULT 0.00,
    valor_total        DECIMAL(10,2)    NOT NULL,
    valor_parcela      DECIMAL(10,2)    NOT NULL,
    status             ENUM('PENDENTE','APROVADO','RECUSADO','ESTORNADO') NOT NULL DEFAULT 'PENDENTE',
    codigo_autorizacao VARCHAR(100)     NULL,
    data_pagamento     DATETIME         NULL,
    criado_em          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_pagamento),
    UNIQUE KEY uq_pagamentos_reserva (id_reserva),
    CONSTRAINT fk_pag_reserva   FOREIGN KEY (id_reserva) REFERENCES reservas (id_reserva) ON DELETE RESTRICT,
    CONSTRAINT chk_pag_debito   CHECK (NOT (tipo = 'DEBITO' AND parcelas > 1)),
    CONSTRAINT chk_pag_parcelas CHECK (parcelas BETWEEN 1 AND 12)
) ENGINE = InnoDB;


CREATE TABLE tickets (
    id_ticket          BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT,
    codigo_ticket      VARCHAR(30)       NOT NULL,
    localizador        CHAR(8)           NOT NULL,
    data_emissao       DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_passagem      ENUM('ECONOMICA','EXECUTIVA','PRIMEIRA_CLASSE') NOT NULL DEFAULT 'ECONOMICA',
    hora_partida       TIME              NOT NULL,
    hora_chegada       TIME              NOT NULL,
    tempo_estimado_min SMALLINT UNSIGNED NOT NULL,
    status             ENUM('ATIVO','CANCELADO','USADO') NOT NULL DEFAULT 'ATIVO',
    id_reserva         BIGINT UNSIGNED   NOT NULL,
    id_pagamento       BIGINT UNSIGNED   NOT NULL,
    PRIMARY KEY (id_ticket),
    UNIQUE KEY uq_tickets_codigo      (codigo_ticket),
    UNIQUE KEY uq_tickets_localizador (localizador),
    UNIQUE KEY uq_tickets_reserva     (id_reserva),
    CONSTRAINT fk_tkt_reserva   FOREIGN KEY (id_reserva)   REFERENCES reservas  (id_reserva)   ON DELETE RESTRICT,
    CONSTRAINT fk_tkt_pagamento FOREIGN KEY (id_pagamento) REFERENCES pagamentos (id_pagamento) ON DELETE RESTRICT
) ENGINE = InnoDB;


CREATE TABLE vendas (
    id_venda    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_reserva  BIGINT UNSIGNED NOT NULL,
    data_venda  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(10,2)   NOT NULL,
    status      ENUM('PENDENTE','CONFIRMADA','CANCELADA') NOT NULL DEFAULT 'PENDENTE',
    PRIMARY KEY (id_venda),
    UNIQUE KEY uq_vendas_reserva (id_reserva),
    CONSTRAINT fk_venda_reserva FOREIGN KEY (id_reserva) REFERENCES reservas (id_reserva) ON DELETE RESTRICT
) ENGINE = InnoDB;


CREATE TABLE vendas_presenciais (
    id_venda         BIGINT UNSIGNED NOT NULL,
    id_funcionario   BIGINT UNSIGNED NOT NULL,
    id_ponto         INT UNSIGNED    NOT NULL,
    confirmado_por   BIGINT UNSIGNED NULL,
    data_confirmacao DATETIME        NULL,
    PRIMARY KEY (id_venda),
    CONSTRAINT fk_vp_venda       FOREIGN KEY (id_venda)       REFERENCES vendas          (id_venda)       ON DELETE CASCADE,
    CONSTRAINT fk_vp_funcionario FOREIGN KEY (id_funcionario) REFERENCES funcionarios    (id_funcionario) ON DELETE RESTRICT,
    CONSTRAINT fk_vp_ponto       FOREIGN KEY (id_ponto)       REFERENCES pontos_de_venda (id_ponto)       ON DELETE RESTRICT,
    CONSTRAINT fk_vp_confirmador FOREIGN KEY (confirmado_por) REFERENCES funcionarios    (id_funcionario) ON DELETE SET NULL
) ENGINE = InnoDB;


CREATE TABLE vendas_online (
    id_venda           BIGINT UNSIGNED NOT NULL,
    id_gerente_virtual BIGINT UNSIGNED NOT NULL,
    data_aprovacao     DATETIME        NULL,
    status_aprovacao   ENUM('PENDENTE','APROVADA','RECUSADA') NOT NULL DEFAULT 'PENDENTE',
    PRIMARY KEY (id_venda),
    CONSTRAINT fk_vo_venda   FOREIGN KEY (id_venda)           REFERENCES vendas       (id_venda)       ON DELETE CASCADE,
    CONSTRAINT fk_vo_gerente FOREIGN KEY (id_gerente_virtual) REFERENCES funcionarios (id_funcionario) ON DELETE RESTRICT
) ENGINE = InnoDB;


CREATE TABLE log_auditoria (
    id_log           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tabela           VARCHAR(60)     NOT NULL,
    id_registro      BIGINT UNSIGNED NOT NULL,
    operacao         ENUM('INSERT','UPDATE','DELETE') NOT NULL,
    dados_anteriores JSON            NULL,
    dados_novos      JSON            NULL,
    id_usuario       BIGINT UNSIGNED NULL,
    data_hora        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ip_address       VARCHAR(45)     NULL,
    PRIMARY KEY (id_log),
    CONSTRAINT fk_log_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE SET NULL
) ENGINE = InnoDB;


SET FOREIGN_KEY_CHECKS = 1;


CREATE INDEX idx_reservas_passageiro   ON reservas (id_passageiro);
CREATE INDEX idx_reservas_status       ON reservas (status);
CREATE INDEX idx_reservas_canal        ON reservas (canal);
CREATE INDEX idx_reservas_data_criacao ON reservas (data_criacao);
CREATE INDEX idx_reservas_programacao  ON reservas (id_programacao);

CREATE INDEX idx_prog_data_viagem ON programacoes_viagem (data_viagem);
CREATE INDEX idx_prog_rota_data   ON programacoes_viagem (id_rota, data_viagem);
CREATE INDEX idx_prog_modal_data  ON programacoes_viagem (id_modal, data_viagem);
CREATE INDEX idx_prog_status      ON programacoes_viagem (status);

CREATE INDEX idx_rotas_origem_destino ON rotas (id_cidade_origem, id_cidade_destino);
CREATE INDEX idx_rotas_tipo           ON rotas (tipo);

CREATE INDEX idx_passageiros_nome ON passageiros (nome);
CREATE INDEX idx_passageiros_nasc ON passageiros (data_nascimento);

CREATE INDEX idx_funcionarios_tipo ON funcionarios (tipo);
CREATE INDEX idx_funcionarios_nome ON funcionarios (nome);

CREATE INDEX idx_modais_tipo           ON modais (tipo);
CREATE INDEX idx_modais_status         ON modais (status);
CREATE INDEX idx_modais_transportadora ON modais (id_transportadora);

CREATE INDEX idx_manut_modal_periodo ON manutencoes (id_modal, data_inicio, data_fim);
CREATE INDEX idx_manut_status        ON manutencoes (status);

CREATE INDEX idx_tickets_status ON tickets (status);

CREATE INDEX idx_pagamentos_status ON pagamentos (status);
CREATE INDEX idx_pagamentos_data   ON pagamentos (data_pagamento);

CREATE INDEX idx_vendas_status ON vendas (status);
CREATE INDEX idx_vendas_data   ON vendas (data_venda);

CREATE INDEX idx_log_tabela_registro ON log_auditoria (tabela, id_registro);
CREATE INDEX idx_log_data_hora       ON log_auditoria (data_hora);
CREATE INDEX idx_log_usuario         ON log_auditoria (id_usuario);
CREATE INDEX idx_log_operacao        ON log_auditoria (operacao);


CREATE OR REPLACE VIEW vw_passageiros_com_idade AS
SELECT
    p.*,
    TIMESTAMPDIFF(YEAR, p.data_nascimento, CURDATE()) AS idade
FROM passageiros p
WHERE p.ativo = 1;


CREATE OR REPLACE VIEW vw_programacoes_disponiveis AS
SELECT
    pv.id_programacao,
    ro.codigo            AS codigo_rota,
    ro.descricao         AS descricao_rota,
    ro.tipo              AS tipo_rota,
    c_orig.nome          AS cidade_origem,
    c_orig.identificador AS cod_origem,
    c_dest.nome          AS cidade_destino,
    c_dest.identificador AS cod_destino,
    pv.data_viagem,
    m.tipo               AS tipo_modal,
    m.modelo             AS modelo_modal,
    m.capacidade         AS capacidade_total,
    pv.vagas_disponiveis,
    pv.valor_base,
    t.nome               AS transportadora
FROM  programacoes_viagem pv
JOIN  rotas           ro     ON ro.id_rota          = pv.id_rota
JOIN  cidades         c_orig ON c_orig.id_cidade     = ro.id_cidade_origem
JOIN  cidades         c_dest ON c_dest.id_cidade     = ro.id_cidade_destino
JOIN  modais          m      ON m.id_modal            = pv.id_modal
JOIN  transportadoras t      ON t.id_transportadora   = m.id_transportadora
WHERE pv.status            = 'ATIVO'
  AND pv.data_viagem      >= CURDATE()
  AND pv.vagas_disponiveis > 0
  AND m.status             = 'DISPONIVEL'
  AND ro.ativo             = 1;


CREATE OR REPLACE VIEW vw_reservas_completas AS
SELECT
    r.id_reserva,
    r.codigo              AS codigo_reserva,
    r.data_criacao,
    r.status              AS status_reserva,
    r.canal,
    r.valor_bruto,
    r.valor_desconto,
    r.valor_total,
    p.nome                AS nome_passageiro,
    p.cpf                 AS cpf_passageiro,
    TIMESTAMPDIFF(YEAR, p.data_nascimento, CURDATE()) AS idade_passageiro,
    ac.nome               AS nome_acompanhante,
    ac.cpf                AS cpf_acompanhante,
    pv.data_viagem,
    ro.descricao          AS rota_descricao,
    c_orig.nome           AS cidade_origem,
    c_orig.identificador  AS cod_origem,
    c_dest.nome           AS cidade_destino,
    c_dest.identificador  AS cod_destino,
    m.tipo                AS tipo_modal,
    m.modelo              AS modelo_modal,
    trans.nome            AS transportadora,
    pg.tipo               AS tipo_pagamento,
    pg.parcelas,
    pg.valor_juros,
    pg.valor_total        AS valor_pago,
    pg.status             AS status_pagamento,
    tk.codigo_ticket,
    tk.localizador,
    tk.status             AS status_ticket
FROM  reservas            r
JOIN  passageiros         p      ON p.id_passageiro      = r.id_passageiro
JOIN  programacoes_viagem pv     ON pv.id_programacao    = r.id_programacao
JOIN  rotas               ro     ON ro.id_rota            = pv.id_rota
JOIN  cidades             c_orig ON c_orig.id_cidade      = ro.id_cidade_origem
JOIN  cidades             c_dest ON c_dest.id_cidade      = ro.id_cidade_destino
JOIN  modais              m      ON m.id_modal             = pv.id_modal
JOIN  transportadoras     trans  ON trans.id_transportadora = m.id_transportadora
LEFT JOIN passageiros     ac     ON ac.id_passageiro       = r.id_acompanhante
LEFT JOIN pagamentos      pg     ON pg.id_reserva          = r.id_reserva
LEFT JOIN tickets         tk     ON tk.id_reserva          = r.id_reserva;


CREATE OR REPLACE VIEW vw_funcionarios_pontos_de_venda AS
SELECT
    f.id_funcionario,
    f.codigo  AS codigo_funcionario,
    f.nome    AS nome_funcionario,
    f.tipo,
    pdv.id_ponto,
    pdv.codigo AS codigo_ponto,
    pdv.nome   AS nome_ponto,
    fpv.data_inicio,
    fpv.data_fim,
    fpv.ativo  AS vinculo_ativo,
    (
        SELECT COUNT(*)
        FROM   funcionarios_pontos_de_venda x
        WHERE  x.id_funcionario = f.id_funcionario
          AND  x.ativo = 1
    ) AS total_pontos_ativos
FROM  funcionarios                 f
JOIN  funcionarios_pontos_de_venda fpv ON fpv.id_funcionario = f.id_funcionario AND fpv.ativo = 1
JOIN  pontos_de_venda              pdv ON pdv.id_ponto        = fpv.id_ponto
WHERE f.ativo = 1;


CREATE OR REPLACE VIEW vw_modais_em_manutencao AS
SELECT
    m.id_modal,
    m.codigo,
    m.tipo,
    m.modelo,
    t.nome     AS transportadora,
    mn.id_manutencao,
    mn.data_inicio,
    mn.data_fim,
    mn.descricao,
    mn.status  AS status_manutencao
FROM  modais          m
JOIN  transportadoras t  ON t.id_transportadora = m.id_transportadora
JOIN  manutencoes     mn ON mn.id_modal          = m.id_modal
WHERE mn.status IN ('AGENDADA', 'EM_ANDAMENTO')
  AND mn.data_inicio <= CURDATE()
  AND mn.data_fim    >= CURDATE();


CREATE OR REPLACE VIEW vw_relatorio_vendas AS
SELECT
    v.id_venda,
    v.data_venda,
    v.status      AS status_venda,
    v.valor_total,
    r.canal,
    r.codigo      AS codigo_reserva,
    r.status      AS status_reserva,
    p.nome        AS nome_passageiro,
    pv_prog.data_viagem,
    ro.descricao  AS rota,
    c_orig.nome   AS origem,
    c_dest.nome   AS destino,
    m.tipo        AS modal,
    vp.id_ponto,
    pdv.nome      AS ponto_de_venda,
    fp.nome       AS nome_funcionario,
    vp.data_confirmacao,
    vo.status_aprovacao,
    vo.data_aprovacao,
    fgv.nome      AS gerente_virtual
FROM  vendas               v
JOIN  reservas             r       ON r.id_reserva          = v.id_reserva
JOIN  passageiros          p       ON p.id_passageiro        = r.id_passageiro
JOIN  programacoes_viagem  pv_prog ON pv_prog.id_programacao = r.id_programacao
JOIN  rotas                ro      ON ro.id_rota              = pv_prog.id_rota
JOIN  cidades              c_orig  ON c_orig.id_cidade        = ro.id_cidade_origem
JOIN  cidades              c_dest  ON c_dest.id_cidade        = ro.id_cidade_destino
JOIN  modais               m       ON m.id_modal               = pv_prog.id_modal
LEFT JOIN vendas_presenciais vp    ON vp.id_venda              = v.id_venda
LEFT JOIN pontos_de_venda   pdv    ON pdv.id_ponto              = vp.id_ponto
LEFT JOIN funcionarios      fp     ON fp.id_funcionario         = vp.id_funcionario
LEFT JOIN vendas_online     vo     ON vo.id_venda               = v.id_venda
LEFT JOIN funcionarios      fgv    ON fgv.id_funcionario        = vo.id_gerente_virtual;


DELIMITER //

DROP TRIGGER IF EXISTS trg_fpv_max_dois_pontos //
CREATE TRIGGER trg_fpv_max_dois_pontos
BEFORE INSERT ON funcionarios_pontos_de_venda
FOR EACH ROW
BEGIN
    DECLARE v_count TINYINT DEFAULT 0;
    SELECT COUNT(*) INTO v_count
    FROM   funcionarios_pontos_de_venda
    WHERE  id_funcionario = NEW.id_funcionario AND ativo = 1;
    IF v_count >= 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RN28: Funcionário já está vinculado ao máximo de 2 pontos de venda.';
    END IF;
END //


DROP TRIGGER IF EXISTS trg_pdv_valida_gerente_ins //
CREATE TRIGGER trg_pdv_valida_gerente_ins
BEFORE INSERT ON pontos_de_venda
FOR EACH ROW
BEGIN
    DECLARE v_tipo VARCHAR(30);
    IF NEW.id_gerente IS NOT NULL THEN
        SELECT tipo INTO v_tipo FROM funcionarios WHERE id_funcionario = NEW.id_gerente;
        IF v_tipo <> 'GERENTE_PDV' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'RN26: O gerente designado deve ter tipo GERENTE_PDV.';
        END IF;
    END IF;
END //

DROP TRIGGER IF EXISTS trg_pdv_valida_gerente_upd //
CREATE TRIGGER trg_pdv_valida_gerente_upd
BEFORE UPDATE ON pontos_de_venda
FOR EACH ROW
BEGIN
    DECLARE v_tipo VARCHAR(30);
    IF NEW.id_gerente IS NOT NULL AND (OLD.id_gerente IS NULL OR NEW.id_gerente <> OLD.id_gerente) THEN
        SELECT tipo INTO v_tipo FROM funcionarios WHERE id_funcionario = NEW.id_gerente;
        IF v_tipo <> 'GERENTE_PDV' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'RN26: O gerente designado deve ter tipo GERENTE_PDV.';
        END IF;
    END IF;
END //


DROP TRIGGER IF EXISTS trg_vo_valida_gerente_ins //
CREATE TRIGGER trg_vo_valida_gerente_ins
BEFORE INSERT ON vendas_online
FOR EACH ROW
BEGIN
    DECLARE v_tipo VARCHAR(30);
    SELECT tipo INTO v_tipo FROM funcionarios WHERE id_funcionario = NEW.id_gerente_virtual;
    IF v_tipo <> 'GERENTE_VIRTUAL' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RN31: O responsável por vendas online deve ser do tipo GERENTE_VIRTUAL.';
    END IF;
END //


DROP TRIGGER IF EXISTS trg_reserva_before_insert //
CREATE TRIGGER trg_reserva_before_insert
BEFORE INSERT ON reservas
FOR EACH ROW
BEGIN
    DECLARE v_nasc_passageiro   DATE;
    DECLARE v_idade             INT DEFAULT 0;
    DECLARE v_nasc_acompanhante DATE;
    DECLARE v_idade_acomp       INT DEFAULT 0;
    DECLARE v_vagas             SMALLINT DEFAULT 0;
    DECLARE v_status_prog       VARCHAR(20);
    DECLARE v_status_modal      VARCHAR(20);

    SELECT data_nascimento INTO v_nasc_passageiro
    FROM   passageiros WHERE id_passageiro = NEW.id_passageiro;

    SET v_idade = TIMESTAMPDIFF(YEAR, v_nasc_passageiro, CURDATE());

    IF v_idade < 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RN03: Passageiros com menos de 2 anos não podem realizar viagens.';
    END IF;

    IF v_idade BETWEEN 2 AND 10 THEN
        IF NEW.id_acompanhante IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'RN04: Passageiros entre 2 e 10 anos precisam de acompanhante cadastrado no sistema.';
        END IF;
        SELECT data_nascimento INTO v_nasc_acompanhante
        FROM   passageiros WHERE id_passageiro = NEW.id_acompanhante;
        SET v_idade_acomp = TIMESTAMPDIFF(YEAR, v_nasc_acompanhante, CURDATE());
        IF v_idade_acomp < 21 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'RN04: O acompanhante deve ter mais de 21 anos.';
        END IF;
        SET NEW.valor_desconto = ROUND(NEW.valor_bruto * 0.40, 2);
        SET NEW.valor_total    = ROUND(NEW.valor_bruto - NEW.valor_desconto, 2);
    ELSE
        SET NEW.valor_total = ROUND(NEW.valor_bruto - NEW.valor_desconto, 2);
    END IF;

    SELECT pv.vagas_disponiveis, pv.status, m.status
    INTO   v_vagas, v_status_prog, v_status_modal
    FROM   programacoes_viagem pv
    JOIN   modais m ON m.id_modal = pv.id_modal
    WHERE  pv.id_programacao = NEW.id_programacao;

    IF v_status_modal = 'MANUTENCAO' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RN18: O modal está em manutenção e não aceita reservas.';
    END IF;
    IF v_status_modal = 'INATIVO' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Modal inativo. Não é possível realizar reservas.';
    END IF;
    IF v_status_prog <> 'ATIVO' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RN07: A programação de viagem selecionada não está disponível.';
    END IF;
    IF v_vagas <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RI01/RN07: Não há vagas disponíveis. Overbooking prevenido.';
    END IF;

    IF NEW.codigo IS NULL OR TRIM(NEW.codigo) = '' THEN
        SET NEW.codigo = CONCAT('RES', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 999999), 6, '0'));
    END IF;
END //


DROP TRIGGER IF EXISTS trg_reserva_after_insert //
CREATE TRIGGER trg_reserva_after_insert
AFTER INSERT ON reservas
FOR EACH ROW
BEGIN
    UPDATE programacoes_viagem
    SET    vagas_disponiveis = vagas_disponiveis - 1
    WHERE  id_programacao = NEW.id_programacao;
END //


DROP TRIGGER IF EXISTS trg_reserva_after_update //
CREATE TRIGGER trg_reserva_after_update
AFTER UPDATE ON reservas
FOR EACH ROW
BEGIN
    IF NEW.status = 'CANCELADA' AND OLD.status <> 'CANCELADA' THEN
        UPDATE programacoes_viagem
        SET    vagas_disponiveis = vagas_disponiveis + 1
        WHERE  id_programacao = NEW.id_programacao;
    END IF;
END //


DROP TRIGGER IF EXISTS trg_pagamento_before_insert //
CREATE TRIGGER trg_pagamento_before_insert
BEFORE INSERT ON pagamentos
FOR EACH ROW
BEGIN
    IF NEW.tipo = 'DEBITO' THEN
        SET NEW.parcelas      = 1;
        SET NEW.valor_juros   = 0.00;
        SET NEW.valor_total   = ROUND(NEW.valor_bruto, 2);
        SET NEW.valor_parcela = ROUND(NEW.valor_bruto, 2);
    ELSEIF NEW.tipo = 'CREDITO' AND NEW.parcelas > 4 THEN
        SET NEW.valor_juros   = ROUND(NEW.valor_bruto * 0.05, 2);
        SET NEW.valor_total   = ROUND(NEW.valor_bruto + NEW.valor_juros, 2);
        SET NEW.valor_parcela = ROUND(NEW.valor_total / NEW.parcelas, 2);
    ELSE
        SET NEW.valor_juros   = 0.00;
        SET NEW.valor_total   = ROUND(NEW.valor_bruto, 2);
        SET NEW.valor_parcela = ROUND(NEW.valor_bruto / NEW.parcelas, 2);
    END IF;
END //


DROP TRIGGER IF EXISTS trg_pagamento_after_update //
CREATE TRIGGER trg_pagamento_after_update
AFTER UPDATE ON pagamentos
FOR EACH ROW
BEGIN
    DECLARE v_id_rota       INT;
    DECLARE v_hora_partida  TIME;
    DECLARE v_hora_chegada  TIME;
    DECLARE v_tempo_est     SMALLINT;
    DECLARE v_codigo_ticket VARCHAR(30);
    DECLARE v_localizador   CHAR(8);

    IF NEW.status = 'APROVADO' AND OLD.status <> 'APROVADO' THEN
        UPDATE reservas SET status = 'CONFIRMADA' WHERE id_reserva = NEW.id_reserva;

        SELECT
            pv.id_rota,
            (SELECT hora_partida FROM trechos_rota WHERE id_rota = pv.id_rota ORDER BY ordem ASC  LIMIT 1),
            (SELECT hora_chegada FROM trechos_rota WHERE id_rota = pv.id_rota ORDER BY ordem DESC LIMIT 1),
            (SELECT COALESCE(SUM(tempo_estimado_min), 0) FROM trechos_rota WHERE id_rota = pv.id_rota)
        INTO v_id_rota, v_hora_partida, v_hora_chegada, v_tempo_est
        FROM  reservas r
        JOIN  programacoes_viagem pv ON pv.id_programacao = r.id_programacao
        WHERE r.id_reserva = NEW.id_reserva;

        SET v_codigo_ticket = CONCAT('TKT', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(NEW.id_pagamento, 8, '0'));
        SET v_localizador   = UPPER(LEFT(MD5(CONCAT(NEW.id_reserva, RAND(), NOW())), 8));

        INSERT INTO tickets (
            codigo_ticket, localizador, data_emissao, tipo_passagem,
            hora_partida, hora_chegada, tempo_estimado_min, status,
            id_reserva, id_pagamento
        ) VALUES (
            v_codigo_ticket, v_localizador, NOW(), 'ECONOMICA',
            v_hora_partida, v_hora_chegada, v_tempo_est, 'ATIVO',
            NEW.id_reserva, NEW.id_pagamento
        );
    END IF;
END //


DROP TRIGGER IF EXISTS trg_manutencao_after_insert //
CREATE TRIGGER trg_manutencao_after_insert
AFTER INSERT ON manutencoes
FOR EACH ROW
BEGIN
    IF NEW.status IN ('AGENDADA', 'EM_ANDAMENTO') AND NEW.data_inicio <= CURDATE() THEN
        UPDATE modais SET status = 'MANUTENCAO' WHERE id_modal = NEW.id_modal;
    END IF;
END //


DROP TRIGGER IF EXISTS trg_manutencao_after_update //
CREATE TRIGGER trg_manutencao_after_update
AFTER UPDATE ON manutencoes
FOR EACH ROW
BEGIN
    DECLARE v_outras_ativas INT DEFAULT 0;
    IF NEW.status IN ('CONCLUIDA', 'CANCELADA') AND OLD.status NOT IN ('CONCLUIDA', 'CANCELADA') THEN
        SELECT COUNT(*) INTO v_outras_ativas
        FROM   manutencoes
        WHERE  id_modal = NEW.id_modal
          AND  status IN ('AGENDADA', 'EM_ANDAMENTO')
          AND  id_manutencao <> NEW.id_manutencao;
        IF v_outras_ativas = 0 THEN
            UPDATE modais SET status = 'DISPONIVEL' WHERE id_modal = NEW.id_modal;
        END IF;
    END IF;
END //


DROP TRIGGER IF EXISTS trg_vendas_no_delete_confirmada //
CREATE TRIGGER trg_vendas_no_delete_confirmada
BEFORE DELETE ON vendas
FOR EACH ROW
BEGIN
    IF OLD.status = 'CONFIRMADA' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RI12: Vendas confirmadas não podem ser excluídas. Registre um cancelamento.';
    END IF;
END //


DROP TRIGGER IF EXISTS trg_audit_reservas_ins //
CREATE TRIGGER trg_audit_reservas_ins
AFTER INSERT ON reservas
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
    VALUES ('reservas', NEW.id_reserva, 'INSERT', NULL,
        JSON_OBJECT('codigo', NEW.codigo, 'status', NEW.status, 'canal', NEW.canal,
                    'valor_total', NEW.valor_total, 'id_passageiro', NEW.id_passageiro,
                    'id_programacao', NEW.id_programacao));
END //

DROP TRIGGER IF EXISTS trg_audit_reservas_upd //
CREATE TRIGGER trg_audit_reservas_upd
AFTER UPDATE ON reservas
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status OR OLD.valor_total <> NEW.valor_total THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES ('reservas', NEW.id_reserva, 'UPDATE',
            JSON_OBJECT('status', OLD.status, 'valor_total', OLD.valor_total),
            JSON_OBJECT('status', NEW.status, 'valor_total', NEW.valor_total));
    END IF;
END //

DROP TRIGGER IF EXISTS trg_audit_reservas_del //
CREATE TRIGGER trg_audit_reservas_del
BEFORE DELETE ON reservas
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
    VALUES ('reservas', OLD.id_reserva, 'DELETE',
        JSON_OBJECT('codigo', OLD.codigo, 'status', OLD.status), NULL);
END //


DROP TRIGGER IF EXISTS trg_audit_pagamentos_ins //
CREATE TRIGGER trg_audit_pagamentos_ins
AFTER INSERT ON pagamentos
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
    VALUES ('pagamentos', NEW.id_pagamento, 'INSERT', NULL,
        JSON_OBJECT('tipo', NEW.tipo, 'parcelas', NEW.parcelas,
                    'valor_total', NEW.valor_total, 'status', NEW.status,
                    'id_reserva', NEW.id_reserva));
END //

DROP TRIGGER IF EXISTS trg_audit_pagamentos_upd //
CREATE TRIGGER trg_audit_pagamentos_upd
AFTER UPDATE ON pagamentos
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES ('pagamentos', NEW.id_pagamento, 'UPDATE',
            JSON_OBJECT('status', OLD.status, 'codigo_autorizacao', OLD.codigo_autorizacao),
            JSON_OBJECT('status', NEW.status, 'codigo_autorizacao', NEW.codigo_autorizacao,
                        'data_pagamento', NEW.data_pagamento));
    END IF;
END //


DROP TRIGGER IF EXISTS trg_audit_tickets_ins //
CREATE TRIGGER trg_audit_tickets_ins
AFTER INSERT ON tickets
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
    VALUES ('tickets', NEW.id_ticket, 'INSERT', NULL,
        JSON_OBJECT('codigo_ticket', NEW.codigo_ticket, 'localizador', NEW.localizador,
                    'status', NEW.status, 'id_reserva', NEW.id_reserva));
END //

DROP TRIGGER IF EXISTS trg_audit_tickets_upd //
CREATE TRIGGER trg_audit_tickets_upd
AFTER UPDATE ON tickets
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES ('tickets', NEW.id_ticket, 'UPDATE',
            JSON_OBJECT('status', OLD.status), JSON_OBJECT('status', NEW.status));
    END IF;
END //


DROP TRIGGER IF EXISTS trg_audit_vendas_upd //
CREATE TRIGGER trg_audit_vendas_upd
AFTER UPDATE ON vendas
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES ('vendas', NEW.id_venda, 'UPDATE',
            JSON_OBJECT('status', OLD.status), JSON_OBJECT('status', NEW.status));
    END IF;
END //


DROP TRIGGER IF EXISTS trg_audit_modais_upd //
CREATE TRIGGER trg_audit_modais_upd
AFTER UPDATE ON modais
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES ('modais', NEW.id_modal, 'UPDATE',
            JSON_OBJECT('status', OLD.status, 'capacidade', OLD.capacidade),
            JSON_OBJECT('status', NEW.status, 'capacidade', NEW.capacidade));
    END IF;
END //


DROP TRIGGER IF EXISTS trg_audit_funcionarios_ins //
CREATE TRIGGER trg_audit_funcionarios_ins
AFTER INSERT ON funcionarios
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
    VALUES ('funcionarios', NEW.id_funcionario, 'INSERT', NULL,
        JSON_OBJECT('codigo', NEW.codigo, 'tipo', NEW.tipo, 'nome', NEW.nome));
END //

DROP TRIGGER IF EXISTS trg_audit_funcionarios_upd //
CREATE TRIGGER trg_audit_funcionarios_upd
AFTER UPDATE ON funcionarios
FOR EACH ROW
BEGIN
    IF OLD.tipo <> NEW.tipo OR OLD.ativo <> NEW.ativo THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES ('funcionarios', NEW.id_funcionario, 'UPDATE',
            JSON_OBJECT('tipo', OLD.tipo, 'ativo', OLD.ativo),
            JSON_OBJECT('tipo', NEW.tipo, 'ativo', NEW.ativo));
    END IF;
END //


DELIMITER ;


SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO perfis (nome, descricao) VALUES
    ('ADMIN',           'Administrador do sistema com acesso total'),
    ('FUNCIONARIO',     'Atendente de ponto de venda físico'),
    ('GERENTE_PDV',     'Gerente responsável por ponto de venda físico'),
    ('GERENTE_VIRTUAL', 'Gerente de negócios virtuais — supervisiona vendas online'),
    ('CLIENTE',         'Cliente final que realiza reservas online');

INSERT INTO cidades (nome, estado, pais, identificador) VALUES
    ('Rio de Janeiro', 'Rio de Janeiro',   'Brasil',         'RIO'),
    ('São Paulo',      'São Paulo',        'Brasil',         'SAO'),
    ('Brasília',       'Distrito Federal', 'Brasil',         'BSB'),
    ('Salvador',       'Bahia',            'Brasil',         'SSA'),
    ('Fortaleza',      'Ceará',            'Brasil',         'FOR'),
    ('Manaus',         'Amazonas',         'Brasil',         'MAO'),
    ('Porto Alegre',   'Rio Grande do Sul','Brasil',         'POA'),
    ('Recife',         'Pernambuco',       'Brasil',         'REC'),
    ('Curitiba',       'Paraná',           'Brasil',         'CWB'),
    ('Belém',          'Pará',             'Brasil',         'BEL'),
    ('Buenos Aires',   'Buenos Aires',     'Argentina',      'EZE'),
    ('Lisboa',         'Lisboa',           'Portugal',       'LIS'),
    ('Miami',          'Florida',          'Estados Unidos', 'MIA'),
    ('Paris',          'Île-de-France',    'França',         'CDG'),
    ('Londres',        'Inglaterra',       'Reino Unido',    'LHR');

INSERT INTO aeroportos (codigo_iata, nome, id_cidade) VALUES
    ('GIG', 'Aeroporto Internacional do Galeão',                    1),
    ('SDU', 'Aeroporto Santos Dumont',                              1),
    ('GRU', 'Aeroporto Internacional de Guarulhos',                 2),
    ('CGH', 'Aeroporto de Congonhas',                               2),
    ('BSB', 'Aeroporto Internacional de Brasília',                  3),
    ('SSA', 'Aeroporto Internacional Dep. Luís Eduardo Magalhães',  4),
    ('FOR', 'Aeroporto Internacional Pinto Martins',                5),
    ('MAO', 'Aeroporto Internacional Eduardo Gomes',                6),
    ('POA', 'Aeroporto Internacional Salgado Filho',                7),
    ('REC', 'Aeroporto Internacional do Recife',                    8),
    ('CWB', 'Aeroporto Internacional Afonso Pena',                  9),
    ('BEL', 'Aeroporto Internacional de Belém',                    10),
    ('EZE', 'Aeroporto Internacional Ministro Pistarini',          11),
    ('LIS', 'Aeroporto Internacional Humberto Delgado',            12),
    ('MIA', 'Miami International Airport',                         13),
    ('CDG', 'Aeroporto Charles de Gaulle',                         14),
    ('LHR', 'Aeroporto de Heathrow',                               15);

INSERT INTO transportadoras (cnpj, nome, telefone, email) VALUES
    ('02012862000160', 'LATAM Airlines Brasil',    '08007278228', 'suporte@latam.com'),
    ('09296295000160', 'Azul Linhas Aéreas',       '08008800848', 'contato@azul.com.br'),
    ('07575651000159', 'Gol Linhas Aéreas',        '08009890009', 'falecomagol@gollinhasaereas.com.br'),
    ('33041260064690', 'Rede Ferroviária Federal',  '08007000800', 'ferroviaria@rff.gov.br'),
    ('60742928000175', 'Comfortbus Transporte',    '01133334444', 'contato@comfortbus.com.br'),
    ('12345678000199', 'Cruzeiros do Brasil S.A.', '02199998888', 'cruzeiros@cruzeiros.com.br');

INSERT INTO modais (codigo, tipo, modelo, ano, capacidade, status, id_transportadora, id_aeroporto_base) VALUES
    ('LATAM-A320-001', 'AVIAO',  'Airbus A320',           2019, 186,  'DISPONIVEL', 1, 1),
    ('LATAM-B777-001', 'AVIAO',  'Boeing 777-300',        2018, 396,  'DISPONIVEL', 1, 3),
    ('AZUL-E195-001',  'AVIAO',  'Embraer E195',          2021, 118,  'DISPONIVEL', 2, 3),
    ('GOL-B737-001',   'AVIAO',  'Boeing 737 MAX',        2022, 189,  'DISPONIVEL', 3, 1),
    ('CBUS-001',       'ONIBUS', 'Mercedes Benz O-400',   2020,  46,  'DISPONIVEL', 5, NULL),
    ('CBUS-002',       'ONIBUS', 'Marcopolo Paradiso G7', 2021,  50,  'DISPONIVEL', 5, NULL),
    ('RFF-TREM-001',   'TREM',   'Trem Regional IC',      2015, 300,  'DISPONIVEL', 4, NULL),
    ('CRUZEIRO-001',   'NAVIO',  'MSC Armonia',           2010, 1200, 'DISPONIVEL', 6, NULL);

INSERT INTO usuarios (email, senha_hash) VALUES
    ('admin@vvv.com.br',          '$2a$12$PLACEHOLDER_HASH_ADMIN_CHANGE_IN_PROD'),
    ('gerente.virtual@vvv.com.br','$2a$12$PLACEHOLDER_HASH_GVIRTUAL_CHANGE_IN_PROD'),
    ('gerente.pdv1@vvv.com.br',   '$2a$12$PLACEHOLDER_HASH_GPDV1_CHANGE_IN_PROD'),
    ('func.joao@vvv.com.br',      '$2a$12$PLACEHOLDER_HASH_JOAO_CHANGE_IN_PROD'),
    ('func.maria@vvv.com.br',     '$2a$12$PLACEHOLDER_HASH_MARIA_CHANGE_IN_PROD');

INSERT INTO usuarios_perfis (id_usuario, id_perfil) VALUES
    (1, 1),
    (2, 4),
    (3, 3),
    (4, 2),
    (5, 2);

INSERT INTO funcionarios (codigo, cpf, nome, tipo, id_usuario) VALUES
    ('ADMIN001', '00000000000', 'Administrador VVV', 'FUNCIONARIO',     1),
    ('GVIRT001', '11111111111', 'Carlos Negócios',   'GERENTE_VIRTUAL', 2),
    ('GPDV001',  '22222222222', 'Ana Gerente PDV',   'GERENTE_PDV',     3),
    ('FUNC001',  '33333333333', 'João Atendente',    'FUNCIONARIO',     4),
    ('FUNC002',  '44444444444', 'Maria Atendente',   'FUNCIONARIO',     5);

INSERT INTO pontos_de_venda (codigo, cnpj, nome, rua, numero, bairro, cep, cidade_endereco, estado_endereco, telefone, id_gerente) VALUES
    ('PDV-RJ-001', '12345678000101', 'VVV Centro Rio',  'Av. Rio Branco', '100', 'Centro',     '20040002', 'Rio de Janeiro', 'RJ', '02133334444', 3),
    ('PDV-SP-001', '98765432000188', 'VVV Paulista SP', 'Av. Paulista',   '900', 'Bela Vista', '01310100', 'São Paulo',      'SP', '01155556666', 3);

INSERT INTO funcionarios_pontos_de_venda (id_funcionario, id_ponto, data_inicio) VALUES
    (4, 1, '2024-01-10'),
    (4, 2, '2024-03-01'),
    (5, 1, '2024-02-01');

INSERT INTO rotas (codigo, descricao, id_cidade_origem, id_cidade_destino, tipo) VALUES
    ('RT-RIO-SAO-01', 'Rio de Janeiro → São Paulo',                    1,  2, 'DIRETA'),
    ('RT-SAO-BSB-01', 'São Paulo → Brasília',                          2,  3, 'DIRETA'),
    ('RT-RIO-SSA-01', 'Rio de Janeiro → Salvador',                     1,  4, 'DIRETA'),
    ('RT-SAO-POA-01', 'São Paulo → Porto Alegre',                      2,  7, 'DIRETA'),
    ('RT-RIO-MAO-01', 'Rio de Janeiro → Manaus (escala São Paulo)',     1,  6, 'COM_ESCALA'),
    ('RT-SAO-MIA-01', 'São Paulo → Miami',                             2, 13, 'DIRETA'),
    ('RT-RIO-LIS-01', 'Rio de Janeiro → Lisboa',                       1, 12, 'DIRETA'),
    ('RT-SAO-CDG-01', 'São Paulo → Paris (escala Lisboa)',              2, 14, 'COM_ESCALA');

INSERT INTO trechos_rota (id_rota, ordem, id_cidade_origem, id_cidade_destino, id_aeroporto_origem, id_aeroporto_destino, hora_partida, hora_chegada, tempo_estimado_min) VALUES
    (1, 1,  1,  2,  1,  3, '07:00:00', '08:10:00',  70),
    (2, 1,  2,  3,  3,  5, '10:00:00', '11:30:00',  90),
    (3, 1,  1,  4,  1,  6, '09:00:00', '11:00:00', 120),
    (4, 1,  2,  7,  3,  9, '06:00:00', '07:40:00', 100),
    (5, 1,  1,  2,  1,  3, '06:00:00', '07:10:00',  70),
    (5, 2,  2,  6,  3,  8, '09:00:00', '12:30:00', 210),
    (6, 1,  2, 13,  3, 15, '23:00:00', '07:30:00', 570),
    (7, 1,  1, 12,  1, 14, '22:00:00', '11:00:00', 660),
    (8, 1,  2, 12,  3, 14, '22:00:00', '12:00:00', 660),
    (8, 2, 12, 14, 14, 16, '14:00:00', '17:20:00', 140);

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status) VALUES
    (1, 1, '2026-04-01', 186,  350.00, 'ATIVO'),
    (1, 1, '2026-04-02', 186,  350.00, 'ATIVO'),
    (1, 4, '2026-04-01', 189,  320.00, 'ATIVO'),
    (2, 2, '2026-04-01', 396,  420.00, 'ATIVO'),
    (2, 3, '2026-04-02', 118,  390.00, 'ATIVO'),
    (3, 4, '2026-04-01', 189,  480.00, 'ATIVO'),
    (4, 3, '2026-04-01', 118,  310.00, 'ATIVO'),
    (5, 2, '2026-04-03', 396,  750.00, 'ATIVO'),
    (6, 2, '2026-04-05', 396, 3200.00, 'ATIVO'),
    (7, 2, '2026-04-10', 396, 4100.00, 'ATIVO'),
    (8, 2, '2026-04-12', 396, 4800.00, 'ATIVO');

INSERT INTO clientes (codigo, nome, cpf, email, telefone)
SELECT 'CLI000001', 'Cliente Demonstração', '52998224725', 'cliente.demo@vvv.com.br', '21999990000'
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE cpf = '52998224725');

SET FOREIGN_KEY_CHECKS = 1;

SET UNIQUE_CHECKS      = @OLD_UNIQUE_CHECKS;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET SQL_MODE           = @OLD_SQL_MODE;
