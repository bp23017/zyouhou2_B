let pollingInterval;

document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    let roomId = urlParams.get('roomId');

    if (!roomId) {
        initializeMatching();
    } else {
        // 直接URLで来た場合（テスト用など）はIDがないので再取得が必要
        console.warn("直接アクセスはIDが欠損する可能性があります");
    }
});

// matching.js の initializeMatching 関数内
async function initializeMatching() {
    try {
        const playerName = "Player_" + Math.floor(Math.random() * 100);
        const res = await fetch(`/api/matching/auto-join?playerName=${encodeURIComponent(playerName)}`, { method: 'POST' });
        const data = await res.json();

        const roomId = data.room.roomId;
        const myId = data.me.id;
        const myColor = data.me.color; // サーバーから届いた色を取得

        console.log(`入室成功！ Room: ${roomId}, MyID: ${myId}, Color: ${myColor}`);
        
        // URLに color を追加して記憶させる
        history.replaceState(null, '', `?roomId=${roomId}&playerId=${myId}&color=${encodeURIComponent(myColor)}`);

        // pollingを開始する際、色も渡せるようにグローバル変数にするか引数を調整
        startPolling(roomId, myId, myColor); 
    } catch (err) {
        console.error("マッチングエラー:", err);
    }
}

// startPolling 関数も修正して color を受け取る
function startPolling(roomId, myId, myColor) {
    pollingInterval = setInterval(async () => {
        const res = await fetch(`/api/matching/status?roomId=${roomId}`);
        const room = await res.json();

        updateUI(room.players);

        if (room.players.length >= 4) {
            clearInterval(pollingInterval);
            setTimeout(() => {
                // ゲーム画面遷移時のURLにも color を含める
                window.location.href = `/game?roomId=${roomId}&playerId=${myId}&color=${encodeURIComponent(myColor)}`;
            }, 2000);
        }
    }, 2000);
}

function updateUI(players) {
    for (let i = 1; i <= 4; i++) {
        const slot = document.getElementById(`p${i}`);
        if (!slot) continue;
        
        const p = players[i-1]; // サーバーから来たi番目のプレイヤー
        const nameLabel = slot.querySelector('.name');
        const iconLabel = slot.querySelector('.icon');

        if (p) {
            // プレイヤーが存在する場合
            slot.classList.add('connected');
            nameLabel.innerText = p.name; // サーバー側の名前を表示
            iconLabel.innerText = "👤";
        } else {
            // 空席の場合
            slot.classList.remove('connected');
            nameLabel.innerText = "待機中...";
            iconLabel.innerText = "?";
        }
    }
    document.getElementById('current-count').innerText = players.length;
}
