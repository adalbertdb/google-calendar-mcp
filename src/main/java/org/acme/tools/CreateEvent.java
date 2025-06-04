package org.acme.tools;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;

import java.io.IOException;

public class CreateEvent{

    @Inject
    Calendar calendarService;

    @Tool(description = "Creates a new event in the user's primary Google Calendar. Requires authenticated Google Calendar API access. Possible errors: invalid date format, authentication issues, or API errors.")
    public String createEvent(
            @ToolArg(description = "The title or name of the event (e.g., 'Team Meeting').") String summary,
            @ToolArg(description = "The physical or virtual location of the event (e.g., 'Conference Room A' or 'Zoom').") String location,
            @ToolArg(description = "A detailed description of the event (e.g., agenda or notes).") String description,
            @ToolArg(description = "The start date and time in ISO 8601 format (e.g., '2025-06-04T10:00:00').") String start,
            @ToolArg(description = "The end date and time in ISO 8601 format (e.g., '2025-06-04T11:00:00').") String end,
            @ToolArg(description = "The time zone for the event (e.g., 'Europe/Madrid'). Defaults to 'Europe/Madrid' if not provided.") String timeZone
    ) {
        try {
            Event event = new Event();
            event.setSummary(summary);
            event.setLocation(location);
            event.setDescription(description);

            validateDateTime(start, "start");
            validateDateTime(end, "end");

            EventDateTime startTime = new EventDateTime()
                    .setDateTime(new DateTime(start))
                    .setTimeZone(timeZone);
            event.setStart(startTime);

            EventDateTime endTime = new EventDateTime()
                    .setDateTime(new DateTime(end))
                    .setTimeZone(timeZone);
            event.setEnd(endTime);

            calendarService.events().insert("primary", event).execute();
            return "Event created successfully";
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid input: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error creating event: " + e.getMessage());
        }
    }
    public static void validateDateTime(String dateTime, String fieldName) {
        try {
            new DateTime(dateTime);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(fieldName + " must be in ISO 8601 format (e.g., '2025-06-04T10:00:00').");
        }
    }
}
