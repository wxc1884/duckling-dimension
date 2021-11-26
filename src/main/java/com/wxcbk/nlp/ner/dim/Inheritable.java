package com.wxcbk.nlp.ner.dim;

import java.util.List;


public interface Inheritable {


//    /**
//     * 生成可继承的token
//     * @return
//     */
//    T generateInheritToken();

    /**
     * 获取维度的依赖
     *
     * @return
     */
    List<String> getDependent();

}
