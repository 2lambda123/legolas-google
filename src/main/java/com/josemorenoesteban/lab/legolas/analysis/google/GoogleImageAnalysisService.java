package com.josemorenoesteban.lab.legolas.analysis.google;

import static com.josemorenoesteban.lab.legolas.analysis.google.Configuration.load;

import static java.util.Optional.ofNullable;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;
import static java.util.Arrays.asList;
import static java.util.stream.Stream.of;
import static java.util.AbstractMap.SimpleEntry;

import com.josemorenoesteban.lab.legolas.analysis.ImageAnalysisResult;
import com.josemorenoesteban.lab.legolas.analysis.ImageAnalysisService;

import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class GoogleImageAnalysisService implements ImageAnalysisService {
    private static final String             NAME        = "google-vision-api";
    private static final Map<String, Float> ADULT_SCORE = unmodifiableMap(of(
                new SimpleEntry<>("VERY_UNLIKELY", 0.00f),
                new SimpleEntry<>("UNLIKELY",      0.33f),
                new SimpleEntry<>("LIKELY",        0.66f),
                new SimpleEntry<>("VERY_LIKELY",   1.00f) )
            .collect(toMap( e -> e.getKey(), e -> e.getValue())) );
    
    private final Configuration conf = load();
    
    private final Function<Supplier<ByteBuffer>, Image> createImage = imageBytes ->
        new Image()
           .encodeContent( imageBytes.get().array() );
    
    private final Function<Image, AnnotateImageRequest> createRequest = image -> 
        new AnnotateImageRequest()
           .setImage( image );
    
    private final Function<AnnotateImageRequest, BatchAnnotateImagesRequest> batchRequest = request -> 
        new BatchAnnotateImagesRequest()
           .setRequests(asList(request));
    
    // TODO persar si utilizar optionals en los parametros
    private final Function<AnnotateImageRequest, AnnotateImageResponse> callService = request -> {
        try {
            return conf
                  .client()
                  .images()
                  .annotate(batchRequest.apply(request))
                  .execute()
                  .getResponses()
                  .get(0); // Sacar la recuperacion de elemento a una funcion
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };
    
    private Function<AnnotateImageResponse, Float> adultContentScore = result -> 
        ADULT_SCORE.get(result.getSafeSearchAnnotation().getAdult());

    private Function<AnnotateImageResponse, Map<String, Float>> labels = result -> 
        result
        .getLabelAnnotations()
        .stream()
        .collect(toMap(EntityAnnotation::getDescription, EntityAnnotation::getScore));   // check if is getConfidence() method
        
    private final Function<AnnotateImageResponse, ImageAnalysisResult> adaptor = result -> 
        new ImageAnalysisResult( labels.apply(result), adultContentScore.apply(result) );
    
    private final Function<Supplier<ByteBuffer>, ImageAnalysisResult> analyzer = 
        adaptor.compose(callService).compose(createRequest).compose(createImage);
    
    @Override
    public String name() { 
        return NAME; 
    }

    @Override
    public Optional<ImageAnalysisResult> analyse(final Supplier<ByteBuffer> imageBytes) { 
        return ofNullable(analyzer.apply(imageBytes)); 
    }
}
