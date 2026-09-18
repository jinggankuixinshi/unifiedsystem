package com.unified.common.util;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * AES-256/GCM 加解密（基于 Spring Security Crypto，随机 IV 随密文携带，避免自维护 JCE 代码）
 * salt 为十六进制字符串，仅参与密钥派生（防彩虹表）
 */
public class AesUtil {

    private static final String SALT = "6b1f2e3a4c5d6e7f";

    public static String encrypt(String plainText, String password) {
        return encryptor(password).encrypt(plainText);
    }

    public static String decrypt(String encrypted, String password) {
        return encryptor(password).decrypt(encrypted);
    }

    private static TextEncryptor encryptor(String password) {
        // delux：AES-256/GCM，随机 IV 随密文携带
        return Encryptors.delux(password, SALT);
    }
}
