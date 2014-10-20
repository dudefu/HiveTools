package com.kcshu.hadoop.domain;

public class Server implements Comparable<Server>{
    private String name;
    private String host;
    private String port;
    private String db;
    private String user;
    private String password;

    public Server(String name, String host, String port, String db, String user, String password){
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

    public String getPort(){
        return port;
    }

    public void setPort(String port){
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
}
