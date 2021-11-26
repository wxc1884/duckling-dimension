package com.wxcbk.nlp.ner.dim.time.enums;

import com.wxcbk.nlp.ner.util.Strings;
import org.apache.commons.lang3.StringUtils;


public enum PeriodType {
    /**
     * 时间段的连接关系，
     * 今天和明天  今天8点到明天7点
     */
    AND("and"),
    TO("to"),
    RANGE("range");

    private String name;

    PeriodType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static String extractType(String str) {
        if ("和".equals(str)) {
            return AND.getName();
        } else if (Strings.orEquals(str, "到", "至", "~")) {
            return TO.getName();
        }
        return StringUtils.EMPTY;
    }


}
