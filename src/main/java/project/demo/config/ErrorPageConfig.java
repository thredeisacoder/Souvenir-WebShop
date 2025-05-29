package project.demo.config;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * Cấu hình error pages để đảm bảo 404 handling hoạt động đúng cách
 */
@Configuration
public class ErrorPageConfig {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerCustomizer() {
        return (factory) -> {
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/error");
            ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error");
            ErrorPage error403Page = new ErrorPage(HttpStatus.FORBIDDEN, "/error");
            
            factory.addErrorPages(error404Page, error500Page, error403Page);
        };
    }
} 