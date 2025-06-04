package org.acme.tools;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
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

public class DeleteEvent {

    @Inject
    Calendar calendarService;

    @Inject
    SelectCalendarTool selectCalendarTool;

    @Tool(description = "Deletes events from the specified Google Calendar matching a search query.")
    public String deleteEventsByQuery(
            @ToolArg(description = "The name of the calendar to delete events from (e.g., 'ai test'). Supports fuzzy matching.") String calendarName,
            @ToolArg(description = "The search query to match event summaries (e.g., 'Team Meeting').") String query,
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

            // Fetch matching events
            Events events = request.execute();
            List<Event> eventList = events.getItems();
            if (eventList.isEmpty()) {
                return "No events found matching the query: " + query + " in calendar " + calendarId + ".";
            }

            // Delete matching events
            int deletedCount = 0;
            for (Event event : eventList) {
                calendarService.events().delete(calendarId, event.getId()).execute();
                deletedCount++;
            }

            return "Successfully deleted " + deletedCount + " event(s) matching the query: " + query + " in calendar " + calendarId + ".";
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid input: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error deleting events: " + e.getMessage());
        }
    }

    @Tool(description = "Deletes all events from the specified Google Calendar within a specified date range.")
    public String deleteEventsByDateRange(
            @ToolArg(description = "The name of the calendar to delete events from (e.g., 'ai test'). Supports fuzzy matching.") String calendarName,
            @ToolArg(description = "The start date and time in ISO 8601 format (e.g., '2025-06-04T00:00:00').") String startDate,
            @ToolArg(description = "The end date and time in ISO 8601 format (e.g., '2025-06-04T23:59:59').") String endDate
    ) {
        try {
            // Resolve calendarName to calendarId
            CalendarSelection calendarSelection = selectCalendarTool.selectCalendar(calendarName);
            String calendarId = calendarSelection.getCalendarId();
            if (calendarId == null) {
                throw new IllegalArgumentException(calendarSelection.getMessage());
            }

            // Validate date inputs
            validateDateTime(startDate, "startDate");
            validateDateTime(endDate, "endDate");

            // Build the event list request
            Calendar.Events.List request = calendarService.events().list(calendarId)
                    .setTimeMin(new DateTime(startDate))
                    .setTimeMax(new DateTime(endDate));

            // Fetch matching events
            Events events = request.execute();
            List<Event> eventList = events.getItems();
            if (eventList.isEmpty()) {
                return "No events found in the specified date range in calendar " + calendarId + ".";
            }

            // Delete matching events
            int deletedCount = 0;
            for (Event event : eventList) {
                calendarService.events().delete(calendarId, event.getId()).execute();
                deletedCount++;
            }

            return "Successfully deleted " + deletedCount + " event(s) in the specified date range in calendar " + calendarId + ".";
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid input: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error deleting events: " + e.getMessage());
        }
    }

    @Tool(description = "Deletes an event or specific instance of a recurring event from the specified Google Calendar by event ID.")
    public String deleteRecurringEvent(
            @ToolArg(description = "The name of the calendar containing the event (e.g., 'ai test'). Supports fuzzy matching.") String calendarName,
            @ToolArg(description = "The unique ID of the event to delete (e.g., 'abc123xyz789').") String eventId,
            @ToolArg(description = "Optional: Specific instance date to delete in ISO 8601 format (e.g., '2025-06-04T10:00:00'). If not provided, deletes all instances.") String instanceDate
    ) {
        try {
            // Resolve calendarName to calendarId
            CalendarSelection calendarSelection = selectCalendarTool.selectCalendar(calendarName);
            String calendarId = calendarSelection.getCalendarId();
            if (calendarId == null) {
                throw new IllegalArgumentException(calendarSelection.getMessage());
            }

            // Validate eventId
            if (eventId == null || eventId.trim().isEmpty()) {
                throw new IllegalArgumentException("Event ID cannot be null or empty.");
            }

            // Check if the event exists
            Event event;
            try {
                event = calendarService.events().get(calendarId, eventId).execute();
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 404) {
                    return "No event found with ID " + eventId + " in calendar " + calendarId + ".";
                }
                throw new RuntimeException("Error checking event existence: " + e.getMessage());
            }

            // Handle recurring event instance or entire series
            if (instanceDate != null && !instanceDate.isEmpty()) {
                // Validate instanceDate
                try {
                    new com.google.api.client.util.DateTime(instanceDate);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("instanceDate must be in ISO 8601 format (e.g., '2025-06-04T10:00:00').");
                }

                // Delete a specific instance of a recurring event
                String instanceId = eventId + "_" + instanceDate.replaceAll("[^0-9T]", "");
                calendarService.events().delete(calendarId, instanceId).execute();
                return "Deleted instance of recurring event with ID " + eventId + " on " + instanceDate + " from calendar " + calendarId + ".";
            } else {
                // Delete the entire event (including all instances if recurring)
                calendarService.events().delete(calendarId, eventId).execute();
                return "Event with ID " + eventId + " deleted successfully (all instances) from calendar " + calendarId + ".";
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid input: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error deleting event: " + e.getMessage());
        }
    }

    @Tool(description = "Deletes ALL events from the specified Google Calendar. Use with caution.")
    public String clearAllEvents(
            @ToolArg(description = "The name of the calendar to clear (e.g., 'ai test'). Supports fuzzy matching.") String calendarName
    ) {
        try {
            // Resolve calendarName to calendarId
            CalendarSelection calendarSelection = selectCalendarTool.selectCalendar(calendarName);
            String calendarId = calendarSelection.getCalendarId();
            if (calendarId == null) {
                throw new IllegalArgumentException(calendarSelection.getMessage());
            }

            // Fetch and delete all events
            Events events = calendarService.events().list(calendarId).execute();
            List<Event> eventList = events.getItems();
            if (eventList != null && !eventList.isEmpty()) {
                for (Event event : eventList) {
                    calendarService.events().delete(calendarId, event.getId()).execute();
                }
            }
            return "All events in calendar " + calendarId + " have been deleted.";
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid input: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error clearing events: " + e.getMessage());
        }
    }
}