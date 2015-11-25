package com.kcshu.hadoop.utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 包工具类
 * 
 * @author zhouhaichao(a)2008.sina.c
 * @version 1.0 & Aug 29, 2014 10:33:02 PM
 */
public class PackageUtils{

    /**
     * 类过滤器。当搜索类的时候只加载指定的类。
     * 
     * @author zhouhaichao(a)2008.sina.com
     */
    public interface ClassFilter{
        /**
         * 类全路径
         * 
         * @param classPath
         * @return
         */
        public boolean accept(String classPath);
    }

    public abstract static class ClassPkgAndNameFilter implements ClassFilter{
        @Override
        public boolean accept(String classPath){
            int idx = classPath.lastIndexOf(".");
            String pkg = null;
            String name = null;
            if(idx != -1){
                pkg = classPath.substring(0, idx);
                name = classPath.substring(idx + 1);
            }
            else{
                pkg = "";
                name = classPath;
            }
            return this.accept(pkg, name);
        }

        public abstract boolean accept(String pkg, String name);
    }

    private static final ClassFilter TRUE = new ClassFilter(){
        @Override
        public boolean accept(String classPath){
            return true;
        }
    };

    /**
     * 获取某包下（包括该包的所有子包）所有类
     * 
     * @param packageName
     *            包名
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName){
        return getClassName(packageName, TRUE);
    }

    /**
     * 获取某包下（包括该包的所有子包）所有类
     * 
     * @param packageName
     *            包名
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName, ClassFilter fileFilter){
        return getClassName(packageName, true, fileFilter);
    }

    public static List<String> getClassName(String packageName, boolean childPackage){
        return getClassName(packageName, childPackage, TRUE);
    }

    /**
     * 获取某包下所有类
     * 
     * @param packageName
     *            包名
     * @param childPackage
     *            是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName, boolean childPackage, ClassFilter filter){
        List<String> fileNames = new ArrayList<String>();
        String packagePath = packageName.replace(".", "/");

        Enumeration<URL> enumerations = null;
        try{
            enumerations = Thread.currentThread().getContextClassLoader().getResources(packagePath);
        }catch(Exception e){
            System.err.println("加载class错误：" + e.getMessage());
            return fileNames;
        }
        while(enumerations.hasMoreElements()){
            URL url = enumerations.nextElement();
            String protocol = url.getProtocol();
            if(protocol.equals("file")){
                fileNames = getSubFolderClassName(url.getPath(), packagePath, childPackage, filter);
            }
            else if(protocol.equals("jar")){
                fileNames = getClassNameByJar(url.getPath(), childPackage, filter);
            }
        }
        return fileNames;
    }

    /**
     * 从项目文件获取某包下所有类
     * 
     * @param filePath
     *            文件路径
     * @param className
     *            类名集合
     * @param childPackage
     *            是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getSubFolderClassName(String filePath, String packagePath, boolean childPackage, ClassFilter filter){
        List<String> myClassName = new ArrayList<String>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        for(File childFile : childFiles){
            if(childFile.isDirectory()){
                if(childPackage){
                    myClassName.addAll(getSubFolderClassName(childFile.getPath(), packagePath, childPackage, filter));
                }
            }
            else{
                String childFilePath = childFile.getPath();
                if(childFilePath.endsWith(".class")){
                    int start = childFilePath.replace(File.separator, "/").indexOf(packagePath);
                    int end = childFilePath.lastIndexOf(".");
                    childFilePath = childFilePath.substring(start, end);
                    childFilePath = childFilePath.replace("\\", ".");
                    if(filter.accept(childFilePath)){
                        myClassName.add(childFilePath);
                    }
                }
            }
        }

        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     * 
     * @param jarPath
     *            jar文件路径
     * @param childPackage
     *            是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJar(String jarPath, boolean childPackage, ClassFilter filter){
        List<String> myClassName = new ArrayList<String>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String packagePath = jarInfo[1].substring(1);
        try{
            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entrys = jarFile.entries();
            while(entrys.hasMoreElements()){
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                if(entryName.endsWith(".class")){
                    int index = entryName.lastIndexOf("/");
                    String myPackagePath;
                    if(index != -1){
                        myPackagePath = entryName.substring(0, index);
                    }
                    else{
                        myPackagePath = entryName;
                    }
                    if(myPackagePath.equals(packagePath) ^ childPackage){
                        entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                        if(filter.accept(entryName)){
                            myClassName.add(entryName);
                        }
                    }
                }
            }
            jarFile.close();
        }catch(Exception e){
        }
        return myClassName;
    }
}