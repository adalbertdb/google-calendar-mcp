package org.acme;

import com.google.api.services.calendar.Calendar;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

public class GoogleCalendarConfig {

    @Inject
    GoogleAuthService authService;

    @Produces
    @Singleton
    public Calendar calendarService() throws Exception {
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                authService.authorize())
                .setApplicationName("Google Calendar MCP Server")
                .build();
    }
}
