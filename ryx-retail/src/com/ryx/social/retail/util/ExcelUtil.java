package com.ryx.social.retail.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import com.ryx.framework.util.MapUtil;

public class ExcelUtil {
	public static final int ZERO = 0;
	public static final int FIRST = 1;
	public static final String EMPTY_HEADERNMAE = "";
	public static final int ONE = 1;
	public static final int TWO = 2;
	public static final int THREE = 3;
	public static final int FOUR = 4;
	public static final int FIVE = 5;

	/**
	 * @param headers 标题
	 * @param result 数据
	 * @param out outputStream输出流对象
	 * @param startRow 标题开始行
	 * @param headerName 标题名称
	 * @throws IOException
	 */
	public static void write(List<Map<String, String>> headers, List<Map<String, Object>> result, OutputStream out, int startRow, String headerName) throws Exception {

		Excel excel = new Excel(headers, startRow, headerName);

		excel.appendRows(result);

		excel.write(out);
	}

	/**
	 * @param headers 标题
	 * @param result 数据
	 * @param out outputStream输出流对象
	 * @param startRow 标题开始行
	 * @param headerName 标题名称
	 * @throws IOException
	 */
	public static void write(String[][] headers, List<Map<String, Object>> result, OutputStream out, int startRow, String headerName)throws Exception {

		Excel excel = new Excel(headers, startRow, headerName);

		excel.appendRows(result);

		excel.write(out);
	}
	
	/**
	 * 
	 * @param mouldPath:模板地址+名称
	 * @param salePath：保存地址+名称
	 * @param itemList：
	 * @throws Exception
	 */
	public static void writeItemExcelByMould(String mouldPath,  OutputStream os, List<Map<String, Object>>itemList ) throws Exception{
		Excel excel = new Excel();
		excel.writeByMould(mouldPath, os, itemList);
		excel.setItemExcel(itemList);
		excel.write(os);
	}
	
	/**
	 * 
	 * @param mouldPath:模板地址+名称
	 * @param salePath：保存地址+名称
	 * @param itemList：
	 * @throws Exception
	 */
	public static void writeTobaccoExcelByMould(String mouldPath,  OutputStream os, List<Map<String, Object>>itemList) throws Exception{
		Excel excel = new Excel();
		excel.writeByMould(mouldPath, os, itemList);
		excel.setTobaccoExcel(itemList);
		excel.write(os);
		
	}
	
	/**
	 * 带合并单元格的标题
	 * @param titleNum 标题总行数
	 * @param oneLevelTitle 一级标题
	 * @param secondLevelTitle 二级标题
	 * @param result 结果集
	 * @param out 输出流对象
	 * @param startRow 开始行
	 * @param headerName Excel文件名称
	 * @throws IOException
	 */
	public static void write(int titleNum,String[][] oneLevelTitle, String[][] secondLevelTitle, List<Map<String, Object>> result, OutputStream out, int startRow, String headerName)throws Exception {
		
		Excel excel = new Excel(titleNum,oneLevelTitle, secondLevelTitle, startRow, headerName);
		
		excel.appendRows(result);
		
		excel.write(out);
	}

	/**
	 * 读取Excel并返回数据
	 * 
	 * @param startRowNum  开始行
	 * @param startColNum 开始列
	 * @param path 文件地址
	 * @return
	 * @throws Exception 
	 */
	public static List<Map<String, Object>> readExcel(int startRowNum, int startColNum, String path) throws Exception {
		Excel excel = new Excel();
		return excel.read(startRowNum, startColNum, path);
	}

}

class Excel {

	private Workbook workbook;
	private Sheet sheet;
	private List<Map<String, String>> headers;
	private int rowIndex = 0;

	public Excel(List<Map<String, String>> headers, int startRow, String headerName) {
		workbook = new HSSFWorkbook();
		sheet = workbook.createSheet();
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cellStyle.setBorderTop((short) 1);
		cellStyle.setBorderRight((short) 1);
		cellStyle.setBorderBottom((short) 1);
		cellStyle.setBorderLeft((short) 1);
		HSSFFont font = (HSSFFont) workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 宽度
		cellStyle.setFont(font);

		this.appendHeader(headers, 2, cellStyle, headerName, startRow);
	}
	
	public Excel(int titleNum,List<Map<String, String>> oneLevelTitles,List<Map<String, String>> secondLevelTitles, int startRow, String headerName) {
		workbook = new HSSFWorkbook();
		sheet = workbook.createSheet();
		
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
				.getIndex());
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cellStyle.setBorderTop((short) 1);
		cellStyle.setBorderRight((short) 1);
		cellStyle.setBorderBottom((short) 1);
		cellStyle.setBorderLeft((short) 1);
		
