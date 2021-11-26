package com.wxcbk.nlp.ner.engine;

import com.wxcbk.nlp.ner.constant.Constants;
import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.util.Strings;


public class DimConfig {

    public String getDimCategory(DimToken dimToken) {
        String dimName = dimToken.getDimName();
        if (Strings.orEquals(dimName, Constants.DIM_TIME_QT, Constants.DIM_TIME_RT, Constants.DIM_TIME_PT, Constants.DIM_TIME_DT)) {
            return Constants.DIM_TIME;
        }
        return dimName;
    }
}
