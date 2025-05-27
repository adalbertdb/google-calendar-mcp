package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.ProducerTemplate;
import jakarta.inject.Inject;

@ApplicationScoped // Standard Quarkus annotation
public class CalendarMcpService {

    @Inject
    ProducerTemplate producerTemplate;

    public String processMcpRequest(String input) {
        try {
            return producerTemplate.requestBody("direct:createEvent", input, String.class);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}