package com.diariokanto.web.controller;

import com.diariokanto.web.dto.UsuarioDTO;
import com.diariokanto.web.dto.UsuarioRegistroDTO;
import com.diariokanto.web.service.PokemonService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Controller
public class AuthController {

    @Autowired
    private PokemonService pokemonService;

    @Value("${api.url}") // Lee 'http://localhost:8080/api' del properties
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();


    @GetMapping("/login")
    public String login(Model model) {
        // 1. Necesario para el autocompletado de Pokémon
        model.addAttribute("listaPokemon", pokemonService.getTodosLosNombres());

        // 2. ¡IMPORTANTE! Necesario para el th:object="${usuario}" del formulario de
        // registro
        // Si no añades esto, la página explota porque no encuentra la variable
        // "usuario"
        model.addAttribute("usuario", new UsuarioRegistroDTO());

        return "login";
    }

    // En AuthController.java

    @GetMapping("/login-2fa")
    public String vista2FA() {
        return "login-2fa"; // Crea este HTML con un input simple
    }

    @PostMapping("/verificar-2fa")
    public String procesar2FA(@RequestParam int codigo, Authentication auth, Model model) {
        UsuarioDTO user = (UsuarioDTO) auth.getPrincipal();
        
        try {
            String url = apiUrl + "/2fa/validar?userId=" + user.getId() + "&code=" + codigo;
            
            // Si la API devuelve 200 OK, pasa. Si devuelve 401 o 403, salta al catch.
            restTemplate.postForEntity(url, null, String.class);
            
            // --- ÉXITO ---
            List<GrantedAuthority> nuevosRoles = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol()));
            Authentication nuevaAuth = new UsernamePasswordAuthenticationToken(user, auth.getCredentials(), nuevosRoles);
            SecurityContextHolder.getContext().setAuthentication(nuevaAuth);
            
            return "redirect:/";

        } catch (HttpClientErrorException e) {
            // --- GESTIÓN DE ERRORES ---
            
            // Si el error es 403 (FORBIDDEN), significa que está BLOQUEADO
            if (e.getStatusCode().value() == 403) {
                model.addAttribute("bloqueado", true); // <--- ESTA ES LA CLAVE
                model.addAttribute("error", "⛔ CUENTA BLOQUEADA TEMPORALMENTE");
                model.addAttribute("mensajeDetalle", "Has superado los 5 intentos permitidos. Por seguridad, espera 1 minuto antes de volver a intentarlo.");
            } else {
                // Si es 401 (UNAUTHORIZED), es solo código mal
                model.addAttribute("error", "Código incorrecto. Inténtalo de nuevo.");
            }
            
            return "login-2fa";
        } catch (Exception e) {
            model.addAttribute("error", "Error de conexión con el servidor.");
            return "login-2fa";
        }
    }
}