package io.swagger.codegen.v3.generators.openapi;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class OpenAPIYamlGenerator extends OpenAPIGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIYamlGenerator.class);

    public static final String OPENAPI_FILENAME_DEFAULT_YAML = "openapi.yaml";

    private String outputFile = OPENAPI_FILENAME_DEFAULT_YAML;

    @Override
    public String getName() {
        return "openapi-yaml";
    }

    @Override
    public String getHelp() {
        return "Creates a static openapi.yaml file.";
    }

    @Override
    protected String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        this.openAPI = openAPI;
        try {
            final String outputString;
            if (flattenSpec) {
                outputString = Yaml.pretty(openAPI);
            } else {
                outputString = Yaml.pretty(this.unflattenedOpenAPI);
            }

            String outputFile = outputFolder + File.separator + this.outputFile;
            FileUtils.writeStringToFile(new File(outputFile), outputString);
            LOGGER.debug("wrote file to " + outputFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
