package com.kcshu.hadoop.tab;

import java.text.Collator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月20日 上午10:12:09
 */
public class Sorter{
    
    public static final void addSorter(Table table){
        TableColumn[] columns = table.getColumns();
        for(TableColumn tableColumn : columns){
            Sorter.addHeaderSorter(table, tableColumn);   
        }
    }
    
    /***
     * 给table添加排序
     * @param table
     * @param column
     */
    public static void addHeaderSorter(final Table table, final TableColumn column){
        int columnIndex = _getColumnIndex(table, column);
        for(TableItem tableItem : table.getItems()){
            String value = tableItem.getText(columnIndex);
            if("".equals(value)){
                continue;
            }
            try{
                Float.parseFloat(value);
            }catch(Exception e){
                addStringSorter(table, column);
                return ;
            }
        }
        addNumberSorter(table, column);
    }
    
    private static void addNumberSorter(final Table table,final TableColumn column){
        column.addListener(SWT.Selection, new Listener(){
            boolean isAscend = true; // 按照升序排序
            public void handleEvent(Event e){
                int columnIndex = _getColumnIndex(table, column);
                TableItem[] items = table.getItems();
                //使用冒泡法进行排序
                for(int rowIdx = 1; rowIdx < items.length; rowIdx++){
                    String strvalue2 = items[rowIdx].getText(columnIndex);
                    boolean isNull = false;
                    if(strvalue2.equals("")){
                        isNull = true;
                        break;
                    }
                    for(int j = 0; j < rowIdx; j++){
                        String strvalue1 = items[j].getText(columnIndex);
                        boolean isLessThan = true;
                        if("".equals(strvalue1)){
                            isLessThan = isAscend ;
                        }else{
                            isLessThan = isNull ? !isAscend : (Double.parseDouble(strvalue2) < Double.parseDouble(strvalue1));
                        }
                        if((isAscend && isLessThan) || (!isAscend && !isLessThan)){
                            String[] values = _getTableItemText(table, items[rowIdx]);
                            Object obj = items[rowIdx].getData();
                            items[rowIdx].dispose();
                            TableItem item = new TableItem(table, SWT.NONE, j);
                            item.setText(values);
                            item.setData(obj);
                            items = table.getItems();
                            break;
                        }
                    }
                }
                table.setSortColumn(column);
                table.setSortDirection((isAscend ? SWT.UP : SWT.DOWN));
                isAscend = !isAscend;
            }
        });
    }

    
    private static void addStringSorter(final Table table,final TableColumn column){
        column.addListener(SWT.Selection, new Listener(){
            boolean isAscend = true; // 按照升序排序
            Collator comparator = Collator.getInstance();
            public void handleEvent(Event e){
                int columnIndex = _getColumnIndex(table, column);
                TableItem[] items = table.getItems();
                //使用冒泡法进行排序
                for(int rowIdx = 1; rowIdx < items.length; rowIdx++){
                    String strvalue2 = items[rowIdx].getText(columnIndex);
                    for(int j = 0; j < rowIdx; j++){
                        String strvalue1 = items[j].getText(columnIndex);
                        boolean isLessThan = comparator.compare(strvalue2, strvalue1) < 0;   
                        if((isAscend && isLessThan) || (!isAscend && !isLessThan)){
                            String[] values = _getTableItemText(table, items[rowIdx]);
                            Object obj = items[rowIdx].getData();
                            items[rowIdx].dispose();
                            TableItem item = new TableItem(table, SWT.NONE, j);
                            item.setText(values);
                            item.setData(obj);
                            items = table.getItems();
                            break;
                        }
                    }
                }
                table.setSortColumn(column);
                table.setSortDirection((isAscend ? SWT.UP : SWT.DOWN));
                isAscend = !isAscend;
            }
        });
    }

    private static int _getColumnIndex(Table table, TableColumn column){
        TableColumn[] columns = table.getColumns();
        for(int i = 0; i < columns.length; i++){
            if(columns[i].equals(column)) return i;
        }
        return -1;
    }

    private static String[] _getTableItemText(Table table, TableItem item){
        int count = table.getColumnCount();
        String[] strs = new String[count];
        for(int i = 0; i < count; i++){
            strs[i] = item.getText(i);
        }
        return strs;
    }
}
