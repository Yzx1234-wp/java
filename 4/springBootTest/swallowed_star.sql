/*
 Navicat Premium Dump SQL

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : swallowed_star

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 18/11/2026 09:56:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cultivation_log
-- ----------------------------
DROP TABLE IF EXISTS `cultivation_log`;
CREATE TABLE `cultivation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账号',
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型：COMPREHEND 参悟 / MEDITATE 吐纳 / BREAKTHROUGH 突破 / FAIL 失败',
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '细节说明',
  `exp_delta` int NOT NULL DEFAULT 0 COMMENT '修为变化（可为负）',
  `realm_index` int NOT NULL DEFAULT 0 COMMENT '发生时的境界序号',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cult_log_user`(`username` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '修炼记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cultivation_log
-- ----------------------------
INSERT INTO `cultivation_log` VALUES (1, 'admin', 'COMPREHEND', '参悟《原始真解》', '绝世典籍 · 得修为 +200', 200, 0, '2026-11-18 08:33:21');
INSERT INTO `cultivation_log` VALUES (2, 'admin', 'COMPREHEND', '参悟《以身为种》', '绝世典籍 · 得修为 +200', 200, 0, '2026-11-18 08:33:24');
INSERT INTO `cultivation_log` VALUES (3, 'admin', 'COMPREHEND', '参悟《六道轮回天功》', '绝世典籍 · 得修为 +200', 200, 0, '2026-11-18 08:33:25');
INSERT INTO `cultivation_log` VALUES (4, 'admin', 'COMPREHEND', '参悟《上苍之手》', '绝世典籍 · 得修为 +200', 200, 0, '2026-11-18 08:33:26');
INSERT INTO `cultivation_log` VALUES (5, 'admin', 'BREAKTHROUGH', '突破「洞天境」', '搬血境 → 洞天境 · 渡劫成功（成功率 68%），结余修为 720', 0, 1, '2026-11-18 08:33:27');
INSERT INTO `cultivation_log` VALUES (6, 'admin', 'BREAKTHROUGH', '突破「化灵境」', '洞天境 → 化灵境 · 渡劫成功（成功率 68%），结余修为 600', 0, 2, '2026-11-18 08:33:28');
INSERT INTO `cultivation_log` VALUES (7, 'admin', 'BREAKTHROUGH', '突破「铭纹境」', '化灵境 → 铭纹境 · 渡劫成功（成功率 68%），结余修为 430', 0, 3, '2026-11-18 08:33:28');
INSERT INTO `cultivation_log` VALUES (8, 'admin', 'FAIL', '冲击「列阵境」失败', '渡劫未成，道基受损（成功率 68%）· 修为 -107', -107, 3, '2026-11-18 08:33:29');
INSERT INTO `cultivation_log` VALUES (9, 'admin', 'FAIL', '冲击「列阵境」失败', '渡劫未成，道基受损（成功率 68%）· 修为 -80', -80, 3, '2026-11-18 08:33:29');
INSERT INTO `cultivation_log` VALUES (10, 'admin', 'BREAKTHROUGH', '突破「列阵境」', '铭纹境 → 列阵境 · 渡劫成功（成功率 68%），结余修为 13', 0, 4, '2026-11-18 08:33:30');
INSERT INTO `cultivation_log` VALUES (11, 'admin', 'COMPREHEND', '参悟《轮回》', '绝世典籍 · 得修为 +200', 200, 4, '2026-11-18 08:33:35');
INSERT INTO `cultivation_log` VALUES (12, 'admin', 'COMPREHEND', '参悟《真龙宝术》', '绝顶典籍 · 得修为 +150', 150, 4, '2026-11-18 08:33:36');
INSERT INTO `cultivation_log` VALUES (13, 'admin', 'COMPREHEND', '参悟《真凰宝术》', '绝顶典籍 · 得修为 +150', 150, 4, '2026-11-18 08:33:37');
INSERT INTO `cultivation_log` VALUES (14, 'admin', 'COMPREHEND', '参悟《轮回宝术》', '一流典籍 · 得修为 +110', 110, 4, '2026-11-18 08:33:38');
INSERT INTO `cultivation_log` VALUES (15, 'admin', 'BREAKTHROUGH', '突破「尊者境」', '列阵境 → 尊者境 · 渡劫成功（成功率 76%），结余修为 323', 0, 5, '2026-11-18 08:33:40');
INSERT INTO `cultivation_log` VALUES (16, 'admin', 'MEDITATE', '打坐吐纳', '调息周天 · 得修为 +54', 54, 5, '2026-11-18 08:35:02');
INSERT INTO `cultivation_log` VALUES (17, 'admin', 'MEDITATE', '打坐吐纳', '调息周天 · 得修为 +54', 54, 5, '2026-11-18 08:57:13');
INSERT INTO `cultivation_log` VALUES (18, 'admin', 'BREAKTHROUGH', '突破「神火境」', '尊者境 → 神火境 · 渡劫成功（成功率 76%），结余修为 51', 0, 6, '2026-11-18 08:57:15');
INSERT INTO `cultivation_log` VALUES (19, 'admin', 'MEDITATE', '打坐吐纳', '调息周天 · 得修为 +60', 60, 6, '2026-11-18 09:51:56');

-- ----------------------------
-- Table structure for gongfa
-- ----------------------------
DROP TABLE IF EXISTS `gongfa`;
CREATE TABLE `gongfa`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '功法名称',
  `cat` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '一级分类（功法/宝术/至尊术/剑诀/秘法/身法）',
  `tier` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品阶/流派简述',
  `glyph` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '卡面图标',
  `origin` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '传承来源（对应前端 from）',
  `owner` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修习者',
  `rank_score` decimal(4, 1) NOT NULL DEFAULT 0.0 COMMENT '品阶评分（「按品阶」排序用）',
  `grade` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '中乘' COMMENT '品阶分级（绝世/绝顶/一流/上乘/中乘）',
  `description` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '功法简介',
  `is_hot` tinyint NOT NULL DEFAULT 0 COMMENT '是否热门典籍：1 是 0 否',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE,
  INDEX `idx_cat`(`cat` ASC) USING BTREE,
  INDEX `idx_grade`(`grade` ASC) USING BTREE,
  INDEX `idx_rank`(`rank_score` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 175 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '大荒功法典籍表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gongfa
-- ----------------------------
INSERT INTO `gongfa` VALUES (1, '原始真解', '功法', '万法之基 · 神引 / 超脱 / 终极三篇', '📜', '尸骸仙帝所留', '石昊', 10.0, '绝世', '不讲宝术、只讲骨文之道的无上真解，号称“万法匆匆过，唯有原始最真”。柳神自上界逃亡下界时随身携带神引篇，后赠予石昊，成为其修行的根本。', 1, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (2, '以身为种', '功法', '石昊独创 · 末法道途', '🌱', '石昊自创', '石昊', 10.0, '绝世', '不循天地大道，反以己身为天地，开辟轮海、道宫、四极、化龙、仙台五大秘境；正是这条路，让他在末法时代仍能独自超脱。', 1, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (3, '六道轮回天功', '功法', '至尊殿堂镇教传承', '☯', '至尊殿堂', '石昊', 9.5, '绝世', '驾驭宝术的无上古天功，可同时催动六种绝世神通，一击“照亮永恒”，是石昊后期最凌厉的爆发手段。', 1, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (4, '上苍之手', '至尊术', '天生至尊术 · 劫罚之极', '🌌', '石昊至尊骨', '石昊', 9.5, '绝世', '至尊骨上孕育的天生宝术，残缺时名“上苍劫光”，号称世间最可怕的劫罚；后借飞仙石之力重新领悟完整形态。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (5, '轮回', '至尊术', '天生至尊术 · 时光秘力', '☸', '第二块至尊骨', '石昊', 9.5, '绝世', '涅槃后新生至尊骨所载之术，掌控时光秘力，可留肉身记忆、附时光碎片；石昊下界身死时，自斩此骨赠予弟弟秦昊。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (6, '八九天功', '功法', '混世魔猿族古经', '🐒', '混世魔猿族 · 禁区之主', '石昊', 9.0, '绝顶', '含七十二变与三头六臂之术，威力不弱于十凶宝术；此功唯有在厮杀中才能变强，为石昊纵横九天十地打下根基。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (7, '鲲鹏宝术', '宝术', '太古十凶之鲲鹏传承', '🐟', '十凶 · 鲲鹏', '石昊', 9.0, '绝顶', '于化魔洞习得完整传承，掌阴阳衍变——大海无量、太阳拳、十万神羽化剑炉，皆为鲲鹏一脉的极致神通。', 1, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (8, '雷帝宝术', '宝术', '雷道之巅 · 上苍劫光', '⚡', '雷帝传承 · 融狻猊法', '石昊', 9.0, '绝顶', '汇雷道四绝天，掌雷霆本源之力，可引天劫炼体，衍化上苍劫光与雷帝指，是石昊雷道神通的巅峰。', 1, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (9, '真龙宝术', '宝术', '太古十凶之真龙传承', '🐲', '十凶 · 真龙', '石昊', 9.0, '绝顶', '十凶之一的真龙传承，含真龙爪、龙吟术等绝学——爪可裂空、吟可镇魂，是肉身搏杀一道的极致宝术。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (10, '真凰宝术', '宝术', '太古十凶之真凰传承', '🦅', '十凶 · 真凰', '石昊', 9.0, '绝顶', '凤翔九天、浴火不灭；以六道轮回天功催动时，可与鲲鹏、雷帝之宝术同时爆发，威势足以照亮永恒。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (11, '第三至尊术', '至尊术', '无名骨文 · 增幅之秘', '✴', '两失至尊骨后新生', '石昊', 9.0, '绝顶', '两块至尊骨被夺后孕育出的无名骨文，可增幅法力、速度与神通，精气神暴涨、战力层层叠加，至仙王绝巅方被彻底参透。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (12, '上苍劫光', '至尊术', '残缺至尊术 · 天降神光', '🌠', '至尊骨（残缺形态）', '石昊', 9.0, '绝顶', '上苍之手的残缺形态，号称世间最可怕的劫罚；天降神光可破灭万物，斩诸天强者于无形。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (13, '柳神法', '秘法', '柳神古法 · 涅槃新术', '🌳', '石村祭灵 · 柳神', '石昊', 9.0, '绝顶', '古法与柳神涅槃后所创新术相合，柔中藏刚、生机无尽，柳枝垂落之处万物回春，亦可绞杀神魔。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (14, '万灵图（战神图录）', '秘法', '原始真解 · 上卷所载', '🗺', '原始真解上卷', '石昊', 9.0, '绝顶', '以万灵之形铭刻符文，万灵图入体可演化列阵之道；石昊在铭纹、列阵两境皆以此稳固道基，化神曦为道符。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (15, '轮回宝术', '宝术', '肉身记忆 · 附时光碎片', '⏳', '石昊自悟', '石昊', 8.5, '一流', '以轮回之术封存肉身记忆，出手时附带时光碎片，令对手如坠旧梦、招架不及，与至尊骨上的“轮回”互为表里。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (16, '草字剑诀', '剑诀', '十凶九叶剑草 · 三大剑诀之一', '🌿', '十凶 · 九叶剑草', '石昊', 8.5, '一流', '讲究“一株草”之意，剑气冲霄；成剑可化九片剑叶斩裂虚空，剑光过处，星辰崩碎。', 1, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (17, '法力免疫', '秘法', '以身为种的衍化', '🛡', '石昊 · 以身为种一路', '石昊', 8.5, '一流', '修至深处可免疫诸般法术攻伐，唯有肉身搏杀方能伤及根本，是他越级挑战诸天强者的底牌之一。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (18, '太古朱雀四击', '宝术', '太古朱雀 · 火道绝学', '🔥', '太古朱雀传承', '石昊', 8.0, '一流', '一式强过一式，四击叠出可焚尽山河；石昊以此在火道上与朱雀一脉的强者争锋，也为火道诸法的根基。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (19, '玄龟宝术', '宝术', '太古玄龟 · 守御传承', '🐢', '太古凶兽 · 玄龟', '石昊', 8.0, '一流', '主守御与血气的太古宝术，龟甲符文可卸力万钧；配合肉身横炼，纵被围攻亦难破其防。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (20, '朱厌宝术', '宝术', '残缺传承 · 与八九天功同源', '🦧', '朱厌一族', '石昊', 7.5, '上乘', '所习虽为残缺传承，却已含三头六臂与七十二变两个方向，与八九天功同源；助石昊在近身厮杀中变化无穷。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (21, '狻猊宝术', '宝术', '雷道化生 · 雷道之始', '🦁', '补天阁', '石昊', 7.0, '上乘', '于补天阁悟得“雷道化生”之妙，是石昊雷道之路的起点，也为日后融合雷道四绝天、成就雷帝宝术铺路。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (22, '雷天雀宝术', '宝术', '雷道飞禽 · 驭雷而行', '🌩', '雷道传承', '石昊', 7.0, '上乘', '化天雀之形驭雷而行，速度与雷罚并重；与狻猊宝术、雷帝术相合，令其雷道诸法愈发圆融。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (23, '金璇波纹功', '功法', '鹏族护身法 · 三重', '🪶', '补天阁藏经阁', '石昊', 7.0, '上乘', '脱胎于金翅大鹏鸟的护体神功，共分三重、分刻于三块大鹏骨之上，能将敌人的攻势化归己用。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (24, '五行涅槃法', '功法', '红凰一族不传之秘', '🎋', '红凰一族', '石昊', 7.0, '上乘', '以五行之力涅槃重塑己身，曾助石昊驱除绿铜锈诅咒；后与朱雀涅槃法并修，肉身涅槃再上一层。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (25, '伪八九天功', '功法', '齐道临自创 · 偷学百家', '📘', '至尊道场 · 齐道临', '石昊', 6.5, '中乘', '齐道临偷学百家之长所创的伪篇，虽不及魔猿族正版，却是石昊初入至尊道场时的重要依仗，也让他摸到了魔猿一族战斗法门的门径。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (26, '朱雀涅槃法', '功法', '涅槃诸法 · 火道重生', '🐦', '火国 · 朱雀传承', '石昊', 6.5, '中乘', '与五行涅槃法、真凰涅槃法并称的涅槃之术，当日曾同被用于为石昊祛除绿铜锈诅咒，借火浴身、重塑肉身。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (27, '缩地成寸', '身法', '遁术 · 一步千里', '🌀', '下界古法', '石昊', 6.0, '中乘', '土遁之术，一步跨过千里山河、遁地不见踪影，是顶级的保命身法，也为近身搏杀创造契机。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (28, '浴火术', '秘法', '借火涅槃 · 火道秘术', '🕯', '火道秘术', '石昊', 6.0, '中乘', '以火浴身，重伤时借火元重塑肉身；与寻火法、祭火道同修，可令火道根基愈发深厚。', 0, '2026-11-18 08:29:41');
INSERT INTO `gongfa` VALUES (29, '金蛇腿', '身法', '近身腿法 · 下界流传', '🐍', '下界古法', '石昊', 5.5, '中乘', '蛇形腿法，起落无声、变向诡谲，专攻下三路，常于缠斗中出其不意地断敌腿骨、破敌重心。', 0, '2026-11-18 08:29:41');

-- ----------------------------
-- Table structure for gongfa_category
-- ----------------------------
DROP TABLE IF EXISTS `gongfa_category`;
CREATE TABLE `gongfa_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鍒嗙被缂栫爜',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鍒嗙被鍚嶇О',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '鎺掑簭鍙凤紙瓒婂皬瓒婇潬鍓嶏級',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '鍒嗙被璇存槑',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '澶ц崚鍔熸硶鍒嗙被瀛楀吀琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gongfa_category
-- ----------------------------
INSERT INTO `gongfa_category` VALUES (1, '功法', '功法', 1, '修行根本大功，可开辟秘境、稳固道基');
INSERT INTO `gongfa_category` VALUES (2, '宝术', '宝术', 2, '十凶传承与雷道、火道等极致神通');
INSERT INTO `gongfa_category` VALUES (3, '至尊术', '至尊术', 3, '至尊骨上孕育的天生之术');
INSERT INTO `gongfa_category` VALUES (4, '剑诀', '剑诀', 4, '剑道绝学，剑气冲霄、可斩星辰');
INSERT INTO `gongfa_category` VALUES (5, '秘法', '秘法', 5, '秘术与图录，藏于真解与祭灵传承之中');
INSERT INTO `gongfa_category` VALUES (6, '身法', '身法', 6, '遁术与近身身法，保命与近战之本');

-- ----------------------------
-- Table structure for gongfa_comprehend
-- ----------------------------
DROP TABLE IF EXISTS `gongfa_comprehend`;
CREATE TABLE `gongfa_comprehend`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账号',
  `gongfa_id` bigint NOT NULL COMMENT '功法 id',
  `exp_gain` int NOT NULL DEFAULT 0 COMMENT '本次参悟所得修为',
  `comprehended_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参悟时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_cult_gongfa`(`username` ASC, `gongfa_id` ASC) USING BTREE,
  INDEX `idx_cult_user`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '功法参悟记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gongfa_comprehend
-- ----------------------------
INSERT INTO `gongfa_comprehend` VALUES (1, 'admin', 1, 200, '2026-11-18 08:33:21');
INSERT INTO `gongfa_comprehend` VALUES (2, 'admin', 2, 200, '2026-11-18 08:33:24');
INSERT INTO `gongfa_comprehend` VALUES (3, 'admin', 3, 200, '2026-11-18 08:33:25');
INSERT INTO `gongfa_comprehend` VALUES (4, 'admin', 4, 200, '2026-11-18 08:33:26');
INSERT INTO `gongfa_comprehend` VALUES (5, 'admin', 5, 200, '2026-11-18 08:33:35');
INSERT INTO `gongfa_comprehend` VALUES (6, 'admin', 9, 150, '2026-11-18 08:33:36');
INSERT INTO `gongfa_comprehend` VALUES (7, 'admin', 10, 150, '2026-11-18 08:33:37');
INSERT INTO `gongfa_comprehend` VALUES (8, 'admin', 15, 110, '2026-11-18 08:33:38');

-- ----------------------------
-- Table structure for quest
-- ----------------------------
DROP TABLE IF EXISTS `quest`;
CREATE TABLE `quest`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `glyph` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '卡面图标',
  `grade` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '上古' COMMENT '任务等级（仙帝/仙王/真仙/上古）',
  `points` int NOT NULL DEFAULT 0 COMMENT '接取奖励积分',
  `poster` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发布方',
  `description` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务简介',
  `is_hot` tinyint NOT NULL DEFAULT 0 COMMENT '是否热门任务：1 是 0 否',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE,
  INDEX `idx_grade`(`grade` ASC) USING BTREE,
  INDEX `idx_points`(`points` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '大荒悬赏任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quest
-- ----------------------------
INSERT INTO `quest` VALUES (1, '猎杀堕落仙帝残魂', '👑', '仙帝', 1000, '上界 · 天庭悬赏', '仙古一战中堕落的仙帝残魂盘踞于天幕裂隙深处，屡屡撕开界膜吞噬生灵。悬赏千分，只求有胆魄者入裂隙将其斩灭——历史上接此悬赏者，无一生还。', 1, '2026-11-18 08:29:41');
INSERT INTO `quest` VALUES (2, '镇压虚神界神念暴动', '🌌', '仙帝', 1000, '九天共议', '虚神界内仙帝道则所化的神念分身近来躁动不安，杀伐之气外泄。需强者入界镇压神位争夺，护住虚神界运转；成者可留界中参悟仙帝道场三日。', 1, '2026-11-18 08:29:41');
INSERT INTO `quest` VALUES (3, '护送柳神祭礼', '🌿', '仙王', 600, '石村全村', '石村将于春祭为祖地柳神送上新采的万年份灵乳。沿途凶兽环伺、邪修觊觎，需接引者一路护送至祖地祭坛——柳神有灵，护送者皆得福泽。', 1, '2026-11-18 08:29:41');
INSERT INTO `quest` VALUES (4, '探寻神灵之墓深处', '⚰', '仙王', 600, '落神渊 · 守墓人', '神灵之墓外围已被探尽，唯深处大雾之内无人敢入。守墓人悬赏探路者带回墓中神灵低语的拓印，拓印越深，报酬越丰；入内者自担生死。', 1, '2026-11-18 08:29:41');
INSERT INTO `quest` VALUES (5, '斩杀矿洞黑雾凶物', '⛏', '仙王', 600, '石国 · 兵部', '石国古矿洞黑雾再度翻涌，雾中凶物夜夜出没，已有三队矿工失联。斩一凶物记六十分，斩凶物之首者额外再赏；矿洞深处连通何地，至今无人知晓。', 0, '2026-11-18 08:29:41');
INSERT INTO `quest` VALUES (6, '血月夜守卫仙古战场', '⚔', '真仙', 300, '上界 · 边荒古域盟', '每逢血月当空，仙古战场万古战魂复苏，古域边村屡遭波及。悬赏修士于血月三夜登墙守卫，护一方生民——能稳住道心者，亦是难得的磨砺。', 1, '2026-11-18 08:29:41');
INSERT INTO `quest` VALUES (7, '采集太阳真火余烬', '☀', '真仙', 300, '火国 · 祭火殿', '太阳神殿开启在即，祭火殿需集齐九枚太阳真火余烬重燃圣火。火山之巅烈焰焚身，寻常修士近前即化飞灰；每采一枚记积分，火灵之体者优先。', 0, '2026-11-18 08:29:41');
INSERT INTO `quest` VALUES (8, '寻回失窃的宝术残页', '📜', '真仙', 300, '补天阁 · 藏经阁', '补天阁藏经阁一部上乘宝术残页被盗贼窃走，据传已流入大荒黑市。寻回残页者记重分，擒得盗贼者另赏；阁主放言，此事关乎阁中颜面。', 0, '2026-11-18 08:29:41');
INSERT INTO `quest` VALUES (9, '为大荒村庄驱赶凶兽', '🐗', '上古', 100, '大荒 · 青石镇', '入秋以来铁背凶猪群屡屡下山毁田伤人，青石镇张贴悬赏：驱散兽群记积分，猎得兽首者加倍。任务不难，最适合初入大荒的年轻修士历练。', 1, '2026-11-18 08:29:41');
INSERT INTO `quest` VALUES (10, '药园除草捉虫', '🌱', '上古', 100, '百草堂', '百草堂灵田新育一批灵药幼苗，最忌杂草与噬灵虫。活计轻省、包一日三餐，日结积分；谨记灵药贵重，伤一株苗扣双倍。', 0, '2026-11-18 08:29:41');

-- ----------------------------
-- Table structure for quest_accept
-- ----------------------------
DROP TABLE IF EXISTS `quest_accept`;
CREATE TABLE `quest_accept`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `quest_id` bigint NOT NULL COMMENT '任务 id',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接取账号',
  `points` int NOT NULL DEFAULT 0 COMMENT '接取时留档的奖励积分（完成时按任务最新积分发放）',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '完成状态：PENDING 待完成 / COMPLETED 已完成（积分已发放）',
  `accepted_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接取时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '后台确认完成时间',
  `completed_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '确认完成的管理员账号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_quest_user`(`quest_id` ASC, `username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '任务接取记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quest_accept
-- ----------------------------
INSERT INTO `quest_accept` VALUES (1, 1, 'admin', 1000, 'COMPLETED', '2026-11-18 08:32:22', '2026-11-18 08:33:02', 'admin');
INSERT INTO `quest_accept` VALUES (2, 2, 'admin', 1000, 'COMPLETED', '2026-11-18 08:32:23', '2026-11-18 08:33:05', 'admin');
INSERT INTO `quest_accept` VALUES (3, 3, 'admin', 600, 'PENDING', '2026-11-18 08:32:25', NULL, NULL);
INSERT INTO `quest_accept` VALUES (4, 4, 'admin', 600, 'COMPLETED', '2026-11-18 08:32:29', '2026-11-18 08:33:08', 'admin');
INSERT INTO `quest_accept` VALUES (5, 8, 'admin', 300, 'PENDING', '2026-11-18 08:32:30', NULL, NULL);
INSERT INTO `quest_accept` VALUES (6, 10, 'admin', 100, 'COMPLETED', '2026-11-18 08:32:31', '2026-11-18 08:33:11', 'admin');

-- ----------------------------
-- Table structure for realm_signup
-- ----------------------------
DROP TABLE IF EXISTS `realm_signup`;
CREATE TABLE `realm_signup`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `realm_id` bigint NOT NULL COMMENT '秘境 id',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报名账号',
  `points` int NOT NULL DEFAULT 0 COMMENT '报名时留档的通关奖励积分',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '完成状态：PENDING 待完成 / COMPLETED 已完成（积分已发放）',
  `signed_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '后台确认完成时间',
  `completed_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '确认完成的管理员账号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_realm_user`(`realm_id` ASC, `username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '秘境报名记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of realm_signup
-- ----------------------------
INSERT INTO `realm_signup` VALUES (1, 1, 'admin', 1000, 'COMPLETED', '2026-11-18 08:32:38', '2026-11-18 08:32:48', 'admin');
INSERT INTO `realm_signup` VALUES (2, 5, 'admin', 300, 'COMPLETED', '2026-11-18 08:32:38', '2026-11-18 08:32:57', 'admin');
INSERT INTO `realm_signup` VALUES (3, 2, 'admin', 600, 'COMPLETED', '2026-11-18 08:32:39', '2026-11-18 08:32:53', 'admin');
INSERT INTO `realm_signup` VALUES (4, 7, 'admin', 100, 'PENDING', '2026-11-18 08:32:40', NULL, NULL);

-- ----------------------------
-- Table structure for secret_realm
-- ----------------------------
DROP TABLE IF EXISTS `secret_realm`;
CREATE TABLE `secret_realm`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '秘境名称',
  `glyph` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '卡面图标',
  `grade` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '上古' COMMENT '秘境等级（仙帝/仙王/真仙/上古）',
  `location` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所在地',
  `open_time` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开启时间',
  `duration` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开启时长',
  `quota` int NOT NULL DEFAULT 0 COMMENT '每期名额（0 表示不限）',
  `points` int NOT NULL DEFAULT 0 COMMENT '秘境通关奖励积分（后台确认完成后发放）',
  `rank_score` decimal(4, 1) NOT NULL DEFAULT 0.0 COMMENT '评级评分（「按评级」排序用）',
  `description` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '秘境介绍',
  `is_hot` tinyint NOT NULL DEFAULT 0 COMMENT '是否热门秘境：1 是 0 否',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE,
  INDEX `idx_grade`(`grade` ASC) USING BTREE,
  INDEX `idx_rank`(`rank_score` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '大荒秘境表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of secret_realm
-- ----------------------------
INSERT INTO `secret_realm` VALUES (1, '虚神界', '🌌', '仙帝', '九天之上 · 天幕裂隙', '每逢甲子 · 十月圆满之夜', '三十日', 100, 1000, 10.0, '仙帝陨落后道则化生的小世界，内藏仙帝道场与成帝机缘。界中日月与外界相异，三十日抵外界三载；入内者斩杀神念分身可夺「神位」，是下界天骄问鼎仙帝之路的第一关。', 1, '2026-11-18 08:29:41');
INSERT INTO `secret_realm` VALUES (2, '神灵之墓', '⚰', '仙王', '大荒极西 · 落神渊', '每岁清明 · 大雾弥漫之时', '十日', 60, 600, 9.5, '上古神灵埋骨之地，墓中神魂未灭，化作万千石像守护葬地。墓内有神灵残留的骨文感悟与半截神骨，悟性绝佳者可于雾中听见神灵低语，窥见仙古秘辛。', 1, '2026-11-18 08:29:41');
INSERT INTO `secret_realm` VALUES (3, '十凶陨落之地', '☄', '仙王', '大荒深处 · 万丈裂谷', '每逢雷劫 · 九霄雷动之刻', '七日', 50, 600, 9.0, '太古十凶陨落之处，凶地中十凶精血与宝术符文散落各处。谷中杀机四伏，凶兽横行；然所获宝术传承皆为世间绝学，历代不少大能皆自此谷中杀出。', 1, '2026-11-18 08:29:41');
INSERT INTO `secret_realm` VALUES (4, '至尊殿堂遗迹', '🏛', '仙王', '上界 · 至尊道场深处', '每三年 · 甲子日开门', '十五日', 40, 600, 9.0, '至尊殿堂遗留的试炼之地，殿中圣像可传六道轮回天功一脉。闯过三千级石阶者可入正殿受传承，石阶越往上凶险越甚，历史上无一人登顶。', 0, '2026-11-18 08:29:41');
INSERT INTO `secret_realm` VALUES (5, '仙古战场', '⚔', '真仙', '上界 · 边荒古域', '每岁秋分 · 血月当空', '二十日', 80, 300, 8.5, '仙古一战的主战场，残垣断壁间仍回荡着万古前的厮杀声。战场上散落着仙古修士的兵刃与战魂，血月之夜战魂复苏，是磨砺道心的绝佳凶地。', 1, '2026-11-18 08:29:41');
INSERT INTO `secret_realm` VALUES (6, '太阳神殿', '☀', '真仙', '火国 · 万丈火山之巅', '每岁夏至 · 烈阳正午', '三日', 30, 300, 8.0, '火道传承圣地，殿心悬浮一缕不灭太阳真火。火灵之体者入殿可引真火淬体、修成太阳宝术；寻常修士亦可凭火道秘宝入内夺火种，只是须受烈焰焚身之痛。', 0, '2026-11-18 08:29:41');
INSERT INTO `secret_realm` VALUES (7, '古矿洞', '⛏', '上古', '下界 · 石国边境', '不定期 · 随矿脉苏醒而开', '不限', 0, 100, 7.0, '一条通往大荒地底的幽深矿道，矿脉中偶有天然神料与上古遗物现世。矿洞深处连通未知古地，黑雾常年不散，进洞者十不存一，却总有人前赴后继。', 0, '2026-11-18 08:29:41');
INSERT INTO `secret_realm` VALUES (8, '大荒祖地', '🌿', '上古', '大荒腹地 · 石村之外', '四时轮转 · 万物复苏之春', '不限', 0, 100, 6.5, '大荒最古老的地域，草木通灵、古兽潜伏。祖地灵气远胜外界，是淬体炼骨的天然道场；柳神祭祀之地便在祖地边缘，得祭灵认可者可听其讲道。', 1, '2026-11-18 08:29:41');

-- ----------------------------
-- Table structure for shop_item
-- ----------------------------
DROP TABLE IF EXISTS `shop_item`;
CREATE TABLE `shop_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `category` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类：丹药 / 法宝 / 武器 / 高级功法',
  `grade` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '上乘' COMMENT '品阶（绝世/绝顶/一流/上乘/中乘）',
  `glyph` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '卡面图标',
  `price` int NOT NULL DEFAULT 0 COMMENT '兑换所需积分',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存件数（0 表示不限量）',
  `sold` int NOT NULL DEFAULT 0 COMMENT '已兑换件数',
  `description` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品简介',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',
  `is_hot` tinyint NOT NULL DEFAULT 0 COMMENT '是否热门：1 是 0 否',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop_name`(`name` ASC) USING BTREE,
  INDEX `idx_shop_cat`(`category` ASC) USING BTREE,
  INDEX `idx_shop_price`(`price` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 145 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '大荒宝库商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_item
-- ----------------------------
INSERT INTO `shop_item` VALUES (1, '涅槃丹', '丹药', '绝世', '🧪', 360, 5, 0, '以不死药残叶炼成的续命圣丹，肉身将死、神魂将散之时服下，可强行涅槃重生一次；一生只可服一枚，第二枚便再无灵效。', 1, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (2, '悟道丹', '丹药', '绝顶', '☯', 320, 0, 0, '渡劫前静心服下，可凝神守一、暂避心魔，令冲击境界时道心通透；丹力只在一炷香内有效，须掐准时机。', 2, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (3, '九转还魂丹', '丹药', '绝顶', '🜂', 280, 0, 3, '九蒸九晒、九转九炼而成，魂魄离体半个时辰内服下仍可唤回；药香一缕，足以让垂死之人睁开眼睛。', 3, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (4, '万灵血丹', '丹药', '一流', '🩸', 180, 0, 0, '取百种凶兽精血凝炼，服之血气翻涌、肉身如烘炉，最适合淬体境的年轻修士打熬筋骨。', 4, 0, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (5, '神曦丹', '丹药', '一流', '🌤', 140, 0, 0, '采晨昏神曦凝成的小丹，入口化为暖流洗涤经脉，久服可令洞天更加开阔。', 5, 0, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (6, '淬体丹', '丹药', '上乘', '🪨', 60, 0, 0, '大荒药堂最常见的入门丹丸，药性温吞却胜在便宜，搬血境修士人人吃得。', 6, 0, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (7, '飞仙石', '法宝', '绝世', '💎', 520, 3, 0, '传说中可载人飞升上界的奇石，握之如握一段时光；石中蕴含飞仙之力，重伤时可借其力瞬间远遁千里。三期只放出三枚。', 1, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (8, '九色神莲', '法宝', '绝世', '🌸', 460, 3, 0, '生于仙古禁地深处的九色莲台，一瓣一色、一色一法，端坐其上可抵御神识侵袭与诅咒之力。', 2, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (9, '长生灯', '法宝', '绝顶', '🏮', 380, 0, 0, '灯芯燃的是自身一缕神魂，灯不灭则命不绝；于绝境中点燃，可为持灯者争来最后一口气。', 3, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (10, '紫金葫芦', '法宝', '绝顶', '🏺', 300, 0, 0, '开口一吸可收人入壶，壶中自成一方小世界；对手修为高出太多时，葫芦亦会被撑裂。', 4, 0, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (11, '乾坤袋', '法宝', '一流', '🎒', 200, 0, 0, '芥子纳须弥的储物法宝，一袋可装下半座山峰的行囊；大荒行走者几乎人手一只。', 5, 0, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (12, '聚灵阵盘', '法宝', '上乘', '🔮', 110, 0, 0, '刻有聚灵符文的青铜阵盘，布于居所四周可将天地灵气收拢三成，打坐吐纳时颇有裨益。', 6, 0, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (13, '星辰剑', '武器', '绝世', '⚔️', 480, 2, 0, '以上界陨铁铸就，剑身内封一缕星辰之力，出鞘时剑光如银河倒卷；相传曾斩落过一尊伪神。', 1, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (14, '龙纹黑金戟', '武器', '绝世', '🔱', 440, 2, 0, '黑金为骨、龙纹为魂，戟锋所指可裂虚空；唯有真龙一脉的肉身之力方能将其威能尽数催发。', 2, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (15, '妖皇尺', '武器', '绝顶', '🪄', 360, 0, 0, '妖皇成道前随身之物，尺身古朴无锋，一尺落下却有镇压万妖之威。', 3, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (16, '雷神锤', '武器', '绝顶', '🔨', 300, 0, 0, '锤身缠绕雷光，一锤落地引动九霄雷霆，最克阴邪与魂体。', 4, 0, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (17, '白骨大棒', '武器', '一流', '🦴', 170, 0, 0, '荒兽腿骨打磨而成的大棒，朴实无华却极重，石村少年们最爱用它练手。', 5, 0, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (18, '玄铁重剑', '武器', '上乘', '🗡️', 90, 0, 0, '重剑无锋、大巧不工，玄铁百炼而成，压手得很，正合初入大荒的修士打基础。', 6, 0, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (19, '原始真解', '高级功法', '绝世', '📜', 1200, 5, 0, '万法之基，神引、超脱、终极三篇尽在其中；不讲宝术、只讲骨文之道。兑换后可于功法首页细读全篇。', 1, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (20, '六道轮回天功', '高级功法', '绝世', '🌀', 980, 5, 0, '至尊殿堂镇教传承，可同时驾驭六种绝世神通——一击照亮永恒，是后期最凌厉的爆发手段。', 2, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (21, '鲲鹏宝术', '高级功法', '绝顶', '🐟', 760, 0, 0, '太古十凶之鲲鹏传承，掌阴阳衍变：大海无量、太阳拳、十万神羽化剑炉，皆在其中。', 3, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (22, '雷帝宝术', '高级功法', '绝顶', '⚡', 720, 0, 0, '汇雷道四绝天，掌雷霆本源，可引天劫炼体，衍化上苍劫光与雷帝指。', 4, 1, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (23, '柳神法', '高级功法', '绝顶', '🌳', 680, 0, 0, '古法与柳神涅槃新术相合，柔中藏刚、生机无尽；柳枝垂落之处万物回春，亦可绞杀神魔。', 5, 0, '2026-11-18 08:29:41');
INSERT INTO `shop_item` VALUES (24, '八九天功', '高级功法', '绝顶', '🐒', 640, 0, 0, '混世魔猿族古经，含七十二变与三头六臂之术，唯有在厮杀中才能变强。', 6, 0, '2026-11-18 08:29:41');

-- ----------------------------
-- Table structure for shop_order
-- ----------------------------
DROP TABLE IF EXISTS `shop_order`;
CREATE TABLE `shop_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '兑换账号',
  `item_id` bigint NOT NULL COMMENT '商品 id',
  `item_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称（留档，商品改名不影响记录）',
  `category` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品分类（留档）',
  `price` int NOT NULL DEFAULT 0 COMMENT '实际扣除的积分',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_order_user`(`username` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_shop_order_item`(`item_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '宝库兑换记录（积分消耗账本）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_order
-- ----------------------------
INSERT INTO `shop_order` VALUES (1, 'admin', 3, '九转还魂丹', '丹药', 280, '2026-11-18 09:52:51');
INSERT INTO `shop_order` VALUES (2, 'admin', 3, '九转还魂丹', '丹药', 280, '2026-11-18 09:52:52');
INSERT INTO `shop_order` VALUES (3, 'admin', 3, '九转还魂丹', '丹药', 280, '2026-11-18 09:53:13');

-- ----------------------------
-- Table structure for star_user
-- ----------------------------
DROP TABLE IF EXISTS `star_user`;
CREATE TABLE `star_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璐﹀彿',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码（明文存储，演示项目不做加密）',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '鏄电О',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `permission` tinyint NOT NULL DEFAULT 1 COMMENT '权限：0 管理员 / 1 默认（普通修士）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '瀹岀編涓栫晫鐢ㄦ埛琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of star_user
-- ----------------------------
INSERT INTO `star_user` VALUES (1, 'admin', '123456', 'song', '2026-11-18 08:29:42', 0);

-- ----------------------------
-- Table structure for user_cultivation
-- ----------------------------
DROP TABLE IF EXISTS `user_cultivation`;
CREATE TABLE `user_cultivation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账号',
  `realm_index` int NOT NULL DEFAULT 0 COMMENT '当前境界序号（0=搬血 … 17=仙帝）',
  `exp` int NOT NULL DEFAULT 0 COMMENT '本境已积修为（突破后结转溢出部分）',
  `total_exp` int NOT NULL DEFAULT 0 COMMENT '历史累计修为（只增不减，用于展示）',
  `breakthrough_count` int NOT NULL DEFAULT 0 COMMENT '成功突破次数',
  `fail_count` int NOT NULL DEFAULT 0 COMMENT '渡劫失败次数',
  `last_meditate_at` datetime NULL DEFAULT NULL COMMENT '上次打坐吐纳时间（冷却判定）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_cult_user`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '修士修炼档案（境界/修为）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_cultivation
-- ----------------------------
INSERT INTO `user_cultivation` VALUES (1, 'admin', 6, 111, 1578, 6, 2, '2026-11-18 09:51:56', '2026-11-18 08:30:46', '2026-11-18 08:30:46');

-- ----------------------------
-- Table structure for world_character
-- ----------------------------
DROP TABLE IF EXISTS `world_character`;
CREATE TABLE `world_character`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '人物姓名',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '称号（如 荒天帝）',
  `glyph` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '卡面图标',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像图片路径（如 /avatars/shihao.jpg）',
  `realm` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '当前境界',
  `faction` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '出身/所属势力',
  `rank_score` decimal(4, 1) NOT NULL DEFAULT 0.0 COMMENT '实力评分（「按实力」排序用）',
  `grade` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '尊者' COMMENT '定位分级（仙帝/准仙帝/仙王/真仙/尊者）',
  `quote` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '经典语录',
  `description` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '人物简介',
  `is_hot` tinyint NOT NULL DEFAULT 0 COMMENT '是否热门人物：1 是 0 否',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE,
  INDEX `idx_faction`(`faction` ASC) USING BTREE,
  INDEX `idx_grade`(`grade` ASC) USING BTREE,
  INDEX `idx_rank`(`rank_score` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 133 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '大荒人物志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of world_character
-- ----------------------------
INSERT INTO `world_character` VALUES (1, '石昊', '荒天帝', '🌞', '/avatars/shihao.jpg', '仙帝境', '石族天庭', 10.0, '仙帝', '我为至尊，谁与争锋！', '天生至尊，却被人偷取至尊骨；自下界大荒一步步杀出，独断万古、镇压九天十地。以身为种独创末法道途，最终于仙帝之位独断万古，是《完美世界》无可争议的第一主角。', 1, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (2, '柳神', '柳神 · 祭灵之神', '🌿', '/avatars/liushen.jpg', '仙王境（巅峰曾触准仙帝）', '大荒祖地', 9.5, '准仙帝', '一枝独秀不为春，万古长青是柳神。', '石昊的启蒙恩师，大荒石村祭灵，真身为上界逃亡而来的仙王级神灵。以一截柳枝护石村周全，传石昊原始真解；一生孤傲坚强，为护石昊屡次血战，是读者心中永远的意难平。', 1, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (3, '小塔', '无名宝塔 · 塔灵', '🗼', '/avatars/xiaota.jpg', '准仙帝器灵', '上界遗迹', 9.5, '准仙帝', '吾曾镇压一界。', '来历神秘的残破宝塔塔灵，曾为仙帝器物，因大战陨落残存。跟随石昊一路成长，关键时刻屡屡出手镇压强敌，最后关头为石昊挡下致命一击，重归寂静。', 1, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (4, '曹雨生', '道祖 · 岁月长河客', '⏳', '/avatars/caoyusheng.jpg', '仙帝境', '岁月长河', 9.5, '仙帝', '我自岁月中来，亦将往岁月中去。', '行走于岁月长河中的神秘老者，算尽古今、布局万古；借荒天帝之势对抗黑暗动乱，是贯穿三世（完美世界·遮天·圣墟）的关键人物。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (5, '火灵儿', '火国公主 · 火凰', '🔥', '/avatars/huolinger.jpg', '真仙境', '火国', 8.5, '真仙', '哪怕化作一捧灰烬，我也要陪你走完这条仙路。', '火国公主，天生火灵之体，性情娇蛮却重情重义。与石昊自幼相识、两小无猜；上界重逢后因国仇家恨苦修行道，最终于黑暗动乱中香消玉殒，只留一缕真灵于火中重生。', 1, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (6, '云曦', '天狐仙子', '🌙', '/avatars/yunxi.jpg', '真仙境', '天神书院', 8.5, '真仙', '此生所求，不过是与君并肩看一场大荒落日。', '天神书院圣地仙子，容颜绝世、心性通透。曾与石昊在下界并肩历险，情愫暗生；上界重聚后随其共抗异域，是石昊身边最温柔的守望者。', 1, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (7, '秦昊', '不灭生灵 · 命运双子', '☯', '/avatars/qinhao.jpg', '仙王境', '石族天庭', 8.0, '仙王', '你我本一体，何分彼此。', '石昊的胞弟，天生携不灭神通。幼时被秦族夺走，兄弟二人命运纠缠半生；从敌对到并肩，最终在黑暗动乱中以不灭之躯为石昊守住身后天地。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (8, '石中天', '石昊祖父 · 战族之魂', '⚔', '/avatars/shizhongtian.jpg', '仙王境', '石族', 8.0, '仙王', '我石家儿郎，宁可站着死，绝不跪着生！', '石昊的祖父，因家族变故流落上界，一生都在等一个重振石族的机会。性如烈火、战意滔天，为护孙儿敢与整个上界为敌，是石昊血脉中最刚烈的一面。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (9, '天角蚁', '太古蚁皇 · 力之极致', '🐜', '/avatars/tianjiaoyi.jpg', '仙王境', '蚁族', 8.0, '仙王', '一力降十会，万法皆可破。', '太古凶虫蚁族的皇者，天生神力冠绝古今，一拳可碎星辰。与石昊不打不相识，结为生死之交，是石昊麾下最可靠的先锋大将。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (10, '神蚕公主', '神蚕族 · 千丝万缕', '🕷', '/avatars/shencan.jpg', '真仙境', '神蚕族', 7.5, '真仙', '丝可缚仙，亦可护你周全。', '神蚕族公主，掌握缚仙神丝与变化之术，古灵精怪、爱憎分明。自上界初遇石昊后便一路同行，于大战中屡出奇兵。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (11, '黄金狮子', '狮王 · 战吼震天', '🦁', '/avatars/huangjingshizi.jpg', '真仙境', '狮族', 7.5, '真仙', '咆哮一声，山河变色！', '黄金狮子族的年轻王者，嗓音如雷、战力狂暴，与石昊从宿敌打成挚友；每次出场必掀腥风血雨，是大荒中著名的莽撞王。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (12, '孟天正', '天神书院 · 护道人', '📖', '/avatars/mengtianzheng.jpg', '仙王境', '天神书院', 7.5, '仙王', '书院之道，有教无类，有敌必诛。', '天神书院的执剑长老，护道人风范：对弟子温和如春风，对敌人冷冽如霜刃。云曦的授业恩师，多次为书院弟子挡下灭顶之灾。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (13, '真凰', '十凶 · 浴火神凰', '🦅', '/avatars/zhenhuang.jpg', '陨落古祖（十凶之列）', '太古十凶', 8.8, '仙王', '涅槃一次，万劫不灭。', '太古十凶之一，通体燃烧不灭神焰，浴火可涅槃、振翅焚九天。其真凰宝术为仙古至高传承之一，陨落后宝术符文散落诸天，得其一羽者便可称霸一方。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (14, '鲲鹏', '十凶 · 吞天之鲲', '🐟', '/avatars/kunpeng.jpg', '陨落古祖（十凶之列）', '太古十凶', 8.5, '仙王', '化鲲沉北冥，化鹏镇九天。', '太古十凶之一，身兼水陆空三栖之能：化鲲可吞海，化鹏可蔽日，速度冠绝古今。其鲲鹏宝术讲究至速至刚，是历代天骄梦寐以求的绝世传承。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (15, '火皇', '火国大帝 · 火灵儿之父', '👑', '/avatars/huohuang.jpg', '仙王境', '火国', 8.0, '仙王', '火国之地，朕即烈阳。', '火国至高掌权者，火灵儿之父。执掌一国气运多年，杀伐果断却护短情深；女儿于黑暗动乱中殒身后，一夜白头，独坐万丈火山之巅望穿岁月。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (16, '麒麟', '十凶 · 祥瑞古麟', '🦌', '/avatars/qilin.jpg', '陨落古祖（十凶之列）', '太古十凶', 8.0, '仙王', '麟踏之地，气运加身。', '太古十凶之一，身负祥瑞与凶威一体：踏地生莲、吼落星辰。其麒麟宝术蕴含气运之道，得传者逢凶化吉，于大战中常能扭转命数。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (17, '大魔神', '石族疯魔战神', '😈', '/avatars/damoshen.jpg', '仙王境', '石族', 7.8, '仙王', '心若疯魔，何惧天地！', '石族走出的一位传奇战神，因至亲之痛入疯魔之境，越战越狂、以伤换命。与石中天同辈论交，是石族血脉里最令人胆寒也最令人唏嘘的一面战旗。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (18, '狻猊', '十凶 · 雷音狻猊', '🦁', '/avatars/suanni.jpg', '陨落古祖（十凶之列）', '太古十凶', 7.6, '真仙', '一声雷音，万兽俯首。', '太古十凶之一，形似狮而带雷纹，吼声即雷罚，可震碎神魂。其狻猊宝术以音波攻伐见长，石昊于大荒得此传承，自此战吼慑敌、先声夺人。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (19, '月婵', '月华仙子 · 上界圣女', '🌕', '/avatars/yuechan.jpg', '真仙境', '上界大教', 7.2, '真仙', '月华照千里，不及他一眼。', '上界大教圣女，天生月华之体，清冷绝尘、道心剔透。与云曦齐名的绝代仙子，曾与石昊于秘境中数度交锋，从针锋相对到暗生敬意，是月下最耀眼的一抹清辉。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (20, '石毅', '石国皇子 · 窃骨之人', '🗡', '/avatars/shiyi.jpg', '尊者境', '石国', 6.0, '尊者', '这至尊骨，本该属于我。', '石国皇子，幼时被移入本属石昊的至尊骨，天资横压一国。然而窃来的气运终有归位之日：石昊归来夺骨，两人恩怨纠缠半生，是石昊登顶路上最早也是最重的阴影。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (21, '秦怡宁', '石昊之母 · 温柔守望', '🌸', '/avatars/qinyining.jpg', '尊者境', '石族', 5.8, '尊者', '娘亲不求你无敌天下，只愿你平安归来。', '石昊之母，性情温婉却极有主见。当年忍痛将幼子留在石村，此后半生都在为家族与儿子之间奔走周旋；乱世之中，她是石昊身后最柔软也最坚韧的那道目光。', 0, '2026-11-18 08:29:41');
INSERT INTO `world_character` VALUES (22, '石云峰', '石村老族长', '🧓', '/avatars/shiyunfeng.jpg', '凡俗巅峰（未证仙途）', '石村', 5.5, '尊者', '小不点，慢些跑，骨头汤要凉了。', '石村老族长，看着石昊从蹒跚学步到踏破山河。一生未出大荒，却把全村最好的东西都留给了那个捧着兽奶碗的孩子；石昊每念及故乡，先想起的总是这一碗热汤。', 0, '2026-11-18 08:29:41');

SET FOREIGN_KEY_CHECKS = 1;
