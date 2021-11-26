package com.wxcbk.nlp.ner.dim.time.enums;

public enum FuzzyMonth {

    /**
     * 上旬 中旬 下旬
     */
    EARLY_MONTH("earlyMonth"),
    MID_MONTH("midMonth"),
    LATER_MONTH("laterMonth");

    private String value;

    FuzzyMonth(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
