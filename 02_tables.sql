-- ============================================================
--  SISTEMA VAI & VOLTA VIAGENS (VVV)
--  Arquivo 02 — DDL: Criação das Tabelas
--  Ordem respeitada por dependência de FK
-- ============================================================

USE vvv;

SET FOREIGN_KEY_CHECKS = 0;


-- ============================================================
-- GRUPO 1: AUTENTICAÇÃO E CONTROLE DE ACESSO
-- ============================================================

CREATE TABLE perfis (
    id_perfil   TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome        ENUM(
                    'CLIENTE',
                    'FUNCIONARIO',
                    'GERENTE_PDV',
                    'GERENTE_VIRTUAL',
                    'ADMIN'
                )                NOT NULL,
    descricao   VARCHAR(200)     NULL,

    PRIMARY KEY (id_perfil),
    UNIQUE KEY  uq_perfis_nome (nome)
) ENGINE = InnoDB
  COMMENT = 'Papéis disponíveis no sistema';


CREATE TABLE usuarios (
    id_usuario    BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    email         VARCHAR(150)     NOT NULL,
    senha_hash    VARCHAR(255)     NOT NULL  COMMENT 'Hash bcrypt — nunca armazenar senha em texto claro',
    ativo         TINYINT(1)       NOT NULL  DEFAULT 1,
    criado_em     DATETIME         NOT NULL  DEFAULT CURRENT_TIMESTAMP,
    ultimo_acesso DATETIME         NULL,

    PRIMARY KEY (id_usuario),
    UNIQUE KEY  uq_usuarios_email (email)
) ENGINE = InnoDB
  COMMENT = 'Credenciais de acesso ao sistema';


CREATE TABLE usuarios_perfis (
    id_usuario   BIGINT UNSIGNED  NOT NULL,
    id_perfil    TINYINT UNSIGNED NOT NULL,
    concedido_em DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_usuario, id_perfil),
    CONSTRAINT fk_up_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_up_perfil  FOREIGN KEY (id_perfil)  REFERENCES perfis   (id_perfil)  ON DELETE RESTRICT
) ENGINE = InnoDB
  COMMENT = 'Associação N:N entre usuários e perfis de acesso';


-- ============================================================
-- GRUPO 2: GEOGRAFIA
-- ============================================================

CREATE TABLE cidades (
    id_cidade     INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome          VARCHAR(100) NOT NULL,
    estado        VARCHAR(100) NOT NULL,
    pais          VARCHAR(100) NOT NULL DEFAULT 'Brasil',
    -- RN11: identificador obrigatório de 3 letras maiúsculas
    identificador CHAR(3)      NOT NULL COMMENT 'RN11: 3 letras maiúsculas, ex: RIO, GRU',

    PRIMARY KEY (id_cidade),
    UNIQUE KEY  uq_cidades_identificador (identificador),
    CONSTRAINT  chk_cidades_identificador CHECK (identificador REGEXP '^[A-Z]{3}$')
) ENGINE = InnoDB
  COMMENT = 'Cidades de origem e destino das viagens';


CREATE TABLE aeroportos (
    id_aeroporto INT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo_iata  CHAR(3)      NOT NULL COMMENT 'Código IATA — RN12',
    nome         VARCHAR(150) NOT NULL,
    id_cidade    INT UNSIGNED NOT NULL,

    PRIMARY KEY (id_aeroporto),
    UNIQUE KEY  uq_aeroportos_iata (codigo_iata),
    CONSTRAINT  fk_aeroportos_cidade FOREIGN KEY (id_cidade) REFERENCES cidades (id_cidade) ON DELETE RESTRICT
) ENGINE = InnoDB
  COMMENT = 'Aeroportos vinculados a cidades — obrigatório para viagens aéreas (RN12)';


-- ============================================================
-- GRUPO 3: ESTRUTURA ORGANIZACIONAL
-- ============================================================

CREATE TABLE transportadoras (
    id_transportadora INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    cnpj              CHAR(14)      NOT NULL COMMENT 'Apenas dígitos',
    nome              VARCHAR(150)  NOT NULL,
    telefone          VARCHAR(20)   NULL,
    email             VARCHAR(150)  NULL,
    ativo             TINYINT(1)    NOT NULL DEFAULT 1,
    criado_em         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_transportadora),
    UNIQUE KEY  uq_transportadoras_cnpj (cnpj)
) ENGINE = InnoDB
  COMMENT = 'Empresas proprietárias dos modais de transporte';


