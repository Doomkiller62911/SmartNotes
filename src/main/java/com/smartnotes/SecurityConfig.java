package com.smartnotes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    
    public static final String[] PUBLIC_URLS = {
       "/login", "/recuperar/**", "/restablecer/**",
        "/css/**", "/js/**", "/webjars/**"
    };
    
    public static final String[] ADMIN_URLS = {
         "/usuarios/**", "/actividad/**",
        "/plantillas/nueva", "/plantillas/administracion", "/plantillas/guardar",
        "/plantillas/*/editar", "/plantillas/*/estado"
    };
    
    public static final String[] ADMIN_OR_RELEASE_URLS = {
         "/revision/**"
    };
    
    public static final String[] REDACTORES_URLS = {
        "/Generador_Notas/**", "/notificaciones/**"
    };
    
    public static final String[] BITACORA_URLS = {
        "/bitacora/**"
    };
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request -> request
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(ADMIN_URLS).hasRole("ADMIN")
                .requestMatchers(ADMIN_OR_RELEASE_URLS).hasAnyRole("ADMIN", "RELEASE")
                .requestMatchers(REDACTORES_URLS).hasAnyRole("LIDER", "SUPERVISOR", "QC")
                .requestMatchers(BITACORA_URLS).hasAnyRole("LIDER", "SUPERVISOR", "QC", "RELEASE")
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
                )
                .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                );
        return http.build();
    }
}
