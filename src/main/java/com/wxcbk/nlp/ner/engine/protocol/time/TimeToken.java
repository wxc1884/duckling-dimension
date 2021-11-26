package com.wxcbk.nlp.ner.engine.protocol.time;

import com.wxcbk.nlp.ner.engine.protocol.BaseDimData;


public class TimeToken extends BaseDimData {
    private String text;
    private int start;
    private int end;
    private Time time;


    public TimeToken(String dimension, String text, int start, int end) {
        this.setDimension(dimension);
        this.text = text;
        this.start = start;
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }
}
