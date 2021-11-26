package com.wxcbk.nlp.ner.dim.time.enums;



public enum FuzzyDay {
    /**
     * 凌晨		0am - 5am
     * 早上		5am - 8am
     * 上午		8am - 12am
     * 中午		11am - 2am
     * 下午		12am - 7pm
     * 傍晚		5pm - 7pm
     * 晚上		6pm - 12pm
     * 白天		5am - 8pm
     * 夜里		7pm - 4am(+1) 104
     */
    BEFORE_DAWN("beforeDawn", 0, 5),
    EARLY_MORNING("morning", 5, 8),
    MORNING("am", 8, 12),
    NOON("noon", 11, 14),
    AFTER_NOON("pm", 12, 19),
    EVENING("evening", 17, 19),
    NIGHT("night", 18, 24),
    DAY("day", 6, 20),
    AT_NIGHT("atNight", 21, 104);

    int start;
    int end;
    String name;

    FuzzyDay(String name, int start, int end) {
        this.start = start;
        this.end = end;
        this.name = name;
    }

    public static String getFuzzyName(String str) {
        if ("凌晨".equals(str)) {
            return BEFORE_DAWN.getName();
        } else if ("早上".equals(str)) {
            return EARLY_MORNING.getName();
        } else if ("上午".equals(str)) {
            return MORNING.getName();
        } else if ("中午".equals(str)) {
            return NOON.getName();
        } else if ("下午".equals(str)) {
            return AFTER_NOON.getName();
        } else if ("傍晚".equals(str)) {
            return EVENING.getName();
        } else if ("晚上".equals(str)) {
            return NIGHT.getName();
        } else if ("白天".equals(str)) {
            return DAY.getName();
        } else if ("夜里".equals(str)) {
            return AT_NIGHT.getName();
        }
        return "fuzzyDay";
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
