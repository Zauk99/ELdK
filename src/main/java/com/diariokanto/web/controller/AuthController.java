package com.diariokanto.web.controller;

import com.diariokanto.web.dto.UsuarioDTO;
import com.diariokanto.web.dto.UsuarioRegistroDTO;
import com.diariokanto.web.service.PokemonService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
public class AuthController {

    @Autowired
    private PokemonService pokemonService;

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
    public String procesar2FA(@RequestParam int codigo, Authentication auth) {
        UsuarioDTO user = (UsuarioDTO) auth.getPrincipal();

        // Llamar a API para validar código
        // OJO: Necesitas un endpoint en API que valide usando ID de usuario y código
        // Si es válido:

        // Actualizamos el rol manualmente a USER o ADMIN
        List<GrantedAuthority> nuevosRoles = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol()));
        Authentication nuevaAuth = new UsernamePasswordAuthenticationToken(user, auth.getCredentials(), nuevosRoles);
        SecurityContextHolder.getContext().setAuthentication(nuevaAuth);

        return "redirect:/";
    }
}