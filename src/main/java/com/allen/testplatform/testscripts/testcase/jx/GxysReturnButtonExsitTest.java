package com.allen.testplatform.testscripts.testcase.jx;

import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.listener.AssertListener;
import com.allen.testplatform.testscripts.listener.ExtentTestNGIReporterListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * @author Fan QingChuan
 * @since 2022/8/5 16:09
 */

@Listeners(value = {ExtentTestNGIReporterListener.class, AssertListener.class})
public class GxysReturnButtonExsitTest extends ConmonGxys {

    private static final ReportLog reportLog = new ReportLog(GxysReturnButtonExsitTest.class);


    @Test
    void testXXX() {
        loginAndChooseProcessCheckPart();
    }
}
