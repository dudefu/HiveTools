package com.kcshu.hadoop.domain;

public class Server implements Comparable<Server>{
    private String name;
    
    //远程连接信息
    private String host;
    private int port;
    private String db;
    private String user;
    private String password;

    //ssh代理信息
    private boolean useSshProxy = false;
    private String sshHost;
    private int sshPort;
    private String sshUserName;
    private String sshPassword;
    private String sshKeyFile;//公钥文件
    private boolean isPushKey = false;//是否使用公钥方式
    
    private int sshProxyPort;//使用SSH代理后本地地址
    
    public Server(String name, String host, int port, String db, String user, String password){
        super();
        this.name = name;
        this.host = host;
        this.port = port;
        this.db = db;
        this.user = user;
        this.password = password;
    }

    public String getId(){
        return String.valueOf(Math.abs(getName().hashCode()));
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getHost(){
        return host;
    }

    public void setHost(String host){
        this.host = host;
    }

    public int getPort(){
        return port;
    }

    public void setPort(int port){
        this.port = port;
    }

    public String getDb(){
        return db;
    }

    public void setDb(String db){
        this.db = db;
    }

    public String getUser(){
        return user;
    }

    public void setUser(String user){
        this.user = user;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    @Override
    public int compareTo(Server o){
        if(null == o){
            return 1;
        }
        return this.name.compareTo(o.getName());
    }

    public String getSshHost()
    {
        return sshHost;
    }

    public void setSshHost(String sshHost)
    {
        this.sshHost = sshHost;
        this.useSshProxy = true;
    }

    public int getSshPort()
    {
        return sshPort;
    }

    public void setSshPort(int sshPort)
    {
        this.sshPort = sshPort;
    }

    public String getSshUserName()
    {
        return sshUserName;
    }

    public void setSshUserName(String sshUserName)
    {
        this.sshUserName = sshUserName;
    }

    public String getSshPassword()
    {
        return sshPassword;
    }

    public void setSshPassword(String sshPassword)
    {
        this.sshPassword = sshPassword;
    }

    public boolean isPushKey()
    {
        return isPushKey;
    }

    public boolean isUseSshProxy()
    {
        return useSshProxy;
    }

    public int getSshProxyPort()
    {
        return sshProxyPort;
    }

    public void setSshProxyPort(int sshProxyPort)
    {
        this.sshProxyPort = sshProxyPort;
    }

    public String getSshKeyFile()
    {
        return sshKeyFile;
    }

    public void setSshKeyFile(String sshKeyFile)
    {
        this.sshKeyFile = sshKeyFile;
        this.isPushKey = true;
    }

    @Override
    public String toString()
    {
        return "Server [name=" + name + ", host=" + host + ", port=" + port + ", db=" + db + ", user=" + user + ", password=" + password + ", useSshProxy="
                + useSshProxy + ", sshHost=" + sshHost + ", sshPort=" + sshPort + ", sshUserName=" + sshUserName + ", sshPassword=" + sshPassword
                + ", sshKeyFile=" + sshKeyFile + ", isPushKey=" + isPushKey + ", sshProxyPort=" + sshProxyPort + "]";
    }
    
}
