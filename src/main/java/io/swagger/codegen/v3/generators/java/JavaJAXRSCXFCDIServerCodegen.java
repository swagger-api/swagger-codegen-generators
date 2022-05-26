package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.features.BeanValidationFeatures;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * Generates a Java JAXRS Server according to JAXRS 2.0 specification, assuming
 * an Apache CXF runtime and a Java EE runtime with CDI enabled. Similar to the
 * original JAXRS generator, this creates API and Service classes in
 * /src/gen/java and a sample ServiceImpl in /src/main/java. The API uses CDI to
 * get an instance of ServiceImpl that implements the Service interface.
 */
public class JavaJAXRSCXFCDIServerCodegen extends JavaJAXRSSpecServerCodegen implements BeanValidationFeatures {

    /**
     * Default constructor
     */
    public JavaJAXRSCXFCDIServerCodegen() {
        useBeanValidation = true;
        outputFolder = "generated-code/JavaJaxRS-CXF-CDI";
        artifactId = "swagger-jaxrs-cxf-cdi-server";
        sourceFolder = "src" + File.separator + "gen" + File.separator + "java";

        // Use standard types
        typeMapping.put("DateTime", "java.util.Date");

        cliOptions.add(CliOption.newBoolean(USE_BEANVALIDATION, "Use BeanValidation API annotations"));
    }

    @Override
    public String getDefaultTemplateDir() {
        return JAXRS_TEMPLATE_DIRECTORY_NAME + "/cxf-cdi";
    }

    @Override
    public String getName() {
        return "jaxrs-cxf-cdi";
    }

    @Override
    public void processOpts() {

        super.processOpts();

        importMapping.put("Valid", "javax.validation.Valid");

        // Three API templates to support CDI injection
        apiTemplateFiles.put("apiService.mustache", ".java");
        apiTemplateFiles.put("apiServiceImpl.mustache", ".java");

        if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
            this.setUseBeanValidation(convertPropertyToBoolean(USE_BEANVALIDATION));
        }

        if (useBeanValidation) {
            writePropertyBack(USE_BEANVALIDATION, useBeanValidation);
        }

        supportingFiles.clear(); // Don't need extra files provided by
                                 // AbstractJAX-RS & Java Codegen

        // writeOptional means these files are only written if they don't
        // already exist

        // POM
        writeOptional(outputFolder, new SupportingFile("pom.mustache", "", "pom.xml"));

        // RestApplication into src/main/java
        writeOptional(outputFolder, new SupportingFile("RestApplication.mustache", (implFolder + '/' + invokerPackage).replace(".", "/"), "RestApplication.java"));

        // Make CDI work in containers with implicit archive scanning disabled
        writeOptional(outputFolder, new SupportingFile("beans.mustache", "src/main/webapp/WEB-INF", "beans.xml"));
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        if (useBeanValidation) {
            final boolean importValidAnnotation = property.getIsContainer() && !property.getIsPrimitiveType() && !property.getIsEnum()
                || !property.getIsContainer() && !property.getIsPrimitiveType();
            if (importValidAnnotation) {
                model.imports.add("Valid");
            }
        }

        // Reinstate JsonProperty
        model.imports.add("JsonProperty");
    }

    @Override
    public String getHelp() {
        return "[WORK IN PROGRESS: generated code depends from Swagger v2 libraries] "
                + "Generates a Java JAXRS Server according to JAXRS 2.0 specification, assuming an "
                + "Apache CXF runtime and a Java EE runtime with CDI enabled.";
    }

    public void setUseBeanValidation(boolean useBeanValidation) {
        this.useBeanValidation = useBeanValidation;
    }
}
