package com.kbj.contextory.global.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SHA256Util {

    // 무작위 16바이트 솔트(Salt)를 생성하여 Base64로 인코딩한 문자열을 반환
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    // 비밀번호와 솔트를 결합하여 SHA-256 해시를 수행하고, 보안성을 높이기 위해 키 스트레칭을 수행
    public static String getEncrypt(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // 솔트 바이트 주입
            md.update(salt.getBytes());
            byte[] passwordBytes = md.digest(password.getBytes());
            
            // 키 스트레칭 (1000회 반복 해싱으로 브루트포스 방지)
            for (int i = 0; i < 1000; i++) {
                md.update(passwordBytes);
                passwordBytes = md.digest();
            }
            
            return Base64.getEncoder().encodeToString(passwordBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}
