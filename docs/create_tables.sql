-- 用户表
create TABLE users
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(255) default NULL,
    password   VARCHAR(255) default NULL,
    phone      VARCHAR(255) default NULL,
    email      VARCHAR(255) default NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ai大模型信息表
create TABLE user_models
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    model_name VARCHAR(255) NOT NULL, -- 模型名称
    model_url  VARCHAR(255) NOT NULL, -- 模型URL
    model_type VARCHAR(255) NOT NULL, -- 模型类型（如 DeepSeek, OpenAI 等）
    api_key    VARCHAR(255) NOT NULL, -- API密钥
    type int(1) DEFAULT 0, -- 0: 对话模型，1: 嵌入模型
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户资料库
create TABLE user_vectors
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    vector_name VARCHAR(255) NOT NULL, -- 向量名称
    vector_id  VARCHAR(255) NOT NULL, -- 对应向量数据库中的ID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 基础配置表（大模型类型配置，小说类型配置）
create TABLE basic_config(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    type  VARCHAR(255) NOT NULL,
    value  VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)

-- 小说信息表
CREATE TABLE novel
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    title          VARCHAR(255) NOT NULL, -- 小说标题
    genre          VARCHAR(50),           -- 题材（玄幻、都市等）
    setting_text   LONGTEXT,              -- 世界观设定（用于RAG入库）
    structure      LONGTEXT,              -- 小说结构
    chapter_outline LONGTEXT,            -- 章节大纲
    status         int(1) DEFAULT 0,     -- 0: 草稿，1: 发布中，2: 已完成
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 小说向量关联表
CREATE TABLE novel_vectors
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    novel_id   BIGINT       NOT NULL,
    vector_id  VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 章节内容表
CREATE TABLE chapters
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    novel_id       BIGINT NOT NULL,
    chapter_number INT    NOT NULL, -- 第几章
    title          VARCHAR(255),    -- 章节标题
    abstract_content VARCHAR(500),  -- 章节摘要
    content        LONGTEXT,        -- 正文内容
    vector_id      VARCHAR(100),    -- 对应向量数据库中的ID（外键）
    status         int(1) DEFAULT 0, -- 0: 待处理，1: 处理中，2: 处理完成
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 异步任务表
create TABLE task(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_name  VARCHAR(255) NOT NULL,
    task_type  VARCHAR(255) NOT NULL,
    task_relation_id BIGINT NOT NULL,
    task_status int(1) DEFAULT 0, -- 0: 待处理，1: 处理中，2: 处理完成
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
