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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        try {
            // A. Registrar (La API guarda la foto y envía el email)
            usuarioService.registrar(registroDTO, ficheroFoto);

            // --- AUTO-LOGIN ELIMINADO ---
            // No podemos iniciar sesión automáticamente porque la cuenta
            // aún no ha sido activada por correo.

            // B. Mensaje de éxito
            redirectAttributes.addFlashAttribute("mensaje", 
                "¡Registro completado! Revisa tu correo para activar la cuenta antes de entrar.");

            // C. Redirigir al LOGIN
            // Te redirige al formulario de inicio de sesión para que esperes el correo.
            return "redirect:/login";

        } catch (Exception e) {
            // D. Si falla (ej: email duplicado), recargamos el formulario
            model.addAttribute("error", "Error en el registro: " + e.getMessage());
            model.addAttribute("usuario", registroDTO);
            model.addAttribute("listaPokemon", pokemonService.getTodosLosNombres());
            
            // IMPORTANTE: Asegúrate de que esto devuelve la vista donde está tu formulario.
            // Si tu formulario de registro está en login.html, deja "login".
            // Si está en registro.html, pon "registro".
            return "login"; 
        }
    }
}