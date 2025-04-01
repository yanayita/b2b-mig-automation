package com.schneider.ei.b2b.mig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class B2BMigAutomation {

    public static void main(String... args) {
        System.setProperty("java.io.tmpdir", "./temporary");
        SpringApplication.run(B2BMigAutomation.class);
    }
}
