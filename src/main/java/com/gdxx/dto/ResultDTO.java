package com.gdxx.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/*
 * 路线结果集封装
 */
@Setter
@Getter
public class ResultDTO implements Comparable {
    private int resultId;
    private List<Step> steps;
    private String totalTime;
    private Double totalCost;
    private Double totalComfortable;

    private double differenceDegree;

    private double eScore;
    private double tScore;
    private double lbsScore;

    private int esGradeNumber;
    private int tsGradeNumber;
    private int lbsGradeNumber;

    @Override
    public int compareTo(Object o) {
        return 1;
    }
}
