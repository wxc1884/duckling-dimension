package com.wxcbk.nlp.ner.dim.time.token;



public class FuzzyToken {
    private String fuzzyType;
    private String fuzzyValue;

    public FuzzyToken(String fuzzyType, String fuzzyValue) {
        this.fuzzyType = fuzzyType;
        this.fuzzyValue = fuzzyValue;
    }

    public String getFuzzyType() {
        return fuzzyType;
    }

    public void setFuzzyType(String fuzzyType) {
        this.fuzzyType = fuzzyType;
    }

    public String getFuzzyValue() {
        return fuzzyValue;
    }

    public void setFuzzyValue(String fuzzyValue) {
        this.fuzzyValue = fuzzyValue;
    }
}
