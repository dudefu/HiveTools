package com.kcshu.hadoop.tab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kcshu.hadoop.export.Excel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kcshu.hadoop.domain.NodeType;
import com.kcshu.hadoop.editors.MyStyledText;
import com.kcshu.hadoop.editors.MyStyledText.ActionCode;
import com.kcshu.hadoop.editors.SQLLineStyleListener;
import com.kcshu.hadoop.editors.UndoManager;
import com.kcshu.hadoop.service.ServerManager;
import com.kcshu.hadoop.task.CallBack;
import com.kcshu.hadoop.task.ExecuteCallBack;
import com.kcshu.hadoop.utils.i18n;
import com.kcshu.hadoop.utils.images;

/**
 * 查询Tab
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月15日 下午3:04:36
 */
public class QueryTab extends AbstractTab{
    private ToolItem execute;//执行，
    private SelectionAdapter executeAction;
    
    private ToolItem stop;//停止
    private SelectionAdapter stopAction;
    
    private ToolItem save;//保存
    private SelectionAdapter saveAction;
    
    private ToolItem open;//打开
    private SelectionAdapter openAction;
    
    private ToolItem export;//导出
    private SelectionAdapter exportAction;
    
    //上一页,下一页
    private ToolItem previous,next;
    private SelectionAdapter previousAction,nextAction;
    
    private MyStyledText inputCmd;//编辑器HQL

    private CTabFolder outDataTab;

    private String hqlFile = null;
    private List<String> runningHsql = new ArrayList<>();
    public QueryTab(CTabFolder tabFolder, TreeItem item){
        super(tabFolder,item);
    }


    @Override
    public void initSubView(Composite composite){
        composite.setLayout(new GridLayout(1, false));
        initMenu(composite);

        SashForm borderForm = new SashForm(composite, SWT.VERTICAL);
        borderForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        initEditor(borderForm);
        initOutDataTab(borderForm);
        borderForm.setWeights(new int[]{1,1});
    }

    public void initMenu(Composite parent){
        ToolBar tb = new ToolBar(parent, SWT.NONE);
        tb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        execute = new ToolItem(tb, SWT.PUSH);
        execute.setText(i18n.menu.console.toRun);
        execute.setImage(images.console.toRun);
        execute.setEnabled(false);
        executeAction = new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                if( QueryTab.this.canClose() ){
                    executeTask();
                }
                else{
                    MessageDialog.openWarning(tabFolder.getShell(),i18n.dialog.query.executed.title,i18n.dialog.query.executed.message);
                }
            }
        };
        execute.addSelectionListener(executeAction);

        stop = new ToolItem(tb, SWT.PUSH);
        stop.setText(i18n.menu.console.stop);
        stop.setImage(images.console.stop);
        stop.setEnabled(false);
        stopAction = new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e){
                intreputExecute();
            }
        };
        stop.addSelectionListener(stopAction);

        new ToolItem(tb, SWT.SEPARATOR | SWT.BORDER);

        save = new ToolItem(tb, SWT.PUSH);
        save.setText(i18n.menu.console.save);
        save.setImage(images.console.save);
        save.setEnabled(false);
        saveAction = new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                saveFile();
            }
        };
        save.addSelectionListener(saveAction);

        open = new ToolItem(tb, SWT.PUSH);
        open.setText(i18n.menu.console.open);
        open.setImage(images.console.open);
        openAction = new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                openFile();
            }
        };
        open.addSelectionListener(openAction);
        
        export = new ToolItem(tb, SWT.PUSH);
        export.setText(i18n.menu.console.export);
        export.setImage(images.console.export);
        export.setEnabled(false);
        exportAction = new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                exportExcel();
            }
        };
        export.addSelectionListener(exportAction);

