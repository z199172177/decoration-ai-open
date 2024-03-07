package com.jd.decoration.ai.soa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@EnableCaching
@SpringBootApplication(scanBasePackages = {"com.jd.decoration.ai"})
@ImportResource(value = {"classpath:spring-root.xml"})
public class DecorationAiSoaApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(DecorationAiSoaApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(DecorationAiSoaApplication.class);
    }

}

