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

-- 4) 大荒人物志表（人物介绍页内容主表）
CREATE TABLE IF NOT EXISTS world_character (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        VARCHAR(64)  NOT NULL                COMMENT '人物姓名',
  title       VARCHAR(64)  DEFAULT NULL            COMMENT '称号（如 荒天帝）',
  glyph       VARCHAR(16)  DEFAULT NULL            COMMENT '卡面图标',
  avatar      VARCHAR(255) DEFAULT NULL            COMMENT '头像图片路径（如 /avatars/shihao.jpg）',
  realm       VARCHAR(64)  DEFAULT NULL            COMMENT '当前境界',
  faction     VARCHAR(64)  DEFAULT NULL            COMMENT '出身/所属势力',
  rank_score  DECIMAL(4,1) NOT NULL DEFAULT 0      COMMENT '实力评分（「按实力」排序用）',
  grade       VARCHAR(16)  NOT NULL DEFAULT '尊者' COMMENT '定位分级（仙帝/准仙帝/仙王/真仙/尊者）',
  quote       VARCHAR(200) DEFAULT NULL            COMMENT '经典语录',
  description VARCHAR(600) DEFAULT NULL            COMMENT '人物简介',
  is_hot      TINYINT      NOT NULL DEFAULT 0      COMMENT '是否热门人物：1 是 0 否',
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_name (name),
  KEY idx_faction (faction),
  KEY idx_grade (grade),
  KEY idx_rank (rank_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='大荒人物志表';

-- 5) 大荒秘境表（秘境首页内容主表）
CREATE TABLE IF NOT EXISTS secret_realm (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        VARCHAR(64)  NOT NULL                COMMENT '秘境名称',
  glyph       VARCHAR(16)  DEFAULT NULL            COMMENT '卡面图标',
  grade       VARCHAR(16)  NOT NULL DEFAULT '上古' COMMENT '秘境等级（仙帝/仙王/真仙/上古）',
  location    VARCHAR(64)  DEFAULT NULL            COMMENT '所在地',
  open_time   VARCHAR(120) DEFAULT NULL            COMMENT '开启时间',
  duration    VARCHAR(32)  DEFAULT NULL            COMMENT '开启时长',
  quota       INT          NOT NULL DEFAULT 0      COMMENT '每期名额（0 表示不限）',
  points      INT          NOT NULL DEFAULT 0      COMMENT '秘境通关奖励积分（后台确认完成后发放）',
  rank_score  DECIMAL(4,1) NOT NULL DEFAULT 0      COMMENT '评级评分（「按评级」排序用）',
  description VARCHAR(600) DEFAULT NULL            COMMENT '秘境介绍',
  is_hot      TINYINT      NOT NULL DEFAULT 0      COMMENT '是否热门秘境：1 是 0 否',
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_name (name),
  KEY idx_grade (grade),
  KEY idx_rank (rank_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='大荒秘境表';

-- 6) 秘境报名记录表（每期每人限报一次；报名后为待完成，后台点「完成」才发积分）
CREATE TABLE IF NOT EXISTS realm_signup (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  realm_id    BIGINT      NOT NULL                COMMENT '秘境 id',
  username    VARCHAR(32) NOT NULL                COMMENT '报名账号',
  points      INT         NOT NULL DEFAULT 0      COMMENT '报名时留档的通关奖励积分',
  status      VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '完成状态：PENDING 待完成 / COMPLETED 已完成（积分已发放）',
  signed_at   DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  completed_at DATETIME   DEFAULT NULL            COMMENT '后台确认完成时间',
  completed_by VARCHAR(32) DEFAULT NULL           COMMENT '确认完成的管理员账号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_realm_user (realm_id, username),
  KEY idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='秘境报名记录表';

-- 7) 悬赏任务表（任务接取页内容主表，等级越高积分越多）
CREATE TABLE IF NOT EXISTS quest (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        VARCHAR(64)  NOT NULL                COMMENT '任务名称',
  glyph       VARCHAR(16)  DEFAULT NULL            COMMENT '卡面图标',
  grade       VARCHAR(16)  NOT NULL DEFAULT '上古' COMMENT '任务等级（仙帝/仙王/真仙/上古）',
  points      INT          NOT NULL DEFAULT 0      COMMENT '接取奖励积分',
  poster      VARCHAR(64)  DEFAULT NULL            COMMENT '发布方',
  description VARCHAR(600) DEFAULT NULL            COMMENT '任务简介',
  is_hot      TINYINT      NOT NULL DEFAULT 0      COMMENT '是否热门任务：1 是 0 否',
  created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_name (name),
  KEY idx_grade (grade),
  KEY idx_points (points)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='大荒悬赏任务表';

-- 8) 任务接取记录表（每项每人限接一次；接取后为待完成，后台点「完成」才发积分）
CREATE TABLE IF NOT EXISTS quest_accept (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  quest_id    BIGINT      NOT NULL                COMMENT '任务 id',
  username    VARCHAR(32) NOT NULL                COMMENT '接取账号',
  points      INT         NOT NULL DEFAULT 0      COMMENT '接取时留档的奖励积分（完成时按任务最新积分发放）',
  status      VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '完成状态：PENDING 待完成 / COMPLETED 已完成（积分已发放）',
  accepted_at DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '接取时间',
  completed_at DATETIME   DEFAULT NULL            COMMENT '后台确认完成时间',
  completed_by VARCHAR(32) DEFAULT NULL           COMMENT '确认完成的管理员账号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_quest_user (quest_id, username),
  KEY idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务接取记录表';

-- ============================================================
-- 9) 老库增量迁移：「后台点完成才加积分」（2026-11-09）
--    新库由上面的 CREATE TABLE 直接建出；本节只给旧表补列，幂等可重复执行。
--    MySQL 8 无 ADD COLUMN IF NOT EXISTS，故用 information_schema + 预处理语句判断。
--    ⚠ 必须在 data.sql 之前执行（data.sql 的秘境种子数据已带 points 列）。
-- ============================================================

-- 9.1) 秘境主表：通关奖励积分（旧库补列后按等级补默认值）
SET @cnt := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'secret_realm' AND COLUMN_NAME = 'points');
SET @ddl := IF(@cnt = 0,
  'ALTER TABLE secret_realm ADD COLUMN points INT NOT NULL DEFAULT 0 COMMENT ''秘境通关奖励积分（后台确认完成后发放）''',
  'DO 0');
