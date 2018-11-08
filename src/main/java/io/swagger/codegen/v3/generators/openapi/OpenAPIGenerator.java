package io.swagger.codegen.v3.generators.openapi;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.templates.HandlebarTemplateEngine;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class OpenAPIGenerator extends DefaultCodegenConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIGenerator.class);

    public static final String OUTPUT_NAME = "outputFile";

    public static final String OPENAPI_FILENAME_DEFAULT_JSON = "openapi.json";

    protected String outputFile = OPENAPI_FILENAME_DEFAULT_JSON;

    public OpenAPIGenerator() {
        super();
        outputFolder = "generated-code/openapi";

        cliOptions.add(new CliOption(OUTPUT_NAME,
                "output filename")
                .defaultValue(OPENAPI_FILENAME_DEFAULT_JSON));
        supportingFiles.add(new SupportingFile("README.md", "", "README.md"));
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.DOCUMENTATION;
    }

    @Override
    public String getName() {
        return "openapi";
    }

    @Override
    public String getHelp() {
        return "Creates a static openapi.json file.";
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);
        String outputString = Json.pretty(openAPI);

        try {
            String outputFile = outputFolder + File.separator + this.outputFile;
            FileUtils.writeStringToFile(new File(outputFile), outputString);
            LOGGER.debug("wrote file to " + outputFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void processOpts() {
        super.processOpts();

        embeddedTemplateDir = templateDir = getTemplateDir();

        if (additionalProperties.containsKey(OUTPUT_NAME) && !StringUtils.isBlank((String) additionalProperties.get(OUTPUT_NAME))) {
            setOutputFile((String) additionalProperties.get(OUTPUT_NAME));
        }
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public String escapeQuotationMark(String input) {
        // just return the original string
        return input;
    }

    @Override
    public String getArgumentsLocation() {
        return null;
    }

    @Override
    public String getDefaultTemplateDir() {
        return "openapi";
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        // just return the original string
        return input;
    }

    @Override
    protected void setTemplateEngine() {
        templateEngine = new HandlebarTemplateEngine(this);
    }
}
