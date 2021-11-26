package com.wxcbk.nlp.ner;

import com.alibaba.fastjson.JSON;
import com.wxcbk.nlp.ner.engine.DimensionEngine;
import com.wxcbk.nlp.ner.engine.protocol.BaseDimData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class Test {
    private static Logger log = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) {
        DimensionEngine recognizer = new DimensionEngine();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String query = scanner.nextLine();
            long start = System.currentTimeMillis();
            Map<String, List<BaseDimData>> data = recognizer.regDimension(query);
            log.info("time:{}", System.currentTimeMillis() - start);
            log.info("timeData:{}", JSON.toJSONString(data));
        }
    }
}
