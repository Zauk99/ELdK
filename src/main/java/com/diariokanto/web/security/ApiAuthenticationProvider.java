package com.diariokanto.web.security;

import com.diariokanto.web.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
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

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UsuarioDTO usuario = response.getBody(); // <--- ESTA ES LA VARIABLE 'usuario'

                // --- LÓGICA DE 2FA ---
                if (usuario.isTwoFactorEnabled()) {
                    // Si tiene 2FA activado, NO le damos su rol real todavía.
                    // Le damos un rol temporal "PRE_AUTH" para que solo pueda ir a la pantalla de código.
                    List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_PRE_AUTH"));
                    
                    System.out.println(">>> LOGIN 2FA REQUERIDO: Usuario " + usuario.getEmail());
                    
                    return new UsernamePasswordAuthenticationToken(usuario, password, authorities);

                } else {
                    // Si NO tiene 2FA, login normal con su rol (ADMIN o USER)
                    List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()));
                    
                    System.out.println(">>> LOGIN ÉXITO: Usuario " + usuario.getEmail());
                    
                    return new UsernamePasswordAuthenticationToken(usuario, password, authorities);
                }
                // ---------------------
            }

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new BadCredentialsException("Credenciales incorrectas");
        } catch (Exception e) {
            throw new BadCredentialsException("Error de conexión con el servidor");
        }

        throw new BadCredentialsException("Error desconocido de autenticación");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}