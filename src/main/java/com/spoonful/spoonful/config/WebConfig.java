package com.spoonful.spoonful.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.media.base-path:/media}")
    private String mediaBase;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(mediaBase + "/**")
                .addResourceLocations(location);
    }
}