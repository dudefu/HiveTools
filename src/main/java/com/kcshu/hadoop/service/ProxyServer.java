package com.kcshu.hadoop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kcshu.hadoop.Config;
import com.kcshu.hadoop.domain.Server;
import com.kcshu.hadoop.utils.AutoPort;

/**
 * SSH代理服务
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & Nov 21, 2014 10:14:04 AM
 */
public class ProxyServer
{
    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);

    protected static ProxyServer inited = null;
    public static ProxyServer init() {
        if(inited == null) {
            inited = new ProxyServer();
        }
        return inited;
    }
    protected JSch jsch;
    protected Map<Server,Session> sessions = null;
    
    private ProxyServer()
    {
        jsch = new JSch();
        sessions = new HashMap<Server, Session>();
    }
    
    public void close() {
        for (Server server : sessions.keySet())
        {
            close(server);
        }
    }
    public void close(Server server) {
        if(sessions.containsKey(server)) {
            System.out.println("关闭："+server.getSshHost()+"："+server.getSshPort()+"->"+server.getSshProxyPort());
            try{
                sessions.get(server).disconnect();
            }catch (Exception e){
            }
            sessions.remove(server);
        }
    }
    public synchronized boolean add(Server server) {
        if( sessions.containsKey(server) ) { //已经添加了
            Session session = sessions.get(server);
            if(session.isConnected()) {
                try{
                    //发送一条心跳检查消息，如果能发送成功说明，还是连接着，如果不成功就需要重新连接
                    session.sendKeepAliveMsg();
                    return true;
                }catch (Exception e){
                    logger.info("连接已经断开，从新连接吧");
                }
            }else {
                try{ session.disconnect(); }catch (Exception e){}
            }
        }
        
        String rhost = server.getHost();//远程地址
        int rport = server.getPort();//远程端口
        int lport = AutoPort.getAvailablePort(rport);//代理本地端口
        
        String sshHost = server.getSshHost();// SSH服务器
        int sshPort = server.getSshPort();// SSH访问端口
        String sshUser = server.getSshUserName();// SSH连接用户名
        String sshPassword = server.getSshPassword();// SSH连接密码
        
        Session session = null;
        try{
            session = jsch.getSession(sshUser, sshHost, sshPort);
            session.setConfig("StrictHostKeyChecking", "no");
            if(server.isPushKey()) {
                if("".equals(sshPassword.trim())) {
                    jsch.addIdentity(server.getSshKeyFile()); 
                }else {
                    jsch.addIdentity(server.getSshKeyFile(),sshPassword);
                }
            }else {
                session.setPassword(sshPassword);
            }
            session.connect();
            session.sendKeepAliveMsg();
            logger.info("SSH 代理连接成功，"+session.getServerVersion() );
            
            int assinged_port = session.setPortForwardingL(lport, rhost, rport);
            server.setSshProxyPort(assinged_port);
            logger.info(String.format("代理成功： %s:%d => localhost:%d",rhost,rport,assinged_port));
            
            sessions.put(server, session);
            return true;
        }
        catch (Exception e)
        {
            logger.error("连接错误",e);
            if(session != null) {
                try{
                    session.disconnect();
                }catch (Exception e1){}   
            }
            return false;   
        }
    }
    
    public static void main(String[] args)
    {
        List<Server> servers = Config.services();
        Server server = servers.get(0);
        init().add(server);
    }
}
