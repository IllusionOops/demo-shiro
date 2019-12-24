package com.wyj;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        String newPs = new SimpleHash("MD5", "123", "abcdefg", 2).toHex();
        System.out.println("newPs====="+newPs);
        SpringApplication.run(DemoApplication.class, args);
    }

}
