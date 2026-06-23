# SoulMate AI — 企业级 AI 伴侣桌面应用

原生 Java Swing 桌面应用 · 双模型驱动（DeepSeek API / Ollama）· 一键 EXE 分发

---

## 功能特性

- 双模型切换 — 支持 DeepSeek API（云端）和 Ollama（本地），随时切换
- 对话记忆 — 自动提取用户偏好（名字、年龄、爱好等），注入上下文
- 原生 Swing GUI — 无需浏览器，双击即聊，系统原生外观
- 零依赖分发 — 内置便携式 JRE，收件人无需安装 Java
- 滑动窗口 — 保留最近 20 轮对话，Token 不溢出

## 快速开始

### 方式一：直接下载 Release（推荐给最终用户）

1. 前往 [Releases](https://github.com/noxargenta/SoulMateAI-b/releases) 页面
2. 下载最新的 `SoulMateAI-发布包.zip`
3. 解压到任意文件夹
4. 双击 `SoulMateAI.exe`
5. 首次运行弹出配置窗口，选择 **DeepSeek API** 并填入 API Key

> 无需安装 Java，便携式 JRE 已内置在发布包中。

### 方式二：自行编译打包（开发者）

#### 前置条件
- JDK 11+（推荐 GraalVM 21+）
- Git

#### 克隆 & 打包

```bash
git clone https://github.com/noxargenta/SoulMateAI-b.git
cd SoulMateAI-b

# Windows 用户 — 双击 package-dist.bat
# 或命令行运行：
package-dist.bat

# Mac/Linux 用户：
bash package-dist.sh
```

构建完成后 `dist/SoulMateAI-发布包.zip` 即为可分发安装包。

## 项目架构

```
src/main/java/com/soulmate/
├── SoulMateApplication.java     # 入口（Swing 原生外观）
├── config/
│   └── AppConfig.java           # 配置管理（Properties 文件）
├── core/
│   └── ChatService.java         # 异步非阻塞聊天服务
├── llm/
│   ├── LlmClient.java           # 策略模式接口
│   └── impl/
│       ├── DeepSeekClient.java  # DeepSeek API 实现
│       └── OllamaClient.java    # 本地 Ollama 实现
├── memory/
│   ├── MemoryRepository.java    # JSON 结构化记忆仓库
│   └── MemoryExtractor.java     # 正则实体提取器
├── model/
│   └── Message.java             # 统一消息 DTO
└── ui/
    ├── ChatWindow.java          # 主聊天窗口
    └── ConfigDialog.java        # 配置对话框
```

## 技术栈

| 组件 | 选型 |
|------|------|
| 语言 | Java 11+ |
| UI 框架 | Swing（UIManager.setLookAndFeel） |
| JSON | Jackson 2.15.2 |
| 日志 | SLF4J + Logback |
| HTTP | java.net.http.HttpClient |
| 构建 | Maven 3.9.x |
| EXE 打包 | Launch4j 3.50 |
| 便携 JRE | jlink（裁剪至 ~90MB） |

## 配置项

配置文件 `config.properties` 在首次运行引导时自动生成：

| 键 | 说明 | 默认值 |
|----|------|--------|
| `llm.engine` | 模型引擎 | `ollama` / `deepseek` |
| `llm.deepseek.apikey` | DeepSeek API Key | `""` |
| `llm.ollama.url` | Ollama 地址 | `http://localhost:11434/api/chat` |
| `llm.ollama.model` | Ollama 模型名 | `qwen2:1.5b` |
| `partner.name` | 伴侣名字 | `小雅` |
| `partner.call_you` | 称呼 | `宝宝` |
| `partner.personality` | 性格描述 | `温柔体贴，喜欢撒娇` |

## 贡献

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feat/xxx`)
3. 提交改动 (`git commit -m "feat: xxx"`)
4. 推送到分支 (`git push origin feat/xxx`)
5. 发起 Pull Request

## License

MIT
