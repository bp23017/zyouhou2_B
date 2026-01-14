package com.example.application.ApplicationServer.Controller;

import org.springframework.stereotype.Service;
import com.example.application.ApplicationServer.Entity.Room;
import jakarta.annotation.PostConstruct; 
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

// 部屋管理を担当するクラス

@Service
public class RoomManager {
    public static RoomManager instance;

    private static Map<String, Room> activeRooms = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        instance = this;
        System.out.println("[RoomManager] インスタンスがセットされました");
    }

    public Room getRoom(String roomId) {
        System.out.println("[RoomManager] Getting room with ID: " + roomId);
        return activeRooms.get(roomId);
    }

    public Room createRoom() {
        Room newRoom = new Room();
        activeRooms.put(newRoom.getRoomId(), newRoom);
        System.out.println("[RoomManager] Created new room with ID: " + newRoom.getRoomId());
        return newRoom;
    }

    public Room findAvailableRoom() {
        System.out.println("[RoomManager] Finding available room");
        return activeRooms.values().stream()
                .filter(room -> room.getPlayers().size() < 4)
                .findFirst()
                .orElse(null);
    }
    
    public void addRoom(Room room) {
        activeRooms.put(room.getRoomId(), room);
    }

}