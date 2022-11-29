package com.allen.testplatform.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密工具类：128位 不带偏移量
 */
public class AesUtils {

    //转16进制用的字节数组
    private final static char[] hexArray = "0123456789abcdef".toCharArray();
    //编码方式
    public static final String CODE_TYPE = "UTF-8";
    //填充类型
    //这里有个大坑,JDK默认不支持AES/ECB/PKCS7Padding,但PKCS5Padding与PKCS7Padding差不多,可以替换用
    public static final String AES_TYPE = "AES/ECB/PKCS5Padding";
    //私钥,AES固定格式为128/192/256 bits.即：16/24/32bytes
    private static final String AES_KEY = "1234567890";

    /**
     * 加密不带偏移量
     */
    public static String encrypt(String cleartext) {
        try {
            SecretKeySpec sKey = new SecretKeySpec(AES_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(AES_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, sKey);
            byte[] decrypted = cipher.doFinal(cleartext.getBytes(CODE_TYPE));
            return byteArrToHex(decrypted);
//            return Base64.encodeBase64String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 解密不带偏移量
     */
    public static String decrypt(String content) {
        try {
//            byte[] sourceBytes = Base64.decodeBase64(content);
            byte[] sourceBytes = hexToByte(content);
            Cipher cipher = Cipher.getInstance(AES_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(AES_KEY.getBytes(), "AES"));
            byte[] decoded = cipher.doFinal(sourceBytes);
            return new String(decoded, CODE_TYPE);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * byte数组转hex
     */
    public static String byteArrToHex(byte... bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * hex转byte数组
     */
    public static byte[] hexToByte(String hex){
        int m, n;
        int byteLen = hex.length() / 2; // 每两个字符描述一个字节
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = Byte.valueOf((byte)intVal);
        }
        return ret;
    }


    public static void main(String[] args) {

        String content= "q123456";
        System.out.println(encrypt(content));
        System.out.println(decrypt(encrypt(content)));
    }
}