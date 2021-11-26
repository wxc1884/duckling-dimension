package com.wxcbk.nlp.ner.dim.time.data;

import com.wxcbk.nlp.ner.Rule.MergeRule;
import com.wxcbk.nlp.ner.Rule.Reference;
import com.wxcbk.nlp.ner.Rule.Rule;
import com.wxcbk.nlp.ner.Rule.RuleParser;
import com.wxcbk.nlp.ner.constant.Constants;
import com.wxcbk.nlp.ner.dim.Dimension;
import com.wxcbk.nlp.ner.dim.time.enums.*;
import com.wxcbk.nlp.ner.dim.time.helper.HolidayProvider;
import com.wxcbk.nlp.ner.dim.time.token.*;
import com.wxcbk.nlp.ner.engine.DimContext;
import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.util.Lists;
import com.wxcbk.nlp.ner.util.Maps;
import com.wxcbk.nlp.ner.util.NumUtil;
import com.wxcbk.nlp.ner.util.TimeUtil;
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



public class DateTime extends DimToken implements Dimension, RuleParser {
    private Loop loop;
    private String direction;
    private boolean lunar;
    private BaseToken holiday;
    private BaseToken year;
    private BaseToken quarter;
    private BaseToken month;
    private BaseToken fuzzyMonth;
    private BaseToken fuzzyWeek;
    private BaseToken week;
    private BaseToken weekDay;
    private BaseToken day;
    private BaseToken fuzzyDay;
    private BaseToken hour;
    private BaseToken minute;
    private BaseToken second;
    private static List<String> dependents = Lists.of(Num.class.getSimpleName(), Holiday.class.getSimpleName());
    private static Pattern p = Pattern.compile("[年月季周日今昨前后午晚昏晨时点刻分秒\\-:]|礼拜|星期");
    private static Logger log = LoggerFactory.getLogger(DateTime.class);

