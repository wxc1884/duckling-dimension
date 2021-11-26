package com.wxcbk.nlp.ner.Rule;

import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.engine.DimContext;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;


public class MergeRule {

    private String name;
    private BiPredicate<Map<String, List<DimToken>>, DimContext> predicate;
    private BiFunction<Map<String, List<DimToken>>, DimContext, List<DimToken>> production;


    public MergeRule(String name, BiPredicate<Map<String, List<DimToken>>, DimContext> predicate, BiFunction<Map<String, List<DimToken>>, DimContext, List<DimToken>> production) {
        this.name = name;
        this.predicate = predicate;
        this.production = production;
    }

    public boolean predict(Map<String, List<DimToken>> dependentTokens, DimContext context) {
        return predicate.test(dependentTokens, context);
    }

    public List<DimToken> produce(Map<String, List<DimToken>> dependentTokens, DimContext context) {
        return production.apply(dependentTokens, context);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BiPredicate<Map<String, List<DimToken>>, DimContext> getPredicate() {
        return predicate;
    }

    public void setPredicate(BiPredicate<Map<String, List<DimToken>>, DimContext> predicate) {
        this.predicate = predicate;
    }

    public BiFunction<Map<String, List<DimToken>>, DimContext, List<DimToken>> getProduction() {
        return production;
    }

    public void setProduction(BiFunction<Map<String, List<DimToken>>, DimContext, List<DimToken>> production) {
        this.production = production;
    }
}
