package com.kcshu.hadoop.tab;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import com.kcshu.hadoop.service.ServerManager;
import com.kcshu.hadoop.task.DescribeFunsCallBack;
import com.kcshu.hadoop.task.ShowFunsCallBack;
import com.kcshu.hadoop.utils.i18n;

/**
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月20日 上午10:58:23
 */
public class ShowFunctionsTab extends AbstractTab{

    protected Table table;
    public ShowFunctionsTab(CTabFolder tabFolder, TreeItem item){
        super(tabFolder, item);
    }

    @Override
    public void initSubView(Composite com){
        TableColumnLayout tclayout = new TableColumnLayout();
        com.setLayout(tclayout);
        
        table = new Table(com, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        
        TableColumn funName = new TableColumn(table, SWT.NONE);
        funName.setText(i18n.tab.funtab.name);
        tclayout.setColumnData(funName, new ColumnWeightData(1,150, false));
        
        TableColumn funDescription = new TableColumn(table, SWT.NONE);
        funDescription.setText(i18n.tab.funtab.description);
        tclayout.setColumnData(funDescription, new ColumnWeightData(2, 200, false));

        TableColumn funExample = new TableColumn(table, SWT.NONE);
        funExample .setText(i18n.tab.funtab.example);
        tclayout.setColumnData(funExample, new ColumnWeightData(2, 200, false));
    }

    @Override
    public void afterInitView(){
        executeTask();
    }
    
    @Override
    public void executeTask(){
        ShowFunsCallBack callBack = new ShowFunsCallBack(database){
            @Override
            public void onData(List<String> param){
                showFunInTable(param);
            }
        };
        ServerManager.get(serverId).execute(callBack);
    }
    
    public void showFunInTable(List<String> param){
        for(int i = 0; i < param.size(); i++){
            final int idx = i;
            String funName  = param.get(i);
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0,funName);
            DescribeFunsCallBack callBack = new DescribeFunsCallBack(database,funName){
                @Override
                public void onData(List<String> param){
                    showDescAndExample(idx,param);
                }
            };
            ServerManager.get(serverId).execute(callBack);
        }
    }
    public void showDescAndExample(int idx,List<String> param){
         TableItem item = table.getItem(idx);
         StringBuffer desc = new StringBuffer();
         StringBuffer example = null;
         for(String string : param){
            if(string.startsWith("Example:")){
                example = new StringBuffer();
                continue;
            }
            if(example == null){
                desc.append(string).append("\n");
            }else{
                example.append(string).append("\n");
            }
        }
        item.setText(1,desc.toString());
        if(example != null){
            item.setText(2,example.toString());   
        }
    }

    @Override
    public String getTabTitleType(){
        return i18n.tab.funs;
    }
}
