package com.wxcbk.nlp.ner.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Maps {


    public static <K, V> void putKlv(Map<K, List<V>> map, K k, V v) {
        if (map == null) {
            return;
        }
        if (map.containsKey(k)) {
            map.get(k).add(v);
        } else {
            List<V> list = new ArrayList<>();
            list.add(v);
            map.put(k, list);
        }
    }
}