-- funcionarios criado ANTES de pontos_de_venda (PDV depende de funcionario como gerente)
CREATE TABLE funcionarios (
    id_funcionario  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo          VARCHAR(20)     NOT NULL,
    cpf             CHAR(11)        NOT NULL COMMENT 'Apenas dígitos',
    nome            VARCHAR(150)    NOT NULL,

    -- Endereço estruturado
    rua             VARCHAR(150)    NULL,
    numero          VARCHAR(20)     NULL,
    complemento     VARCHAR(100)    NULL,
    bairro          VARCHAR(100)    NULL,
    cep             CHAR(8)         NULL COMMENT 'Apenas dígitos',
    cidade_endereco VARCHAR(100)    NULL,
    estado_endereco CHAR(2)         NULL,

    -- Herança por discriminador — Opção A acordada
    tipo            ENUM(
                        'FUNCIONARIO',
                        'GERENTE_PDV',
                        'GERENTE_VIRTUAL'
                    )               NOT NULL DEFAULT 'FUNCIONARIO'
                    COMMENT 'GERENTE_PDV: pontos físicos | GERENTE_VIRTUAL: canal online (RN31)',

    ativo           TINYINT(1)      NOT NULL DEFAULT 1,
    criado_em       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario      BIGINT UNSIGNED NOT NULL,

    PRIMARY KEY (id_funcionario),
    UNIQUE KEY  uq_funcionarios_codigo  (codigo),
    UNIQUE KEY  uq_funcionarios_cpf     (cpf),
    UNIQUE KEY  uq_funcionarios_usuario (id_usuario),
    CONSTRAINT  fk_funcionarios_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT
) ENGINE = InnoDB
  COMMENT = 'Funcionários, gerentes de PDV e gerentes virtuais (herança por discriminador)';


CREATE TABLE pontos_de_venda (
    id_ponto        INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    codigo          VARCHAR(20)   NOT NULL,
    cnpj            CHAR(14)      NOT NULL COMMENT 'Apenas dígitos',
    nome            VARCHAR(150)  NOT NULL,

    -- Endereço estruturado
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

    -- RN26: cada PDV deve ter um gerente responsável
    -- nullable para permitir INSERT antes de designar gerente (trigger valida o tipo)
    id_gerente      BIGINT UNSIGNED NULL COMMENT 'RN26: gerente responsável do tipo GERENTE_PDV',

    PRIMARY KEY (id_ponto),
    UNIQUE KEY  uq_pdv_codigo (codigo),
    UNIQUE KEY  uq_pdv_cnpj   (cnpj),
    CONSTRAINT  fk_pdv_gerente FOREIGN KEY (id_gerente) REFERENCES funcionarios (id_funcionario) ON DELETE SET NULL
) ENGINE = InnoDB
  COMMENT = 'Pontos de venda físicos (RN25, RN26)';


CREATE TABLE funcionarios_pontos_de_venda (
    id_fpv          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_funcionario  BIGINT UNSIGNED NOT NULL,
    id_ponto        INT UNSIGNED    NOT NULL,
    data_inicio     DATE            NOT NULL,
    data_fim        DATE            NULL COMMENT 'NULL = vínculo ativo sem prazo definido',
    ativo           TINYINT(1)      NOT NULL DEFAULT 1,

    PRIMARY KEY (id_fpv),
    -- Sem UNIQUE em (funcionario, ponto, ativo): permitir histórico (ex: mesmo funcionário
    -- que saiu e voltou ao mesmo PDV). A regra de máx. 2 pontos é controlada pelo trigger.
    CONSTRAINT fk_fpv_funcionario FOREIGN KEY (id_funcionario) REFERENCES funcionarios    (id_funcionario) ON DELETE CASCADE,
    CONSTRAINT fk_fpv_ponto       FOREIGN KEY (id_ponto)       REFERENCES pontos_de_venda (id_ponto)       ON DELETE CASCADE
    -- RN28: trigger trg_fpv_max_dois_pontos garante máx. 2 pontos ativos por funcionário
) ENGINE = InnoDB
  COMMENT = 'Vínculo N:N entre funcionários e PDVs — máx. 2 por funcionário (RN28)';


