@echo off
REM ============================================================
REM 吞噬星空 项目一键启动脚本 (GBK 编码)
REM 适用场景: 本机每次重置后 node_modules / target 被清空, 需重新构建
REM 路径策略: 所有项目内路径基于脚本所在目录 %~dp0, 可随文件夹移动
REM 用法: 双击本文件, 或在 cmd 中运行 start.bat
REM ============================================================

SETLOCAL
SET "ROOT=%~dp0"
SET "BACKEND=%ROOT%springBootTest"
SET "FRONTEND=%ROOT%swallowed-star-vue"
SET "JAR=%BACKEND%\target\springBootTest-0.0.1-SNAPSHOT.jar"
SET "VITE_BIN=%FRONTEND%\node_modules\.bin\vite.cmd"

REM ---- 外部工具路径 (本机绝对路径, 这些不在项目内) ----
SET "JAVA=java"
SET "MYSQL=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

REM 定位 IDEA 自带 Maven (本机装在 D 盘, 不在 C:\Program Files)
SET "MAVEN="
IF EXIST "D:\IntelliJ IDEA 2024.3.2.2\plugins\maven\lib\maven3\bin\mvn.cmd" (
  SET "MAVEN=D:\IntelliJ IDEA 2024.3.2.2\plugins\maven\lib\maven3\bin\mvn.cmd"
)
IF NOT DEFINED MAVEN (
  FOR /F "delims=" %%d IN ('dir /b /ad "C:\Program Files\JetBrains\IntelliJ IDEA *" 2^>nul') DO (
    IF NOT DEFINED MAVEN IF EXIST "C:\Program Files\JetBrains\%%d\plugins\maven\lib\maven3\bin\mvn.cmd" SET "MAVEN=C:\Program Files\JetBrains\%%d\plugins\maven\lib\maven3\bin\mvn.cmd"
  )
)
IF NOT DEFINED MAVEN (
  FOR /F "delims=" %%d IN ('dir /b /ad "D:\IntelliJ IDEA *" 2^>nul') DO (
    IF NOT DEFINED MAVEN IF EXIST "D:\%%d\plugins\maven\lib\maven3\bin\mvn.cmd" SET "MAVEN=D:\%%d\plugins\maven\lib\maven3\bin\mvn.cmd"
  )
)
IF NOT DEFINED MAVEN SET "MAVEN=%BACKEND%\mvnw.cmd"
echo 使用 Maven: %MAVEN%

echo.
echo [0/5] 检查项目目录 ...
IF NOT EXIST "%BACKEND%\pom.xml" (
  echo [错误] 未找到后端目录: %BACKEND%
  echo        请确认 start.bat 与 springBootTest / swallowed-star-vue 在同一层目录
  pause
  goto :eof
)
IF NOT EXIST "%FRONTEND%\package.json" (
  echo [错误] 未找到前端目录: %FRONTEND%
  echo        请确认 start.bat 与 springBootTest / swallowed-star-vue 在同一层目录
  pause
  goto :eof
)
echo        后端: %BACKEND%
echo        前端: %FRONTEND%

echo [1/5] 启动 MySQL 服务 (MySQL80) ...
net start MySQL80 >nul 2>&1
echo        如已运行会提示, 可忽略

echo [2/5] 初始化数据库 (建库/建表, 可重复执行) ...
IF NOT EXIST "%MYSQL%" (
  echo [警告] 未找到 MySQL 客户端: %MYSQL%
) ELSE (
  "%MYSQL%" -uroot -p123456 < "%BACKEND%\star_user.sql"
  IF ERRORLEVEL 1 (
    echo [警告] 数据库初始化失败, 请确认 MySQL 已启动且密码为 123456
  ) ELSE (
    echo        数据库就绪
  )
)

echo [3/5] 用 Maven 重新打包后端 (target 已清空, 必须重建) ...
pushd "%BACKEND%"
call "%MAVEN%" -DskipTests package
IF ERRORLEVEL 1 (
  echo [错误] 后端构建失败, 请检查 Maven 与网络
  popd
  pause
  goto :eof
)
popd

echo [4/5] 启动后端  http://127.0.0.1:8081 ...
IF NOT EXIST "%JAR%" (
  echo [错误] 未找到构建产物: %JAR%
  pause
  goto :eof
)
REM 用 start 的 /d 参数指定工作目录, 避免嵌套引号导致的路径解析错误
start "swallowed-star-backend" /d "%BACKEND%" cmd /k "java -jar target\springBootTest-0.0.1-SNAPSHOT.jar --server.port=8081 --spring.profiles.active=dev"

echo        等待后端就绪 (最多 60 秒) ...
SET /A WAITN=0
:wait_backend
SET /A WAITN+=1
netstat -ano | findstr ":8081" | findstr "LISTENING" >nul 2>&1
IF NOT ERRORLEVEL 1 goto backend_ready
IF %WAITN% GEQ 30 goto backend_ready
ping -n 2 127.0.0.1 >nul
goto wait_backend
:backend_ready

echo [5/5] 检查前端依赖并启动  http://localhost:5173 ...
REM 注意: 只判断 node_modules 目录存在是不够的, 半残目录里 .bin\vite.cmd 会缺失
REM 注意: 块内的 echo 不能出现半角括号, 否则会被当成语句块结束
IF NOT EXIST "%VITE_BIN%" (
  echo        node_modules 不完整或缺失, 正在安装前端依赖, 约需 1-2 分钟 ...
  pushd "%FRONTEND%"
  IF EXIST "package-lock.json" (
    call npm ci --no-audit --no-fund
  ) ELSE (
    call npm install --no-audit --no-fund
  )
  IF ERRORLEVEL 1 (
    echo        npm ci 失败, 改用 npm install 重试 ...
    call npm install --no-audit --no-fund
  )
  popd
)
IF NOT EXIST "%VITE_BIN%" (
  echo [错误] 前端依赖安装失败, 请在 swallowed-star-vue 目录手动执行: npm install
  pause
  goto :eof
)
start "swallowed-star-frontend" /d "%FRONTEND%" cmd /k "npm run dev"

echo.
echo ===================================================
echo  启动完成!
echo  前端: http://localhost:5173  (/api 自动代理到 8081)
echo  后端: http://127.0.0.1:8081/api
echo  演示账号: admin / 123456  (首次启动自动创建)
echo  停止项目请运行 pause.bat
echo ===================================================
ENDLOCAL
