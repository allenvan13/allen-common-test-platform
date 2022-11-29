package com.allen.testplatform.common.utils;

/**
 * @author Fan QingChuan
 * @since 2022/4/8 19:14
 */
public class ArrayUtils {


    public static void main(String[] args) {
        double[][] doubles22 = new double[][]{};
        double[][] doubles1 = new double[][]{{100,100,20,54},{102,103,7,2},{105,10,81,07}};
        double[][] doubles2 = new double[][]{{100,100},{102,103},{105,107},{108,109},{100,109},{102,187}};
        double[][] doubles3 = new double[][]{{1504,1500,1507,1520},{2500,25,64,63},{0,1504,1505,1503}};
        double[][] doubles4 = new double[][]{{28,25,69,144,753},{952,15,24444,7745,5525},{854,4447,7745,522,51}};
        boolean[][] booleans = new boolean[][]{{true,true,true,true},{false,false,false,false},{false,true,false,true}};
        boolean[][] booleans2 = new boolean[][]{{true,true},{false,false},{false,true}};


        System.out.println(toString(doubles1));
        System.out.println(toString(booleans));
        System.out.println(toString(doubles1[0]));
    }

    public static String toString(double[][] a){
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "{{}}";

        StringBuilder b = new StringBuilder();
        b.append("{");
        for (int i = 0; ; i++) {
            b.append(toString(a[i]));
            if (i == iMax)
                return b.append('}').toString();
            b.append(", ");
        }
    }

    public static String toString(double[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "{}";
        StringBuilder b = new StringBuilder();
        b.append('{');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append('}').toString();
            b.append(", ");
        }
    }

    public static String toString(boolean[][] a){
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "{{}}";

        StringBuilder b = new StringBuilder();
        b.append("{");
        for (int i = 0; ; i++) {
            b.append(toString(a[i]));
            if (i == iMax)
                return b.append('}').toString();
            b.append(", ");
        }
    }

    public static String toString(boolean[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "{}";

        StringBuilder b = new StringBuilder();
        b.append('{');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append('}').toString();
            b.append(", ");
        }
    }
}