-- ============================================================
-- GRUPO 4: MODAIS E MANUTENÇÃO
-- ============================================================

CREATE TABLE modais (
    id_modal          INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    codigo            VARCHAR(20)     NOT NULL,
    tipo              ENUM(
                          'AVIAO',
                          'TREM',
                          'ONIBUS',
                          'NAVIO'
                      )               NOT NULL,
    modelo            VARCHAR(100)    NOT NULL,
    ano               YEAR            NOT NULL,
    capacidade        SMALLINT UNSIGNED NOT NULL COMMENT 'Número total de assentos/vagas',
    status            ENUM(
                          'DISPONIVEL',
                          'MANUTENCAO',
                          'INATIVO'
                      )               NOT NULL DEFAULT 'DISPONIVEL',
    id_transportadora INT UNSIGNED    NOT NULL,

    -- Opção A: campo nullable; obrigatório por CHK quando tipo = AVIAO (RN12)
    id_aeroporto_base INT UNSIGNED    NULL
        COMMENT 'Aeroporto base — obrigatório para AVIAO (RN12), NULL para outros modais',

    ativo             TINYINT(1)      NOT NULL DEFAULT 1,
    criado_em         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_modal),
    UNIQUE KEY  uq_modais_codigo (codigo),
    CONSTRAINT  fk_modais_transportadora FOREIGN KEY (id_transportadora) REFERENCES transportadoras (id_transportadora) ON DELETE RESTRICT,
    -- ON DELETE RESTRICT (não SET NULL): coluna usada em CHECK — MySQL 8 não permite SET NULL + CHECK na mesma coluna (ERROR 3823)
    CONSTRAINT  fk_modais_aeroporto      FOREIGN KEY (id_aeroporto_base) REFERENCES aeroportos      (id_aeroporto)       ON DELETE RESTRICT,
    -- RN12: avião DEVE ter aeroporto base
    CONSTRAINT  chk_modais_aviao_aeroporto CHECK (
        NOT (tipo = 'AVIAO' AND id_aeroporto_base IS NULL)
    )
) ENGINE = InnoDB
  COMMENT = 'Meios de transporte (avião, trem, ônibus, navio) — RN15, RN16';


CREATE TABLE manutencoes (
    id_manutencao INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    id_modal      INT UNSIGNED  NOT NULL,
    data_inicio   DATE          NOT NULL,
    data_fim      DATE          NOT NULL,
    descricao     TEXT          NULL,
    status        ENUM(
                      'AGENDADA',
                      'EM_ANDAMENTO',
                      'CONCLUIDA',
                      'CANCELADA'
                  )             NOT NULL DEFAULT 'AGENDADA',
    criado_em     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_manutencao),
    CONSTRAINT fk_manutencoes_modal  FOREIGN KEY (id_modal) REFERENCES modais (id_modal) ON DELETE RESTRICT,
    CONSTRAINT chk_manutencoes_datas CHECK (data_fim >= data_inicio)
) ENGINE = InnoDB
  COMMENT = 'Períodos de manutenção dos modais — RN17, RN18';


-- ============================================================
-- GRUPO 5: ROTAS E PROGRAMAÇÃO DE VIAGENS
-- ============================================================

CREATE TABLE rotas (
    id_rota           INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    codigo            VARCHAR(20)   NOT NULL,
    descricao         VARCHAR(200)  NULL,
    id_cidade_origem  INT UNSIGNED  NOT NULL,
    id_cidade_destino INT UNSIGNED  NOT NULL,
    tipo              ENUM(
                          'DIRETA',
                          'COM_ESCALA'
                      )             NOT NULL DEFAULT 'DIRETA' COMMENT 'RN10',
    ativo             TINYINT(1)    NOT NULL DEFAULT 1,
    criado_em         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_rota),
    UNIQUE KEY  uq_rotas_codigo (codigo),
    CONSTRAINT  fk_rotas_origem  FOREIGN KEY (id_cidade_origem)  REFERENCES cidades (id_cidade) ON DELETE RESTRICT,
    CONSTRAINT  fk_rotas_destino FOREIGN KEY (id_cidade_destino) REFERENCES cidades (id_cidade) ON DELETE RESTRICT,
    -- RN09: origem != destino
    CONSTRAINT  chk_rotas_cidades CHECK (id_cidade_origem <> id_cidade_destino)
) ENGINE = InnoDB
  COMMENT = 'Rotas pré-cadastradas entre cidades (RN09, RN10)';


