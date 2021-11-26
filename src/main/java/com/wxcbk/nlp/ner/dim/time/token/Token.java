package com.wxcbk.nlp.ner.dim.time.token;

import com.wxcbk.nlp.ner.dim.time.enums.TimeDim;
import com.wxcbk.nlp.ner.dim.time.enums.TokenType;


public abstract class Token {
    private TimeDim timeDim;
    private TokenType tokenType;
    private String text;
    private int start;
    private int end;

    public TimeDim getTimeDim() {
        return timeDim;
    }

    public void setTimeDim(TimeDim timeDim) {
        this.timeDim = timeDim;
    }

    public TokenType getType() {
        return tokenType;
    }

    public void setType(TokenType type) {
        this.tokenType = type;
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
}
