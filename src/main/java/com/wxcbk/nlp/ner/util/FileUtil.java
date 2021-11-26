package com.wxcbk.nlp.ner.util;

import cn.hutool.core.lang.ClassScanner;
import com.csvreader.CsvReader;
import com.wxcbk.nlp.ner.Rule.Rule;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class FileUtil {
    private static Logger log = LoggerFactory.getLogger(FileUtil.class);

    public static List<List<String>> readCsv(String path) {
        InputStream is1 = FileUtil.class.getResourceAsStream(path);
        CsvReader csvReader = new CsvReader(is1, ',', StandardCharsets.UTF_8);
        List<List<String>> list = new ArrayList<>();
        try {
            csvReader.readHeaders();
            while (csvReader.readRecord()) {
                String[] rawLines = csvReader.getValues();
                List<String> lineList = new ArrayList<>();
                Collections.addAll(lineList, rawLines);
                list.add(lineList);
            }

        } catch (Exception e) {
            log.error("read csv data error:", e);
        }
        return list;
    }

    public static <T> List<Class<T>> scanSubClass(String packageName, Class<T> superClass) {
        List<Class<T>> classes = new ArrayList<>();
        Set<Class<?>> subClasses = ClassScanner.scanPackageBySuper(packageName, superClass);
        if (CollectionUtils.isEmpty(subClasses)) {
            return classes;
        }
        subClasses.forEach(s -> {
            Class<T> t = (Class<T>) s;
            classes.add(t);
        });
        return classes;

    }

    public static <T> List<Rule> collectRules(Class<T> cls) {
        List<Rule> rules = new ArrayList<>();
        try {
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                if (field.getModifiers() == 10 && field.getType() == Rule.class) {
                    field.setAccessible(true);
                    Rule rule = (Rule) field.get(field.getName());
                    rule.setTimeDim(cls.getSimpleName());
                    rules.add(rule);
                }
            }
        } catch (Exception e) {
            log.error("collect rule error:", e);
        }
        return rules;
    }
}
