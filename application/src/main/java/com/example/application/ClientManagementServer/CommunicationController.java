package com.example.application.ClientManagementServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

@Component
@ServerEndpoint("/client-management")
public class CommunicationController {
    public static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    private static final ClientManagementController authController = new ClientManagementController();

    @OnMessage
    public void onMessage(String json, Session session) {
        // 管理サーバーが担当するタスクのみを処理
        if (json.contains("LOGIN") || json.contains("REGISTER") || json.contains("MATCHING") || json.contains("LOGOUT")) {
            authController.processClientMessage(json, session);
        } else {
            System.out.println("[Communication] 警告: 管理サーバーでゲームメッセージを受信しました。無視します。");
        }
    }
    
    public static void sendToUser(String userId, String message) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        }
    }
}