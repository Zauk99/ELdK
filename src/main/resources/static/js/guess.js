let currentPokemon = null;
let attempts = 0;
const MAX_ATTEMPTS = 5;
// Actualmente hay 1025 Pokémon, pero los últimos a veces no tienen artwork oficial. 
// 1010 es un número seguro para la mayoría de los artworks.
const TOTAL_POKEMON = 1010; 

window.onload = initGame;

async function initGame() {
    // 1. Limpiar UI antes de mostrar nada nuevo
    attempts = 0;
    document.getElementById('tries').innerText = attempts;
    document.getElementById('message').innerText = "Cargando Pokémon...";
    document.getElementById('guess-input').value = "";
    document.getElementById('guess-input').disabled = false;
    
    // Gestión de botones
    document.getElementById('guess-btn').style.display = "inline-block";
    document.getElementById('reset-btn').style.display = "none";
    
    const imgElement = document.getElementById('pokemon-img');
    imgElement.classList.add('hidden'); // Lo ocultamos totalmente mientras carga

    // 2. Elegir ID aleatorio de todas las generaciones
    const randomId = Math.floor(Math.random() * TOTAL_POKEMON) + 1;
    
    try {
        const response = await fetch(`https://pokeapi.co/api/v2/pokemon/${randomId}`);
        currentPokemon = await response.json();
        
        // 3. Preparar imagen
        imgElement.src = currentPokemon.sprites.other['official-artwork'].front_default;
        
        // Esperamos a que la imagen cargue internamente antes de mostrarla pixelada
        imgElement.onload = () => {
            updatePixelation();
            imgElement.classList.remove('hidden');
            document.getElementById('message').innerText = "";
            document.getElementById('guess-input').focus(); // Poner el cursor listo para escribir
        };
        
    } catch (error) {
        document.getElementById('message').innerText = "Error de conexión. Reintenta.";
        console.error(error);
    }
}

function updatePixelation() {
    const img = document.getElementById('pokemon-img');
    // Calculamos nivel: 5 (max pixelado) a 1 (limpio)
    let level = MAX_ATTEMPTS - attempts;
    if (level < 1) level = 1;
    img.className = `pixel-level-${level}`;
}

function checkGuess() {
    const userInput = document.getElementById('guess-input').value.toLowerCase().trim();
    if (!userInput) return; // No contar intentos si el input está vacío

    const pokemonName = currentPokemon.name.toLowerCase();

    if (userInput === pokemonName) {
        endGame(true);
    } else {
        attempts++;
        document.getElementById('tries').innerText = attempts;
        
        if (attempts >= MAX_ATTEMPTS) {
            endGame(false);
        } else {
            document.getElementById('message').innerText = "¡No! Intenta otra vez.";
            document.getElementById('guess-input').value = ""; // Limpiar para el siguiente intento
            updatePixelation();
        }
    }
}

function endGame(win) {
    const img = document.getElementById('pokemon-img');
    const msg = document.getElementById('message');
    const input = document.getElementById('guess-input');
    
    img.className = "pixel-level-1"; // Revelar totalmente
    input.disabled = true; // Bloquear escritura
    
    // Ocultar botón adivinar y mostrar reiniciar
    document.getElementById('guess-btn').style.display = "none";
    document.getElementById('reset-btn').style.display = "inline-block";

    if(win) {
        msg.innerHTML = `<span style="color: green;">¡Correcto! Es <strong>${currentPokemon.name.toUpperCase()}</strong></span>`;
    } else {
        msg.innerHTML = `<span style="color: red;">Se acabaron los intentos. Era <strong>${currentPokemon.name.toUpperCase()}</strong></span>`;
    }
}