PREPARE pw_stmt FROM @ddl;
EXECUTE pw_stmt;
DEALLOCATE PREPARE pw_stmt;
SET @ddl := IF(@cnt = 0,
  'UPDATE secret_realm SET points = CASE grade WHEN ''仙帝'' THEN 1000 WHEN ''仙王'' THEN 600 WHEN ''真仙'' THEN 300 ELSE 100 END WHERE points = 0',
  'DO 0');
PREPARE pw_stmt FROM @ddl;
EXECUTE pw_stmt;
DEALLOCATE PREPARE pw_stmt;

-- 9.2) 任务接取记录：完成状态 / 完成时间 / 确认人
SET @need_status := (SELECT COUNT(*) FROM information_schema.COLUMNS
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'quest_accept' AND COLUMN_NAME = 'status');
SET @ddl := IF(@need_status = 0,
  'ALTER TABLE quest_accept ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT ''PENDING'' COMMENT ''完成状态：PENDING 待完成 / COMPLETED 已完成（积分已发放）''',
  'DO 0');
PREPARE pw_stmt FROM @ddl;
EXECUTE pw_stmt;
DEALLOCATE PREPARE pw_stmt;

SET @cnt := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'quest_accept' AND COLUMN_NAME = 'completed_at');
SET @ddl := IF(@cnt = 0,
  'ALTER TABLE quest_accept ADD COLUMN completed_at DATETIME DEFAULT NULL COMMENT ''后台确认完成时间''',
  'DO 0');
PREPARE pw_stmt FROM @ddl;
EXECUTE pw_stmt;
DEALLOCATE PREPARE pw_stmt;

SET @cnt := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'quest_accept' AND COLUMN_NAME = 'completed_by');
SET @ddl := IF(@cnt = 0,
  'ALTER TABLE quest_accept ADD COLUMN completed_by VARCHAR(32) DEFAULT NULL COMMENT ''确认完成的管理员账号''',
  'DO 0');
PREPARE pw_stmt FROM @ddl;
EXECUTE pw_stmt;
DEALLOCATE PREPARE pw_stmt;

-- 旧规则是「接取即发积分」，补列时把存量记录直接置为已完成，避免修士积分凭空消失
SET @ddl := IF(@need_status = 0,
  'UPDATE quest_accept SET status = ''COMPLETED'', completed_at = COALESCE(completed_at, accepted_at) WHERE status IS NULL OR status <> ''COMPLETED''',
  'DO 0');
PREPARE pw_stmt FROM @ddl;
EXECUTE pw_stmt;
DEALLOCATE PREPARE pw_stmt;

-- 9.3) 秘境报名记录：奖励积分 / 完成状态 / 完成时间 / 确认人
SET @cnt := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'realm_signup' AND COLUMN_NAME = 'points');
SET @ddl := IF(@cnt = 0,
  'ALTER TABLE realm_signup ADD COLUMN points INT NOT NULL DEFAULT 0 COMMENT ''报名时留档的通关奖励积分''',
  'DO 0');
PREPARE pw_stmt FROM @ddl;
EXECUTE pw_stmt;
DEALLOCATE PREPARE pw_stmt;

SET @need_realm_status := (SELECT COUNT(*) FROM information_schema.COLUMNS
                           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'realm_signup' AND COLUMN_NAME = 'status');
SET @ddl := IF(@need_realm_status = 0,
  'ALTER TABLE realm_signup ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT ''PENDING'' COMMENT ''完成状态：PENDING 待完成 / COMPLETED 已完成（积分已发放）''',
  'DO 0');
PREPARE pw_stmt FROM @ddl;
EXECUTE pw_stmt;
DEALLOCATE PREPARE pw_stmt;

SET @cnt := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'realm_signup' AND COLUMN_NAME = 'completed_at');
SET @ddl := IF(@cnt = 0,
  'ALTER TABLE realm_signup ADD COLUMN completed_at DATETIME DEFAULT NULL COMMENT ''后台确认完成时间''',
  'DO 0');
PREPARE pw_stmt FROM @ddl;
EXECUTE pw_stmt;
DEALLOCATE PREPARE pw_stmt;

SET @cnt := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'realm_signup' AND COLUMN_NAME = 'completed_by');
SET @ddl := IF(@cnt = 0,
  'ALTER TABLE realm_signup ADD COLUMN completed_by VARCHAR(32) DEFAULT NULL COMMENT ''确认完成的管理员账号''',
  'DO 0');
PREPARE pw_stmt FROM @ddl;
EXECUTE pw_stmt;
DEALLOCATE PREPARE pw_stmt;

-- 旧规则下报名从不发积分，存量报名记录标记为已完成（积分保持 0），总积分不变
SET @ddl := IF(@need_realm_status = 0,
  'UPDATE realm_signup SET status = ''COMPLETED'', completed_at = COALESCE(completed_at, signed_at) WHERE status IS NULL OR status = ''PENDING''',
  'DO 0');
PREPARE pw_stmt FROM @ddl;
EXECUTE pw_stmt;
DEALLOCATE PREPARE pw_stmt;
