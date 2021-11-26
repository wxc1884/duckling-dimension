package com.wxcbk.nlp.ner.engine;

import com.wxcbk.nlp.ner.Rule.Reference;
import com.wxcbk.nlp.ner.Rule.Predict;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DimContext {
    private String sn;
    private String query;
    private DateTime nowTime;
    private List<String> parsedDim;
    private Map<String, List<Reference>> references;
    private Map<String, Predict> predicts;

    public DimContext(String query) {
        this.query = query;
        this.parsedDim = new ArrayList<>();
        this.references = new HashMap<>();
        this.predicts = new HashMap<>();
    }


    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public Map<String, Predict> getPredicts() {
        return predicts;
    }

    public void setPredicts(Map<String, Predict> predicts) {
        this.predicts = predicts;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public DateTime getNowTime() {
        return nowTime;
    }

    public void setNowTime(DateTime nowTime) {
        this.nowTime = nowTime;
    }

    public List<String> getParsedDim() {
        return parsedDim;
    }

    public void setParsedDim(List<String> parsedDim) {
        this.parsedDim = parsedDim;
    }

    public Map<String, List<Reference>> getReferences() {
        return references;
    }

    public void setReferences(Map<String, List<Reference>> references) {
        this.references = references;
    }
}
