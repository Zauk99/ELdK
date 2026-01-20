document.addEventListener('DOMContentLoaded', function () {
    // === LÓGICA DEL MENÚ DE USUARIO (DROPDOWN) ===
    const menuBtn = document.getElementById('userMenuBtn');
    const dropdown = document.getElementById('userDropdown');

    if (menuBtn && dropdown) {
        menuBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            dropdown.classList.toggle('show');
        });

        document.addEventListener('click', function (e) {
            if (!dropdown.contains(e.target) && !menuBtn.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });
    }
});

// === LÓGICA DEL MODAL DE BORRADO SEGURO ===

// Palabra que el usuario debe escribir para desbloquear
const PALABRA_CLAVE = "CONFIRMAR";

function abrirModalBorrar(urlAccion) {
    // Buscamos los elementos cada vez para evitar errores de variables no definidas
    const modal = document.getElementById('modalBorrar');
    const form = document.getElementById('formBorrarModal');
    const input = document.getElementById('inputConfirmacion');
    const btn = document.getElementById('btnBorrarReal');

    if (!modal || !form) {
        console.error("Error: No se encuentra el modal en el HTML");
        return;
    }
    
    // 1. Asignamos la URL al formulario del modal
    form.action = urlAccion;
    
    // 2. Reseteamos el estado (limpiar input y bloquear botón)
    input.value = "";
    input.classList.remove('valido');
    btn.disabled = true;
    
    // 3. Mostramos el modal
    modal.classList.add('show');
    
    // 4. Ponemos el foco en el input para escribir directo
    setTimeout(() => input.focus(), 100);
}

function cerrarModalBorrar() {
    const modal = document.getElementById('modalBorrar');
    if (modal) {
        modal.classList.remove('show');
    }
}

function validarBorrado() {
    const input = document.getElementById('inputConfirmacion');
    const btn = document.getElementById('btnBorrarReal');
    
    if (!input || !btn) return;

    // Comprobamos si coincide con la palabra clave (ignorando mayúsculas/minúsculas)
    if (input.value.toUpperCase() === PALABRA_CLAVE) {
        btn.disabled = false;
        input.classList.add('valido'); // Pone el borde verde
    } else {
        btn.disabled = true;
        input.classList.remove('valido');
    }
}

// Cerrar si se hace clic fuera de la caja negra (en el fondo oscuro)
window.addEventListener('click', function(e) {
    const modal = document.getElementById('modalBorrar');
    if (e.target === modal) {
        cerrarModalBorrar();
    }
});