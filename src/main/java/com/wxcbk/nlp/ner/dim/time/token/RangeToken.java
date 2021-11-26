package com.wxcbk.nlp.ner.dim.time.token;

import com.wxcbk.nlp.ner.dim.time.enums.Grain;
import com.wxcbk.nlp.ner.dim.time.enums.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;


public class RangeToken extends Token {
    private boolean interval;
    private Grain grain;
    private List<String> values;

    public RangeToken(Grain grain, boolean interval, int start, int end, Matcher m) {
        this.setType(TokenType.RANGE);
        this.grain = grain;
        this.interval = interval;
        this.setText(m.group());
        this.setStart(start);
        this.setEnd(end);
        fillValues(start, end);
    }

    public RangeToken(Grain grain, int start, int end, Matcher m) {
        this.setType(TokenType.RANGE);
        this.grain = grain;
        this.interval = false;
        this.setText(m.group());
        this.setStart(m.start());
        this.setEnd(m.end());
        fillValues(start, end);
    }

    public RangeToken(Grain grain, Matcher m, boolean interval, int value1, int value2) {
        this.setType(TokenType.RANGE);
        this.grain = grain;
        this.interval = interval;
        this.setText(m.group());
        this.setStart(m.start());
        this.setEnd(m.end());
        List<String> list = new ArrayList<>();
        list.add(String.valueOf(value1));
        list.add(String.valueOf(value2));
        this.setValues(list);
    }

    private void fillValues(int start, int end) {
        List<String> list = new ArrayList<>();
        for (int i = start; i < end + 1; i++) {
            list.add(String.valueOf(i));
        }
        this.setValues(list);
    }


    public boolean isInterval() {
        return interval;
    }

    public void setInterval(boolean interval) {
        this.interval = interval;
    }

    public Grain getGrain() {
        return grain;
    }

    public void setGrain(Grain grain) {
        this.grain = grain;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
