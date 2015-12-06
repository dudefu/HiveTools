package com.kcshu.hadoop.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.wb.swt.SWTResourceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLLineStyleListener implements LineStyleListener{

    private Color keyword = SWTResourceManager.getColor(SWT.COLOR_BLUE);
    private Color note_color = SWTResourceManager.getColor(SWT.COLOR_GRAY);

    private Color param_key_color = SWTResourceManager.getColor(SWT.COLOR_RED);
    private Color param_value_color = SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN);

    String[] keywords = new String[]{
            "SELECT",
            "FROM",
            "LEFT", "JOIN", "RIGHT",

            "WHERE", "AND", "OR", "GROUP", "HAVING", "BY",
            "ORDER", "DESC",
            "UNION", "ALL",
            "LIMIT",

            "CREATE","TABLE",
            "INSERT","IS",
            
            "STRING","INT","BIGINT","MAP","COMMENT","PARTITIONED",
            "ROW","FORMAT","DELIMITED","FIELDS","TERMINATED", 
            "COLLECTION","ITEMS","KEYS",
            "ALTER","DATABASE","STORED AS","LOCATION","PARTITION",
            "LOAD","DATA","LOCAL","OVERWRITE","INTO",
            "SHOW","TABLES",
            "DROP","IF","EXISTS","SUM","FORMATTED",
            
            "LEFT","RIGHT","JOIN","OUTER",

            "DISTINCT",

            "NULL","=","<","+","-","*","/","%","&","||"
    };

    @Override
    public void lineGetStyle(LineStyleEvent event){
        if(keywords == null || keywords.length == 0){ return; }

        String lineText = event.lineText.toUpperCase();
        //空行不做任何处理
        if ("".equals(lineText.trim())) {
            return;
        }

        //有内容处理颜色值
        List<StyleRange> styles = new ArrayList<StyleRange>();
        if (lineText.startsWith("--")) {
            Pattern pattern = Pattern.compile("--[\\s]*(set|SET)[\\s]*([a-zA-Z0-9_]*)[\\s]*=[\\s]*(.*)");
            Matcher m = pattern.matcher(lineText);
            if (m.matches()) {
                //set
                styles.add(new StyleRange(event.lineOffset + m.start(1), m.end(1) - m.start(1), keyword, null));
                //key
                styles.add(new StyleRange(event.lineOffset + m.start(2), m.end(2) - m.start(2), param_key_color, null));
                //value
                styles.add(new StyleRange(event.lineOffset + m.start(3), m.end(3) - m.start(3), param_value_color, null));
            } else {
                //整行注释
                styles.add(new StyleRange(event.lineOffset, lineText.length(), note_color, null));
            }
        } else {
            int start = 0;
            int length = lineText.length();
            while (start < length) {
                if (Character.isLetter(lineText.charAt(start))) {
                    StringBuffer buf = new StringBuffer();
                    int i = start;
                    for (; i < length && Character.isLetter(lineText.charAt(i)); i++) {
                        buf.append(lineText.charAt(i));
                    }
                    if (Arrays.asList(keywords).contains(buf.toString())) {
                        styles.add(new StyleRange(event.lineOffset + start, i - start, keyword, null, SWT.NORMAL));
                    }
                    start = i;
                } else {
                    start++;
                }
            }

            start = 0;
            while (start < length && (start = lineText.indexOf("${", start)) != -1) {
                if(start >= length){
                    break;
                }
                int end = lineText.indexOf("}", start + 2);
                if(end == -1){
                    break;
                }
                styles.add(new StyleRange(event.lineOffset + start, end - start + 1, param_key_color , null, SWT.NORMAL));
                start = end + 1;
            }
        }
        event.styles = (StyleRange[]) styles.toArray(new StyleRange[0]);
    }
}