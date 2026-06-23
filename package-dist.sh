#!/bin/bash
set -e
cd "$(dirname "$0")"

[ -z "$JAVA_HOME" ] && { echo "请设置 JAVA_HOME 环境变量"; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "未找到 mvn 命令，请确认 Maven 已安装"; exit 1; }

echo "[1/3] 编译项目..."
mvn package -DskipTests

echo "[2/3] 生成便携式 JRE..."
rm -rf jre
"$JAVA_HOME/bin/jlink" \
    --module-path "$JAVA_HOME/jmods" \
    --add-modules java.base,java.desktop,java.net.http,java.logging,java.xml,jdk.crypto.ec,jdk.unsupported,java.naming,java.management \
    --output jre \
    --strip-debug --compress zip-6 --no-header-files --no-man-pages

echo "[3/3] 组装发布包..."
rm -rf dist
mkdir -p dist/SoulMateAI
cp target/SoulMateAI.exe dist/SoulMateAI/
mv jre dist/SoulMateAI/jre

cd dist
if command -v zip &>/dev/null; then
    zip -r SoulMateAI-发布包.zip SoulMateAI/
else
    powershell -Command "Compress-Archive -Path 'SoulMateAI/*' -DestinationPath 'SoulMateAI-发布包.zip' -Force"
fi
cd ..

echo ""
echo "完成: $(pwd)/dist/SoulMateAI-发布包.zip"
