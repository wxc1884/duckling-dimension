package com.wxcbk.nlp.ner.dim.time.data;

import com.wxcbk.nlp.ner.Rule.Reference;
import com.wxcbk.nlp.ner.Rule.Rule;
import com.wxcbk.nlp.ner.dim.Dimension;
import com.wxcbk.nlp.ner.engine.DimContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Num implements Dimension {

    private static Pattern p = Pattern.compile("([零一二两俩三四五六七八九十百千亿]{1,4}|\\d)+");


    @Override
    public List<Reference> initRefer() {
        List<Reference> references = new ArrayList<>();
        references.add(new Reference("num", "[零一二两三四五六七八九十百千亿\\d]+"));
        references.add(new Reference("yearNum", "[零一二两三四五六七八九十百千]{1,4}|\\d{1,4}"));
        references.add(new Reference("monthNum", "[一二两三四五六七八九十]{1,4}|\\d{1,2}"));
        references.add(new Reference("weekNum", "[一二两三四五六日]{1}|\\d{1}"));
        references.add(new Reference("dayNum", "[一二两三四五六七八九十]{1,3}|\\d{1,2}"));
        references.add(new Reference("hourNum", "[一二两三四五六七八九十]{1,3}|\\d{1,2}"));
        references.add(new Reference("minuteNum", "[一二两三四五六七八九十]{1,3}|\\d{1,2}"));
        references.add(new Reference("secondNum", "[一二两三四五六七八九十]{1,3}|\\d{1,2}"));
        return references;
    }

    @Override
    public Reference generateRefer(Rule rule, DimContext dimContext) {
        return null;
    }

    @Override
    public boolean isDimension(DimContext dimContext) {
        Matcher m = p.matcher(dimContext.getQuery());
        return m.find();
    }

    @Override
    public List<String> getDependent() {
        return null;
    }
}
