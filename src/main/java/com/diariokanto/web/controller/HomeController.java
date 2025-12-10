package com.diariokanto.web.controller;

import com.diariokanto.web.dto.UsuarioDTO;
import com.diariokanto.web.service.CategoriaService;
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
    private CategoriaService categoriaService; // <--- ¡AQUÍ ESTÁ LA CLAVE
    // !
    @Autowired
    private NoticiaService noticiaService;

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
    public String eliminarComentario(@PathVariable Long id,
            @RequestHeader(value = "Referer", required = false) String referer) {
        // Llamamos al servicio de comentarios que ya tenías
        comentarioService.eliminarComentario(id);
        // Volvemos a la página desde donde se pulsó el botón
        return "redirect:" + (referer != null ? referer : "/");
    }

    // 1. Modificar el index para filtrar si pulsas Enter
    @GetMapping("/")
    public String index(Model model,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String categoria) {

        List<NoticiaDTO> noticias;

        // Lógica de filtrado
        if (busqueda != null && !busqueda.isEmpty()) {
            noticias = noticiaService.obtenerNoticias(busqueda);
            model.addAttribute("busquedaActual", busqueda);
        } else if (categoria != null && !categoria.isEmpty()) {
            // Reutilizamos el método de búsqueda de noticias, ya que la API ahora
            // recibe ?categoria=... en el mismo endpoint /api/noticias
            // (Nota: Asegúrate de que tu NoticiaService.obtenerNoticias o similar
            // pueda mandar el parámetro 'categoria' a la API, o crea uno específico)
            noticias = noticiaService.buscarPorCategoria(categoria);
            model.addAttribute("categoriaActual", categoria);
        } else {
            noticias = noticiaService.obtenerNoticias();
        }

        model.addAttribute("listaNoticias", noticias);

        // Pasamos las categorías para el menú desplegable
        model.addAttribute("listaCategorias", categoriaService.listarTodas());

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