package org.acme.tools;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;

import java.io.IOException;
import java.util.List;

public class SelectCalendarTool {

    @Inject
    Calendar calendarService;

    @Tool(description = "Lists available Google Calendars for the user and selects one for use. Requires authenticated Google Calendar API access. Possible errors: authentication issues or API errors.")
    public String selectCalendar(
            @ToolArg(description = "The ID or summary of the calendar to select (e.g., 'primary' or 'Work Calendar'). If empty, lists all available calendars.") String calendarInput
    ) {
        try {
            // Fetch the user's calendar list
            CalendarList calendarList = calendarService.calendarList().list().execute();
            List<CalendarListEntry> calendars = calendarList.getItems();

            if (calendars.isEmpty()) {
                return "No calendars found for this user.";
            }

            // If no input provided, list all calendars
            if (calendarInput == null || calendarInput.trim().isEmpty()) {
                StringBuilder response = new StringBuilder("Available calendars:\n");
                for (CalendarListEntry calendar : calendars) {
                    response.append("ID: ").append(calendar.getId())
                            .append(", Name: ").append(calendar.getSummary())
                            .append("\n");
                }
                response.append("Please provide a calendar ID or name to select.");
                return response.toString();
            }

            // Find calendar by ID or summary
            for (CalendarListEntry calendar : calendars) {
                if (calendar.getId().equalsIgnoreCase(calendarInput) ||
                        calendar.getSummary().equalsIgnoreCase(calendarInput)) {
                    return "Selected calendar: " + calendar.getSummary() + " (ID: " + calendar.getId() + ")";
                }
            }

            return "No calendar found with ID or name: " + calendarInput + ". Please try again.";
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error selecting calendar: " + e.getMessage());
        }
    }
}