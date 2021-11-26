package com.wxcbk.nlp.ner.dim.time.data;

import com.wxcbk.nlp.ner.Rule.MergeRule;
import com.wxcbk.nlp.ner.Rule.Reference;
import com.wxcbk.nlp.ner.Rule.Rule;
import com.wxcbk.nlp.ner.Rule.RuleParser;
import com.wxcbk.nlp.ner.constant.Constants;
import com.wxcbk.nlp.ner.dim.time.enums.Grain;
import com.wxcbk.nlp.ner.dim.time.enums.Hint;
import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.dim.Dimension;
import com.wxcbk.nlp.ner.dim.time.token.Duration;
import com.wxcbk.nlp.ner.engine.DimContext;
import com.wxcbk.nlp.ner.util.Lists;
import com.wxcbk.nlp.ner.util.Maps;
import com.wxcbk.nlp.ner.util.NumUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author :owen
 * @date :2021/7/28 9:51
 * @Description : 时间量
 */
public class DurationTime extends DimToken implements Dimension, RuleParser {
    private String loop;
    private String direction;
    private Duration year;
    private Duration quarter;
    private Duration month;
    private Duration week;
    private Duration fuzzyWeek;
    private Duration day;
    private Duration hour;
    private Duration minute;
    private Duration second;
    private static List<String> dependents = Lists.of(Num.class.getSimpleName(), Holiday.class.getSimpleName());
    private static Pattern p = Pattern.compile("[年月季周日天午晚晨时点刻分秒]|礼拜|星期");
    private static Logger log = LoggerFactory.getLogger(DurationTime.class);

