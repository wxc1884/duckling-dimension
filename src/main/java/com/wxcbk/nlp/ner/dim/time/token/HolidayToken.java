package com.wxcbk.nlp.ner.dim.time.token;

import com.wxcbk.nlp.ner.dim.time.enums.Grain;
import com.wxcbk.nlp.ner.dim.time.enums.TimeDim;
import com.wxcbk.nlp.ner.dim.time.enums.TokenType;


public class HolidayToken extends BaseToken {
    private String holiday;
    private String interval;

    public HolidayToken(Grain grain, String grainValue, String text, int start, int end) {
        this.setType(TokenType.T_POINT);
        this.setTimeDim(TimeDim.DT);
        this.setGrain(grain);
        this.setGrainValue(grainValue);
        this.setText(text);
        this.setHoliday(grainValue);
        this.setStart(start);
        this.setEnd(end);
    }

    public String getHoliday() {
        return holiday;
    }

    public void setHoliday(String holiday) {
        this.holiday = holiday;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }
}
