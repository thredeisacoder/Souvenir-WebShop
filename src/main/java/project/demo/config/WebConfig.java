package project.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private FlashMessageInterceptor flashMessageInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Expose error-messages.json file for client-side validation
        registry.addResourceHandler("/error-messages.json")
                .addResourceLocations("classpath:error-messages.json");
                
        // Add any other resource handlers here
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add flash message interceptor to convert error/success messages to message codes
        registry.addInterceptor(flashMessageInterceptor);
    }
} 