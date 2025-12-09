package com.diariokanto.web.controller;

import com.diariokanto.web.dto.UsuarioDTO;
import com.diariokanto.web.service.ComentarioService;
import com.diariokanto.web.service.NoticiaService;
import com.diariokanto.web.dto.NoticiaDTO;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class HomeController {

    @Autowired
    private NoticiaService noticiaService;

    // En HomeController.java
    @GetMapping("/login")
    public String login() {
        return "login"; // Busca el archivo templates/login.html
    }

    // Inyecta el servicio nuevo
    @Autowired
    private ComentarioService comentarioService;

    // MODIFICAR: verNoticia para que cargue los comentarios
    @GetMapping("/noticia/{id}")
    public String verNoticia(@PathVariable Long id, Model model) {
        var noticia = noticiaService.obtenerPorId(id);
        if (noticia == null)
            return "redirect:/";

        // Cargar comentarios
        var comentarios = comentarioService.obtenerPorNoticia(id);

        model.addAttribute("noticia", noticia);
        model.addAttribute("comentarios", comentarios); // <--- NUEVO
        return "noticia-completa";
    }

    @PostMapping("/noticia/{id}/comentar")
    public String publicarComentario(@PathVariable Long id, 
                                     @RequestParam String texto, 
                                     Authentication authentication) { // 1. Usamos Authentication
        
        if (authentication != null && authentication.isAuthenticated()) {
            String email;
            Object principal = authentication.getPrincipal();

            // 2. Comprobamos qué hay guardado en la sesión
            if (principal instanceof UsuarioDTO) {
                // Si es el objeto complejo, sacamos el email limpio
                email = ((UsuarioDTO) principal).getEmail();
            } else {
                // Si es solo texto (por si acaso), lo usamos tal cual
                email = principal.toString();
            }

            // 3. Enviamos el email correcto
            comentarioService.enviarComentario(id, email, texto);
        }
        return "redirect:/noticia/" + id;
    }

    // ... imports ...

    @PostMapping("/comentarios/eliminar/{id}")
    public String eliminarComentario(@PathVariable Long id, @RequestHeader(value = "Referer", required = false) String referer) {
        // Llamamos al servicio de comentarios que ya tenías
        comentarioService.eliminarComentario(id);
        // Volvemos a la página desde donde se pulsó el botón
        return "redirect:" + (referer != null ? referer : "/");
    }

    // 1. Modificar el index para filtrar si pulsas Enter
    @GetMapping("/")
    public String index(@RequestParam(required = false) String busqueda, Model model) {
        var noticias = noticiaService.obtenerNoticias(busqueda);
        model.addAttribute("listaNoticias", noticias);
        model.addAttribute("busquedaActual", busqueda); // Para mantener el texto en el input
        return "index";
    }

    // 2. Nuevo endpoint para el AJAX del Dropdown (devuelve JSON, no HTML)
    // 👇👇 AQUÍ ESTABA EL ERROR 👇👇
    @GetMapping("/api/busqueda-noticias")
    @ResponseBody
    public List<NoticiaDTO> buscarNoticiasApi(@RequestParam String query) { 
        // Antes ponía: List<NoticiaService.NoticiaDTO> -> ESO ESTÁ MAL AHORA
        // Ahora debe ser: List<NoticiaDTO>
        return noticiaService.obtenerNoticias(query);
    }
}