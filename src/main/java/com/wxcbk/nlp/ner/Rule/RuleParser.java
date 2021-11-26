package com.wxcbk.nlp.ner.Rule;

import java.util.List;


public interface RuleParser {


    /**
     * 获取mergeRule
     * @return
     */
    List<MergeRule> getMergeRule();
}
