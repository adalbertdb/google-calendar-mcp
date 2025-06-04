package org.acme.tools;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import org.acme.tools.extra.CalendarSelection;

import java.io.IOException;
import java.util.List;

import static org.acme.tools.CreateEvent.validateDateTime;

public class ListEventsTool {

    @Inject
    Calendar calendarService;

    @Inject
    SelectCalendarTool selectCalendarTool;

    @Tool(description = "Lists events from a Google Calendar specified by name (fuzzy matched). Requires authenticated Google Calendar API access. Possible errors: no matching calendar, invalid date format, authentication issues, or API errors.")
    public String listEvents(
            @ToolArg(description = "The name of the calendar to list events from (e.g., 'ai test'). Supports fuzzy matching.") String calendarName,
            @ToolArg(description = "Optional search query to match event summaries (e.g., 'Team Meeting').") String query,
            @ToolArg(description = "Optional start date to filter events (ISO 8601 format, e.g., '2025-06-04T00:00:00').") String startDate,
            @ToolArg(description = "Optional end date to filter events (ISO 8601 format, e.g., '2025-06-04T23:59:59').") String endDate
    ) {
        try {
            // Resolve calendarName to calendarId
            CalendarSelection calendarSelection = selectCalendarTool.selectCalendar(calendarName);
            String calendarId = calendarSelection.getCalendarId();
            if (calendarId == null) {
                throw new IllegalArgumentException(calendarSelection.getMessage());
            }

            // Validate date inputs if provided
            if (startDate != null && !startDate.isEmpty()) {
                validateDateTime(startDate, "startDate");
            }
            if (endDate != null && !endDate.isEmpty()) {
                validateDateTime(endDate, "endDate");
            }

            // Build the event list request
            Calendar.Events.List request = calendarService.events().list(calendarId);
            if (query != null && !query.trim().isEmpty()) {
                request.setQ(query); // Search by summary or description
            }
            if (startDate != null && !startDate.isEmpty()) {
                request.setTimeMin(new com.google.api.client.util.DateTime(startDate));
            }
            if (endDate != null && !endDate.isEmpty()) {
                request.setTimeMax(new com.google.api.client.util.DateTime(endDate));
            }

            // Fetch events
            Events events = request.execute();
            List<Event> eventList = events.getItems();
            if (eventList.isEmpty()) {
                return "No events found in calendar " + calendarId + " matching the provided criteria.";
            }

            // Format event details
            StringBuilder response = new StringBuilder("Events in calendar " + calendarId + ":\n");
            for (Event event : eventList) {
                String start = event.getStart().getDateTime() != null
                        ? event.getStart().getDateTime().toString()
                        : event.getStart().getDate().toString();
                String end = event.getEnd().getDateTime() != null
                        ? event.getEnd().getDateTime().toString()
                        : event.getEnd().getDate().toString();
                response.append("ID: ").append(event.getId())
                        .append(", Summary: ").append(event.getSummary() != null ?
                                event.getSummary() : "No summary")
                        .append(", Start: ").append(start)
                        .append(", End: ").append(end)
                        .append("\n");
            }

            return response.toString();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid input: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error listing events: " + e.getMessage());
        }
    }
}