		this.appendHeader(titleNum, oneLevelTitles, secondLevelTitles, 2, cellStyle, headerName, startRow);
	}

	public Excel() {
	}

	public Excel(String[][] headerArrays, int startRow, String headerName) {
		this(parseArray2List(headerArrays), startRow, headerName);
	}
	
	public Excel(int titleNum,String[][] oneLevelTitleArrays,String[][] secondLevelTitleArrays, int startRow, String headerName) {
		this(titleNum,parseArray2List4Merge(oneLevelTitleArrays),parseArray2List4Merge(secondLevelTitleArrays), startRow, headerName);
	}

	/**
	 * 专为构造函数使用 将list转为array
	 */
	private static List<Map<String, String>> parseArray2List(String[][] arrays) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> map;
		for (String[] array : arrays) {
			map = new HashMap<String, String>();
			map.put("header_name", array[0]);
			map.put("header_value", array[1]);
			list.add(map);
		}
		return list;
	}
	
	private static List<Map<String, String>> parseArray2List4Merge(String[][] arrays) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> map;
		for (String[] array : arrays) {
			map = new HashMap<String, String>();
			map.put("header_name", array[0]);
			map.put("header_value", array[1]);
			map.put("rowspan", array[3]);
			map.put("colspan", array[5]);
			list.add(map);
		}
		return list;
	}
	
	private void appendHeader(List<Map<String, String>> headers, int emptyRow,
			CellStyle cellStyle, String headerName, int startRow) {
		this.headers = headers;
		CellRangeAddress cellRange = new CellRangeAddress(startRow, 1, 0, headers.size() - 1);
		sheet.addMergedRegion(cellRange);// 合并单元格
		Row title = sheet.createRow(0);
		title.setHeight((short) 350);// 行高
		Cell headerCell = title.createCell(0);
		
		// 设置标题水平、垂直居中，字体加粗
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		HSSFFont font = (HSSFFont) workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 宽度
		style.setFont(font);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		headerCell.setCellStyle(style);
		if(!headerName.equals("")) headerCell.setCellValue(headerName);
		
		// 设置合并后的单元格样式
		this.setCellRangeStyle(1, cellRange, sheet, workbook);
		
		// 加空行
		while (emptyRow-- > 0) {
			rowIndex++;
		}
		Row newRow = sheet.createRow(rowIndex++);
		newRow.setHeight((short) 40);// 行高
		int cellIndex = 0;
		for (Map<String, String> header : headers) {
			String headerValue = header.get("header_value"); // 表头
			Cell cell = newRow.createCell(cellIndex++);
			cell.setCellValue(headerValue);
			cell.setCellStyle(cellStyle);
		}
	}

	private void appendHeader(int titleNum,List<Map<String, String>> oneLevelTitle,List<Map<String, String>> secondLevelTitle, int emptyRow, 
			CellStyle cellStyle, String headerName, int startRow) {
		this.headers = secondLevelTitle;
		CellRangeAddress cellRange = new CellRangeAddress(startRow, 1, 0, headers
				.size() - 1);
		sheet.addMergedRegion(cellRange);// 合并单元格
		Row title = sheet.createRow(0);
		title.setHeight((short) 350);// 行高
		Cell headerCell = title.createCell(0);
		
		// 设置标题水平、垂直居中，字体加粗
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		HSSFFont font = (HSSFFont) workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 宽度
		style.setFont(font);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		headerCell.setCellStyle(style);
		if(!headerName.equals("")) headerCell.setCellValue(headerName);
		
		// 设置合并后的单元格样式
		this.setCellRangeStyle(1, cellRange, sheet, workbook);
		
		// 加空行
		while (emptyRow-- > 0) {
			rowIndex++;
		}
		this.mergeLine(titleNum, oneLevelTitle, secondLevelTitle);
	}
	
	private void setNormalStyle(Cell cell) {

	}

	private void mergeLine(int titleRows,List<Map<String, String>> oneLevelTitle,List<Map<String, String>> secondLevelTitle) {
		Row row;
		Cell cell;
		//单元格样式
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
				.getIndex());
		HSSFFont font = (HSSFFont) workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 宽度
		style.setFont(font);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setBorderTop((short) 1);
		style.setBorderRight((short) 1);
		style.setBorderBottom((short) 1);
		style.setBorderLeft((short) 1);
		
		//1.根据传入的行数和标题个数,先创建出基本的表格
		for(int i=rowIndex;i<titleRows+rowIndex;i++){
			row = sheet.createRow(i);
			for(int j=0;j<secondLevelTitle.size();j++){
				cell = row.createCell(j);
			}
		}
		//2.创建一级标题
		int colspan = 0;
		int rowspan = 0;
		int cellIndex = 0;
		for(Map<String, String> map : oneLevelTitle){
			if(cellIndex == oneLevelTitle.size()-1) {
				rowIndex++;
			}
			row = sheet.getRow(rowIndex);//获取第一行
			String headerValue = map.get("header_value"); // 表头
			rowspan = MapUtil.getInt(map, "rowspan");
			colspan = MapUtil.getInt(map, "colspan");
			
			if(rowspan>1 && colspan>1){
				sheet.addMergedRegion(new CellRangeAddress(rowIndex, (short)rowIndex+rowspan-1, cellIndex, colspan-1));
				cell = row.getCell(cellIndex);
				cell.setCellValue(headerValue);
				cell.setCellStyle(style);
				cellIndex += colspan;
				continue;
			}
			if(rowspan>1){
				sheet.addMergedRegion(new CellRangeAddress(rowIndex, (short)rowIndex+rowspan-1, cellIndex, cellIndex));
				cell = row.getCell(cellIndex);
				cell.setCellValue(headerValue);
				cell.setCellStyle(style);
				cellIndex ++;
			}
			if(colspan>1){
				sheet.addMergedRegion(new CellRangeAddress(rowIndex, (short)rowIndex, cellIndex, cellIndex+colspan-1));
				cell = row.getCell(cellIndex);
				cell.setCellValue(headerValue);
				cell.setCellStyle(style);
				cellIndex += colspan;
			}
		}
		//3.创建二级标题
		cellIndex = 0;
		for(Map<String, String> map : secondLevelTitle){
			row = sheet.getRow(rowIndex+1);
			cell = row.createCell(cellIndex);
			cell.setCellValue(map.get("header_value"));
			cell.setCellStyle(style);
			cellIndex++;
		}
		rowIndex+=titleRows;
	}

	public void appendRows(List<Map<String, Object>> rows)throws Exception {
		for (Map<String, Object> row : rows) {
			appendRow(row);
		}
	}

	public void appendRow(Map<String, Object> row) throws Exception {
		Row newRow = sheet.createRow(rowIndex++);
		newRow.setHeight((short) 440);// 行高
		int cellIndex = 0;
		for (Map<String, String> header : headers) {
			String headerName = header.get("header_name"); // 字段名
			String cellValue = MapUtil.getString(row, headerName); // 数据库里查询的字段值
			// 放到单元格中
			Cell cell = newRow.createCell(cellIndex++);
			cell.setCellValue(cellValue);

			CellStyle style = workbook.createCellStyle();
			style.setAlignment(CellStyle.ALIGN_CENTER);
			style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			style.setBorderTop((short) 1);
			style.setBorderRight((short) 1);
			style.setBorderBottom((short) 1);
			style.setBorderLeft((short) 1);
			
			cell.setCellStyle(style);
			
		}
	}

	/**
	 * 设置合并后单元格的样式
	 * 
	 * @param borderWidth
	 *            边框宽度
	 * @param cellRange
	 * @param sheet
	 * @param workbook
	 */
	public void setCellRangeStyle(int borderWidth, CellRangeAddress cellRange, Sheet sheet, Workbook workbook) {
		RegionUtil.setBorderBottom(borderWidth, cellRange, sheet, workbook);
		RegionUtil.setBorderLeft(borderWidth, cellRange, sheet, workbook);
		RegionUtil.setBorderRight(borderWidth, cellRange, sheet, workbook);
		RegionUtil.setBorderTop(borderWidth, cellRange, sheet, workbook);
	}

	public void write(OutputStream out) throws IOException {
		workbook.write(out);
		out.close();
	}

	public void writeByMould(String mouldPath, OutputStream os, List<Map<String, Object>> itemList ) throws Exception {
		
		File fi=new File(mouldPath);//excel模板路径  
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(fi));  

        workbook = new HSSFWorkbook(fs);//读取excel模板  
        sheet = workbook.getSheetAt(0);//读取了模板内所有sheet内容
	}
	
	public void setTobaccoExcel(List<Map<String, Object>> itemList ) throws Exception{
		int mouldTitleNum = 1;
		short heigth = sheet.getRow(mouldTitleNum).getHeight();
		Row row = null;
		CellStyle style = sheet.getRow(mouldTitleNum).getCell(0).getCellStyle();
        int index = 1;
		for (Map<String, Object> item : itemList) {
            row = sheet.createRow(index);
            row.setRowStyle(style);
            row.setHeight(heigth);
            index++;
            
        	setCells(MapUtil.getString(item, "item_bar"), 0, row, style);
        	setCells(MapUtil.getString(item, "item_name"), 1, row, style);
        	setCells(MapUtil.getString(item, "item_kind_name"), 2, row, style);
        	setCells(MapUtil.getString(item, "unit_name"), 3, row, style);
        	setCells(MapUtil.getString(item, "qty_whse"), 4, row, style);
           
        }
	}
	
	public void setItemExcel(List<Map<String, Object>> itemList ) throws Exception{
		int mouldTitleNum = 2;
		short heigth = sheet.getRow(mouldTitleNum).getHeight();
		Row row = null;
		CellStyle style = sheet.getRow(mouldTitleNum).getCell(0).getCellStyle();
		String itemId = "";
        String itemId2 = "";
        int index = 2;
        int column = 0;//列
		for (Map<String, Object> item : itemList) {
            itemId = MapUtil.getString(item, "item_id");
            if (!itemId.equals(itemId2)) {
                row = sheet.createRow( index );
                row.setRowStyle(style);
                row.setHeight(heigth);
                index++;
                column = 7;
            }
            
            itemId2 = itemId;
            if (MapUtil.getInt(item, "unit_ratio") == 1) {
            	setCells(MapUtil.getString(item, "big_bar"), 0, row, style);
            	setCells(MapUtil.getString(item, "item_name"), 1, row, style);
            	setCells(MapUtil.getString(item, "item_kind_name"), 2, row, style);
            	setCells(MapUtil.getString(item, "cost"), 3, row, style);
            	setCells(MapUtil.getString(item, "big_pri4"), 4, row, style);
            	setCells(MapUtil.getString(item, "big_unit_name"), 5, row, style);
            	setCells(MapUtil.getString(item, "qty_whse"), 6, row, style);
            } else {
            	setCells(MapUtil.getString(item, "big_bar"), column++, row, style);
            	setCells(MapUtil.getString(item, "big_pri4"), column++, row, style);
            	setCells(MapUtil.getString(item, "big_unit_name"), column++, row, style);
            	setCells(MapUtil.getString(item, "unit_ratio"), column++, row, style);
            }
        }
	}
	
	private void setCells(String value, int column, Row row, CellStyle style) throws Exception{
		Cell cell = row.createCell(column);
    	cell.setCellValue(value);
    	cell.setCellStyle(style);
	}
	
	/**
	 * 读取Excel
	 * 
	 * @param path
	 *            文件地址
	 * @return
	 * @throws Exception 
	 */
	public List<Map<String, Object>> read(int startRowNum, int startCellNum, String path) throws Exception {
	List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		
		InputStream is = new FileInputStream(path);
		workbook = new HSSFWorkbook(is);
		
		// 1.循环表格
		for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
			Sheet sheet = workbook.getSheetAt(sheetNum);
			if (sheet == null) {
				continue;
			}
			
			// 2.循环行
			for (int rowNum = startRowNum; rowNum <= sheet.getLastRowNum(); rowNum++) {
				Map<String, Object> valueMap = new HashMap<String, Object>();
				Row row = sheet.getRow(rowNum);
				if (row == null) {
					continue;
				}
				// 3.循环列,得到值
				for (int colNum = startCellNum; colNum < row.getLastCellNum(); colNum++) {
					valueMap.put(String.valueOf(colNum), getValue(row.getCell(colNum)));
				}
				resultList.add(valueMap);
			}
		}
		return resultList;
	}

	@SuppressWarnings( { "unused", "static-access" })
	private String getValue(Cell cell) {
		if (cell == null || ("") == cell.toString()) {
			return null;
		}
		if (cell.getCellType() == cell.CELL_TYPE_BOOLEAN) {
			// 返回布尔类型的值
			return String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == cell.CELL_TYPE_NUMERIC) {
			// 返回数值类型的值
			return String.valueOf(cell.getNumericCellValue());
		} else if (cell.getCellType() == cell.CELL_TYPE_STRING) {
			// 返回字符串类型的值
			return cell.getStringCellValue();
		} else if (cell.getCellType() == cell.CELL_TYPE_ERROR) {
			return String.valueOf(cell.getErrorCellValue());
		} else if (cell.getCellType() == cell.CELL_TYPE_FORMULA) {
			return String.valueOf(cell.getCachedFormulaResultType());
		} else {
			return "";
		}
	}
}
