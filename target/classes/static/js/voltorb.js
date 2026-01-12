/**
 * VOLTORB FLIP - SCRIPT FINAL
 * Características: 
 * - Toolbar y Modos.
 * - Imágenes personalizadas.
 * - Tiempos: 2s Mensaje / 5s Reinicio.
 * - Lógica: Baja nivel al perder (min 1), Mantiene nivel al ganar si es el Máximo (5).
 */

// --- CONFIGURACIÓN Y ESTADO ---
const BOARD_SIZE = 5;
const MAX_LEVEL = 5; // Definimos el tope de niveles

let board = [];
let revealedCount = 0;
let totalWinningTiles = 0;
let currentLevel = 1;
let currentMode = 'flip';
// Variable preparada para tu futuro sistema de puntuación (aún no se muestra)
let currentScore = 0; 

// Configuración de dificultad
const LEVEL_CONFIGS = {
    1: { voltorbs: 6, twos: 3, threes: 1 },
    2: { voltorbs: 7, twos: 4, threes: 2 },
    3: { voltorbs: 7, twos: 5, threes: 3 },
    4: { voltorbs: 8, twos: 5, threes: 4 },
    5: { voltorbs: 8, twos: 5, threes: 5 }
};

function initGame() {
    board = [];
    revealedCount = 0;
    totalWinningTiles = 0;
    
    const boardElement = document.getElementById('board');
    const overlay = document.getElementById('level-overlay');

    boardElement.innerHTML = ''; 
    if (overlay) overlay.classList.add('hidden');
    document.getElementById('status').innerText = `Nivel ${currentLevel}`;
    
    setMode('flip');

    // Usamos la configuración del nivel actual
    const config = LEVEL_CONFIGS[currentLevel];
    const totalCells = BOARD_SIZE * BOARD_SIZE;
    
    let values = [];
    for (let i = 0; i < config.voltorbs; i++) values.push(0);
    for (let i = 0; i < config.twos; i++) values.push(2);
    for (let i = 0; i < config.threes; i++) values.push(3);
    while (values.length < totalCells) values.push(1);

    // Mezclar casillas
    for (let i = values.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [values[i], values[j]] = [values[j], values[i]];
    }

    // Rellenar matriz
    for (let r = 0; r < BOARD_SIZE; r++) {
        board[r] = [];
        for (let c = 0; c < BOARD_SIZE; c++) {
            let val = values.pop();
            board[r][c] = val;
            if (val > 1) totalWinningTiles++;
        }
    }
    renderBoard();
}

function renderBoard() {
    const boardElement = document.getElementById('board');
    for (let r = 0; r < BOARD_SIZE; r++) {
        for (let c = 0; c < BOARD_SIZE; c++) {
            const cell = document.createElement('div');
            cell.classList.add('cell');
            cell.dataset.row = r;
            cell.dataset.col = c;
            cell.onclick = () => handleCellInteraction(r, c, cell);
            cell.oncontextmenu = (e) => e.preventDefault();
            boardElement.appendChild(cell);
        }
        boardElement.appendChild(createHint(r, 'row'));
    }
    for (let c = 0; c < BOARD_SIZE; c++) {
        boardElement.appendChild(createHint(c, 'col'));
    }
}

function handleCellInteraction(r, c, element) {
    if (element.classList.contains('revealed')) return;

    if (currentMode === 'flip') {
        revealCell(r, c, element);
    } else {
        const markIcons = { 'mark-0': '💣', 'mark-1': '1', 'mark-2': '2', 'mark-3': '3' };
        const icon = markIcons[currentMode];
        if (element.dataset.mark === icon) {
            delete element.dataset.mark;
        } else {
            element.dataset.mark = icon;
        }
    }
}

function revealCell(r, c, element) {
    const val = board[r][c];
    delete element.dataset.mark;
    element.classList.add('revealed');

    if (val === 0) {
        // --- DERROTA ---
        element.classList.add('voltorb');
        document.getElementById('status').innerText = "¡BOOM! Has perdido.";
        
        disableBoard();
        revealAllTiles(); 

        // Si es mayor que 1, bajamos. Si es 1, se queda en 1.
        if (currentLevel > 1) {
            currentLevel--;
        }

        setTimeout(initGame, 3000); 

    } else {
        // --- ACIERTO ---
        element.innerText = val;
        if (val > 1) revealedCount++;
        
        if (revealedCount === totalWinningTiles) {
            // --- VICTORIA ---
            const overlay = document.getElementById('level-overlay');
            if (overlay) overlay.classList.remove('hidden');
            
            disableBoard();
            revealAllTiles();
            
            // --- NUEVA LÓGICA DE PROGRESIÓN ---
            if (currentLevel < MAX_LEVEL) {
                currentLevel++; // Subimos si no es el máximo
            }
            // Si currentLevel ya es 5, no hacemos nada (se repite el 5)

            // Mensaje durante 2 segundos
            setTimeout(() => {
                if (overlay) overlay.classList.add('hidden');
                document.getElementById('status').innerText = "¡Nivel completado! Preparando el siguiente...";
            }, 2000);

            // Reinicio total a los 5 segundos
            setTimeout(initGame, 5000);
        }
    }
}

function revealAllTiles() {
    const cells = document.querySelectorAll('.cell[data-row]');
    cells.forEach(cell => {
        const r = parseInt(cell.dataset.row);
        const c = parseInt(cell.dataset.col);
        const val = board[r][c];
        if (!cell.classList.contains('revealed')) {
            cell.classList.add('revealed');
            cell.style.opacity = "0.7"; 
            if (val === 0) {
                cell.classList.add('voltorb');
                cell.innerText = "";
            } else {
                cell.innerText = val;
            }
            delete cell.dataset.mark;
        }
    });
}

function setMode(mode) {
    currentMode = mode;
    document.querySelectorAll('.tool').forEach(t => t.classList.remove('active'));
    let buttonId = mode === 'flip' ? 'tool-flip' : `tool-${mode.replace('mark-', '')}`;
    const activeTool = document.getElementById(buttonId);
    if (activeTool) activeTool.classList.add('active');
}

function createHint(index, type) {
    let sum = 0, voltorbs = 0;
    for (let i = 0; i < BOARD_SIZE; i++) {
        let val = (type === 'row') ? board[index][i] : board[i][index];
        if (val === 0) voltorbs++;
        else sum += val;
    }
    const hint = document.createElement('div');
    hint.classList.add('cell', 'hint');
    hint.innerHTML = `<span>${sum}</span><hr><span style="display:flex;align-items:center;gap:3px;">
        <img src="/img/voltorb.png" style="width:14px;">${voltorbs}
    </span>`;
    return hint;
}

function disableBoard() {
    const cells = document.querySelectorAll('.cell');
    cells.forEach(c => c.onclick = null);
}

window.onload = initGame;