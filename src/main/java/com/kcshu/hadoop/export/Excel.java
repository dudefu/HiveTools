package com.kcshu.hadoop.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.kcshu.hadoop.utils.ExceptionUtil;
import com.kcshu.hadoop.utils.i18n;

/**
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月17日 下午3:29:38
 */
public class Excel extends Thread{
    protected Table table;
    protected String filePath;
    protected boolean isSelected = false;
    /**
     * 导出table数据到Excel
     * @param table
     * @param filePath
     * @param isSelected
     */
    public Excel(Table table, String filePath, boolean isSelected){
        this.table = table;
        this.filePath = filePath;
        this.isSelected = isSelected;
    }

    @Override
    public void run(){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        try{
            writeExcel(sheet);
            outExcelStream(workbook);
            MessageDialog.openInformation(table.getShell(), i18n.export.title,i18n.export.message + " " + new File(filePath).getName() );
        }catch(final Exception e){
            MessageDialog.openError(table.getShell(), "Error !", ExceptionUtil.toString(e));
        }
    }

    /**
     * 输出Excel数据流到外部
     * 
     * @param workbook
     * @throws Exception
     */
    protected void outExcelStream(HSSFWorkbook workbook) throws Exception{
        OutputStream gzipOut = null;
        try{
            gzipOut = new FileOutputStream(filePath);
            workbook.write(gzipOut);
            IOUtils.closeQuietly(gzipOut);
        }catch(IOException e){}finally{
            IOUtils.closeQuietly(gzipOut);
        }
    }

    /**
     * @param sheet
     */
    public void writeExcel(HSSFSheet sheet){
        TableColumn[] heads = table.getColumns();
        HSSFRow header = sheet.createRow(0);
        for(int i = 0; i < heads.length; i++){
            HSSFCell cell = header.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(heads[i].getText());
            cell.setCellValue(text);
        }
        
        TableItem[] rows = isSelected ? table.getSelection() : table.getItems();
        for(int i = 0; i < rows.length; i++){
            HSSFRow row = sheet.createRow(i + 1);
            for(int j = 0; j < heads.length; j++){
                row.createCell(j)
                        .setCellValue(new HSSFRichTextString(rows[i].getText(j)));
            }
        }
    }

}
