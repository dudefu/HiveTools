package com.kcshu.hadoop.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.kcshu.hadoop.domain.Server;

/**
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 - May 6, 2014
 **/
public class HiveServer{
    private Server server;

    public HiveServer(Server server){
        this.server = server;
    }
    protected Connection openConnect(String db) throws Exception{
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        String host = server.getHost();
        int prot = server.getPort();
        if(server.isUseSshProxy()) {
            host = "localhost";
            prot = server.getSshProxyPort();
        }
        String url = String.format("jdbc:hive2://%s:%d/%s",host,prot,db);
        Connection con = DriverManager.getConnection(url, server.getUser(), server.getPassword());
        return con;
    }

    protected void closeConnect(Connection con){
        if(con != null){
            try{
                con.close();
                con = null;
            }catch(Exception e){}
        }
    }

    public List<String> showDatabases() throws Exception{
        String db = server.getDb().trim();
        List<String> tables = new ArrayList<String>();
        Connection con = openConnect(db);
        if(!"".equals(db)){
            tables.add(db);
            closeConnect(con);
            return tables;
        }
        Statement stat = null;
        ResultSet rs = null;
        try{
            stat = con.createStatement();
            rs = stat.executeQuery("show databases");
            while(rs.next()){
                tables.add(rs.getString(1));
            }
            return tables;
        }finally{
            try{
                rs.close();
            }catch(Exception e){}
            try{
                stat.close();
            }catch(Exception e){}
            closeConnect(con);
        }
    }
    
    public List<String> showTables(String db) throws Exception{
        List<String> tables = new ArrayList<String>();
        Connection con = openConnect(db);
        Statement stat = null;
        ResultSet rs = null;
        try{
            stat = con.createStatement();
            rs = stat.executeQuery("show tables");
            while(rs.next()){
                tables.add(rs.getString(1));
            }
            return tables;
        }finally{
            try{
                rs.close();
            }catch(Exception e){}
            try{
                stat.close();
            }catch(Exception e){}
            closeConnect(con);
        }
    }
    
    public List<String> describeFunctions(String db,String fun) throws Exception{
        List<String> list = new ArrayList<String>();
        Connection con = openConnect(db);
        Statement stat = null;
        ResultSet rs = null;
        try{
            stat = con.createStatement();
            rs = stat.executeQuery("DESCRIBE FUNCTION EXTENDED "+fun);
            while(rs.next()){
                list.add(rs.getString(1));
            }
            return list;
        }finally{
            try{
                rs.close();
            }catch(Exception e){}
            try{
                stat.close();
            }catch(Exception e){}
            closeConnect(con);
        }
    }
    
    public List<String> showFunctions(String db) throws Exception{
        List<String> tables = new ArrayList<String>();
        Connection con = openConnect(db);
        Statement stat = null;
        ResultSet rs = null;
        try{
            stat = con.createStatement();
            rs = stat.executeQuery("show functions");
            while(rs.next()){
                tables.add(rs.getString(1));
            }
            return tables;
        }finally{
            try{
                rs.close();
            }catch(Exception e){}
            try{
                stat.close();
            }catch(Exception e){}
            closeConnect(con);
        }
    }

    public List<Map<String,String>> descrobe(String db,String table) throws Exception{
        List<Map<String,String>> columns = new ArrayList<Map<String,String>>();
        Connection con = openConnect(db);
        Statement stat = null;
        ResultSet rs = null;
        try{
            stat = con.createStatement();
            rs = stat.executeQuery("describe " + table);
            while(rs.next()){
                Map<String,String> column = new LinkedHashMap<String,String>();
                column.put("name", StringUtils.trimToEmpty(rs.getString(1)));
                column.put("type", StringUtils.trimToEmpty(rs.getString(2)));
                column.put("commit", StringUtils.trimToEmpty(rs.getString(3)));
                columns.add(column);
            }
            return columns;
        }finally{
            try{
                rs.close();
            }catch(Exception e){}
            try{
                stat.close();
            }catch(Exception e){}
            closeConnect(con);
        }
    }

    public List<String[]> loadGrid(String db,String hql) throws Exception{
        hql = hql.replace("\\s+", " ");
        Connection con =  openConnect(db);
        Statement stat = null;
        ResultSet rs = null;
        try{
            List<String[]> grids = new ArrayList<String[]>();
            stat = con.createStatement();
            if(stat.execute(hql)){
                rs = stat.getResultSet();
                ResultSetMetaData rsmd = rs.getMetaData();
                int cc = rsmd.getColumnCount();
                String[] header = new String[cc];
                for(int i = 0; i < cc; i++){
                    header[i] = rsmd.getColumnName(i + 1);
                }
                grids.add(header);
                while(rs.next()){
                    String[] columns = new String[cc];
                    for(int i = 0; i < cc; i++){
                        columns[i] = rs.getString(i + 1);
                    }
                    grids.add(columns);
                }
            }else{
                grids.add(new String[]{"rows updated"});
                grids.add(new String[]{String.valueOf(stat.getUpdateCount())});
            }
            return grids;
        }finally{
            try{
                rs.close();
            }catch(Exception e){}
            try{
                stat.close();
            }catch(Exception e){}
            closeConnect(con);
        }
    }
    
    public static void main(String[] args){
        Server server = new Server("name", "192.168.0.215", 10000, "redunion", "zhouhaichao","123qwe");
        HiveServer hiveServer = new HiveServer(server);
        try{
            System.out.println(hiveServer.loadGrid("redunion","select * from day_fr"));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
