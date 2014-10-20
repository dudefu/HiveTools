package com.kcshu.hadoop.task;

import com.kcshu.hadoop.domain.Server;

/**
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月15日 下午5:57:15
 */
public abstract class CallBackAdapter<V> implements CallBack<V>{

    protected Server server;

    public void setServer(Server server){
        this.server = server;
    }

    public Server getServer(){
        return server;
    }

    @Override
    public void onData(V param){

    }

    @Override
    public void onException(Exception e){

    }

    @Override
    public V call() throws Exception{
        return null;
    }
}
