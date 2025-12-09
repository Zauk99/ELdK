package com.diariokanto.web.security;

import com.diariokanto.web.dto.UsuarioDTO; // Asegúrate de tener este DTO en el frontend
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApiAuthenticationProvider implements AuthenticationProvider {

    @Value("${api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public ApiAuthenticationProvider() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            // Preparamos los datos para enviar a la API
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("email", email);
            loginRequest.put("password", password);

            String url = apiUrl + "/usuarios/login";

            // Hacemos la llamada POST a tu Backend
            ResponseEntity<UsuarioDTO> response = restTemplate.postForEntity(url, loginRequest, UsuarioDTO.class);

            // ... dentro del if (response.getStatusCode().is2xxSuccessful() ...

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UsuarioDTO usuario = response.getBody();

                // Creamos el Rol
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol());
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(authority);

                // --- CHIVATO PARA VER SI LLEGAMOS AQUÍ ---
                System.out.println(">>> LOGIN ÉXITO: Usuario " + usuario.getEmail() + " autenticado correctamente.");
                // -----------------------------------------

                return new UsernamePasswordAuthenticationToken(usuario, password, authorities);
            }
            // ...

        } catch (HttpClientErrorException.Unauthorized e) {
            // La API devolvió 401 -> Contraseña mal
            throw new BadCredentialsException("Credenciales incorrectas");
        } catch (Exception e) {
            // Error de conexión u otro problema
            throw new BadCredentialsException("Error de conexión con el servidor");
        }

        throw new BadCredentialsException("Error desconocido de autenticación");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}