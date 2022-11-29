package com.allen.testplatform.testscripts.listener;

import com.allen.testplatform.common.utils.CommonUtils;
import com.allen.testplatform.common.utils.DateUtils;
import com.allen.testplatform.testscripts.config.Assertion;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.AndroidTestBase;
import com.allen.testplatform.testscripts.testcase.base.IOSTestBase;
import com.allen.testplatform.testscripts.testcase.base.SpringWebTestBase;
import com.allen.testplatform.testscripts.testcase.base.WebTestBase;
import org.openqa.selenium.WebDriver;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AssertListener extends TestListenerAdapter{

    private static final ReportLog reportLog = new ReportLog(AssertListener.class);

    @Override
    public void onTestStart(ITestResult tr) {
        Class testClazz = tr.getTestClass().getRealClass();
        ITestNGMethod method = tr.getMethod();
        reportLog.info("测试开始======== >>  ClassName:[{}], MethodName: [{}]" , testClazz.getSimpleName(),method.getMethodName());
        Assertion.flag = true;
        Assertion.errors.clear();
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        Class testClazz = tr.getTestClass().getRealClass();
        ITestNGMethod method = tr.getMethod();
        reportLog.info("测试失败======== >>  ClassName:[{}], MethodName: [{}]" , testClazz.getSimpleName(),method.getMethodName());

        //截图
        try {
            autoTakeScreenshotV3(tr,testClazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.handleAssertion(tr);
    }

    private void autoTakeScreenshotV3 (ITestResult tr,Class testClazz) throws Exception {
        //先判断当前class 是否为WebTestBase  AppTestBase SpringWebTestBase -> getDriver
        //再判断父类是否为 WebTestBase 或 AppTestBase  SpringWebTestBase 中1种  -> getDriver
        //先判断当前class 是否为 SpringWithSeleniumTestBase   SpringWithSeleniumTestBase -> getDriver
        //再判断父类是否为 SpringWithSeleniumTestBase 或 SpringWithAndroidTestBase 中1种 SpringWithSeleniumTestBase -> getDriver

        List<String> noUserList = Arrays.asList("SpringTestBase","TestBase","AppTestBase","SpringAppTestBase");
        List<Class<?>> baseClasses = CommonUtils.getClasses("com.allen.testplatform.testscripts.testcase.base")
                .stream()
                .filter(clazz -> clazz.getSimpleName().contains("Base") && !noUserList.contains(clazz.getSimpleName()))
                .collect(Collectors.toList());

        Class<?> targetBaseClass = null;
        if (baseClasses.contains(testClazz)) {
            targetBaseClass = baseClasses.stream().filter(clazz -> clazz.getSimpleName().equals(testClazz.getSimpleName())).findFirst().orElse(null);
            assert targetBaseClass != null;

        }else if (baseClasses.contains(testClazz.getSuperclass())) {
            targetBaseClass = baseClasses.stream().filter(clazz -> clazz.getSimpleName().equals(testClazz.getSuperclass().getSimpleName())).findFirst().orElse(null);
            assert targetBaseClass != null;

        }else {
            /**
             * Notice: 当未继承UI自动化测试相关base类 仍存在失败自动截图的需求
             * 则需case实现  WebDriver getDriver() 及 String takeScreenshot(WebDriver driver,String path)方法
             */
            try {
                Object instance = tr.getInstance();
                Method method1 = testClazz.getMethod("getDriver");
                WebDriver driver = (WebDriver) method1.invoke(instance);
                Method takeScreenshot = testClazz.getMethod("takeScreenshot",WebDriver.class,String.class);

                String formatDate = DateUtils.getTimeSuffix();
                String filePath = tr.getTestClass().getRealClass().getSimpleName().concat("_").concat(tr.getMethod().getMethodName()).concat("_").concat(formatDate).concat(".png");
                String result = (String) takeScreenshot.invoke(instance, driver, filePath);
                reportLog.info("截图成功 ======== >> 路径: [{}]",result);
                tr.setAttribute(tr.getTestClass().getRealClass().getSimpleName().concat(tr.getMethod().getMethodName()),result);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                reportLog.info(" ======== >> 截图失败! 没有实现方法 <? extends WebDriver> getDriver() 及 String takeScreenshot(WebDriver,String)方法");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if (targetBaseClass != null) {
            String fileAbsolutePath = null;
            try {
                Method takeScreenshotMethod = targetBaseClass.getMethod("takeScreenshot", ITestResult.class);
                fileAbsolutePath = (String) takeScreenshotMethod.invoke(tr.getInstance(), tr);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }finally {
                if (fileAbsolutePath != null) {
                    tr.setAttribute(tr.getTestClass().getRealClass().getSimpleName().concat(tr.getMethod().getMethodName()),fileAbsolutePath);
                    reportLog.info("截图成功 ======== >> 路径: [{}]",fileAbsolutePath);
                }else {
                    reportLog.info(" ======== >> 截图失败");
                }
            }
        }
    }

    private void autoTakeScreenshotV2 (ITestResult tr,Class testClazz) throws IOException {
        WebTestBase instance1 = null;
        AndroidTestBase instance2 = null;
        IOSTestBase instance3 = null;
        SpringWebTestBase instance4 = null;

        switch (testClazz.getSimpleName()) {
            case "WebTestBase":
                instance1 = (WebTestBase) tr.getInstance();
                break;
            case "AndroidTestBase":
                instance2 = (AndroidTestBase) tr.getInstance();
                break;
            case "IOSTestBase":
                instance3 = (IOSTestBase) tr.getInstance();
                break;
            case "SpringWebTestBase":
                instance4 = (SpringWebTestBase) tr.getInstance();
                break;
//            case "SpringWebTestBase":
//                instance4 = (SpringWebTestBase) tr.getInstance();
//                break;
//            case "SpringWebTestBase":
//                instance4 = (SpringWebTestBase) tr.getInstance();
//                break;
            default:
                switch (testClazz.getSuperclass().getSimpleName()) {
                    case "TestBase":
                    case "SpringTestBase":
                    case "Object":
                        /**
                         * Notice: 当未继承UI自动化测试相关base类 仍存在失败自动截图的需求
                         * 则需case实现  WebDriver getDriver() 及 String takeScreenshot(WebDriver driver,String path)方法
                         */
                        try {
                            Object instance = tr.getInstance();
                            Method method1 = testClazz.getMethod("getDriver");
                            WebDriver driver = (WebDriver) method1.invoke(instance);
                            Method takeScreenshot = testClazz.getMethod("takeScreenshot",WebDriver.class,String.class);

                            String formatDate = DateUtils.getTimeSuffix();
                            String filePath = tr.getTestClass().getRealClass().getSimpleName().concat("_").concat(tr.getMethod().getMethodName()).concat("_").concat(formatDate).concat(".png");
                            takeScreenshot.invoke(instance, driver, filePath);
                            tr.setAttribute(tr.getTestClass().getRealClass().getSimpleName().concat(tr.getMethod().getMethodName()),filePath);
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "AndroidTestBase":
                        instance2 = (AndroidTestBase) tr.getInstance();
                        break;
                    case "IOSTestBase":
                        instance3 = (IOSTestBase) tr.getInstance();
                        break;
                    case "WebTestBase":
                    case "AndroidDefaultLocalServiceTestBase":
                    case "AndroidCustomLocalServiceTestBase":
                        instance1 = (WebTestBase) tr.getInstance();
                        break;
                    case "SpringWithSeleniumTestBase":
                    case "SpringWithAndroidTestBase":
                        instance4 = (SpringWebTestBase) tr.getInstance();
                        break;
                    default:
                        break;
                }
                break;
        }

        if (instance1 != null) {
            takeScreenshotInWeb(instance1,tr);
        }

        if (instance2 != null) {
            takeScreenshotAndroid(instance2,tr);
        }

        if (instance3 != null) {
            takeScreenshotIOS(instance3,tr);
        }

        if (instance4 != null) {
            takeScreenshotInWebSpring(instance4,tr);
        }
    }

    private void takeScreenshotInWeb(WebTestBase instance, ITestResult tr) throws IOException {
        if (instance.getDriver() != null) {
            String fileAbsolutePath = instance.takeScreenshot(tr, instance.getDriver());
            tr.setAttribute(tr.getTestClass().getRealClass().getSimpleName().concat(tr.getMethod().getMethodName()),fileAbsolutePath);
            reportLog.info("截图成功 ======== >> 路径: [{}]",fileAbsolutePath);
        }else {
            reportLog.info("{} 未初始化 截图失败!",instance.getDriver().getClass().getSimpleName());
        }
    }

    private void takeScreenshotAndroid(AndroidTestBase instance, ITestResult tr) throws IOException {
        if (instance.getDriver() != null) {
            String fileAbsolutePath = instance.takeScreenshot(tr, instance.getDriver());
            tr.setAttribute(tr.getTestClass().getRealClass().getSimpleName().concat(tr.getMethod().getMethodName()),fileAbsolutePath);
            reportLog.info("截图成功 ======== >> 路径: [{}]",fileAbsolutePath);
        }else {
            reportLog.info("{} 未初始化 截图失败!",instance.getDriver().getClass().getSimpleName());
        }
    }

    private void takeScreenshotIOS(IOSTestBase instance, ITestResult tr) throws IOException {
        if (instance.getDriver() != null) {
            String fileAbsolutePath = instance.takeScreenshot(tr, instance.getDriver());
            tr.setAttribute(tr.getTestClass().getRealClass().getSimpleName().concat(tr.getMethod().getMethodName()),fileAbsolutePath);
            reportLog.info("截图成功 ======== >> 路径: [{}]",fileAbsolutePath);
        }else {
            reportLog.info("{} 未初始化 截图失败!",instance.getDriver().getClass().getSimpleName());
        }
    }

    private void takeScreenshotInWebSpring(SpringWebTestBase instance, ITestResult tr) throws IOException {
        if (instance.getDriver() != null) {
            String fileAbsolutePath = instance.takeScreenshot(tr, instance.getDriver());
            tr.setAttribute(tr.getTestClass().getRealClass().getSimpleName().concat(tr.getMethod().getMethodName()),fileAbsolutePath);
            reportLog.info("截图成功 ======== >> 路径: [{}]",fileAbsolutePath);
        }else {
            reportLog.info("{} 未初始化 截图失败!",instance.getDriver().getClass().getSimpleName());
        }
    }


    @Override
    public void onTestSkipped(ITestResult tr) {
        Class testClazz = tr.getTestClass().getRealClass();
        ITestNGMethod method = tr.getMethod();
        reportLog.info("测试Skip======== >>  ClassName:[{}], MethodName: [{}]" , testClazz.getSimpleName(),method.getMethodName());

        this.handleAssertion(tr);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        Class testClazz = tr.getTestClass().getRealClass();
        ITestNGMethod method = tr.getMethod();
        reportLog.info("测试通过======== >>  ClassName:[{}], MethodName: [{}]" , testClazz.getSimpleName(),method.getMethodName());

        this.handleAssertion(tr);
    }

    private int index = 0;

    private void handleAssertion(ITestResult tr){
        if(!Assertion.flag){
            Throwable throwable = tr.getThrowable();
            if(throwable==null){
                throwable = new Throwable();
            }
            StackTraceElement[] traces = throwable.getStackTrace();
            StackTraceElement[] alltrace = new StackTraceElement[0];
            for (Error e : Assertion.errors) {
                StackTraceElement[] errorTraces = e.getStackTrace();
                StackTraceElement[] et = this.getKeyStackTrace(tr, errorTraces);
                StackTraceElement[] message = new StackTraceElement[]{new StackTraceElement("message : "+e.getMessage()+" in method : ", tr.getMethod().getMethodName(), tr.getTestClass().getRealClass().getSimpleName(), index)};
                index = 0;
                alltrace = this.merge(alltrace, message);
                alltrace = this.merge(alltrace, et);
            }
            if(traces!=null){
                traces = this.getKeyStackTrace(tr, traces);
                alltrace = this.merge(alltrace, traces);
            }
            throwable.setStackTrace(alltrace);
            tr.setThrowable(throwable);
            Assertion.flag = true;
            Assertion.errors.clear();
            tr.setStatus(ITestResult.FAILURE);
        }
    }

    private StackTraceElement[] getKeyStackTrace(ITestResult tr, StackTraceElement[] stackTraceElements){
        List<StackTraceElement> ets = new ArrayList<>();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if(stackTraceElement.getClassName().equals(tr.getTestClass().getName())){
                ets.add(stackTraceElement);
                index = stackTraceElement.getLineNumber();
            }
        }
        StackTraceElement[] et = new StackTraceElement[ets.size()];
        for (int i = 0; i < et.length; i++) {
            et[i] = ets.get(i);
        }
        return et;
    }

    private StackTraceElement[] merge(StackTraceElement[] traces1, StackTraceElement[] traces2){
        StackTraceElement[] ste = new StackTraceElement[traces1.length+traces2.length];
        for (int i = 0; i < traces1.length; i++) {
            ste[i] = traces1[i];
        }
        for (int i = 0; i < traces2.length; i++) {
            ste[traces1.length+i] = traces2[i];
        }
        return ste;
    }


}