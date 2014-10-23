package com.kcshu.hadoop.tab;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
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
public class DatabaseAttrTab extends AbstractTab{

    protected Table table;
    public DatabaseAttrTab(CTabFolder tabFolder, TreeItem item){
        super(tabFolder, item);
    }

    @Override
    public void initSubView(Composite com){
        TableColumnLayout tclayout = new TableColumnLayout();
        com.setLayout(tclayout);
        
        table = new Table(com, SWT.NONE);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true,true,1,1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        TableColumn funName = new TableColumn(table, SWT.NONE);
        funName.setText("属性");
        tclayout.setColumnData(funName, new ColumnWeightData(0,150, false));
        
        TableColumn funDescription = new TableColumn(table, SWT.NONE);
        funDescription.setText("值");
        tclayout.setColumnData(funDescription, new ColumnWeightData(1, 240, true));
    }
    
    @Override
    public void afterInitView(){
        super.afterInitView();
        self.setImage(images.popmenu.database.attr);
        executeTask();
    }

    @Override
    public void executeTask(){
        String hql = "describe database "+database;
        CallBack<List<String[]>> back = new ExecuteCallBack(database,hql){
            @Override
            public void onData(List<String[]> param){
                taskId = null;
                String[] names = param.get(0);
                String[] values = param.get(1);
                for(int i = 0; i < names.length; i++){
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(new String[]{names[i],values[i]});
                }
            }
            
            @Override
            public void onException(Exception e){
                taskId = null;
                ExceptionUtil.show(tabFolder.getShell(),e);
                intreputTask();
            }
        };
        taskId = ServerManager.get(getServerId()).execute(back);
    }

    /**
     * 返回添加到标题上的，类型内容
     * @return
     */
    public String getTabTitleType(){
        return "属性";
    }
}
