package io.swagger.codegen.v3.generators.openapi;

import io.swagger.codegen.v3.CliOption;
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
    public static final String FLATTEN_SPEC = "flattenSpec";

    public static final String OPENAPI_FILENAME_DEFAULT_JSON = "openapi.json";

    private String outputFile = OPENAPI_FILENAME_DEFAULT_JSON;

    protected boolean flattenSpec = true;

    public OpenAPIGenerator() {
        super();
        outputFolder = "generated-code/openapi";

        cliOptions.add(new CliOption(OUTPUT_NAME,
                "output filename")
                .defaultValue(getOutputFile()));

        cliOptions.add(new CliOption(FLATTEN_SPEC,
            "flatten the spec by moving all inline complex schema to components, and add a ref in element",
            "boolean")
            .defaultValue(Boolean.TRUE.toString()));

        supportingFiles.add(new SupportingFile("README.md", "", "README.md"));
    }

    protected String getOutputFile() {
        return outputFile;
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
        final String outputString;
        if (flattenSpec) {
            outputString = Json.pretty(openAPI);
        } else {
            outputString = Json.pretty(this.unflattenedOpenAPI);
        }

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

        if (additionalProperties
            .containsKey(FLATTEN_SPEC) &&
            (
                !(additionalProperties.get(FLATTEN_SPEC) instanceof String) ||
                    !StringUtils.isBlank((String) additionalProperties.get(FLATTEN_SPEC))
            )
        ) {
            this.flattenSpec = Boolean.valueOf(additionalProperties.get(FLATTEN_SPEC).toString());
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

    @Override
    public boolean needsUnflattenedSpec() {
        return true;
    }
}
