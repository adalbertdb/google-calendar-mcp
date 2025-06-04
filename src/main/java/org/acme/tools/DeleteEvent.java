package org.acme.tools;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;

import java.io.IOException;
import java.util.List;

import static org.acme.tools.CreateEvent.validateDateTime;

public class DeleteEvent {

    @Inject
    Calendar calendarService;

    @Tool(description = "Deletes events from the user's primary Google Calendar matching a search query. Requires authenticated Google Calendar API access. Possible errors: no matching events, authentication issues, or API errors.")
    public String deleteEventsByQuery(
            @ToolArg(description = "The search query to match event summaries (e.g., 'Team Meeting').") String query,
            @ToolArg(description = "Optional start date to filter events (ISO 8601 format, e.g., '2025-06-04T00:00:00').") String startDate,
            @ToolArg(description = "Optional end date to filter events (ISO 8601 format, e.g., '2025-06-04T23:59:59').") String endDate
    ) {
        try {
            // Build the event list request
            Calendar.Events.List request = calendarService.events().list("primary");
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
                return "No events found matching the query: " + query;
            }

            // Delete matching events
            int deletedCount = 0;
            for (Event event : eventList) {
                calendarService.events().delete("primary", event.getId()).execute();
                deletedCount++;
            }

            return "Successfully deleted " + deletedCount + " event(s) matching the query: " + query;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid input: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error deleting events: " + e.getMessage());
        }
    }

    @Tool(description = "Deletes all events from the user's primary Google Calendar within a specified date range. Requires authenticated Google Calendar API access. Possible errors: invalid date format, authentication issues, or API errors.")
    public String deleteEventsByDateRange(
            @ToolArg(description = "The start date and time in ISO 8601 format (e.g., '2025-06-04T00:00:00').") String startDate,
            @ToolArg(description = "The end date and time in ISO 8601 format (e.g., '2025-06-04T23:59:59').") String endDate
    ) {
        try {
            // Validate date inputs
            validateDateTime(startDate, "startDate");
            validateDateTime(endDate, "endDate");

            // Build the event list request
            Calendar.Events.List request = calendarService.events().list("primary")
                    .setTimeMin(new DateTime(startDate))
                    .setTimeMax(new DateTime(endDate));

            // Fetch matching events
            Events events = request.execute();
            List<Event> eventList = events.getItems();
            if (eventList.isEmpty()) {
                return "No events found in the specified date range.";
            }

            // Delete matching events
            int deletedCount = 0;
            for (Event event : eventList) {
                calendarService.events().delete("primary", event.getId()).execute();
                deletedCount++;
            }

            return "Successfully deleted " + deletedCount + " event(s) in the specified date range.";
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid input: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error deleting events: " + e.getMessage());
        }
    }

    @Tool(description = "Deletes an event or specific instance of a recurring event from the user's primary Google Calendar by event ID. Requires authenticated Google Calendar API access. Possible errors: invalid event ID, event not found, authentication issues, or API errors.")
    public String deleteRecurringEvent(
            @ToolArg(description = "The unique ID of the event to delete (e.g., 'abc123xyz789').") String eventId,
            @ToolArg(description = "Optional: Specific instance date to delete in ISO 8601 format (e.g., '2025-06-04T10:00:00'). If not provided, deletes all instances.") String instanceDate
    ) {
        try {
            // Validate eventId
            if (eventId == null || eventId.trim().isEmpty()) {
                throw new IllegalArgumentException("Event ID cannot be null or empty.");
            }

            // Check if the event exists
            Event event;
            try {
                event = calendarService.events().get("primary", eventId).execute();
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 404) {
                    return "No event found with ID " + eventId + ".";
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
                calendarService.events().delete("primary", instanceId).execute();
                return "Deleted instance of recurring event with ID " + eventId + " on " + instanceDate + ".";
            } else {
                // Delete the entire event (including all instances if recurring)
                calendarService.events().delete("primary", eventId).execute();
                return "Event with ID " + eventId + " deleted successfully (all instances).";
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid input: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error deleting event: " + e.getMessage());
        }
    }

    @Tool(description = "Deletes ALL events from the user's primary Google Calendar. Use with caution. Requires authenticated Google Calendar API access. Possible errors: authentication issues or API errors.")
    public String clearAllEvents() {
        try {
            Events events = calendarService.events().list("primary").execute();
            List<Event> eventList = events.getItems();
            if (eventList != null && !eventList.isEmpty()) {
                for (Event event : eventList) {
                    calendarService.events().delete("primary", event.getId()).execute();
                }
            }
            return "All events in the primary calendar have been deleted.";
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error clearing events: " + e.getMessage());
        }
    }
}