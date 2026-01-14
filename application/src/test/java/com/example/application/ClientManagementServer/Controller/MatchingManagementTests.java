package com.example.application.ClientManagementServer.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
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

class MatchingManagementTests {
    private final Gson gson = new Gson();
    private MatchingManagement matchingManagement;

    @BeforeEach
    void setUp() {
        matchingManagement = new MatchingManagement();
        clearMatchingState();
    }

    @Test
    void addUserToWaitListBroadcastsWaitStatus() throws Exception {
        Session session = mock(Session.class);
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        when(session.isOpen()).thenReturn(true);
        when(session.getBasicRemote()).thenReturn(basic);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        matchingManagement.addUserToWaitList(session, "Alice", "u1");

        verify(basic).sendText(captor.capture());
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> payload = gson.fromJson(captor.getValue(), type);
        assertEquals("WAIT_STATUS", payload.get("taskName"));
        List<?> players = (List<?>) payload.get("players");
        assertEquals(List.of("Alice"), players);
    }

    @Test
    void addUserToWaitListDoesNotAddDuplicateUserId() throws Exception {
        Session session1 = mock(Session.class);
        RemoteEndpoint.Basic basic1 = mock(RemoteEndpoint.Basic.class);
        when(session1.isOpen()).thenReturn(true);
        when(session1.getBasicRemote()).thenReturn(basic1);

        Session session2 = mock(Session.class);
        RemoteEndpoint.Basic basic2 = mock(RemoteEndpoint.Basic.class);
        when(session2.isOpen()).thenReturn(true);
        when(session2.getBasicRemote()).thenReturn(basic2);

        matchingManagement.addUserToWaitList(session1, "Alice", "u1");
        clearInvocations(basic1);

        matchingManagement.addUserToWaitList(session2, "Bob", "u1");

        verify(basic1).sendText(anyString());
        verifyNoInteractions(basic2);
    }

    @Test
    void addUserToWaitListIgnoresBlankUserName() {
        Session session = mock(Session.class);

        matchingManagement.addUserToWaitList(session, "", "u1");

        verifyNoInteractions(session);
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
