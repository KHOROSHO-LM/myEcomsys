package org.example.ecms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.ecms.mapper")
public class EcmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcmsApplication.class, args);
    }

}
