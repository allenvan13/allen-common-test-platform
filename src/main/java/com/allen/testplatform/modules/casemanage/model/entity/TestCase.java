package com.allen.testplatform.modules.casemanage.model.entity;

import cn.nhdc.common.database.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 测试平台-测试用例
 * </p>
 *
 * @author Fan QingChuan
 * @since 2022-06-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("test_case")
public class TestCase extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 测试集ID
     */
    private Long suiteId;

    /**
     * 团队code JX-匠星 XWH-希望云 DWWB-地网无边 JSPT-技术平台 DSJPT-大数据平台 ALL-所有团队
     */
    private String teamCode;

    /**
     * 测试用例编码
     */
    private String caseCode;

    /**
     * 测试用例类型  1-接口测试 2-Web测试 3-安卓APP测试 4-IOS APP测试 9-其他混合测试
     */
    private Integer type;

    /**
     * 用例顺序号
     */
    private Integer sort;

    /**
     * 用例描述
     */
    private String description;

}
