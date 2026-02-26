# AI 小说生成器 (ai-novel-generator)

基于 **RAG（检索增强生成）** 的 AI 创作平台，支持创建小说项目、管理章节、上传参考资料，并借助大语言模型与向量检索辅助生成内容。

## 功能特性

- **小说项目管理**：创建、编辑、查看小说，支持类型与状态（草稿 / 发布中 / 已完成）
- **章节与大纲**：管理小说章节，结合 AI 生成章节大纲与内容
- **资料库**：上传并管理参考资料（用户向量），供 RAG 检索使用
- **向量检索**：集成 Qdrant 存储章节与用户资料向量，支持语义检索
- **AI 集成**：基于 LangChain4j + OpenAI（可配置 API Key / Base URL），用于对话与嵌入
- **邮箱验证码登录**：输入邮箱获取验证码，验证码登录；未注册邮箱将自动创建账号

## 技术栈

| 层级     | 技术 |
|----------|------|
| 后端     | Java 17、Spring Boot 3.2、Spring Data JPA、LangChain4j、Qdrant、MySQL |
| 前端     | Vue 3、Vue Router、Vite 5、Axios |
| AI/向量  | OpenAI API（Chat + Embedding）、Qdrant 向量库 |

## 项目结构

```
ai-noval-generator/
├── src/                    # 后端 (Spring Boot)
│   └── main/java/com/viking/ai/novel/
│       ├── application/     # 应用服务
│       ├── domain/          # 领域模型与仓储接口
│       ├── infrastructure/  # 配置、AI 服务、JPA/Qdrant 实现
│       └── interfaces/     # Controller、DTO、Mapper
├── web/                    # 前端 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/            # 接口封装 (novels, chapters, novelVectors, userVectors)
│   │   ├── components/     # 公共组件（创建项目、API 设置等）
│   │   ├── router/         # 路由
│   │   └── views/          # 页面（首页、小说列表/详情/表单、资料库）
│   └── package.json
├── pom.xml
└── README.md
```

## 环境要求

- **JDK 17**
- **Node.js** 18+（用于前端）
- **MySQL** 8.x
- **Qdrant**（向量数据库，默认端口 6333）
- **OpenAI API Key**（或兼容接口的 Base URL + Key）

## 快速开始

### 1. 数据库与 Qdrant

- 创建 MySQL 数据库，例如：`ai_novel`
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

## 配置说明

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

前端开发环境下 API 代理在 `web/vite.config.js` 中配置（`/api` → `http://localhost:8080`）。

## Docker 启动

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

## 常用命令

- **后端**：`mvn spring-boot:run`（开发）、`mvn clean package`（打包）
- **前端**：`npm run dev`（开发）、`npm run build`（构建）、`npm run preview`（预览构建结果）

## 许可证

请根据项目实际情况添加许可证信息。
