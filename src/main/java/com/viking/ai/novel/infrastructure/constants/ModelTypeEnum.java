package com.viking.ai.novel.infrastructure.constants;

public enum ModelTypeEnum {

    NORMAL(0, "普通数据库"),
    VECTOR(1, "向量数据库"),
    ;

    private int type;
    private String description;

    ModelTypeEnum(int type, String description) {
        this.type = type;
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
