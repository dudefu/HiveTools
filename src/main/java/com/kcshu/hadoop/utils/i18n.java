package com.kcshu.hadoop.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月13日 下午6:27:57
 */
public class i18n {
	protected static final ResourceBundle bundle = ResourceBundle.getBundle("i18n", Locale.getDefault());

	public static final String get(String key) {
		return bundle.getString(key);
	}

	public static class display {
		public static String title = get("display.title");
	}

	public static class close {
		public static String title = get("display.shell.close.title");
		public static String context = get("display.shell.close.context");
	}
	
	public static class pop{
		public static class server{
		    //服务未连接
		    public static String connect = "连接";
		    //--
			public static String modify = "修改";
			public static String del = "删除";
			
			//服务已经连接
			public static String query = "新建查询";
			//--
			public static String funs = "支持方法";
			public static String attr = "属性";
			//---
			public static String reflush = "刷新";
            public static String close = "关闭";
		}
		public static class nul{
			public static String newCon = "新建连接";
			public static String refresh = "刷新所有";
		}
		
		public static class database{
            public static String attr = "属性";
            public static String query = "新建查询";
            public static String newTab = "新建表";
            public static String refresh = "刷新";
        }
		
		public static class table{
		    public static String query = "新建查询";
		    //---
		    public static String rename = "重命名";
		    public static String del = "删除";
		    public static String modify = "编辑";
            public static String attr = "属性";
            
            public static String refresh = "刷新";
        }
	}

	public static class menu {
		public static class server{
			public static String txt = "服务器";
			public static String add = "添加";
			public static String del = "删除";
			public static String modify = "修改";
			public static String attr = "属性";
			//---
			public static String exit = "退出";
			//---
			public static String close = "关闭连接";
		}
		
		public static class tools{
			public static String txt = "工具";
			
			public static class export {
				public static String title = "导出";
				public static String excel = "Excel";
				public static String txt = "txt";
			}
		}
		
		public static class help {
			public static String txt = "帮助";
			public static String about = "关于";	
		}

        public static class console{
            public static String toRun = "执行(F9)";
            public static String stop = "停止(F7)";
            public static String executeing = "执行中..";
            
            public static String save = "保存(F6)";
            public static String open = "打开(F10)";
            
            public static String export = "导出";
            public static String previous = "上一页";
            public static String next = "下一页";
        }
	}

	public static class dialog {
		public static class about {
			public static String title = menu.help.about;
			public static String version 	= "版本";
			public static String developer = "开发者";
			public static String issue = "报告问题";
			public static String star  = "捐助";
			
			public static String versionNo = "0.0.1";
			public static String email = "zhouhaichao(a)2008.sina.com";
			public static String issueClick = "<a href=\"https://github.com/tinycalf/hivetools/issues?state=open\">"+issue+"</a>";
			public static String headTitle = "<a href=\"https://github.com/tinycalf/hivetools\">HiveServer2 Client(hivetools)</a>";
			public static String starClick = "<a href=\"https://github.com/tinycalf/hivetools/issues?state=open\">"+star+"</a>";
		}
		public static class addserver{
			public static String title = "添加管理服务器";
			public static String name = "名称：";
			public static String host = "地址：";
			public static String port = "端口：";
			public static String dbname = "库：";
			public static String username = "用户名：";
			public static String passwd = "密码：";
			public static class ctl{
				public static String ok = "确定创建";
				public static String cannel = "取消";
				public static String test = "测试连接";
			}
		}
		public static class modifyServer{
		    public static String title = "更新服务器";
		    public static class ctl{
                public static String ok = "确定更新";
                public static String cannel = "取消更新";
                public static String test = "测试连接";
            }
		}
		
		public static class query{
		    public static class executed{
	            public static String title = "警告";
	            public static String message = "正在执行！";
	        }   
		}
	}
	
	public static class tree{
		public static String server = "Hive服务";
	}
	
	public static class msgbox{
	    public static class openServerError{
	        public static String title = "消息";
            public static String message = "打开连接错误!";
	    }
	    public static class addServerError{
            public static String title = "错误";
            public static String message = "信息填写不完整！";
        }
        public static class testServerError{
            public static String title = "消息";
            public static String message = "测试连接成功!";
        }
        public static class exit{
            public static String title = "警告";
            public static String message = "检查到有任务正在运行，请确定立即停止所有任务并退出？";
        }
        public static class delServerConfirm{
            public static String title = "提示";
            public static String message = "确定删除服务";
        }
	}
	
	public static class export{
        public static String title = "提示";
        public static String message = "导出excel成功";
    }
	
	public static class tab{
	    public static String query = "查询";
	    public static String funs = i18n.pop.server.funs;
	    public static class funtab{
	        public static String name = "方法名";
	        public static String description = "方法描述";
	        public static String example = "方法示例";
	    }
	}

	public static String ok = "OK";
	
	public static void init(){
	    try{
            List<String> list = PackageUtils.getClassName("com.kcshu.hadoop.utils", false, new PackageUtils.ClassFilter(){
                @Override
                public boolean accept(String classPath){
                    return classPath.startsWith("com.kcshu.hadoop.utils.i18n");
                }
            });
            for(String clzName : list){
                Class<?> clz = Class.forName(clzName);
                String pkg = clz.getName()
                        .replace("com.kcshu.hadoop.utils.i18n$","")
                        .replace("com.kcshu.hadoop.utils.i18n","");
                pkg = pkg.replace("$",".");
                
                Field[] fields = clz.getFields();
                for(Field field : fields){
                    if(Modifier.isFinal(field.getModifiers())){
                       continue; 
                    }
                    String key = pkg + "."+field.getName();
                    if("".equals(pkg)){
                        key = field.getName();
                    }
                    field.set(null,get(key));
                }
            }
	    }catch(Exception e){
	        
	    }
    }
	public static void main(String[] args) throws Exception{
	    init();
    }
}
