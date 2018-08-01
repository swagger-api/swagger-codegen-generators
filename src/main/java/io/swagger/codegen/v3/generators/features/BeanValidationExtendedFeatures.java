package io.swagger.codegen.v3.generators.features;

public interface BeanValidationExtendedFeatures {

    // Language (implementing Client/Server) supports automatic BeanValidation (1.1)
    String USE_BEANVALIDATION_FEATURE = "useBeanValidationFeature";

    void setUseBeanValidationFeature(boolean useBeanValidationFeature);

}
