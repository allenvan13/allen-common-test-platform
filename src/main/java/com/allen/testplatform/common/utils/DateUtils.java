package com.allen.testplatform.common.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Fan QingChuan
 * @since 2021/12/4 12:56
 */
public class DateUtils extends DateUtil {

    public static DateTime addMinute(int minute, DateTime date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strTime = sdf.format(date.getTime() + minute*1000*60);
            Date time = sdf.parse(strTime);
            return DateTime.of(time);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static DateTime subMinute(int minute, DateTime date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strTime = sdf.format(date.getTime() - minute*1000*60);
            Date time = sdf.parse(strTime);
            return DateTime.of(time);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        DateTime dateTime = DateUtil.date();
        System.out.println(dateTime);
        System.out.println(addMinute(5,dateTime));
        System.out.println(subMinute(5,dateTime));
        System.out.println(getTimeStampSuffix());
    }

    /**
     * 加天数
     */
    public static Date addDate(Date date, Integer day) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        //把日期往后增加一天,整数  往后推,负数往前移动
        calendar.add(Calendar.DATE, day);
        //这个时间就是日期往后推一天的结果
        return calendar.getTime();
    }

    public static String getTimeSuffix() {
        Date time = Calendar.getInstance().getTime();
        return new SimpleDateFormat("yyyy_MM_dd_HHmmss_SSS").format(time);
    }

    public static String getDateSuffix() {
        Date time = Calendar.getInstance().getTime();
        return new SimpleDateFormat("yyyy_MM_dd").format(time);
    }

    public static long getTimeStampSuffix() {
        return Calendar.getInstance().getTimeInMillis()/1000L;
    }

}
