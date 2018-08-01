package io.swagger.codegen.v3.generators.features;

public interface SpringFeatures extends BeanValidationFeatures {

    String GENERATE_SPRING_APPLICATION = "generateSpringApplication";

    String GENERATE_SPRING_BOOT_APPLICATION = "generateSpringBootApplication";

    String USE_SPRING_ANNOTATION_CONFIG = "useSpringAnnotationConfig";

    void setGenerateSpringApplication(boolean useGenerateSpringApplication);

    void setGenerateSpringBootApplication(boolean generateSpringBootApplication);

    void setUseSpringAnnotationConfig(boolean useSpringAnnotationConfig);


}
