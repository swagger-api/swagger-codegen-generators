package io.swagger.codegen.v3.generators.openapi;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class OpenAPIYamlGenerator extends DefaultCodegenConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIYamlGenerator.class);

    public static final String OUTPUT_NAME = "outputFile";

    public static final String SWAGGER_FILENAME_DEFAULT_YAML = "openapi.yaml";

    protected String outputFile = SWAGGER_FILENAME_DEFAULT_YAML;


    public OpenAPIYamlGenerator() {
        super();
        outputFolder = "generated-code/openapi";

        cliOptions.add(new CliOption(OUTPUT_NAME,
                "output filename")
                .defaultValue(SWAGGER_FILENAME_DEFAULT_YAML));

        supportingFiles.add(new SupportingFile("README.md", "", "README.md"));
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.DOCUMENTATION;
    }

    @Override
    public String getName() {
        return "openapi-yaml";
    }

    @Override
    public String getHelp() {
        return "Creates a static openapi.yaml file.";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        embeddedTemplateDir = templateDir = getTemplateDir();

        if (additionalProperties.containsKey(OUTPUT_NAME) && !StringUtils.isBlank((String) additionalProperties.get(OUTPUT_NAME))) {
            setOutputFile((String) additionalProperties.get(OUTPUT_NAME));
        }
    }

    @Override
    public String getDefaultTemplateDir() {
        return "openapi";
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);
        try {
            String valueAsString = Yaml.pretty(openAPI);
            String outputFile = outputFolder + File.separator + this.outputFile;
            FileUtils.writeStringToFile(new File(outputFile), valueAsString);
            LOGGER.debug("wrote file to " + outputFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String escapeQuotationMark(String input) {
        // just return the original string
        return input;
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        // just return the original string
        return input;
    }
}
