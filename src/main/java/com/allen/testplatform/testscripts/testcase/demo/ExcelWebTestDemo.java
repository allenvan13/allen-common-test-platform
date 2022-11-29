package com.allen.testplatform.testscripts.testcase.demo;

import com.allen.testplatform.modules.casemanage.model.vo.TestCaseVo;
import com.allen.testplatform.testscripts.listener.ExtentTestNGIReporterListener;
import com.allen.testplatform.testscripts.testcase.base.WebTestBase;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Web类型 Excel执行方式 自动化测试demo <br>
 * Method 1 :  {@link #testExcelExcute}  正向逻辑执行  <br>
 * Method 2 : {@link #testExcelInvokeExcute}  反射逻辑执行  <br>
 * Method 3 : {@link #testUseBaseMethod()}  同时可调用基类方法  <br>
 *
 * ps: 以上2种方式 原理不同 但执行效果类似 具体选择哪种取决于调用者意愿
 *
 * @author Fan QingChuan
 * @since 2022/6/22 18:01
 */
//@Test(description = "Excel方式自动化测试示例-正常逻辑&反射执行逻辑")
@Listeners(value = ExtentTestNGIReporterListener.class)
public class ExcelWebTestDemo extends WebTestBase {

    @Test(description = "正向逻辑: 操作方法封装在 WebTestBase 中, 测试用例中可直接调用  解析Excel -> caseSteps 根据遍历操作编码switch执行各项操作")
    void testExcelExcute(
            @Optional("C:\\Users\\allen\\Desktop\\自动化测试_Web类型测试用例_2022_10_25_103024_449.xlsx") String fileName,
            @Optional("testcase")String sheetName,@Optional("null")Integer headerRowNumber) {

        List<TestCaseVo> caseVoList = analysisExcelUiCase(fileName, sheetName, headerRowNumber);
        excuteExcelUiTest(caseVoList);
    }

    @Test(description = "反射逻辑: 操作封装在 WebCommon 通过 WebTestBase 中反射获取BasePage.class 并实例化 再根据操作编码+分类枚举 获取对应方法 并反射(invoke)执行测试")
    void testExcelInvokeExcute(
            @Optional("C:\\Users\\allen\\Desktop\\自动化测试_Web类型测试用例_2022_10_25_103024_449.xlsx") String fileName,
            @Optional("testcase")String sheetName,@Optional("null")Integer headerRowNumber)
            throws  IllegalAccessException, InstantiationException {

        List<TestCaseVo> caseVoList = analysisExcelUiCase(fileName, sheetName, headerRowNumber);
        invokeExcelUiTest(caseVoList);
    }

    @Test(enabled = false,
            description = "直接调用SeleniumTestBase方法进行测试")
    void testUseBaseMethod() {
        //初始化(实例化)driver 注意此处实例化的SeleniumTestBase基类中的WebDriver
        initBaseBrowser("chrome:102");
        //打开后台登录地址
        openUrl("http://uat-jxadmin.host.cn/#/cooperation-manage/home");
        //输入用户名
        inputText("id","username","ATE001");
        //输入密码
        inputText("id","password","a123456");
        //点击登录
        click("id","login-button");
        //断言是否成功登录
        assertElementText("xpath","/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span","NHATE-员工A");
        //关闭浏览器
        closeBrowser();
    }



}
