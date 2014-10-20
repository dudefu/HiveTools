package com.kcshu.hadoop.tab;


/**
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月15日 下午3:03:35
 */
public interface Tab{
    //tab属性
    public static final String TAB_ID = "tabId";//tabId
    public static final String TAB_TYPE = "tabType";
    public static final String TAB_QUERY_EXECUTE = "tabQuerying";//查询tab是否正在查询
    
    public boolean close();
    public boolean canClose();
    public String getServerId();
}
