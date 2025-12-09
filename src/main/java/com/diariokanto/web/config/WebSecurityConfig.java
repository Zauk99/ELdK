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
                // Registramos nuestro proveedor personalizado
                .authenticationProvider(apiAuthenticationProvider)
                .authorizeHttpRequests((requests) -> requests
                        // AÑADIMOS "/pokemon/**" para que el detalle sea público
                        .requestMatchers(
                                "/",
                                "/index",
                                "/login",
                                "/noticia/**",
                                "/pokedex/**",
                                "/registro",
                                "/pokemon/**", // <--- ¡ESTA ES LA CLAVE!
                                "/css/**",
                                "/img/**",
                                "/admin/**",
                                "/js/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true) // <--- AÑADE ESTA LÍNEA
                        .permitAll())
                .logout((logout) -> logout
                        .logoutUrl("/logout") // La URL que recibe la petición POST del botón
                        .logoutSuccessUrl("/") // <--- Al terminar, vete al Inicio
                        .permitAll()
                );

        return http.build();
    }
}