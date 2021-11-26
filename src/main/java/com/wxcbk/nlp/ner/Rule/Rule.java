package com.wxcbk.nlp.ner.Rule;


import com.wxcbk.nlp.ner.enums.ReferType;
import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.engine.DimContext;
import com.wxcbk.nlp.ner.util.Strings;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Rule {
    private static Logger log = LoggerFactory.getLogger(Rule.class);
    private String timeDim;
    private String name;
    private String regex;
    private Function<ProductOption, DimToken> production;
    private RulePattern rulePattern;


    public Rule(String name, String regex, Function<ProductOption, DimToken> production) {
        this.name = name;
        this.regex = regex;
        this.production = production;
        this.rulePattern = new RulePattern(regex);
    }

    public List<DimToken> produce(Predict predict, DimContext dimContext) {
        List<DimToken> dimTokens = new ArrayList<>();
        Matcher m = predict.getMatcher();
        while (m.find()) {
            DimToken dimToken = this.production.apply(new ProductOption(name, predict, dimContext));
            if (dimToken != null) {
                dimTokens.add(dimToken);
            }
        }
        return dimTokens;
    }

    public boolean predict(DimContext context) {
        Map<String, List<Reference>> referMap = context.getReferences();
        for (String group : rulePattern.getReferGroupNames()) {
            if (!referMap.containsKey(Strings.trimNum(group))) {
                return false;
            }
        }
        Pattern p = generatePattern(context, rulePattern);
        if (p == null) {
            return false;
        }
        Matcher m = p.matcher(context.getQuery());
        if (m.find()) {
            Predict predict = new Predict();
            predict.setRuleName(this.name);
            predict.setRegex(p.toString());
            predict.setMatcher(m.reset());
            context.getPredicts().put(this.name, predict);
            return true;
        }
        return false;

    }

    private Pattern generatePattern(DimContext context, RulePattern rulePattern) {
        if (CollectionUtils.isEmpty(rulePattern.getReferGroupNames())) {
            return rulePattern.getPattern();
        } else {
            String regex = rulePattern.getOriginRegex();
            for (String refer : rulePattern.getReferGroupNames()) {
                List<String> rs = new ArrayList<>();
                List<Reference> references = context.getReferences().get(Strings.trimNum(refer));
                if (CollectionUtils.isEmpty(references)) {
                    return null;
                }
                if (references.size() == 1 && references.get(0).getReferType() == ReferType.PATTERN) {
                    return references.get(0).getPattern();
                }
                references.forEach(r -> rs.add(r.getRegex()));
                regex = regex.replace("{<" + refer + ">}", "(?<" + refer + ">" + String.join("|", rs) + ")");
            }
            try {
                return Pattern.compile(regex);
            } catch (Exception e) {
                log.error("error");
            }

        }
        return null;
    }

    public String getTimeDim() {
        return timeDim;
    }

    public void setTimeDim(String timeDim) {
        this.timeDim = timeDim;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public RulePattern getRulePattern() {
        return rulePattern;
    }

    public void setRulePattern(RulePattern rulePattern) {
        this.rulePattern = rulePattern;
    }
}