CREATE TABLE trechos_rota (
    id_trecho              INT UNSIGNED       NOT NULL AUTO_INCREMENT,
    id_rota                INT UNSIGNED       NOT NULL,
    ordem                  TINYINT UNSIGNED   NOT NULL COMMENT 'Sequência do trecho na rota',
    id_cidade_origem       INT UNSIGNED       NOT NULL,
    id_cidade_destino      INT UNSIGNED       NOT NULL,

    -- RN12: aeroporto obrigatório em trechos aéreos (validado na camada de app + RN do modal)
    id_aeroporto_origem    INT UNSIGNED       NULL,
    id_aeroporto_destino   INT UNSIGNED       NULL,

    hora_partida           TIME               NOT NULL COMMENT 'RN14',
    hora_chegada           TIME               NOT NULL COMMENT 'RN14',
    tempo_estimado_min     SMALLINT UNSIGNED  NOT NULL COMMENT 'RN14: tempo estimado em minutos',

    PRIMARY KEY (id_trecho),
    UNIQUE KEY  uq_trechos_rota_ordem (id_rota, ordem),
    CONSTRAINT  fk_trechos_rota              FOREIGN KEY (id_rota)               REFERENCES rotas      (id_rota)      ON DELETE CASCADE,
    CONSTRAINT  fk_trechos_cidade_orig       FOREIGN KEY (id_cidade_origem)      REFERENCES cidades    (id_cidade)    ON DELETE RESTRICT,
    CONSTRAINT  fk_trechos_cidade_dest       FOREIGN KEY (id_cidade_destino)     REFERENCES cidades    (id_cidade)    ON DELETE RESTRICT,
    CONSTRAINT  fk_trechos_aeroporto_orig    FOREIGN KEY (id_aeroporto_origem)   REFERENCES aeroportos (id_aeroporto) ON DELETE SET NULL,
    CONSTRAINT  fk_trechos_aeroporto_dest    FOREIGN KEY (id_aeroporto_destino)  REFERENCES aeroportos (id_aeroporto) ON DELETE SET NULL
) ENGINE = InnoDB
  COMMENT = 'Segmentos ordenados de cada rota — suporta escalas (RN10, RN14)';


CREATE TABLE programacoes_viagem (
    id_programacao    INT UNSIGNED       NOT NULL AUTO_INCREMENT,
    id_rota           INT UNSIGNED       NOT NULL,
    id_modal          INT UNSIGNED       NOT NULL,
    data_viagem       DATE               NOT NULL,
    vagas_disponiveis SMALLINT UNSIGNED  NOT NULL COMMENT 'Decrementado a cada reserva criada',
    valor_base        DECIMAL(10,2)      NOT NULL COMMENT 'Valor da passagem antes de descontos',
    status            ENUM(
                          'ATIVO',
                          'CANCELADO',
                          'ENCERRADO'
                      )                 NOT NULL DEFAULT 'ATIVO',
    criado_em         DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_programacao),
    UNIQUE KEY  uq_prog_rota_modal_data (id_rota, id_modal, data_viagem),
    CONSTRAINT  fk_prog_rota   FOREIGN KEY (id_rota)  REFERENCES rotas   (id_rota)   ON DELETE RESTRICT,
    CONSTRAINT  fk_prog_modal  FOREIGN KEY (id_modal) REFERENCES modais  (id_modal)  ON DELETE RESTRICT,
    CONSTRAINT  chk_prog_vagas CHECK (vagas_disponiveis >= 0),
    CONSTRAINT  chk_prog_valor CHECK (valor_base > 0)
) ENGINE = InnoDB
  COMMENT = 'Instância operacional: rota + modal + data. Ponto de controle de capacidade (RN07)';


-- ============================================================
-- GRUPO 6: PASSAGEIROS
-- ============================================================