/*
        new ToolItem(tb, SWT.SEPARATOR | SWT.BORDER);

        previous = new ToolItem(tb, SWT.PUSH);
        previous.setText(i18n.menu.console.previous);
        previous.setImage(images.console.previous);
        previous.setEnabled(false);
        previousAction = new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){

            }
        };
        previous.addSelectionListener(previousAction);


        next = new ToolItem(tb, SWT.PUSH);
        next.setText(i18n.menu.console.next);
        next.setImage(images.console.next);
        next.setEnabled(false);
        nextAction = new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){

            }
        };
        next.addSelectionListener(nextAction);
*/

    }

    protected void exportExcel(){
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(),SWT.SAVE);
        dialog.setFilterExtensions(new String[]{"*.xls","*.*"});
        String file = dialog.open();
        if(file != null){
            CTabItem selectTabItems = outDataTab.getSelection();
            if(selectTabItems != null){
                Table table = (Table)selectTabItems.getControl();
                Display.getCurrent().syncExec(new Excel(table, file));
            }else{
                MessageDialog.openError(tabFolder.getShell(), "导出？", "请选择您要导出的数据标签！！");
            }
        }
    }

    protected void saveFile(){
        if(hqlFile == null){
            FileDialog dialog = new FileDialog(tabFolder.getShell(),SWT.SAVE);
            dialog.setFilterExtensions(new String[]{"*.sql", "*.hql","*.*"});
            hqlFile = dialog.open();
        }
        if(hqlFile != null){
            save.setEnabled(false);
            FileOutputStream out = null;
            try{
                out = new FileOutputStream(hqlFile);
                String connext = inputCmd.getText();
                out.write(connext.getBytes("UTF-8"));
            }catch(Exception ex){
            }finally{
               IOUtils.closeQuietly(out); 
            }
            this.setText(new File(hqlFile).getName());
        }
    }

    protected void openFile(){
        FileDialog dialog = new FileDialog(tabFolder.getShell());
        dialog.setFilterExtensions(new String[]{"*.sql", "*.hql","*.*"});
        String file = dialog.open();
        if(file != null){
            self.setText(new java.io.File(file).getName());
            hqlFile = file;
            FileInputStream input = null;
            try{
                input = new FileInputStream(hqlFile);
                List<String> lines = IOUtils.readLines(input,"UTF-8");
                inputCmd.setText("");
                for(String line : lines){
                    inputCmd.append(line+"\n");
                }
            }catch(Exception ex){
            }finally{
               IOUtils.closeQuietly(input); 
            }
        }
    }

    public void initEditor(SashForm sashForm){
        inputCmd = new MyStyledText(sashForm, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        inputCmd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        inputCmd.setFont(SWTResourceManager.getFont("凌晨依然", 11, SWT.NORMAL));
        
        inputCmd.addExtendedModifyListener(new ExtendedModifyListener(){
            @Override
            public void modifyText(ExtendedModifyEvent event){
                String sql = inputCmd.getText();
                boolean enabled = !"".equals(sql.trim());
                execute.setEnabled(enabled);
                save.setEnabled(enabled);
            }
        });
        inputCmd.addVerifyKeyListener(new VerifyKeyListener(){
            public void verifyKey(VerifyEvent event){
                if(!canClose()){
                    event.doit = false;
                }
            }
        });
        inputCmd.addLineStyleListener(new SQLLineStyleListener());

        //绑定快捷键
        inputCmd.setKeyBinding('A' | SWT.CTRL, ST.SELECT_ALL);
        inputCmd.setKeyBinding('Z' | SWT.CTRL, ActionCode.UNDO);
        inputCmd.setKeyBinding('Y' | SWT.CTRL, ActionCode.REDO);
        inputCmd.setKeyBinding('F' | SWT.CTRL, ActionCode.CLEAR);
        inputCmd.setKeyBinding('D' | SWT.CTRL, ActionCode.DELETE);
        inputCmd.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent e){
                switch(e.keyCode | e.stateMask){ 
                    //执行
                    case SWT.F9: if(execute.isEnabled()) executeAction.widgetSelected(null); break;
                    //停止
                    case SWT.F7: if(stop.isEnabled()) stopAction.widgetSelected(null);  break;
                    //保存
                    case 's' | SWT.CTRL : 
                    case SWT.F6: if(save.isEnabled()) saveAction.widgetSelected(null); break;
                    //打开
                    case SWT.F10: if(open.isEnabled()) openAction.widgetSelected(null); break;
                    default: break;
                }
            }
        });

        UndoManager undoManager = new UndoManager(50);
        undoManager.connect(inputCmd);

        inputCmd.setUndoManager(undoManager);
        
        defaultSql();

        //TASK 输入框最大化
        sashForm.setMaximizedControl(inputCmd);
    }
    
    public void defaultSql(){
        if(treeItem.getData(NodeType.NODE_TYPE) == NodeType.TABLE){
            StringBuffer where = new StringBuffer();
            StringBuffer hql = new StringBuffer("SELECT\n");
            int childSize = treeItem.getItemCount();
            if( childSize == 0 ){
                hql.append(" * ");
            }else{
                for(int i = 0; i < childSize; i++){
                    TreeItem child = treeItem.getItem(i);
                    if(child.getData(NodeType.NODE_TYPE) == NodeType.FIELD_PARTITION){
                        if(where.length() == 0){
                            where.append("\nWHERE\n\t");
                        }else{
                            where.append("\n\tAND");
                        }
                        where.append(child.getText()).append(" = '' ");
                    }else{
                        if( i != 0 ){
                            hql.append(",");
                        }else{
                            hql.append("\t");
                        }
                        hql.append(child.getText());
                    }
                }   
            }
            hql.append("\nFROM").append(" ").append(treeItem.getText()).append(where);
            inputCmd.setText(hql.toString());
        }
    }

    public void initOutDataTab(SashForm borderForm){
        outDataTab = new CTabFolder(borderForm, SWT.NONE);
        outDataTab.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        outDataTab.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseDoubleClick(MouseEvent e){
                if(getSashForm().getMaximizedControl() != outDataTab){
                    getSashForm().setMaximizedControl(outDataTab);
                }
                else{
                    getSashForm().setMaximizedControl(null);
                }
            }
        });
        outDataTab.addCTabFolder2Listener(new CTabFolder2Adapter(){
            @Override
            public void close(CTabFolderEvent event) {
                super.close(event);
                if(outDataTab.getItemCount() == 1){//最后关闭的那个
                    getSashForm().setMaximizedControl(inputCmd);
                }
            }
        });
    }

    /**
     * 执行框SashForm
     * @return
     */
    public SashForm getSashForm(){
        //取消SQL输入框最大化
        Control[] childrens = ((Composite)this.getControl()).getChildren();
        SashForm sashForm = (SashForm)childrens[1];
        return sashForm;
    }

    public void executeTask(){
        execute.setImage(images.console.executeing);
        execute.setText(i18n.menu.console.executeing);

        stop.setEnabled(true);
        open.setEnabled(false);
        export.setEnabled(false);

        for(CTabItem item : outDataTab.getItems()){
            item.dispose();
        }

        String hql = inputCmd.getText().trim();
        Map<String,String> map = new HashMap<>();
        String[] lines = hql.split("(\r\n|\n)");
        Pattern pattern = Pattern.compile("--[\\s]*(set|SET)[\\s]*([a-zA-Z0-9_]*)[\\s]*=[\\s]*(.*)");
        for (String line : lines){
            Matcher m = pattern.matcher(line);
            if(m.matches()){
                String key = m.group(2);
                String value = m.group(3);
                map.put(key,value);
                hql = hql.replace(line,"");
            }
        }

        for (String sql : hql.split(";")){
            if(!sql.trim().equals("")){
                sql = sql.trim();
                for (String param : map.keySet()){
                    sql = sql.replace("${hiveconf:"+param+"}",map.get(param));
                    sql = sql.replace("${"+param+"}",map.get(param));
                }
                runningHsql.add(sql);
            }
        }
       /*if(!map.isEmpty()){
            inputCmd.append("\r\n\r\n\r\n");
            for(Map.Entry<String,String> entry : map.entrySet()){
                inputCmd.append("--"+entry.getKey()+"="+entry.getValue()+"\r\n");
            }
        }*/
        runHql();
    }
    protected void runHql(){
        if(runningHsql.size()==0){
            intreputExecute();
        }else{
            String sql = runningHsql.remove(0);
            System.out.println(sql);
            CallBack<List<String[]>> back = new ExecuteCallBack(database,sql){
                @Override
                public void onData(List<String[]> param){
                    showDataInTableColumn(param);
                    taskIds.remove(0);
                    runHql();
                }
                @Override
                public void onException(Exception e){
                    taskIds.remove(0);
                    runHql();
                    String title = i18n.dialog.query.executed.title;
                    StringWriter writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    MessageDialog.openError(tabFolder.getShell(), title, writer.toString());
                }
            };
            taskIds.add(ServerManager.get(getServerId()).execute(back));
        }
    }

    public void showDataInTableColumn(List<String[]> objs){
        getSashForm().setMaximizedControl(null);

        CTabItem tabItem = new CTabItem(outDataTab, SWT.BORDER);
        tabItem.setShowClose(true);
        tabItem.setText("Out "+outDataTab.getItemCount());

        Table table = new Table(outDataTab, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        if(objs.size() != 0){
            export.setEnabled(true);
            String[] head = objs.get(0);
            table.setLayout(new GridLayout(head.length, false));
            
            TableColumn lineNumber = new TableColumn(table, SWT.NONE);
            if(objs.size() < 100){
                lineNumber.setWidth(35);
            }else{
                lineNumber.setWidth(50);
            }
            lineNumber.setMoveable(false);
            lineNumber.setResizable(false);
            
            for(int i = 0; i < head.length; i++){
                TableColumn tblclmn = new TableColumn(table, SWT.NONE);
                tblclmn.setWidth(150);
                //设置表头可移动，默认为false  
                tblclmn.setMoveable(true);
                tblclmn.setResizable(true);
                tblclmn.setText(head[i]);
            }
            
            for(int i = 1; i < objs.size(); i++){
                String[] column = objs.get(i);
                TableItem item = new TableItem(table, SWT.NONE);
                item.setText(0,String.valueOf(i));
                for(int j = 0; j < column.length; j++){
                    String string = column[j];
                    if(string == null){
                        string = "";
                    }
                    item.setText(j+1,string);   
                }
            }
            Sorter.addSorter(table);
        }
        tabItem.setControl(table);
        outDataTab.setSelection(tabItem);
    }

    /**
     * 停止执行
     */
    public void intreputExecute(){
        super.intreputTask();
        
        execute.setImage(images.console.toRun);
        execute.setText(i18n.menu.console.toRun);

        stop.setEnabled(false);
        open.setEnabled(true);
        export.setEnabled(tabFolder.getItemCount()>0);
    }
    
    @Override
    public String getTabTitleType(){
        return i18n.tab.query;
    }
}