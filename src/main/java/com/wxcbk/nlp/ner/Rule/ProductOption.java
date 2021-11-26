package com.wxcbk.nlp.ner.Rule;

import com.wxcbk.nlp.ner.engine.DimContext;


public class ProductOption {
    private String ruleName;
    private Predict predict;
    private DimContext dimContext;


    public ProductOption(String ruleName, Predict predict, DimContext dimContext) {
        this.ruleName = ruleName;
        this.predict = predict;
        this.dimContext = dimContext;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Predict getPredict() {
        return predict;
    }

    public void setPredict(Predict predict) {
        this.predict = predict;
    }

    public DimContext getDimContext() {
        return dimContext;
    }

    public void setDimContext(DimContext dimContext) {
        this.dimContext = dimContext;
    }
}
