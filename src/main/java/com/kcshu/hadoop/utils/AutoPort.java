package com.kcshu.hadoop.utils;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & Nov 11, 2014 3:36:31 PM
 */
public class AutoPort
{
    private static void bindPort(String host, int port) throws Exception
    {
        Socket s = new Socket();
        s.bind(new InetSocketAddress(host, port));
        s.close();
    }

    private static boolean isAvailablePort(String ip,int port)
    {
        try
        {
            bindPort(ip, port);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    /**
     * 获取一个可以使用的本地地址
     * @param startPort
     * @return
     */
    public static int getAvailablePort(int startPort) {
        
        for (int port = startPort; port < 65535 ; port++)
        {
            if(isAvailablePort("localhost", port)) {
                return port;
            }
        }
        return 0;
    }

}
