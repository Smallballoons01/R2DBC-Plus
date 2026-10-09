-- R2DBC-Plus 测试用 H2 兼容表结构
-- 语法尽量贴近 MySQL（H2 的 MySQL 兼容模式下反引号可用），
-- 目的是让 CI 与本地都能零外部依赖跑通测试。

DROP TABLE IF EXISTS test_user;
DROP TABLE IF EXISTS test_account;
DROP TABLE IF EXISTS test_account_group;
DROP TABLE IF EXISTS clean_example;

-- 对应实体 TestUser：test_user
CREATE TABLE test_user (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(64),
    email        VARCHAR(128),
    age          INT,
    enabled      BOOLEAN,
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    version      INT       DEFAULT 0,
    deleted      BOOLEAN  DEFAULT FALSE
);

-- 对应实体 TestAccount：test_account
CREATE TABLE test_account (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id    BIGINT,
    uuid        VARCHAR(64),
    account     VARCHAR(100),
    password    VARCHAR(255),
    nickname    VARCHAR(100),
    avatar      VARCHAR(500),
    remark      VARCHAR(500),
    status      INT,
    is_open     INT      DEFAULT 0,
    country     VARCHAR(50),
    is_del      INT      DEFAULT 0,
    gmt_create  TIMESTAMP,
    gmt_modified TIMESTAMP,
    create_by   VARCHAR(64),
    updated_by  VARCHAR(64),
    sort_num    BIGINT,
    version     INT      DEFAULT 0
);

-- Mapper XML 中引用的分组表
CREATE TABLE test_account_group (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(64),
    remark   VARCHAR(255)
);

-- 对应实体 CleanExampleEntity：clean_example
CREATE TABLE clean_example (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(64),
    created_time TIMESTAMP,
    updated_time TIMESTAMP
);

INSERT INTO test_user (username, email, age, enabled, version, deleted) VALUES
    ('alice', 'alice@example.com', 30, TRUE, 0,  FALSE),
    ('bob',   'bob@example.com',   25, TRUE, 0,  FALSE),
    ('carol', 'carol@example.com', 35, FALSE, 0, FALSE);

INSERT INTO test_account
    (group_id, uuid, account, password, nickname, avatar, remark, status, is_open, country, is_del, sort_num, version)
VALUES
    (1, 'test-uuid-001', 'test1@example.com', 'password123', '测试账号1', 'avatar1.jpg', '备注1', 1, 0, 'CN', 0, 1, 0),
    (1, 'test-uuid-002', 'test2@example.com', 'password123', '测试账号2', 'avatar2.jpg', '备注2', 1, 0, 'CN', 0, 2, 0),
    (2, 'test-uuid-003', 'test3@example.com', 'password123', '测试账号3', 'avatar3.jpg', '备注3', 1, 1, 'US', 0, 3, 0);

INSERT INTO test_account_group (id, name, remark) VALUES
    (1, '默认分组', '默认分组'),
    (2, '测试分组', '测试分组');