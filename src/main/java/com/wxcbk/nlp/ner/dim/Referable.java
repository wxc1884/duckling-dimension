package com.wxcbk.nlp.ner.dim;

import com.wxcbk.nlp.ner.Rule.Reference;
import com.wxcbk.nlp.ner.Rule.Rule;
import com.wxcbk.nlp.ner.engine.DimContext;

import java.util.List;


public interface Referable {


    /**
     * 生成引用对象
     *
     * @return
     */
    List<Reference> initRefer();


    /**
     * 生成索引规则
     * @param rule
     * @param dimContext
     * @return
     */
    Reference generateRefer(Rule rule, DimContext dimContext);
}
