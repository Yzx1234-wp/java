# 完美世界 项目 · AI 启动索引 (PROJECT_AI_NOTE.md)

> 本文件供 AI 助手快速理解项目，避免本机每次重置后重新探索。
> 重置约定：node_modules、target 会被清空；MySQL 服务保留，但库可能需重建。
> 下次启动本项目，请先读本文件，然后直接跑 `start.bat` / `pause.bat`。
> 2026-10-16：整体 UI 已由「吞噬星空」科幻风改版为动漫《完美世界》玄幻风
> （仙古金 + 翠柳青配色、大荒背景图、石昊/柳神等演示数据），后端接口未变。

## 0. 一句话结论
一套「星域登录/注册」全栈 Demo：Spring Boot 后端 + Vue3 前端，MySQL 存储。
启动顺序：MySQL → 建库 → 构建后端 → 起后端(8081) → 装前端依赖 → 起前端(5173)。
已提供 `start.bat` / `pause.bat` 一键启停（GBK 编码，路径基于脚本目录可移动）。

## 1. 项目结构
- `springBootTest/` —— 后端 (Spring Boot 3.4.5 / MyBatis-Plus 3.5.9 / MySQL8 / BCrypt)
  - 启动类：`src/main/java/com/fenglin/springboottest/SpringBootTestApplication.java`
  - 配置：`src/main/resources/application-dev.properties`（端口 8081，数据源 root/123456，库 swallowed_star）
  - 启动建表：`src/main/resources/schema.sql`（**只建表，不建库**）
  - 本地建库建表脚本：`star_user.sql`（根目录，幂等，必须先执行以保证库存在）
  - 构建产物：`target/springBootTest-0.0.1-SNAPSHOT.jar`
- `swallowed-star-vue/` —— 前端 (Vue3 + Vite5)
  - 代理：`vite.config.js` 把 `/api` 代理到 `http://192.168.68.71:8081`
  - 脚本：`npm run dev`(5173) / build / preview / lint / format
  - 依赖锁：`package-lock.json` 存在，可用 `npm ci`

## 2. 本机环境（已验证 2026-10-15）
- Java：OpenJDK 21 (Zulu) ✅ 满足 Spring Boot 3.4.5 需要 17+
- Node：v22.22.2 / npm 10.9.7 ✅
- Maven：不在 PATH；IDEA 装在 **D 盘**，用 IDEA 自带 →
  `D:\IntelliJ IDEA 2024.3.2.2\plugins\maven\lib\maven3\bin\mvn.cmd`
  - `start.bat` 已做多候选路径探测：先试 D 盘已知路径，再扫 `C:\Program Files\JetBrains\IntelliJ IDEA *`、`D:\IntelliJ IDEA *` 等，最后退回项目 `springBootTest/mvnw.cmd`（wrapper 会下载 Maven 3.9.9，README 提示个别环境会损坏，优先用 IDEA Maven）
  - `~/.m2/repository` 已缓存 763M，可离线构建
- MySQL：服务名 `MySQL80`（当前运行中）；客户端
  `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`；账号 root / 123456；
  库 `swallowed_star` 初始不存在（需初始化）

## 3. 启动步骤（下次直接跑 start.bat，无需逐步手动）
1. `net start MySQL80`（若已运行忽略）
2. `"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -uroot -p123456 < springBootTest\star_user.sql`（建库+表，幂等）
3. 后端构建：`mvn -DskipTests package`（在 springBootTest 目录；target 被清空需重建）
4. 后端运行：`java -jar target\springBootTest-0.0.1-SNAPSHOT.jar --server.port=8081 --spring.profiles.active=dev`
5. 前端：`cd swallowed-star-vue && npm install && npm run dev`（node_modules 被清空需重装）
- 演示账号：`admin / 123456`（后端首次启动由 DataInitializer 自动创建）

## 4. 常见坑（已写进脚本，这里留给 AI 复盘）
- `schema.sql` 只建表不建库 → 必须先跑 `star_user.sql`，否则后端起不来。
- Maven 不在 PATH → 用 IDEA 自带 Maven 绝对路径（脚本已处理）。
- MySQL 客户端不在 PATH → 脚本用绝对路径。
- node_modules / target 每次重置被删 → 脚本每次重新 install / package。
- 若重置把 `~/.m2` 也清了，首次 `mvn package` 需联网下载依赖。
- 2026-10-21 修复：`start` 里不要再写 `cmd /k "cd /d "%VAR%" && ..."`（引号嵌套会被 cmd 拆碎导致路径报错），
  统一改为 `start "标题" /d "%VAR%" cmd /k "命令"`，已实测前后端窗口工作目录正确。
  同时把 Maven 备选探测从 `FOR /D ... ("路径 通配符")`（带引号不展开）改为 `FOR /F` + `dir /b /ad`。
