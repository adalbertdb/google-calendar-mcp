package org.acme;
import org.apache.camel.ProducerTemplate;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/calendar")
public class CalendarResource {

    @Inject
    ProducerTemplate producerTemplate;

    @POST
    @Path("/create-event")
    public String createEvent(String eventJson) {
        producerTemplate.sendBody("direct:createEvent", eventJson);
        return "Evento enviado a crear";
    }
}
