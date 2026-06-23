@echo off
chcp 65001 >nul
title SoulMateAI 发布包生成

if "%JAVA_HOME%"=="" (
    echo 请设置 JAVA_HOME 环境变量后重试。
    pause
    exit /b 1
)

where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo 未找到 mvn 命令，请确认 Maven 已安装并加入 PATH。
    pause
    exit /b 1
)

echo [1/3] 编译项目...
call mvn package -DskipTests
if %ERRORLEVEL% neq 0 (
    pause
    exit /b 1
)

echo [2/3] 生成便携式 JRE...
if exist jre rmdir /s /q jre
"%JAVA_HOME%\bin\jlink" ^
    --module-path "%JAVA_HOME%\jmods" ^
    --add-modules java.base,java.desktop,java.net.http,java.logging,java.xml,jdk.crypto.ec,jdk.unsupported,java.naming,java.management ^
    --output jre ^
    --strip-debug --compress zip-6 --no-header-files --no-man-pages

echo [3/3] 组装发布包...
if exist dist rmdir /s /q dist
mkdir dist\SoulMateAI
copy target\SoulMateAI.exe dist\SoulMateAI\ >nul
move jre dist\SoulMateAI\jre >nul
powershell -Command "Compress-Archive -Path 'dist\SoulMateAI\*' -DestinationPath 'dist\SoulMateAI-发布包.zip' -Force"

echo.
echo 完成: %cd%\dist\SoulMateAI-发布包.zip
pause
