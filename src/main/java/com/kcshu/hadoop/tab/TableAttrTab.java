package com.kcshu.hadoop.tab;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import com.kcshu.hadoop.service.ServerManager;
import com.kcshu.hadoop.task.CallBack;
import com.kcshu.hadoop.task.ExecuteCallBack;
import com.kcshu.hadoop.utils.ExceptionUtil;
import com.kcshu.hadoop.utils.images;

/**
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & Oct 23, 2014 10:53:17 AM
 */
public class TableAttrTab extends AbstractTab{
    
    protected TabFolder attrTab;
    protected Table fieldTable;
    protected Table partitionFieldTable;
    protected Table infoTable;
    protected Table storgeTable;
    protected Table partitonsTable;
    
    protected boolean isPartitionTable = false;
    public TableAttrTab(CTabFolder tabFolder, TreeItem item){
        super(tabFolder, item);
    }
    
    @Override
    public void afterInitView(){
        super.afterInitView();
        self.setImage(images.popmenu.table.attr);
        executeTask();
    }
    
    @Override
    public void initSubView(Composite com){
        com.setLayout(new GridLayout(1, false));
        
        attrTab = new TabFolder(com, SWT.NONE);
        attrTab.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true,true,1,1));

        fieldTable          = initTable("字段",new String[]{"名称","类型","描述"});
        partitionFieldTable = initTable("分区字段",new String[]{"名称","类型","描述"});
        infoTable           = initTable("详细信息",new String[]{"名称","值",""});
        storgeTable         = initTable("存储信息",new String[]{"名称","值",""});
        partitonsTable      = initTable("分区信息",new String[]{"分区"});
        partitonsTable.setHeaderVisible(false);
        
        attrTab.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                if(attrTab.getSelectionIndex() == 4){
                    showPartitions();
                }
            }
        });
    }
    
    public Table initTable(String tabName,String[] fields){
        TabItem tabitem = new TabItem(attrTab, SWT.BORDER);
        tabitem.setText(tabName);
        
        Composite com = new Composite(attrTab, SWT.NONE);
        TableColumnLayout tclayout = new TableColumnLayout();
        com.setLayout(tclayout);
       
        Table table = new Table(com, SWT.NONE);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        for(int i = 0; i < fields.length; i++){
            String field = fields[i];
            TableColumn funName = new TableColumn(table, SWT.NONE);
            funName.setText(field);
            tclayout.setColumnData(funName, new ColumnWeightData(i,150,false));
        }
        tabitem.setControl(com);
        return table;
    }
    
    @Override
    public void executeTask(){
        String table = treeItem.getText();
        String hql = "desc formatted "+table;
        CallBack<List<String[]>> back = new ExecuteCallBack(database,hql){
            @Override
            public void onData(List<String[]> param){
                taskIds.clear();
                param.remove(0);param.remove(0);param.remove(0);
                
                List<String[]> field = new ArrayList<String[]>();
                List<String[]> partitionField = new ArrayList<String[]>();
                List<String[]> info = new ArrayList<String[]>();
                List<String[]> storge = new ArrayList<String[]>();
                
                List<String[]> current = field;
                
                for(int i = 0; i < param.size(); i++){
                    String[] cc = param.get(i);
                    if( (cc[0] == null || "".equals(cc[0])) 
                            && (cc[1] == null || "".equals(cc[1])) 
                            && (cc[2] == null || "".equals(cc[2])) ){
                        continue;
                    }
                    String name = cc[0];
                    if("# Partition Information".equals(name)){
                        current = partitionField;
                        isPartitionTable = true;
                        i++;
                    }else if("# Detailed Table Information".equals(name)){
                        current = info;
                    }else if("# Storage Information".equals(name)){
                        current = storge;
                    }else{
                        current.add(cc);
                    }
                }
                showTableItem(fieldTable,field);
                showTableItem(partitionFieldTable,partitionField);
                showTableItem(infoTable,info);
                showTableItem(storgeTable,storge);
            }
            @Override
            public void onException(Exception e){
                taskIds.clear();
                ExceptionUtil.show(tabFolder.getShell(),e);
                intreputTask();
            }
        };
        taskIds.add(ServerManager.get(getServerId()).execute(back));
    }
    
    public void showTableItem(Table table,List<String[]> items){
        for(String[] strings : items){
            new TableItem(table, SWT.NONE).setText(strings);   
        }
    }
    
    public void showPartitions(){
        if( !isPartitionTable ){
            return;
        }
        if( Boolean.TRUE.equals(partitonsTable.getData("OPEN")) ){
            return;
        }
        String table = treeItem.getText();
        String hql = "show partitions "+table;
        CallBack<List<String[]>> back = new ExecuteCallBack(database,hql){
            @Override
            public void onData(List<String[]> param){
                param.remove(0);
                showTableItem(partitonsTable, param);
                partitonsTable.setData("OPEN",true);
            }
            @Override
            public void onException(Exception e){
                partitonsTable.setData("OPEN",false);
                ExceptionUtil.show(tabFolder.getShell(),e);
                intreputTask();
            }
        };
        ServerManager.get(getServerId()).execute(back);
    }
    
    @Override
    public String getTabTitleType(){
        return "属性";
    }
}