    private static Rule loopT = new Rule("loopT", "每个?(年|月|(周|星期|礼拜)|天)", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        Grain grain = Grain.extractGrain(m.group(1));
        TmPoint tp = new TmPoint(grain, Constants.GRAIN_NO_VALUE, m);
        DateTime dateTime = new DateTime(p.getRuleName(), tp, m);
        dateTime.setLoop(Loop.extractLoop(grain.getName()));
        return dateTime;
    });

    private static Rule lunarOnly = new Rule("lunar", "[农阴]历", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        DateTime dateTime = new DateTime(p.getRuleName(), m.group(), m.start(), m.end());
        dateTime.setLunar(true);
        dateTime.setHint(Hint.AUX);
        return dateTime;
    });

    private static Rule lunarOneMonth = new Rule("lunarOneMonth", "正月", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint tp = new TmPoint(Grain.MONTH, "1", m);
        DateTime dateTime = new DateTime(p.getRuleName(), tp, m);
        dateTime.setLunar(true);
        return dateTime;
    });

    private static Rule lunar12Month = new Rule("lunar12Month", "腊月", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint tp = new TmPoint(Grain.MONTH, "12", m);
        DateTime dateTime = new DateTime(p.getRuleName(), tp, m);
        dateTime.setLunar(true);
        return dateTime;
    });

    private static Rule lunarDay = new Rule("lunarDay", "初{<dayNum>}", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String dayNum = m.group("dayNum");
        if (StringUtils.isNotEmpty(dayNum)) {
            Integer num = NumUtil.parseInteger(dayNum);
            if (num > 0 && num < 11) {
                TmPoint tp = new TmPoint(Grain.DAY, dayNum, m);
                DateTime dateTime = new DateTime(p.getRuleName(), tp, m);
                dateTime.setHint(Hint.AUX);
                dateTime.setLunar(true);
                return dateTime;
            }
        }
        return null;
    });

    private static Rule lunarDay1 = new Rule("lunarDay1", "{<dayNum>}", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String dayNum = m.group("dayNum");
        if (StringUtils.isNotEmpty(dayNum)) {
            Integer num = NumUtil.parseInteger(dayNum);
            if (num > 11 && num < 30) {
                TmPoint tp = new TmPoint(Grain.DAY, dayNum, m);
                DateTime dateTime = new DateTime(p.getRuleName(), tp, m);
                dateTime.setHint(Hint.AUX);
                dateTime.setLunar(true);
                return dateTime;
            }
        }
        return null;
    });

    private static Rule loopHoliday = new Rule("loopHoliday", "每?个?(法定)?节假日", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint tp = new TmPoint(Grain.DAY, Constants.GRAIN_NO_VALUE, m);
        DateTime dateTime = new DateTime(p.getRuleName(), tp, m);
        dateTime.setLoop(Loop.LOOP_HOLIDAY);
        return dateTime;
    });

    private static Rule weekDays = new Rule("weekDays", "(每)?个?工作日", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint weekDays = new TmPoint(Grain.WEEK_FUZZY, new FuzzyToken(Constants.FUZZY_TYPE_WEEK, FuzzyWeek.WEEK_DAYS.getName()), m);
        DateTime dateTime = new DateTime(p.getRuleName(), weekDays, m);
        if (StringUtils.isNotEmpty(m.group(1))) {
            dateTime.setLoop(Loop.LOOP_WEEK_DAYS);
        }
        return dateTime;
    });

    private static Rule weekEnd = new Rule("weekEnd", "(每)?个?周末", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint weekEnd = new TmPoint(Grain.WEEK_FUZZY, new FuzzyToken(Constants.FUZZY_TYPE_WEEK, FuzzyWeek.WEEK_END.getName()), m);
        DateTime dateTime = new DateTime(p.getRuleName(), weekEnd, m);
        if (StringUtils.isNotEmpty(m.group(1))) {
            dateTime.setLoop(Loop.LOOP_WEEK_END);
        }
        return dateTime;
    });

    private static Rule loopWT = new Rule("loopWT", "每个?(周|星期|礼拜){<weekNum>}(?!点|时)", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String value = m.group("weekNum");
        TmPoint tp = new TmPoint(Grain.WEEK_DAY, value, m);
        DateTime dateTime = new DateTime(p.getRuleName(), tp, m);
        dateTime.setLoop(Loop.extractLoop(Grain.WEEK.getName()));
        return dateTime;
    });

    private static Rule yOnly = new Rule("yOnly", "{<yearNum>}年|版", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint tp = new TmPoint(Grain.YEAR, m.group("yearNum"), m);
        return new DateTime(p.getRuleName(), tp, m);
    });

    /**
     * eg: 2021/6/2
     */
    private static Rule ymd = new Rule("ymd", "([\\d]{2,4})[.\\-,，/]([\\d]{1,2})[.\\-,，/]([\\d]{1,2})", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint year = new TmPoint(Grain.YEAR, m.group(1), m.group(1), m.start(1), m.end(1));
        DateTime dateTime = new DateTime(p.getRuleName(), year, m);
        TmPoint month = new TmPoint(Grain.MONTH, m.group(2), m.group(2), m.start(2), m.end(2));
        TmPoint day = new TmPoint(Grain.DAY, m.group(3), m.group(3), m.start(3), m.end(3));
        dateTime.setMonth(month);
        dateTime.setDay(day);
        return dateTime;
    });

    private static Rule md = new Rule("md", "([\\d]{1,2})[.\\-,，/]([\\d]{1,2})", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint month = new TmPoint(Grain.MONTH, m.group(1), m.group(1), m.start(1), m.end(1));
        DateTime dateTime = new DateTime(p.getRuleName(), month, m);
        TmPoint day = new TmPoint(Grain.DAY, m.group(2), m.group(2), m.start(2), m.end(2));
        dateTime.setDay(day);
        return dateTime;
    });

    private static Rule mOnly = new Rule("mOnly", "{<monthNum>}月份?", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint tp = new TmPoint(Grain.MONTH, m.group("monthNum"), m);
        return new DateTime(p.getRuleName(), tp, m);
    });

    private static Rule mIOnly = new Rule("mIOnly", "(上|下|这|本)一?个?月份?", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String interval = TimeUtil.extractInterval(m.group(1));
        Interval monthI = new Interval(Grain.MONTH, interval, m);
        return new DateTime(p.getRuleName(), monthI, m);
    });

    private static Rule wIOnly = new Rule("wIOnly", "(上|下|这|本)一?个?(周|礼拜|星期)", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String interval = TimeUtil.extractInterval(m.group(1));
        Interval weekI = new Interval(Grain.WEEK, interval, m.group(), m.start(), m.end());
        return new DateTime(p.getRuleName(), weekI, m);
    });

    private static Rule wOnly = new Rule("wOnly", "第{<weekNum>}个?(周|礼拜|星期)", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint tp = new TmPoint(Grain.WEEK, m.group("weekNum"), m);
        return new DateTime(p.getRuleName(), tp, m);
    });

    private static Rule wIDayOnly = new Rule("wIDayOnly", "(上|下|这)一?个?(周|礼拜|星期){<weekNum>}", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String interval = TimeUtil.extractInterval(m.group(1));
        Interval weekDayI = new Interval(Grain.WEEK_DAY, m.group("weekNum"), interval, m.group(), m.start(), m.end());
        return new DateTime(p.getRuleName(), weekDayI, m);

    });

    private static Rule wDayOnly = new Rule("wDayOnly", "(周|礼拜|星期){<weekNum>}(?!点|时)", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint weekDay = new TmPoint(Grain.WEEK_DAY, m.group("weekNum"), m);
        return new DateTime(p.getRuleName(), weekDay, m);
    });

    private static Rule dOnly = new Rule("dOnly", "{<dayNum>}[日号]", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        TmPoint tp = new TmPoint(Grain.DAY, m.group("dayNum"), m);
        return new DateTime(p.getRuleName(), tp, m);
    });

    private static Rule dIOnly = new Rule("dIOnly", "(大前|前|昨|今|明|大后|后)天", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String dayInterval = TimeUtil.extractInterval(m.group(1));
        Interval dayI = new Interval(Grain.DAY, dayInterval, m);
        return new DateTime(p.getRuleName(), dayI, m);
    });

    private static Rule dIOnly2 = new Rule("dIOnly2", "{<num>}天", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String dayInterval = m.group("num");
        Interval dayI = new Interval(Grain.DAY, dayInterval, m);
        DateTime dateTime = new DateTime(p.getRuleName(), dayI, m);
        dateTime.setHint(Hint.AUX);
        return dateTime;
    });

    private static Rule dfIOnly = new Rule("dIOnly", "(昨|今|明)晚", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String dayInterval = TimeUtil.extractInterval(m.group(1));
        Interval dayI = new Interval(Grain.DAY, dayInterval, m);
        FuzzyToken fuzzyDayToken = new FuzzyToken(Constants.FUZZY_TYPE_DAY, FuzzyDay.NIGHT.getName());
        TmPoint tp = new TmPoint(Grain.DAY_FUZZY, fuzzyDayToken, m);
        DateTime dateTime = new DateTime(p.getRuleName(), dayI, m);
        dateTime.setToken(tp);
        return dateTime;
    });

    private static Rule fuzzyDayOnly = new Rule("fuzzyDayOnly", "(凌晨|早上|上午|中午|下午|傍晚|晚上|夜里|白天)", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        FuzzyToken fuzzyDayToken = new FuzzyToken(Constants.FUZZY_TYPE_DAY, FuzzyDay.getFuzzyName(m.group()));
        TmPoint tp = new TmPoint(Grain.DAY_FUZZY, fuzzyDayToken, m);
        return new DateTime(p.getRuleName(), tp, m);
    });

    private static Rule hmsCN = new Rule("hmsCN", "({<hourNum>}[时点]钟?)过?({<minuteNum>}分?)?({<secondNum>}秒)?", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        DimContext c = p.getDimContext();
        IntersectHour hour = IntersectHour.findIntersect(c.getQuery(), m, "hourNum");
        String minute = m.group("minuteNum");
        String second = m.group("secondNum");
        DateTime dateTime = new DateTime(p.getRuleName(), c.getQuery().substring(hour.getStart(), m.end()), hour.getStart(), m.end());
        if (StringUtils.isNotEmpty(hour.getValue())) {
            TmPoint tp = new TmPoint(Grain.HOUR, hour.getValue(), m);
            dateTime.setHour(tp);
        }
        if (StringUtils.isNotEmpty(minute)) {
            TmPoint tp = new TmPoint(Grain.MINUTE, minute, m);
            dateTime.setMinute(tp);
        }
        if (StringUtils.isNotEmpty(second)) {
            TmPoint tp = new TmPoint(Grain.SECOND, second, m);
            dateTime.setSecond(tp);
        }
        return dateTime;
    });

    private static Rule hmCN = new Rule("hmCN", "差({<minuteNum>}分钟?)({<hourNum>}[时点]钟?)", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        int hour = NumUtil.parseInt(m.group("hourNum"));
        int minute = NumUtil.parseInt(m.group("minuteNum"));
        DateTime dateTime = new DateTime(p.getRuleName(), m.group(), m.start(), m.end());
        if (minute < 60) {
            hour = hour - 1;
            minute = 60 - minute;
        } else if (minute > 60 && minute < 120) {
            hour = hour - 2;
            minute = 120 - minute;
        }
        TmPoint htp = new TmPoint(Grain.HOUR, String.valueOf(hour), m);
        dateTime.setHour(htp);
        TmPoint mtp = new TmPoint(Grain.MINUTE,  String.valueOf(minute), m);
        dateTime.setMinute(mtp);

        return dateTime;
    });

    private static Rule hmsNum = new Rule("hmsNum", "([\\d]{1,2}):([\\d]{1,2}):?([\\d]{1,2})?", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        DateTime dateTime = new DateTime(p.getRuleName(), m.group(), m.start(), m.end());
        String hour = m.group(1);
        String minute = m.group(2);
        String second = m.group(3);
        if (StringUtils.isNotEmpty(hour)) {
            TmPoint tp = new TmPoint(Grain.HOUR, hour, m.group(1), m.start(1), m.end(1));
            dateTime.setHour(tp);
        }
        if (StringUtils.isNotEmpty(minute)) {
            TmPoint tp = new TmPoint(Grain.MINUTE, minute, m.group(2), m.start(2), m.end(2));
            dateTime.setMinute(tp);
        }
        if (StringUtils.isNotEmpty(second)) {
            TmPoint tp = new TmPoint(Grain.SECOND, second, m.group(3), m.start(3), m.end(3));
            dateTime.setSecond(tp);
        }
        return dateTime;
    });

    private static Rule hHalf = new Rule("hHalf", "({<hourNum>}[时点])([一二三\\d]{1})?(刻|半)", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        DimContext c = p.getDimContext();
        IntersectHour inferHour = IntersectHour.findIntersect(c.getQuery(), m, "hourNum");
        DateTime dateTime = new DateTime(p.getRuleName(), c.getQuery().substring(inferHour.getStart(), m.end()), inferHour.getStart(), m.end());
        TmPoint hour = new TmPoint(Grain.HOUR, inferHour.getValue(), c.getQuery().substring(inferHour.getStart(), m.end(1)), inferHour.getStart(), m.end(1));
        dateTime.setHour(hour);
        String halfValue = TimeUtil.extractHalf(m.group(3), m.group(4));
        TmPoint minute = new TmPoint(Grain.MINUTE, halfValue, m.group(2), m.start() - m.start(1), m.end());
        dateTime.setMinute(minute);
        return dateTime;
    });

    private static Rule holidayOnly = new Rule("holidayOnly", "(上|下)?(个|年)?{<holiday>}", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String holidayName = HolidayProvider.searchNormHoliday(m.group("holiday"));
        HolidayToken holiday = new HolidayToken(Grain.HOLIDAY, holidayName, m.group(), m.start(), m.end());
        if (StringUtils.isNotEmpty(m.group(1))) {
            String interval = TimeUtil.extractInterval(m.group(1));
            holiday.setInterval(interval);
        }
        return new DateTime(p.getRuleName(), holiday, m);
    });

    @Override
    public List<MergeRule> getMergeRule() {
        return Lists.of(new MergeRule("mergeDate", predicate(), production()));
    }

    private BiPredicate<Map<String, List<DimToken>>, DimContext> predicate() {
        return (tokens, c) -> {
            List<DimToken> dimTokens = tokens.get("DateTime");
            List<DimToken> directionTokens = tokens.get("Direction");
            return CollectionUtils.isNotEmpty(dimTokens) && dimTokens.size() >= 2 || CollectionUtils.isNotEmpty(dimTokens) && CollectionUtils.isNotEmpty(directionTokens);
        };
    }

    private BiFunction<Map<String, List<DimToken>>, DimContext, List<DimToken>> production() {
        return (tokens, c) -> {
            Map<String, List<DateTime>> grain2Token = transGrainToTokens(tokens.get("DateTime"));
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

    private void grainTokenCombineDirection(String query, Map<String, List<DateTime>> grain2Token, Map<String, List<Direction>> directionTokens) {
        Map<String, String> directionPatternStr = fetchDirectionStr(directionTokens);
        Map<String, Direction> textPosToDirectionToken = transTextPos2DirectionToken(directionTokens);
        grain2Token.forEach((k, v) -> {
            for (DateTime dt : v) {
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


    private List<DimToken> mergeToken(List<String> grainList, Map<String, List<DateTime>> grain2Token, DimContext context) {
        List<DimToken> dimTokens = new ArrayList<>();
        Pattern mergePattern = mergeDatePattern(grainList, grain2Token);
        if (mergePattern == null) {
            return null;
        }
        Matcher m = mergePattern.matcher(context.getQuery());
        Map<String, DateTime> textPos2Token = transTextPos2Token(grain2Token);
        while (m.find()) {
            DateTime dateTime = new DateTime();
            dateTime.setText(m.group());
            dateTime.setStart(m.start());
            dateTime.setEnd(m.end());
            dateTime.setRules(new ArrayList<>());
            dateTime.setHint(Hint.COMBINE);
            List<DateTime> canCombineToken = selectCombineToken(m, grainList, textPos2Token);
            combineToken(dateTime, canCombineToken);
            dimTokens.add(dateTime);
        }
        return dimTokens;
    }

    private Map<String, DateTime> transTextPos2Token(Map<String, List<DateTime>> grain2Token) {
        Map<String, DateTime> textPos2Token = new HashMap<>();
        grain2Token.values().forEach(dateTimes -> {
            dateTimes.forEach(dt -> textPos2Token.put(dt.getText() + "_" + dt.getStart(), dt));
        });
        return textPos2Token;
    }

    private List<DateTime> selectCombineToken(Matcher m, List<String> grainList, Map<String, DateTime> textPos2Token) {
        List<DateTime> canTokens = new ArrayList<>();
        for (String grain : grainList) {
            String value = m.group(grain);
            if (value != null) {
                DateTime dt = textPos2Token.get(value + "_" + m.start(grain));
                if (dt != null) {
                    canTokens.add(dt);
                }
            }
        }
        return canTokens;
    }

    private void combineToken(DateTime combineToken, List<DateTime> canComposeToken) {
        if (CollectionUtils.isEmpty(canComposeToken)) {
            return;
        }
        canComposeToken.forEach(can -> copyCanToCombineToken(combineToken, can));

    }

    private void copyCanToCombineToken(DateTime combineToken, DateTime candiToken) {
        if (candiToken.getLoop() != null) {
            combineToken.setLoop(candiToken.getLoop());
        }
        if (candiToken.isLunar()) {
            combineToken.setLunar(true);
        }
        if (candiToken.getDirection() != null) {
            combineToken.setDirection(candiToken.getDirection());
        }
        if (candiToken.getHoliday() != null) {
            combineToken.setHoliday(candiToken.getHoliday());
        }
        if (candiToken.getYear() != null) {
            combineToken.setYear(candiToken.getYear());
        }
        if (candiToken.getQuarter() != null) {
            combineToken.setQuarter(candiToken.getQuarter());
        }
        if (candiToken.getMonth() != null) {
            combineToken.setMonth(candiToken.getMonth());
        }
        if (candiToken.getFuzzyMonth() != null) {
            combineToken.setFuzzyMonth(candiToken.getFuzzyMonth());
        }
        if (candiToken.getWeek() != null) {
            combineToken.setWeek(candiToken.getWeek());
        }
        if (candiToken.getFuzzyWeek() != null) {
            combineToken.setFuzzyWeek(candiToken.getFuzzyWeek());
        }
        if (candiToken.getWeekDay() != null) {
            combineToken.setWeekDay(candiToken.getWeekDay());
        }
        if (candiToken.getWeek() != null) {
            combineToken.setWeek(candiToken.getWeek());
        }
        if (candiToken.getDay() != null) {
            combineToken.setDay(candiToken.getDay());
        }
        if (candiToken.getFuzzyDay() != null) {
            combineToken.setFuzzyDay(candiToken.getFuzzyDay());
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


    private List<String> obtainGrains(Map<String, List<DateTime>> grain2Token) {
        List<String> list = new ArrayList<>(grain2Token.keySet());
        list.sort(Comparator.comparingInt(Grain::getGrainLevel));
        return list;
    }

    private Map<String, List<DateTime>> transGrainToTokens(List<DimToken> dimTokens) {
        Map<String, List<DateTime>> dateTimeMap = new HashMap<>(8);
        if (CollectionUtils.isEmpty(dimTokens)) {
            return dateTimeMap;
        }
        for (DimToken token : dimTokens) {
            DateTime dtToken = (DateTime) token;
            String grain = fetchTokenGrain(dtToken);
            if (!grain.equals(Constants.NO_DT)) {
                Maps.putKlv(dateTimeMap, grain, dtToken);
            }
        }
        return dateTimeMap;
    }

    private Pattern mergeDatePattern(List<String> grainList, Map<String, List<DateTime>> grain2Token) {
        String patternStr = generatePatternStr(grainList, grain2Token);
        try {
            return Pattern.compile(patternStr);
        } catch (Exception e) {
            log.error("dateTime mergeRule compile error:", e);
        }
        return null;
    }

    private String generatePatternStr(List<String> grainList, Map<String, List<DateTime>> grain2Token) {
        List<String> ptnList = new ArrayList<>();
        int size = grainList.size();
        for (int i = size - 1; i >= 0; i--) {
            String groupName = grainList.get(i);
            List<String> matchList = fetchMatchList(grain2Token.get(groupName));
            matchList.sort(Comparator.comparingInt(String::length).reversed());
            if (i == 0 || (i == 1 && size > 1)) {
                ptnList.add("(?<" + groupName + ">" + String.join("|", matchList) + ")");
            } else {
                ptnList.add("(?<" + groupName + ">" + String.join("|", matchList) + ")?");
            }
        }
        return String.join("[的 ]{0,2}", ptnList);
    }

    private List<String> fetchMatchList(List<DateTime> dateTimes) {
        List<String> matchList = new ArrayList<>();
        dateTimes.forEach(dt -> matchList.add(dt.getText()));
        return matchList;
    }

    private String fetchTokenGrain(DateTime dateTime) {
        if (dateTime.getHour() != null) {
            return Constants.HOUR;
        } else if (dateTime.getFuzzyDay() != null) {
            return Constants.FUZZY_TYPE_DAY;
        } else if (dateTime.getDay() != null || dateTime.getHoliday() != null) {
            return Constants.DAY;
        } else if (dateTime.getWeekDay() != null) {
            return Constants.WEEK_DAY;
        } else if (dateTime.getWeek() != null) {
            return Constants.WEEK;
        } else if (dateTime.getFuzzyWeek() != null) {
            return Constants.FUZZY_TYPE_WEEK;
        } else if (dateTime.getFuzzyMonth() != null) {
            return Constants.FUZZY_TYPE_MONTH;
        } else if (dateTime.getMonth() != null) {
            return Constants.MONTH;
        } else if (dateTime.isLunar()) {
            return Constants.LUNAR;
        } else if (dateTime.getQuarter() != null) {
            return Constants.QUARTER;
        } else if (dateTime.getYear() != null) {
            return Constants.YEAR;
        }
        return Constants.NO_DT;
    }

    public DateTime() {
        this.setDimName(DateTime.class.getSimpleName());
    }

    public DateTime(String ruleName, BaseToken token, Matcher m) {
        this.setDimName(DateTime.class.getSimpleName());
        this.setText(m.group());
        this.setRules(Lists.of(ruleName));
        this.setStart(m.start());
        this.setEnd(m.end());
        this.setToken(token);
    }

    public DateTime(String ruleName, String text, int start, int end) {
        this.setDimName(DateTime.class.getSimpleName());
        this.setText(text);
        this.setRules(Lists.of(ruleName));
        this.setStart(start);
        this.setEnd(end);
    }

    public HolidayToken getHoliday() {
        return (HolidayToken) holiday;
    }

    public void setHoliday(HolidayToken holiday) {
        this.holiday = holiday;
    }

    public BaseToken getYear() {
        return year;
    }

    public void setYear(BaseToken year) {
        this.year = year;
    }

    public BaseToken getQuarter() {
        return quarter;
    }

    public void setQuarter(BaseToken quarter) {
        this.quarter = quarter;
    }

    public BaseToken getMonth() {
        return month;
    }

    public void setMonth(BaseToken month) {
        this.month = month;
    }

    public BaseToken getWeek() {
        return week;
    }

    public void setWeek(BaseToken week) {
        this.week = week;
    }

    public BaseToken getDay() {
        return day;
    }

    public void setDay(BaseToken day) {
        this.day = day;
    }

    public BaseToken getHour() {
        return hour;
    }

    public void setHour(BaseToken hour) {
        this.hour = hour;
    }

    public BaseToken getMinute() {
        return minute;
    }

    public void setMinute(BaseToken minute) {
        this.minute = minute;
    }

    public BaseToken getSecond() {
        return second;
    }

    public void setSecond(BaseToken second) {
        this.second = second;
    }

    public BaseToken getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(BaseToken weekDay) {
        this.weekDay = weekDay;
    }

    public void setHoliday(BaseToken holiday) {
        this.holiday = holiday;
    }

    public BaseToken getFuzzyMonth() {
        return fuzzyMonth;
    }

    public void setFuzzyMonth(BaseToken fuzzyMonth) {
        this.fuzzyMonth = fuzzyMonth;
    }

    public BaseToken getFuzzyWeek() {
        return fuzzyWeek;
    }

    public void setFuzzyWeek(BaseToken fuzzyWeek) {
        this.fuzzyWeek = fuzzyWeek;
    }

    public BaseToken getFuzzyDay() {
        return fuzzyDay;
    }

    public void setFuzzyDay(BaseToken fuzzyDay) {
        this.fuzzyDay = fuzzyDay;
    }

    public Loop getLoop() {
        return loop;
    }

    public void setLoop(Loop loop) {
        this.loop = loop;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isLunar() {
        return lunar;
    }

    public void setLunar(boolean lunar) {
        this.lunar = lunar;
    }

    public List<String> fetchValidGrain() {
        List<String> grains = new ArrayList<>();
        if (holiday != null) {
            grains.add(Constants.HOLIDAY);
        }
        if (year != null) {
            grains.add(Constants.YEAR);
        }
        if (month != null) {
            grains.add(Constants.MONTH);
        }
        if (fuzzyMonth != null) {
            grains.add(Constants.MONTH_FUZZY);
        }
        if (week != null) {
            grains.add(Constants.WEEK);
        }
        if (fuzzyWeek != null) {
            grains.add(Constants.WEEK_FUZZY);
        }
        if (weekDay != null) {
            grains.add(Constants.WEEK_DAY);
        }
        if (day != null) {
            grains.add(Constants.DAY);
        }
        if (fuzzyDay != null) {
            grains.add(Constants.DAY_FUZZY);
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

    private void setToken(BaseToken baseToken) {
        Grain grain = baseToken.getGrain();
        if (grain == Grain.HOLIDAY) {
            this.holiday = baseToken;
        } else if (grain == Grain.YEAR) {
            this.year = baseToken;
        } else if (grain == Grain.MONTH) {
            this.month = baseToken;
        } else if (grain == Grain.WEEK) {
            this.week = baseToken;
        } else if (grain == Grain.WEEK_DAY) {
            this.weekDay = baseToken;
        } else if (grain == Grain.DAY) {
            this.day = baseToken;
        } else if (grain == Grain.HOUR) {
            this.hour = baseToken;
        } else if (grain == Grain.MINUTE) {
            this.minute = baseToken;
        } else if (grain == Grain.SECOND) {
            this.second = baseToken;
        } else if (grain == Grain.WEEK_FUZZY) {
            this.fuzzyWeek = baseToken;
        } else if (grain == Grain.MONTH_FUZZY) {
            this.fuzzyMonth = baseToken;
        } else if (grain == Grain.DAY_FUZZY) {
            this.fuzzyDay = baseToken;
        }
    }


    @Override
    public boolean isDimension(DimContext dimContext) {
        Matcher m = p.matcher(dimContext.getQuery());
        return (m.find() || Lists.containsOr(dimContext.getParsedDim(), dependents));
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

    public static class IntersectHour {
        private String value;
        private int start;


        public static IntersectHour findIntersect(String query, Matcher m, String hourGroup) {
            String originHour = m.group(hourGroup);
            char[] cs = originHour.toCharArray();
            int index = m.start(hourGroup);
            if (index < 1 || cs.length < 2) {
                return new IntersectHour(m.group(hourGroup), m.start(hourGroup));
            }
            String str = query.substring(index - 1, index);
            if ("周".equals(str) || ("期".equals(str) && query.contains("星期")) || ("拜".equals(str) && query.contains("礼拜"))) {
                if (NumUtil.chineseToNum(String.valueOf(cs[0])) < 8) {
                    return new IntersectHour(originHour.substring(1), index + 1);
                }

            }

            return new IntersectHour(m.group(hourGroup), m.start(hourGroup));

        }

        private IntersectHour(String value, int start) {
            this.value = value;
            this.start = start;
        }


        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }
    }

}
