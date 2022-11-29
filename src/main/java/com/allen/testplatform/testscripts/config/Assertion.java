package com.allen.testplatform.testscripts.config;


import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Assertion
 * 将testng中Assert改成失败、成功断言后继续执行后续代码
 * @author FanQingChuan
 * @since 2021/10/30 23:58
 */
public class Assertion {

    private static final ReportLog reportLog = new ReportLog(Assertion.class);

    public static boolean flag = true;
    public static List<Error> errors = new ArrayList<Error>();

    public static void assertEquals(Object actual, Object expected, String message) {
        try{
            reportLog.info("=============断 言 中==============");
            Assert.assertEquals(actual, expected, message);
        }catch(Error e){
            errors.add(e);
            flag = false;
        }finally {
            reportLog.info("=============断言结束=============");
        }
    }


    public static void assertListEquals(List<?> actual,List<?> expected,String failMessage) {
        Assert.assertEquals(actual.size(),expected.size(),"测试失败! 2个List长度不一致,停止后续测试");

//        actual.forEach(a -> {
//            expected.forEach(e -> {
//            });
//        });
    }

    /**
     * @Description: 断言List<?>中是否存在某元素
     * @param actual
     * @param element
     * @param failMessage
     */
    public static void assertExist(List<?> actual, Object element, String failMessage){
        boolean rs = false;
        for (int i = 0; i < actual.size(); i++) {
            if (actual.get(i).toString().equals(element.toString())) {
                rs = true;
            }
        }

        try {
            reportLog.info("=============断 言 中==============");
            Assert.assertTrue(rs,failMessage);
        } catch(Error e){
            errors.add(e);
            flag = false;
        }finally {
            reportLog.info("=============断言结束=============");
        }
    }

    /**
     * @Description: 断言List<?>中是否不存在某元素
     * @param actual
     * @param element
     * @param failMessage
     */
    public static void assertNotExist(List<?> actual, Object element, String failMessage){
        boolean rs = true;
        for (int i = 0; i < actual.size(); i++) {
            if (actual.get(i).toString().equals(element.toString())) {
                rs = false;
                break;
            }
        }
        try {
            reportLog.info("=============断 言 中==============");
            Assert.assertTrue(rs,failMessage);
        } catch(Error e){
            errors.add(e);
            flag = false;
        }finally {
            reportLog.info("=============断言结束=============");
        }
    }

    public static void verifyEquals(Object actual, Object expected){
        try{

            reportLog.info("=============断 言 中==============");
            Assert.assertEquals(actual, expected);
        }catch(Error e){
            errors.add(e);
            flag = false;
        }finally {
            reportLog.info("=============断言结束=============");
        }
    }

    public static void verifyEquals(Object actual, Object expected, String failMessage){
        try{
            reportLog.info("=============断 言 中==============");
            Assert.assertEquals(actual, expected, failMessage);
        }catch(Error e){
            errors.add(e);
            flag = false;
        }finally {
            reportLog.info("=============断言结束=============");
        }
    }

    public static void verifyNotEquals(Object actual, Object expected, String failMessage){
        try{
            reportLog.info("=============断 言 中==============");
            Assert.assertNotEquals(actual, expected, failMessage);
        }catch(Error e){
            errors.add(e);
            flag = false;
        }finally {
            reportLog.info("=============断言结束=============");
        }
    }

    public static void verifyNulls(Object actual){
        try{
            reportLog.info("=============断 言 中==============");
            Assert.assertNull(actual);
        }catch(Error e){
            errors.add(e);
            flag = false;
        }finally {
            reportLog.info("=============断言结束=============");
        }
    }

    public static void verifyNulls(Object actual, String failMessage){
        try{
            reportLog.info("=============断 言 中==============");
            Assert.assertNull(actual, failMessage);
        }catch(Error e){
            errors.add(e);
            flag = false;
        }finally {
            reportLog.info("=============断言结束=============");
        }
    }

    public static void verifyNotNulls(Object actual, String failMessage){
        try{
            reportLog.info("=============断 言 中==============");
            Assert.assertNotNull(actual, failMessage);
        }catch(Error e){
            errors.add(e);
            flag = false;
        }finally {
            reportLog.info("=============断言结束=============");
        }
    }

    public static void verifyTrue(Boolean condition, String failMessage){
        try{
            reportLog.info("=============断 言 中==============");
            Assert.assertTrue(condition, failMessage);
        }catch(Error e){
            errors.add(e);
            flag = false;
        }finally {
            reportLog.info("=============断言结束=============");
        }
    }

    public static void verifyNotTrue(Boolean condition, String failMessage){
        try{
            reportLog.info("=============断 言 中==============");
            Assert.assertFalse(condition, failMessage);
        }catch(Error e){
            errors.add(e);
            flag = false;
        }finally {
            reportLog.info("=============断言结束=============");
        }
    }
    //其他需要的断言方法都可以这样写进来.....

}
