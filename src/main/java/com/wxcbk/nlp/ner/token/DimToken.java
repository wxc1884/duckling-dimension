package com.wxcbk.nlp.ner.token;


import com.wxcbk.nlp.ner.dim.time.enums.Hint;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


public abstract class DimToken {
    private String dimName;
    private String text;
    private Hint hint;
    private int start;
    private int end;
    private List<String> rules;


    public String getDimName() {
        return dimName;
    }

    public void setDimName(String dimName) {
        this.dimName = dimName;
    }

    public Hint getHint() {
        return hint;
    }

    public void setHint(Hint hint) {
        this.hint = hint;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }

    public int getTextLength() {
        if (StringUtils.isEmpty(text)) {
            return 0;
        }
        return text.length();
    }

    @Override
    public String toString() {
        return "DimToken{" +
                "dimName='" + dimName + '\'' +
                ", text='" + text + '\'' +
                ", hint=" + hint +
                ", start=" + start +
                ", end=" + end +
                ", rules=" + rules.toString() +
                '}';
    }
}
