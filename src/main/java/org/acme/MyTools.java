package org.acme.tools;

import io.quarkiverse.mcp.Tool;
import io.quarkiverse.mcp.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.util.DateTime;
import java.util.List;

@ApplicationScoped
public class MyTools {
    @Inject
    Calendar calendarService;

    @Tool("createEvent")
    public String createEvent(
            @ToolArg("summary") String summary,
            @ToolArg("location") String location,
            @ToolArg("description") String description,
            @ToolArg("start") String start,
            @ToolArg("end") String end
    ) {
        try {
            Event event = new Event();
            event.setSummary(summary);
            event.setLocation(location);
            event.setDescription(description);

            EventDateTime startTime = new EventDateTime()
                    .setDateTime(new DateTime(start))
                    .setTimeZone("Europe/Madrid");
            event.setStart(startTime);

            EventDateTime endTime = new EventDateTime()
                    .setDateTime(new DateTime(end))
                    .setTimeZone("Europe/Madrid");
            event.setEnd(endTime);

            calendarService.events().insert("primary", event).execute();
            return "Event created successfully";
        } catch (Exception e) {
            throw new RuntimeException("Error creating event: " + e.getMessage());
        }
    }

    @Tool("listEvents")
    public List<Event> listEvents(
            @ToolArg("calendarId") String calendarId,
            @ToolArg("timeMin") String timeMin,
            @ToolArg("timeMax") String timeMax
    ) {
        try {
            return calendarService.events().list(calendarId)
                    .setTimeMin(new DateTime(timeMin))
                    .setTimeMax(new DateTime(timeMax))
                    .execute()
                    .getItems();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving events: " + e.getMessage());
        }
    }
}