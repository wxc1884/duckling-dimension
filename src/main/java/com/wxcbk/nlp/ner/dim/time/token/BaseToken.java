package com.wxcbk.nlp.ner.dim.time.token;

import com.wxcbk.nlp.ner.dim.time.enums.Grain;


public abstract class BaseToken extends Token {
    private Grain grain;
    private String grainValue;


    public Grain getGrain() {
        return grain;
    }

    public void setGrain(Grain grain) {
        this.grain = grain;
    }

    public String getGrainValue() {
        return grainValue;
    }

    public void setGrainValue(String grainValue) {
        this.grainValue = grainValue;
    }
}
