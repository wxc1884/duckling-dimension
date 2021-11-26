package com.wxcbk.nlp.ner.engine.protocol.time;

import com.wxcbk.nlp.ner.engine.protocol.BaseDimData;


public class Time extends BaseDimData {
    private String subType;
    private BaseSubTime subTime;

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public BaseSubTime getSubTime() {
        return subTime;
    }

    public void setSubTime(BaseSubTime subTime) {
        this.subTime = subTime;
    }
}
