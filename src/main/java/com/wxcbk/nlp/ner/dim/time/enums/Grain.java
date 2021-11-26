package com.wxcbk.nlp.ner.dim.time.enums;

import com.wxcbk.nlp.ner.util.Strings;


public enum Grain {

    /**
     * 时间粒度：
     * 年，季度，月，周，日，时，分，秒
     */
    YEAR("year", 11),
    QUARTER("quarter", 10),
    LUNAR("lunar",10),
    MONTH("month", 9),
    MONTH_FUZZY("fuzzyMonth", 6),
    WEEK("week", 7),
    WEEK_FUZZY("fuzzyWeek", 6),
    WEEK_DAY("weekDay", 5),
    HOLIDAY("holiday", 5),
    DAY("day", 5),
    DAY_FUZZY("fuzzyDay", 4),
    HOUR("hour", 3),
    MINUTE("minute", 2),
    SECOND("second", 1);


    private String name;
    private int level;

    Grain(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public static int getGrainLevel(String name) {
        for (Grain grain : Grain.values()) {
            if (name.equals(grain.getName())) {
                return grain.getLevel();
            }
        }
        return -1;
    }

    public static Grain fetchGrain(String name) {
        for (Grain grain : Grain.values()) {
            if (name.equals(grain.getName())) {
                return grain;
            }
        }
        return null;
    }

    public static Grain extractGrain(String query) {
        if (Strings.orEquals(query, "年")) {
            return YEAR;
        }
        if (Strings.orEquals(query, "月")) {
            return MONTH;
        }
        if (Strings.orEquals(query, "周", "星期", "礼拜")) {
            return WEEK;
        }
        if (Strings.orEquals(query, "天")) {
            return DAY;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
