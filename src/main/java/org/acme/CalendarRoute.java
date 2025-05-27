package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CalendarRoute extends RouteBuilder {
    @Override
    public void configure() {
        from("direct:createEvent")
                .log("Processing: ${body}")
                .doTry()
                .process(exchange -> {
                    String input = exchange.getIn().getBody(String.class);
                    System.out.println(">>> Recibido: " + input);
                    String[] parts = input.split(", ");
                    if (parts.length != 5 || !parts[0].startsWith("create event: ")) {
                        throw new IllegalArgumentException("Invalid input format. Expected: create event: summary, location, description, start, end");
                    }

                    String summary = parts[0].replace("create event: ", "").trim();
                    String location = parts[1].trim();
                    String description = parts[2].trim();
                    String startTime = parts[3].trim();
                    String endTime = parts[4].trim();

                    com.google.api.services.calendar.model.Event event = new com.google.api.services.calendar.model.Event();
                    event.setSummary(summary);
                    event.setLocation(location);
                    event.setDescription(description);

                    com.google.api.services.calendar.model.EventDateTime start = new com.google.api.services.calendar.model.EventDateTime();
                    start.setDateTime(new com.google.api.client.util.DateTime(ZonedDateTime.parse(startTime).toInstant().toEpochMilli()));
                    start.setTimeZone("Europe/Madrid");
                    event.setStart(start);

                    com.google.api.services.calendar.model.EventDateTime end = new com.google.api.services.calendar.model.EventDateTime();
                    end.setDateTime(new com.google.api.client.util.DateTime(ZonedDateTime.parse(endTime).toInstant().toEpochMilli()));
                    end.setTimeZone("Europe/Madrid");
                    event.setEnd(end);

                    exchange.getIn().setBody(event);
                })
                .process(exchange -> {
                    Object body = exchange.getIn().getBody();
                    System.out.println(">>> Tipo de body antes de insertar en Google Calendar: " + (body != null ? body.getClass().getName() : "null"));
                })
                .to("google-calendar://events/insert?calendarId=primary&inBody=content")
                .setBody(simple("Event created successfully"))
                .doCatch(Exception.class)
                .setBody(simple("Error creating event: ${exception.message}"))
                .end();

    }
}
