package com.cycleproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String userDir = System.getProperty("user.dir");
        System.out.println("Working directory: " + userDir);
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + userDir + "/uploads/")
                .setCachePeriod(0);
        
        System.out.println("Configuring resource handler for /uploads/** at file:" + userDir + "/uploads/");
    }
}
