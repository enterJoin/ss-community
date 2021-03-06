package com.atlxw.community.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * TODO SHA256加密原理一定要知道！
 */
public class Sha256 {
    /**
     * 利用hava原生的类实现SHA256加密
     * @param str   加密后的报文
     * @return
     */
    public static String getSHA256(String str){
        MessageDigest messageDigest;
        String encodestr = "";

        try {
            messageDigest = MessageDigest.getInstance("SHA-256");

            messageDigest.update(str.getBytes("UTF-8"));
            encodestr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encodestr;
    }

    /**
     * 将byte转化为16进制
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes){
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;

        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if(temp.length() == 1){
                //得到一位的进行补0的操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }

        return stringBuffer.toString();
    }
}
