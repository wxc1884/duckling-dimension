package com.wxcbk.nlp.ner.dim.time.data;

import com.wxcbk.nlp.ner.Rule.Reference;
import com.wxcbk.nlp.ner.Rule.Rule;
import com.wxcbk.nlp.ner.dim.time.helper.HolidayProvider;
import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.dim.Dimension;
import com.wxcbk.nlp.ner.engine.DimContext;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;


public class Holiday extends DimToken implements Dimension {


    @Override
    public List<Reference> initRefer() {
        return null;
    }

    @Override
    public Reference generateRefer(Rule rule, DimContext dimContext) {
        return null;
    }

    @Override
    public boolean isDimension(DimContext dimContext) {
        List<String> holidays = HolidayProvider.findHolidayToken(dimContext.getQuery());
        if (CollectionUtils.isNotEmpty(holidays)) {
            Reference refer = new Reference("holiday", String.join("|", holidays));
            List<Reference> list = new ArrayList<>();
            list.add(refer);
            dimContext.getReferences().put("holiday", list);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getDependent() {
        return null;
    }
}
