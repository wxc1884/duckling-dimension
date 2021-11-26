package com.wxcbk.nlp.ner.dim.time.enums;


public enum Hint {

    /**
     * 被其他token组合了
     */
    COMPOSED,
    /**
     *辅助的token 如最近，未来
     */
    AUX,
    /**
     * 组合的结果
     */
    COMBINE

}
