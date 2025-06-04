package org.acme.tools;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import org.acme.tools.extra.CalendarOption;
import org.acme.tools.extra.CalendarSelection;

import java.io.IOException;
import java.util.List;

public class SelectCalendarTool {

    @Inject
    Calendar calendarService;

    @Tool(description = "Automatically selects a Google Calendar based on a user-provided name with fuzzy matching, returning the calendar ID for use in other tools. Lists all calendars if no name is provided or no match is found. Requires authenticated Google Calendar API access.")
    public CalendarSelection selectCalendar(
            @ToolArg(description = "The name of the calendar to select (e.g., 'ai test'). Supports fuzzy matching. If empty, lists all available calendars.") String calendarName
    ) {
        try {
            // Fetch the user's calendar list
            CalendarList calendarList = calendarService.calendarList().list().execute();
            List<CalendarListEntry> calendars = calendarList.getItems();
            List<CalendarOption> calendarOptions = calendars.stream()
                    .map(c -> new CalendarOption(c.getId(), c.getSummary() != null ? c.getSummary() : "Untitled Calendar"))
                    .toList();

            if (calendars.isEmpty()) {
                return new CalendarSelection("No calendars found for this user.", null, calendarOptions);
            }

            // If no input provided, list all calendars
            if (calendarName == null || calendarName.trim().isEmpty()) {
                return new CalendarSelection("Please provide a calendar name to select.", null, calendarOptions);
            }

            // Normalize input for fuzzy matching
            String normalizedInput = calendarName.toLowerCase().trim();

            // Find best matching calendar
            CalendarListEntry bestMatch = null;
            int minDistance = Integer.MAX_VALUE;
            for (CalendarListEntry calendar : calendars) {
                String calendarSummary = calendar.getSummary() != null ? calendar.getSummary().toLowerCase() : "";

                // Check for exact match, ID match, or substring match
                if (calendarSummary.equals(normalizedInput) ||
                        calendar.getId().equalsIgnoreCase(normalizedInput) ||
                        calendarSummary.contains(normalizedInput)) {
                    bestMatch = calendar;
                    break; // Prefer exact or substring match
                }

                // Calculate Levenshtein distance for fuzzy matching
                int distance = calculateLevenshteinDistance(normalizedInput, calendarSummary);
                if (distance < minDistance && distance <= Math.max(3, normalizedInput.length() / 2)) { // Allow reasonable variation
                    minDistance = distance;
                    bestMatch = calendar;
                }
            }

            // Return result
            if (bestMatch != null) {
                return new CalendarSelection(
                        "Selected calendar: " + bestMatch.getSummary() + " (ID: " + bestMatch.getId() + ")",
                        bestMatch.getId(),
                        calendarOptions
                );
            } else {
                return new CalendarSelection(
                        "No calendar found matching '" + calendarName + "'. Please provide a closer matching name.",
                        null,
                        calendarOptions
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Google Calendar API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error selecting calendar: " + e.getMessage());
        }
    }

    // Simple Levenshtein distance implementation for fuzzy matching
    private int calculateLevenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[len1][len2];
    }
}