-- Script de criação do banco do Catálogo Simples (MySQL 8+)
CREATE DATABASE IF NOT EXISTS catalogo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE catalogo;

CREATE TABLE IF NOT EXISTS item_midia (
    id             INT           PRIMARY KEY AUTO_INCREMENT,
    titulo         VARCHAR(255)  NOT NULL,
    autor_diretor  VARCHAR(255)  NOT NULL,
    ano_lancamento INT           NOT NULL,
    genero         VARCHAR(100)  NOT NULL,
    sinopse        TEXT,
    tipo_midia     VARCHAR(50)   NOT NULL,
    -- Segunda linha de defesa: o banco também recusa dados inválidos
    CONSTRAINT ck_ano  CHECK (ano_lancamento BETWEEN 1000 AND 9999),
    CONSTRAINT ck_tipo CHECK (tipo_midia IN ('Livro', 'Filme', 'Série'))
);
