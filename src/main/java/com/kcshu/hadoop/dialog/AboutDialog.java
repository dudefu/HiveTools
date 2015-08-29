package com.kcshu.hadoop.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kcshu.hadoop.utils.i18n;

public class AboutDialog extends AbstractDialog {

	/**
	 * Create the dialog.
	 * @param parent
	 * @param image
	 */
	public AboutDialog(Shell parent, Image image) {
		super(parent, image);
	}

	/**
	 * Create contents of the dialog.
	 */
	protected void createContents() {
		shell.setText(i18n.dialog.about.title);
		shell.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		final int width = image.getBounds().width;
	    final int height = image.getBounds().height;
		final Image scaled050 = new Image(shell.getDisplay(),
		        image.getImageData().scaledTo((int)(width*0.5),(int)(height*0.5)));
		
		Label btnNewButton_1 = new Label(composite, SWT.FLAT);
		btnNewButton_1.setBounds(0, 0, 80, 27);
		btnNewButton_1.setImage(scaled050);
		
		SelectionAdapter openUrl = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Program.launch(arg0.text);
			}
		};
		
		Link lblNewLabel = new Link(composite, SWT.NONE);
		lblNewLabel.addSelectionListener(openUrl);
		lblNewLabel.setFont(SWTResourceManager.getFont("Arial", 20, SWT.NORMAL));
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lblNewLabel.setBounds(0, 0, 61, 17);
		lblNewLabel.setText(i18n.dialog.about.headTitle);
		
		Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		//
		new Label(composite, SWT.NONE);
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setText(i18n.dialog.about.version);
		
		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		lblNewLabel_2.setText(i18n.dialog.about.versionNo);
		//
		new Label(composite, SWT.NONE);
		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
		lblNewLabel_3.setText(i18n.dialog.about.developer);
		Link link = new Link(composite, SWT.NONE);
		link.addSelectionListener(openUrl);
		link.setText(i18n.dialog.about.email);
		//-
		new Label(composite, SWT.NONE);
		Label lblReportIssue = new Label(composite, SWT.NONE);
		lblReportIssue.setText(i18n.dialog.about.issue);
		
		Link link_1 = new Link(composite, SWT.NONE);
		link_1.addSelectionListener(openUrl);
		link_1.setText(i18n.dialog.about.issueClick);
		
		//-
		new Label(composite, SWT.NONE);
		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
		Button btnNewButton = new Button(composite_1, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.dispose();
			}
		});
		btnNewButton.setText(i18n.ok);

		super.createContents();
	}
	@Override
	public int getStyle() {
		return SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;
	}
}
