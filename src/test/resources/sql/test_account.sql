-- 测试账号表结构
-- 对应实体类：com.scrm.r2dbc.plus.test.entity.TestAccount

DROP TABLE IF EXISTS `test_account`;

CREATE TABLE `test_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id，自动增长',
  `group_id` bigint(20) DEFAULT NULL COMMENT '分组id',
  `uuid` varchar(64) DEFAULT NULL COMMENT '用户uuid',
  `account` varchar(100) DEFAULT NULL COMMENT '账号',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `nickname` varchar(100) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` int(11) DEFAULT NULL COMMENT '状态',
  `is_open` int(11) DEFAULT '0' COMMENT '是否启用 0 启用 1 禁用',
  `country` varchar(50) DEFAULT NULL COMMENT '国家',
  `is_del` int(11) DEFAULT '0' COMMENT '是否删除 0 未删除 1 已删除',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `sort_num` bigint(20) DEFAULT NULL COMMENT '排序字段',
  PRIMARY KEY (`id`),
  KEY `idx_uuid` (`uuid`),
  KEY `idx_account` (`account`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_open` (`is_open`),
  KEY `idx_is_del` (`is_del`),
  KEY `idx_gmt_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试账号表';

-- 插入测试数据
INSERT INTO `test_account` (
  `group_id`, `uuid`, `account`, `password`, `nickname`, `avatar`, 
  `remark`, `status`, `is_open`, `country`, `is_del`, `sort_num`
) VALUES 
(1, 'test-uuid-001', 'test1@example.com', 'password123', '测试账号1', 'avatar1.jpg', '这是测试账号1', 1, 0, 'CN', 0, 1),
(1, 'test-uuid-002', 'test2@example.com', 'password123', '测试账号2', 'avatar2.jpg', '这是测试账号2', 1, 0, 'CN', 0, 2),
(2, 'test-uuid-003', 'test3@example.com', 'password123', '测试账号3', 'avatar3.jpg', '这是测试账号3', 2, 0, 'US', 0, 3),
(2, 'test-uuid-004', 'test4@example.com', 'password123', '测试账号4', 'avatar4.jpg', '这是测试账号4', 1, 1, 'CN', 0, 4),
(3, 'test-uuid-005', 'test5@example.com', 'password123', '测试账号5', 'avatar5.jpg', '这是测试账号5', 1, 0, 'JP', 0, 5),
(1, 'test-uuid-006', 'test6@example.com', 'password123', '测试账号6', 'avatar6.jpg', '这是测试账号6', 3, 0, 'CN', 1, 6),
(2, 'test-uuid-007', 'test7@example.com', 'password123', '测试账号7', 'avatar7.jpg', '这是测试账号7', 1, 0, 'KR', 0, 7),
(3, 'test-uuid-008', 'test8@example.com', 'password123', '测试账号8', 'avatar8.jpg', '这是测试账号8', 2, 0, 'CN', 0, 8),
(1, 'test-uuid-009', 'test9@example.com', 'password123', '测试账号9', 'avatar9.jpg', '这是测试账号9', 1, 0, 'US', 0, 9),
(2, 'test-uuid-010', 'test10@example.com', 'password123', '测试账号10', 'avatar10.jpg', '这是测试账号10', 1, 0, 'CN', 0, 10);

-- 创建其他可能需要的测试表

-- 账号分组表
DROP TABLE IF EXISTS `test_account_group`;

CREATE TABLE `test_account_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(100) NOT NULL COMMENT '分组名称',
  `description` varchar(500) DEFAULT NULL COMMENT '分组描述',
  `uuid` varchar(64) DEFAULT NULL COMMENT '用户uuid',
  `is_del` int(11) DEFAULT '0' COMMENT '是否删除 0 未删除 1 已删除',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `sort_num` bigint(20) DEFAULT NULL COMMENT '排序字段',
  PRIMARY KEY (`id`),
  KEY `idx_uuid` (`uuid`),
  KEY `idx_is_del` (`is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账号分组表';

-- 插入分组测试数据
INSERT INTO `test_account_group` (`id`, `name`, `description`, `uuid`, `sort_num`) VALUES 
(1, '客服组1', '第一个客服组', 'test-uuid', 1),
(2, '客服组2', '第二个客服组', 'test-uuid', 2),
(3, '客服组3', '第三个客服组', 'test-uuid', 3);
