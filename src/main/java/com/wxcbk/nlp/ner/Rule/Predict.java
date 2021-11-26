package com.wxcbk.nlp.ner.Rule;

import com.wxcbk.nlp.ner.dim.time.enums.Grain;

import java.util.regex.Matcher;


public class Predict {
    private Grain grain;
    private String regex;
    private Matcher matcher;
    private String ruleName;


    public Grain getGrain() {
        return grain;
    }

    public void setGrain(Grain grain) {
        this.grain = grain;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
}
