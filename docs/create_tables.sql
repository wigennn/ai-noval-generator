-- ============================================================
-- AI 小说生成器 - 建表脚本（MySQL）
-- 规范：小写表名、下划线命名、主键自增、必备 COMMENT、合理索引与唯一键
-- ============================================================

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
CREATE TABLE users (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username   VARCHAR(255)          DEFAULT NULL COMMENT '用户名',
    password   VARCHAR(255)          DEFAULT NULL COMMENT '密码（加密存储）',
    phone      VARCHAR(255)          DEFAULT NULL COMMENT '手机号',
    email      VARCHAR(255)          NOT NULL COMMENT '邮箱（登录账号）',
    score      INT                   DEFAULT 100 COMMENT '积分',
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 用户 AI 模型配置表（对话模型、嵌入模型等）
-- ------------------------------------------------------------
CREATE TABLE user_models (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT       NOT NULL COMMENT '用户ID',
    model_name VARCHAR(255) NOT NULL COMMENT '模型名称',
    model_url  VARCHAR(255) NOT NULL COMMENT '模型 API 地址',
    model_type VARCHAR(255) NOT NULL COMMENT '模型类型（如 DeepSeek、OpenAI）',
    api_key    VARCHAR(255) NOT NULL COMMENT 'API 密钥',
    type       TINYINT      NOT NULL DEFAULT 0 COMMENT '0: 对话模型，1: 嵌入模型',
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_models_user_type (user_id, type),
    KEY idx_user_models_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户AI模型配置表';

-- ------------------------------------------------------------
-- 用户资料库（用户上传的向量资料，用于 RAG）
-- ------------------------------------------------------------
CREATE TABLE user_vectors (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    vector_name VARCHAR(255) NOT NULL COMMENT '资料库名称',
    vector_id   VARCHAR(255) NOT NULL COMMENT '向量库中的向量ID',
    created_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_vectors_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资料库（向量）';

-- ------------------------------------------------------------
-- 小说信息表
-- ------------------------------------------------------------
CREATE TABLE novel (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id           BIGINT       NOT NULL COMMENT '所属用户ID',
    title             VARCHAR(255) NOT NULL COMMENT '小说标题',
    genre             VARCHAR(50)           DEFAULT NULL COMMENT '题材（玄幻、都市等）',
    setting_text      LONGTEXT              DEFAULT NULL COMMENT '世界观设定（用于 RAG 入库）',
    structure         LONGTEXT              DEFAULT NULL COMMENT '小说架构/结构',
    chapter_outline   LONGTEXT              DEFAULT NULL COMMENT '章节大纲',
    chapter_number    INT                   DEFAULT 0 COMMENT '计划章节总数',
    chapter_word_count INT                  DEFAULT 0 COMMENT '每章计划字数',
    status            TINYINT              DEFAULT 0 COMMENT '0: 草稿，1: 发布中，2: 已完成',
    created_at        TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_novel_user_id (user_id),
    KEY idx_novel_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说信息表';

-- ------------------------------------------------------------
-- 小说向量关联表（小说关联的资料库向量，用于 RAG）
-- ------------------------------------------------------------
CREATE TABLE novel_vectors (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    novel_id   BIGINT       NOT NULL COMMENT '小说ID',
    vector_id  VARCHAR(255) NOT NULL COMMENT '向量库中的向量ID',
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_novel_vectors_novel_vector (novel_id, vector_id),
    KEY idx_novel_vectors_novel_id (novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说与资料库向量关联表';

-- ------------------------------------------------------------
-- 章节内容表
-- ------------------------------------------------------------
CREATE TABLE chapters (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    novel_id         BIGINT       NOT NULL COMMENT '所属小说ID',
    chapter_number   INT          NOT NULL COMMENT '章节序号（第几章）',
    title            VARCHAR(255)          DEFAULT NULL COMMENT '章节标题',
    abstract_content VARCHAR(500)          DEFAULT NULL COMMENT '章节摘要',
    content          LONGTEXT              DEFAULT NULL COMMENT '正文内容',
    vector_id        VARCHAR(100)          DEFAULT NULL COMMENT '向量库中本章节向量ID',
    status           TINYINT              DEFAULT 0 COMMENT '0: 待生成，1: 生成中，2: 已完成',
    created_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_chapters_novel_number (novel_id, chapter_number),
    KEY idx_chapters_novel_id (novel_id),
    KEY idx_chapters_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节内容表';

-- ------------------------------------------------------------
-- 基础配置表（大模型类型、小说类型等键值配置）
-- ------------------------------------------------------------
CREATE TABLE basic_config (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    type       VARCHAR(255) NOT NULL COMMENT '配置类型',
    value      VARCHAR(255) NOT NULL COMMENT '配置值',
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_basic_config_type (type(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='基础配置表';

-- ------------------------------------------------------------
-- 异步任务表
-- ------------------------------------------------------------
CREATE TABLE task (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    task_name        VARCHAR(255) NOT NULL COMMENT '任务名称',
    task_type        VARCHAR(255) NOT NULL COMMENT '任务类型',
    task_relation_id BIGINT       NOT NULL COMMENT '关联业务ID（如小说ID、章节ID）',
    task_status      TINYINT               DEFAULT 0 COMMENT '0: 待处理，1: 处理中，2: 完成，3: 失败',
    created_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_task_type_status (task_type(50), task_status),
    KEY idx_task_relation_id (task_relation_id),
    KEY idx_task_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异步任务表';

create table score_log(
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT       NOT NULL COMMENT '用户ID',
    use_score      INT          NOT NULL COMMENT '积分',
    use_score_type VARCHAR(255) NOT NULL COMMENT '积分类型',
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_score_log_user_id (user_id),
    KEY idx_score_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';