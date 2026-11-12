package com.aquaguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AquaGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(AquaGuardApplication.class, args);
        System.out.println("===============================================================");
        System.out.println("💧 AquaGuard AI Web Application Started Successfully!");
        System.out.println("🌐 URL: http://localhost:8080");
        System.out.println("ℹ️  Mode: DEMO SIMULATION (Presentation & College Review Build)");
        System.out.println("===============================================================");
    }
}
