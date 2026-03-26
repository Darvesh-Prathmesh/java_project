package eventmanagement.dao;

import eventmanagement.model.*;
import eventmanagement.util.Database;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class UserDAO {

    /**
     * Hashes a plain-text password with SHA-256 and returns the hex digest.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public User loginUser(String email, String password) {
        String hashed = hashPassword(password);
        String query = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            stmt.setString(2, hashed);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String roleStr = rs.getString("role");
                Role role = Role.valueOf(roleStr);
                return fetchSpecificUser(userId, email, role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User fetchSpecificUser(int userId, String email, Role role) throws SQLException {
        Connection conn = Database.getConnection();
        if (role == Role.ORGANIZATION) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM organizations WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return new Organization(userId, email, rs.getString("organization_name"));
        } else if (role == Role.PARTICIPANT) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM participants WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return new Participant(userId, email, rs.getString("first_name"), rs.getString("last_name"));
        } else if (role == Role.VOLUNTEER) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM volunteers WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return new Volunteer(userId, email, rs.getString("first_name"), rs.getString("last_name"), rs.getInt("hours_logged"));
        }
        return null;
    }

    public boolean registerOrganization(String email, String password, String orgName) {
        return registerBaseUser(email, hashPassword(password), Role.ORGANIZATION, conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO organizations (user_id, organization_name) VALUES (LAST_INSERT_ID(), ?)")) {
                stmt.setString(1, orgName);
                stmt.executeUpdate();
            }
        });
    }

    public boolean registerParticipant(String email, String password, String firstName, String lastName) {
        return registerBaseUser(email, hashPassword(password), Role.PARTICIPANT, conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO participants (user_id, first_name, last_name) VALUES (LAST_INSERT_ID(), ?, ?)")) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.executeUpdate();
            }
        });
    }

    public boolean registerVolunteer(String email, String password, String firstName, String lastName) {
        return registerBaseUser(email, hashPassword(password), Role.VOLUNTEER, conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO volunteers (user_id, first_name, last_name, hours_logged) VALUES (LAST_INSERT_ID(), ?, ?, 0)")) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.executeUpdate();
            }
        });
    }

    private boolean registerBaseUser(String email, String passwordHash, Role role, ChildTableInserter inserter) {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            String insertUser = "INSERT INTO users (email, password_hash, role) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertUser)) {
                stmt.setString(1, email);
                stmt.setString(2, passwordHash);
                stmt.setString(3, role.name());
                stmt.executeUpdate();
            }
            inserter.insert(conn);
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    private interface ChildTableInserter {
        void insert(Connection conn) throws SQLException;
    }
}
