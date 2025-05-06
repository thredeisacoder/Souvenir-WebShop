package project.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomAuthenticationProvider authProvider;
        
        @Autowired
        private CustomAccessDeniedHandler accessDeniedHandler;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/cart/**").authenticated()
                                                .requestMatchers("/account/**").authenticated()
                                                .requestMatchers("/checkout/**").authenticated()
                                                .requestMatchers("/**").permitAll())
                                .exceptionHandling(exceptions -> exceptions
                                                .accessDeniedHandler(accessDeniedHandler)
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        // Chuyển hướng đến trang đăng nhập với tham số denied
                                                        String redirectUrl = "/auth/login?denied=true";
                                                        
                                                        // Lấy đường dẫn hiện tại để có thể chuyển hướng lại sau khi đăng nhập
                                                        String requestUri = request.getRequestURI();
                                                        if (requestUri.startsWith("/cart")) {
                                                            redirectUrl = "/auth/login?redirect=cart";
                                                        }
                                                        
                                                        response.sendRedirect(redirectUrl);
                                                }))
                                .csrf(csrf -> csrf.disable())
                                .addFilterBefore(new SessionAuthenticationFilter(),
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationManager authManager(HttpSecurity http) throws Exception {
                AuthenticationManagerBuilder authenticationManagerBuilder = http
                                .getSharedObject(AuthenticationManagerBuilder.class);
                authenticationManagerBuilder.authenticationProvider(authProvider);
                return authenticationManagerBuilder.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}