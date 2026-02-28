# AI 小说生成器 (ai-novel-generator)

基于 **RAG（检索增强生成）** 的 AI 创作平台，支持创建小说项目、管理章节、上传参考资料，并借助大语言模型与向量检索辅助生成内容。

## ✨ 核心优势

### 1. **RAG 增强创作**
- 智能向量检索：章节生成时自动从向量数据库检索最相关的 top3 片段，确保风格和设定的一致性
- 资料库关联：支持上传参考资料并关联到小说，AI 生成时自动参考相关内容
- 语义理解：基于向量相似度匹配，而非简单的关键词搜索

### 2. **实时流式生成**
- WebSocket 流式输出：小说结构、章节大纲、章节内容均支持实时流式生成，无需等待
- 可中断生成：支持在生成过程中随时停止，灵活控制生成流程
- 富文本展示：Markdown 渲染，美观展示生成内容

### 3. **灵活的模型配置**
- 多模型支持：支持 OpenAI、DeepSeek 等多种大语言模型
- 分离配置：对话模型和嵌入模型独立配置，可分别使用不同服务
- 用户级配置：每个用户可配置自己的 API Key 和模型参数

### 4. **完善的创作流程**
- 结构化生成：从小说架构 → 章节大纲 → 章节内容的完整创作流程
- 自动同步：章节大纲生成后自动提取章节标题并创建章节记录
- 智能提示：基于章节标题、摘要和世界观设定构建查询，精准检索相关片段

## 🎯 已实现功能

### 核心功能

#### 1. **小说项目管理**
- ✅ 创建、编辑、删除小说
- ✅ 支持设置总章数和章节字数
- ✅ 小说状态管理（草稿 / 发布中 / 已完成）
- ✅ 小说导出（TXT / Markdown / Word）

#### 2. **流式生成系统**
- ✅ **小说结构生成**：基于标题、题材、世界观设定生成完整的小说架构
- ✅ **章节大纲生成**：根据小说结构生成详细的章节大纲，支持继续生成（带已有大纲内容，完成后更新小说表）
- ✅ **章节内容生成**：基于章节标题、摘要和前文生成完整章节内容；支持弹框确认后再生成、一键按章节顺序依次生成、支持停止
- ✅ 所有生成均支持 WebSocket 实时流式输出
- ✅ 生成过程中可随时停止

#### 3. **RAG 检索增强**
- ✅ 章节生成时自动查询向量数据库，获取 top3 最相关片段
- ✅ 查询文本智能构建：章节标题 + 章节摘要 + 世界观设定
- ✅ 检索结果自动融入 AI 提示词，指导生成过程
- ✅ 支持从已生成章节和资料库中检索相关内容

#### 4. **资料库管理**
- ✅ 上传文本资料并自动向量化
- ✅ 资料库列表管理
- ✅ 小说可关联多个资料库
- ✅ 资料库内容在章节生成时自动参与 RAG 检索

#### 5. **用户与认证**
- ✅ 邮箱验证码登录
- ✅ 未注册邮箱自动创建账号
- ✅ 用户积分（score）与积分记录（score_log）
- ✅ 用户级 API 配置管理
- ✅ 登录校验与资源越权防护（全局切面：`@RequireLogin`、`@CheckNovelOwner`、`@CheckChapterOwner`、`@CheckNovelVectorOwner` 等）

#### 6. **API 设置**
- ✅ 支持多种模型渠道（OpenAI、DeepSeek、自定义）
- ✅ 对话模型和嵌入模型独立配置
- ✅ 用户级配置持久化
- ✅ 配置验证和错误提示

#### 7. **任务管理**
- ✅ 任务进度实时显示
- ✅ 任务状态跟踪（待处理 / 处理中 / 完成 / 失败）
- ✅ 任务列表查询

#### 8. **章节管理**
- ✅ 章节列表展示
- ✅ 章节详情查看（富文本渲染）
- ✅ 章节详情导出 TXT
- ✅ 章节自动同步：从大纲提取章节标题并创建占位章节
- ✅ 章节状态管理（待处理 / 生成中 / 已完成）

