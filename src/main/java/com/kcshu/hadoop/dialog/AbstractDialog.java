package com.kcshu.hadoop.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	protected Image image;
	protected int minWidth = 350;
	protected int minHeight = 150;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param image
	 */
	public AbstractDialog(Shell parent, Image image) {
		super(parent, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		this.image = image;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		shell = new Shell(getParent(), getStyle());
		shell.setImage(image);

		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	protected void createContents() {
		shell.setMinimumSize(minWidth, minHeight);
		shell.pack();
		Rectangle screenSize = shell.getParent().getBounds();
		Rectangle shellSize = shell.getBounds();
		shell.setLocation(screenSize.x + screenSize.width / 2 - shellSize.width / 2, screenSize.y + screenSize.height / 2 - shellSize.height / 2);
	}

}
