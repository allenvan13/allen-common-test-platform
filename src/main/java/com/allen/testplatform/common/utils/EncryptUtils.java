package com.allen.testplatform.common.utils;

import com.xiaoleilu.hutool.crypto.Mode;
import com.xiaoleilu.hutool.crypto.Padding;
import com.xiaoleilu.hutool.crypto.symmetric.AES;

public class EncryptUtils {

    private static final String ENCRYPT_KEY = "NHDC-ACCOUNT-USE";
    private static final String ENCRYPT_IV = "1234512345123411";

    public static String decrypt(String password) {
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, ENCRYPT_KEY.getBytes(), ENCRYPT_IV.getBytes());
        return aes.decryptStr(password);
    }

    public static String encrypt(String password) {
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, ENCRYPT_KEY.getBytes(), ENCRYPT_IV.getBytes());
        return aes.encryptHex(password);
    }

    public static void main(String[] args) {

//        String x = "L4GA42Q9XJWTI50LW4PYWTW8OPIUDIV8";
//        System.out.println(x);
//
//        System.out.println(encrypt(x));
//
//        System.out.println(decrypt("YXBwX2U2MjdhZjQwOm54cnA1ZHJ2bHRscjVreDQ="));


        System.out.println(encrypt("JIVCPUROF6IEZDMWLO8N7H126HKGTWAS"));
    }
}