- 2026-10-21 第二处修复（真凶）：`swallowed-star-vue/node_modules` 可能是**空壳目录**——
  只有 108 个空文件夹、总大小 188K、没有 `.bin\vite.cmd`，`vite/` 里连 `bin` 都没有。
  此时 `npm run dev` 直接报找不到 vite。判定方式：看 `node_modules\.bin\vite.cmd` 是否存在，
  不要只看 `node_modules` 目录是否存在。已用 `npm ci` 重装（135 包，约 2 分钟，现 69M）。
- 2026-10-21 第三处修复：`IF (...)` 块内的 `echo` **不能出现半角括号**，否则报「此时不应有 ...」，
  需改成中文括号或去掉。
- 已把固定 `timeout /t 10` 换成轮询 8081 端口（最多 60s），后端慢启动也不会误判。

## 5. 启停脚本说明
- `start.bat`（GBK）：一键执行上面 1–5，后端/前端各开一个命令行窗口常驻。
- `pause.bat`（GBK）：按端口 8081/5173 杀进程并关闭窗口（保留 MySQL）。
- 路径全部基于 `%~dp0`（脚本所在目录）的相对引用，项目文件夹可整体移动。

## 6. 接口速查
- 基址：`http://192.168.68.71:8081/api`
- GET `/api/auth/me`（带 `Authorization: Bearer <token>`）
- POST `/api/auth/login` `{username,password}` → token
- POST `/api/auth/register` `{username,password}`
- POST `/api/auth/logout`

## 7. 后台管理系统（2026-10-26 新增，2026-11-02 已重构为 2.0，见第 12 节）
首页导航栏右侧新增「后台管理」按钮 → 跳转 `/admin/login`（仅登录、无注册，账号复用 admin/123456）。
登录后进入 `/admin`，对首页「大荒功法库」数据（gongfa 表）做增删改查。
- 前端（与前台会话独立，token 存 `ss_admin_token`）：
  - `src/api/admin.js` 接口层；`src/views/admin/AdminLogin.vue` 登录页；`src/views/admin/AdminIndex.vue` 管理页
  - 路由守卫：无 `ss_admin_token` 访问 `/admin` 会跳 `/admin/login`
- 后端：`controller/AdminController.java`，`/api/admin/gongfa/list|POST|PUT/{id}|DELETE/{id}`，
  全部走 Bearer token 鉴权（复用 TokenStore，未带/失效返回 401）
- 注意：Gongfa 的 JSON 字段名为 `from / rank / desc / hot`（与首页卡片一致），前端提交表单已做映射

## 8. 大荒人物志 · 人物介绍页（2026-10-28 新增）
首页导航由「功法首页 / 十凶宝术 / 剑诀秘藏 / 至尊骨术 / 修行问道」精简为「功法首页 / 人物介绍」，
原四个占位模块删除；点「人物介绍」进入 `/characters`。
- 数据表：`world_character`（schema.sql 启动自动建表），演示人物 12 位由 data.sql `INSERT IGNORE` 幂等灌入
- 后端：`entity/WorldCharacter.java`（JSON 字段 `rank/hot` 映射）、
  `mapper/WorldCharacterMapper.java`、`service/CharacterService(+Impl)`、`controller/CharacterController`
  - 接口：GET `/api/characters/list?keyword&faction&grade&sortBy`、`/factions`、`/grades`、`/hot`、`/{id}`
  - 分级：仙帝/准仙帝/仙王/真仙/尊者；势力按首屏数据自动聚合
- 前端：`src/views/Characters.vue`（金色玄幻风，与登录页同主题：panel 卡片 + 仙古金高亮 + 详情弹层）、
  `src/api/character.js`；路由 `/characters` 有 `ss_token` 守卫，导航复用全局 `.topnav` 样式
- 2026-10-28 已实测：`mvn package` 后重启 8081，人物接口全部返回正常；前端 vite 热更新后 `/characters` 200

## 9. 大荒秘境录 · 秘境首页（2026-10-28 新增）
首页导航现为「功法首页 / 人物介绍 / 秘境首页」三页互跳；点「秘境首页」进入 `/realms`。
- 数据表：`secret_realm`（秘境主表）+ `realm_signup`（报名记录，realm_id+username 唯一限报一次），
  schema.sql 启动自动建表，data.sql 幂等灌入 8 处演示秘境（虚神界/神灵之墓/十凶陨落之地等）
