package com.allen.testplatform.common.handler;

import com.alibaba.excel.util.BooleanUtils;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.context.RowWriteHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

@Slf4j
public class IOSTestHeadWriteHandler implements RowWriteHandler {

    @Override
    public void afterRowDispose(RowWriteHandlerContext context) {
        if (BooleanUtils.isTrue(context.getHead())) {
            Sheet sheet = context.getWriteSheetHolder().getSheet();
            Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();
            // 在第一行 第二列创建一个批注
            Comment comment = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short)1, 0, (short)2, 1));
            // 输入批注信息
            comment.setString(new XSSFRichTextString("测试步骤序号必填, 步骤顺序不一定要连续 但先后顺序必须正确,否则执行顺序将不可控"));

            Comment comment1 = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 0, 0, (short) 0, 0));
            comment1.setString(new XSSFRichTextString("测试用例编码格式: 团队code_业务code(业务或端口code)_测试功能简称code_编号"));
            // 将批注添加到单元格对象中
            sheet.getRow(0).getCell(1).setCellComment(comment);
            sheet.getRow(0).getCell(0).setCellComment(comment1);
        }
    }
}