#### 9. **界面优化**
- ✅ Markdown 富文本渲染
- ✅ 响应式设计
- ✅ 暗色主题支持
- ✅ 实时进度提示

### 技术特性

- ✅ **WebSocket (STOMP)**：实时双向通信
- ✅ **策略模式**：流式生成逻辑抽象，易于扩展
- ✅ **全局切面权限**：登录校验与资源归属校验，防止越权访问
- ✅ **事务管理**：确保数据一致性
- ✅ **异常处理**：完善的错误处理和日志记录
- ✅ **代码规范**：清晰的架构分层和代码组织

## 🚀 未来规划

### 1. **资料库关联设计优化**
- 📋 支持更多资料格式：PDF、Word、Markdown 文件上传
- 📋 资料库分类和标签管理
- 📋 资料库内容预览和编辑
- 📋 智能资料推荐：根据小说题材自动推荐相关资料
- 📋 资料库版本管理：支持更新和回滚

### 2. **任务异步化完善**
- 📋 RocketMQ 集成优化：完善异步任务队列管理
- 📋 任务优先级调度：支持设置任务优先级
- 📋 任务重试机制：失败任务自动重试
- 📋 任务批量处理：支持批量生成章节
- 📋 任务进度详情：显示更详细的生成进度信息

### 3. **章节扩写功能**
- 📋 智能扩写：基于现有章节内容，AI 自动扩写增加细节
- 📋 指定段落扩写：选择特定段落进行扩写
- 📋 扩写风格控制：支持设置扩写风格（详细描述 / 对话增强 / 环境描写等）
- 📋 扩写预览：扩写前预览效果，支持撤销

### 4. **去 AI 味功能**
- 📋 风格调整：自动调整生成文本，减少 AI 痕迹
- 📋 语言润色：优化表达方式，使其更自然流畅
- 📋 个性化写作：学习用户偏好，生成更符合个人风格的内容
- 📋 多轮优化：支持对生成内容进行多轮优化迭代
- 📋 风格模板：提供多种写作风格模板供选择

### 5. **其他规划**
- 📋 协作功能：多人协作创作
- 📋 版本控制：章节内容版本管理和对比
- 📋 统计分析：生成字数、耗时等数据统计
- 📋 模板系统：小说模板和章节模板
- 📋 移动端适配：响应式优化，支持移动端访问

## 🛠 技术栈

| 层级     | 技术 |
|----------|------|
| 后端     | Java 17、Spring Boot 3.2、Spring Data JPA、LangChain4j、Qdrant、MySQL、RocketMQ、WebSocket (STOMP) |
| 前端     | Vue 3、Vue Router、Vite 5、Axios、Markdown-it、@stomp/stompjs |
| AI/向量  | OpenAI API（Chat + Embedding）、DeepSeek、Qdrant 向量库 |
| 架构模式 | DDD（领域驱动设计）、策略模式、RAG（检索增强生成） |

## 📁 项目结构

```
ai-noval-generator/
├── docs/                   # 文档与脚本
│   └── create_tables.sql   # MySQL 建表脚本（含表注释、索引、唯一键）
├── src/                    # 后端 (Spring Boot)
│   └── main/java/com/viking/ai/novel/
│       ├── application/    # 应用服务层
│       ├── domain/         # 领域模型与仓储接口
│       ├── infrastructure/ # 基础设施层（配置、AI 服务、JPA/Qdrant 实现）
│       └── interfaces/     # 接口层（Controller、DTO、Mapper、AOP 权限切面）
├── web/                    # 前端 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/            # 接口封装
│   │   ├── components/     # 公共组件
│   │   ├── router/         # 路由
│   │   ├── utils/          # 工具函数（WebSocket、Markdown 等）
│   │   └── views/          # 页面视图
│   └── package.json
├── pom.xml
├── docker-compose.yml      # Docker 一键启动
└── README.md
```

## ⚙️ 环境要求

- **JDK 17**
- **Node.js** 18+（用于前端）
- **MySQL** 8.x
- **Qdrant**（向量数据库，默认端口 6333）
- **RocketMQ**（可选，用于 AI 异步生成）
- **大语言模型 API Key**（OpenAI、DeepSeek 等）

