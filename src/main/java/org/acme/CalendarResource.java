package org.acme;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.inject.Inject;

@Path("/calendar")
public class CalendarResource {

    @Inject
    CalendarMcpService calendarMcpService;

    @POST
    @Path("/create-event")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String createEvent(String eventText) {
        return calendarMcpService.processMcpRequest(eventText);
    }
}
