package com.wxcbk.nlp.ner.enums;

import com.wxcbk.nlp.ner.dim.time.enums.TimeDim;


public enum DimType {
    /**
     * 时间
     */
    TIME("time", 1.0),
    /**
     * 辅助
     */
    NO_DIM("no_dim", 0);

    private String name;
    private double weight;

    DimType(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }

    public static double getWeight(String fullName) {
        for (DimType dim : DimType.values()) {
            if ("time".equals(dim.getName())) {
                TimeDim timeDim = TimeDim.getTimDim(fullName);
                if (timeDim != null) {
                    return timeDim.getWeight();
                }
            } else {
                if (dim.getName().equals(fullName)) {
                    return dim.getWeight();
                }
            }

        }
        return -1.0;
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

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
