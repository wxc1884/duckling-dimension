package com.wxcbk.nlp.ner.dim;

import com.wxcbk.nlp.ner.engine.DimContext;



public interface Dimension extends Inheritable, Referable {
    /**
     * 判断是否是属于这个维度
     *
     * @param dimContext
     * @return
     */
    boolean isDimension(DimContext dimContext);

    /**
     * 获取dimension name
     *
     * @return
     */
    default String dimensionName() {
        return this.getClass().getSimpleName();
    }
}
