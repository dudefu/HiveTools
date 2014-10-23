package com.kcshu.hadoop.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月16日 上午11:17:24
 */
public class ExceptionUtil{
    public static final String toString(Exception e){
        StringWriter w = new StringWriter();
        e.printStackTrace(new PrintWriter(w));
        return w.toString();
    }
    public static final void show(Shell shell,Exception e){
        String title = i18n.dialog.query.executed.title;
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        MessageDialog.openError(shell, title, writer.toString());
    }
}
