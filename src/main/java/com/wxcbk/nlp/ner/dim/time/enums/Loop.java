package com.wxcbk.nlp.ner.dim.time.enums;

import com.wxcbk.nlp.ner.constant.Constants;
import org.apache.commons.lang3.StringUtils;


public enum Loop {

    /**
     * 每天，每月
     */
    LOOP_DAY("everyDay"),
    LOOP_WEEK("everyWeek"),
    LOOP_WEEK_DAYS("everyWeekDays"),
    LOOP_WEEK_END("everyWeekEnd"),
    LOOP_WEEK_DAY("everyWeekDay"),
    LOOP_MONTH("everyMonth"),
    LOOP_QUARTER("everyQuarter"),
    LOOP_YEAR("everyYear"),
    LOOP_HOLIDAY("holiday");

    private String name;

    private Loop(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Loop extractLoop(String grain) {
        if (StringUtils.isEmpty(grain)) {
            return null;
        }
        switch (grain) {
            case Constants.YEAR:
                return LOOP_YEAR;
            case Constants.QUARTER:
                return LOOP_QUARTER;
            case Constants.MONTH:
                return LOOP_MONTH;
            case Constants.WEEK:
                return LOOP_WEEK;
            case Constants.WEEK_DAY:
                return LOOP_WEEK_DAYS;
            case Constants.DAY:
                return LOOP_DAY;
            default:
        }
        return null;
    }
}
