package com.wxcbk.nlp.ner.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Lists {

    public static <T> List<T> of(T... items) {
        if (items.length == 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(items));
    }

    public static boolean containsOr(List<String> ol, List<String> sl) {
        for (String str : sl) {
            if (ol.contains(str)) {
                return true;
            }
        }
        return false;
    }
}
