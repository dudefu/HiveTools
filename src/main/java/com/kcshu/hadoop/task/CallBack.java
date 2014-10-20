package com.kcshu.hadoop.task;

import java.util.concurrent.Callable;

import com.kcshu.hadoop.domain.Server;

/**
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月14日 下午4:30:14
 */
public interface CallBack<V> extends Callable<V>{
    public void onData(V param);

    public void onException(Exception e);

    public void setServer(Server serverId);
}
