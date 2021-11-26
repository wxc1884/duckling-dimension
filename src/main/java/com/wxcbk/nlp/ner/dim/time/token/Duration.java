package com.wxcbk.nlp.ner.dim.time.token;

import com.wxcbk.nlp.ner.dim.time.enums.Grain;
import com.wxcbk.nlp.ner.dim.time.enums.TimeDim;
import com.wxcbk.nlp.ner.dim.time.enums.TokenType;

import java.util.regex.Matcher;


public class Duration extends Token {
    private Grain grain;
    private String grainValue;


    public Duration(Grain grain, String grainValue, Matcher m) {
        this.setType(TokenType.DURATION);
        this.setTimeDim(TimeDim.DT);
        this.grain = grain;
        this.grainValue = grainValue;
        this.setText(m.group());
        this.setStart(m.start());
        this.setEnd(m.end());
    }

    public Duration(Grain grain, String grainValue) {
        this.setType(TokenType.DURATION);
        this.setTimeDim(TimeDim.DT);
        this.grain = grain;
        this.grainValue = grainValue;
    }

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
