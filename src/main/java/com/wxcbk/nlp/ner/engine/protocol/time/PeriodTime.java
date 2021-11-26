package com.wxcbk.nlp.ner.engine.protocol.time;


public class PeriodTime extends BaseSubTime {
    private PointTime from;
    private PointTime to;


    public PointTime getFrom() {
        return from;
    }

    public void setFrom(PointTime from) {
        this.from = from;
    }

    public PointTime getTo() {
        return to;
    }

    public void setTo(PointTime to) {
        this.to = to;
    }
}