CREATE TABLE passageiros (
    id_passageiro   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo          VARCHAR(20)     NOT NULL,
    cpf             CHAR(11)        NOT NULL COMMENT 'Apenas dígitos — RN06',
    nome            VARCHAR(150)    NOT NULL COMMENT 'RN06',

    -- data_nascimento ao invés de idade: o dado não envelhece
    -- RN03, RN04, RN05: idade calculada dinamicamente
    data_nascimento DATE            NOT NULL,

    telefone        VARCHAR(20)     NULL COMMENT 'RN06',
    profissao       VARCHAR(100)    NULL COMMENT 'RN06',

    -- Endereço estruturado — RN06
    rua             VARCHAR(150)    NULL,
    numero          VARCHAR(20)     NULL,
    complemento     VARCHAR(100)    NULL,
    bairro          VARCHAR(100)    NULL,
    cep             CHAR(8)         NULL,
    cidade_endereco VARCHAR(100)    NULL,
    estado_endereco CHAR(2)         NULL,

    ativo           TINYINT(1)      NOT NULL DEFAULT 1,
    criado_em       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- nullable: funcionário pode cadastrar passageiro sem conta no sistema
    id_usuario      BIGINT UNSIGNED NULL,

    PRIMARY KEY (id_passageiro),
    UNIQUE KEY  uq_passageiros_codigo (codigo),
    UNIQUE KEY  uq_passageiros_cpf    (cpf),
    CONSTRAINT  fk_passageiros_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE SET NULL
) ENGINE = InnoDB
  COMMENT = 'Dados dos passageiros (RN06). Idade sempre calculada via data_nascimento';


-- ============================================================
-- GRUPO 7: RESERVAS, PAGAMENTOS E TICKETS
-- ============================================================

CREATE TABLE reservas (
    id_reserva      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo          VARCHAR(20)     NOT NULL,
    data_criacao    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status          ENUM(
                        'PENDENTE',
                        'CONFIRMADA',
                        'CANCELADA'
                    )               NOT NULL DEFAULT 'PENDENTE',
    canal           ENUM(
                        'ONLINE',
                        'PRESENCIAL'
                    )               NOT NULL COMMENT 'RF11 / RF12',

    valor_bruto     DECIMAL(10,2)   NOT NULL COMMENT 'Valor original sem desconto',
    valor_desconto  DECIMAL(10,2)   NOT NULL DEFAULT 0.00 COMMENT 'RN04: 40% automático para 2–10 anos',
    valor_total     DECIMAL(10,2)   NOT NULL COMMENT 'valor_bruto - valor_desconto; calculado via trigger',

    -- RN01: uma reserva pertence a exatamente um passageiro
    id_passageiro   BIGINT UNSIGNED NOT NULL,
    id_programacao  INT UNSIGNED    NOT NULL,

    -- RN04: acompanhante obrigatório para passageiros entre 2 e 10 anos
    id_acompanhante BIGINT UNSIGNED NULL COMMENT 'RN04: passageiro adulto >21 anos; obrigatório se 2–10 anos',

    PRIMARY KEY (id_reserva),
    UNIQUE KEY  uq_reservas_codigo (codigo),
    CONSTRAINT  fk_res_passageiro   FOREIGN KEY (id_passageiro)  REFERENCES passageiros        (id_passageiro)  ON DELETE RESTRICT,
    CONSTRAINT  fk_res_programacao  FOREIGN KEY (id_programacao) REFERENCES programacoes_viagem (id_programacao) ON DELETE RESTRICT,
    CONSTRAINT  fk_res_acompanhante FOREIGN KEY (id_acompanhante) REFERENCES passageiros        (id_passageiro)  ON DELETE RESTRICT,
    CONSTRAINT  chk_res_valores     CHECK (valor_bruto >= 0 AND valor_desconto >= 0 AND valor_total >= 0)
) ENGINE = InnoDB
  COMMENT = 'Reservas de passagens — RN01, RN02, RN07, RN08';


