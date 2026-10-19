-- ============================================================
-- 自动建表（启动幂等，表已存在则跳过）
-- 项目：完美世界 · 大荒功法库
-- ============================================================

-- 1) 用户表
CREATE TABLE IF NOT EXISTS star_user (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  username    VARCHAR(32)  NOT NULL                COMMENT '账号',
  password    VARCHAR(128) NOT NULL                COMMENT '密码（BCrypt 加密存储）',
  nickname    VARCHAR(64)  DEFAULT NULL            COMMENT '昵称',
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='完美世界用户表';

-- 2) 功法分类字典表（首页「类别」筛选的数据源）
CREATE TABLE IF NOT EXISTS gongfa_category (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  code        VARCHAR(32)  NOT NULL                COMMENT '分类编码',
  name        VARCHAR(32)  NOT NULL                COMMENT '分类名称',
  sort_no     INT          NOT NULL DEFAULT 0      COMMENT '排序号（越小越靠前）',
  remark      VARCHAR(255) DEFAULT NULL            COMMENT '分类说明',
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='大荒功法分类字典表';

-- 3) 功法典籍表（首页内容主表）
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
