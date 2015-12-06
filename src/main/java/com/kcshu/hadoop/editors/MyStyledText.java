package com.kcshu.hadoop.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GlyphMetrics;
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
        final StyledText inputCmd = this;

        inputCmd.addLineStyleListener(new SQLLineStyleListener());

        //绑定剪切，复制，粘贴快捷键
        //绑定快捷键
        inputCmd.setKeyBinding('A' | SWT.CTRL, ST.SELECT_ALL);
        inputCmd.setKeyBinding('Z' | SWT.CTRL, ActionCode.UNDO);
        inputCmd.setKeyBinding('Y' | SWT.CTRL, ActionCode.REDO);
        inputCmd.setKeyBinding('F' | SWT.CTRL, ActionCode.CLEAR);
        inputCmd.setKeyBinding('D' | SWT.CTRL, ActionCode.DELETE);
        //is for mac
        inputCmd.setKeyBinding('A' | SWT.COMMAND, ST.SELECT_ALL);
        inputCmd.setKeyBinding('Z' | SWT.COMMAND, ActionCode.UNDO);
        inputCmd.setKeyBinding('Y' | SWT.COMMAND, ActionCode.REDO);
        inputCmd.setKeyBinding('F' | SWT.COMMAND, ActionCode.CLEAR);
        inputCmd.setKeyBinding('D' | SWT.COMMAND, ActionCode.DELETE);

        //重做，撤销内容保存
        undoManager = new UndoManager(50);
        undoManager.connect(inputCmd);
        this.setUndoManager(undoManager);

        //show line number
        inputCmd.addLineStyleListener(new LineStyleListener() {
            @Override
            public void lineGetStyle(LineStyleEvent event) {
                // Using ST.BULLET_NUMBER sometimes results in weird alignment.
                //event.bulletIndex = styledText.getLineAtOffset(event.lineOffset);
                StyleRange styleRange = new StyleRange();
                styleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);
                styleRange.background = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
                styleRange.font = inputCmd.getFont();

                int maxLine = inputCmd.getLineCount();
                int bulletLength = Integer.toString(maxLine).length();
                // Width of number character is half the height in monospaced font, add 1 character width for right padding.
                int bulletWidth = (bulletLength + 1) * inputCmd.getLineHeight() / 2;
                styleRange.metrics = new GlyphMetrics(0, 0, bulletWidth);
                event.bullet = new Bullet(ST.BULLET_TEXT, styleRange);

                // getLineAtOffset() returns a zero-based line index.
                int bulletLine = inputCmd.getLineAtOffset(event.lineOffset) + 1;
                event.bullet.text = String.format("%-" + (bulletLength) + "s ", bulletLine);
            }
        });
        inputCmd.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                // For line number redrawing.
                inputCmd.redraw();
            }
        });
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