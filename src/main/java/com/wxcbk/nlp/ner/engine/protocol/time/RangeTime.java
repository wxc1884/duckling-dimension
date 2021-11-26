package com.wxcbk.nlp.ner.engine.protocol.time;

import java.util.List;


public class RangeTime extends BaseSubTime {
    private String type;
    private String seriesType;
    private SingleRange singleRange;
    private List<PointTime> multiRange;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeriesType() {
        return seriesType;
    }

    public void setSeriesType(String seriesType) {
        this.seriesType = seriesType;
    }

    public SingleRange getSingleRange() {
        return singleRange;
    }

    public void setSingleRange(SingleRange singleRange) {
        this.singleRange = singleRange;
    }

    public List<PointTime> getMultiRange() {
        return multiRange;
    }

    public void setMultiRange(List<PointTime> multiRange) {
        this.multiRange = multiRange;
    }


    public static class SingleRange {
        private List<Integer> year;
        private List<Integer> quarter;
        private List<Integer> month;
        private List<Integer> week;
        private List<Integer> weekDay;
        private List<Integer> day;
        private List<Integer> hour;
        private List<Integer> minute;
        private List<Integer> second;
        private List<Integer> intervalYear;
        private List<Integer> intervalQuarter;
        private List<Integer> intervalMonth;
        private List<Integer> intervalWeek;
        private List<Integer> intervalDay;
        private PointTime commonTime;

        public List<Integer> getYear() {
            return year;
        }

        public void setYear(List<Integer> year) {
            this.year = year;
        }

        public List<Integer> getQuarter() {
            return quarter;
        }

        public void setQuarter(List<Integer> quarter) {
            this.quarter = quarter;
        }

        public List<Integer> getMonth() {
            return month;
        }

        public void setMonth(List<Integer> month) {
            this.month = month;
        }

        public List<Integer> getWeek() {
            return week;
        }

        public void setWeek(List<Integer> week) {
            this.week = week;
        }

        public List<Integer> getWeekDay() {
            return weekDay;
        }

        public void setWeekDay(List<Integer> weekDay) {
            this.weekDay = weekDay;
        }

        public List<Integer> getDay() {
            return day;
        }

        public void setDay(List<Integer> day) {
            this.day = day;
        }

        public List<Integer> getHour() {
            return hour;
        }

        public void setHour(List<Integer> hour) {
            this.hour = hour;
        }

        public List<Integer> getMinute() {
            return minute;
        }

        public void setMinute(List<Integer> minute) {
            this.minute = minute;
        }

        public List<Integer> getSecond() {
            return second;
        }

        public void setSecond(List<Integer> second) {
            this.second = second;
        }

        public List<Integer> getIntervalYear() {
            return intervalYear;
        }

        public void setIntervalYear(List<Integer> intervalYear) {
            this.intervalYear = intervalYear;
        }

        public List<Integer> getIntervalQuarter() {
            return intervalQuarter;
        }

        public void setIntervalQuarter(List<Integer> intervalQuarter) {
            this.intervalQuarter = intervalQuarter;
        }

        public List<Integer> getIntervalMonth() {
            return intervalMonth;
        }

        public void setIntervalMonth(List<Integer> intervalMonth) {
            this.intervalMonth = intervalMonth;
        }

        public List<Integer> getIntervalWeek() {
            return intervalWeek;
        }

        public void setIntervalWeek(List<Integer> intervalWeek) {
            this.intervalWeek = intervalWeek;
        }

        public List<Integer> getIntervalDay() {
            return intervalDay;
        }

        public void setIntervalDay(List<Integer> intervalDay) {
            this.intervalDay = intervalDay;
        }

        public PointTime getCommonTime() {
            return commonTime;
        }

        public void setCommonTime(PointTime commonTime) {
            this.commonTime = commonTime;
        }
    }

}
