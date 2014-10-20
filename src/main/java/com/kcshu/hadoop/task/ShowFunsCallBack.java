package com.kcshu.hadoop.task;

import java.util.List;

import com.kcshu.hadoop.service.HiveServer;

/**
 * 显示所有支持方法
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月20日 上午11:26:43
 */
public class ShowFunsCallBack extends CallBackAdapter<List<String>>{

    protected String database;

    public ShowFunsCallBack(String database){
        this.database = database;
    }

    @Override
    public List<String> call() throws Exception{
        HiveServer hive = new HiveServer(getServer());
        return hive.showFunctions(database);
    }
}
