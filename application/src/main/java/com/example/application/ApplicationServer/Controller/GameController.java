package com.example.application.ApplicationServer.Controller;

import org.springframework.web.bind.annotation.*;

import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final RoomManager roomManager;
    private final DiceController diceController;

    public GameController(RoomManager roomManager, DiceController diceController) {
        this.roomManager = roomManager;
        this.diceController = diceController;
    }

    /**
     * ダイスを振る処理（4人対戦対応）
     */
    @PostMapping("/roll")
    public ResponseEntity<?> rollDice(
            @RequestParam String roomId,
            @RequestParam String playerId,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) Integer targetValue) {

        Room room = roomManager.getRoom(roomId);
        if (room == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("部屋が見つかりません");

        // 1. ターンチェック：現在の番のプレイヤーを取得
        List<Player> players = room.getPlayers();
        Player currentPlayer = players.get(room.getTurnIndex());

        // 自分の番ではない人がリクエストを送ってきたらエラーを返す
        if (!currentPlayer.getId().equals(playerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("あなたの番ではありません");
        }

        // 2. ダイス実行（既存の DiceController を利用）
        int rolledNumber = diceController.executeRoll(itemType, targetValue);
        
        // 3. 移動処理
        int oldPos = currentPlayer.getCurrentPosition();
        int newPos = (oldPos + rolledNumber) % 20;
        currentPlayer.setCurrentPosition(newPos);

        // 4. スタート（0番マス）通過判定
        if (oldPos + rolledNumber >= 20) {
            // 単位取得処理（Playerオブジェクト内の単位を増やす）
            currentPlayer.setEarnedUnits(currentPlayer.getEarnedUnits() + currentPlayer.getExpectedUnits());
        }

        // 5. 卒業判定
        boolean isGraduated = currentPlayer.getEarnedUnits() >= 124;


        // クライアントに現在の部屋の全状態を返す
        Map<String, Object> response = new HashMap<>();
        response.put("rolledNumber", rolledNumber);
        response.put("room", room); // 全員の最新位置が含まれる
        response.put("isGraduated", isGraduated);

        return ResponseEntity.ok(response);
    }
}