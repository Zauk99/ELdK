package com.diariokanto.web.controller;

import com.diariokanto.web.dto.NoticiaDTO;
import com.diariokanto.web.dto.UsuarioDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // ... imports ...
    @Autowired
    private UsuarioService usuarioService; // Asegúrate de inyectarlo

    @Autowired
    private NoticiaService noticiaService;

    // Constante para el límite: 2MB en bytes
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    // Mostrar el formulario
    @GetMapping("/crear-noticia")
    public String mostrarFormulario(Model model) {
        model.addAttribute("noticia", new NoticiaDTO());
        return "formulario-noticia"; // Plantilla HTML
    }

    // Procesar el formulario
    @PostMapping("/guardar-noticia")
    public String guardarNoticia(@ModelAttribute NoticiaDTO noticia,
            @RequestParam("ficheroImagen") MultipartFile fichero,
            RedirectAttributes redirectAttributes, Model model) { // <--- Añadir Model

        // --- VALIDACIÓN TAMAÑO (CREAR) ---
        if (!fichero.isEmpty() && fichero.getSize() > MAX_FILE_SIZE) {
            model.addAttribute("errorImagen", "La imagen supera el tamaño máximo permitido (2MB).");
            model.addAttribute("noticia", noticia); // Mantenemos lo que escribió el usuario
            return "formulario-noticia"; // Volvemos a la vista sin guardar
        }
        // ---------------------------------

        noticiaService.guardarNoticiaMultipart(noticia, fichero);

        // AÑADIMOS EL MENSAJE
        redirectAttributes.addFlashAttribute("mensajeAdmin", "Noticia creada con éxito.");
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
            @RequestParam("ficheroImagen") MultipartFile fichero,
            RedirectAttributes redirectAttributes) {
        noticiaService.actualizarNoticiaMultipart(noticia.getId(), noticia, fichero);
        
        // AÑADIMOS EL MENSAJE
        redirectAttributes.addFlashAttribute("mensajeAdmin", "Noticia actualizada correctamente.");
        return "redirect:/";
    }

    // 3. Eliminar Noticia
    @PostMapping("/eliminar/{id}")
    public String eliminarNoticia(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        noticiaService.eliminarNoticia(id);
        // AÑADIMOS EL MENSAJE
        redirectAttributes.addFlashAttribute("mensajeAdmin", "Noticia eliminada con éxito.");
        return "redirect:/";
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

    // En src/main/java/com/diariokanto/web/controller/AdminController.java
    @GetMapping("/usuarios")
    public String listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model) {

        // Llamamos al servicio (que llamará a la API)
        // Debes adaptar tu UsuarioService.java para que acepte estos parámetros
        Page<UsuarioDTO> paginaUsuarios = usuarioService.obtenerUsuariosPaginados(buscar, page, size, sort, dir);

        model.addAttribute("usuarios", paginaUsuarios.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaUsuarios.getTotalPages());
        model.addAttribute("totalItems", paginaUsuarios.getTotalElements());
        model.addAttribute("buscar", buscar);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("reverseDir", dir.equals("asc") ? "desc" : "asc");

        return "admin-usuarios";
    }

    // En AdminController.java

    @PostMapping("/usuarios/borrar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            // Capturamos el error (por ejemplo, si es el último admin)
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}