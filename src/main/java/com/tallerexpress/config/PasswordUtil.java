package com.tallerexpress.config;
import java.nio.charset.StandardCharsets; import java.security.MessageDigest;
public final class PasswordUtil {
 private PasswordUtil(){}
 public static String sha256(String text){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)); StringBuilder s=new StringBuilder(); for(byte x:b)s.append(String.format("%02x",x)); return s.toString();}catch(Exception e){throw new IllegalStateException(e);}}
}
