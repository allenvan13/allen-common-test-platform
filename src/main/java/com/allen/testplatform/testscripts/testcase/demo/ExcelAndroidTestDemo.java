package com.allen.testplatform.testscripts.testcase.demo;

import com.allen.testplatform.modules.casemanage.model.vo.TestCaseVo;
import com.allen.testplatform.testscripts.config.AppiumCapabilities;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.listener.ExtentTestNGIReporterListener;
import com.allen.testplatform.testscripts.testcase.base.AndroidTestBase;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Android类型 Excel执行方式 自动化测试demo <br>
 * Method 1 :  {@link #testExcelExcute}  正向逻辑执行  <br>
 * Method 2 : {@link #testExcelInvokeExcute}  反射逻辑执行  <br>
 * Method 3 : {@link #testUseBaseMethod}  同时可调用基类方法  <br>
 *
 * ps: 以上2种方式 原理不同 但执行效果类似 具体选择哪种取决于调用者意愿
 * A
 * @author Fan QingChuan
 * @since 2022/6/22 18:01
 */

@Test(description = "Excel方式自动化测试示例-正常逻辑&反射执行逻辑")
@Listeners(value = ExtentTestNGIReporterListener.class)
public class ExcelAndroidTestDemo extends AndroidTestBase {

    @Test(description = "正常逻辑: 操作方法封装在 AndroidTestBase 中, 测试用例中可直接调用  解析Excel -> caseSteps 根据遍历操作编码switch执行各项操作")
    void testExcelExcute(
            @Optional("C:\\Users\\allen\\Desktop\\自动化测试_安卓APP类型测试用例_2022_10_26_145945_622.xlsx") String fileName,
            @Optional("testcase")String sheetName,@Optional("null")Integer headerRowNumber) {

        List<TestCaseVo> caseVoList = analysisExcelUiCase(fileName, sheetName, headerRowNumber);
        excuteExcelUiTest(caseVoList);
    }


    @Test(description = "反射逻辑: 操作封装在 AndroidCommon 通过 AndroidTestBase 中反射获取 AndroidBasePage.class 并实例化 再根据操作编码+分类枚举 获取对应方法 并反射(invoke)执行测试")
    void testExcelInvokeExcute(
            @Optional("C:\\Users\\allen\\Desktop\\自动化测试_安卓APP类型测试用例_2022_10_26_145945_622.xlsx") String fileName,
            @Optional("testcase")String sheetName,@Optional("null")Integer headerRowNumber)
            throws  IllegalAccessException, InstantiationException {

        List<TestCaseVo> caseVoList = analysisExcelUiCase(fileName, sheetName, headerRowNumber);
        invokeExcelUiTest(caseVoList);
    }


    @Test
    void testUseBaseMethod(@Optional("ATE002")String username,@Optional("a123456")String password) {
        //开启Appium服务
        startDefaultBaseService();
        //初始化AndroidDriver
        initBaseAndroidDriver(AppiumCapabilities.getHarmonyJx());
        //点击同意隐私协议
        clickNegatively(LocateType.ID,"cn.host.qc:id/tv_privacy_agree");
        //输入用户名
        inputText(LocateType.ID,"cn.host.qc:id/etAccount",username);
        //输入密码
        inputText(LocateType.ID,"cn.host.qc:id/etPassword",password);
        //点击勾选同意隐私政策
        click(LocateType.ID,"cn.host.qc:id/protocolCb");
        //点击登录
        click(LocateType.ID,"cn.host.qc:id/btnLogin");

        assertToastHasAppeared(10,"toast消息文本");
    }

}
