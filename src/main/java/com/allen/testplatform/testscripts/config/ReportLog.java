package com.allen.testplatform.testscripts.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Reporter;

/**
 * ReportLog
 * 控制台、Reporter日志集中调用
 *
 * @author FanQingChuan
 * @since 2021/11/14 17:41
 */
public class ReportLog{

    private Logger LOGGER;
    
    public ReportLog(Class<?> clazz){
        LOGGER = LoggerFactory.getLogger(clazz);
    }

    public void info(String message,Object... args) {
        String replaceMessage = message.replaceAll("\\{\\}", "%s");
        LOGGER.info(message,args);
        Reporter.log(String.format(replaceMessage, args));
    }

    public void error(String message,Object... args) {
        String replaceMessage = message.replaceAll("\\{\\}", "%s");
        LOGGER.error(message,args);
        Reporter.log(String.format(replaceMessage, args));
    }

}
