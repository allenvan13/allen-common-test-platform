package com.allen.testplatform.testscripts.testcase.demo;

import com.allen.testplatform.config.CurrentEnvironmentConfig;
import com.allen.testplatform.feign.QualityCheckServiceFeign;
import com.allen.testplatform.feign.vo.ProcessDetailDto;
import com.allen.testplatform.modules.databuilder.mapper.UserCenterMapper;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.service.UcUserService;
import com.allen.testplatform.modules.databuilder.service.impl.MockDemoServiceImpl;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.SpringTestBase;
import com.alibaba.fastjson.JSONArray;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.annotation.Resource;
import javax.xml.bind.ValidationException;
import java.util.Arrays;
import java.util.List;

/**
 * mock测试示例 <br>
 * 使用场景: 测试时应根据情况选择是否mock 以下条件适宜mock<br>
 * 1、某功能、接口、方法暂未开发实现，但已知相关返回参数或类型<br>
 * 2、依赖对象难以创建 或 依赖方api功能调用困难 或 触发某些功能条件苛刻<br>
 * 3、不需要真实的调用对方方法,不需要真实的连接数据库拿取或写入数据<br>
 *
 * 使用方法:请看源码注释说明  See examples in javadoc for {@link Mockito} class
 *
 * @author Fan QingChuan
 * @since 2022/6/8 9:56
 */

//@RunWith(SpringJUnit4ClassRunner.class)
//@RunWith(SpringRunner.class)
//@RunWith(JUnitRunner.class)
@Test(description = "测试示例-mockito运用")
public class MockitotTestDemo extends SpringTestBase {

    private static final ReportLog reportLog = new ReportLog(MockitotTestDemo.class);

    @Resource
    private CurrentEnvironmentConfig currentEnv;

    @InjectMocks
    @Spy
    MockDemoServiceImpl mockDemoService = new MockDemoServiceImpl();

    @MockBean
    UcUserService ucUserService;

    @MockBean
    QualityCheckServiceFeign processFeign;

    @MockBean
    UserCenterMapper ucMapper;


    @BeforeClass
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @AfterClass
    public void tearDown(){
    }

    @Test(description = "mock测试 打桩基础")
    void test002(){
        int x = 1,y = 1;
        int expect = 10;
        Mockito.when(mockDemoService.sum(x,y)).thenReturn(expect);
        int result = mockDemoService.sum(x, y);
        Assert.assertEquals(expect,result,"测试失败!");
    }

    @Test(description = "mock测试-断言异常类型")
    void test003() {
        try {
            mockDemoService.getUcUser(null);
        }catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException, "测试失败! 期待异常RuntimeException 实际为其他异常  " + e.getClass());
        }
    }

    @Test
    void test004() throws ValidationException {
        String createUserName = "NHATE-员工A";
        UcUser expectUser = new UcUser();
        expectUser.setUserId(12345678765432L);
        expectUser.setRealName(createUserName);

        Mockito.doCallRealMethod().when(ucUserService).getCreateUser(createUserName);
//        Mockito.doReturn(expectUser).when(mockDemoService).getUcUser(createUserName);

        UcUser resultUser = mockDemoService.getUcUser(createUserName);
        Assert.assertEquals(resultUser,expectUser,"测试失败! 返回结果: "+resultUser);
    }

    @Test
    void test005() {
        List<Integer> mockList = Mockito.mock(List.class);
        mockList.add(1);
        mockList.add(2);
        mockList.add(3);
        mockList.add(4);
        mockList.add(1);
        Mockito.when(mockList.get(0)).thenReturn(66);
        Mockito.when(mockList.get(2)).thenThrow(new RuntimeException("运行时异常!"));
        reportLog.info("{}", mockList.get(0));
        mockList.get(2);
    }

    @Test
    void test006() {
        ProcessDetailDto processDetailDto = new ProcessDetailDto();
        processDetailDto.setBanName("XXXXXXXXXXXXXXXX");
        processDetailDto.setCheckId(1111111111111111L);

        Mockito.doReturn(Arrays.asList(processDetailDto)).when(mockDemoService).getProcessDetails(456523456542L);

        List<ProcessDetailDto> resultList = mockDemoService.getProcessDetails(456523456542L);
        Assert.assertEquals(resultList.size(),1,"测试失败 " + JSONArray.toJSONString(resultList));
    }


}
