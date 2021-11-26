package com.wxcbk.nlp.ner.dim.time.enums;



public enum TimeDim {
    /**
     * 时间种类
     */
    DT("DateTime", 1.1),
    QT("DurationTime", 1.2),
    PT("PeriodTime", 1.3),
    RT("RangeTime", 1.4),
    TP("Direction", 0);


    private String name;
    private double weight;

    TimeDim(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }

    public static TimeDim getTimDim(String fullName) {
        for (TimeDim td : TimeDim.values()) {
            if (td.getName().equals(fullName)) {
                return td;
            }
        }
        return null;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
