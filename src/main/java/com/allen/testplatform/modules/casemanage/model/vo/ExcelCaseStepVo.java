package com.allen.testplatform.modules.casemanage.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.*;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import lombok.Data;

@Data
@HeadRowHeight(30)
@ContentRowHeight(15)
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 42)
@ContentFontStyle(fontName = "宋体",fontHeightInPoints = 12)
@HeadFontStyle(fontName = "黑体",fontHeightInPoints = 12)
public class ExcelCaseStepVo {

    @ColumnWidth(25)
    @ExcelProperty(value = "测试用例编码",index = 0)
    private String caseCode;

    /**
     * 步骤顺序号
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "测试步骤序号",index = 1)
    private Integer sort;

    /**
     * 步骤描述
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "测试步骤描述",index = 2)
    private String description;

    /**
     * 关键字(操作)编码
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "操作编码",index = 3)
    private String actionKeyword;

    /**
     * 元素定位方式(类型)
     */
    @ColumnWidth(13)
    @ExcelProperty(value = "元素定位方式",index = 4)
    private String elementLocateType;

    /**
     * 元素定位信息
     */
    @ColumnWidth(75)
    @ExcelProperty(value = "元素定位信息",index = 5)
    private String elementLocateValue;

    /**
     * 输入值
     */
    @ColumnWidth(70)
    @ExcelProperty(value = "输入值",index = 6)
    private String parameter;

    /**
     * 测试人员名称
     */
    @ColumnWidth(9)
    @ExcelProperty(value = "测试人员",index = 7)
    private String tester;

    /**
     * 开发团队Code
     */
    @ColumnWidth(9)
    @ExcelProperty(value = "开发团队",index = 8)
    private String teamCode;
}
