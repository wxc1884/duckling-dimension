package com.wxcbk.nlp.ner.dim.time.enums;


public enum FuzzyWeek {
    /**
     * 工作日，周末
     */
    WEEK_DAYS("weekDays"),
    WEEK_END("weekEnd");

    private String name;

    FuzzyWeek(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
