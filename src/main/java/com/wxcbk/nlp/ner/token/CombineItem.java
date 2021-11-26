package com.wxcbk.nlp.ner.token;

import com.wxcbk.nlp.ner.dim.time.enums.Hint;


public class CombineItem {
    private double prior;
    private int textLength;
    private int start;
    private int end;
    private Hint hint;
    private DimToken dimToken;

    public double getPrior() {
        return prior;
    }

    public void setPrior(double prior) {
        this.prior = prior;
    }

    public int getTextLength() {
        return textLength;
    }

    public void setTextLength(int textLength) {
        this.textLength = textLength;
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

    public DimToken getDimToken() {
        return dimToken;
    }

    public void setDimToken(DimToken dimToken) {
        this.dimToken = dimToken;
    }

    public Hint getHint() {
        return hint;
    }

    public void setHint(Hint hint) {
        this.hint = hint;
    }
}
