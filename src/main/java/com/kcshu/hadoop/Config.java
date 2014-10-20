package com.kcshu.hadoop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.kcshu.hadoop.domain.Server;

/**
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月16日 下午4:21:06
 */
public class Config{
    protected static Properties configs = null;

    private static final void init(){
        if(configs == null){
            configs = new Properties();
            load(); //加载配置项
        }
    }

    protected static void load(){
        File folder = new File(System.getProperty("user.dir"));
        if(!folder.exists()){
            folder.mkdirs();
        }
        File profile = new File(folder, "service.properties");
        try{
            configs.load(new FileInputStream(profile));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected static void write(){
        File folder = new File(System.getProperty("user.dir"));
        if(!folder.exists()){
            folder.mkdirs();
        }
        File profile = new File(folder, "service.properties");
        try{
            configs.store(new FileOutputStream(profile), "");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Properties all(){
        init();
        return configs;
    }

    public static void addServer(Server server){
        String id = server.getId();
        String name = server.getName();
        String host = server.getHost();
        String port = server.getPort();
        String dbname = server.getDb();
        String username = server.getUser();
        String passwd = server.getPassword();

        configs.put("service.id." + id, name);
        configs.put("service.host." + id, host);
        configs.put("service.port." + id, port);
        configs.put("service.dbname." + id, dbname);
        configs.put("service.username." + id, username);
        configs.put("service.passwd." + id, passwd);
        write();
    }
    
    public static void delServer(String id){
        init();
        
        configs.remove("service.id." + id);
        configs.remove("service.host." + id);
        configs.remove("service.port." + id);
        configs.remove("service.dbname." + id);
        configs.remove("service.username." + id);
        configs.remove("service.passwd." + id);
        write();
    }

    public static List<Server> services(){
        init();
        
        List<Server> services = new ArrayList<Server>();
        Set<Object> keys = configs.keySet();
        for(Object key : keys){
            if(key.toString().startsWith("service.id.")){
                String id = key.toString().replace("service.id.", "");
                services.add(getServer(id));
            }
        }
        return services;
    }
    
    public static Server getServer(String id){
        String name = configs.get("service.id."+id).toString();
        String host = configs.getProperty("service.host." + id);
        String port = configs.getProperty("service.port." + id);
        String dbname = configs.getProperty("service.dbname." + id);
        String username = configs.getProperty("service.username." + id);
        String passwd = configs.getProperty("service.passwd." + id);
        return new Server(name, host, port, dbname, username, passwd);
    }
}
