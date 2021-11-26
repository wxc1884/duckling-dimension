package com.wxcbk.nlp.ner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NumUtil {
    private static Pattern p = Pattern.compile("\\d+");

    public static int parseInt(String query) {
        return chineseToNum(query);
    }

    public static List<Integer> stringToIntList(List<String> list) {
        List<Integer> intList = new ArrayList<>();
        for (String str : list) {
            intList.add(parseInt(str));
        }
        return intList;
    }

    public static Integer parseInteger(String query) {
        if (StringUtils.isEmpty(query)) {
            return null;
        }
        Matcher m = p.matcher(query);
        if (m.matches()) {
            return Integer.parseInt(query);
        }
        return chineseToNum(query);
    }

    public static boolean isIntegerNum(String query) {
        return true;
    }

    public static int chineseToNum(String query) {
        Matcher m = p.matcher(query);
        if (m.matches()) {
            return Integer.parseInt(query);
        }
        StringBuilder sb = new StringBuilder();
        char[] cs = query.toCharArray();
        for (char c : cs) {
            if (c == '零') {
                sb.append(0);
            } else if (c == '一' || c == '壹') {
                sb.append(1);
            } else if (c == '二' || c == '两' || c == '贰') {
                sb.append(2);
            } else if (c == '三' || c == '叁') {
                sb.append(3);
            } else if (c == '四' || c == '肆') {
                sb.append(4);
            } else if (c == '五' || c == '伍') {
                sb.append(5);
            } else if (c == '六' || c == '陆') {
                sb.append(6);
            } else if (c == '七' || c == '柒' || c == '日') {
                sb.append(7);
            } else if (c == '八' || c == '捌') {
                sb.append(8);
            } else if (c == '九' || c == '玖') {
                sb.append(9);
            } else if (c == '十' || c == '拾') {
                sb.append(10);
            }
        }
        return Integer.parseInt(sb.toString());
    }
}
