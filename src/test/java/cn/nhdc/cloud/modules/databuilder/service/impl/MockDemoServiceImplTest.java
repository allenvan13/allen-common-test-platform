package com.allen.testplatform.modules.databuilder.service.impl;

import com.allen.testplatform.feign.QualityCheckServiceFeign;
import com.allen.testplatform.feign.vo.ProcessDetailDto;
import com.allen.testplatform.modules.databuilder.mapper.UserCenterMapper;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.service.UcUserService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.xml.bind.ValidationException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@DisplayName("Mockito单元测试用例")
@ExtendWith(SpringExtension.class)
@SpringBootTest
class MockDemoServiceImplTest {

    @InjectMocks
    @Spy
    MockDemoServiceImpl mockDemoServiceImpl = new MockDemoServiceImpl();

    @MockBean
    UcUserService ucUserService;

    @MockBean
    UserCenterMapper ucMapper;

    @MockBean
    QualityCheckServiceFeign processFeign;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testSum() {
        int x = 1,y = 1;
        int expect = 10;
        Mockito.when(mockDemoServiceImpl.sum(x,y)).thenReturn(expect);
        int result = mockDemoServiceImpl.sum(x, y);
        Assertions.assertEquals(expect,result,"测试失败!");
    }

    @Test
    void testGetUser() {
        try {
            mockDemoServiceImpl.getUcUser(null);
        }catch (Exception e) {
            Assertions.assertTrue(e instanceof RuntimeException,"测试失败! 期待异常RuntimeException 实际为其他异常  " + e.getClass());
        }
    }

    @Test
    void testMy() throws ValidationException {
        String createUserName = "NHATE-员工A";
        UcUser user = new UcUser();
        user.setUserId(12345678765432L);
        user.setRealName(createUserName);

//        Mockito.doCallRealMethod().when(ucUserService).getCreateUser(createUserName);
//        Mockito.doReturn(user).when(ucUserService).getCreateUser(createUserName);
        Mockito.doReturn(user).when(mockDemoServiceImpl).getUcUser(createUserName);
        UcUser ucUser = mockDemoServiceImpl.getUcUser(createUserName);
        Assertions.assertNotNull(ucUser.getUserId(),"测试失败! 不为空");
        log.info("{}", JSONObject.toJSONString(ucUser));
    }

    @Test
    void testDemo2() {
        ProcessDetailDto processDetailDto = new ProcessDetailDto();
        processDetailDto.setBanName("XXXXXXXXXXXXXXXXXXXXXXXX");

        Mockito.doReturn(Arrays.asList(processDetailDto)).when(mockDemoServiceImpl).getProcessDetails(12345678L);

        List<ProcessDetailDto> actual = mockDemoServiceImpl.getProcessDetails(12345678L);
        log.info("{}", JSONArray.toJSONString(actual));
        Assert.assertEquals(actual.get(0).getBanName(),"XXXXXXXXXXXXXXXXXXXXXXXX");

    }


}
