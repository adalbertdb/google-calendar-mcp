package org.acme;

import io.quarkiverse.mcp.server.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.util.DateTime;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class MyTools {
    @Inject
    Calendar calendarService;

    @Tool(description = "hello")
    String hello(@ToolArg(description = "name") String name) {
        return "Hello from tool!"+name;
    }

    @Tool(description = "createEvent")
    public String createEvent(
            @ToolArg(description = "summary") String summary,
            @ToolArg(description = "location") String location,
            @ToolArg(description = "description") String description,
            @ToolArg(description = "start") String start,
            @ToolArg(description = "end") String end
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
}

//    @Tool("listEvents")
//    public String listEvents(
//            @ToolArg("calendarId") String calendarId,
//            @ToolArg("timeMin") String timeMin,
//            @ToolArg("timeMax") String timeMax
//    ) {
//        try {
//            return calendarService.events().list(calendarId)
//                    .setTimeMin(new DateTime(timeMin))
//                    .setTimeMax(new DateTime(timeMax))
//                    .execute()
//                    .getItems();
//        } catch (Exception e) {
//            throw new RuntimeException("Error retrieving events");
//        }
//    }
//}