package project.demo.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.PostConstruct;

@Configuration
public class DatabaseConfig {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private Environment env;

    @PostConstruct
    public void initialize() {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("=== DATABASE CONNECTION TEST ===");
            System.out.println("Database connection successful!");
            System.out.println("Database URL: " + env.getProperty("spring.datasource.url"));
            System.out.println("Database username: " + env.getProperty("spring.datasource.username"));
            System.out.println("Database product name: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("Database product version: " + connection.getMetaData().getDatabaseProductVersion());
            System.out.println("Catalog: " + connection.getCatalog());
            System.out.println("Schema: " + connection.getSchema());
            System.out.println("==============================");
        } catch (SQLException e) {
            System.err.println("=== DATABASE CONNECTION ERROR ===");
            System.err.println("Failed to establish database connection!");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error code: " + e.getErrorCode());
            e.printStackTrace();
            System.err.println("===============================");
        }
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        // Test database connection and presence of Customer table
        try {
            String sql = "SELECT COUNT(*) FROM Customer";
            int count = jdbcTemplate.queryForObject(sql, Integer.class);
            System.out.println("=== TABLE CHECK ===");
            System.out.println("Found " + count + " records in Customer table");
            System.out.println("===================");
        } catch (Exception e) {
            System.err.println("=== TABLE CHECK ERROR ===");
            System.err.println("Error accessing Customer table: " + e.getMessage());
            System.err.println("==========================");
        }
        
        return jdbcTemplate;
    }
}