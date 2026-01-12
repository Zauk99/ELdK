let currentPokemon = null;
let attempts = 0;
const MAX_ATTEMPTS = 5;
const TOTAL_POKEMON = 1010; 

// Al cargar la ventana, simplemente preparamos el foco inicial
window.onload = () => {
    console.log("Diario de Kanto: Minijuego listo.");
};

// Quitar overlay de instrucciones e iniciar
function startGame() {
    const overlay = document.getElementById('instructions-overlay');
    if (overlay) overlay.classList.add('hidden');
    initGame();
}

async function initGame() {
    attempts = 0;
    document.getElementById('tries').innerText = attempts;
    document.getElementById('message').innerText = "Cargando Pokémon...";
    
    const input = document.getElementById('guess-input');
    input.value = "";
    input.disabled = false;
    
    document.getElementById('guess-btn').style.display = "inline-block";
    document.getElementById('reset-btn').style.display = "none";
    
    const imgElement = document.getElementById('pokemon-img');
    imgElement.classList.add('hidden');

    const randomId = Math.floor(Math.random() * TOTAL_POKEMON) + 1;
    
    try {
        const response = await fetch(`https://pokeapi.co/api/v2/pokemon/${randomId}`);
        currentPokemon = await response.json();
        imgElement.src = currentPokemon.sprites.other['official-artwork'].front_default;
        
        imgElement.onload = () => {
            updatePixelation();
            imgElement.classList.remove('hidden');
            document.getElementById('message').innerText = "";
            input.focus(); // El teclado queda listo para escribir
        };
        
    } catch (error) {
        document.getElementById('message').innerText = "Error de conexión.";
    }
}

function updatePixelation() {
    const img = document.getElementById('pokemon-img');
    let level = MAX_ATTEMPTS - attempts;
    if (level < 1) level = 1;
    img.className = `pixel-level-${level}`;
}

function checkGuess() {
    const userInput = document.getElementById('guess-input').value.toLowerCase().trim();
    if (!userInput) return; 

    const pokemonName = currentPokemon.species.name.toLowerCase(); 

    if (userInput === pokemonName) {
        endGame(true);
    } else {
        attempts++;
        document.getElementById('tries').innerText = attempts;
        if (attempts >= MAX_ATTEMPTS) {
            endGame(false);
        } else {
            document.getElementById('message').innerText = "¡No! Intenta otra vez.";
            document.getElementById('guess-input').value = ""; 
            updatePixelation();
        }
    }
}

function endGame(win) {
    const img = document.getElementById('pokemon-img');
    const msg = document.getElementById('message');
    const input = document.getElementById('guess-input');
    
    img.className = "pixel-level-1"; // Revelar Pokémon
    input.disabled = true; // Bloquear teclado para adivinar
    
    document.getElementById('guess-btn').style.display = "none";
    document.getElementById('reset-btn').style.display = "inline-block";

    const displayName = currentPokemon.species.name.toUpperCase();

    if(win) {
        msg.innerHTML = `<span style="color: green;">¡Correcto! Es <strong>${displayName}</strong></span>`;
    } else {
        msg.innerHTML = `<span style="color: red;">Se acabaron los intentos. Era <strong>${displayName}</strong></span>`;
    }
}

/**
 * GESTOR ÚNICO DE TECLADO
 * Centraliza el uso de la tecla "Enter"
 */
window.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
        const instructions = document.getElementById('instructions-overlay');
        const resetBtn = document.getElementById('reset-btn');
        const input = document.getElementById('guess-input');

        // 1. Si las instrucciones están abiertas, ENTER las cierra
        if (instructions && !instructions.classList.contains('hidden')) {
            startGame();
            return;
        }

        // 2. Si el juego ha terminado (botón reset visible), ENTER reinicia
        if (resetBtn && resetBtn.style.display !== 'none') {
            initGame();
            return;
        }

        // 3. Si el juego está activo y el usuario está escribiendo, ENTER comprueba nombre
        if (input && !input.disabled && document.activeElement === input) {
            checkGuess();
        }
    }
});