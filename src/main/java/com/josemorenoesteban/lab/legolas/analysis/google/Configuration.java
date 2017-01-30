package com.josemorenoesteban.lab.legolas.analysis.google;

import static java.util.Objects.requireNonNull;

import com.google.api.services.vision.v1.Vision;

import java.util.ServiceLoader;

public interface Configuration {
    static Configuration instance() { 
        final ServiceLoader<Configuration> loader = ServiceLoader.load(Configuration.class);
        return requireNonNull(loader.iterator().next(), 
                              "No Google configuration class found");
    }
    
    Vision client();
}
