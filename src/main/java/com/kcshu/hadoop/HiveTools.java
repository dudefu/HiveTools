package com.kcshu.hadoop;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.kcshu.hadoop.dialog.AboutDialog;
import com.kcshu.hadoop.dialog.AddServerDialog;
import com.kcshu.hadoop.domain.NodeType;
import com.kcshu.hadoop.domain.Server;
import com.kcshu.hadoop.service.ServerManager;
import com.kcshu.hadoop.tab.QueryTab;
import com.kcshu.hadoop.tab.DatabaseAttrTab;
import com.kcshu.hadoop.tab.ShowFunctionsTab;
import com.kcshu.hadoop.tab.Tab;
import com.kcshu.hadoop.tab.TableAttrTab;
import com.kcshu.hadoop.task.DescribeCallBack;
import com.kcshu.hadoop.task.ShowDatabasesCallBack;
import com.kcshu.hadoop.task.ShowTablesCallBack;
import com.kcshu.hadoop.utils.ExceptionUtil;
import com.kcshu.hadoop.utils.i18n;
import com.kcshu.hadoop.utils.images;

public class HiveTools{

    protected Shell shell;

    protected Menu menuServer; // 上层菜单

    protected Menu serverPopMenuClosed; // 服务弹出菜单,
    protected Menu serverPopMenuOpen; // 服务弹出菜单,

    protected Menu nullPopMenu; // 服务弹出菜单

    protected Menu databasesPopMenu;//database Pop Menu
    protected Menu tablePopMenu; // table Pop Menu

    protected Tree tree; // 左侧服务树
    protected TreeItem rootServers; // 左侧服务数ROOT

    protected CTabFolder tabFolder;

    /**
     * Launch the application.
     * 
     * @param args
     */
    public static void main(String[] args){
        HiveTools window = new HiveTools();
        window.open();
    }

