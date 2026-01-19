package com.diariokanto.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.diariokanto.web.security.ApiAuthenticationProvider;

@Configuration
public class WebSecurityConfig {

    @Autowired
    private ApiAuthenticationProvider apiAuthenticationProvider; // Inyectamos nuestro proveedor

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(apiAuthenticationProvider)
            .authorizeHttpRequests((requests) -> requests
                // 1. RECURSOS PÚBLICOS Y ESTÁTICOS (¡IMPORTANTE: PONER ESTO PRIMERO!)
                .requestMatchers(
                        "/", 
                        "/index", 
                        "/login", 
                        "/registro", 
                        "/noticia/**",
                        "/pokedex/**",
                        "/pokemon/**",
                        "/minijuegos/**",
                        "/equipos",       
                        "/equipos/**",
                        "/css/**",   // <--- ESTO ES CRUCIAL PARA EL ERROR DE CSS
                        "/img/**",   // <--- ESTO PARA LAS IMÁGENES
                        "/js/**",    // <--- ESTO PARA EL JAVASCRIPT
                        "/error")
                .permitAll()

                // 2. ZONA 2FA (Solo para el rol temporal PRE_AUTH)
                .requestMatchers("/login-2fa", "/verificar-2fa").hasRole("PRE_AUTH")

                // 3. ZONA ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 4. RESTO DE RUTAS (Requiere login normal USER o ADMIN)
                .anyRequest().authenticated())
            
            .formLogin((form) -> form
                .loginPage("/login")
                // ... tu configuración de éxito del login ...
                .successHandler((request, response, authentication) -> {
                    // Tu lógica de redirección si es PRE_AUTH o normal
                    boolean isPreAuth = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_PRE_AUTH"));
                        
                    if (isPreAuth) {
                        response.sendRedirect("/login-2fa");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .permitAll())
            .logout((logout) -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}