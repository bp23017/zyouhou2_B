package com.example.application.ApplicationServer.Controller;

import org.springframework.web.bind.annotation.*;

import com.example.application.ApplicationServer.Entity.Player;
import com.example.application.ApplicationServer.Entity.Room;

import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

// マッチング関連のAPIを提供するコントローラクラス
@RestController
@RequestMapping("/api/matching")
public class MatchingController {
    private final RoomManager roomManager;

    public MatchingController(RoomManager roomManager) {
        System.out.println("MatchingController initialized with RoomManager.");
        this.roomManager = roomManager;
    }

 @PostMapping("/auto-join")
public ResponseEntity<Map<String, Object>> autoJoin(@RequestParam String playerName) {
    Room room = roomManager.findAvailableRoom();
    if (room == null) {
        room = roomManager.createRoom();
    }

    // 1. カラーリスト（CSSの色名またはカラーコード）
    List<String> colors = List.of("#ff4d4d", "#4d94ff", "#4dff88", "#ffdb4d"); // 赤, 青, 緑, 黄

    // 2. 現在の「参加済み人数」を数えて、自分の色を決める
    int playerIndex = room.getPlayers().size(); // 0人目なら0、1人目なら1...
    String myColor = colors.get(playerIndex % colors.size());
    System.out.println("Assigning color " + myColor + " to player " + playerName);

    // 3. プレイヤーを作成（名前と決まった色を渡す）
    Player me = new Player(playerName, myColor);
    room.addPlayer(me);
    System.out.println("Player " + me.getName() + " joined room " + room.getRoomId() + " with color " + myColor);

    Map<String, Object> response = new HashMap<>();
    response.put("room", room);
    response.put("me", me);
    System.out.println(me);

    return ResponseEntity.ok(response);
}

    @GetMapping("/status")
    public ResponseEntity<Room> getStatus(@RequestParam String roomId) {
        Room room = roomManager.getRoom(roomId);
        System.out.println("Fetching status for room ID: " + roomId);
        return (room != null) ? ResponseEntity.ok(room) : ResponseEntity.notFound().build();
    }
    @PostMapping("/register-room")
    public ResponseEntity<String> registerRoomFromManagement(@RequestBody Map<String, Object> data) {
    // 1. 部屋を作成
    Room room = new Room();
    room.setRoomId((String) data.get("roomId"));
    System.out.println("[MatchingController] Registering room from management: " + room.getRoomId());
    
    // 2. プレイヤーリストを復元
    List<Map<String, Object>> playersData = (List<Map<String, Object>>) data.get("players");
    for (Map<String, Object> pData : playersData) {
        Player player = new Player((String) pData.get("name"), (String) pData.get("color"));
        player.setId((String) pData.get("id")); 
        player.setEarnedUnits(0);
        player.setExpectedUnits(25);
        room.addPlayer(player);
        System.out.println("[MatchingController] Added player: " + player.getName() + " (ID: " + player.getId() + ")");
    }
    
    // 3. 共有の RoomManager に登録
    RoomManager.instance.addRoom(room); 


    System.out.println("[App Server] 管理サーバーから同期完了。RoomID: " + room.getRoomId());
    for (Player p : room.getPlayers()) {
        System.out.println("  - 同期プレイヤー: " + p.getName() + " (ID: " + p.getId() + ")");
    }
    return ResponseEntity.ok("Success");
}
}