package hei.school.minou.security;

import hei.school.minou.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/auth/login",
                        "/login",
                        "/ping",
                        "/health/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**")
                    .permitAll()
                    // Student self-service
                    .requestMatchers("/students/me", "/students/me/**")
                    .hasRole("STUDENT")
                    // Teacher self-service
                    .requestMatchers("/teachers/me", "/teachers/me/**")
                    .hasAnyRole("ADMIN", "TEACHER")
                    // Admin: users
                    .requestMatchers(HttpMethod.POST, "/users")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/users")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/users/me")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/users/**")
                    .hasRole("ADMIN")
                    // Admin: promotions + UI
                    .requestMatchers(HttpMethod.POST, "/promotions")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/promotions/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/promotions", "/promotions/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/ui/promotions", "/ui/promotions/**")
                    .hasRole("ADMIN")
                    // Admin: groups + student group change
                    .requestMatchers(HttpMethod.POST, "/groups")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/groups")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/students/*/group")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/students/*/grades")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/students/*/transcript/send")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/students/*/transcript")
                    .hasAnyRole("ADMIN", "STUDENT")
                    // Admin: courses create + teacher/group assignment
                    .requestMatchers(HttpMethod.POST, "/courses")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/courses/*/teachers", "/courses/*/groups")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/courses/*/assignements")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/courses")
                    .hasRole("ADMIN")
                    // Teacher + admin: course scoped reads/writes
                    .requestMatchers(
                        HttpMethod.GET,
                        "/courses/*/groups",
                        "/courses/*/students",
                        "/courses/*/grades",
                        "/courses/*/assignements")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.POST, "/courses/*/grades", "/grades")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.PUT, "/grades/**")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.POST, "/exams", "/exams/*/groups")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.GET, "/exams")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/**")
                    .denyAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
