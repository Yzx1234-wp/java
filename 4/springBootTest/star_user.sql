-- ============================================================
-- 完美世界 · 建库建表 SQL
-- 数据库：swallowed_star  （本机 MySQL：root / 123456）
-- 说明：本文件由 start.bat 在启动前执行一次；幂等，可重复执行。
--       功法典籍数据由后端启动时的 data.sql 自动灌入（INSERT IGNORE 幂等）。
-- ============================================================

-- 1) 若数据库尚未创建，先执行下面两行（已加 IF NOT EXISTS，可重复执行）：
CREATE DATABASE IF NOT EXISTS swallowed_star
  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 2) 切换到目标库
USE swallowed_star;

-- 3) 创建用户表（幂等）
CREATE TABLE IF NOT EXISTS star_user (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  username    VARCHAR(32)  NOT NULL                COMMENT '账号',
  password    VARCHAR(128) NOT NULL                COMMENT '密码（明文存储，演示项目不做加密）',
  nickname    VARCHAR(64)  DEFAULT NULL            COMMENT '昵称',
  permission  TINYINT      NOT NULL DEFAULT 1      COMMENT '权限：0 管理员 / 1 默认（普通修士）',
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='完美世界用户表';

-- 3.1) 老库增量补列：permission 列不存在时自动补上（幂等）
SET @add_permission = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE star_user ADD COLUMN permission TINYINT NOT NULL DEFAULT 1 COMMENT ''权限：0 管理员 / 1 默认（普通修士）'' AFTER nickname',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'swallowed_star' AND TABLE_NAME = 'star_user' AND COLUMN_NAME = 'permission'
);
PREPARE stmt FROM @add_permission;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) 功法分类字典表（首页「类别」筛选的数据源）
CREATE TABLE IF NOT EXISTS gongfa_category (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  code        VARCHAR(32)  NOT NULL                COMMENT '分类编码',
  name        VARCHAR(32)  NOT NULL                COMMENT '分类名称',
  sort_no     INT          NOT NULL DEFAULT 0      COMMENT '排序号（越小越靠前）',
  remark      VARCHAR(255) DEFAULT NULL            COMMENT '分类说明',
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='大荒功法分类字典表';

-- 5) 功法典籍表（首页内容主表）
CREATE TABLE IF NOT EXISTS gongfa (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        VARCHAR(64)  NOT NULL                COMMENT '功法名称',
  cat         VARCHAR(32)  NOT NULL                COMMENT '一级分类（功法/宝术/至尊术/剑诀/秘法/身法）',
  tier        VARCHAR(160) DEFAULT NULL            COMMENT '品阶/流派简述',
  glyph       VARCHAR(16)  DEFAULT NULL            COMMENT '卡面图标',
  origin      VARCHAR(64)  DEFAULT NULL            COMMENT '传承来源（对应前端 from）',
  owner       VARCHAR(64)  DEFAULT NULL            COMMENT '修习者',
  rank_score  DECIMAL(4,1) NOT NULL DEFAULT 0      COMMENT '品阶评分（「按品阶」排序用）',
  grade       VARCHAR(16)  NOT NULL DEFAULT '中乘' COMMENT '品阶分级（绝世/绝顶/一流/上乘/中乘）',
  description VARCHAR(600) DEFAULT NULL            COMMENT '功法简介',
  is_hot      TINYINT      NOT NULL DEFAULT 0      COMMENT '是否热门典籍：1 是 0 否',
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_name (name),
  KEY idx_cat (cat),
  KEY idx_grade (grade),
  KEY idx_rank (rank_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='大荒功法典籍表';

-- 6) 演示账号说明：
--    演示账号 admin / 123456 由后端在「首次启动」时自动创建（密码明文存储，管理员权限 permission=0），
--    无需手动插入。手动插入示例：
--    INSERT INTO star_user (username, password, nickname, permission)
--    VALUES ('admin', '123456', '完美世界', 0);
