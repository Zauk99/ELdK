package com.diariokanto.web.controller;

import com.diariokanto.web.dto.UsuarioRegistroDTO;
import com.diariokanto.web.security.ApiAuthenticationProvider;
import com.diariokanto.web.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ApiAuthenticationProvider authenticationProvider; // Inyectamos nuestro validador

    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute UsuarioRegistroDTO registroDTO,
            @RequestParam(value = "ficheroFoto", required = false) MultipartFile foto,
            HttpServletRequest request) {

        // --- CHIVATO DE DEBUG ---
        System.out.println(">>> FRONTEND CONTROLLER: Intento de registro.");
        if (foto == null) {
            System.out.println(">>> FRONTEND: La variable 'foto' es NULL. (Revisar name='ficheroFoto' en HTML)");
        } else if (foto.isEmpty()) {
            System.out.println(">>> FRONTEND: La variable 'foto' está VACÍA. (Revisar enctype en HTML)");
        } else {
            System.out.println(">>> FRONTEND: ¡Foto recibida! Nombre: " + foto.getOriginalFilename() + " Tamaño: "
                    + foto.getSize());
        }
        // -------------------------

        try {
            usuarioService.registrar(registroDTO, foto);

            // ... resto del código (auto-login, etc.) ...

            // 2. AUTO-LOGIN: Si no ha fallado lo anterior, iniciamos sesión automáticamente

            // Creamos un token de intento de login con los datos que tenemos en la mano
            // Usamos el email (o username) y la contraseña SIN encriptar
            UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(
                    registroDTO.getEmail(), registroDTO.getPassword());

            // Validamos contra la API (esto nos devolverá el UsuarioDTO con rol y todo)
            Authentication auth = authenticationProvider.authenticate(authReq);

            // Establecemos la autenticación en el contexto de seguridad de Spring
            SecurityContextHolder.getContext().setAuthentication(auth);

            // IMPORTANTE: Guardamos el contexto en la sesión HTTP para que se mantenga al
            // navegar
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            System.out.println(">>> INTENTO DE REGISTRO RECIBIDO: " + registroDTO.getEmail());

            // 3. Redirigimos a la portada con la sesión ya iniciada
            return "redirect:/";

        } catch (Exception e) {
            System.err.println("ERROR EN REGISTRO: " + e.getMessage());
            e.printStackTrace(); // Esto nos dará todos los detalles

            return "redirect:/login?error=" + e.getMessage();
        }
    }
}