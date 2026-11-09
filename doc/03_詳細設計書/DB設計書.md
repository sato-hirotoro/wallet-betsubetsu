# DB設計書:SEPANT（折半額計算システム）

| 項目 | 内容 |
|---|---|
| システム名 | SEPANT（折半額計算システム） |
| 対象 | フェーズ1 |
| 作成日 | 2026-10-19 |
| 前提文書 | [基本設計書](../02_基本設計書/基本設計書.md) |

---

## 1. 概要

### 1.1 本書の目的
基本設計書の5章（データ設計）で定めた論理設計をもとに、MySQL上で実現するための物理設計を定める。

### 1.2 基本設計からの変更点
| No | 変更内容 | 理由 |
|---|---|---|
| 1 | 区分（kubun）のコード値を、折半・立替それぞれ変動費と固定費に分けた（6値）。 | 基本設計の4値では、食費（変動費）と家賃（固定費）がどちらも「折半」になり、ホームの「変動費」「固定費」の行や、IMG-03の費目プルダウンを分けられないため。 |

---

## 2. DB共通設定

| 項目 | 設定値 | 備考 |
|---|---|---|
| DBMS | MySQL 9.7.3 | |
| データベース名 | sepant | |
| ストレージエンジン | InnoDB | 外部キーとトランザクションを使うため。 |
| 文字コード | utf8mb4 | 日本語を扱うため。 |
| 照合順序 | utf8mb4_0900_ai_ci | MySQLの既定値。 |
| 接続ユーザー | sepant_app | sepantデータベースへのSELECT・INSERT・UPDATE・DELETE権限のみを付与する。 |

---

## 3. 命名規則

| 対象 | 規則 | 例 |
|---|---|---|
| テーブル | マスタは`m_`、トランザクションは`t_`を付ける。ローマ字のスネークケースとする（userのみ英語）。 | `m_himoku`、`t_hendohi_meisai` |
| コード | 末尾を`_cd`とする。 | `himoku_cd` |
| 年月 | 末尾を`_yymm`または`_mm`とする。値は西暦6桁の`YYYYMM`で保存する。 | `taisho_yymm`、`kaishi_mm` |
| 日付 | 末尾を`_dd`とする。値は日付型（年月日）で保存する。 | `shiharai_dd`、`seisan_dd` |

- カラム名の`_yymm`・`_mm`・`_dd`は、保存する値の桁数ではなく「年月」「日付」の種類を表す。

---

## 4. テーブル定義

### 4.1 m_user（利用者）
| No | 項目名 | カラム名 | データ型 | PK | FK | NOT NULL | 既定値 | 備考 |
|---|---|---|---|---|---|---|---|---|
| 1 | 利用者ID | user_id | INT | ○ | | ○ | AUTO_INCREMENT | 1:夫、2:妻 |
| 2 | 利用者名 | user_name | VARCHAR(10) | | | ○ | | |

### 4.2 m_himoku（費目）
| No | 項目名 | カラム名 | データ型 | PK | FK | NOT NULL | 既定値 | 備考 |
|---|---|---|---|---|---|---|---|---|
| 1 | 費目CD | himoku_cd | CHAR(3) | ○ | | ○ | | `001`形式 |
| 2 | 費目名 | himoku_name | VARCHAR(20) | | | ○ | | |
| 3 | 区分 | kubun | CHAR(2) | | | ○ | | 6章参照 |
| 4 | 表示順 | hyoji_jun | INT | | | ○ | | 画面の行の並び順 |

### 4.3 t_hendohi_meisai（変動費明細）
| No | 項目名 | カラム名 | データ型 | PK | FK | NOT NULL | 既定値 | 備考 |
|---|---|---|---|---|---|---|---|---|
| 1 | 明細ID | meisai_id | INT | ○ | | ○ | AUTO_INCREMENT | |
| 2 | 対象年月 | taisho_yymm | CHAR(6) | | | ○ | | `YYYYMM` |
| 3 | 支払者 | user_id | INT | | m_user | ○ | | |
| 4 | 費目CD | himoku_cd | CHAR(3) | | m_himoku | ○ | | 区分が01・02の費目のみ |
| 5 | 支払日 | shiharai_dd | DATE | | | ○ | | 対象年月の日付 |
| 6 | 金額 | kingaku | INT | | | ○ | | 1以上 |
| 7 | 備考 | biko | VARCHAR(50) | | | | NULL | |

