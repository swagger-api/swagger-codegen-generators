package io.swagger.codegen.v3.generators.features;

public interface CXFServerFeatures extends CXFFeatures, SwaggerFeatures, SpringFeatures, JbossFeature, BeanValidationExtendedFeatures, SwaggerUIFeatures {

    String USE_WADL_FEATURE = "useWadlFeature";

    String USE_MULTIPART_FEATURE = "useMultipartFeature";

    String ADD_CONSUMES_PRODUCES_JSON = "addConsumesProducesJson";

    String USE_ANNOTATED_BASE_PATH = "useAnnotatedBasePath";

    String GENERATE_NON_SPRING_APPLICATION = "generateNonSpringApplication";

    void setUseWadlFeature(boolean useWadlFeature);

    void setUseMultipartFeature(boolean useMultipartFeature);

    void setAddConsumesProducesJson(boolean addConsumesProducesJson);

    void setUseAnnotatedBasePath(boolean useAnnotatedBasePath);

    void setGenerateNonSpringApplication(boolean generateNonSpringApplication);

}