- 后端：`entity/SecretRealm.java`（JSON 字段 `openTime/rank/desc/hot`；`signedCount/signed` 为非表字段查询时填充）、
  `entity/RealmSignup.java`、`mapper/SecretRealmMapper` + `RealmSignupMapper`、`service/RealmService(+Impl)`、
  `controller/RealmController`、`controller/dto/RealmSignupRequest`
  - 接口：GET `/api/realms/list?keyword&grade&sortBy`（token 可选，带 token 返回 signed 标记）、
    `/grades`、`/hot`、`/{id}`、GET `/my`（需登录）、POST `/signup` {realmId}（需登录，校验名额/重复）、
    DELETE `/signup/{realmId}`（取消报名）
  - 等级：仙帝/仙王/真仙/上古；quota=0 表示不限名额
  - 坑：TokenStore 为 ConcurrentHashMap，`getUserId(null)` 会 NPE → controller 解析 token 前必须判空
- 前端：`src/views/Realms.vue`（登录页同款金色玄幻风；卡片含开启时间横幅、名额进度条、报名/取消按钮、详情弹层）、
  `src/api/realm.js`；路由 `/realms` 有 `ss_token` 守卫；Home.vue 与 Characters.vue 导航均已加「秘境首页」
- 2026-10-28 已实测：报名→重复报名拒绝→我的报名→取消报名全链路通过；vite `/realms` 200

## 10. 大荒悬赏榜 · 任务接取页（2026-10-28 新增）
首页导航现为四页互跳：「功法首页 / 人物介绍 / 秘境首页 / 任务接取」；点「任务接取」进入 `/quests`。
- 数据表：`quest`（悬赏主表，grade 等级 + points 积分，等级越高积分越多）
  + `quest_accept`（接取记录，quest_id+username 唯一限接一次，points 留档），
  schema.sql 启动自动建表，data.sql 幂等灌入 10 项演示任务
- 后端：`entity/Quest.java`（JSON 字段 `desc/hot`；`acceptedCount/accepted` 非表字段查询时填充）、
  `entity/QuestAccept.java`、`mapper/QuestMapper` + `QuestAcceptMapper`（含 `sumPoints` 求总积分）、
  `service/QuestService(+Impl)`、`controller/QuestController`、`controller/dto/QuestAcceptRequest`
  - 接口：GET `/api/quests/list?keyword&grade&sortBy`（token 可选，带 token 返回 accepted 标记）、
    `/grades`、`/hot`、`/{id}`、GET `/points`（我的总积分，需登录）、GET `/my`（我的接取，需登录）、
    POST `/accept` {questId}（需登录，接取即发积分、防重复）
  - 等级与积分：仙帝 1000 / 仙王 600 / 真仙 300 / 上古 100（积分存表，排序按积分递降）
- 前端：`src/views/Quests.vue`（登录页同款金色玄幻风；积分高亮、导航栏积分徽章、统计条、
  接取按钮+详情弹层，已接取变翠色）+ `src/api/quest.js`；路由 `/quests` 有 `ss_token` 守卫；
  Home.vue / Characters.vue / Realms.vue 导航均已加「任务接取」
- 2026-10-28 已实测：接取仙帝级 +1000 → 接取上古级 +100（总 1100）→ 重复接取拒绝 →
  我的积分/接取记录正确；未登录浏览正常（token 判空已吸取 realms 模块 NPE 教训）；vite `/quests` 200

## 11. 我的主页 · 修士名帖（2026-10-28 新增）
首页导航现为五页互跳：「功法首页 / 人物介绍 / 秘境首页 / 任务接取 / 我的主页」；点「我的主页」进入 `/profile`。
- **不新建表**：全部由 star_user + quest_accept + realm_signup + quest + secret_realm 聚合而来
- 后端：`controller/dto/ProfileVO.java`（summary 聚合 VO：账号/总积分/境界/成就/动态）、
  `service/ProfileService(+Impl)`、`controller/ProfileController`
  - 接口（全部需登录，未登录 401）：GET `/api/profile/summary`（总览：总积分、任务/秘境数、
    修炼境界 tier（凡人→搬血→…→仙帝共 17 阶，按总积分定位并给进度）、8 枚成就徽章 badges、
    修炼动态 activities（任务+秘境合并时间线最近 12 条））、GET `/quests`（我的任务明细含接取时间与留档积分）、
    GET `/realms`（我的秘境明细含报名时间与开启时间）
