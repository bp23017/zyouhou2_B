/**
 * game.js (4人対戦・同期対応版)
 */
let myPlayerId = new URLSearchParams(window.location.search).get('playerId');
let roomId = new URLSearchParams(window.location.search).get('roomId');
let myColor = new URLSearchParams(window.location.search).get('color');
let localPlayerPositions = {};

console.log("取得した自分のID:", myPlayerId);// 前回の位置を記録して、移動を検知する

document.addEventListener('DOMContentLoaded', async () => {
    const diceBtn = document.getElementById('diceStart');
    const eventMessage = document.getElementById('event-message');

    console.log(`自分のID: ${myPlayerId}, 部屋ID: ${roomId}`);

    // --- 1. 初回：全プレイヤーの駒を生成 ---
    const initialRes = await fetch(`/api/matching/status?roomId=${roomId}`);
    const room = await initialRes.json();
    setupPlayersUI(room.players);

    // --- 2. 同期ループ：2秒おきに「誰か動いた？」と確認しに行く ---
    setInterval(async () => {
        const res = await fetch(`/api/matching/status?roomId=${roomId}`);
        if (!res.ok) return;
        const currentRoom = await res.json();

        // ターン管理：自分の番ならボタンを有効化
        updateTurnStatus(currentRoom);

        // 位置の同期：自分以外の駒が動いていたら画面上で動かす
        syncPlayerPieces(currentRoom.players);

        // 誰かが卒業していたら終了
        if (currentRoom.players.some(p => p.earnedUnits >= 124)) {
            location.href = "/result";
        }
    }, 2000);

    // --- 3. ダイスを振る処理 ---
    diceBtn.addEventListener('click', async () => {
        diceBtn.disabled = true;
        eventMessage.innerText = "ダイスを振っています...";

        const url = `/api/game/roll?roomId=${roomId}&playerId=${myPlayerId}`;
        try {
            const res = await fetch(url, { method: 'POST' });
            if (!res.ok) throw new Error(await res.text());
            
            const data = await res.json();
            console.log(`${data.rolledNumber} が出ました！`);
            // 自分の移動アニメーションはポーリング側で自動的に処理されます
        } catch (error) {
            alert(error.message);
            diceBtn.disabled = false;
        }
    });
});

function setupPlayersUI(players) {
    const container = document.querySelector('.board-container');
    players.forEach((p) => { // i は使いません
        if (!document.getElementById(`player-${p.id}`)) {
            const piece = document.createElement('div');
            piece.id = `player-${p.id}`;
            piece.className = `player-piece`;
            
            piece.style.backgroundColor = p.color; 
            piece.style.borderColor = p.color;
            
            console.log(`プレイヤー ${p.name} に色 ${p.color} を適用しました`);
            
            piece.innerText = p.name.substring(0, 1);
            container.appendChild(piece);
        }
        updatePieceVisual(p.id, p.currentPosition);
    });
}

function updateTurnStatus(room) {
    const diceBtn = document.getElementById('diceStart');
    const eventMessage = document.getElementById('event-message');
    const currentPlayer = room.players[room.turnIndex];

    if (currentPlayer.id === myPlayerId) {
        diceBtn.disabled = false;
        eventMessage.innerText = "あなたの番です！";
        diceBtn.style.boxShadow = "0 0 15px #f1c40f"; // 光らせる
    } else {
        diceBtn.disabled = true;
        eventMessage.innerText = `${currentPlayer.name}さんの番です...`;
        diceBtn.style.boxShadow = "none";
    }
}

function syncPlayerPieces(players) {
    players.forEach(p => {
        if (localPlayerPositions[p.id] !== p.currentPosition) {
            console.log(`${p.name}が移動しました`);
            updatePieceVisual(p.id, p.currentPosition);
            localPlayerPositions[p.id] = p.currentPosition;
        }
    });
}

function updatePieceVisual(playerId, index) {
    const piece = document.getElementById(`player-${playerId}`);
    const cell = document.getElementById(`cell-${index}`);
    if (piece && cell) {
        // 駒が重ならないよう、少しだけランダムにずらす
        const offset = (playerId.length % 5) * 4;
        piece.style.top = `${cell.offsetTop + 5 + offset}px`;
        piece.style.left = `${cell.offsetLeft + 5 + offset}px`;
    }
}