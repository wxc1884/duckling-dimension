package com.wxcbk.nlp.ner.dim.time.data;

import com.wxcbk.nlp.ner.Rule.MergeRule;
import com.wxcbk.nlp.ner.Rule.Reference;
import com.wxcbk.nlp.ner.Rule.Rule;
import com.wxcbk.nlp.ner.Rule.RuleParser;
import com.wxcbk.nlp.ner.dim.time.enums.Hint;
import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.dim.Dimension;
import com.wxcbk.nlp.ner.engine.DimContext;
import com.wxcbk.nlp.ner.util.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PeriodTime extends DimToken implements Dimension, RuleParser {
    private DateTime from;
    private DateTime to;
    private static List<String> dependents = Lists.of(Num.class.getSimpleName(), DateTime.class.getSimpleName());
    private static Pattern p = Pattern.compile("[到至~]");
    private static Logger log = LoggerFactory.getLogger(PeriodTime.class);


    public PeriodTime(DateTime from, DateTime to, Matcher m) {
        this();
        this.setText(m.group());
        this.setStart(m.start());
        this.setEnd(m.end());
        this.from = from;
        this.to = to;
    }

    public PeriodTime() {
        this.setDimName(this.dimensionName());
    }

    @Override
    public boolean isDimension(DimContext dimContext) {
        Matcher m = p.matcher(dimContext.getQuery());
        return (m.find() && Lists.containsOr(dimContext.getParsedDim(), dependents));
    }

    @Override
    public List<String> getDependent() {
        return dependents;
    }

    @Override
    public List<Reference> initRefer() {
        return null;
    }

    @Override
    public Reference generateRefer(Rule rule, DimContext dimContext) {
        return null;
    }

    @Override
    public List<MergeRule> getMergeRule() {
        return Lists.of(new MergeRule("mergePeriod", predict(), production()));
    }

    private BiPredicate<Map<String, List<DimToken>>, DimContext> predict() {
        return (tokens, c) -> {
            List<DimToken> dimTokens = tokens.get("DateTime");
            return CollectionUtils.isNotEmpty(dimTokens) && dimTokens.size() >= 2;
        };
    }

    private BiFunction<Map<String, List<DimToken>>, DimContext, List<DimToken>> production() {
        return (tokens, c) -> {
            List<DimToken> periodTokens = new ArrayList<>();
            List<DimToken> dimTokens = tokens.get("DateTime");
            //{<dateTime>}(和|到|至|~){<dateTime1>}
            Pattern p = generatePeriodPattern(dimTokens);
            if (p == null) {
                return periodTokens;
            }
            Matcher m = p.matcher(c.getQuery());
            while (m.find()) {
                DateTime fromDateTime = pickDateTime(m.group("dateTime"), m.start("dateTime"), dimTokens);
                DateTime toDateTime = pickDateTime(m.group("dateTime1"), m.start("dateTime1"), dimTokens);
                mergePeriodToken(fromDateTime, toDateTime, m, periodTokens);
            }
            return periodTokens;
        };
    }

    private DateTime pickDateTime(String text, int start, List<DimToken> dimTokens) {
        String index = text + "_" + start;
        for (DimToken token : dimTokens) {
            String tokenIndex = token.getText() + "_" + token.getStart();
            if (tokenIndex.equals(index)) {
                return (DateTime) token;
            }
        }
        return null;
    }

    private Pattern generatePeriodPattern(List<DimToken> dimTokens) {
        List<String> tokenTexts = obtainText(dimTokens);
        if (CollectionUtils.isEmpty(tokenTexts)) {
            return null;
        }
        tokenTexts.sort(Comparator.comparingInt(String::length).reversed());
        try {
            return Pattern.compile("(?<dateTime>" + String.join("|", tokenTexts) + ")[到至~]" + "(?<dateTime1>" + String.join("|", tokenTexts) + ")");
        } catch (Exception e) {
            log.error("period ptn compile error:", e);
        }
        return null;
    }

    private List<String> obtainText(List<DimToken> dimTokens) {
        List<String> texts = new ArrayList<>();
        for (DimToken token : dimTokens) {
            texts.add(token.getText());
        }
        return texts;
    }

    private void mergePeriodToken(DateTime from, DateTime to, Matcher m, List<DimToken> periodTokens) {
        if (from == null || to == null) {
            return;
        }
        from.setHint(Hint.COMPOSED);
        List<String> rules = new ArrayList<>(from.getRules());
        to.setHint(Hint.COMPOSED);
        rules.addAll(to.getRules());
        PeriodTime periodTime = new PeriodTime(from, to, m);
        periodTime.setHint(Hint.COMBINE);
        periodTime.setRules(rules);
        periodTokens.add(periodTime);
    }

    public DateTime getFrom() {
        return from;
    }

    public void setFrom(DateTime from) {
        this.from = from;
    }

    public DateTime getTo() {
        return to;
    }

    public void setTo(DateTime to) {
        this.to = to;
    }
}
