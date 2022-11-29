package com.allen.testplatform.common.handler;

import com.allen.testplatform.common.enums.TestTeamEnum;
import com.allen.testplatform.testscripts.enums.WebActionTypeEnum;
import com.allen.testplatform.testscripts.enums.LocateTypeEnum;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.List;

public class WebTestSheetWriteHandler implements SheetWriteHandler {

    @Override
    public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {

    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {

        List<String> actionKeywords = WebActionTypeEnum.getAllKeyword();
        List<String> actionDescriptions = WebActionTypeEnum.getAllDescription();
        List<String> locateTypes = LocateTypeEnum.getAllType();
        List<String> locateDescriptions = LocateTypeEnum.getAllDescription();
        List<String> teamCodes = TestTeamEnum.getAllTeamCode();
        List<String> teamNames = TestTeamEnum.getAllTeamName();

        Workbook workbook = writeWorkbookHolder.getWorkbook();

        //字体备用 内容-宋体12  标题-黑体12
        Font headFont = workbook.createFont();
        headFont.setFontName("黑体");
        headFont.setFontHeightInPoints((short) 12);

        Font contentFont = workbook.createFont();
        contentFont.setFontName("宋体");
        contentFont.setFontHeightInPoints((short) 12);

        //1.创建字典表sheet
        String sheetName = "description";
        Sheet proviceSheet = workbook.createSheet(sheetName);
        proviceSheet.setColumnWidth(0,25*256);
        proviceSheet.setColumnWidth(1,110*256);
        proviceSheet.setColumnWidth(2,20*256);
        proviceSheet.setColumnWidth(3,50*256);
        proviceSheet.setColumnWidth(4,10*256);
        proviceSheet.setColumnWidth(5,10*256);
        // 设置隐藏
//        workbook.setSheetHidden(1,true);
        //设置标题header样式
        Row headerRow = proviceSheet.createRow(0);
        headerRow.createCell(0).setCellValue("操作编码简介");
        headerRow.createCell(2).setCellValue("定位方式");
        headerRow.createCell(4).setCellValue("开发团队");
        //合并标题表头
        CellRangeAddress region1 = new CellRangeAddress(0,0,0,1);
        proviceSheet.addMergedRegion(region1);
        CellRangeAddress region2 = new CellRangeAddress(0,0,2,3);
        proviceSheet.addMergedRegion(region2);
        CellRangeAddress region3 = new CellRangeAddress(0,0,4,5);
        proviceSheet.addMergedRegion(region3);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(contentFont);
        headerStyle.setAlignment(HorizontalAlignment.LEFT);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFillBackgroundColor(IndexedColors.LIGHT_BLUE.index);
        headerRow.setRowStyle(headerStyle);

//        CellStyle cellStyle = workbook.createCellStyle();
//        cellStyle.setFillBackgroundColor(IndexedColors.LIGHT_ORANGE.index);
//        cellStyle.setAlignment(HorizontalAlignment.LEFT);
//        cellStyle.setFont(contentFont);
        //2.循环赋值（为了防止下拉框的行数与隐藏域的行数相对应，将隐藏域加到结束行之后）
        for (int i = 0, length = actionKeywords.size(); i < length; i++) {
            // i:表示你开始的行数  0表示你开始的列数
            Row row = proviceSheet.createRow(i + 1);
            row.createCell(0).setCellValue(actionKeywords.get(i));
            row.createCell(1).setCellValue(actionDescriptions.get(i));

            if (i < locateTypes.size()) {
                row.createCell(2).setCellValue(locateTypes.get(i));
                row.createCell(3).setCellValue(locateDescriptions.get(i));
            }

            if (i < teamCodes.size()) {
                row.createCell(4).setCellValue(teamCodes.get(i));
                row.createCell(5).setCellValue(teamNames.get(i));
            }
        }

//        Sheet sheetDes = workbook.getSheet(sheetName);
//
//        for (int i = 0; i < sheetDes.getLastRowNum(); i++) {
//            Row row = sheetDes.getRow(i);
//            for (int j = 0; j < row.getLastCellNum(); j++) {
//                row.getCell(j).setCellStyle(headerStyle);
//            }
//        }

        //添加下拉列表-操作编码
        Sheet sheet = workbook.getSheet("testcase");
        String formula1 = sheetName + "!$A$2:$A$" + (actionKeywords.size()+10);
        sheet.addValidationData(SetDataValidation(workbook,sheetName,formula1,1,20000,3,3));
        //添加下拉列表-定位方式
        String formula2 = sheetName + "!$C$2:$C$" + (locateTypes.size()+10);
        sheet.addValidationData(SetDataValidation(workbook,sheetName,formula2,1,20000,4,4));
        //添加下拉列表-开发团队
        String formula3 = sheetName + "!$E$2:$E$" + (teamCodes.size()+10);
        sheet.addValidationData(SetDataValidation(workbook,sheetName,formula3,1,20000,8,8));

    }

    public DataValidation SetDataValidation(Workbook wb,String sheetName,String strFormula, int firstRow, int endRow, int firstCol, int endCol) {
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        DataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) wb.getSheet(sheetName));
        DataValidationConstraint formulaListConstraint = dvHelper.createFormulaListConstraint(strFormula);
        return dvHelper.createValidation(formulaListConstraint, regions);
    }
}
