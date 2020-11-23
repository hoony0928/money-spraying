package com.kakaopay.spraying;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableJpaRepositories
@EnableRedisRepositories
@SpringBootApplication
public class SprayingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SprayingApplication.class, args);
    }

}