    /**
     * Open the window.
     */
    public void open(){
        Display display = Display.getDefault();
        i18n.init();
        images.regShell(display);

        createContents();

        shell.open();
        shell.layout();

        reflushServiceTree();

        while(!shell.isDisposed()){
            try{
                if(!display.readAndDispatch()){
                    display.sleep();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        display.dispose();
    }

    /**
     * Create contents of the window.
     */
    protected void createContents(){
        initShell();
        initMenu();
        initPopMenu();
        initSash();
    }

    /**
     * 初始化所有服务
     */
    private void reflushServiceTree(){
        List<Server> services = Config.services();
        if(services.size() == 0){
            addServer();
        }
        else{
            rootServers.removeAll();
            for(Server server : services){
                addServerTreeItem(server);
            }
            rootServers.setExpanded(true);
            rootServers.setData(NodeType.NODE_ITEM_OPENED, true);
        }
    }

    private void initShell(){
        shell = new Shell();
        shell.addListener(SWT.Close, new Listener(){
            public void handleEvent(Event event){
                event.doit = exitApplication();
            }
        });
        shell.setText(i18n.display.title);
        shell.setLayout(new GridLayout(1, false));
        shell.setImage(images.appIcon);
        shell.setMinimumSize(600, 480);
        shell.setMaximized(true);
    }

    private void initMenu(){
        Menu menu = new Menu(shell, SWT.BAR);
        initMenuServer(menu);
        //initMenuTools(menu);
        initMenuHelp(menu);
        shell.setMenuBar(menu);
    }

    private void initPopMenu(){
        initNullPopMenu();
        initServerPopMenu();
        initDatabasesPopMenu();
        initTablePopMenu();
    }

    private void initNullPopMenu(){
        nullPopMenu = new Menu(shell);

        createItem(nullPopMenu,
                i18n.pop.nul.newCon, images.popmenu.nul.add,
                new SelectionAdapter(){
                    @Override
                    public void widgetSelected(SelectionEvent arg0){
                        addServer();
                    }
                });

        new MenuItem(nullPopMenu, SWT.SEPARATOR);

        createItem(nullPopMenu,
                i18n.pop.nul.refresh, images.popmenu.nul.refresh,
                new SelectionAdapter(){
                    @Override
                    public void widgetSelected(SelectionEvent arg0){
                        reflushServiceTree();
                    }
                });
    }

    protected void initServerPopMenu(){
        serverPopMenuClosed = new Menu(shell);

        createItem(serverPopMenuClosed,
                i18n.pop.server.connect, images.popmenu.server.connect,
                new SelectionAdapter(){
                    @Override
                    public void widgetSelected(SelectionEvent e){
                        treeItemSelected(true);
                    }
                });

        new MenuItem(serverPopMenuClosed, SWT.SEPARATOR);

        createItem(serverPopMenuClosed,
                i18n.pop.server.modify, images.popmenu.server.modify,
                new SelectionAdapter(){
                    @Override
                    public void widgetSelected(SelectionEvent e){
                        modifyServer();
                    }
                });

        createItem(serverPopMenuClosed,
                i18n.pop.server.del, images.popmenu.server.remove,
                new SelectionAdapter(){
                    @Override
                    public void widgetSelected(SelectionEvent arg0){
                        removeServer();
                    }
                });

        serverPopMenuOpen = new Menu(shell);
        createItem(serverPopMenuOpen,
                i18n.pop.server.query, images.popmenu.server.query,
                new SelectionAdapter(){
                    @Override
                    public void widgetSelected(SelectionEvent e){
                        newQueryTab();
                    }
                });

        new MenuItem(serverPopMenuOpen, SWT.SEPARATOR);

        MenuItem funs = new MenuItem(serverPopMenuOpen, SWT.NONE);
        funs.setText(i18n.pop.server.funs);
        funs.setImage(images.popmenu.server.functions);
        funs.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                TreeItem item = tree.getSelection()[0];
                tabFolder.setSelection(new ShowFunctionsTab(tabFolder, item));
            }
        });
        MenuItem attr = new MenuItem(serverPopMenuOpen, SWT.NONE);
        attr.setText(i18n.pop.server.attr);
        attr.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                
            }
        });

        new MenuItem(serverPopMenuOpen, SWT.SEPARATOR);

        MenuItem reflush = new MenuItem(serverPopMenuOpen, SWT.NONE);
        reflush.setText(i18n.pop.server.reflush);
        reflush.setImage(images.popmenu.server.refresh);
        reflush.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                TreeItem selTree = tree.getSelection()[0];
                selTree.setData(NodeType.NODE_ITEM_OPENED, false);
                toggleServer(selTree, true);
            }
        });

        MenuItem close = new MenuItem(serverPopMenuOpen, SWT.NONE);
        close.setText(i18n.pop.server.close);
        close.setImage(images.popmenu.server.disconnect);
        close.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                TreeItem selTree = tree.getSelection()[0];
                toggleServer(selTree, false);
            }
        });

    }

    public void initDatabasesPopMenu(){
        databasesPopMenu = new Menu(shell, SWT.NONE);

        MenuItem query = new MenuItem(databasesPopMenu, SWT.NONE);
        query.setText(i18n.pop.database.query);
        query.setImage(images.popmenu.database.query);
        query.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                newQueryTab();
            }
        });
        
        MenuItem newTab = new MenuItem(databasesPopMenu, SWT.NONE);
        newTab.setText(i18n.pop.database.newTab);
        newTab.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
               /* CREATE [TEMPORARY] [EXTERNAL] TABLE [IF NOT EXISTS] [db_name.]table_name   -- (Note: TEMPORARY available in Hive 0.14.0 and later)
                [(col_name data_type [COMMENT col_comment], ...)]
                [COMMENT table_comment]
                [PARTITIONED BY (col_name data_type [COMMENT col_comment], ...)]
                [CLUSTERED BY (col_name, col_name, ...) [SORTED BY (col_name [ASC|DESC], ...)] INTO num_buckets BUCKETS]
                [SKEWED BY (col_name, col_name, ...) ON ([(col_value, col_value, ...), ...|col_value, col_value, ...])
                                                    [STORED AS DIRECTORIES]    -- (Note: Only available in Hive 0.10.0 and later)]
                [
                 [ROW FORMAT row_format] [STORED AS file_format]
                 | STORED BY 'storage.handler.class.name' [WITH SERDEPROPERTIES (...)]   -- (Note: Only available in Hive 0.6.0 and later)
                ]
                [LOCATION hdfs_path]
                [TBLPROPERTIES (property_name=property_value, ...)]  -- (Note: Only available in Hive 0.6.0 and later)
                [AS select_statement];  -- (Note: Only available in Hive 0.5.0 and later, and not supported when creating external tables.)
               
              CREATE [TEMPORARY] [EXTERNAL] TABLE [IF NOT EXISTS] [db_name.]table_name
                LIKE existing_table_or_view_name
                [LOCATION hdfs_path];
               
              data_type
                : primitive_type
                | array_type
                | map_type
                | struct_type
                | union_type  -- (Note: Only available in Hive 0.7.0 and later)
               
              primitive_type
                : TINYINT
                | SMALLINT
                | INT
                | BIGINT
                | BOOLEAN
                | FLOAT
                | DOUBLE
                | STRING
                | BINARY      -- (Note: Only available in Hive 0.8.0 and later)
                | TIMESTAMP   -- (Note: Only available in Hive 0.8.0 and later)
                | DECIMAL     -- (Note: Only available in Hive 0.11.0 and later)
                | DECIMAL(precision, scale)  -- (Note: Only available in Hive 0.13.0 and later)
                | VARCHAR     -- (Note: Only available in Hive 0.12.0 and later)
                | CHAR        -- (Note: Only available in Hive 0.13.0 and later)
               
              array_type
                : ARRAY < data_type >
               
              map_type
                : MAP < primitive_type, data_type >
               
              struct_type
                : STRUCT < col_name : data_type [COMMENT col_comment], ...>
               
              union_type
                 : UNIONTYPE < data_type, data_type, ... >  -- (Note: Only available in Hive 0.7.0 and later)
               
              row_format
                : DELIMITED [FIELDS TERMINATED BY char [ESCAPED BY char]] [COLLECTION ITEMS TERMINATED BY char]
                      [MAP KEYS TERMINATED BY char] [LINES TERMINATED BY char]
                      [NULL DEFINED AS char]   -- (Note: Only available in Hive 0.13 and later)
                | SERDE serde_name [WITH SERDEPROPERTIES (property_name=property_value, property_name=property_value, ...)]
               
              file_format:
                : SEQUENCEFILE
                | TEXTFILE
                | RCFILE      -- (Note: Only available in Hive 0.6.0 and later)
                | ORC         -- (Note: Only available in Hive 0.11.0 and later)
                | AVRO        -- (Note: Only available in Hive 0.14.0 and later)
                | INPUTFORMAT input_format_classname OUTPUTFORMAT output_format_classname*/
            }
        });
        
        MenuItem attr = new MenuItem(databasesPopMenu, SWT.NONE);
        attr.setText(i18n.pop.database.attr);
        attr.setImage(images.popmenu.database.attr);
        attr.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                TreeItem item = tree.getSelection()[0];
                tabFolder.setSelection(new DatabaseAttrTab(tabFolder, item));
            }
        });
        
        

        new MenuItem(databasesPopMenu, SWT.SEPARATOR);

        createItem(databasesPopMenu,
                i18n.pop.database.refresh, images.popmenu.database.refresh
                ,new SelectionAdapter(){
                    @Override
                    public void widgetSelected(SelectionEvent e){
                        TreeItem treeItem = tree.getSelection()[0];
                        treeItem.setData(NodeType.NODE_ITEM_OPENED,false);
                        databaseTreeItemSelected(treeItem, true);
                    }
                });
    }

    public void initTablePopMenu(){
        tablePopMenu = new Menu(shell, SWT.NONE);

        MenuItem query = new MenuItem(tablePopMenu, SWT.NONE);
        query.setText(i18n.pop.table.query);
        query.setImage(images.popmenu.table.query);
        query.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                newQueryTab();
            }
        });

        new MenuItem(tablePopMenu, SWT.SEPARATOR);

        MenuItem rename = new MenuItem(tablePopMenu, SWT.NONE);
        rename.setText(i18n.pop.table.rename);
        rename.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){

            }
        });
        MenuItem modify = new MenuItem(tablePopMenu, SWT.NONE);
        modify.setText(i18n.pop.table.modify);
        modify.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){

            }
        });
        MenuItem delete = new MenuItem(tablePopMenu, SWT.NONE);
        delete.setText(i18n.pop.table.del);
        delete.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){

            }
        });

        MenuItem attr = new MenuItem(tablePopMenu, SWT.NONE);
        attr.setText(i18n.pop.table.attr);
        attr.setImage(images.popmenu.table.attr);
        attr.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                tableAttributeTab();
            }
        });

        new MenuItem(tablePopMenu, SWT.SEPARATOR);

        MenuItem refresh = new MenuItem(tablePopMenu, SWT.NONE);
        refresh.setText(i18n.pop.table.refresh);
        refresh.setImage(images.popmenu.table.refresh);
        refresh.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                TreeItem treeItem = tree.getSelection()[0];
                treeItemTableShow(treeItem);
            }
        });
    }

    protected void tableAttributeTab(){
        TreeItem treeItem = tree.getSelection()[0];
        tabFolder.setSelection(new TableAttrTab(tabFolder, treeItem));
    }

    /**
     * 创建查询Tab
     */
    protected void newQueryTab(){
        TreeItem item = tree.getSelection()[0];
        tabFolder.setSelection(new QueryTab(tabFolder, item));
    }

    private void initMenuHelp(Menu menu){
        // help
        Menu menuHtlp = new Menu(menu);
        MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
        mntmHelp.setText(i18n.menu.help.txt);
        mntmHelp.setMenu(menuHtlp);

        MenuItem mntmAbout = new MenuItem(menuHtlp, SWT.NONE);
        mntmAbout.addSelectionListener(new SelectionAdapter(){

            @Override
            public void widgetSelected(SelectionEvent arg0){
                AboutDialog dialog = new AboutDialog(shell, images.appIcon);
                dialog.open();
            }
        });
        mntmAbout.setText(i18n.menu.help.about);
    }

    protected void initMenuServer(Menu menu){
        // server
        menuServer = new Menu(menu);
        menuServer.addMenuListener(new MenuListener(){

            @Override
            public void menuShown(MenuEvent e){
                TreeItem[] selectedItems = tree.getSelection();
                if(selectedItems != null && selectedItems.length > 0){
                    NodeType type = (NodeType) selectedItems[0].getData(NodeType.NODE_TYPE);
                    //选中server节点,并且关闭着才可以编辑
                    if(type == NodeType.SERVER){
                        if(Boolean.TRUE.equals(selectedItems[0].getData(NodeType.NODE_ITEM_OPENED))){
                            menuServer.getItem(1).setEnabled(false);// 修改
                            menuServer.getItem(2).setEnabled(false);// 删除
                            menuServer.getItem(3).setEnabled(false);// 删除
                        }
                        else{
                            menuServer.getItem(1).setEnabled(true);// 修改
                            menuServer.getItem(2).setEnabled(true);// 删除
                            menuServer.getItem(3).setEnabled(true);// 删除
                        }
                    }
                    else{
                        menuServer.getItem(1).setEnabled(false);// 修改
                        menuServer.getItem(2).setEnabled(false);// 删除
                        menuServer.getItem(3).setEnabled(true);// 删除
                    }
                }
            }

            @Override
            public void menuHidden(MenuEvent e){}
        });

        MenuItem miServer = new MenuItem(menu, SWT.CASCADE);
        miServer.setText(i18n.menu.server.txt);
        miServer.setMenu(menuServer);

        createItem(menuServer,
                i18n.menu.server.add, images.menu.server.add,
                new SelectionAdapter(){
                    @Override
                    public void widgetSelected(SelectionEvent arg0){
                        addServer();
                    }
                });

        MenuItem mntmEdit = new MenuItem(menuServer, SWT.NONE);
        mntmEdit.setText(i18n.menu.server.modify);
        mntmEdit.setImage(images.menu.server.modify);
        mntmEdit.setEnabled(false);
        mntmEdit.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                modifyServer();
            }
        });

        MenuItem mntmDelete = new MenuItem(menuServer, SWT.NONE);
        mntmDelete.setText(i18n.menu.server.del);
        mntmDelete.setImage(images.menu.server.del);
        mntmDelete.setEnabled(false);
        mntmDelete.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent arg0){
                removeServer();
            }
        });

        MenuItem mntmProperties = new MenuItem(menuServer, SWT.NONE);
        mntmProperties.setText(i18n.menu.server.attr);
        mntmProperties.setEnabled(false);
        mntmProperties.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                //serverProperties();
            }
        });

        new MenuItem(menuServer, SWT.SEPARATOR);

        MenuItem mntmExit = new MenuItem(menuServer, SWT.NONE);
        mntmExit.setImage(images.menu.server.exit);
        mntmExit.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent arg0){
                shell.close();
            }
        });
        mntmExit.setText(i18n.menu.server.exit);
    }

    protected void initMenuTools(Menu menu){
        // tools
        Menu menuTools = new Menu(menu);
        MenuItem toolsMenuItem = new MenuItem(menu, SWT.CASCADE);
        toolsMenuItem.setText(i18n.menu.tools.txt);
        toolsMenuItem.setMenu(menuTools);

        Menu mentToolsExport = new Menu(menuTools);
        MenuItem mntmExport = new MenuItem(menuTools, SWT.CASCADE);
        mntmExport.setText(i18n.menu.tools.export.title);
        mntmExport.setMenu(mentToolsExport);

        MenuItem mntmExportExcel = new MenuItem(mentToolsExport, SWT.CASCADE);
        mntmExportExcel.setEnabled(false);
        mntmExportExcel.setText(i18n.menu.tools.export.excel);

        MenuItem mntmExportTxt = new MenuItem(mentToolsExport, SWT.CASCADE);
        mntmExportTxt.setEnabled(false);
        mntmExportTxt.setText(i18n.menu.tools.export.txt);
    }

    private void initSash(){
        SashForm sashForm = new SashForm(shell, SWT.NONE);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        initTree(sashForm);
        initTab(sashForm);
        sashForm.setWeights(new int[]{1, 4});
    }

    /**
     * 初始化TAB
     * 
     * @param sashForm
     */
    public void initTab(final SashForm sashForm){
        tabFolder = new CTabFolder(sashForm, SWT.BORDER);
        //双击全屏
        tabFolder.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseDoubleClick(MouseEvent e){
                if(sashForm.getMaximizedControl() != tabFolder){
                    sashForm.setMaximizedControl(tabFolder);
                }
                else{
                    sashForm.setMaximizedControl(null);
                }
            }
        });
        //选择某一个tab
        tabFolder.addSelectionListener(new SelectionAdapter(){

            @Override
            public void widgetSelected(SelectionEvent e){
                /*
                 * if (tabFolder.getSelection() == mainTabItem)
                 * treeItemSelected(true);
                 */
            }
        });

        tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
        tabFolder.setFocus();

        CTabItem mainTabItem = new CTabItem(tabFolder, SWT.BORDER);
        mainTabItem.setImage(images.tree.root);
        mainTabItem.setText("Hive服务浏览");
    }

    private void initTree(SashForm sashForm){
        tree = new Tree(sashForm, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        tree.addSelectionListener(new SelectionAdapter(){

            @Override
            public void widgetSelected(SelectionEvent e){
                treeItemSelected(false);
            }
        });

        tree.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseDown(MouseEvent mouseEvent){
                if(mouseEvent.count == 2){
                    treeItemSelected(true);
                }
                else if(mouseEvent.button == 3){ // RIGHT
                    Point point = new Point(mouseEvent.x, mouseEvent.y);
                    TreeItem selectedItem = tree.getItem(point);
                    treeItemRightSelected(selectedItem);
                }
            }
        });

        // ROOT
        rootServers = new TreeItem(tree, SWT.NONE);
        rootServers.setImage(images.tree.root);
        rootServers.setText(i18n.tree.server);
        rootServers.setData(NodeType.NODE_TYPE, NodeType.ROOT);
        rootServers.setData(NodeType.NODE_ITEM_OPENED, true);
        rootServers.setExpanded(true);
    }

    /**
     * 添加服务选择框
     */
    protected void addServer(){
        AddServerDialog dialog = new AddServerDialog(shell, images.appIcon);
        Server server = (Server) dialog.open();
        if(server != null){
            Config.addServer(server);
            ServerManager.add(server);
            TreeItem treeItem = addServerTreeItem(server);
            serverTreeItemSelected(treeItem, false);
        }
    }

    /**
     * 删除服务
     */
    public void removeServer(){
        String title = i18n.msgbox.delServerConfirm.title;
        String message = i18n.msgbox.delServerConfirm.message;
        boolean del = MessageDialog.openConfirm(shell, title, message);
        if(del){
            TreeItem sel = tree.getSelection()[0];
            String serverId = sel.getData().toString();
            Config.delServer(serverId);
            sel.dispose();
            //BUG 0001 ,删除后不能刷新树，不然已经打开的就关闭了
            //reflushServiceTree();
        }
    }

    public void modifyServer(){
        TreeItem item = tree.getSelection()[0];
        String serverId = item.getData().toString();

        AddServerDialog dialog = new AddServerDialog(shell, images.appIcon);
        dialog.setServer(Config.getServer(serverId));

        Server newServer = (Server) dialog.open();
        if(newServer != null){
            item.dispose();
            Config.delServer(serverId);
            Config.addServer(newServer);

            ServerManager.remove(serverId);
            ServerManager.add(newServer);

            TreeItem treeItem = addServerTreeItem(newServer);
            serverTreeItemSelected(treeItem, false);
        }
    }

    /**
     * 添加一个服务
     * 
     * @param server
     * @return
     */
    private TreeItem addServerTreeItem(Server server){
        TreeItem treeItem = new TreeItem(rootServers, 0);
        treeItem.setText(server.getName());
        treeItem.setData(server.getId());
        treeItem.setData(NodeType.NODE_TYPE, NodeType.SERVER);
        treeItem.setImage(images.tree.server);
        return treeItem;
    }

    /**
     * 选中某个服务
     * 
     * @param selectedItem
     * @param refresh
     */
    private void serverTreeItemSelected(final TreeItem selectedItem, boolean refresh){
        tree.setSelection(selectedItem);
        if(!refresh){ return; }
        toggleServer(selectedItem, true);
    }

    /**
     * 选中某个服务的数据库
     * 
     * @param treeItem
     *            选中的几点
     * @param refresh
     *            是否强制刷新
     */
    private void databaseTreeItemSelected(final TreeItem treeItem, boolean refresh){
        tree.setSelection(treeItem);
        
        //不需要刷新
        if(!refresh){ return; }

        if(!Boolean.TRUE.equals(treeItem.getData(NodeType.NODE_ITEM_OPENED))){
            
            if(Boolean.TRUE.equals(treeItem.getData(NodeType.NODE_LOADING))){ return; }
            
            treeItem.setImage(images.tree.loading);
            treeItem.setData(NodeType.NODE_LOADING, true);

            String database = treeItem.getText();
            ShowTablesCallBack back = new ShowTablesCallBack(database){
                @Override
                public void onData(List<String> tables){
                    this.resetTree();
                    treeItem.removeAll();
                    treeItem.setData(NodeType.NODE_ITEM_OPENED, true);
                    for(String table : tables){
                        appendTableTree(treeItem, table);
                    }
                    //展开
                    treeItem.setExpanded(true);
                }

                @Override
                public void onException(Exception e){
                    this.resetTree();
                }

                public void resetTree(){
                    treeItem.setImage(images.tree.database);
                    treeItem.setData(NodeType.NODE_LOADING, false);
                }

                /**
                 * 添加一个table到服务上
                 * 
                 * @param databaseTreeItem
                 * @param table
                 * @param fields
                 */
                public void appendTableTree(TreeItem databaseTreeItem,String tableName){
                    TreeItem table = new TreeItem(databaseTreeItem, SWT.NONE);
                    table.setText(tableName);
                    table.setData(NodeType.NODE_TYPE, NodeType.TABLE);
                    table.setImage(images.tree.table);
                    treeItemTableShow(table);
                }
            };
            String serverId = treeItem.getParentItem().getData().toString();
            ServerManager.get(serverId).showTables(back);
        }
    }
    
    private void treeItemTableShow(final TreeItem tableItem){
        String tableName = tableItem.getText();
        String database = tableItem.getParentItem().getText();
        String serverId = tableItem.getParentItem().getParentItem().getData().toString();
        
        tableItem.setImage(images.tree.loading);
        
        DescribeCallBack describeCallBack = new DescribeCallBack(database, tableName){
            public void onData(List<Map<String,String>> fields) {
                tableItem.removeAll();
                tableItem.setImage(images.tree.table);
                boolean startParttionField = false;
                for(Map<String,String> fieldAttr : fields){
                    String name = fieldAttr.get("name");
                    if("".equals(name)){
                        //不处理
                    }else if( name.indexOf("#") != -1 ){//分区开始标示
                        startParttionField = true;
                    }else{
                        TreeItem field = new TreeItem(tableItem,SWT.NONE);
                        field.setText(name);
                        if(startParttionField){
                            field.setImage(images.tree.partition);
                            field.setData(NodeType.NODE_TYPE, NodeType.FIELD_PARTITION);
                        }else{
                            field.setImage(images.tree.field);
                            field.setData(NodeType.NODE_TYPE, NodeType.FIELD);
                        }
                    }
                }
            }
            @Override
            public void onException(Exception e){
                tableItem.setImage(images.tree.table);
            }
        };
        ServerManager.get(serverId).describe(describeCallBack);
    }

    private void treeItemSelected(boolean refresh){
        TreeItem[] items = tree.getSelection();
        NodeType type = (NodeType) items[0].getData(NodeType.NODE_TYPE);

        switch(type){
            case ROOT:{
                //reflushServiceTree();
            }
            break;
            case SERVER:{
                serverTreeItemSelected(items[0], refresh);
            }
            break;
            case DATABASE:{
                databaseTreeItemSelected(items[0], refresh);
            }
            break;
            case TABLE : {
                if(refresh){
                    tableAttributeTab();
                }
            }break;
            default:
            break;
        }
    }

    private void treeItemRightSelected(TreeItem selectedItem){
        tree.setMenu(null);
        treeItemSelected(false);
        if(selectedItem == rootServers || selectedItem == null){
            tree.setMenu(nullPopMenu);
        }
        else{
            NodeType type = (NodeType) selectedItem.getData(NodeType.NODE_TYPE);
            switch(type){
                case ROOT:
                    tree.setMenu(nullPopMenu);
                break;
                case SERVER:
                    if(Boolean.TRUE.equals(selectedItem.getData(NodeType.NODE_ITEM_OPENED))){
                        tree.setMenu(serverPopMenuOpen);
                    }
                    else{
                        tree.setMenu(serverPopMenuClosed);
                    }
                break;
                case DATABASE:
                    tree.setMenu(databasesPopMenu);
                break;
                case TABLE:
                    tree.setMenu(tablePopMenu);
                break;
                default:
            }
        }
    }

    /**
     * 服务开/关
     * 
     * @param serverItem
     * @param open
     */
    private void toggleServer(final TreeItem serverItem, boolean open){
        if(open){ //打开服务
            if(!Boolean.TRUE.equals(serverItem.getData(NodeType.NODE_ITEM_OPENED))){

                //如果为正在加载，直接返回不能重复加载不然就会出错
                if(Boolean.TRUE.equals(serverItem.getData(NodeType.NODE_LOADING))){ return; }
                //设置为正在加载
                serverItem.setImage(images.tree.loading);
                serverItem.setData(NodeType.NODE_LOADING, true);

                ShowDatabasesCallBack adapter = new ShowDatabasesCallBack(){
                    public void onData(List<String> dbs){
                        serverItem.removeAll();
                        for(String dbName : dbs){
                            TreeItem dbItem = new TreeItem(serverItem, SWT.NONE);
                            dbItem.setText(dbName);
                            dbItem.setData(NodeType.NODE_TYPE, NodeType.DATABASE);
                            dbItem.setImage(images.tree.database);
                        }
                        serverItem.setExpanded(true);
                        serverItem.setData(NodeType.NODE_ITEM_OPENED, true);
                        serverItem.setData(NodeType.NODE_LOADING, false);
                        serverItem.setImage(images.tree.server_connected);
                    }

                    @Override
                    public void onException(Exception e){
                        String msg = ExceptionUtil.toString(e);
                        MessageDialog.openError(shell,
                                i18n.msgbox.openServerError.title,
                                i18n.msgbox.openServerError.message + "\n" + msg);
                    }
                };
                String serverId = serverItem.getData().toString();
                ServerManager.get(serverId).showDatabases(adapter);
            }
        }
        else{
            if(Boolean.TRUE.equals(serverItem.getData(NodeType.NODE_ITEM_OPENED))){
                serverItem.removeAll();
            }
            serverItem.setData(NodeType.NODE_ITEM_OPENED, false);
            serverItem.setImage(images.tree.server);

            //关闭所有该服务下的tab
            String serverId = serverItem.getData().toString();
            CTabItem[] cTabItems = tabFolder.getItems();
            for(CTabItem cTabItem : cTabItems){
                if(cTabItem instanceof Tab){
                    Tab tab = (Tab) cTabItem;
                    if(tab.getServerId().equals(serverId)){
                        tab.close();
                    }
                }
            }
        }
    }

    public boolean exitApplication(){
        if(ServerManager.distory(false)){
            boolean exit = MessageDialog.openConfirm(shell, i18n.msgbox.exit.title, i18n.msgbox.exit.message);
            if(exit){
                ServerManager.distory(true);
            }
            else{
                return false;
            }
        }
        return true;
    }

    protected MenuItem createItem(Menu menu, String label, Image icon, SelectionListener selectionListener){
        MenuItem menuitem = new MenuItem(menu, SWT.CASCADE);
        menuitem.setText(label);
        if(icon != images.nil){
            menuitem.setImage(icon);
        }
        if(selectionListener != null){
            menuitem.addSelectionListener(selectionListener);
        }
        return menuitem;
    }
}
