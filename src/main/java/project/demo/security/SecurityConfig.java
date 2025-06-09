package project.demo.security;

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

import project.demo.security.oauth2.CustomOAuth2UserService;
import project.demo.security.oauth2.OAuth2AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomAuthenticationProvider authProvider;
        private final CustomAccessDeniedHandler accessDeniedHandler;

        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

        public SecurityConfig(CustomAuthenticationProvider authProvider,
                        CustomAccessDeniedHandler accessDeniedHandler,
                        CustomOAuth2UserService customOAuth2UserService,
                        OAuth2AuthenticationSuccessHandler oauth2SuccessHandler) {
                this.authProvider = authProvider;
                this.accessDeniedHandler = accessDeniedHandler;
                this.customOAuth2UserService = customOAuth2UserService;
                this.oauth2SuccessHandler = oauth2SuccessHandler;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/cart/**").authenticated()
                                                .requestMatchers("/account/**").authenticated()
                                                .requestMatchers("/checkout/**").authenticated()
                                                .requestMatchers("/auth/complete-profile").authenticated()
                                                .requestMatchers("/oauth2/**").permitAll()
                                                .requestMatchers("/**").permitAll())
                                .exceptionHandling(exceptions -> exceptions
                                                .accessDeniedHandler(accessDeniedHandler)
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        // Chuyển hướng đến trang đăng nhập với tham số denied
                                                        String redirectUrl = "/auth/login?denied=true";

                                                        // Lấy đường dẫn hiện tại để có thể chuyển hướng lại sau khi
                                                        // đăng nhập
                                                        String requestUri = request.getRequestURI();
                                                        if (requestUri.startsWith("/cart")) {
                                                                redirectUrl = "/auth/login?redirect=cart";
                                                        }

                                                        response.sendRedirect(redirectUrl);
                                                }))
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/auth/login")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(oauth2SuccessHandler)
                                                .failureUrl("/auth/login?error=oauth2"))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/")
                                                .deleteCookies("JSESSIONID")
                                                .invalidateHttpSession(true))
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