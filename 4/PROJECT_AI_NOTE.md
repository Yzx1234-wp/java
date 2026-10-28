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
  - 代理：`vite.config.js` 把 `/api` 代理到 `http://localhost:8081`
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
- 基址：`http://127.0.0.1:8081/api`
- GET `/api/auth/me`（带 `Authorization: Bearer <token>`）
- POST `/api/auth/login` `{username,password}` → token
- POST `/api/auth/register` `{username,password}`
- POST `/api/auth/logout`

## 7. 后台管理系统（2026-10-26 新增）
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