CREATE TABLE pagamentos (
    id_pagamento       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_reserva         BIGINT UNSIGNED NOT NULL,
    tipo               ENUM(
                           'CREDITO',
                           'DEBITO'
                       )               NOT NULL COMMENT 'RN19',
    parcelas           TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'RN20: até 4 sem juros | RN21: acima de 4 com 5%',
    valor_bruto        DECIMAL(10,2)   NOT NULL,
    valor_juros        DECIMAL(10,2)   NOT NULL DEFAULT 0.00 COMMENT 'Calculado via trigger (RN21)',
    valor_total        DECIMAL(10,2)   NOT NULL COMMENT 'valor_bruto + valor_juros',
    valor_parcela      DECIMAL(10,2)   NOT NULL COMMENT 'valor_total / parcelas',
    status             ENUM(
                           'PENDENTE',
                           'APROVADO',
                           'RECUSADO',
                           'ESTORNADO'
                       )               NOT NULL DEFAULT 'PENDENTE',
    codigo_autorizacao VARCHAR(100)    NULL COMMENT 'Código retornado pela operadora de cartão',
    data_pagamento     DATETIME        NULL,
    criado_em          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_pagamento),
    -- RN22: 1:1 com reserva — um pagamento por reserva
    UNIQUE KEY  uq_pagamentos_reserva (id_reserva),
    CONSTRAINT  fk_pag_reserva        FOREIGN KEY (id_reserva) REFERENCES reservas (id_reserva) ON DELETE RESTRICT,
    -- RN19: débito não pode ser parcelado
    CONSTRAINT  chk_pag_debito        CHECK (NOT (tipo = 'DEBITO' AND parcelas > 1)),
    -- Limite de parcelas
    CONSTRAINT  chk_pag_parcelas      CHECK (parcelas BETWEEN 1 AND 12)
) ENGINE = InnoDB
  COMMENT = 'Pagamentos por cartão de crédito ou débito — RN19, RN20, RN21, RN22';


CREATE TABLE tickets (
    id_ticket          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo_ticket      VARCHAR(30)     NOT NULL,
    localizador        CHAR(8)         NOT NULL COMMENT 'Código alfanumérico único para check-in',
    data_emissao       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_passagem      ENUM(
                           'ECONOMICA',
                           'EXECUTIVA',
                           'PRIMEIRA_CLASSE'
                       )               NOT NULL DEFAULT 'ECONOMICA' COMMENT 'RN24',
    hora_partida       TIME            NOT NULL COMMENT 'RN24',
    hora_chegada       TIME            NOT NULL COMMENT 'RN24',
    tempo_estimado_min SMALLINT UNSIGNED NOT NULL COMMENT 'RN24: tempo total em minutos',
    status             ENUM(
                           'ATIVO',
                           'CANCELADO',
                           'USADO'
                       )               NOT NULL DEFAULT 'ATIVO',
    id_reserva         BIGINT UNSIGNED NOT NULL,
    id_pagamento       BIGINT UNSIGNED NOT NULL,

    PRIMARY KEY (id_ticket),
    UNIQUE KEY  uq_tickets_codigo      (codigo_ticket),
    UNIQUE KEY  uq_tickets_localizador (localizador),
    UNIQUE KEY  uq_tickets_reserva     (id_reserva) COMMENT '1:1 com reserva',
    CONSTRAINT  fk_tkt_reserva   FOREIGN KEY (id_reserva)   REFERENCES reservas  (id_reserva)   ON DELETE RESTRICT,
    CONSTRAINT  fk_tkt_pagamento FOREIGN KEY (id_pagamento) REFERENCES pagamentos (id_pagamento) ON DELETE RESTRICT
) ENGINE = InnoDB
  COMMENT = 'Tickets emitidos automaticamente após pagamento aprovado — RN23, RN24';


-- ============================================================
-- GRUPO 8: VENDAS (base + subtipos)
-- ============================================================

CREATE TABLE vendas (
    id_venda    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_reserva  BIGINT UNSIGNED NOT NULL,
    data_venda  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(10,2)   NOT NULL,
    status      ENUM(
                    'PENDENTE',
                    'CONFIRMADA',
                    'CANCELADA'
                )               NOT NULL DEFAULT 'PENDENTE',

    PRIMARY KEY (id_venda),
    UNIQUE KEY  uq_vendas_reserva (id_reserva) COMMENT '1:1 com reserva',
    CONSTRAINT  fk_venda_reserva FOREIGN KEY (id_reserva) REFERENCES reservas (id_reserva) ON DELETE RESTRICT
    -- RI12: trigger trg_vendas_no_delete_confirmada impede exclusão de venda confirmada
) ENGINE = InnoDB
  COMMENT = 'Registro base de vendas (RF11, RF12) — subtipos: vendas_presenciais / vendas_online';


