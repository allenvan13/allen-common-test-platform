package com.allen.testplatform.common.utils;

/**
 * @author Fan QingChuan
 * @since 2022/4/1 19:13
 */
public class MathUtils {

    public static Double convertNeg(Double number){
        if (number > 0) {
            number = number - ( number * 2 );
        }
        return number;
    }

    public static Double convertPos(Double number){
        if (number < 0) {
            number = number - ( number * 2 );
        }
        return number;
    }


    public static double maxInArray(double[] arrayNumbers ) {
        double max = arrayNumbers[0];
        for (int i = 0; i < arrayNumbers.length; i++) {
            if (arrayNumbers[i] >= max) {
                max = arrayNumbers[i];
            }
        }
        return max;
    }

    public static double minInArray(double[] arrayNumbers ) {
        double min = arrayNumbers[0];
        for (int i = 0; i < arrayNumbers.length; i++) {
            if (arrayNumbers[i] <= min) {
                min = arrayNumbers[i];
            }
        }
        return min;
    }

    /**
     * 判断某个值是否在某个区间内(区间闭合,包含所在的起始值和结束值)
     *
     * @param current 特定数值
     * @param min     区间起始位
     * @param max     区间结束位
     * @return true 在;false 不在
     */
    public static boolean rangeInDefined(Double current, Double min, Double max) {
        if (current == null || min == null || max == null) {
            return false;
        }
        return Math.max(min, current) == Math.min(current, max);
    }

    public static boolean rangeInDefined(Integer current, Integer min, Integer max) {
        if (current == null || min == null || max == null) {
            return false;
        }
        return Math.max(min, current) == Math.min(current, max);
    }
}
