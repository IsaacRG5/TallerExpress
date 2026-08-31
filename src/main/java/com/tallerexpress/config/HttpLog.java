package com.tallerexpress.config;
import java.time.LocalDateTime;
public final class HttpLog { private HttpLog(){} public static void log(String method,String resource,String detail){System.out.printf("[%s] %s %s -> %s%n", LocalDateTime.now(),method,resource,detail==null?"":detail);} }
