package org.acme;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.Desktop;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class GoogleAuthService {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/calendar");

    public Credential authorize() throws Exception {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/credentials.json")))
        );
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow, receiver, authEndpoint -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(authEndpoint));
                } catch (Exception e) {
                    System.err.println("Failed to open browser: " + e.getMessage());
                }
            } else {
                System.out.println("Please visit: " + authEndpoint);
            }
            System.out.println("Please visit: " + authEndpoint);
            org.jboss.logging.Logger.getLogger(GoogleAuthService.class).info("Auth endpoint URL: " + authEndpoint);
        });
        return app.authorize("user");
    }
}