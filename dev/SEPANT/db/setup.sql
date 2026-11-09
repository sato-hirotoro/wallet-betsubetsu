-- =========================================================
-- SEPANT DBセットアップ（DB設計書 2章・7章・8章）
--   実行: /usr/local/mysql/bin/mysql -u root -p < db/setup.sql
--   ※ 既存の sepant データベースは削除して作り直す
-- =========================================================
DROP DATABASE IF EXISTS sepant;
CREATE DATABASE sepant CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 接続ユーザー（SELECT・INSERT・UPDATE・DELETE のみ）
CREATE USER IF NOT EXISTS 'sepant_app'@'localhost' IDENTIFIED BY 'sepant_app_pw';
GRANT SELECT, INSERT, UPDATE, DELETE ON sepant.* TO 'sepant_app'@'localhost';
FLUSH PRIVILEGES;

USE sepant;

CREATE TABLE m_user (
    user_id   INT         NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(10) NOT NULL,
    PRIMARY KEY (user_id)
) ENGINE = InnoDB;

CREATE TABLE m_himoku (
    himoku_cd   CHAR(3)     NOT NULL,
    himoku_name VARCHAR(20) NOT NULL,
    kubun       CHAR(2)     NOT NULL,
    hyoji_jun   INT         NOT NULL,
    PRIMARY KEY (himoku_cd),
    CONSTRAINT chk_himoku_kubun CHECK (kubun IN ('01', '02', '03', '04', '05', '06'))
) ENGINE = InnoDB;

CREATE TABLE t_hendohi_meisai (
    meisai_id   INT         NOT NULL AUTO_INCREMENT,
    taisho_yymm CHAR(6)     NOT NULL,
    user_id     INT         NOT NULL,
    himoku_cd   CHAR(3)     NOT NULL,
    shiharai_dd DATE        NOT NULL,
    kingaku     INT         NOT NULL,
    biko        VARCHAR(50) NULL,
    PRIMARY KEY (meisai_id),
    INDEX idx_hendohi_cell (taisho_yymm, user_id, himoku_cd),
    CONSTRAINT fk_hendohi_user   FOREIGN KEY (user_id)   REFERENCES m_user (user_id),
    CONSTRAINT fk_hendohi_himoku FOREIGN KEY (himoku_cd) REFERENCES m_himoku (himoku_cd),
    CONSTRAINT chk_hendohi_kingaku CHECK (kingaku >= 1)
) ENGINE = InnoDB;

CREATE TABLE t_kotei_settei (
    settei_id INT         NOT NULL AUTO_INCREMENT,
    himoku_cd CHAR(3)     NOT NULL,
    user_id   INT         NOT NULL,
    kingaku   INT         NOT NULL,
    kaishi_mm CHAR(6)     NOT NULL,
    shuryo_mm CHAR(6)     NOT NULL,
    biko      VARCHAR(50) NULL,
    PRIMARY KEY (settei_id),
    INDEX idx_kotei_kikan (kaishi_mm, shuryo_mm),
    CONSTRAINT fk_kotei_user   FOREIGN KEY (user_id)   REFERENCES m_user (user_id),
    CONSTRAINT fk_kotei_himoku FOREIGN KEY (himoku_cd) REFERENCES m_himoku (himoku_cd),
    CONSTRAINT chk_kotei_kingaku CHECK (kingaku >= 1),
    CONSTRAINT chk_kotei_kikan   CHECK (shuryo_mm >= kaishi_mm)
) ENGINE = InnoDB;

CREATE TABLE t_seisan (
    taisho_yymm CHAR(6) NOT NULL,
    seisan_dd   DATE    NOT NULL,
    PRIMARY KEY (taisho_yymm)
) ENGINE = InnoDB;

-- 初期データ（DB設計書 8章）
INSERT INTO m_user (user_id, user_name) VALUES
    (1, '夫'),
    (2, '妻');

INSERT INTO m_himoku (himoku_cd, himoku_name, kubun, hyoji_jun) VALUES
    ('001', '食費',     '01', 10),
    ('002', '日用品',   '01', 20),
    ('003', '光熱費',   '01', 30),
    ('004', '携帯代',   '02', 40),
    ('005', '家賃',     '03', 50),
    ('006', 'NHK',      '03', 60),
    ('007', 'Wi-Fi',    '03', 70),
    ('008', '結婚資金', '04', 80),
    ('009', '住居手当', '05', 90),
    ('010', '皿洗い',   '06', 100),
    ('011', '夕食',     '06', 110);
