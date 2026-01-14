package com.example.application.ClientManagementServer.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.application.ClientManagementServer.Entity.RankRecord;
import com.example.application.ClientManagementServer.Entity.User;

class DatabaseAccessTests {
    private static TestDriver testDriver;
    private DatabaseAccess databaseAccess;

    @BeforeAll
    static void registerDriver() throws SQLException {
        testDriver = new TestDriver();
        DriverManager.registerDriver(testDriver);
    }

    @AfterAll
    static void unregisterDriver() throws SQLException {
        DriverManager.deregisterDriver(testDriver);
    }

    @BeforeEach
    void setUp() {
        databaseAccess = new DatabaseAccess();
        databaseAccess.url = "jdbc:mock:test";
        databaseAccess.user = "user";
        databaseAccess.pass = "pass";
    }

    @Test
    void getUserByUsernameReturnsUserWhenFound() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        String sql = "SELECT id, username, password FROM account WHERE username = ?";
        when(con.prepareStatement(sql)).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("id")).thenReturn("u1");
        when(rs.getString("username")).thenReturn("alice");
        when(rs.getString("password")).thenReturn("secret");

        testDriver.setConnection(con);

        User user = databaseAccess.getUserByUsername("alice");

        assertNotNull(user);
        assertEquals("u1", user.userId());
        assertEquals("alice", user.userName());
        assertEquals("secret", user.password());
        verify(ps).setString(1, "alice");
    }

    @Test
    void getUserByUsernameReturnsNullWhenMissing() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        String sql = "SELECT id, username, password FROM account WHERE username = ?";
        when(con.prepareStatement(sql)).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        testDriver.setConnection(con);

        User user = databaseAccess.getUserByUsername("missing");

        assertNull(user);
        verify(ps).setString(1, "missing");
    }

    @Test
    void getLoginStatusByUsernameReturnsStateWhenFound() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        String sql = "SELECT login_state FROM account WHERE username = ?";
        when(con.prepareStatement(sql)).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getBoolean("login_state")).thenReturn(true);

        testDriver.setConnection(con);

        boolean result = databaseAccess.getLoginStatusByUsername("alice");

        assertTrue(result);
        verify(ps).setString(1, "alice");
    }

    @Test
    void getLoginStatusByUsernameReturnsFalseWhenMissing() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        String sql = "SELECT login_state FROM account WHERE username = ?";
        when(con.prepareStatement(sql)).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        testDriver.setConnection(con);

        boolean result = databaseAccess.getLoginStatusByUsername("missing");

        assertEquals(false, result);
        verify(ps).setString(1, "missing");
    }

    @Test
    void getRankRecordByUsernameReturnsCountsWhenFound() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        String sql = "SELECT rank1, rank2, rank3, rank4 FROM account WHERE username = ?";
        when(con.prepareStatement(sql)).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("rank1")).thenReturn(1);
        when(rs.getInt("rank2")).thenReturn(2);
        when(rs.getInt("rank3")).thenReturn(3);
        when(rs.getInt("rank4")).thenReturn(4);

        testDriver.setConnection(con);

        RankRecord record = databaseAccess.getRankRecordByUsername("alice");

        assertEquals(new RankRecord(1, 2, 3, 4), record);
        verify(ps).setString(1, "alice");
    }

    @Test
    void getRankRecordByUsernameReturnsZerosWhenMissing() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        String sql = "SELECT rank1, rank2, rank3, rank4 FROM account WHERE username = ?";
        when(con.prepareStatement(sql)).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        testDriver.setConnection(con);

        RankRecord record = databaseAccess.getRankRecordByUsername("missing");

        assertEquals(new RankRecord(0, 0, 0, 0), record);
        verify(ps).setString(1, "missing");
    }

    @Test
    void incrementRankCountUpdatesTargetColumn() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        String sql = "UPDATE account SET rank2 = rank2 + 1 WHERE username = ?";
        when(con.prepareStatement(sql)).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        testDriver.setConnection(con);

        boolean result = databaseAccess.incrementRankCount("alice", 2);

        assertTrue(result);
        verify(ps).setString(1, "alice");
    }

    @Test
    void createUserInsertsAndReturnsUser() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        String sql = "INSERT INTO account (id, username, password) VALUES (?, ?, ?)";
        when(con.prepareStatement(sql)).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        testDriver.setConnection(con);

        User user = databaseAccess.createUser("alice", "secret", "u1");

        assertEquals(new User("u1", "alice", "secret"), user);
        verify(ps).setString(1, "u1");
        verify(ps).setString(2, "alice");
        verify(ps).setString(3, "secret");
    }

    @Test
    void setLoginStatusUpdatesLoginState() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        String sql = "UPDATE account SET login_state = ? WHERE username = ?";
        when(con.prepareStatement(sql)).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        testDriver.setConnection(con);

        boolean result = databaseAccess.setLoginStatus("alice", true);

        assertTrue(result);
        verify(ps).setBoolean(1, true);
        verify(ps).setString(2, "alice");
    }

    @Test
    void resetAllLoginStatusesExecutesUpdate() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        String sql = "UPDATE account SET login_state = 0";
        when(con.prepareStatement(sql)).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(2);

        testDriver.setConnection(con);

        databaseAccess.resetAllLoginStatuses();

        verify(ps).executeUpdate();
    }

    private static class TestDriver implements Driver {
        private volatile Connection connection;

        void setConnection(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            if (!acceptsURL(url)) {
                return null;
            }
            if (connection == null) {
                throw new SQLException("No connection configured for test driver");
            }
            return connection;
        }

        @Override
        public boolean acceptsURL(String url) {
            return url != null && url.startsWith("jdbc:mock:");
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 1;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("No parent logger");
        }
    }
}
