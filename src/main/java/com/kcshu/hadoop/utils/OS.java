package com.kcshu.hadoop.utils;

/**
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 &23:502015/12/18
 */
public class OS {

    private static String OS = System.getProperty("os.name").toLowerCase();
    private static com.kcshu.hadoop.utils.OS _instance = new OS();

    private OS() {
    }


    public static boolean isLinux() {
        return OS.indexOf("linux") >= 0;
    }

    public static boolean isMacOS() {
        return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") < 0;
    }


    public static boolean isMacOSX() {
        return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") > 0;
    }

    public static boolean isWindows() {
        return OS.indexOf("windows") >= 0;
    }
}