    private static Rule yQOnly = new Rule("yQOnly", "{<num>}年", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        if (!NumUtil.isIntegerNum(m.group("num"))) {
            return null;
        }
        Duration yearD = new Duration(Grain.YEAR, m.group("num"), m);
        return new DurationTime(p.getRuleName(), yearD, m);
    });

    private static Rule mQOnly = new Rule("mQOnly", "{<num>}个月", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        Duration monthD = new Duration(Grain.MONTH, m.group("num"), m);
        return new DurationTime(p.getRuleName(), monthD, m);
    });

    private static Rule wQOnly = new Rule("wQOnly", "{<num>}(星期|礼拜|周)", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        Duration weekD = new Duration(Grain.WEEK, m.group("num"), m);
        return new DurationTime(p.getRuleName(), weekD, m);

    });

    private static Rule wfQOnly = new Rule("wfQOnly", "{<num>}个工作日", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        Duration weekDayD = new Duration(Grain.WEEK_FUZZY, m.group("num"), m);
        return new DurationTime(p.getRuleName(), weekDayD, m);
    });

    private static Rule dQOnly = new Rule("dQOnly", "{<num>}天", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        Duration dayD = new Duration(Grain.DAY, m.group("num"), m);
        return new DurationTime(p.getRuleName(), dayD, m);
    });

    private static Rule hQOnly = new Rule("hQOnly", "{<num>}个?(半)?小时(半)?", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        Duration hourD = new Duration(Grain.HOUR, m.group("num"), m);
        DurationTime hourDT = new DurationTime(p.getRuleName(), hourD, m);
        if (StringUtils.isNotEmpty(m.group(2)) || StringUtils.isNotEmpty(m.group(3))) {
            hourDT.setMinute(new Duration(Grain.MINUTE, "30"));
        }
        return hourDT;
    });

    private static Rule halfHQOnly = new Rule("halfHQOnly", "半个?小时", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        Duration hourD = new Duration(Grain.MINUTE, "30", m);
        return new DurationTime(p.getRuleName(), hourD, m);
    });

    private static Rule minuteQOnly = new Rule("minuteQOnly", "{<num>}分钟?", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        Duration minuteD = new Duration(Grain.MINUTE, m.group("num"), m);
        return new DurationTime(p.getRuleName(), minuteD, m);

    });

    private static Rule secondQOnly = new Rule("secondQOnly", "{<num>}秒钟?", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        Duration secondD = new Duration(Grain.SECOND, m.group("num"), m);
        return new DurationTime(p.getRuleName(), secondD, m);
    });


    public DurationTime() {
        this.setDimName(DurationTime.class.getSimpleName());
    }

    public DurationTime(String ruleName, Duration token, Matcher m) {
        this.setDimName(DurationTime.class.getSimpleName());
        this.setText(m.group());
        this.setRules(Lists.of(ruleName));
        this.setStart(m.start());
        this.setEnd(m.end());
        this.setToken(token);
    }

    public DurationTime(String ruleName, String direction, Matcher m) {
        this.setDimName(DurationTime.class.getSimpleName());
        this.setText(m.group());
        this.setRules(Lists.of(ruleName));
        this.setStart(m.start());
        this.setEnd(m.end());
        this.setDirection(direction);
    }

    private void setToken(Duration duration) {
        Grain grain = duration.getGrain();
        if (grain == Grain.YEAR) {
            this.year = duration;
        } else if (grain == Grain.MONTH) {
            this.month = duration;
        } else if (grain == Grain.WEEK) {
            this.week = duration;
        } else if (grain == Grain.WEEK_FUZZY) {
            this.fuzzyWeek = duration;
        } else if (grain == Grain.DAY) {
            this.day = duration;
        } else if (grain == Grain.HOUR) {
            this.hour = duration;
        } else if (grain == Grain.MINUTE) {
            this.minute = duration;
        } else if (grain == Grain.SECOND) {
            this.second = duration;
        }
    }


    @Override
    public boolean isDimension(DimContext dimContext) {
        Matcher m = p.matcher(dimContext.getQuery());
        return (m.find() || dimContext.getParsedDim().contains(Holiday.class.getSimpleName()));
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
        return Lists.of(new MergeRule("mergeDuration", predicate(), production()));
    }

    private BiPredicate<Map<String, List<DimToken>>, DimContext> predicate() {
        return (tokens, c) -> {
            List<DimToken> dimTokens = tokens.get("DurationTime");
            List<DimToken> directionTokens = tokens.get("Direction");
            return CollectionUtils.isNotEmpty(dimTokens) && dimTokens.size() >= 2 || CollectionUtils.isNotEmpty(dimTokens) && CollectionUtils.isNotEmpty(directionTokens);
        };
    }

    private BiFunction<Map<String, List<DimToken>>, DimContext, List<DimToken>> production() {
        return (tokens, c) -> {
            Map<String, List<DurationTime>> grain2Token = transGrainToTokens(tokens.get("DurationTime"));
            Map<String, List<Direction>> directionTokens = pickDirectionTokens(tokens.get("Direction"));
            if (MapUtils.isNotEmpty(directionTokens)) {
                grainTokenCombineDirection(c.getQuery(), grain2Token, directionTokens);
            }
            if (grain2Token.size() < 2) {
                return null;
            }
            List<String> grainList = obtainGrains(grain2Token);
            return mergeToken(grainList, grain2Token, c);
        };
    }

    private Map<String, List<Direction>> pickDirectionTokens(List<DimToken> dimTokens) {
        Map<String, List<Direction>> dateTimeMap = new HashMap<>(8);
        if (CollectionUtils.isEmpty(dimTokens)) {
            return dateTimeMap;
        }
        for (DimToken token : dimTokens) {
            Direction dtToken = (Direction) token;
            Maps.putKlv(dateTimeMap, dtToken.getPos(), dtToken);
        }
        return dateTimeMap;
    }

    private void grainTokenCombineDirection(String query, Map<String, List<DurationTime>> grain2Token, Map<String, List<Direction>> directionTokens) {
        Map<String, String> directionPatternStr = fetchDirectionStr(directionTokens);
        Map<String, Direction> textPosToDirectionToken = transTextPos2DirectionToken(directionTokens);
        grain2Token.forEach((k, v) -> {
            for (DurationTime dt : v) {
                String grainWithDirectionPatternStr = createDirectionPatternStr(dt.getText(), directionPatternStr);
                Pattern p;
                try {
                    p = Pattern.compile(grainWithDirectionPatternStr);
                } catch (Exception e) {
                    log.error("compile direction pattern error:", e);
                    return;
                }
                Matcher m = p.matcher(query);
                while (m.find()) {
                    int grainStart = m.start("grain");
                    int grainEnd = m.end("grain");
                    if (grainStart != dt.getStart() || grainEnd != dt.getEnd()) {
                        continue;
                    }
                    if (directionPatternStr.containsKey("suffix")) {
                        String suffixValue = m.group("suffix");
                        int suffixPos = m.end("suffix");
                        if (StringUtils.isNotEmpty(suffixValue) && textPosToDirectionToken.containsKey(suffixValue + "_" + suffixPos)) {
                            Direction direction = textPosToDirectionToken.get(suffixValue + "_" + suffixPos);
                            direction.setHint(Hint.COMPOSED);
                            dt.setDirection(direction.getDirection());
                            dt.setText(m.group());
                            dt.setEnd(suffixPos);
                        }
                    }
                    if (directionPatternStr.containsKey("prefix")) {
                        String prefixValue = m.group("prefix");
                        int prefixPos = m.start("prefix");
                        if (StringUtils.isNotEmpty(prefixValue) && textPosToDirectionToken.containsKey(prefixValue + "_" + prefixPos)) {
                            Direction direction = textPosToDirectionToken.get(prefixValue + "_" + prefixPos);
                            direction.setHint(Hint.COMPOSED);
                            dt.setDirection(direction.getDirection());
                            dt.setText(m.group());
                            dt.setStart(prefixPos);
                        }
                    }
                    break;
                }
            }
        });
    }

    private String createDirectionPatternStr(String grainText, Map<String, String> directionPatternStr) {
        StringBuilder sb = new StringBuilder();
        if (directionPatternStr.containsKey("prefix")) {
            sb.append("(?<prefix>").append(directionPatternStr.get("prefix")).append(")");
        }
        sb.append("(?<grain>").append(grainText).append(")");
        if (directionPatternStr.containsKey("suffix")) {
            sb.append("(?<suffix>").append(directionPatternStr.get("suffix")).append(")");
        }
        return sb.toString();
    }

    private Map<String, String> fetchDirectionStr(Map<String, List<Direction>> directionTokens) {
        Map<String, String> directionStr = new HashMap<>();
        directionTokens.forEach((k, v) -> {
            List<String> directions = new ArrayList<>();
            v.forEach(dt -> {
                if (!directions.contains(dt.getText())) {
                    directions.add(dt.getText());
                }
            });
            directionStr.put(k, String.join("|", directions));
        });
        return directionStr;
    }

    private Map<String, Direction> transTextPos2DirectionToken(Map<String, List<Direction>> directionTokens) {
        Map<String, Direction> textPos2Token = new HashMap<>();
        directionTokens.forEach((k, v) -> {
            if ("prefix".equals(k)) {
                v.forEach(dt -> textPos2Token.put(dt.getText() + "_" + dt.getStart(), dt));
            } else {
                v.forEach(dt -> textPos2Token.put(dt.getText() + "_" + dt.getEnd(), dt));
            }
        });
        return textPos2Token;
    }

    private List<DimToken> mergeToken(List<String> grainList, Map<String, List<DurationTime>> grain2Token, DimContext context) {
        List<DimToken> dimTokens = new ArrayList<>();
        Pattern mergePattern = mergeDatePattern(grainList, grain2Token);
        if (mergePattern == null) {
            return null;
        }
        Matcher m = mergePattern.matcher(context.getQuery());
        Map<String, DurationTime> textPos2Token = transTestPos2Token(grain2Token);
        while (m.find()) {
            DurationTime durationTime = new DurationTime();
            durationTime.setText(m.group());
            durationTime.setStart(m.start());
            durationTime.setEnd(m.end());
            durationTime.setRules(new ArrayList<>());
            durationTime.setHint(Hint.COMBINE);
            List<DurationTime> canCombineToken = selectCombineToken(m, grainList, textPos2Token);
            combineToken(durationTime, canCombineToken);
            dimTokens.add(durationTime);
        }
        return dimTokens;
    }

    private Map<String, DurationTime> transTestPos2Token(Map<String, List<DurationTime>> grain2Token) {
        Map<String, DurationTime> textPos2Token = new HashMap<>();
        grain2Token.values().forEach(dateTimes -> {
            dateTimes.forEach(dt -> textPos2Token.put(dt.getText() + "_" + dt.getStart(), dt));
        });
        return textPos2Token;
    }

    private List<DurationTime> selectCombineToken(Matcher m, List<String> grainList, Map<String, DurationTime> textPos2Token) {
        List<DurationTime> canTokens = new ArrayList<>();
        for (String grain : grainList) {
            String value = m.group(grain);
            if (value != null) {
                DurationTime dt = textPos2Token.get(value + "_" + m.start(grain));
                if (dt != null) {
                    canTokens.add(dt);
                }
            }
        }
        return canTokens;
    }

    private void combineToken(DurationTime combineToken, List<DurationTime> canComposeToken) {
        if (CollectionUtils.isEmpty(canComposeToken)) {
            return;
        }
        canComposeToken.forEach(can -> copyCanToCombineToken(combineToken, can));

    }

    private void copyCanToCombineToken(DurationTime combineToken, DurationTime candiToken) {
        if (candiToken.getYear() != null) {
            combineToken.setYear(candiToken.getYear());
        }
        if (candiToken.getQuarter() != null) {
            combineToken.setQuarter(candiToken.getQuarter());
        }
        if (candiToken.getMonth() != null) {
            combineToken.setMonth(candiToken.getMonth());
        }
        if (candiToken.getWeek() != null) {
            combineToken.setWeek(candiToken.getWeek());
        }
        if (candiToken.getFuzzyWeek() != null) {
            combineToken.setFuzzyWeek(candiToken.getFuzzyWeek());
        }
        if (candiToken.getWeek() != null) {
            combineToken.setWeek(candiToken.getWeek());
        }
        if (candiToken.getDay() != null) {
            combineToken.setDay(candiToken.getDay());
        }
        if (candiToken.getHour() != null) {
            combineToken.setHour(candiToken.getHour());
        }
        if (candiToken.getMinute() != null) {
            combineToken.setMinute(candiToken.getMinute());
        }
        if (candiToken.getSecond() != null) {
            combineToken.setSecond(candiToken.getSecond());
        }
        candiToken.setHint(Hint.COMPOSED);
        combineToken.getRules().addAll(candiToken.getRules());
    }


    private List<String> obtainGrains(Map<String, List<DurationTime>> grain2Token) {
        List<String> list = new ArrayList<>(grain2Token.keySet());
        list.sort(Comparator.comparingInt(Grain::getGrainLevel));
        return list;
    }

    private Map<String, List<DurationTime>> transGrainToTokens(List<DimToken> dimTokens) {
        Map<String, List<DurationTime>> dateTimeMap = new HashMap<>(8);
        if (CollectionUtils.isEmpty(dimTokens)) {
            return dateTimeMap;
        }
        for (DimToken token : dimTokens) {
            DurationTime qtToken = (DurationTime) token;
            String grain = fetchTokenGrain(qtToken);
            if (!grain.equals(Constants.NO_DT)) {
                Maps.putKlv(dateTimeMap, grain, qtToken);
            }
        }
        return dateTimeMap;
    }

    private Pattern mergeDatePattern(List<String> grainList, Map<String, List<DurationTime>> grain2Token) {
        String patternStr = generatePatternStr(grainList, grain2Token);
        try {
            return Pattern.compile(patternStr);
        } catch (Exception e) {
            log.error("dateTime mergeRule compile error:", e);
        }
        return null;
    }

    private String generatePatternStr(List<String> grainList, Map<String, List<DurationTime>> grain2Token) {
        List<String> ptnList = new ArrayList<>();
        int size = grainList.size();
        for (int i = size - 1; i >= 0; i--) {
            String groupName = grainList.get(i);
            List<String> matchList = fetchMatchList(grain2Token.get(groupName));
            if (i == 0 || (i == 1 && size > 1)) {
                ptnList.add("(?<" + groupName + ">" + String.join("|", matchList) + ")");
            } else {
                ptnList.add("(?<" + groupName + ">" + String.join("|", matchList) + ")?");
            }
        }
        return String.join("[零又]?", ptnList);
    }

    private List<String> fetchMatchList(List<DurationTime> durationTimes) {
        List<String> matchList = new ArrayList<>();
        durationTimes.forEach(dt -> matchList.add(dt.getText()));
        return matchList;
    }

    private String fetchTokenGrain(DurationTime duration) {
        if (duration.getSecond() != null) {
            return Constants.SECOND;
        } else if (duration.getMinute() != null) {
            return Constants.MINUTE;
        } else if (duration.getHour() != null) {
            return Constants.HOUR;
        } else if (duration.getDay() != null || duration.getFuzzyWeek() != null) {
            return Constants.DAY;
        } else if (duration.getWeek() != null) {
            return Constants.WEEK;
        } else if (duration.getMonth() != null) {
            return Constants.MONTH;
        } else if (duration.getQuarter() != null) {
            return Constants.QUARTER;
        } else if (duration.getYear() != null) {
            return Constants.YEAR;
        }
        return Constants.NO_DT;
    }

    public List<String> fetchValidGrain() {
        List<String> grains = new ArrayList<>();
        if (year != null) {
            grains.add(Constants.YEAR);
        }
        if (month != null) {
            grains.add(Constants.MONTH);
        }
        if (week != null) {
            grains.add(Constants.WEEK);
        }
        if (fuzzyWeek != null) {
            grains.add(Constants.WEEK_FUZZY);
        }
        if (day != null) {
            grains.add(Constants.DAY);
        }
        if (hour != null) {
            grains.add(Constants.HOUR);
        }
        if (minute != null) {
            grains.add(Constants.MINUTE);
        }
        if (second != null) {
            grains.add(Constants.SECOND);
        }
        return grains;
    }


    public String getLoop() {
        return loop;
    }

    public void setLoop(String loop) {
        this.loop = loop;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Duration getYear() {
        return year;
    }

    public void setYear(Duration year) {
        this.year = year;
    }

    public Duration getQuarter() {
        return quarter;
    }

    public void setQuarter(Duration quarter) {
        this.quarter = quarter;
    }

    public Duration getMonth() {
        return month;
    }

    public void setMonth(Duration month) {
        this.month = month;
    }

    public Duration getWeek() {
        return week;
    }

    public void setWeek(Duration week) {
        this.week = week;
    }

    public Duration getDay() {
        return day;
    }

    public void setDay(Duration day) {
        this.day = day;
    }

    public Duration getHour() {
        return hour;
    }

    public void setHour(Duration hour) {
        this.hour = hour;
    }

    public Duration getMinute() {
        return minute;
    }

    public void setMinute(Duration minute) {
        this.minute = minute;
    }

    public Duration getSecond() {
        return second;
    }

    public void setSecond(Duration second) {
        this.second = second;
    }

    public static List<String> getDependents() {
        return dependents;
    }

    public static void setDependents(List<String> dependents) {
        DurationTime.dependents = dependents;
    }

    public Duration getFuzzyWeek() {
        return fuzzyWeek;
    }

    public void setFuzzyWeek(Duration fuzzyWeek) {
        this.fuzzyWeek = fuzzyWeek;
    }
}
