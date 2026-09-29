-- ============================================================
--  SISTEMA VAI & VOLTA VIAGENS (VVV)
--  Arquivo 01 — Criação do Banco de Dados
--  SGBD   : MySQL 8.x
--  Engine : InnoDB
--  Charset: utf8mb4 / utf8mb4_unicode_ci
-- ============================================================

SET @OLD_UNIQUE_CHECKS    = @@UNIQUE_CHECKS,     UNIQUE_CHECKS    = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE         = @@SQL_MODE,           SQL_MODE         = 'TRADITIONAL';

DROP DATABASE IF EXISTS vvv;

CREATE DATABASE vvv
    CHARACTER SET  utf8mb4
    COLLATE        utf8mb4_unicode_ci;

USE vvv;

-- ============================================================
-- Configurações de sessão recomendadas para produção
-- ============================================================
SET time_zone = '-03:00';   -- Fuso horário: América/São_Paulo
