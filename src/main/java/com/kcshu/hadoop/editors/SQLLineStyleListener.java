package com.kcshu.hadoop.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.wb.swt.SWTResourceManager;

public class SQLLineStyleListener implements LineStyleListener{

    private Color color = SWTResourceManager.getColor(SWT.COLOR_BLUE);
    private Color variableColor = SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN);

    String[] keywords = new String[]{
            "SELECT", "FROM",
            "WHERE", "AND", "OR", "GROUP","HAVING","BY",
            "ORDER", "DESC",
            "UNION","ALL",
            "LIMIT",
            "CREATE","TABLE",
            
            "STRING","INT","BIGINT","MAP","COMMENT","PARTITIONED",
            "ROW","FORMAT","DELIMITED","FIELDS","TERMINATED", 
            "COLLECTION","ITEMS","KEYS",
            "ALTER","DATABASE","STORED AS","LOCATION","PARTITION",
            "LOAD","DATA","LOCAL","OVERWRITE","INTO",
            "SHOW","TABLES",
            "DROP","IF","EXISTS","SUM","FORMATTED",
            
            "LEFT","RIGHT","JOIN","OUTER",
            
            "NULL","=","<","+","-","*","/","%","&","||"
    };

    @Override
    public void lineGetStyle(LineStyleEvent event){
        if(keywords == null || keywords.length == 0){ return; }

        String lineText = event.lineText.toUpperCase();

        List<StyleRange> styles = new ArrayList<StyleRange>();
        int start = 0;
        int length = lineText.length();
        while(start < length){
            if(Character.isLetter(lineText.charAt(start))){
                StringBuffer buf = new StringBuffer();
                int i = start;
                for(; i < length && Character.isLetter(lineText.charAt(i)); i++){
                    buf.append(lineText.charAt(i));
                }
                if(Arrays.asList(keywords).contains(buf.toString())){
                    styles.add(new StyleRange(event.lineOffset + start, i - start, color, null, SWT.NORMAL));
                }
                start = i;
            }
            else if(lineText.charAt(start) == '#'){
                StringBuffer buf = new StringBuffer();
                buf.append('#');
                int i = start + 1;
                for(; i < length && Character.isLetter(lineText.charAt(i)); i++){
                    buf.append(lineText.charAt(i));
                }
                if(buf.toString().matches("#[a-zA-Z]+\\d?")){
                    styles.add(new StyleRange(event.lineOffset + start, i - start, variableColor, null, SWT.NORMAL));
                }
                start = i;
            }
            else{
                start++;
            }
        }
        event.styles = (StyleRange[]) styles.toArray(new StyleRange[0]);
    }

}