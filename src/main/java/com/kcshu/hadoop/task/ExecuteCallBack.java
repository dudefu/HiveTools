package com.kcshu.hadoop.task;

import java.util.List;

import com.kcshu.hadoop.service.HiveServer;


/**
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月15日 下午6:00:15
 */
public class ExecuteCallBack extends CallBackAdapter<List<String[]>>{
    
    private String hql;
    private String database;
    
    public ExecuteCallBack(String database,String hql){
        this.hql = hql;
        this.database = database;
    }

    @Override
    public List<String[]> call() throws Exception{
        return new HiveServer(getServer()).loadGrid(database, hql);
    }
}
