package com.example.application.ApplicationServer.Controller;

import org.springframework.stereotype.Service;
import com.example.application.ApplicationServer.Entity.Room;
import jakarta.annotation.PostConstruct; // ✅ 追加
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class RoomManager {
    // ✅ 追加：外部から参照できるようにする
    public static RoomManager instance;

    private static Map<String, Room> activeRooms = new ConcurrentHashMap<>();

    // ✅ 追加：Spring起動時に自分をセットする
    @PostConstruct
    public void init() {
        instance = this;
        System.out.println("[RoomManager] インスタンスがセットされました");
    }

    public Room getRoom(String roomId) {
        return activeRooms.get(roomId);
    }

    public Room createRoom() {
        Room newRoom = new Room();
        activeRooms.put(newRoom.getRoomId(), newRoom);
        return newRoom;
    }

    public Room findAvailableRoom() {
        return activeRooms.values().stream()
                .filter(room -> room.getPlayers().size() < 4)
                .findFirst()
                .orElse(null);
    }
}