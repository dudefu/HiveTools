package com.kcshu.hadoop.service;

import java.util.UUID;
import java.util.concurrent.Future;

import org.eclipse.swt.widgets.Display;

import com.kcshu.hadoop.domain.Server;
import com.kcshu.hadoop.task.CallBack;

/**
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月14日 下午4:25:21
 */
public class ServerService{
    protected Server server;

    protected ServerService(Server server){
        this.server = server;
    }

    public Server getServer(){
        return server;
    }

    /**
     * 返回所有databases
     * @param back
     */
    public <V> UUID showDatabases(CallBack<V> back){
        return this.execute(back);
    }
    
    /**
     * 显示所有tables
     * @param back
     * @return
     */
    public <V> UUID showTables(CallBack<V> back){
        return this.execute(back);
    }

    public <V> UUID describe(CallBack<V> back){
        return this.execute(back);
    }
    
    /**
     * 执行某些任务
     * 
     * @param id
     * @param back
     */
    public <V> UUID execute(final CallBack<V> back){
        final UUID taskId = UUID.randomUUID();
        back.setServer(server);
        Future<?> future = ServerManager.taskExecutor.submit(new Runnable(){
            @Override
            public void run(){
                try{
                    if( !ProxyServer.init().add(server) ) {
                        back.onException(new Exception("未能代开代理地址"));
                        return;
                    }
                    final V out = back.call();
                    if(!Thread.interrupted()){ //未停止
                        Display.getDefault().syncExec(new Runnable(){
                            @Override
                            public void run(){
                                back.onData(out);                
                            }
                        }); 
                    }
                }catch(final Exception e){
                    Display.getDefault().syncExec(new Runnable(){
                        @Override
                        public void run(){
                            back.onException(e);
                        }
                    }); 
                }
                ServerManager.tasks.remove(taskId);
            }
        });
        ServerManager.tasks.put(taskId,future);
        return taskId;
    }
    public void killTask(UUID uuid){
        if(ServerManager.tasks.containsKey(uuid)){
            Future<?> future = ServerManager.tasks.get(uuid);
            try{
                future.cancel(true);
            }catch(Exception e){
                e.printStackTrace();
            }
            ServerManager.tasks.remove(uuid);
        }
    }

    public boolean test()
    {
        HiveServer hive = new HiveServer(getServer());
        try {
            hive.showDatabases();
            return true;
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void close() {
        ProxyServer.init().close(server);
    }
}
