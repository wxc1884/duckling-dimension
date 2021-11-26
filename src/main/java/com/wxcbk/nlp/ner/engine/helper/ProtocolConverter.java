package com.wxcbk.nlp.ner.engine.helper;

import com.wxcbk.nlp.ner.engine.DimConfig;
import com.wxcbk.nlp.ner.engine.protocol.BaseDimData;
import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.engine.protocol.ProtocolAdapter;

import com.wxcbk.nlp.ner.util.FileUtil;
import com.wxcbk.nlp.ner.util.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProtocolConverter {
    private static Logger log = LoggerFactory.getLogger(ProtocolAdapter.class);

    private Map<String, ProtocolAdapter> protocolAdapters;
    private DimConfig dimConfig;

    public ProtocolConverter() {
        this.dimConfig = new DimConfig();
        initProtocolAdapter();
    }

    private void initProtocolAdapter() {
        protocolAdapters = new HashMap<>();
        List<Class<ProtocolAdapter>> protocolAdapterClazz = FileUtil.scanSubClass("com.wxcbk.nlp.ner", ProtocolAdapter.class);
        for (Class<ProtocolAdapter> clazz : protocolAdapterClazz) {
            try {
                ProtocolAdapter adapter = clazz.newInstance();
                protocolAdapters.put(adapter.getDimension(), adapter);
            } catch (Exception e) {
                log.error("init protocolAdapter:{} error:", clazz.getSimpleName(), e);
            }
        }
    }

    public Map<String, List<BaseDimData>> protocolConvert(List<DimToken> dimTokens) {
        Map<String, List<BaseDimData>> baseDims = new HashMap<>();
        if (CollectionUtils.isEmpty(dimTokens)) {
            return baseDims;
        }
        Map<String, List<DimToken>> dimTokenMap = new HashMap<>(4);
        fillDimTokenMap(dimTokenMap, dimTokens, dimConfig);
        dimTokenMap.forEach((k, v) -> baseDims.put(k, protocolAdapters.get(k).convertProtocol(v)));
        return baseDims;
    }

    private void fillDimTokenMap(Map<String, List<DimToken>> dimTokenMap, List<DimToken> dimTokens, DimConfig dimConfig) {
        for (DimToken dimToken : dimTokens) {
            String dimCategory = dimConfig.getDimCategory(dimToken);
            Maps.putKlv(dimTokenMap, dimCategory, dimToken);
        }
    }


}
