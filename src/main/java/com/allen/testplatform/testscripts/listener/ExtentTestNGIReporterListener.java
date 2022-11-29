package com.allen.testplatform.testscripts.listener;

import com.allen.testplatform.common.utils.CommonUtils;
import com.allen.testplatform.common.utils.DateUtils;
import com.allen.testplatform.common.utils.StringUtils;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.ResourceCDN;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.model.TestAttribute;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Configuration
@Data
public class ExtentTestNGIReporterListener implements IReporter {

    //生成的路径以及文件名
    @Value("${report.extenttestng.outpath}")
    private String OUTPUT_FOLDER = "test-output" + CommonUtils.SEPARATOR +"reports" +CommonUtils.SEPARATOR;

    //报告文件 默认名称
    @Value("${report.extenttestng.filename}")
    private String FILE_NAME = "ExtentReporter.html";

    //测试报告 默认title
    @Value("${report.extenttestng.title}")
    private String REPORT_NAME = "NewHope 接口自动化测试-测试报告";

    private ExtentReports extent;

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

        if (xmlSuites.size() == 1) {
            String temp = xmlSuites.get(0).getFileName() == null ? "AutoTest.xml" :xmlSuites.get(0).getFileName();
            String formatDate = DateUtils.getTimeSuffix();

            FILE_NAME = StringUtils.subString(temp,"testngxml"+ CommonUtils.SEPARATOR,".xml").concat("-Reporter").concat(formatDate).concat(".html");
            if (FILE_NAME.contains("无法截取目标字符串")) {
                FILE_NAME = "AutoTest-Reporter".concat(formatDate).concat(".html");
            }
        }

        if (suites.size() == 1) {
            REPORT_NAME = suites.get(0).getName().concat("-测试报告");
            if (REPORT_NAME.contains("Default Suite")) {
                REPORT_NAME = "NewHope 自动化测试-测试报告";
            }
        }

        init();

        boolean createSuiteNode = suites.size() > 1;            //是否多suite测试

        for (ISuite suite : suites) {
            Map<String, ISuiteResult> result = suite.getResults();
            //如果suite里面没有任何用例，直接跳过，不在报告里生成
            if(result.size()==0){
                continue;
            }

            //统计suite下的成功、失败、跳过的总用例数
            int suiteFailSize=0;
            int suitePassSize=0;
            int suiteSkipSize=0;

//            ExtentTest extentTest = new ExtentTest();
            ExtentTest suiteTest = null;
            //存在多个suite的情况下，在报告中将同一个一个suite的测试结果归为一类，创建一级节点。
            if(createSuiteNode){
                suiteTest = extent.createTest(suite.getName()).assignCategory(suite.getName());
            }

            boolean createSuiteResultNode = result.size() > 1;          //是否多用例

            for (ISuiteResult r : result.values()) {
                ExtentTest resultNode;
                ITestContext context = r.getTestContext();

                if(createSuiteResultNode){
                    //没有创建suite的情况下，将在SuiteResult的创建为一级节点，否则创建为suite的一个子节点。
                    if( null == suiteTest){
                        resultNode = extent.createTest(r.getTestContext().getName());
                    }else{
                        resultNode = suiteTest.createNode(r.getTestContext().getName());
                    }
                }else{
                    resultNode = suiteTest;
                }

                if(resultNode != null){
//                    resultNode.getModel().setName(r.getTestContext().getName());   // 设置测试节点名字为测试集名称
                    resultNode.getModel().setName(suite.getName()+" : "+r.getTestContext().getName());
                    resultNode.assignCategory();
                    //修改 增加设置标签为测试用例描述
                    resultNode.assignCategory(r.getTestContext().getName());
                    resultNode.getModel().setStartTime(r.getTestContext().getStartDate());
                    resultNode.getModel().setEndTime(r.getTestContext().getEndDate());
                    //统计SuiteResult下的数据
                    int passSize = r.getTestContext().getPassedTests().size();
                    int failSize = r.getTestContext().getFailedTests().size();
                    int skipSize = r.getTestContext().getSkippedTests().size();
                    suitePassSize += passSize;
                    suiteFailSize += failSize;
                    suiteSkipSize += skipSize;

                    if(failSize>0){
                        resultNode.getModel().setStatus(Status.FAIL);
                    }

                    String pfsSize = String.format("Pass: %s ; Fail: %s ; Skip: %s ;",passSize,failSize,skipSize);
                    //页面-测试节点下展示
                    resultNode.getModel().setDescription(pfsSize+" Description: ");
                }

                buildTestNodes(resultNode,context.getFailedTests(), Status.FAIL);
                buildTestNodes(resultNode,context.getSkippedTests(), Status.SKIP);
                buildTestNodes(resultNode,context.getPassedTests(), Status.PASS);

            }

            if(suiteTest!= null){
                suiteTest.getModel().setDescription(String.format("Pass: %s ; Fail: %s ; Skip: %s ;",suitePassSize,suiteFailSize,suiteSkipSize));
                if(suiteFailSize>0){
                    suiteTest.getModel().setStatus(Status.FAIL);
                }
            }

        }
//        for (String s : Reporter.getOutput()) {
//            extent.setTestRunnerOutput(s);
//        }

