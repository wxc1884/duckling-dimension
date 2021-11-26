package com.wxcbk.nlp.ner.dim.time.helper;

import com.wxcbk.nlp.ner.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HolidayProvider {
    private static Logger log = LoggerFactory.getLogger(HolidayProvider.class);

    private static Map<String, String> holidayToken2Norm;
    private static Pattern p;
    private static final String PATH = "/holiday.txt";

    static {
        holidayToken2Norm = new HashMap<>();
        List<List<String>> lines = FileUtil.readCsv(PATH);
        for (List<String> line : lines) {
            String norm = line.get(0);
            holidayToken2Norm.put(norm, norm);
            if (line.size() == 2) {
                String syn = line.get(1);
                String[] synList = syn.split("\\|");
                for (String str : synList) {
                    holidayToken2Norm.put(str, norm);
                }
            }
        }
        List<String> holidays = new ArrayList<>(holidayToken2Norm.keySet());
        holidays.sort(Comparator.comparingInt(String::length).reversed());
        try {
            p = Pattern.compile("(" + String.join("|", holidays) + ")");
        } catch (Exception e) {
            log.error("compile holiday pattern error:", e);
        }
    }

    public static List<String> findHolidayToken(String query) {
        List<String> list = new ArrayList<>();
        Matcher m = p.matcher(query);
        while (m.find()) {
            list.add(m.group());
        }
        return list;
    }

    public static String searchNormHoliday(String query) {
        if (holidayToken2Norm.containsKey(query)) {
            return holidayToken2Norm.get(query);
        }
        return query;
    }

    public static void main(String[] args) {
        List<String> list = findHolidayToken("春节端午节");
        System.out.println(list);
    }
}

