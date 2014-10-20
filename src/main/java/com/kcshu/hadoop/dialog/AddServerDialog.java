package com.kcshu.hadoop.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.kcshu.hadoop.domain.Server;
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
		
		Composite composite = new Composite(shell,SWT.BORDER|SWT.FILL);
		composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true,true, 1, 1));
		GridLayout layout = new GridLayout(2,false);
		composite.setLayout(layout);
		
		//名称
		new Label(composite, SWT.NONE).setText(i18n.dialog.addserver.name);
		wName = new Text(composite, SWT.BORDER);
		wName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		wName.setTextLimit(30);
		wName.setFocus();
		
		//host:prot/dbname
		/*Composite addressComposite = new Composite(composite, SWT.NONE);
		addressComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		addressComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));*/
		
		new Label(composite, SWT.NONE).setText(i18n.dialog.addserver.host);
		wHost = new Text(composite, SWT.BORDER);
		wHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		wHost.setTextLimit(15);
		new Label(composite, SWT.NONE).setText(i18n.dialog.addserver.port);
		wPort = new Text(composite, SWT.BORDER);
		wPort.setText("10000");
		new Label(composite, SWT.NONE).setText(i18n.dialog.addserver.dbname);
		wDbName = new Text(composite, SWT.BORDER);
		wDbName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		//用户名
		new Label(composite, SWT.NONE).setText(i18n.dialog.addserver.username);
		wUsername = new Text(composite, SWT.BORDER);
		wUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		wUsername.selectAll();
		//密码
		new Label(composite, SWT.NONE).setText(i18n.dialog.addserver.passwd);
		wPasswd = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		wPasswd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		wPasswd.selectAll();
		
		Composite ctl = new Composite(composite, SWT.NONE);
		ctl.setLayout(new FillLayout(SWT.HORIZONTAL));
		ctl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,2, 1));
		
		btnOk = new Button(ctl, SWT.NONE );
		btnOk.setText(i18n.dialog.addserver.ctl.ok);
		btnOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if(test()){
					String name = wName.getText();
					
					String host = wHost.getText();
					String port = wPort.getText();
					String dbName = wDbName.getText();
					
					String username = wUsername.getText();
					String password = wPasswd.getText();
					result = new Server(name, host, port, dbName, username, password);
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
	/**
	 * 如果是更新才做的话
	 */
	public void ifUpdate(){
	    if(server != null){
	       wName.setText(server.getName());
	       wHost.setText(server.getHost());
	       wPort.setText(server.getPort());
	       wDbName.setText(server.getDb());
	       wUsername.setText(server.getUser());
	       wPasswd.setText(server.getPassword());
	       
	       shell.setText(i18n.dialog.modifyServer.title);
	       
	       btnOk.setText(i18n.dialog.modifyServer.ctl.ok);
	       btnTest.setText(i18n.dialog.modifyServer.ctl.test);
	       btnCancel.setText(i18n.dialog.modifyServer.ctl.cannel);
	    }
	}
	
	public boolean test(){
		String name = wName.getText();
		
		String host = wHost.getText();
		String port = wPort.getText();
		//String dbName = wDbName.getText();
		
		//String username = wUsername.getText();
		//String password = wPasswd.getText();
		
		if( name.length() == 0 
				|| host.length() == 0 || port.length() == 0 ){
			MessageDialog.openError(shell,i18n.msgbox.addServerError.title,i18n.msgbox.addServerError.message);
			return false;
		}
		return true;
	}
	
	@Override
	public int getStyle() {
		return SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;
	}
}
