package com.wxcbk.nlp.ner.dim.time.token;

import com.wxcbk.nlp.ner.dim.time.enums.Grain;
import com.wxcbk.nlp.ner.dim.time.enums.TimeDim;
import com.wxcbk.nlp.ner.dim.time.enums.TokenType;

import java.util.regex.Matcher;


public class Interval extends BaseToken {
    private String grainInterval;

    public Interval(Grain grain, String grainValue, String grainInterval, String text, int start, int end) {
        this(grain, grainInterval, text, start, end);
        this.setGrainValue(grainValue);
    }

    public Interval(Grain grain, String grainInterval, String text, int start, int end) {
        this.setTimeDim(TimeDim.DT);
        this.setType(TokenType.INTERVAL);
        this.setGrain(grain);
        this.grainInterval = grainInterval;
        this.setStart(start);
        this.setEnd(end);
        this.setText(text);
    }

    public Interval(Grain grain, String grainInterval, Matcher m) {
        this.setTimeDim(TimeDim.DT);
        this.setType(TokenType.INTERVAL);
        this.setGrain(grain);
        this.grainInterval = grainInterval;
        this.setStart(m.start());
        this.setEnd(m.end());
        this.setText(m.group());
    }

    public String getGrainInterval() {
        return grainInterval;
    }

    public void setGrainInterval(String grainInterval) {
        this.grainInterval = grainInterval;
    }
}
