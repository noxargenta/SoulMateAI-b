@echo off
chcp 65001 >nul
title SoulMateAI 一键发布包生成工具
echo =============================================
echo   SoulMateAI 一键发布包生成
echo =============================================
echo.

REM 切换到脚本所在目录（处理含空格的路径）
pushd "%~dp0"

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
    popd
    exit /b 1
)
echo [1/5] 使用 JDK: %JDK_PATH%

REM ------ Step 1: 准备 Maven ------
echo [2/5] 准备构建工具...
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "maven\apache-maven-3.9.16\bin\mvn.cmd" (
        set MVN_CMD=maven\apache-maven-3.9.16\bin\mvn.cmd
    ) else (
        echo   正在下载 Maven...
        curl -sL "https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip" -o "%TEMP%\maven.zip"
        powershell -Command "Expand-Archive '%TEMP%\maven.zip' -DestinationPath 'maven'" >nul
        if not exist "maven\apache-maven-3.9.16\bin\mvn.cmd" (
            echo [错误] Maven 下载或解压失败！
            pause
            popd
            exit /b 1
        )
        set MVN_CMD=maven\apache-maven-3.9.16\bin\mvn.cmd
    )
) else (
    set MVN_CMD=mvn
)
echo   构建工具就绪

REM ------ Step 2: 用 jlink 创建便携 JRE ------
echo [3/5] 正在生成便携式 JRE（约 90MB，第一次会比较慢）...
if exist "jre" rmdir /s /q "jre"
"%JDK_PATH%\bin\jlink" ^
    --module-path "%JDK_PATH%\jmods" ^
    --add-modules java.base,java.desktop,java.net.http,java.logging,java.xml,jdk.crypto.ec,jdk.unsupported,java.naming,java.management ^
    --output "jre" ^
    --strip-debug --compress zip-6 --no-header-files --no-man-pages
if %ERRORLEVEL% neq 0 (
    echo.
    echo [错误] jlink 执行失败！
    echo   可能原因：JDK 版本过旧或不支持 jlink
    echo   请使用 JDK 11 及以上版本
    pause
    popd
    exit /b 1
)
echo   ✅ JRE 生成完成

REM ------ Step 3: 编译 + 打包 exe ------
echo [4/5] 正在编译项目并打包 exe...
if exist "target" rmdir /s /q "target"
call "%MVN_CMD%" package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo.
    echo [错误] 编译打包失败！请检查上方错误信息。
    pause
    popd
    exit /b 1
)
if not exist "target\SoulMateAI.exe" (
    echo [错误] target\SoulMateAI.exe 未生成，launch4j 可能执行失败。
    pause
    popd
    exit /b 1
)
echo   ✅ exe 打包成功

REM ------ Step 4: 组装发布目录 ------
echo [5/5] 正在组装发布包...
if exist "dist" rmdir /s /q "dist"
mkdir "dist\SoulMateAI"

copy "target\SoulMateAI.exe" "dist\SoulMateAI\" >nul
move "jre" "dist\SoulMateAI\jre" >nul

REM 使用说明
echo SoulMate AI v1.0 > "dist\SoulMateAI\使用说明.txt"
echo. >> "dist\SoulMateAI\使用说明.txt"
echo 使用方法： >> "dist\SoulMateAI\使用说明.txt"
echo 1. 双击 SoulMateAI.exe 启动程序 >> "dist\SoulMateAI\使用说明.txt"
echo 2. 首次运行会弹出配置窗口，选择 DeepSeek API 并输入 API Key >> "dist\SoulMateAI\使用说明.txt"
echo 3. 保存配置后即可开始聊天 >> "dist\SoulMateAI\使用说明.txt"
echo. >> "dist\SoulMateAI\使用说明.txt"
echo 注意：需要联网使用 DeepSeek API；Ollama 需本地运行 >> "dist\SoulMateAI\使用说明.txt"

REM 压缩成 zip
powershell -Command "Compress-Archive -Path '.\dist\SoulMateAI\*' -DestinationPath '.\dist\SoulMateAI-发布包.zip' -Force"
if %ERRORLEVEL% neq 0 (
    echo [警告] PowerShell 压缩失败，但解压目录已生成可直接使用。
    echo   解压目录位置：%cd%\dist\SoulMateAI\
    pause
    popd
    exit /b 0
)

REM 显示最终结果
echo.
echo =============================================
echo   ✅ 全部完成！
echo.
dir "dist\SoulMateAI-发布包.zip" | find "SoulMateAI"
echo.
echo   把这个文件发给任何人：
echo   %cd%\dist\SoulMateAI-发布包.zip
echo.
echo   对方解压后双击 SoulMateAI.exe 即可使用！
echo =============================================
pause
popd
