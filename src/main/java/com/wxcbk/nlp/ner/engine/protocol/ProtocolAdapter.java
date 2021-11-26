package com.wxcbk.nlp.ner.engine.protocol;

import com.wxcbk.nlp.ner.token.DimToken;

import java.util.List;


public interface ProtocolAdapter {


    /**
     * 协议转化
     * @param tokens
     * @return
     */
    List<BaseDimData> convertProtocol(List<DimToken> tokens);


    /**
     * 获取维度
     * @return
     */
    String getDimension();
}
