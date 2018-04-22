package io.swagger.codegen.languages.java;

/**
 * Generates a Java JAXRS API using jersey.
 * Delegates to a service interface.
 */
public class JavaJerseyDIServerCodegen extends JavaJerseyServerCodegen {

    public JavaJerseyDIServerCodegen() {
        super();
        outputFolder = "generated-code/JavaJaxRS-DI";
    }

    @Override
    public String getName() {
        return "jaxrs-di";
    }

    @Override
    public String getHelp() {
        return "[WORK IN PROGRESS: generated code depends from Swagger v2 libraries] "
                + "Generates a Java JAXRS API delegating to a service interface.";
    }

    @Override
    public void addTemplateFiles() {
        super.apiTemplateFiles.remove("api.mustache");
        super.apiTemplateFiles.put("di/api.mustache", ".java");
        super.apiTemplateFiles.put("di/apiService.mustache", ".java");

        apiTestTemplateFiles.clear(); // TODO: add test template
        // clear model and api doc template as this codegen
        // does not support auto-generated markdown doc at the moment
        // TODO: add doc templates
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");
    }
}