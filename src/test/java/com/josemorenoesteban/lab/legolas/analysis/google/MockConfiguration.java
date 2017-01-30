package com.josemorenoesteban.lab.legolas.analysis.google;

import static java.util.Arrays.asList;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

public class MockConfiguration implements Configuration {
    private final Vision vision = new MockVision();
    
    @Override
    public Vision client() {
        return vision;
    }

    private final Supplier<List<AnnotateImageResponse>> responses = () -> {
        AnnotateImageResponse response = new AnnotateImageResponse();
        response.setSafeSearchAnnotation(new SafeSearchAnnotation().setAdult("VERY_UNLIKELY"));
        response.setLabelAnnotations(asList(labelAnnotation("Label1", 0f)));
        return asList(response);
    };
    
    private EntityAnnotation labelAnnotation(final String description, final Float score) {
        return new EntityAnnotation()
                  .setDescription(description)
                  .setScore(score);
    }

    public class MockVision extends Vision {
        private MockVision() {
            super(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), new GoogleCredential().setAccessToken( "" ));
        }

        @Override
        public Images images() {
            return new Vision.Images() {
                @Override
                public Images.Annotate annotate(BatchAnnotateImagesRequest bair) throws IOException {
                    return new Images.Annotate(null) {
                        @Override
                        public BatchAnnotateImagesResponse execute() throws IOException {
                            BatchAnnotateImagesResponse batchResponse = new BatchAnnotateImagesResponse();
                            batchResponse.setResponses(responses.get());
                            return batchResponse;
                        }
                    };
                }
            };
        }
    }
}
