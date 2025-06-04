package org.acme.tools.extra;

public class EventResponse {
    private final String message;
    private final String eventId;

    public EventResponse(String message, String eventId) {
        this.message = message;
        this.eventId = eventId;
    }

    public String getMessage() {
        return message;
    }

    public String getEventId() {
        return eventId;
    }

    @Override
    public String toString() {
        return message + " (ID: " + eventId + ")";
    }
}
