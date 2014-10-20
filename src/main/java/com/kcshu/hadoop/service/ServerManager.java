package com.kcshu.hadoop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.kcshu.hadoop.Config;
import com.kcshu.hadoop.domain.Server;

/**
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月15日 下午5:58:46
 */
public class ServerManager{
    public static final ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
    //当前注册的服务，运行于暂停均包括
    protected static Map<String,ServerService> services = null;
    //当前运行的服务
    public static Map<UUID,Future<?>> tasks = new HashMap<UUID,Future<?>>();

    private static final void init(){
        if(services == null){
            services = new HashMap<String,ServerService>();
            List<Server> sc = Config.services();
            for(Server server : sc){
                services.put(server.getId(),new ServerService(server));
            }
        }
    }

    public static final ServerService add(Server server){
        init();
        String id = server.getId();
        if(!services.containsKey(id)){
            services.put(id, new ServerService(server));
        }
        return services.get(id);
    }
    
    public static final void remove(String serverId){
        init();
        services.remove(serverId);
    }

    public static final ServerService get(String id){
        init();
        
        if(!services.containsKey(id)){ return null; }
        return services.get(id);
    }
    
    /**
     * 停止所有正在运行的服务
     * @param atOne 是否强制退出并关闭所有服务
     * @return
     */
    public static final boolean distory(boolean atOne){
        if(atOne){
            taskExecutor.shutdownNow();
        }else{
            if(tasks.size() > 0){
                return true;
            }
            taskExecutor.shutdown();
        }
        return false;
    }
}
