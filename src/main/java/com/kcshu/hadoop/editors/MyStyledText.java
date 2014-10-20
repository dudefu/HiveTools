package com.kcshu.hadoop.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 自定义的StyledText，增加了Undo、Redo和清除操作。<br>
 * 
 * @author qujinlong
 */
public class MyStyledText extends StyledText
{
    /**
     * @param parent
     * @param style
     */
    public MyStyledText(Composite parent, int style)
    {
        super(parent, style);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.custom.StyledText#invokeAction(int)
     */
    public void invokeAction(int action)
    {
        // 增加一个自定义的删除操作，只有当选中文本的时候才将文本删除。
        // 否则，ST.DELETE_NEXT会将光标所在位置的后一个字符删除。
        if(action == ActionCode.DELETE && getSelectionCount() > 0) action = ST.DELETE_NEXT;

        super.invokeAction(action);

        switch(action)
        {
            case ActionCode.UNDO:
                undo();
            break;
            case ActionCode.REDO:
                redo();
            break;
            case ActionCode.CLEAR:
                clear();
            break;
        }
    }

    private void undo()
    {
        if(undoManager != null) undoManager.undo();
    }

    private void redo()
    {
        if(undoManager != null) undoManager.redo();
    }

    private void clear()
    {
        super.setText("");
    }

    private UndoManager undoManager = null;

    /**
     * @return Returns undoManager.
     */
    public UndoManager getUndoManager()
    {
        return undoManager;
    }

    /**
     * @param undoManager
     *            - The undoManager to set.
     */
    public void setUndoManager(UndoManager undoManager)
    {
        this.undoManager = undoManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    public void dispose()
    {
        if(undoManager != null) undoManager.disconnect();

        super.dispose();
    }

    public static class ActionCode
    {
        public static final int UNDO = Integer.MAX_VALUE;

        public static final int REDO = UNDO - 1;

        public static final int CLEAR = UNDO - 2;

        public static final int DELETE = UNDO - 3;
    }

    public static void main(String[] args)
    {
        final Display display = Display.getDefault();
        final Shell shell = new Shell();
        shell.setLayout(new GridLayout());
        shell.setSize(420, 250);
        shell.setText("SWT Application");

        MyStyledText styledText = new MyStyledText(shell, SWT.BORDER);
        GridData gd_styledText = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd_styledText.heightHint = 200;
        gd_styledText.widthHint = 400;
        styledText.setLayoutData(gd_styledText);

        // Ctrl+C, Ctrl+X, Ctrl+V 都是StyledText的默认行为。

        // styledText.setKeyBinding('C' | SWT.CTRL, ST.COPY);
        // styledText.setKeyBinding('V' | SWT.CTRL, ST.PASTE);
        // styledText.setKeyBinding('X' | SWT.CTRL, ST.CUT);

        styledText.setKeyBinding('A' | SWT.CTRL, ST.SELECT_ALL);
        styledText.setKeyBinding('Z' | SWT.CTRL, ActionCode.UNDO);
        styledText.setKeyBinding('Y' | SWT.CTRL, ActionCode.REDO);
        styledText.setKeyBinding('F' | SWT.CTRL, ActionCode.CLEAR);
        styledText.setKeyBinding('D' | SWT.CTRL, ActionCode.DELETE);

        UndoManager undoManager = new UndoManager(50);
        undoManager.connect(styledText);

        styledText.setUndoManager(undoManager);

        shell.open();

        shell.layout();

        while(!shell.isDisposed())
        {
            if(!display.readAndDispatch()) display.sleep();
        }
    }
}