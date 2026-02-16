package com.zhilian.zr.importing.enums;

public enum ImportStrategy {
    FULL_REPLACE("FULL_REPLACE", "全部覆盖"),
    ID_CARD_MERGE("ID_CARD_MERGE", "同证件号覆盖");

    private final String code;
    private final String name;

    ImportStrategy(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }

    public static ImportStrategy fromCode(String code) {
        for (ImportStrategy strategy : values()) {
            if (strategy.code.equals(code)) {
                return strategy;
            }
        }
        return ID_CARD_MERGE;
    }
}