CREATE TABLE vendas_presenciais (
    id_venda        BIGINT UNSIGNED NOT NULL COMMENT 'PK compartilhada com vendas',
    id_funcionario  BIGINT UNSIGNED NOT NULL COMMENT 'Quem efetuou a venda',
    id_ponto        INT UNSIGNED    NOT NULL COMMENT 'PDV onde ocorreu a venda',

    -- RN29: confirmar manualmente em PDV físico
    confirmado_por  BIGINT UNSIGNED NULL COMMENT 'Funcionário que confirmou (RN29)',
    data_confirmacao DATETIME       NULL,

    PRIMARY KEY (id_venda),
    CONSTRAINT fk_vp_venda       FOREIGN KEY (id_venda)        REFERENCES vendas          (id_venda)          ON DELETE CASCADE,
    CONSTRAINT fk_vp_funcionario FOREIGN KEY (id_funcionario)  REFERENCES funcionarios    (id_funcionario)    ON DELETE RESTRICT,
    CONSTRAINT fk_vp_ponto       FOREIGN KEY (id_ponto)        REFERENCES pontos_de_venda (id_ponto)          ON DELETE RESTRICT,
    CONSTRAINT fk_vp_confirmador FOREIGN KEY (confirmado_por)  REFERENCES funcionarios    (id_funcionario)    ON DELETE SET NULL
) ENGINE = InnoDB
  COMMENT = 'Detalhes de vendas realizadas em pontos físicos — RF11, RN29';


CREATE TABLE vendas_online (
    id_venda          BIGINT UNSIGNED NOT NULL COMMENT 'PK compartilhada com vendas',

    -- RN31: gerente de negócios virtuais supervisiona
    id_gerente_virtual BIGINT UNSIGNED NOT NULL COMMENT 'RN31: deve ser do tipo GERENTE_VIRTUAL',
    data_aprovacao     DATETIME        NULL,
    status_aprovacao   ENUM(
                           'PENDENTE',
                           'APROVADA',
                           'RECUSADA'
                       )               NOT NULL DEFAULT 'PENDENTE' COMMENT 'RF17, RN32',

    PRIMARY KEY (id_venda),
    CONSTRAINT fk_vo_venda   FOREIGN KEY (id_venda)           REFERENCES vendas       (id_venda)          ON DELETE CASCADE,
    CONSTRAINT fk_vo_gerente FOREIGN KEY (id_gerente_virtual) REFERENCES funcionarios (id_funcionario)    ON DELETE RESTRICT
    -- trigger trg_vo_valida_gerente garante que id_gerente_virtual seja GERENTE_VIRTUAL
) ENGINE = InnoDB
  COMMENT = 'Detalhes de vendas online — RF12, RF17, RN30–RN34';


-- ============================================================
-- GRUPO 9: AUDITORIA
-- ============================================================

CREATE TABLE log_auditoria (
    id_log           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tabela           VARCHAR(60)     NOT NULL COMMENT 'Nome da tabela auditada',
    id_registro      BIGINT UNSIGNED NOT NULL COMMENT 'PK do registro alterado',
    operacao         ENUM(
                         'INSERT',
                         'UPDATE',
                         'DELETE'
                     )               NOT NULL,
    dados_anteriores JSON            NULL COMMENT 'Estado antes da alteração (NULL em INSERT)',
    dados_novos      JSON            NULL COMMENT 'Estado após a alteração (NULL em DELETE)',
    id_usuario       BIGINT UNSIGNED NULL COMMENT 'NULL = operação de sistema/trigger',
    data_hora        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ip_address       VARCHAR(45)     NULL COMMENT 'IPv4 ou IPv6',

    PRIMARY KEY (id_log),
    -- FKs intencionalmente ausentes em id_registro (tabela dinâmica)
    CONSTRAINT fk_log_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE SET NULL
) ENGINE = InnoDB
  COMMENT = 'Log centralizado de auditoria — RnF06';


SET FOREIGN_KEY_CHECKS = 1;
