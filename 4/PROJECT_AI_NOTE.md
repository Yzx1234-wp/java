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
