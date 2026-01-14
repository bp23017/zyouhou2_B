package com.example.application.ClientManagementServer.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;

class CommunicationControllerTests {
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        CommunicationController.userSessions.clear();
        clearMatchingState();
    }

    @Test
    void sendToUserSendsWhenSessionOpen() {
        Session session = mock(Session.class);
        RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
        when(session.isOpen()).thenReturn(true);
        when(session.getAsyncRemote()).thenReturn(async);
        CommunicationController.userSessions.put("u1", session);

        CommunicationController.sendToUser("u1", "hello");

        verify(async).sendText("hello");
    }

    @Test
    void sendToUserDoesNothingWhenSessionClosed() {
        Session session = mock(Session.class);
        RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
        when(session.isOpen()).thenReturn(false);
        when(session.getAsyncRemote()).thenReturn(async);
        CommunicationController.userSessions.put("u1", session);

        CommunicationController.sendToUser("u1", "hello");

        verify(session, never()).getAsyncRemote();
    }

    @Test
    void onMessageMatchingTaskTriggersWaitStatusBroadcast() throws Exception {
        CommunicationController controller = new CommunicationController();
        Session session = mock(Session.class);
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        when(session.isOpen()).thenReturn(true);
        when(session.getBasicRemote()).thenReturn(basic);

        String json = "{\"taskName\":\"MATCHING\",\"userId\":\"u1\",\"userName\":\"Alice\",\"password\":\"\"}";
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        controller.onMessage(json, session);

        verify(basic).sendText(captor.capture());
        Map<String, Object> payload = gson.fromJson(captor.getValue(), new TypeToken<Map<String, Object>>() {}.getType());
        assertEquals("WAIT_STATUS", payload.get("taskName"));
        List<?> players = (List<?>) payload.get("players");
        assertEquals(List.of("Alice"), players);
    }

    private static void clearMatchingState() {
        try {
            Field waitListField = MatchingManagement.class.getDeclaredField("matchingWaitList");
            waitListField.setAccessible(true);
            Deque<?> waitList = (Deque<?>) waitListField.get(null);
            waitList.clear();

            Field activeRoomsField = MatchingManagement.class.getDeclaredField("activeRooms");
            activeRoomsField.setAccessible(true);
            Map<?, ?> activeRooms = (Map<?, ?>) activeRoomsField.get(null);
            activeRooms.clear();
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset matching state", e);
        }
    }
}
