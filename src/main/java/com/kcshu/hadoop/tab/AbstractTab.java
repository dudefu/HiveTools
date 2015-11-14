package com.kcshu.hadoop.tab;

import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

import com.kcshu.hadoop.domain.NodeType;
import com.kcshu.hadoop.domain.TabType;
import com.kcshu.hadoop.service.ServerManager;
import com.kcshu.hadoop.utils.images;

/**
 * 上层抽象Tab
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月15日 下午3:04:36
 */
public abstract class AbstractTab extends CTabItem implements Tab{

    protected UUID id;//tabId主要用户标示
    protected String serverId;//serverId主要用户获取server信息
    protected String database = "";//当前处于哪个database下面,
    protected List<UUID> taskIds = new ArrayList<>();

    protected TreeItem treeItem;
    protected CTabFolder tabFolder;
    protected CTabItem self;
    
    public AbstractTab(CTabFolder tabFolder, TreeItem item){
        super(tabFolder, SWT.NONE);
        this.treeItem = item;
        this.tabFolder = tabFolder;
        self = this;
        initView();
        afterInitView();
    }

    /**
     * 初始化框架布局
     */
    protected void initView(){
        String tabName = null;
        
        NodeType nodeType = (NodeType)treeItem.getData(NodeType.NODE_TYPE);
        if(nodeType.equals(NodeType.SERVER)){
            serverId = treeItem.getData().toString();
            String db = ServerManager.get(serverId).getServer().getDb();
            if(!"".equals(db)){
                database = db;   
            }
            tabName = getTabTitleType() + "@" + treeItem.getText();
        }else if(nodeType.equals(NodeType.DATABASE)){
            database = treeItem.getText();
            TreeItem serverItem = treeItem.getParentItem();
            tabName = getTabTitleType() + "@" + serverItem.getText() + "/" + database;
            serverId = serverItem.getData().toString();
        }else if(nodeType.equals(NodeType.TABLE)){
            TreeItem databaseTreeItem = treeItem.getParentItem();
            database = databaseTreeItem.getText();
            TreeItem serverItem = databaseTreeItem.getParentItem();
            tabName = getTabTitleType() + "@" + serverItem.getText() + "/" + database + "/" + treeItem.getText();
            serverId = serverItem.getData().toString();
        }
        
        self.setShowClose(true);
        self.setImage(images.tab);
        self.setText(tabName);

        //设置识别属性
        id = UUID.randomUUID();
        self.setData(serverId);
        self.setData(TAB_ID, id);
        self.setData(TAB_TYPE, TabType.QUERY);
        self.setData(TAB_QUERY_EXECUTE, false);
        
        self.addDisposeListener(new DisposeListener(){
            @Override
            public void widgetDisposed(DisposeEvent e){
                interruptedTask();
            }
        });

        //添加内容
        Composite composite = new Composite(tabFolder, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        initSubView(composite);
        self.setControl(composite);
    }

    public abstract void initSubView(Composite com);
  
    public abstract void executeTask();

    /**
     * 停止执行
     */
    public void interruptedTask(){
        for (UUID taskId : taskIds){
            ServerManager.get(serverId).killTask(taskId);
        }
        taskIds.clear();
    }

    public void afterInitView(){
        
    }
    
    @Override
    public boolean close(){
        interruptedTask();
        dispose();
        return true;
    }
    
    @Override
    public boolean canClose(){
        return taskIds.size()==0;
    }

    public UUID getId(){
        return id;
    }
    public String getServerId(){
        return serverId;
    }
    
    /**
     * 返回添加到标题上的，类型内容
     * @return
     */
    public String getTabTitleType(){
        return "";
    }
}