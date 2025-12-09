document.addEventListener('DOMContentLoaded', function () {
    const menuBtn = document.getElementById('userMenuBtn');
    const dropdown = document.getElementById('userDropdown');

    if (menuBtn && dropdown) {
        // 1. Al hacer clic en el botón, alternar la clase 'show'
        menuBtn.addEventListener('click', function (e) {
            e.stopPropagation(); // Evita que el clic llegue al document y lo cierre inmediatamente
            dropdown.classList.toggle('show');
        });

        // 2. Al hacer clic en cualquier otro sitio, cerrar el menú
        document.addEventListener('click', function (e) {
            if (!dropdown.contains(e.target) && !menuBtn.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });
    }
});

