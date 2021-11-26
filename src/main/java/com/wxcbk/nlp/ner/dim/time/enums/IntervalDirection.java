package com.wxcbk.nlp.ner.dim.time.enums;


public enum IntervalDirection {
    /**
     * 时间间隔的方向
     * 一周前
     * 3天后
     */
    BEFORE("before"),
    AFTER("after"),
    RECENT("recent"),
    FUTURE("future"),
    AROUND("around");

    private String name;


    IntervalDirection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
