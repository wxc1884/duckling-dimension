package com.wxcbk.nlp.ner.dim.time.data;


import com.wxcbk.nlp.ner.Rule.MergeRule;
import com.wxcbk.nlp.ner.Rule.Reference;
import com.wxcbk.nlp.ner.Rule.Rule;
import com.wxcbk.nlp.ner.Rule.RuleParser;
import com.wxcbk.nlp.ner.constant.Constants;
import com.wxcbk.nlp.ner.dim.time.enums.Grain;
import com.wxcbk.nlp.ner.dim.time.enums.Hint;
import com.wxcbk.nlp.ner.dim.time.token.Interval;
import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.dim.Dimension;
import com.wxcbk.nlp.ner.dim.time.token.RangeToken;
import com.wxcbk.nlp.ner.engine.DimContext;
import com.wxcbk.nlp.ner.util.Lists;
import com.wxcbk.nlp.ner.util.NumUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RangeTime extends DimToken implements Dimension, RuleParser {
    private String type;
    private RangeToken range;
    private DateTime dateTime;
    private List<DateTime> multiTimes;
    private static List<String> dependents = Lists.of(Num.class.getSimpleName(), DateTime.class.getSimpleName());
    private static Pattern p = Pattern.compile("[今明周和到至~]");
    private static Logger log = LoggerFactory.getLogger(PeriodTime.class);

    private static Rule wRange = new Rule("wRange", "(下|上|这)?(周|星期|礼拜){<weekNum>}[到至](周|星期|礼拜){<weekNum1>}({<num>}天)?", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        int start = NumUtil.chineseToNum(m.group("weekNum"));
        int end = NumUtil.chineseToNum(m.group("weekNum1"));
        RangeToken rangeToken = new RangeToken(Grain.WEEK_DAY, start, end, m);
        RangeTime rangeTime = new RangeTime(p.getRuleName(), rangeToken, m);
        rangeTime.setType("range");
        rangeTime.setHint(Hint.AUX);
        return rangeTime;
    });

    private static Rule todayAndTomorrow = new Rule("todayAndTomorrow", "今明(两天)?", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        Interval tI = new Interval(Grain.DAY, "0", m);
        DateTime today = new DateTime(p.getRuleName(), tI, m);
        Interval tomI = new Interval(Grain.DAY, "1", m);
        DateTime tomorrow = new DateTime(p.getRuleName(), tomI, m);
        List<DateTime> dateTimes = new ArrayList<>();
        dateTimes.add(today);
        dateTimes.add(tomorrow);
        RangeTime rangeTime = new RangeTime(p.getRuleName(), dateTimes, m);
        rangeTime.setType("and");
        return rangeTime;
    });

    @Override
    public boolean isDimension(DimContext dimContext) {
        Matcher m = p.matcher(dimContext.getQuery());
        return (m.find() && Lists.containsOr(dimContext.getParsedDim(), dependents));
    }

    @Override
    public List<MergeRule> getMergeRule() {
        return Lists.of(new MergeRule("weekRange", predict(), production()), new MergeRule("andRange", predict1(), production1()));
    }

    private BiPredicate<Map<String, List<DimToken>>, DimContext> predict() {
        return (tokens, c) -> {
            List<DimToken> dimTokens = tokens.get("DateTime");
            if (CollectionUtils.isEmpty(dimTokens)) {
                return false;
            }
            for (DimToken token : dimTokens) {
                if (((DateTime) token).getHour() != null) {
                    return true;
                }
            }
            return false;
        };
    }

    private BiPredicate<Map<String, List<DimToken>>, DimContext> predict1() {
        return (tokens, c) -> {
            List<DimToken> dimTokens = tokens.get("DateTime");
            return CollectionUtils.isNotEmpty(dimTokens) && dimTokens.size() >= 2;
        };
    }

    private BiFunction<Map<String, List<DimToken>>, DimContext, List<DimToken>> production1() {
        return (tokens, c) -> {
            List<DimToken> rangeTimes = new ArrayList<>();
            List<DateTime> dateTokens = fetchDateTime(tokens.get("DateTime"));
            // {<leftDate>}和{<rightDate>}
            Pattern p = generatePattern(dateTokens);
            if (p == null) {
                return rangeTimes;
            }
            Matcher m = p.matcher(c.getQuery());
            while (m.find()) {
                DateTime leftDt = pickDateTime(m.group("leftDate"), m.start("leftDate"), dateTokens);
                DateTime rightDt = pickDateTime(m.group("rightDate"), m.start("rightDate"), dateTokens);
                mergeRangeTime(leftDt, rightDt, m, rangeTimes);
            }
            return rangeTimes;
        };
    }

    private BiFunction<Map<String, List<DimToken>>, DimContext, List<DimToken>> production() {
        return (tokens, c) -> {
            List<DimToken> rangeTimes = new ArrayList<>();
            List<DateTime> hourTokens = fetchHourDateTime(tokens.get("DateTime"));
            List<RangeTime> rangeTokens = fetchRangeTime(tokens.get("RangeTime"));
            // {<range>}{<hour>}
            Pattern p = generatePattern(hourTokens, rangeTokens);
            if (p == null) {
                return rangeTimes;
            }
            Matcher m = p.matcher(c.getQuery());
            while (m.find()) {
                DateTime dt = pickDateTime(m.group("hour"), m.start("hour"), hourTokens);
                RangeTime rt = pickRangeTime(m.group("range"), m.start("range"), rangeTokens);
                mergeRangeTime(dt, rt, m, rangeTimes);
            }
            return rangeTimes;
        };
    }

    private DateTime pickDateTime(String text, int start, List<DateTime> hourTokens) {
        String index = text + "_" + start;
        for (DateTime dt : hourTokens) {
            String textIndex = dt.getText() + "_" + start;
            if (index.equals(textIndex)) {
                return dt;
            }
        }
        return null;
    }

    private RangeTime pickRangeTime(String text, int start, List<RangeTime> rangeTokens) {
        String index = text + "_" + start;
        for (RangeTime rt : rangeTokens) {
            String textIndex = rt.getText() + "_" + start;
            if (index.equals(textIndex)) {
                return rt;
            }
        }
        return null;
    }

    private void mergeRangeTime(DateTime dt, RangeTime rt, Matcher m, List<DimToken> rangeTimes) {
        if (rt == null) {
            return;
        }
        RangeTime rangeTime = new RangeTime();
        rangeTime.setHint(Hint.COMBINE);
        rangeTime.setText(m.group());
        rangeTime.setStart(m.start());
        rangeTime.setEnd(m.end());
        rangeTime.setDateTime(dt);
        rangeTime.setRange(rt.getRange());
        rangeTime.setType(Constants.DIM_TIME_RT_RANGE);
        List<String> rules = new ArrayList<>(dt.getRules());
        rules.addAll(rt.getRules());
        rangeTime.setRules(rules);
        rangeTimes.add(rangeTime);

    }

    private void mergeRangeTime(DateTime lt, DateTime rt, Matcher m, List<DimToken> rangeTimes) {
        if (rt == null) {
            return;
        }
        RangeTime rangeTime = new RangeTime();
        rangeTime.setHint(Hint.COMBINE);
        rangeTime.setText(m.group());
        rangeTime.setStart(m.start());
        rangeTime.setEnd(m.end());
        rangeTime.setType(Constants.DIM_TIME_RT_AND);
        List<DateTime> dts = Lists.of(lt, rt);
        rangeTime.setMultiTimes(dts);
        List<String> rules = new ArrayList<>(lt.getRules());
        rules.addAll(rt.getRules());
        rangeTime.setRules(rules);
        rangeTimes.add(rangeTime);

    }

    private Pattern generatePattern(List<DateTime> hourTokens, List<RangeTime> rangeTokens) {
        if (CollectionUtils.isEmpty(hourTokens) || CollectionUtils.isEmpty(rangeTokens)) {
            return null;
        }
        List<String> dtTexts = hourTokens.stream().map(DimToken::getText).collect(Collectors.toList());
        List<String> rTexts = rangeTokens.stream().map(DimToken::getText).collect(Collectors.toList());
        try {
            return Pattern.compile("(?<range>" + String.join("|", rTexts) + ")[的]?" + "(?<hour>" + String.join("|", dtTexts) + ")");
        } catch (Exception e) {
            log.error("range ptn compile error:", e);
        }
        return null;
    }

    private Pattern generatePattern(List<DateTime> dateTokens) {
        if (CollectionUtils.isEmpty(dateTokens)) {
            return null;
        }
        List<String> dtTexts = dateTokens.stream().map(DimToken::getText).distinct().sorted(Comparator.comparingInt(String::length).reversed()).collect(Collectors.toList());
        try {
            return Pattern.compile("(?<leftDate>" + String.join("|", dtTexts) + ")和" + "(?<rightDate>" + String.join("|", dtTexts) + ")");
        } catch (Exception e) {
            log.error("range ptn compile error:", e);
        }
        return null;
    }

    private List<DateTime> fetchHourDateTime(List<DimToken> dimTokens) {
        List<DateTime> dateTimes = new ArrayList<>();
        if (CollectionUtils.isEmpty(dimTokens)) {
            return dateTimes;
        }

        for (DimToken token : dimTokens) {
            DateTime dt = (DateTime) token;
            if (dt.getHour() != null) {
                dateTimes.add(dt);
            }
        }
        return dateTimes;
    }

    private List<DateTime> fetchDateTime(List<DimToken> dimTokens) {
        List<DateTime> dateTimes = new ArrayList<>();
        if (CollectionUtils.isEmpty(dimTokens)) {
            return dateTimes;
        }

        for (DimToken token : dimTokens) {
            DateTime dt = (DateTime) token;
            dateTimes.add(dt);
        }
        return dateTimes;
    }

    private List<RangeTime> fetchRangeTime(List<DimToken> rangeTimeTokens) {
        if (CollectionUtils.isEmpty(rangeTimeTokens)) {
            return new ArrayList<>();

        }
        return rangeTimeTokens.stream().map(t -> (RangeTime) t).collect(Collectors.toList());
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

    public RangeTime(String rule, RangeToken token, Matcher m) {
        this();
        this.range = token;
        this.setText(m.group());
        this.setStart(m.start());
        this.setEnd(m.end());
        this.setRules(Lists.of(rule));
    }

    public RangeTime(String rule, List<DateTime> dateTimes, Matcher m) {
        this();
        this.multiTimes = dateTimes;
        this.setText(m.group());
        this.setStart(m.start());
        this.setEnd(m.end());
        this.setRules(Lists.of(rule));
    }

    public RangeTime() {
        this.setDimName(this.dimensionName());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RangeToken getRange() {
        return range;
    }

    public void setRange(RangeToken range) {
        this.range = range;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<DateTime> getMultiTimes() {
        return multiTimes;
    }

    public void setMultiTimes(List<DateTime> multiTimes) {
        this.multiTimes = multiTimes;
    }
}
