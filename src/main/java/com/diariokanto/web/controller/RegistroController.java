package com.diariokanto.web.controller;

import com.diariokanto.web.dto.UsuarioRegistroDTO;
import com.diariokanto.web.security.ApiAuthenticationProvider;
import com.diariokanto.web.service.PokemonService;
import com.diariokanto.web.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class RegistroController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private PokemonService pokemonService;
    @Autowired private ApiAuthenticationProvider authenticationProvider; // Para el Auto-Login

    // 1. Mostrar el formulario (GET) con la lista de Pokémon
    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new UsuarioRegistroDTO());
        model.addAttribute("listaPokemon", pokemonService.getTodosLosNombres());
        return "login";
    }

    // 2. Procesar el registro (POST)
    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute("usuario") UsuarioRegistroDTO registroDTO,
                                   @RequestParam(value = "ficheroFoto", required = false) MultipartFile ficheroFoto,
                                   HttpServletRequest request,
                                   Model model) {
        try {
            // A. Registrar (La API guarda la foto)
            usuarioService.registrar(registroDTO, ficheroFoto);

            // B. Auto-Login (Magia para iniciar sesión solo)
            UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(
                    registroDTO.getEmail(), registroDTO.getPassword());
            Authentication auth = authenticationProvider.authenticate(authReq);
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            // C. Redirigir al inicio
            return "redirect:/";

        } catch (Exception e) {
            // D. Si falla, recargamos el formulario Y LA LISTA DE POKEMON (Muy importante)
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("usuario", registroDTO);
            model.addAttribute("listaPokemon", pokemonService.getTodosLosNombres());
            return "login";
        }
    }
}