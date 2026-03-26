package eventmanagement.dao;

import eventmanagement.model.*;
import eventmanagement.util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

public class EventDAO {

    public PriorityQueue<Event> getAllEvents() {
        PriorityQueue<Event> events = new PriorityQueue<>();
        String query = "SELECT * FROM events";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int eventId = rs.getInt("event_id");
                int orgId = rs.getInt("org_id");
                String title = rs.getString("title");
                LocalDateTime eventDate = rs.getTimestamp("event_date").toLocalDateTime();
                String location = rs.getString("location");
                EventType type = EventType.valueOf(rs.getString("event_type"));
                String description = rs.getString("description");
                int maxCapacity = rs.getInt("max_capacity");
                String status = rs.getString("status");
                
                Event event = new Event(eventId, orgId, title, eventDate, location, type, description, maxCapacity, status);
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public List<Event> getEventsByOrgId(int orgId) {
        List<Event> events = new ArrayList<>();
        PriorityQueue<Event> allEvents = getAllEvents();
        for (Event event : allEvents) {
            if (event.getOrgId() == orgId) {
                events.add(event);
            }
        }
        return events;
    }

    public boolean createEvent(int orgId, String title, LocalDateTime eventDate, String location, EventType type, String description, int maxCapacity, String status) {
        String insertEvent = "INSERT INTO events (org_id, title, event_date, location, event_type, description, max_capacity, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertEvent)) {
            stmt.setInt(1, orgId);
            stmt.setString(2, title);
            stmt.setTimestamp(3, Timestamp.valueOf(eventDate));
            stmt.setString(4, location);
            stmt.setString(5, type.name());
            stmt.setString(6, description);
            stmt.setInt(7, maxCapacity);
            stmt.setString(8, status);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEventStatus(int eventId, String status) {
        String query = "UPDATE events SET status = ? WHERE event_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, eventId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEvent(int eventId) {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM registrations WHERE event_id = ?")) {
                stmt.setInt(1, eventId);
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM events WHERE event_id = ?")) {
                stmt.setInt(1, eventId);
                stmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean registerForEvent(int eventId, int userId, Role role, String applicationText) {
        String status = (role == Role.VOLUNTEER) ? "PENDING" : "ACCEPTED";
        // Generate TKT conditionally if accepted right away
        String ticketNumber = status.equals("ACCEPTED") ? ("TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()) : null;

        String query = "INSERT INTO registrations (event_id, user_id, registration_role, application_text, status, ticket_number) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, eventId);
            stmt.setInt(2, userId);
            stmt.setString(3, role.name());
            stmt.setString(4, applicationText);
            stmt.setString(5, status);
            stmt.setString(6, ticketNumber);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Already registered or error: " + e.getMessage());
            return false;
        }
    }

    public int getRSVPCount(int eventId) {
        String query = "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status = 'ACCEPTED'";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Helper class for Registrations
    public static class EventRegistration {
        public int registrationId;
        public int userId;
        public String email;
        public String role;
        public String applicationText;
        public String status;
        public String ticketNumber;
    }

    public List<EventRegistration> getRegistrationsForEvent(int eventId) {
        List<EventRegistration> list = new ArrayList<>();
        String query = "SELECT r.registration_id, r.user_id, r.registration_role, r.application_text, r.status, r.ticket_number, u.email " +
                       "FROM registrations r JOIN users u ON r.user_id = u.user_id WHERE r.event_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                EventRegistration reg = new EventRegistration();
                reg.registrationId = rs.getInt("registration_id");
                reg.userId = rs.getInt("user_id");
                reg.role = rs.getString("registration_role");
                reg.applicationText = rs.getString("application_text");
                reg.status = rs.getString("status");
                reg.ticketNumber = rs.getString("ticket_number");
                reg.email = rs.getString("email");
                list.add(reg);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateRegistrationStatus(int registrationId, String status) {
        // If we transition to ACCEPTED, we should guarantee they get a ticket number if they didn't have one!
        String query;
        if (status.equals("ACCEPTED")) {
            String newTicket = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            query = "UPDATE registrations SET status = ?, ticket_number = COALESCE(ticket_number, ?) WHERE registration_id = ?";
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, status);
                stmt.setString(2, newTicket);
                stmt.setInt(3, registrationId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); return false; }
        } else {
            query = "UPDATE registrations SET status = ? WHERE registration_id = ?";
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, status);
                stmt.setInt(2, registrationId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); return false; }
        }
    }

    public boolean removeRegistration(int registrationId) {
        String query = "DELETE FROM registrations WHERE registration_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, registrationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Event> getRegisteredEventsForUser(int userId) {
        List<Event> events = new ArrayList<>();
        PriorityQueue<Event> allEvents = getAllEvents();
        String query = "SELECT event_id FROM registrations WHERE user_id = ? AND status = 'ACCEPTED'";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Integer> eventIds = new ArrayList<>();
            while (rs.next()) eventIds.add(rs.getInt("event_id"));
            
            for (Event e : allEvents) {
                if (eventIds.contains(e.getEventId())) {
                    events.add(e);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return events;
    }

    public String getTicketNumberForUserEvent(int eventId, int userId) {
        String query = "SELECT ticket_number FROM registrations WHERE event_id = ? AND user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, eventId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("ticket_number");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
