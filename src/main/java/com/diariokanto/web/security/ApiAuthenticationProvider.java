package com.diariokanto.web.security;

import com.diariokanto.web.dto.UsuarioDTO;
import com.diariokanto.web.dto.UsuarioRegistroDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException; // <--- IMPORTANTE
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException; // <--- IMPORTANTE
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApiAuthenticationProvider implements AuthenticationProvider {

    @Value("${api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public ApiAuthenticationProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        String url = apiUrl + "/usuarios/login";
        UsuarioRegistroDTO loginRequest = new UsuarioRegistroDTO();
        loginRequest.setEmail(username);
        loginRequest.setPassword(password);

        try {
            ResponseEntity<UsuarioDTO> response = restTemplate.postForEntity(url, loginRequest, UsuarioDTO.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                UsuarioDTO usuario = response.getBody();
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()));
                return new UsernamePasswordAuthenticationToken(usuario, password, authorities);
            } else {
                throw new BadCredentialsException("Error desconocido en la respuesta de la API");
            }

        } catch (HttpClientErrorException e) {
            // --- AQUÍ ESTÁ LA SOLUCIÓN ---
            
            // 1. Si la API devuelve 403 Forbidden -> CUENTA NO ACTIVADA
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new DisabledException("Tu cuenta aún no ha sido activada. Por favor, revisa tu correo.");
            }
            
            // 2. Si la API devuelve 401 Unauthorized -> CONTRASEÑA MAL
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BadCredentialsException("Usuario o contraseña incorrectos.");
            }

            // Otros errores
            throw new BadCredentialsException("Error de autenticación: " + e.getMessage());

        } catch (Exception e) {
            throw new AuthenticationServiceException("Error de conexión con el servidor de usuarios.");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}