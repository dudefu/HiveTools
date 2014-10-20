package com.kcshu.hadoop.tab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kcshu.hadoop.domain.NodeType;
import com.kcshu.hadoop.editors.MyStyledText;
import com.kcshu.hadoop.editors.MyStyledText.ActionCode;
import com.kcshu.hadoop.editors.SQLLineStyleListener;
import com.kcshu.hadoop.editors.UndoManager;
import com.kcshu.hadoop.export.Excel;
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

    private Table table;

    private String hqlFile = null;

    public QueryTab(CTabFolder tabFolder, TreeItem item){
        super(tabFolder,item);
    }

    @Override
    public void initSubView(Composite composite){
        composite.setLayout(new GridLayout(1, false));
        
        initMenu(composite);

        SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        initEditor(sashForm);
        initTable(sashForm);
        sashForm.setWeights(new int[]{1, 2});

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
                if( taskId == null){
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
    }

    protected void exportExcel(){
        FileDialog dialog = new FileDialog(table.getShell(),SWT.SAVE);
        dialog.setFilterExtensions(new String[]{"*.xls","*.*"});
        String file = dialog.open();
        if(file != null){
            Display.getCurrent().syncExec(new Excel(table, file));   
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
                if(taskId != null){
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

    public void initTable(SashForm sashForm){
        table = new Table(sashForm, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    }

    public void executeTask(){
        execute.setImage(images.console.executeing);
        execute.setText(i18n.menu.console.executeing);

        stop.setEnabled(true);
        open.setEnabled(false);
        export.setEnabled(false);
        
        String hql = inputCmd.getText().trim();
        CallBack<List<String[]>> back = new ExecuteCallBack(database,hql){
            @Override
            public void onData(List<String[]> param){
                taskId = null;
                intreputExecute();
                showTableColumn(param);
            }

            @Override
            public void onException(Exception e){
                taskId = null;
                String title = i18n.dialog.query.executed.title;
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                MessageDialog.openError(tabFolder.getShell(), title, writer.toString());
                intreputExecute();
            }
        };
        taskId = ServerManager.get(getServerId()).execute(back);
    }

    public void showTableColumn(List<String[]> objs){
        table.removeAll();
        table.clearAll();
        
        for( TableColumn tc : table.getColumns() ){
            tc.dispose();
        }
        
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
        export.setEnabled(false);
    }
    
    @Override
    public String getTabTitleType(){
        return i18n.tab.query;
    }
}