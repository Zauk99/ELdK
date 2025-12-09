package com.diariokanto.web.controller;

import com.diariokanto.web.dto.NoticiaDTO;
import com.diariokanto.web.service.NoticiaService;
import com.diariokanto.web.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // ... imports ...
    @Autowired
    private UsuarioService usuarioService; // Asegúrate de inyectarlo

    @Autowired
    private NoticiaService noticiaService;

    // Mostrar el formulario
    @GetMapping("/crear-noticia")
    public String mostrarFormulario(Model model) {
        model.addAttribute("noticia", new NoticiaDTO());
        return "formulario-noticia"; // Plantilla HTML
    }

    // Procesar el formulario
    @PostMapping("/guardar-noticia")
    public String guardarNoticia(@ModelAttribute NoticiaDTO noticia,
            @RequestParam("ficheroImagen") MultipartFile fichero) {
        // Llamamos al nuevo método multipart
        noticiaService.guardarNoticiaMultipart(noticia, fichero);
        return "redirect:/";
    }

    // ... imports ...

    // 1. Mostrar formulario de EDICIÓN (Carga datos existentes)
    @GetMapping("/editar/{id}")
    public String editarNoticia(@PathVariable Long id, Model model) {
        // Reutilizamos el servicio de lectura para obtener los datos
        NoticiaDTO noticia = noticiaService.obtenerPorId(id);
        model.addAttribute("noticia", noticia);
        model.addAttribute("esEdicion", true); // Flag para la vista
        return "formulario-noticia";
    }

    // 2. Procesar la actualización
    @PostMapping("/actualizar")
    public String actualizarNoticia(@ModelAttribute NoticiaDTO noticia,
            @RequestParam("ficheroImagen") MultipartFile fichero) {
        noticiaService.actualizarNoticiaMultipart(noticia.getId(), noticia, fichero);
        return "redirect:/noticia/" + noticia.getId();
    }

    // 3. Eliminar Noticia
    @PostMapping("/eliminar/{id}")
    public String eliminarNoticia(@PathVariable Long id) {
        noticiaService.eliminarNoticia(id);
        return "redirect:/";
    }

    // 1. Ver la lista de usuarios
    @GetMapping("/usuarios")
    public String panelUsuarios(Model model) {
        model.addAttribute("listaUsuarios", usuarioService.listarTodos());
        return "admin-usuarios";
    }

    // 2. Procesar el cambio de rol
    @PostMapping("/usuarios/cambiar-rol")
    public String cambiarRol(@RequestParam Long id, @RequestParam String nuevoRol) {
        try {
            usuarioService.cambiarRol(id, nuevoRol);
            return "redirect:/admin/usuarios?exito=true";
        } catch (Exception e) {
            // Si intenta degradar al admin principal, saldrá este error
            return "redirect:/admin/usuarios?error=No se pudo cambiar el rol";
        }
    }
}