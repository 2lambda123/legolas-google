package com.josemorenoesteban.lab.legolas.analysis.google;

import com.josemorenoesteban.lab.legolas.analysis.Environment;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;

public class DefaultConfiguration implements Configuration {
    private static final String GOOGLE_ACCESS_TOKEN_ENV = "GOOGLE_ACCESS_TOKEN";

    private final Vision vision;

    public DefaultConfiguration() {
        String token = Environment.value.apply(GOOGLE_ACCESS_TOKEN_ENV).orElseThrow(() -> new RuntimeException(""));
        GoogleCredential credential = new GoogleCredential().setAccessToken( token );
        token = null;   // here removes references to token to be garbage collected asap
        this.vision = new Vision(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential);
    }
    
    @Override
    public Vision client() {
        return vision;
    }
}
