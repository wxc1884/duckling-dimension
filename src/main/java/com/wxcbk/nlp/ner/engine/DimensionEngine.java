package com.wxcbk.nlp.ner.engine;

import com.wxcbk.nlp.ner.Rule.MergeRule;
import com.wxcbk.nlp.ner.Rule.Reference;
import com.wxcbk.nlp.ner.Rule.Rule;
import com.wxcbk.nlp.ner.Rule.RuleParser;
import com.wxcbk.nlp.ner.constant.Constants;
import com.wxcbk.nlp.ner.dim.Dimension;
import com.wxcbk.nlp.ner.dim.time.enums.Hint;
import com.wxcbk.nlp.ner.dim.time.helper.HolidayProvider;
import com.wxcbk.nlp.ner.engine.helper.DimensionManager;
import com.wxcbk.nlp.ner.engine.helper.ProtocolConverter;
import com.wxcbk.nlp.ner.engine.protocol.BaseDimData;
import com.wxcbk.nlp.ner.enums.DimType;
import com.wxcbk.nlp.ner.token.CombineItem;
import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.util.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class DimensionEngine {

    private static Logger log = LoggerFactory.getLogger(DimensionEngine.class);
    private DimensionManager manager;
    private ProtocolConverter protocolConverter;


    public DimensionEngine() {
        this.manager = new DimensionManager();
        this.protocolConverter = new ProtocolConverter();
        init();
    }

    private void init() {
        HolidayProvider.findHolidayToken(Constants.TIME_TEST);
    }

    public Map<String, List<BaseDimData>> regDimension(String query) {
        List<DimToken> parseTokens = parseDim(query);
        if (CollectionUtils.isEmpty(parseTokens)) {
            return null;
        }
        return protocolConverter.protocolConvert(parseTokens);
    }


    private List<DimToken> parseDim(String query) {
        DimContext context = new DimContext(query);
        List<Dimension> dimensions = searchDimension(context, manager.getDependencies(), manager.getName2Dim());
        List<DimToken> dimTokens = filterRule(context, manager.getDim2Rules(), dimensions);
        List<DimToken> mergeTokens = mergeRule(dimTokens, context, dimensions);
        return arrangeToken(dimTokens, mergeTokens);
    }

    private List<DimToken> filterRule(DimContext dimContext, Map<String, List<Rule>> dim2Rules, List<Dimension> dimensions) {
        if (CollectionUtils.isEmpty(dimensions)) {
            return null;
        }
        List<Rule> rules = parseStringToRule(dimContext, dimensions, dim2Rules);
        return produceToken(rules, dimContext);
    }

    private List<DimToken> mergeRule(List<DimToken> dimTokens, DimContext context, List<Dimension> dimensions) {
        if (CollectionUtils.isEmpty(dimensions)) {
            return null;
        }
        if (CollectionUtils.isEmpty(dimTokens)) {
            return null;
        }
        List<DimToken> combineTokens = new ArrayList<>();
        Map<String, List<DimToken>> dim2Tokens = transDimToTokens(dimTokens);
        for (Dimension dim : dimensions) {
            List<MergeRule> rules = null;
            if (dim instanceof RuleParser) {
                RuleParser ruleParser = (RuleParser) dim;
                rules = ruleParser.getMergeRule();
            }
            if (CollectionUtils.isEmpty(rules)) {
                continue;
            }
            for (MergeRule rule : rules) {
                if (!rule.predict(dim2Tokens, context)) {
                    continue;
                }
                List<DimToken> dimMergeTokens = rule.produce(dim2Tokens, context);
                if (CollectionUtils.isNotEmpty(dimMergeTokens)) {
                    addDim2Token(dim, dim2Tokens, dimMergeTokens);
                    combineTokens.addAll(dimMergeTokens);
                }
            }

        }
        return combineTokens;
    }

    private void addDim2Token(Dimension dim, Map<String, List<DimToken>> dim2Tokens, List<DimToken> dimMergeTokens) {
        String dimName = dim.dimensionName();
        if (dim2Tokens.containsKey(dimName)) {
            dim2Tokens.get(dimName).addAll(dimMergeTokens);
        } else {
            dim2Tokens.put(dimName, dimMergeTokens);
        }
    }

    private Map<String, List<DimToken>> transDimToTokens(List<DimToken> dimTokens) {
        Map<String, List<DimToken>> dim2Tokens = new HashMap<>();
        for (DimToken dimToken : dimTokens) {
            Maps.putKlv(dim2Tokens, dimToken.getDimName(), dimToken);
        }
        return dim2Tokens;
    }

    private List<DimToken> arrangeToken(List<DimToken> dimTokens, List<DimToken> mergeTokens) {
        if (CollectionUtils.isEmpty(dimTokens) && CollectionUtils.isEmpty(dimTokens)) {
            return null;
        }
        List<DimToken> candidateTokens = toOneList(dimTokens, mergeTokens);
        candidateTokens.forEach(can -> log.info("parse dimToken:{}", can.toString()));
        pruneToken(candidateTokens);
        return candidateTokens;
    }

    private void pruneToken(List<DimToken> candidateTokens) {
        List<DimToken> removes = new ArrayList<>();
        List<CombineItem> combineItems = transCombineItem(candidateTokens);
        pickPruneToken(removes, combineItems);
        removes.forEach(candidateTokens::remove);
    }

    private List<CombineItem> transCombineItem(List<DimToken> candidateTokens) {
        List<CombineItem> combineItems = new ArrayList<>();
        candidateTokens.forEach(can -> {
            CombineItem item = new CombineItem();
            item.setPrior(DimType.getWeight(can.getDimName()));
            item.setStart(can.getStart());
            item.setEnd(can.getEnd());
            item.setTextLength(can.getEnd() - can.getStart());
            item.setDimToken(can);
            item.setHint(can.getHint());
            combineItems.add(item);
        });
        return combineItems;

    }

    private void pickPruneToken(List<DimToken> removes, List<CombineItem> combineItems) {
        sortByLengthAndWeight(combineItems);
        List<CombineItem> retainItems = new ArrayList<>();
        boolean isRemove;
        for (CombineItem canItem : combineItems) {
            isRemove = false;
            if (canItem.getHint() == Hint.COMPOSED || canItem.getHint() == Hint.AUX) {
                removes.add(canItem.getDimToken());
                continue;
            }
            for (CombineItem retain : retainItems) {
                if (retain.getTextLength() >= canItem.getTextLength()) {
                    if (retain.getStart() <= canItem.getStart() && retain.getEnd() >= canItem.getEnd()) {
                        isRemove = true;
                        removes.add(canItem.getDimToken());
                    }
                }
            }
            if (!isRemove) {
                retainItems.add(canItem);
            }
        }
    }

    private void sortByLengthAndWeight(List<CombineItem> combineItems) {
        combineItems.sort((e1, e2) -> {
            if (e1.getPrior() > e2.getPrior()) {
                return -1;
            } else if (e1.getPrior() < e2.getPrior()) {
                return 1;
            } else {
                return Integer.compare(e2.getTextLength(), e1.getTextLength());
            }
        });
    }


    private List<DimToken> toOneList(List<DimToken> dimTokens, List<DimToken> mergeTokens) {
        List<DimToken> tokens = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dimTokens)) {
            tokens.addAll(dimTokens);
        }
        if (CollectionUtils.isNotEmpty(mergeTokens)) {
            tokens.addAll(mergeTokens);
        }
        return tokens;
    }


    private List<Dimension> searchDimension(DimContext dimContext, List<List<String>> dependencies, Map<String, Dimension> name2Dim) {
        List<Dimension> list = new ArrayList<>();
        for (List<String> dimList : dependencies) {
            for (String dim : dimList) {
                if (name2Dim.get(dim).isDimension(dimContext)) {
                    dimContext.getParsedDim().add(dim);
                    list.add(name2Dim.get(dim));
                    List<Reference> references = name2Dim.get(dim).initRefer();
                    addContext(references, dimContext);
                }
            }
        }
        return list;
    }

    private List<DimToken> produceToken(List<Rule> rules, DimContext context) {
        List<DimToken> dimTokens = new ArrayList<>();
        for (Rule rule : rules) {
            List<DimToken> tokens = rule.produce(context.getPredicts().get(rule.getName()), context);
            dimTokens.addAll(tokens);
        }
        return dimTokens;
    }

    private List<Rule> parseStringToRule(DimContext dimContext, List<Dimension> timeDimensions, Map<String, List<Rule>> dim2Rules) {
        List<Rule> validRule = new ArrayList<>();
        for (Dimension dim : timeDimensions) {
            List<Rule> rules = dim2Rules.get(dim.dimensionName());
            if (CollectionUtils.isEmpty(rules)) {
                continue;
            }
            for (Rule rule : rules) {
                if (rule.predict(dimContext)) {
                    validRule.add(rule);
                    Reference ref = dim.generateRefer(rule, dimContext);
                    addContext(ref, dimContext);
                }
            }
        }
        return validRule;
    }

    private void addContext(List<Reference> references, DimContext dimContext) {
        if (CollectionUtils.isEmpty(references)) {
            return;
        }
        Map<String, List<Reference>> refs = dimContext.getReferences();
        references.forEach(r -> Maps.putKlv(refs, r.getName(), r));
    }

    private void addContext(Reference reference, DimContext dimContext) {
        if (reference == null) {
            return;
        }
        Map<String, List<Reference>> refs = dimContext.getReferences();
        Maps.putKlv(refs, reference.getName(), reference);
    }
}
