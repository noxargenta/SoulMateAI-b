#!/bin/bash
# SoulMateAI 一键发布包生成（bash版，在 git bash 中运行）
set -e

cd "$(dirname "$0")"
DIST="$(pwd)/dist"
JDK_PATH="${JAVA_HOME:-/c/Program Files/Java/graalvm-jdk-24.0.2+11.1}"

echo "============================================="
echo "  SoulMateAI 发布包生成器"
echo "============================================="

# ---- Step 1: 准备 Maven ----
echo "[1/5] 准备构建工具..."
MVN_CMD="$(pwd)/maven/apache-maven-3.9.16/bin/mvn"
if [ ! -f "$MVN_CMD" ]; then
    if [ -f maven.zip ]; then
        unzip -q maven.zip -d maven
    else
        curl -sL "https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip" -o maven.zip
        unzip -q maven.zip -d maven
    fi
    MVN_CMD="$(pwd)/maven/apache-maven-3.9.16/bin/mvn"
fi

# ---- Step 2: jlink 生成 JRE 到项目根目录 ----
echo "[2/5] 生成便携式 JRE..."
rm -rf jre  # 清理旧的
"$JDK_PATH/bin/jlink" \
    --module-path "$JDK_PATH/jmods" \
    --add-modules java.base,java.desktop,java.net.http,java.logging,java.xml,jdk.crypto.ec,jdk.unsupported,java.naming,java.management \
    --output jre \
    --strip-debug --compress zip-6 --no-header-files --no-man-pages
echo "  ✅ JRE 生成完成（$(du -sh jre | cut -f1)）"

# ---- Step 3: 编译 + 打包 exe（此时 jre/ 已存在，launch4j 验证通过） ----
echo "[3/5] 编译并打包 exe..."
rm -rf target  # 手动清理，避免 mvn clean 的文件锁问题
"$MVN_CMD" package -DskipTests 2>&1 | grep -E "BUILD|Successfully|ERROR"
echo "  ✅ 编译打包完成"

# ---- Step 4: 组装发布目录 ----
echo "[4/5] 组装发布包..."
rm -rf "$DIST"
mkdir -p "$DIST/SoulMateAI"

# 复制 exe 和便携 JRE
cp "target/SoulMateAI.exe" "$DIST/SoulMateAI/"
mv jre "$DIST/SoulMateAI/jre"

# 创建使用说明
cat > "$DIST/SoulMateAI/使用说明.txt" << 'README'
SoulMate AI v1.0

使用方法：
1. 双击 SoulMateAI.exe 启动程序
2. 首次运行会弹出配置窗口，选择 DeepSeek API 并输入 API Key
3. 保存配置后即可开始聊天

注意：需要联网使用 DeepSeek API；Ollama 需本地运行
README

# ---- Step 5: 压缩 zip ----
echo "[5/5] 压缩发布包..."
cd "$DIST"
# 使用 PowerShell 的 Compress-Archive（Windows 自带）或 7z 备选
if command -v zip &>/dev/null; then
    zip -r "SoulMateAI-发布包.zip" "SoulMateAI/" > /dev/null
elif command -v 7z &>/dev/null; then
    7z a -tzip "SoulMateAI-发布包.zip" "SoulMateAI/" > /dev/null
else
    powershell -Command "Compress-Archive -Path 'SoulMateAI/*' -DestinationPath 'SoulMateAI-发布包.zip' -Force" > /dev/null 2>&1
fi
cd "$(dirname "$0")"

# 删除项目根目录的 jre（已移到 dist）
rm -rf jre

echo ""
echo "============================================="
echo "  ✅ 全部完成！"
echo ""
SIZE=$(ls -lh "$DIST/SoulMateAI-发布包.zip" | awk '{print $5}')
echo "  发布包: dist/SoulMateAI-发布包.zip"
echo "  大小: $SIZE"
echo ""
echo "  将这个 zip 发给任何人，解压后直接双击"
echo "  SoulMateAI.exe 即可使用，无需安装 Java！"
echo "============================================="
