package com.wxcbk.nlp.ner.engine.helper;

import com.wxcbk.nlp.ner.Rule.Rule;
import com.wxcbk.nlp.ner.dim.Dimension;
import com.wxcbk.nlp.ner.engine.DimensionEngine;
import com.wxcbk.nlp.ner.util.FileUtil;
import com.wxcbk.nlp.ner.util.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DimensionManager {

    private static Logger log = LoggerFactory.getLogger(DimensionEngine.class);
    private Map<String, List<Rule>> dim2Rules;
    private Map<String, Dimension> name2Dim;
    private List<List<String>> dependencies;

    public DimensionManager() {
        List<Class<Dimension>> dimensionClasses = FileUtil.scanSubClass("com.wxcbk.nlp.ner",Dimension.class);
        initDimensions(dimensionClasses);
        initRules(dimensionClasses);
        computeDependency();
    }

    private void initRules(List<Class<Dimension>> ruleClasses) {
        dim2Rules = new HashMap<>();
        ruleClasses.forEach(c -> {
            List<Rule> rules = FileUtil.collectRules(c);
            if (CollectionUtils.isNotEmpty(rules)) {
                dim2Rules.put(c.getSimpleName(), rules);
            }
        });
    }

    private void initDimensions(List<Class<Dimension>> dimensions) {
        String name = "";
        name2Dim = new HashMap<>(16);
        try {
            for (Class<Dimension> d : dimensions) {
                name = d.getSimpleName();
                name2Dim.put(name, d.newInstance());
            }
        } catch (Exception e) {
            log.error("init dimension:{} error:", name, e);
        }
    }

    private void computeDependency() {
        dependencies = new ArrayList<>();
        Map<Integer, List<String>> num2dimName = new HashMap<>();
        Map<String, Integer> dimName2Num = new HashMap<>();
        List<String> handledDim = new ArrayList<>();
        for (Map.Entry<String, Dimension> entry : name2Dim.entrySet()) {
            if (handledDim.contains(entry.getKey())) {
                Maps.putKlv(num2dimName, dimName2Num.get(entry.getKey()), entry.getKey());
                continue;
            }
            int num = computeDimNum(entry.getValue(), dimName2Num, handledDim);
            dimName2Num.put(entry.getKey(), num);
            Maps.putKlv(num2dimName, num, entry.getKey());
            handledDim.add(entry.getKey());
        }
        num2dimName.forEach((k, v) -> dependencies.add(v));

    }

    private int computeDimNum(Dimension dimension, Map<String, Integer> dimName2Num, List<String> handledDim) {
        String dimName = dimension.dimensionName();
        if (handledDim.contains(dimName)) {
            return dimName2Num.get(dimName);
        }
        if (CollectionUtils.isEmpty(dimension.getDependent())) {
            dimName2Num.put(dimName, 0);
            handledDim.add(dimName);
            return 0;
        }
        int max = 0;
        for (String dim : dimension.getDependent()) {
            if (handledDim.contains(dim)) {
                int newMax = dimName2Num.get(dim) + 1;
                if (newMax > max) {
                    max = newMax;
                }
            } else {
                int newMax = computeDimNum(name2Dim.get(dim), dimName2Num, handledDim) + 1;
                if (newMax > max) {
                    max = newMax;
                }
            }
            dimName2Num.put(dimName, max);
            handledDim.add(dimName);
        }
        return max;
    }

    public Map<String, List<Rule>> getDim2Rules() {
        return dim2Rules;
    }

    public void setDim2Rules(Map<String, List<Rule>> dim2Rules) {
        this.dim2Rules = dim2Rules;
    }

    public Map<String, Dimension> getName2Dim() {
        return name2Dim;
    }

    public void setName2Dim(Map<String, Dimension> name2Dim) {
        this.name2Dim = name2Dim;
    }

    public List<List<String>> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<List<String>> dependencies) {
        this.dependencies = dependencies;
    }
}
