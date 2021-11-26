package com.wxcbk.nlp.ner.Rule;

import com.wxcbk.nlp.ner.enums.ReferType;

import java.util.regex.Pattern;


public class Reference {
    private String name;
    private String regex;
    private Pattern pattern;
    private ReferType referType;

    public Reference(String name, String regex) {
        this.name = name;
        this.regex = regex;
        this.referType = ReferType.REGEX;
    }

    public Reference(String name, Pattern pattern) {
        this.name = name;
        this.pattern = pattern;
        this.referType = ReferType.PATTERN;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public ReferType getReferType() {
        return referType;
    }

    public void setReferType(ReferType referType) {
        this.referType = referType;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
