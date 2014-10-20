package com.kcshu.hadoop.task;

import java.util.List;

import com.kcshu.hadoop.service.HiveServer;

/**
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月15日 下午6:00:15
 */
public class ShowDatabasesCallBack extends CallBackAdapter<List<String>>{
    @Override
    public List<String> call() throws Exception{
        List<String> databases = new HiveServer(getServer()).showDatabases();
        return databases;
    }
}
