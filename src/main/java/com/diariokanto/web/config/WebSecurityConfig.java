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
                .csrf(crsf -> crsf.disable())
                .authenticationProvider(apiAuthenticationProvider)
                .authorizeHttpRequests((requests) -> requests
                        // 1. RECURSOS PÚBLICOS (Añadimos "/error" para solucionar el fallo menor)
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
                                "/css/**",
                                "/img/**",
                                "/js/**",
                                "/error") // <--- ¡IMPORTANTE! Permitir ver la página de error
                        .permitAll()

                        // 2. SOLUCIÓN CRÍTICA: La zona ADMIN requiere Rol ADMIN
                        // (Antes estaba en permitAll, por eso entraba todo el mundo)
                        .requestMatchers("/admin/**").hasRole("ADMIN") 

                        // 3. El resto requiere estar logueado (USER o ADMIN)
                        .anyRequest().authenticated())
                
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}