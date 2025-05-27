package org.acme;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestPost {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String body = "create event: Reunión importante, Oficina Central, Reunión para discutir proyecto, 2025-05-27T15:00:00+02:00, 2025-05-27T16:00:00+02:00";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/calendar/create-event"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Respuesta: " + response.body());
    }
}
