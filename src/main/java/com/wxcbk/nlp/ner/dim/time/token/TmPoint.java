package com.wxcbk.nlp.ner.dim.time.token;

import com.wxcbk.nlp.ner.dim.time.enums.Grain;
import com.wxcbk.nlp.ner.dim.time.enums.TimeDim;
import com.wxcbk.nlp.ner.dim.time.enums.TokenType;

import java.util.regex.Matcher;


public class TmPoint extends BaseToken {
    private FuzzyToken fuzzyItem;

    public TmPoint(Grain grain, String grainValue, String text, int start, int end) {
        this.setType(TokenType.T_POINT);
        this.setTimeDim(TimeDim.DT);
        this.setGrain(grain);
        this.setGrainValue(grainValue);
        this.setText(text);
        this.setStart(start);
        this.setEnd(end);
    }

    public TmPoint(Grain grain, String grainValue, Matcher m) {
        this.setType(TokenType.T_POINT);
        this.setTimeDim(TimeDim.DT);
        this.setGrain(grain);
        this.setGrainValue(grainValue);
        this.setText(m.group());
        this.setStart(m.start());
        this.setEnd(m.end());
    }

    public TmPoint(Grain grain, FuzzyToken fuzzyItem, Matcher m) {
        this.setType(TokenType.T_POINT);
        this.setTimeDim(TimeDim.DT);
        this.setGrain(grain);
        this.setFuzzyItem(fuzzyItem);
        this.setText(m.group());
        this.setStart(m.start());
        this.setEnd(m.end());
    }

    public FuzzyToken getFuzzyItem() {
        return fuzzyItem;
    }

    public void setFuzzyItem(FuzzyToken fuzzyItem) {
        this.fuzzyItem = fuzzyItem;
    }

}
