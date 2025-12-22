package com.example.application.ApplicationServer.sample;

import com.example.application.ClientManagementServer.Message.ApplicationMessage;
import com.google.gson.Gson;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ServerEndpoint("/application")
public class EndpointSample {
    private static Set<Session> establishedSessions = Collections.synchronizedSet(new HashSet<Session>());

    static Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session, EndpointConfig ec) {
        establishedSessions.add(session);
        System.out.println("[ApplicationServerSample] onOpen:" + session.getId());
    }

    @OnMessage
    public void onMessage(final String message, final Session session) throws IOException {
        System.out.println("[ApplicationServerSample] onMessage: " + message);

        ApplicationMessage req = gson.fromJson(message, ApplicationMessage.class);

        if ("CREATE_ROOM".equals(req.getTaskName())) {
            String roomId = "room-" + UUID.randomUUID();

            ApplicationToClientManagementMessage res = new ApplicationToClientManagementMessage("ROOM_CREATED",
                    req.getMatchId(), roomId);

            System.out.println(gson.toJson(res));
            session.getAsyncRemote().sendText(gson.toJson(res));
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("[ApplicationServerSample] onClose:" + session.getId());
        establishedSessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("[ApplicationServerSample] onError:" + session.getId());
    }

    public void sendMessage(Session session, String message) {
        System.out.println("[ApplicationServerSample] sendMessage(): " + message);
        try {
            // 同期送信（sync）
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBroadcastMessage(String message) {
        System.out.println("[ApplicationServerSample] sendBroadcastMessage(): " + message);
        establishedSessions.forEach(session -> {
            // 非同期送信（async）
            session.getAsyncRemote().sendText(message);
        });
    }
}