## 🚀 快速开始

### 1. 数据库与 Qdrant

- 创建 MySQL 数据库，例如：`ai_novel`
- 执行建表脚本：`docs/create_tables.sql`（含 users、novel、chapters、novel_vectors、score_log 等表及索引、注释）
- 启动 Qdrant（Docker 示例）：
  ```bash
  docker run -p 6333:6333 qdrant/qdrant
  ```

### 2. 后端配置与启动

复制或修改 `src/main/resources/application.yml` 中的配置：

- `spring.datasource`：MySQL 地址、库名、用户名、密码
- `qdrant`：Qdrant 的 host、port、collection-name
- `langchain4j.open-ai.*`：OpenAI API Key、Base URL、模型名等

通过环境变量设置 API Key（推荐）：

```bash
export OPENAI_API_KEY=sk-xxx
```

启动后端：

```bash
mvn spring-boot:run
```

服务默认运行在 **http://localhost:8080**。

### 3. 前端安装与运行

```bash
cd web
npm install
npm run dev
```

前端默认运行在 **http://localhost:5173**，并通过 Vite 代理将 `/api` 转发到 `http://localhost:8080`。

### 4. 访问应用

浏览器打开 http://localhost:5173，即可使用「创建新项目」、小说详情、章节管理、资料库等功能。

## 📝 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 后端端口 | 8080 |
| `spring.datasource.url` | MySQL 连接串 | localhost:3306/ai_novel |
| `qdrant.host` / `qdrant.port` | Qdrant 地址 | localhost:6333 |
| `langchain4j.open-ai.chat-model.api-key` | OpenAI API Key | 可由 `OPENAI_API_KEY` 覆盖 |
| `langchain4j.open-ai.chat-model.base-url` | API 基础 URL | https://api.openai.com/v1 |
| `langchain4j.open-ai.chat-model.model-name` | 对话模型 | gpt-4 |
| `langchain4j.embedding-model.open-ai.model-name` | 嵌入模型 | text-embedding-3-small |
| `spring.mail.host` / `spring.mail.username` 等 | 邮件服务（可选，用于发送登录验证码） | 不配置时验证码仅打印到控制台 |
| `rocketmq.name-server` | RocketMQ 地址（可选，用于 AI 异步生成） | 不配置时仅支持实时生成 |
| `rocketmq.enabled` | 是否启用 RocketMQ | false |

前端开发环境下 API 代理在 `web/vite.config.js` 中配置（`/api` → `http://localhost:8080`）。

## 🐳 Docker 启动

使用 Docker Compose 一键启动全部服务（MySQL、Qdrant、后端、前端）。

**前置**：安装 [Docker](https://docs.docker.com/get-docker/) 与 [Docker Compose](https://docs.docker.com/compose/install/)。

**启动**（在项目根目录执行）：

```bash
# 可选：设置 MySQL 密码与 OpenAI API Key（否则使用默认或占位值）
export MYSQL_PASSWORD=your_mysql_password
export OPENAI_API_KEY=sk-xxx

docker compose up -d --build
```

- **前端**：http://localhost （80 端口，Nginx 托管并代理 `/api` 到后端）
- **后端**：http://localhost:8080
- **MySQL**：localhost:3306（库名 `ai_novel`，默认用户/密码见 `docker-compose.yml`）
- **Qdrant**：http://localhost:6333

**说明**：

- 首次启动会构建后端与前端镜像，并等待 MySQL、Qdrant 健康后再启动后端。
- 后端使用 `application-docker.yml` 配置（数据源指向 `mysql`、Qdrant 指向 `qdrant`）。
- 数据持久化：`mysql_data`、`qdrant_data` 两个 volume。

**停止**：

```bash
docker compose down
```

仅停止并删除容器，保留数据卷。需同时删除数据卷时使用：`docker compose down -v`。

## 📚 常用命令

- **后端**：`mvn spring-boot:run`（开发）、`mvn clean package`（打包）
- **前端**：`npm run dev`（开发）、`npm run build`（构建）、`npm run preview`（预览构建结果）

## 📄 许可证

本项目采用 MIT 许可证。