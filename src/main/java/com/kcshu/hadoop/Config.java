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
        delServer(id);
        
        String name = server.getName();
        String host = server.getHost();
        int port = server.getPort();
        String dbname = server.getDb();
        String username = server.getUser();
        String passwd = server.getPassword();

        configs.put("service.id." + id, name);
        configs.put("service.host." + id, host);
        configs.put("service.port." + id, String.valueOf(port));
        configs.put("service.dbname." + id, dbname);
        configs.put("service.username." + id, username);
        configs.put("service.passwd." + id, passwd);
        
        if(server.isUseSshProxy()) {
            String sshHost = server.getSshHost();
            int sshPort = server.getSshPort();
            String sshUsername = server.getSshUserName();
            String sshPassword = server.getSshPassword();
            
            configs.put("service.ssh.host." + id, sshHost);
            configs.put("service.ssh.port." + id, String.valueOf(sshPort));
            configs.put("service.ssh.username." + id, sshUsername);
            configs.put("service.ssh.passwd." + id, sshPassword);
            
            if(server.isPushKey()) {
                String keyFile = server.getSshKeyFile();
                configs.put("service.ssh.iskey." + id,keyFile);
            }
            
        }
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
        
        configs.remove("service.ssh.host." + id);
        configs.remove("service.ssh.port." + id);
        configs.remove("service.ssh.username." + id);
        configs.remove("service.ssh.passwd." + id);
        configs.remove("service.ssh.iskey." + id);
        
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
        int port = Integer.parseInt(configs.getProperty("service.port." + id));
        String dbname = configs.getProperty("service.dbname." + id);
        String username = configs.getProperty("service.username." + id);
        String passwd = configs.getProperty("service.passwd." + id);
        Server server = new Server(name, host, port, dbname, username, passwd);
        
        String sshHost = configs.getProperty("service.ssh.host."+id);
        if( sshHost != null && !"".equals(sshHost.trim()) ) {
            server.setSshHost(sshHost);
            
            int sshPort = Integer.parseInt(configs.getProperty("service.ssh.port." + id));
            String sshUserName = configs.getProperty("service.ssh.username." + id);
            String sshPassword = configs.getProperty("service.ssh.passwd." + id);
            String keyFile = configs.getProperty("service.ssh.iskey." + id);
            
            server.setSshPort(sshPort);
            server.setSshUserName(sshUserName);
            server.setSshPassword(sshPassword);
            if(keyFile != null && !"".equals(keyFile.trim())) {
                server.setSshKeyFile(keyFile);
            }
        }
        return server;
    }
}
