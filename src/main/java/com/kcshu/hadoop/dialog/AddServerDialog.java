package com.kcshu.hadoop.dialog;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.kcshu.hadoop.domain.Server;
import com.kcshu.hadoop.service.ProxyServer;
import com.kcshu.hadoop.service.ServerManager;
import com.kcshu.hadoop.utils.i18n;

/**
 * 添加服务Dialog
 * @author zhouhaichao(a)2008.sina.com
 */
public class AddServerDialog extends AbstractDialog {
	
	protected Text wName;
	protected Text wHost,wPort,wDbName;
	protected Text wUsername,wPasswd;
	protected Server server;//如果是编辑的话就需要设置次三处

	protected Button btnOk;//确定
	protected Button btnTest;//测试连接
	protected Button btnCancel;//取消
	
	protected Button labelSSHTrunal;
	    Text wSshHost,wSshPort,wSshUserName;
	    Text wSshPassword;
	    Button sshSecturyKey;
	    Combo sshPasswordType;
	protected Label[] labels;
	protected Label keyLabel;
	protected File sshKeyFile;
	
    /**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public AddServerDialog(Shell parent, Image image) {
		super(parent, image);
	}
	
	public void setServer(Server server){
        this.server = server;
    }

	/**
	 * Create contents of the dialog.
	 */
	protected void createContents() {
		shell.setText(i18n.dialog.addserver.title);
		shell.setLayout(new GridLayout(1, true));
		
		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true,true, 1, 1));
		
		//基本信息
		TabItem baseTab = new TabItem(tabFolder, SWT.BORDER);
        baseTab.setText("基本信息");
		Composite baseComposite = new Composite(tabFolder,SWT.FILL);
		baseComposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true,true, 1, 1));
		baseComposite.setLayout(new GridLayout(2,false));
		baseTab.setControl(baseComposite);
		
		//名称
		new Label(baseComposite, SWT.NONE).setText(i18n.dialog.addserver.name);
		wName = new Text(baseComposite, SWT.BORDER);
		wName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		wName.setTextLimit(30);
		wName.setFocus();
		
		new Label(baseComposite, SWT.NONE).setText(i18n.dialog.addserver.host);
		wHost = new Text(baseComposite, SWT.BORDER);
		wHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		wHost.setTextLimit(15);
		
		new Label(baseComposite, SWT.NONE).setText(i18n.dialog.addserver.port);
		wPort = new Text(baseComposite, SWT.BORDER );
		wPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		wPort.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if( e.keyCode >= 48 && e.keyCode <= 57 ) {
                    char newChar = (char)e.keyCode;
                    String now = wPort.getText() + newChar;
                    if( Integer.parseInt(now) > 65535) {
                        e.doit = false;
                    }
                }else if( e.keyCode == SWT.DEL || e.keyCode == 8 /**删除*/ 
                        || e.keyCode == 16777219 || e.keyCode == 16777220 /*left and right*/) {
                    
                } else {
                    e.doit = false;
                }
            }
        });
		wPort.setText("10000");
		
		new Label(baseComposite, SWT.NONE).setText(i18n.dialog.addserver.dbname);
		wDbName = new Text(baseComposite, SWT.BORDER);
		wDbName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		//用户名
		new Label(baseComposite, SWT.NONE).setText(i18n.dialog.addserver.username);
		wUsername = new Text(baseComposite, SWT.BORDER);
		wUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		wUsername.selectAll();
		
		//密码
		new Label(baseComposite, SWT.NONE).setText(i18n.dialog.addserver.passwd);
		wPasswd = new Text(baseComposite, SWT.BORDER | SWT.PASSWORD);
		wPasswd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		wPasswd.selectAll();
		
		//添加一条线
        new Label(baseComposite, SWT.SEPARATOR | SWT.HORIZONTAL)
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        
        
        // ############ SSH 代理 ##################
        TabItem sshTab = new TabItem(tabFolder, SWT.BORDER);
        sshTab.setText("SSH代理");
        Composite sshComposite = new Composite(tabFolder,SWT.FILL);
        sshComposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true,true, 1, 1));
        sshComposite.setLayout(new GridLayout(2,false));
        sshTab.setControl(sshComposite);
        
		labelSSHTrunal = new Button(sshComposite,SWT.CHECK);
		labelSSHTrunal.setText("使用SSH代理");
		labelSSHTrunal.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,2, 1));
		labelSSHTrunal.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e){
                boolean enabled =  labelSSHTrunal.getSelection();
                useSSHProxy(enabled);
            }
        });
		
		labels = new Label[5];
		
		//主机或者地址
		labels[0] =  label(sshComposite,"SSH地址：");
        wSshHost = new Text(sshComposite, SWT.BORDER );
        wSshHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        //SSH端口号
        labels[1] =  label(sshComposite,"SSH端口号：");
        wSshPort = new Text(sshComposite, SWT.BORDER);
        wSshPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        wSshPort.setText("22");
        wSshPort.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if( e.keyCode >= 48 && e.keyCode <= 57 ) {
                    char newChar = (char)e.keyCode;
                    String now = wSshPort.getText() + newChar;
                    if( Integer.parseInt(now) > 65535) {
                        e.doit = false;
                    }
                }else if( e.keyCode == SWT.DEL || e.keyCode == 8 /**删除*/ 
                        || e.keyCode == 16777219 || e.keyCode == 16777220 /*left and right*/) {
                    
                } else {
                    e.doit = false;
                }
            }
        });
        //用户名
        labels[2] =  label(sshComposite,"用户名：");
        wSshUserName = new Text(sshComposite, SWT.BORDER );
        wSshUserName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        labels[3] = label(sshComposite,"验证方式：");
        sshPasswordType = new Combo(sshComposite,SWT.DROP_DOWN | SWT.READ_ONLY);
        sshPasswordType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        sshPasswordType.add("密码");
        sshPasswordType.add("私钥");
        sshPasswordType.setText("密码");
        sshPasswordType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                boolean enabled = (sshPasswordType.getText().equals("私钥"));
                usePubKeyFile(enabled);
            }
        });
        
        keyLabel  = label(sshComposite,"私钥文件：");
        sshSecturyKey = new Button(sshComposite,SWT.NONE);
        sshSecturyKey.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));
        sshSecturyKey.setText("选择文件");
        sshSecturyKey.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e){
                FileDialog dialog = new FileDialog(shell);
                dialog.setFilterExtensions(new String[]{"id_rsa","*.*"});
                String file = dialog.open();
                if(file != null) {
                    sshKeyFile = new File(file);
                    sshSecturyKey.setText(sshKeyFile.getName());
                }
            }
        });
        sshSecturyKey.setEnabled(false);
        
        
        //密码
        labels[4] =  label(sshComposite,"密码：    ");
        wSshPassword = new Text(sshComposite, SWT.BORDER | SWT.PASSWORD);
        wSshPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        useSSHProxy(false);
        
        //＃＃　控制扭　＃＃
		Composite ctl = new Composite(baseComposite, SWT.NONE);
		ctl.setLayout(new FillLayout(SWT.HORIZONTAL));
		ctl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,2, 1));
		
		btnOk = new Button(ctl, SWT.NONE );
		btnOk.setText(i18n.dialog.addserver.ctl.ok);
		btnOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if(test()){
					result = get();
					shell.dispose();
				}
			}
		});
		
		btnTest  = new Button(ctl, SWT.NONE | SWT.CENTER);
		btnTest.setText(i18n.dialog.addserver.ctl.test);
		btnTest.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if( test() ){
					MessageDialog.openInformation(shell,
							i18n.msgbox.testServerError.title,i18n.msgbox.testServerError.message);	
				}
			}
		});
		
		btnCancel  = new Button(ctl, SWT.NONE | SWT.CENTER);
		btnCancel.setText(i18n.dialog.addserver.ctl.cannel);
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				shell.dispose();
			}
		});
		
		
		this.ifUpdate();
		super.createContents();
	}
	
	public Label label(Composite c , String text) {
	    Label label = new Label(c,SWT.NONE);
	    label.setText(text);
	    label.setEnabled(false);
	    return label;
	} 
	
	public void useSSHProxy(boolean enabled) {
	    wSshHost.setEnabled(enabled);
        wSshPort.setEnabled(enabled);
        wSshUserName.setEnabled(enabled);
        wSshPassword.setEnabled(enabled);
        sshPasswordType.setEnabled(enabled);
        for (int i = 0; i < labels.length; i++)
        {
            labels[i].setEnabled(enabled);
        }
	}
	public void usePubKeyFile(boolean enabled) {
	    keyLabel.setEnabled(enabled);
        sshSecturyKey.setEnabled(enabled);
        labels[4].setText(enabled?"密码短语：" : "密码：");
	}
	
	/**
	 * 如果是更新才做的话
	 */
	public void ifUpdate(){
	    if(server != null){
	       wName.setText(server.getName());
	       wHost.setText(server.getHost());
	       wPort.setText(String.valueOf(server.getPort()));
	       wDbName.setText(server.getDb());
	       wUsername.setText(server.getUser());
	       wPasswd.setText(server.getPassword());
	       
	       shell.setText(i18n.dialog.modifyServer.title);
	       btnOk.setText(i18n.dialog.modifyServer.ctl.ok);
	       btnTest.setText(i18n.dialog.modifyServer.ctl.test);
	       btnCancel.setText(i18n.dialog.modifyServer.ctl.cannel);

           if(server.isUseSshProxy()) {
               labelSSHTrunal.setSelection(true);
               useSSHProxy(true);
               
               wSshHost.setText(server.getSshHost());
               wSshPort.setText(String.valueOf(server.getSshPort()));
               wSshUserName.setText(server.getSshUserName());
               wSshPassword.setText(server.getSshPassword());
               if( server.isPushKey() ) {
                   sshPasswordType.setText("私钥");
                   sshKeyFile = new File(server.getSshKeyFile());
                   sshSecturyKey.setText(sshKeyFile.getName());
                   usePubKeyFile(true);
               }
           }
	    }
	}
	
	public boolean test(){
		Server server = get();
		//@BUG 如果这里不写的话，就出出现，
		//错误原因是无法更新ServerManager中的内容，导致无法使用代理
		//所以此处直接给出一个TEST
		server.setName(server.getName()+"_fortest");
		
		if( !ProxyServer.init().add(server) ) {
		    MessageDialog.openError(shell,"错误","代理设置错误");
		    return false;
		}
		boolean succ = ServerManager.add(server).test();
		ProxyServer.init().close(server);//关闭代理，一定要关闭代理
		if(!succ) {
		    MessageDialog.openError(shell,"错误","远程地址/用户名/密码错误");
		}
		return succ;
	}
	
	public Server get() {
	    
	    String name = wName.getText();
        
        //hive信息
        String host = wHost.getText();
        int port = Integer.parseInt(wPort.getText());
        String dbName = wDbName.getText();
        String username = wUsername.getText();
        String password = wPasswd.getText();
        
        Server server = new Server(name,host,port,dbName,username,password);
        
        if(labelSSHTrunal.getSelection()) { //ssh信息
            String sshHost = wSshHost.getText();
            int sshPort = Integer.parseInt(wSshPort.getText().trim());
            String sshUserName = wSshUserName.getText();
            String sshPassword = wSshPassword.getText();
            server.setSshHost(sshHost);
            server.setSshPort(sshPort);
            server.setSshUserName(sshUserName);
            server.setSshPassword(sshPassword);
            if( sshPasswordType.getText().equals("私钥") ) {
                server.setSshKeyFile(sshKeyFile.getPath());
            }
        }
        return server;
	}
	
	@Override
	public int getStyle() {
		return SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;
	}
}
