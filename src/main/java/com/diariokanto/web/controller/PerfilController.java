package com.diariokanto.web.controller;

import com.diariokanto.web.dto.UsuarioDTO;
import com.diariokanto.web.service.PokemonService;
import com.diariokanto.web.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private PokemonService pokemonService;

    // 1. VISTA DE LECTURA (Sidebar + Datos)
    @GetMapping
    public String verPerfil(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return "redirect:/login";

        UsuarioDTO usuario = (UsuarioDTO) auth.getPrincipal();
        model.addAttribute("usuario", usuario);
        // No necesitamos la lista de pokémon aquí, solo en la edición
        
        return "perfil"; // Apunta al NUEVO html
    }

    // 2. VISTA DE EDICIÓN (El formulario antiguo)
    @GetMapping("/editar")
    public String editarPerfil(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return "redirect:/login";

        UsuarioDTO usuario = (UsuarioDTO) auth.getPrincipal();
        model.addAttribute("usuario", usuario);
        model.addAttribute("listaPokemon", pokemonService.getTodosLosNombres());
        
        return "form-perfil"; // Apunta al archivo RENOMBRADO
    }

    // 3. GUARDAR (Recibe los datos del formulario)
    @PostMapping("/guardar")
    public String guardarPerfil(@ModelAttribute UsuarioDTO usuarioForm,
                                @RequestParam(required = false) MultipartFile ficheroFoto,
                                Authentication auth) {
        try {
            UsuarioDTO usuarioSesion = (UsuarioDTO) auth.getPrincipal();
            UsuarioDTO usuarioActualizado = usuarioService.actualizar(
                usuarioSesion.getId(), usuarioForm, ficheroFoto
            );

            // Actualizar sesión
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    usuarioActualizado, auth.getCredentials(), auth.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            // Redirigimos a la vista de lectura con mensaje
            return "redirect:/perfil?exito=true"; 
            
        } catch (Exception e) {
            String mensaje = e.getMessage();
            if (mensaje != null && mensaje.contains("nombre de usuario")) {
                return "redirect:/perfil/editar?errorField=username"; // Volver a editar si falla
            }
            return "redirect:/perfil/editar?error=" + mensaje;
        }
    }

    // --- NUEVA GESTIÓN DE CONTRASEÑA ---

    @GetMapping("/cambiar-password")
    public String vistaCambiarPassword() {
        return "cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String procesarCambioPassword(@RequestParam String pass1, 
                                         @RequestParam String pass2,
                                         Authentication auth) {
        
        // 1. Verificación de igualdad (Input rojo si falla)
        if (!pass1.equals(pass2)) {
            return "redirect:/perfil/cambiar-password?errorField=mismatch";
        }
        
        try {
            UsuarioDTO usuario = (UsuarioDTO) auth.getPrincipal();
            usuarioService.cambiarPassword(usuario.getId(), pass1);
            return "redirect:/perfil?passExito=true";
        } catch (Exception e) {
            return "redirect:/perfil/cambiar-password?error=Error general";
        }
    }
}