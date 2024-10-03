package io.swagger.codegen.v3.generators.features;

public interface IgnoreUnknownJacksonFeatures {
    // Language supports generating JsonIgnoreProperties(ignoreUnknown = true)
    String IGNORE_UNKNOWN_JACKSON_ANNOTATION = "ignoreUnknownJacksonAnnotation";

    void setIgnoreUnknownJacksonAnnotation(boolean ignoreUnknownJacksonAnnotation);

    boolean isIgnoreUnknownJacksonAnnotation();
}