| インデックス名 | カラム | 種類 | 用途 |
|---|---|---|---|
| idx_hendohi_cell | taisho_yymm, user_id, himoku_cd | 通常 | ホームの集計と、IMG-02のセル単位の検索・削除 |

### 4.4 t_kotei_settei（固定設定）
| No | 項目名 | カラム名 | データ型 | PK | FK | NOT NULL | 既定値 | 備考 |
|---|---|---|---|---|---|---|---|---|
| 1 | 設定ID | settei_id | INT | ○ | | ○ | AUTO_INCREMENT | |
| 2 | 費目CD | himoku_cd | CHAR(3) | | m_himoku | ○ | | 区分が03〜06の費目のみ |
| 3 | 利用者ID | user_id | INT | | m_user | ○ | | 固定費は支払う人、収入は受け取る人、当番は担当者 |
| 4 | 金額 | kingaku | INT | | | ○ | | 1以上。1か月あたりの金額 |
| 5 | 開始月 | kaishi_mm | CHAR(6) | | | ○ | | `YYYYMM` |
| 6 | 終了月 | shuryo_mm | CHAR(6) | | | ○ | | `YYYYMM`。開始月以降 |
| 7 | 備考 | biko | VARCHAR(50) | | | | NULL | |

| インデックス名 | カラム | 種類 | 用途 |
|---|---|---|---|
| idx_kotei_kikan | kaishi_mm, shuryo_mm | 通常 | 対象月に有効な固定設定の検索 |

- 年月は`YYYYMM`の固定6桁で保存するため、文字列の大小比較がそのまま年月の前後関係になる。対象月に有効な設定は`kaishi_mm <= 対象年月 AND shuryo_mm >= 対象年月`で求める。

### 4.5 t_seisan（精算）
| No | 項目名 | カラム名 | データ型 | PK | FK | NOT NULL | 既定値 | 備考 |
|---|---|---|---|---|---|---|---|---|
| 1 | 対象年月 | taisho_yymm | CHAR(6) | ○ | | ○ | | `YYYYMM`。行があれば精算済み |
| 2 | 精算日 | seisan_dd | DATE | | | ○ | | 精算済にした日 |

---

## 5. 制約

| テーブル | 制約名 | 内容 |
|---|---|---|
| m_himoku | chk_himoku_kubun | 区分は`01`〜`06`のいずれか。 |
| t_hendohi_meisai | chk_hendohi_kingaku | 金額は1以上。 |
| t_kotei_settei | chk_kotei_kingaku | 金額は1以上。 |
| t_kotei_settei | chk_kotei_kikan | 終了月は開始月以降。 |
| 外部キー全般 | — | 参照先の削除・更新は禁止する（RESTRICT）。マスタの行を消すときは、先に参照している行を消す。 |

- 次のチェックはDBでは行わず、アプリで行う（処理設計書参照）。
  - 年月の形式（`YYYYMM`）
  - 支払日が対象年月の日付であること
  - 費目の区分が、登録するテーブルに合っていること
  - 固定設定の期間の重複

---

## 6. コード値

### 6.1 区分（m_himoku.kubun）
| コード | 名称 | 意味 | 費目の例 | 登録するテーブル |
|---|---|---|---|---|
| 01 | 折半（変動費） | 毎月の支払いを2人で折半する支出 | 食費、日用品、光熱費 | t_hendohi_meisai |
| 02 | 立替（変動費） | 相手の分を立て替えた、毎月変わる支出 | 携帯代 | t_hendohi_meisai |
| 03 | 折半（固定費） | 毎月同じ額を2人で折半する支出 | 家賃、NHK、Wi-Fi | t_kotei_settei |
| 04 | 立替（固定費） | 相手の分を立て替えた、毎月同じ額の支出 | 結婚資金 | t_kotei_settei |
| 05 | 収入 | 負担額から差し引く収入 | 住居手当 | t_kotei_settei |
| 06 | 当番 | 担当者の負担額を調整する当番 | 皿洗い、夕食 | t_kotei_settei |

- アプリでは列挙型（Kubun）で同じコード値を定義する（処理設計書参照）。

---

## 7. DDL

```sql
CREATE DATABASE sepant CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
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
```

- 外部キーは、ON DELETE・ON UPDATEを指定しない（MySQLの既定でRESTRICTと同じ動作になる）。

---

## 8. 初期データ

```sql
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
```

- 費目は例であり、実際の運用に合わせてSQLで追加・変更する（基本設計書F-01）。
- 表示順は10刻みにし、後から間に費目を追加できるようにする。

---

以上
