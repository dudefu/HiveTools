package com.kcshu.hadoop.utils;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * 图片管理
 * 
 * @author zhouhaichao(a)2008.sina.com
 */
public class images{
    private static Display display = null;
    
    public static final void regShell(Display display){
        images.display = display;
    }
    public static final Image get(String icon){
        return new Image(display,images.class.getResourceAsStream("/images/"+icon+".png"));
    }
    public static final Image nil = null;

    public static final Image appIcon = get("icon");

    public static class tree{
        public static final Image root = get("root");
        
        public static final Image server = get("server");
        public static final Image server_connected = get("server_connected");

        public static final Image database = get("database");
        public static final Image table = get("table");
        public static final Image field = get("field");
        public static final Image partition = get("partition");
        
        public static final Image loading = get("loading");
    }

    public static final Image tab = get("tab");

    public static class console{
        public static final Image toRun = get("execute");
        public static final Image executeing = tree.loading;
        public static final Image stop = get("stop");

        public static final Image open = get("open");
        public static final Image save = get("save");
        
        public static final Image export = get("table_export");
        

        public static final Image previous = get("go_previous");
        public static final Image next = get("go_next");
    }
    
    public static class menu{
        public static class server{
            public static final Image add = get("new");
            public static final Image del = get("remove");
            public static final Image attr = get("attr");
            public static final Image modify = get("modify");
            //--
            public static final Image exit = get("exit");
        }
    }
    public static class popmenu{
        public static class nul{
            public static final Image add = get("new");
            public static final Image refresh = get("refresh");
        }
        public static class server{
            public static final Image connect = get("connect");
            public static final Image modify = get("modify");
            public static final Image remove = get("remove");
            
            public static final Image query = get("tab");
            public static final Image functions = get("fun");
            public static final Image refresh = nul.refresh;
            public static final Image disconnect = get("disconnect");
        }
        
        public static class database{
            public static final Image query = get("tab");
            public static final Image refresh = nul.refresh;
            public static final Image attr = get("attr");
        }
        
        public static class table{
            public static final Image query = get("tab");
            public static final Image refresh = nul.refresh;
        }
    }
    
}