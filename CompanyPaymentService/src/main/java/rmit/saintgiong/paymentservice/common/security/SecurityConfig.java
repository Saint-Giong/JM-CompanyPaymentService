package rmit.saintgiong.paymentservice.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import rmit.saintgiong.shared.type.Role;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final JweAuthRequestFilter jweAuthRequestFilter;

        public SecurityConfig(JweAuthRequestFilter jweAuthRequestFilter) {
                this.jweAuthRequestFilter = jweAuthRequestFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                // CORS is handled by the Gateway - disable here to avoid duplicate headers
                                .cors(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                // PUBLIC ENDPOINT FOR ACTUATOR and SWAGGER
                                                .requestMatchers(
                                                                "/actuator/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/api-docs/**",
                                                                "/api-docs.yaml",
                                                                "/v3/api-docs/**",
                                                                "/favicon.ico")
                                                .permitAll()
                                                .requestMatchers(
                                                        "/stripe/**"
                                                ).permitAll()
                                                .anyRequest().hasRole(Role.COMPANY.name()))
                                .sessionManagement(
                                                session -> session
                                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jweAuthRequestFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
