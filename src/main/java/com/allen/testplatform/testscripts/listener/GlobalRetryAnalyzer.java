package com.allen.testplatform.testscripts.listener;

import com.allen.testplatform.testscripts.config.ReportLog;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class GlobalRetryAnalyzer implements IRetryAnalyzer {

    private static final ReportLog reportLog = new ReportLog(GlobalRetryAnalyzer.class);

    private int currentCount = 1;//当前运行的次数

    private static final int maxRetryCount = 2;//允许重试的最大次数

    @Override
    public boolean retry(ITestResult result) {
        if (currentCount <= maxRetryCount) {
            reportLog.info("失败重试======== >> 方法[" + result.getMethod().getMethodName() + "]执行失败,进入失败案例重试模式,正在进行第[" + currentCount + "]次重试");
            currentCount++;
            return true;
        }
        int index = currentCount - 1;
        reportLog.info("退出重试======== >> 共计执行[" + (index+1) + "]次,退出失败重试");
        return false;
    }

    public void resetCount(){
        currentCount = 1;
    }

//    @Override
//    public boolean retry(ITestResult result) {
//        if(currentCount <= maxRetryCount) {
//            reportLog.info("失败重试======== >> 方法[" + result.getMethod().getMethodName() + "]执行失败,进入失败案例重试模式,正在进行第[" + currentCount + "]次重试");
//            currentCount ++;
//            return true;
//        }
//        int index = currentCount - 1;
//        reportLog.info("退出重试======== >> 共计执行[" + (index+1) + "]次,退出失败重试");
//        return false;
//    }
}
