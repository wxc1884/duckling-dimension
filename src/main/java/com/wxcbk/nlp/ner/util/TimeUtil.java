package com.wxcbk.nlp.ner.util;

import com.wxcbk.nlp.ner.dim.time.enums.IntervalDirection;
import org.apache.commons.lang3.StringUtils;


public class TimeUtil {

    public static String extractInterval(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        if (Strings.orEquals(str, "大前")) {
            return "-3";
        }
        if (Strings.orEquals(str, "前")) {
            return "-2";
        }
        if (Strings.orEquals(str, "上", "昨")) {
            return "-1";
        }
        if (Strings.orEquals(str, "这", "今", "本")) {
            return "0";
        }
        if (Strings.orEquals(str, "下", "明")) {
            return "1";
        }
        if (Strings.orEquals(str, "后")) {
            return "2";
        }
        if (Strings.orEquals(str, "大后")) {
            return "3";
        }
        return "";
    }

    public static String extractDirection(String directStr) {
        if (StringUtils.isEmpty(directStr)) {
            return null;
        }
        if ("前".equals(directStr)) {
            return IntervalDirection.BEFORE.getName();
        }
        if ("后".equals(directStr)) {
            return IntervalDirection.AFTER.getName();
        }
        if ("左右".equals(directStr)) {
            return IntervalDirection.AROUND.getName();
        }
        if (Strings.orEquals(directStr, "最近", "近")) {
            return IntervalDirection.RECENT.getName();
        }
        if ("未来".equals(directStr)) {
            return IntervalDirection.FUTURE.getName();
        }
        return StringUtils.EMPTY;
    }

    public static String extractDirectionPos(String direction) {
        if (Strings.orEquals(direction, IntervalDirection.RECENT.getName(), IntervalDirection.FUTURE.getName())) {
            return "prefix";
        }
        return "suffix";
    }

    public static String extractHalf(String num, String half) {
        if ("半".equals(half)) {
            return "30";
        }
        if ("刻".equals(half)) {
            if (num != null) {
                return String.valueOf(NumUtil.chineseToNum(num) * 15);
            }
        }
        return "15";
    }
}
