package com.wxcbk.nlp.ner.util;

import org.apache.commons.lang3.StringUtils;


public class Strings {


    public static boolean orEquals(String str, String... strings) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        for (String t : strings) {
            if (str.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public static String trimNum(String query) {
        return query.replaceAll("\\d{1,3}", "");
    }
}
