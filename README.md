# Souvenir Project

## Overview
A modern web application for managing and showcasing souvenirs, built with Spring Boot and SASS.

## Technologies Used
- Spring Boot
- Spring Security
- Thymeleaf
- SASS/SCSS
- Maven
- Java 17+
- Node.js (for SASS compilation)
- SQL Server
- JPA/Hibernate

## Project Structure
```
demo/
├── src/
│   ├── main/
│   │   ├── java/          # Java source files
│   │   └── resources/
│   │       ├── static/    # Static resources
│   │       │   └── assets/
│   │       │       ├── scss/  # SASS source files
│   │       │       ├── css/   # Compiled CSS
│   │       │       ├── js/    # JavaScript files
│   │       │       └── images/
│   │       └── templates/ # Thymeleaf templates
│   └── test/             # Test files
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven
- Node.js and npm
- SQL Server 2019 or higher

### Installation
1. Clone the repository
```bash
git clone [repository-url]
```

2. Install Java dependencies
```bash
cd demo
mvn install
```

3. Install Node.js dependencies
```bash
npm install
```

4. Configure SQL Server
- Create a new database named 'sourvenir'
- Ensure SQL Server is running on localhost:1433
- Update the database credentials in `application.properties` if needed

### Running the Application

1. Start the Spring Boot application:
```bash
mvn spring-boot:run
```

2. Start SASS compilation in watch mode:
```bash
npm run sass
```

The application will be available at `http://localhost:8080`

### Database Configuration
The project uses SQL Server with the following default configuration:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=sourvenir;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=your_password
```

Important database settings:
- Server: localhost
- Port: 1433
- Database name: sourvenir
- Authentication: SQL Server Authentication
- Default username: sa
- Default password: your_password (change this in production)

### SASS Development
- SASS files are located in `src/main/resources/static/assets/scss/`
- The main SASS file is `main.scss`
- CSS is automatically compiled to `src/main/resources/static/assets/css/`
- Changes to SASS files are watched and automatically compiled when you run:
```bash
npm run sass
```

### Application Properties
Key application settings (`application.properties`):
```properties
server.port=8080
spring.thymeleaf.cache=false
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Project Features
- Modern responsive design using SASS/SCSS
- Secure authentication and authorization with Spring Security
- RESTful API architecture
- Dynamic template rendering with Thymeleaf
- SQL Server database integration
- Automatic database schema updates
- Hot-reload for template changes

## Development
- Templates are cached in production but disabled in development for hot-reloading
- SQL logging is enabled to help with debugging database operations
- Thymeleaf templates support UTF-8 encoding for proper character rendering
- Database schema is automatically updated through JPA/Hibernate

## Production Deployment Notes
- Change database password in production
- Consider enabling template caching in production
- Review SQL Server security settings
- Configure proper SSL/TLS for database connection
- Adjust `ddl-auto` setting based on deployment needs

## Contributing
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE file for details