        extent.flush();
    }

    private void init() {
        if (FILE_NAME.contains(CommonUtils.SEPARATOR)) {
            String[] split = FILE_NAME.split("/|\\\\");
            FILE_NAME = split[split.length - 1];
        }

        //文件夹不存在的话进行创建
        File reportDir= new File(OUTPUT_FOLDER);
        if(!reportDir.exists() && !reportDir.isDirectory()){
            reportDir.mkdir();
        }
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(OUTPUT_FOLDER + FILE_NAME);


        // 设置静态文件的DNS
        //怎么样解决cdn.rawgit.com访问不了的情况
        htmlReporter.config().setResourceCDN(ResourceCDN.EXTENTREPORTS);

        htmlReporter.config().setDocumentTitle("NewHope 自动化测试");
        htmlReporter.config().setReportName(REPORT_NAME);
        htmlReporter.config().setChartVisibilityOnOpen(true);
        htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setCSS(".node.level-1  ul{ display:none;} .node.level-1.active ul{display:block;}");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
        extent.setReportUsesManualConfiguration(true);
    }

    private void buildTestNodes(ExtentTest extenttest, IResultMap tests, Status status) {
        //存在父节点时，获取父节点的标签
        String[] categories=new String[0];
        if(extenttest != null ){
            List<TestAttribute> categoryList = extenttest.getModel().getCategoryContext().getAll();
            categories = new String[categoryList.size()];
            for(int index=0;index<categoryList.size();index++){
                categories[index] = categoryList.get(index).getName();
            }
        }

        ExtentTest test;

        if (tests.size() > 0) {
            //调整用例排序，按时间排序
            Set<ITestResult> treeSet = new TreeSet<>(new Comparator<ITestResult>() {
                @Override
                public int compare(ITestResult o1, ITestResult o2) {
                    return o1.getStartMillis()<o2.getStartMillis()?-1:1;
                }
            });

            treeSet.addAll(tests.getAllResults());

            for (ITestResult result : treeSet) {
                Object[] parameters = result.getParameters();
                String temp = null;
                String name;
                if (parameters.length != 0 ){
                    //如果有参数，则使用参数的toString组合代替报告中的name
                    for(Object param:parameters){
                        if(param != null) {
                            if (temp == null) {
                                temp = "["+param+"]";
                            }else {
                                temp += "["+param+"]";
                            }
                        }
                    }
                }

                if(temp != null) {
                    if (temp.length()>50) {
                        temp = temp.substring(0,49)+"...";
                    }

                    name = result.getMethod().getMethodName().concat(temp);
                }else {
                    name = result.getMethod().getMethodName();
                }

                if(extenttest==null){
                    test = extent.createTest(name);
                }else{
                    //作为子节点进行创建时，设置同父节点的标签一致，便于报告检索。
                    test = extenttest.createNode(name).assignCategory(categories);
                }

                //添加@Test注解中description，非标签展示
                test.getModel().setDescription(" 测试用例描述 "+result.getMethod().getDescription());
                //添加@Test注解中description进标签展示
//                test.assignCategory(result.getMethod().getDescription());

//                test = extent.createTest(result.getMethod().getMethodName());
                for (String group : result.getMethod().getGroups()){
                    test.assignCategory(group);                     //添加测试分组名字进标签
                }

                //添加日志进测试用例里
                List<String> outputList = Reporter.getOutput(result);
                test.log(Status.INFO,"==========开始测试==========");
                for(String output:outputList){
                    //将用例的log输出报告中
                    test.debug(output);
                }

                if (result.getThrowable() != null) {
                    test.log(status, result.getThrowable());

                }
                test.log(status, "测试结果: " + status.toString().toLowerCase() + "ed");
                test.log(Status.INFO,"==========测试结束==========");

                test.getModel().setStartTime(getTime(result.getStartMillis()));
                test.getModel().setEndTime(getTime(result.getEndMillis()));

                /**
                 * 错误截图 放置在extendReporter中
                 * */
                //获取截图的路径
                String key = result.getTestClass().getRealClass().getSimpleName().concat(result.getMethod().getMethodName());
                String ScreenShotPath = (String)result.getAttribute(key);

                if (ScreenShotPath != null) {
                    try {
                        test.fail("报错截图如下,点击放大").addScreenCaptureFromPath(ScreenShotPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Date getTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }

}