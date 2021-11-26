package com.wxcbk.nlp.ner.engine.protocol.time;

import com.wxcbk.nlp.ner.constant.Constants;
import com.wxcbk.nlp.ner.dim.time.data.DateTime;
import com.wxcbk.nlp.ner.dim.time.data.DurationTime;
import com.wxcbk.nlp.ner.engine.protocol.BaseDimData;
import com.wxcbk.nlp.ner.engine.protocol.ProtocolAdapter;
import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.util.NumUtil;
import com.wxcbk.nlp.ner.dim.time.token.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class TimeProtocolAdapter implements ProtocolAdapter {

    @Override
    public List<BaseDimData> convertProtocol(List<DimToken> tokens) {
        List<BaseDimData> timeList = new ArrayList<>();
        for (DimToken dimToken : tokens) {
            String dimName = dimToken.getDimName();
            Time time = new Time();
            switch (dimName) {
                case Constants.DIM_TIME_DT:
                    assemblePointTimeData(time, dimToken);
                    break;
                case Constants.DIM_TIME_QT:
                    assembleIntervalTimeData(time, dimToken);
                    break;
                case Constants.DIM_TIME_RT:
                    assembleRangeTimeData(time, dimToken);
                    break;
                case Constants.DIM_TIME_PT:
                    assemblePeriodTimeData(time, dimToken);
                    break;
                default:
            }
            TimeToken timeToken = new TimeToken(this.getDimension(), dimToken.getText(), dimToken.getStart(), dimToken.getEnd());
            timeToken.setTime(time);
            timeList.add(timeToken);
        }
        return timeList;
    }

    @Override
    public String getDimension() {
        return "time";
    }

    private void assemblePointTimeData(Time time, DimToken dimToken) {
        DateTime token = (DateTime) dimToken;
        time.setSubType(Constants.SUB_PTT);
        PointTime pointTime = new PointTime();
        assemblePointTime(token, pointTime);
        time.setSubTime(pointTime);

    }

    private void assembleIntervalTimeData(Time time, DimToken dimToken) {
        DurationTime token = (DurationTime) dimToken;
        time.setSubType(Constants.SUB_ILT);
        IntervalTime intervalTime = new IntervalTime();
        assembleIntervalTime(token, intervalTime);
        time.setSubTime(intervalTime);
    }

    private void assemblePeriodTimeData(Time time, DimToken dimToken) {
        com.wxcbk.nlp.ner.dim.time.data.PeriodTime token = (com.wxcbk.nlp.ner.dim.time.data.PeriodTime) dimToken;
        time.setSubType(Constants.SUB_PDT);
        PeriodTime periodTime = new PeriodTime();
        assemblePeriodTime(token, periodTime);
        time.setSubTime(periodTime);
    }

    private void assembleRangeTimeData(Time time, DimToken dimToken) {
        com.wxcbk.nlp.ner.dim.time.data.RangeTime rgToken = (com.wxcbk.nlp.ner.dim.time.data.RangeTime) dimToken;
        time.setSubType(Constants.SUB_RGT);
        RangeTime rangeTime = new RangeTime();
        assembleRangeTime(rgToken, rangeTime);
        time.setSubTime(rangeTime);
    }

    private void assemblePeriodTime(com.wxcbk.nlp.ner.dim.time.data.PeriodTime ptToken, PeriodTime periodTime) {
        PointTime from = new PointTime();
        assemblePointTime(ptToken.getFrom(), from);
        periodTime.setFrom(from);
        PointTime to = new PointTime();
        assemblePointTime(ptToken.getTo(), to);
        periodTime.setTo(to);
        completeFromAndToData(from, to);
    }

    private void assembleRangeTime(com.wxcbk.nlp.ner.dim.time.data.RangeTime rtToken, RangeTime rangeTime) {
        rangeTime.setType(rtToken.getType());
        RangeToken rangeToken = rtToken.getRange();
        DateTime dateToken = rtToken.getDateTime();
        if (CollectionUtils.isNotEmpty(rtToken.getMultiTimes())) {
            rangeTime.setSeriesType(Constants.DIM_TIME_RT_MULTI);
            List<PointTime> pointTimes = new ArrayList<>();
            assembleMultiRangeTime(pointTimes, rtToken.getMultiTimes());
            if (rtToken.getType().equals(Constants.DIM_TIME_RT_AND)) {
                completeFromAndToData(pointTimes.get(0), pointTimes.get(1));
                completeFromAndToData(pointTimes.get(1), pointTimes.get(0));
            }
            rangeTime.setMultiRange(pointTimes);

        } else if (rangeToken != null) {
            rangeTime.setSeriesType(Constants.DIM_TIME_RT_SINGLE);
            RangeTime.SingleRange singleRange = new RangeTime.SingleRange();
            assembleSingleRangeTime(rangeToken, singleRange, dateToken);
            rangeTime.setSingleRange(singleRange);
        }
    }

    private void completeFromAndToData(PointTime from, PointTime to) {
        if (StringUtils.isNotEmpty(from.getLoop()) && StringUtils.isEmpty(to.getLoop())) {
            to.setLoop(from.getLoop());
        }
        if (from.getHoliday() != null && to.getHoliday() == null) {
            to.setHoliday(from.getHoliday());
        }
        if (from.getYear() != null && to.getYear() == null) {
            to.setYear(from.getYear());
        }
        if (from.getMonth() != null && to.getMonth() == null) {
            to.setMonth(from.getMonth());
        }
        if (from.getWeek() != null && to.getWeek() == null) {
            to.setWeek(from.getWeek());
        }
        if (from.getWeekDay() != null && to.getWeekDay() == null) {
            to.setWeekDay(from.getWeekDay());
        }
        if (from.getDay() != null && to.getDay() == null) {
            to.setDay(from.getDay());
        }
        if (from.getHour() != null && to.getHour() == null) {
            to.setHour(from.getHour());
        }
        if (from.getMinute() != null && to.getMinute() == null) {
            to.setMinute(from.getMinute());
        }
        if (from.getSecond() != null && to.getSecond() == null) {
            to.setSecond(from.getSecond());
        }
        if (from.getIntervalYear() != null && to.getIntervalYear() == null) {
            to.setIntervalYear(from.getIntervalYear());
        }
        if (from.getIntervalMonth() != null && to.getIntervalMonth() == null) {
            to.setIntervalMonth(from.getIntervalMonth());
        }
        if (from.getIntervalWeek() != null && to.getIntervalWeek() == null) {
            to.setIntervalWeek(from.getIntervalWeek());
        }
        if (from.getIntervalDay() != null && to.getIntervalDay() == null) {
            to.setIntervalDay(from.getIntervalDay());
        }
        if (from.getFuzzyDay() != null && to.getFuzzyDay() == null) {
            if (to.getHour() != null && to.getHour() < 12) {
                to.setFuzzyDay(from.getFuzzyDay());
            }
        }

    }

    private void assembleSingleRangeTime(RangeToken rangeToken, RangeTime.SingleRange singleRange, DateTime dateTime) {
        String grain = rangeToken.getGrain().getName();
        List<Integer> values = NumUtil.stringToIntList(rangeToken.getValues());
        if (rangeToken.isInterval()) {
            setIntervalRange(grain, values, singleRange);
        } else {
            setGrainRange(grain, values, singleRange);
        }
        if (dateTime != null) {
            PointTime pointTime = new PointTime();
            assemblePointTime(dateTime, pointTime);
            singleRange.setCommonTime(pointTime);
        }
    }

    private void assembleMultiRangeTime(List<PointTime> pointTimes, List<DateTime> dateTimes) {
        for (DateTime dateTime : dateTimes) {
            PointTime pointTime = new PointTime();
            assemblePointTime(dateTime, pointTime);
            pointTimes.add(pointTime);
        }
    }

    private void setIntervalRange(String grain, List<Integer> values, RangeTime.SingleRange singleRange) {
        switch (grain) {
            case Constants.YEAR:
                singleRange.setIntervalYear(values);
                break;
            case Constants.QUARTER:
                singleRange.setIntervalQuarter(values);
                break;
            case Constants.MONTH:
                singleRange.setIntervalMonth(values);
                break;
            case Constants.WEEK:
                singleRange.setIntervalWeek(values);
                break;
            case Constants.DAY:
                singleRange.setIntervalDay(values);
                break;
            default:
        }
    }

    private void setGrainRange(String grain, List<Integer> values, RangeTime.SingleRange singleRange) {
        switch (grain) {
            case Constants.YEAR:
                singleRange.setYear(values);
                break;
            case Constants.QUARTER:
                singleRange.setQuarter(values);
                break;
            case Constants.MONTH:
                singleRange.setMonth(values);
                break;
            case Constants.WEEK:
                singleRange.setWeek(values);
                break;
            case Constants.WEEK_DAY:
                singleRange.setWeekDay(values);
                break;
            case Constants.DAY:
                singleRange.setDay(values);
                break;
            case Constants.HOUR:
                singleRange.setHour(values);
                break;
            case Constants.MINUTE:
                singleRange.setMinute(values);
                break;
            case Constants.SECOND:
                singleRange.setSecond(values);
                break;
            default:
        }
    }

    private void assemblePointTime(DateTime dateTime, PointTime pointTime) {
        if (dateTime.getLoop() != null) {
            pointTime.setLoop(dateTime.getLoop().getName());
        }
        if (dateTime.isLunar()) {
            pointTime.setLunar("true");
        }
        if (StringUtils.isNotEmpty(dateTime.getDirection())) {
            pointTime.setDirection(dateTime.getDirection());
        }
        List<String> grains = dateTime.fetchValidGrain();
        for (String grain : grains) {
            setGrainToken(grain, dateTime, pointTime);
        }
    }

    private void assembleIntervalTime(DurationTime durationTime, IntervalTime intervalTime) {
        if (StringUtils.isNotEmpty(durationTime.getLoop())) {
            intervalTime.setLoop(durationTime.getLoop());
        }
        if (StringUtils.isNotEmpty(durationTime.getDirection())) {
            intervalTime.setDirection(durationTime.getDirection());
        }
        List<String> grains = durationTime.fetchValidGrain();
        for (String grain : grains) {
            setGrainToken(grain, durationTime, intervalTime);
        }


    }

    private void setGrainToken(String grain, DateTime dateTime, PointTime pointTime) {
        switch (grain) {
            case Constants.HOLIDAY:
                HolidayToken holidayToken = dateTime.getHoliday();
                pointTime.setHoliday(holidayToken.getHoliday());
                pointTime.setIntervalHoliday(NumUtil.parseInteger(holidayToken.getInterval()));
                break;
            case Constants.YEAR:
                copyTokenValue(dateTime.getYear(), pointTime, Constants.YEAR);
                break;
            case Constants.MONTH:
                copyTokenValue(dateTime.getMonth(), pointTime, Constants.MONTH);
                break;
            case Constants.MONTH_FUZZY:
                copyTokenValue(dateTime.getFuzzyMonth(), pointTime, Constants.MONTH_FUZZY);
                break;
            case Constants.WEEK:
                copyTokenValue(dateTime.getWeek(), pointTime, Constants.WEEK);
                break;
            case Constants.WEEK_FUZZY:
                copyTokenValue(dateTime.getFuzzyWeek(), pointTime, Constants.WEEK_FUZZY);
                break;
            case Constants.WEEK_DAY:
                copyTokenValue(dateTime.getWeekDay(), pointTime, Constants.WEEK_DAY);
                break;
            case Constants.DAY:
                copyTokenValue(dateTime.getDay(), pointTime, Constants.DAY);
                break;
            case Constants.DAY_FUZZY:
                copyTokenValue(dateTime.getFuzzyDay(), pointTime, Constants.DAY_FUZZY);
                break;
            case Constants.HOUR:
                copyTokenValue(dateTime.getHour(), pointTime, Constants.HOUR);
                break;
            case Constants.MINUTE:
                copyTokenValue(dateTime.getMinute(), pointTime, Constants.MINUTE);
                break;
            case Constants.SECOND:
                copyTokenValue(dateTime.getSecond(), pointTime, Constants.SECOND);
                break;
            default:
        }
    }

    private void setGrainToken(String grain, DurationTime durationTime, IntervalTime intervalTime) {
        switch (grain) {
            case Constants.YEAR:
                intervalTime.setYear(NumUtil.parseInteger(durationTime.getYear().getGrainValue()));
                break;
            case Constants.MONTH:
                intervalTime.setMonth(NumUtil.parseInteger(durationTime.getMonth().getGrainValue()));
                break;
            case Constants.WEEK:
                intervalTime.setWeek(NumUtil.parseInteger(durationTime.getWeek().getGrainValue()));
                break;
            case Constants.WEEK_FUZZY:
                intervalTime.setWeekDays(NumUtil.parseInteger(durationTime.getFuzzyWeek().getGrainValue()));
                break;
            case Constants.DAY:
                intervalTime.setDay(NumUtil.parseInteger(durationTime.getDay().getGrainValue()));
                break;
            case Constants.HOUR:
                intervalTime.setHour(NumUtil.parseInteger(durationTime.getHour().getGrainValue()));
                break;
            case Constants.MINUTE:
                intervalTime.setMinute(NumUtil.parseInteger(durationTime.getMinute().getGrainValue()));
                break;
            case Constants.SECOND:
                intervalTime.setSecond(NumUtil.parseInteger(durationTime.getSecond().getGrainValue()));
                break;
            default:
        }
    }

    private void copyTokenValue(BaseToken ot, PointTime st, String grain) {
        if (ot instanceof Interval) {
            Interval oi = (Interval) ot;
            copyIntervalValue(oi, st, grain);

        } else if (ot instanceof TmPoint) {
            TmPoint om = (TmPoint) ot;
            copyPointValue(om, st, grain);
        }
    }

    private void copyPointValue(TmPoint tmPoint, PointTime pointTime, String grain) {
        if (Constants.GRAIN_NO_VALUE.equals(tmPoint.getGrainValue())) {
            return;
        }
        switch (grain) {
            case Constants.YEAR: {
                pointTime.setYear(NumUtil.parseInteger(tmPoint.getGrainValue()));
                break;
            }
            case Constants.MONTH: {
                pointTime.setMonth(NumUtil.parseInteger(tmPoint.getGrainValue()));
                break;
            }
            case Constants.MONTH_FUZZY: {
                pointTime.setFuzzyMonth(tmPoint.getFuzzyItem().getFuzzyValue());
                break;
            }
            case Constants.WEEK: {
                pointTime.setWeek(NumUtil.parseInteger(tmPoint.getGrainValue()));
                break;
            }
            case Constants.WEEK_FUZZY: {
                pointTime.setFuzzyWeek(tmPoint.getFuzzyItem().getFuzzyValue());
                break;
            }
            case Constants.WEEK_DAY: {
                pointTime.setWeekDay(NumUtil.parseInteger(tmPoint.getGrainValue()));
                break;
            }
            case Constants.DAY: {
                pointTime.setDay(NumUtil.parseInteger(tmPoint.getGrainValue()));
                break;
            }
            case Constants.DAY_FUZZY: {
                pointTime.setFuzzyDay(tmPoint.getFuzzyItem().getFuzzyValue());
                break;
            }
            case Constants.HOUR: {
                pointTime.setHour(NumUtil.parseInteger(tmPoint.getGrainValue()));
                break;
            }
            case Constants.MINUTE: {
                pointTime.setMinute(NumUtil.parseInteger(tmPoint.getGrainValue()));
                break;
            }
            case Constants.SECOND: {
                pointTime.setSecond(NumUtil.parseInteger(tmPoint.getGrainValue()));
                break;
            }
            default:
        }
    }

    private void copyIntervalValue(Interval interval, PointTime pointTime, String grain) {
        switch (grain) {
            case Constants.YEAR:
                pointTime.setIntervalYear(NumUtil.parseInt(interval.getGrainInterval()));
                break;
            case Constants.QUARTER:
                pointTime.setIntervalQuarter(NumUtil.parseInt(interval.getGrainInterval()));
                break;
            case Constants.MONTH:
                pointTime.setIntervalMonth(NumUtil.parseInt(interval.getGrainInterval()));
                break;
            case Constants.WEEK:
                pointTime.setIntervalWeek(NumUtil.parseInt(interval.getGrainInterval()));
                break;
            case Constants.WEEK_DAY:
                pointTime.setIntervalWeek(NumUtil.parseInt(interval.getGrainInterval()));
                pointTime.setWeekDay(NumUtil.parseInteger(interval.getGrainValue()));
                break;
            case Constants.DAY:
                pointTime.setIntervalDay(NumUtil.parseInt(interval.getGrainInterval()));
                break;
            default:
        }
    }
}
