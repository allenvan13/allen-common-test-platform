package com.allen.testplatform.common.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ExcelUtils {
    public static HSSFSheet ExcelSheet;
    public static HSSFWorkbook ExcelBook;
    public static HSSFRow Row;
    public static HSSFCell Cell;

    /**
     * 加载Excel
     * @param Path:文件路径
     */
    public static void setExcelFile(String Path) {
        FileInputStream ExcelFile;
        try {
            ExcelFile = new FileInputStream(Path);
            ExcelBook = new HSSFWorkbook(ExcelFile);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 结果回写excel
     *
     * @param Result
     *            ：执行结果回写
     * @param RowNum
     *            ：每次判断关键字的行号
     * @param ColNum
     *            : 固定的结果列
     * @param Path
     *            ：文件路径
     * @param SheetName
     *            ：sheet页
     */
    public static void setCellData(String Result, int RowNum, int ColNum,
                                   String Path, String SheetName) {
        try {
            // 获取到excel的sheet表单
            ExcelSheet = ExcelBook.getSheet(SheetName);
            // 获取sheet表单的行数
            Row = ExcelSheet.getRow(RowNum);
            // 行+列确定单元格
            Cell = Row.getCell(ColNum, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            // 结果写入，如果没有单元格则创建单元格
            if (Cell == null) {
                Cell = Row.createCell(ColNum);
            }
            Cell.setCellValue(Result);
            // 这个操作其实是重新创建了一个excel
            FileOutputStream fileOut = new FileOutputStream(Path);
            ExcelBook.write(fileOut);
            fileOut.flush();// 这个特别注意，需要刷新一下数据，相当于保存
            fileOut.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取excel表格数据
     *
     * @param RowNum
     * @param CloNum
     * @param SheetName
     * @return
     */
    public static String getCellDate(int RowNum, int CloNum, String SheetName) {
        ExcelSheet = ExcelBook.getSheet(SheetName);
        Cell = ExcelSheet.getRow(RowNum).getCell(CloNum);
        // 万一单元格出现int类型，获取却不是String，先设置其单元格值为String;或者在单元格写入数字前单引号'
        Cell.setCellType(CellType.STRING);
        String cellData = Cell.getStringCellValue();
        return cellData;

    }

    /**
     * 获取最后sheet最后一行
     *
     * @param SheetName
     * @return
     */
    public static int getLastRowNums(String SheetName) {
        try {
            ExcelSheet = ExcelBook.getSheet(SheetName);
            int rowCount = ExcelSheet.getPhysicalNumberOfRows();
            // getLastRowNum()+1;//最后一行行标，比行数小1
            return rowCount;
        } catch (Exception e) {
            throw (e);
        }
    }

    public static void insertPicExcel(String filePath, String sheetName) {
        FileOutputStream fileOut = null;
        BufferedImage bufferImg = null;
        // 先把读进来的图片放到一个ByteArrayOutputStream中，以便产生ByteArray
        try {
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            bufferImg = ImageIO.read(new File(filePath));
            ImageIO.write(bufferImg, "png", byteArrayOut);
            HSSFSheet testStepSheet = ExcelBook.getSheet(sheetName);
            // 画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
            HSSFPatriarch patriarch = testStepSheet.createDrawingPatriarch();
            // anchor主要用于设置图片的属性
            HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 255, 255,
                    (short) 1, 1, (short) 5, 8);
            anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE); // setAnchorType(3)不支持这个数值了
            // 插入图片
            patriarch
                    .createPicture(anchor, ExcelBook.addPicture(
                            byteArrayOut.toByteArray(),
                            HSSFWorkbook.PICTURE_TYPE_JPEG));
            fileOut = new FileOutputStream("test.xls");
            // 写入excel文件
            ExcelBook.write(fileOut);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
