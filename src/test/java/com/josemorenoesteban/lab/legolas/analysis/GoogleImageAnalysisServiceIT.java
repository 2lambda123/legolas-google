package com.josemorenoesteban.lab.legolas.analysis;

import static java.nio.file.Files.readAllBytes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

public class GoogleImageAnalysisServiceIT {
    private ImageAnalysisService service;   // The SUT 
    
    @Before
    public void setup() {
        service = ImageAnalysisService.byName("google-vision-api");
    }
    
    @Test
    public void canLoadServiceByName() {
        assertNotNull( service );
    }
    
    @Test
    public void canAnalyseChucho() {
        Optional<ImageAnalysisResult> result  = service.analyse(() -> forImage.apply("/chucho.jpg"));
        assertTrue(result.isPresent());
        assertEquals(0f, result.get().adultContentScore(), 0);
        assertEquals(1, result.get().labels().size());
    }

    // Helper functions
    
    private final Function<String, File> fromClassLoader = filename -> {
        try {
            return new File(this.getClass().getResource(filename).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    };
    
    private final Function<File, ByteBuffer> buffer = file -> {
        try {
            return ByteBuffer.wrap(readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };
    
    private final Function<String, ByteBuffer> forImage = name -> buffer.compose(fromClassLoader).apply(name);
}
