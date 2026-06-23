@echo off
chcp 65001 >nul
title SoulMateAI 一键发布包生成工具
echo =============================================
echo   SoulMateAI 一键发布包生成
echo =============================================
echo.

REM ------ 自动检测 JDK ------
if not "%JAVA_HOME%"=="" (
    set JDK_PATH=%JAVA_HOME%
) else if exist "C:\Program Files\Java\graalvm-jdk-24.0.2+11.1" (
    set JDK_PATH=C:\Program Files\Java\graalvm-jdk-24.0.2+11.1
) else if exist "C:\Program Files\Java\jdk-24" (
    set JDK_PATH=C:\Program Files\Java\jdk-24
) else if exist "C:\Program Files\Eclipse Adoptium\jdk-11.0.22.7-hotspot" (
    set JDK_PATH=C:\Program Files\Eclipse Adoptium\jdk-11.0.22.7-hotspot
) else (
    echo [错误] 未找到 JDK。请设置 JAVA_HOME 环境变量后重试。
    pause
    exit /b 1
)
echo [1/5] 使用 JDK: %JDK_PATH%

REM ------ Step 1: 准备 Maven ------
echo [2/5] 准备构建工具...
set MVN_CMD=mvn
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "%~dp0maven\apache-maven-3.9.16\bin\mvn.cmd" (
        set MVN_CMD=%~dp0maven\apache-maven-3.9.16\bin\mvn.cmd
    ) else (
        echo   正在下载 Maven...
        curl -sL "https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip" -o "%TEMP%\maven.zip"
        powershell -Command "Expand-Archive '%TEMP%\maven.zip' -DestinationPath '%~dp0maven'" >nul
        set MVN_CMD=%~dp0maven\apache-maven-3.9.16\bin\mvn.cmd
    )
)

REM ------ Step 2: 用 jlink 创建便携 JRE ------
echo [3/5] 生成便携式 JRE...
if exist "%~dp0jre" rmdir /s /q "%~dp0jre"
"%JDK_PATH%\bin\jlink" ^
    --module-path "%JDK_PATH%\jmods" ^
    --add-modules java.base,java.desktop,java.net.http,java.logging,java.xml,jdk.crypto.ec,jdk.unsupported,java.naming,java.management ^
    --output "%~dp0jre" ^
    --strip-debug --compress zip-6 --no-header-files --no-man-pages
if %ERRORLEVEL% neq 0 (
    echo [错误] jlink 失败！请确认你的 JDK 支持 jlink。
    pause
    exit /b 1
)
echo   ✅ JRE 生成完成

REM ------ Step 3: 编译 + 打包 exe（现在 jre/ 已存在，launch4j 验证通过） ------
echo [4/5] 编译并打包 exe...
if exist "%~dp0target" rmdir /s /q "%~dp0target"
"%MVN_CMD%" package -DskipTests >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [错误] 编译打包失败！
    pause
    exit /b 1
)
echo   ✅ exe 打包成功

REM ------ Step 4: 组装发布目录 ------
echo [5/5] 组装发布包...
if exist "%~dp0dist" rmdir /s /q "%~dp0dist"
mkdir "%~dp0dist\SoulMateAI"

copy "%~dp0target\SoulMateAI.exe" "%~dp0dist\SoulMateAI\" >nul
move "%~dp0jre" "%~dp0dist\SoulMateAI\jre" >nul

REM 使用说明
echo SoulMate AI v1.0 > "%~dp0dist\SoulMateAI\使用说明.txt"
echo. >> "%~dp0dist\SoulMateAI\使用说明.txt"
echo 使用方法： >> "%~dp0dist\SoulMateAI\使用说明.txt"
echo 1. 双击 SoulMateAI.exe 启动程序 >> "%~dp0dist\SoulMateAI\使用说明.txt"
echo 2. 首次运行会弹出配置窗口，选择 DeepSeek API 并输入 API Key >> "%~dp0dist\SoulMateAI\使用说明.txt"
echo 3. 保存配置后即可开始聊天 >> "%~dp0dist\SoulMateAI\使用说明.txt"
echo. >> "%~dp0dist\SoulMateAI\使用说明.txt"
echo 注意：需要联网使用 DeepSeek API；Ollama 需本地运行 >> "%~dp0dist\SoulMateAI\使用说明.txt"

REM 压缩成 zip
powershell -Command "Compress-Archive -Path '%~dp0dist\SoulMateAI\*' -DestinationPath '%~dp0dist\SoulMateAI-发布包.zip' -Force" >nul 2>&1

echo.
echo =============================================
echo   ✅ 全部完成！
echo.
echo   发布包位置:
echo   %~dp0dist\SoulMateAI-发布包.zip
echo.
echo   将这个 zip 发给任何人，解压后双击
echo   SoulMateAI.exe 即可使用！
echo =============================================
pause
