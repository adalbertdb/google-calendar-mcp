package org.acme.tools.extra;

import java.util.List;

public class CalendarSelection {
    private final String message;
    private final String calendarId;
    private final List<CalendarOption> availableCalendars;

    public CalendarSelection(String message, String calendarId, List<CalendarOption> availableCalendars) {
        this.message = message;
        this.calendarId = calendarId;
        this.availableCalendars = availableCalendars != null ? availableCalendars : List.of();
    }

    public String getMessage() {
        return message;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public List<CalendarOption> getAvailableCalendars() {
        return availableCalendars;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(message);
        if (!availableCalendars.isEmpty()) {
            sb.append("\nAvailable calendars:\n");
            for (CalendarOption option : availableCalendars) {
                sb.append("ID: ").append(option.id).append(", Name: ").append(option.name).append("\n");
            }
        }
        return sb.toString();
    }
}
