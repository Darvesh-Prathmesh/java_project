package eventmanagement.model;

import java.time.LocalDateTime;

public class Event implements Comparable<Event> {
    private int eventId;
    private int orgId;
    private String title;
    private LocalDateTime eventDate;
    private String location;
    private EventType eventType;
    private String description;
    private int maxCapacity;
    private String status;

    public Event(int eventId, int orgId, String title, LocalDateTime eventDate, String location, EventType eventType, String description, int maxCapacity, String status) {
        this.eventId = eventId;
        this.orgId = orgId;
        this.title = title;
        this.eventDate = eventDate;
        this.location = location;
        this.eventType = eventType;
        this.description = description;
        this.maxCapacity = maxCapacity;
        this.status = status;
    }

    public int getEventId() { return eventId; }
    public int getOrgId() { return orgId; }
    public String getTitle() { return title; }
    public LocalDateTime getEventDate() { return eventDate; }
    public String getLocation() { return location; }
    public EventType getEventType() { return eventType; }
    public String getDescription() { return description; }
    public int getMaxCapacity() { return maxCapacity; }
    public String getStatus() { return status; }

    @Override
    public int compareTo(Event o) {
        return this.eventDate.compareTo(o.eventDate);
    }
}