- 前端：`src/views/Profile.vue`（登录页同款金色玄幻风；名帖卡+统计条+境界进度条+成就徽章墙
  （未点亮灰显）+ 任务/秘境/动态三页签列表）+ `src/api/profile.js`；路由 `/profile` 有 `ss_token` 守卫；
  Home/Characters/Realms/Quests 四页导航均已加「我的主页」
- 2026-10-28 已实测：summary 返回总积分 1100 / 铭纹境 66.7% / 4 枚徽章点亮；报名秘境后
  动态时间线正确合并；未登录 401；vite `/profile` 200

## 12. 后台管理控制台 2.0（2026-11-02 重构）

原后台只有「功法管理」单页，且存在多处不可用/缺陷，本次整体重构：

### 修复的原有缺陷
- 重名新增/修改会直接 500（Spring 默认错误页）→ 新增 GlobalExceptionHandler，
  唯一键冲突翻译成「xx名称「yy」已存在，请换一个」等友好提示
- 品阶评分量纲错误：数据实际是 0-10，旧表单却让填 0-100 → 统一为 0-10（步进 0.1）
- 任何注册用户拿到自己的 token 都能调 /api/admin/* → 新增 AdminGuard 管理员白名单
  （application.properties 的 admin.allow-users，默认 admin），非管理员返回 403
- 后台页内容超高无法滚动（body overflow:hidden）→ .adm-content 自带滚动容器

### 后端新增（全部在 /api/admin/*，均需管理员 Bearer token）
- `common/GlobalExceptionHandler`：唯一键/约束/参数/兜底异常统一翻译
- `service/AdminGuard`：token 校验 + 管理员白名单（401/403）
- `common/Validators`：表单校验小工具；`controller/dto/IdsRequest|PasswordRequest|AdminUserVO`
- AdminController（功法）：list/categories/create/update/delete/batch-delete/toggle-hot，
  新增前先查重名（409），评分 0-10 校验
- AdminCharacterController（人物）：同上 CRUD，字段为 description（不是 desc！）
- AdminRealmController（秘境）：CRUD + GET /{id}/signups + DELETE /signups/{signupId}，
  删除秘境时连带清理 realm_signup
- AdminQuestController（悬赏）：CRUD + GET /{id}/accepts + DELETE /accepts/{acceptId}，
  删除任务时连带清理 quest_accept
- AdminUserController（修士账号）：list（含积分/接取数/报名数聚合）、PUT /{id} 改昵称、
  PUT /{id}/password 重置密码、DELETE /{id} 删账号（连带报名/接取记录，管理员账号禁删）
- AdminOverviewController：GET /me（当前管理员）、GET /overview（各模块计数、
  分类/等级分布、最近 8 条报名与接取动态）
- Mapper 新增：RealmSignupMapper.countByUser、QuestAcceptMapper.statsByUser
- 配置：application.properties 增加 `admin.allow-users=admin`

### 前端重构（swallowed-star-vue）
- 路由：/admin 变为布局路由（AdminLayout.vue），子路由
  /admin/dashboard|gongfa|characters|realms|quests|users；守卫校验 ss_admin_token
- 删除旧 AdminIndex.vue；AdminLogin.vue 登录后调 /admin/me 校验权限（非管理员 403 提示）
- `src/api/admin.js` 重写：crud() 工厂 + isAuthError；401/403 统一跳登录页
- 通用件：views/admin/components/{AdminTable,AdminFormModal,AdminConfirm,AdminRecordModal}.vue、
  composables/useCrudPage.js、admin.css（adm- 前缀样式）
- 六个页面：Dashboard（概览+分布条+动态流）、GongfaManage、CharacterManage、
  RealmManage（含报名记录弹窗+撤销）、QuestManage（含接取记录弹窗+撤销）、
  UserManage（改昵称/重置密码/删账号）
- 表格支持多选批量删除、热门一键切换、排序下拉、客户端分页

### 2026-11-02 已实测
- mvn package 后重启 8081：全模块 CRUD/重名 409/校验 400/越权 403/记录撤销 全部 curl 通过
- vite build + eslint 通过；headless Chrome 实测六个页面渲染、
  新增→重名拦截→编辑→删除→报名记录→改昵称→退出登录 全链路 UI 通过
- 注意：本机沙箱环境变量 SERVER__PORT 会覆盖端口，手工起后端务必带 `--server.port=8081`

## 13. 人物页改版 + 人物头像（2026-11-02）

### 天骄榜位置调整
- Characters.vue 主体改为两栏 `.char-body`：左侧人物列表（minmax(0,1fr)）+ 右侧天骄榜
  （`.rank-side` 300px，`position:sticky; top:96px`，超高自带滚动）
- ≤1080px 时榜单折叠到列表上方（order:-1），榜单两列；≤640px 榜单单列
- 榜单行改为紧凑两行式：名次/头像/姓名+境界/评分 一行 + 底部实力条（flex-wrap 全宽）

### 人物头像
- AI 生成了 22 张水墨/厚涂仙侠风半身头像（1024²），Python Pillow 批量裁掉底部水印、
  居中裁方后压缩为 360² JPG，存于 `swallowed-star-vue/public/avatars/*.jpg`
  （原始大图在 C:/WorkBuddyTmp/avatars_raw，处理脚本 C:/WorkBuddyTmp/make_avatars.py）
- DB：world_character 新增 `avatar VARCHAR(255)`；schema.sql 已含该列；
  老库迁移 + 22 人头像路径回填在 `config/DataInitializer.migrateCharacterAvatarColumn()`（幂等，
  注意 data.sql 阶段执行早于 CommandLineRunner，勿把依赖新列的 UPDATE 放进 data.sql）
- 前端：人物卡/天骄榜/详情弹层支持 `<img class="glyph img">`，无 avatar 时回退卡面图标；
  后台人物管理表格「图标」列改为头像缩略图（.adm-avatar），表单新增「头像图片路径」字段
- 验证：mvn 重启后 rank/list 接口返回 avatar；无头 Chrome 实测宽屏榜单在右、
  窄屏折叠在上、32 处头像全部加载、详情弹层头像正常；vite build 通过

## 14. 人物页布局微调（2026-11-02 第三次）
- 内容区上限 1120 → 1720（题头/筛选/主体同步加宽），天骄榜贴内容区最右
- 人物列表固定 3 列（原 auto-fill 最小 300 只能排 2 列）：≥1201px 3 列、
  1081–1200px 2 列、≤1080px 榜单折叠到上方后恢复 3 列、≤860px 2 列、≤640px 1 列
- 无头 Chrome 实测 1500/1920/1366/1000/700 五档宽度：均无横向滚动，
  1500/1366 榜单右缘距视口仅 28px（≈贴边），1920 因容器上限居中余 105px

## 15. 人物头像换版 · 动画风（2026-11-02 第四次）
- 用户反馈原水墨厚涂风不符合《完美世界》动画，全部 22 张按 3D CG 国漫动画画风
  （动画同款：石昊黑发高马尾金纹战袍、火灵儿红裙火凤、柳神绿衣柳枝等）重新生成，
  原图存 C:/WorkBuddyTmp/avatars_anime，处理脚本 make_avatars_anime.py（按文件名时间戳映射）
- 仍走 裁底部水印9% -> 居中裁方 -> 360px JPG 流程，原地覆盖 public/avatars/*.jpg，
  DB avatar 路径未变、无后端改动；无头浏览器复测 32 处图片全部加载、详情弹层正常
- 提示：浏览器可能缓存旧图，需 Ctrl+F5 强刷

## 16. 积分规则改造 · 后台点「完成」才加积分（2026-11-09）

> 本节取代第 9/10 节里「报名/接取即发积分」的旧描述。

### 新规则
- 接取任务 / 报名秘境后记录为「待完成」（PENDING），**不加分**；
- 后台「悬赏任务 / 秘境管理」的记录弹窗里点「完成」→ 记录变「已完成」（COMPLETED），
  并按任务/秘境**当前配置积分**发放；点「撤销完成」回到待完成并收回积分；
- 总积分 = 已完成任务积分 + 已完成秘境积分（此前秘境不发积分，本次给 secret_realm 新增 points）；
- 待完成记录用户可自行取消；已完成记录用户不可取消（报错提示找管理员）。

### 数据结构（老库迁移在 schema.sql 第 9 节，information_schema+PREPARE 幂等补列）
- ⚠ 迁移放在 schema.sql 而不是 DataInitializer：data.sql 的秘境种子已带 points 列，必须先补列后灌数据；
- quest_accept / realm_signup 新增 status(PENDING/COMPLETED)、completed_at、completed_by；realm_signup 另有 points 快照；
- secret_realm 新增 points；老库补列时按等级回填 仙帝1000/仙王600/真仙300/上古100；
- 存量归一：旧 quest_accept 全部置 COMPLETED（保住已发积分），旧 realm_signup 置 COMPLETED 且 points=0（历史从没发过）→ 升级后总积分不凭空增减。

### 后端
- 新接口（管理员 token）：POST /api/admin/quests/accepts/{id}/complete | /revert、
  POST /api/admin/realms/signups/{id}/complete | /revert（重复完成 409，积分按当前配置发放并回写记录）；
- 计分口径：QuestAcceptMapper/RealmSignupMapper 的 sumPoints 只统计 COMPLETED；
  GET /api/quests/points 现返回 points=任务+秘境总积分（附 questPoints/realmPoints，供各页导航积分徽章）；
- 实体新增非表字段：Quest.completed/completedCount、SecretRealm.completed/completedCount；
  ProfileVO 新增 questDoneCount/realmDoneCount、Activity.status；
- 接取/报名接口返回 pendingPoints（待发积分，不是到账）。

### 前端
- 后台：AdminRecordModal 重写（列 format / type:status|points 胶囊、完成/撤销完成按钮、busyId 防连点、
  顶部提示与「共 X 条 · 已完成 Y · 待完成 Z」汇总，弹窗加宽 lg）；QuestManage/RealmManage 记录弹窗接入，
  列表加完成数，秘境表单加「通关奖励积分」；Dashboard 加「待完成审核」卡片；
- 前台：Quests/Realms 接取/报名不再即时加分，卡片显示「⏳ 待后台确认」，已完成显示「✓ 已完成 · +N」且按钮禁用；
  统计条改「我的积分·已完成 / 待后台确认」；Profile 任务/秘境/动态列表显示状态与完成时间，仅已完成显示积分；
- 顺带修复旧 bug：Profile.vue 的 activities 未定义导致「修炼动态」页签恒空。

### 已实测（2026-11-09）
- mvn package 重启 8081：curl 全链路通过——接取→积分不变→后台完成→+300→重复完成 409→已完成取消被拒→
  撤销完成→积分回退；秘境报名→完成→总积分 4100→5100→撤销→4100；
- vite build + eslint 通过（仅 4 条历史 warning）；无头 Chrome(CDP) 实测 5 个页面与记录弹窗渲染，控制台零告警；
- 注意：本机沙箱环境变量 SERVER__PORT 会覆盖端口，手工起后端务必带 `--server.port=8081`。

## 17. 修炼境界 · 问道台（2026-11-16 新增，境界与积分彻底解耦）

首页导航现为六页互跳：「功法首页 / 人物介绍 / 秘境首页 / 任务接取 / 修炼境界 / 我的主页」；
点「修炼境界」进入 `/cultivation`（有 `ss_token` 守卫）。六项导航在 ≤1280px 时隐藏时钟并收窄菜单项（style.css）。

### 设计：境界怎么涨（不靠积分）
- 修为来源只有两个，全部与积分无关：
  1. **参悟典籍**：研读 gongfa 库典籍，按品阶得修为（绝世 200 / 绝顶 150 / 一流 110 / 上乘 80 / 中乘 55），
     **每部典籍一生只能参悟一次**（29 部上限，unique key username+gongfa_id）；
  2. **打坐吐纳**：30 秒冷却，每次 +（24 + 境界×6）修为，是可持续的日常积累。
- **境界不自动上涨**：本境修为积满（各境所需 80→1920 递增，仙帝为顶）后，
  必须本人点「冲击境界」**渡劫**；
- 渡劫成功率 = 60% + 已参悟功法数 × 2%（上限 92%），成功晋下一境（溢出修为结转）；
  失败不跌境界但修为折损 25%，可再蓄力重冲。
- 「我的主页」的境界进度条同步改为这套修为口径（ProfileVO.Tier 新增 exp/need/remain/top），
  积分再多境界也不会自动变，个人主页有「去问道台修行」入口。

### 数据结构（schema.sql 第 10 节，启动幂等建表，无需迁移）
- `user_cultivation`：username 唯一；realm_index（0=搬血…17=仙帝）、exp（本境修为）、
  total_exp（累计）、breakthrough_count、fail_count、last_meditate_at（吐纳冷却）；
- `gongfa_comprehend`：参悟记录，uk(username, gongfa_id) 保证一部典籍只参悟一次；
- `cultivation_log`：参悟/吐纳/突破/失败留痕（type：COMPREHEND/MEDITATE/BREAKTHROUGH/FAIL）。

### 后端（全部 /api/cultivation/*，均需登录；TokenStore 判空教训已吸取）
- `common/CultivationLadder`：十八境阶梯、need 阈值、修为收益、成功率公式（唯一事实源）；
- `entity/UserCultivation|GongfaComprehend|CultivationLog` + 三个 Mapper（纯 BaseMapper）；
- `service/CultivationService(+Impl)`、`controller/CultivationController`：
  - GET `/overview`（境界/修为/成功率/十八境 ladder/可参悟典籍列表一次带全）；
  - POST `/comprehend` {gongfaId}（防重复参悟）、POST `/meditate`（冷却校验）、
    POST `/breakthrough`（修为不足/最高境均友好报错）；
  - GET `/logs?limit=`（修炼记录）。
- 注意：DATETIME 秒级截断会让冷却多算 1 秒，cooldownLeft 已 clamp 到冷却总时长。

### 前端
- `src/api/cultivation.js`、`src/views/Cultivation.vue`（金色玄幻风：
  境界主盘（大字境界 + 修为进度条 + 渡劫按钮呼吸光效 + 成功率）、修炼两法卡、
  参悟典籍网格（品阶筛选 + 只看未参悟）、十八境阶梯（done/current/locked 三态）、修炼记录时间线；
  渡劫成功整卡金光闪烁 + toast）；
- 路由 `/cultivation`；Home/Characters/Realms/Quests/Profile 五页导航均插入「修炼境界」；
- Profile.vue 修炼境界区块文案改为修为口径（修为圆满时提示可渡劫）。

### 已实测（2026-11-16）
- mvn package 重启 8081：curl 全链路通过——参悟《原始真解》+200 → 重复参悟被拒 →
  渡劫成功晋「洞天境」（结余修为 120 结转）→ 吐纳 +30 → 冷却内再吐纳被拒 →
  logs/overview 正确；未登录 401；admin 积分 4100 但境界只到洞天（积分与境界解耦验证通过）；
- vite build + eslint 通过；无头 Chrome 实测 `/cultivation` 与 `/profile` 渲染正常
  （截图 C:/Users/Administrator/AppData/Local/Temp/cult.png）；
- 无头 Chrome + CDP 真实交互实测（node uitest.mjs，Runtime.evaluate 点击）：
  参悟《以身为种》修为 150→350、卡片转「已参悟」；吐纳 +30 且按钮进入「冷却 28s」；
  渡劫随机失败 → 修为 380→285（-95）并写入失败记录；品阶「绝世」5 张 → 勾选「只看未参悟」剩 3 张；
  修炼记录 6 条时间线正常；1024px 下 scrollWidth=innerWidth（无横向溢出）；控制台 JS 异常 0。

## 18. 后台管理 2.1 · 新增「修炼境界」模块（2026-11-16）

后台从六模块扩为七模块，侧边栏在「悬赏任务」与「修士账号」之间新增「修炼境界」。

### 后端（/api/admin/cultivations/*，均走 AdminGuard 管理员校验）
- `AdminCultivationController` + `controller/dto/AdminCultivationVO`：
  - GET `/list?keyword=`：全部账号 + 修炼档案聚合（未入门按「搬血 · 修为 0」展示，
    profiled=false）；字段含 realmIndex/realmName/exp/need/progress/totalExp/
    comprehendCount/breakthroughCount/failCount/lastMeditateAt/updatedAt；
  - PUT `/{username}` body {realmIndex, exp}：演示纠错用，校验 0-17 与 0-999999，
    调整后**回读档案**（DB ON UPDATE 刷新后的 updated_at 才准确），并写入
    cultivation_log（type=ADMIN ·「天道法则变动」）；
  - POST `/{username}/reset`：删除 user_cultivation + gongfa_comprehend + cultivation_log
    （境界归零，**积分不受影响**）。
- `GongfaComprehendMapper` 新增 countByUser（各账号参悟数）/ countAll（全站累计）。
- `AdminOverviewController` 概览新增：cultCount（已入门修士数）、totalComprehend、totalBreakthrough、
  totalFail、cultByRealm（十八境分布，仅列有人数的境界）。

### 前端
- `views/admin/CultivationManage.vue`（新页）：账号/昵称/当前境界胶囊/本境修为/参悟典籍/
  破境/渡劫失败/累计修为/最近变动 + 操作「调整境界（弹窗 select 十八境 + 数字修为）」「重置修炼（危险确认）」；
- `api/admin.js` 新增 cultivationApi；路由 `/admin/cultivations`；AdminLayout 菜单加项（图标 火苗）并标注 v2.1 · 七模块；
- `Dashboard.vue`：修士数据统计行改为 账号/已入门修士/累计参悟/成功破境/待审核/累计积分，
  新增整行「修士境界分布」面板（十八境 190px 多列紧凑条状图，空态提示去前台参悟）、
  快捷入口新增「修炼境界」；
- 前台 Cultivation.vue 修炼记录新增 ADMIN 类型图标（⚖️），后台调整可在修炼记录时间线看到。

### 已实测（2026-11-16）
- curl：list（admin 洞天/285/2 参悟）→ adjust 到尊者 exp100（progress 26.3）→ overview
  cultCount/totalComprehend/totalBreakthrough/cultByRealm 正确 → reset（profiled=false、境界归零）→
  adjust 回洞天 285（updatedAt 已刷新）→ 前台 logs 出现「天道法则变动」记录；
- 前端 eslint 0 error、vite build 通过；无头 Chrome 实测后台两页：侧边栏七项齐全、
  列表 1 行数据、概览新增统计与境界分布面板渲染正常
  （截图 outputs/后台-修炼境界管理.png、outputs/后台-概览境界分布.png）。

## 19. 商店兑换 · 大荒宝库（2026-11-16 新增，积分体系接入消耗）

导航增至七项：功法首页 / 人物介绍 / 秘境首页 / 任务接取 / **商店兑换** / 修炼境界 / 我的主页。

### 规则
- 用**积分**兑换四类商品：丹药 / 法宝 / 武器 / 高级功法（共 24 件种子商品，全部《完美世界》主题）；
- **积分口径升级为钱包制**：`可用积分 = 累计获得（任务+秘境已完成发放） - 商店兑换消耗`；
  shop_order 表即消耗账本，quest_accept / realm_signup 的发放逻辑不动；
- 「我的主页」「任务页」的积分徽章同步改为可用口径；成就徽章仍按**历史累计获得**判定（兑换不会熄灭成就）；
- 部分商品限量（如飞仙石 3 件、星辰剑 2 件、原始真解 5 件），stock=0 表示不限量；
  兑换用条件 UPDATE（`stock=0 OR sold<stock`）兜住并发抢购；
- 未登录浏览公开；兑换 / 钱包 / 记录需登录。

### 后端
- schema.sql 第 11 节：`shop_item`（商品）+ `shop_order`（兑换记录=消耗账本）；data.sql 第 7 节 24 件种子；
- 实体 `ShopItem`（owned/remain/soldOut/affordable 为查询时补充字段）、`ShopOrder`；
  Mapper `ShopItemMapper`（countByCategory / increaseSold 条件更新）、`ShopOrderMapper`（sumSpent 等）；
- `ShopService(+Impl)`：search（关键词/分类/排序 default=热门优先+价格升/priceAsc/priceDesc，
  填充 owned、soldOut、affordable）、categories、wallet（earned/spent/available/orderCount）、
  redeem（事务：积分校验→库存校验→条件扣库存→写 order）、myOrders、spentPoints；
- `ShopController`：`/api/shop/list|categories|wallet|redeem|orders`；
- 积分口径联动：`ProfileServiceImpl`（totalPoints=可用，新增 earnedPoints/spentPoints，徽章按 earned）、
  `QuestController /quests/points`（新增 earned/spent）。

### 前端
- `views/Shop.vue`（新页）：宝库题头+搜索+热门宝物快捷、积分钱包卡（可用/累计/已消耗/次数）、
  品类+排序筛选、商品网格（品阶徽章/分类色/限量余量/已兑换件数/买不起置灰「积分不足」）、
  兑换确认弹层（所需积分/可用余额/兑换后余额/库存）、我的兑换记录时间线；
- `api/shop.js`；路由 `/shop`（登录守卫）；六页导航插入「商店兑换」（任务接取之后）；
- 我的主页：积分卡改「可用积分 · 累计 X / 已兑 Y」，名帖加「宝库已兑」chip + 「去宝库兑换」入口；
- style.css：新增 ≤1180px 导航再收窄规则（七项防挤压）。

### 已实测（2026-11-16）
- curl 全链路：categories（24 件四类）→ list 排序 → 兑换乾坤袋 -200（wallet 3900）→ orders/owned=1 →
  profile（total 3900/earned 4100/spent 200，徽章数不变）→ quests/points 同口径 →
  未登录 401 → 新号积分不足报「还差 110 积分」→ 临时限量商品二次兑换报「已被兑完」（条件更新兜并发验证通过）→
  测试数据与临时账号已清理；
- 无头 Chrome + CDP 真实点击：分类筛选 24→6、价格排序首卡「淬体丹 60」、卡片兑换→弹层→确认兑换
  （toast「兑换成功 · 花费 60 积分」、钱包 3900→3840、兑换记录 +1、卡片转「已兑换 1 件」）、
  七页导航回归全 PASS、1440/1024 无横向溢出、JS 异常 0（favicon 404 为项目历史遗留）；
- eslint 0 error、vite build 通过；后端已重新打包并在 8081 运行。
