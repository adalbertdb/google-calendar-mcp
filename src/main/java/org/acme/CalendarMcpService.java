package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.ProducerTemplate;
import jakarta.inject.Inject;

import java.io.PrintWriter;
import java.io.StringWriter;

@ApplicationScoped
public class CalendarMcpService {

    @Inject
    ProducerTemplate producerTemplate;

    public String processMcpRequest(String input) {
        try {
            return producerTemplate.requestBody("direct:createEvent", input, String.class);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return "Error: " + sw.toString(); // <-- muestra todo el error
        }
    }
}
