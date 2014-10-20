package com.kcshu.hadoop.task;

import java.util.List;
import java.util.Map;

import com.kcshu.hadoop.service.HiveServer;

/**
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月15日 下午6:00:15
 */
public class DescribeCallBack extends CallBackAdapter<List<Map<String,String>>>{

    protected String database;
    protected String table;

    public DescribeCallBack(String database,String table){
        this.database = database;
        this.table = table;
    }

    @Override
    public List<Map<String,String>> call() throws Exception{
        HiveServer hive = new HiveServer(getServer());
        return hive.descrobe(database,table);
    }
}
