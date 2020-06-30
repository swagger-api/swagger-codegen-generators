package io.swagger.codegen.v3.generators.kotlin;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.parameters.Parameter;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.SupportingFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.Handlebars;
import org.apache.commons.lang3.StringUtils;

import static java.util.Collections.singletonMap;

public class KotlinServerCodegen extends AbstractKotlinCodegen {

    public static final String DEFAULT_LIBRARY = Constants.KTOR;
    public static final String GENERATE_APIS = "generateApis";

    private static Logger LOGGER = LoggerFactory.getLogger(KotlinServerCodegen.class);
    private Boolean autoHeadFeatureEnabled = true;
    private Boolean conditionalHeadersFeatureEnabled = false;
    private Boolean hstsFeatureEnabled = true;
    private Boolean corsFeatureEnabled = false;
    private Boolean compressionFeatureEnabled = true;

    // This is here to potentially warn the user when an option is not supoprted by the target framework.
    private Map<String, List<String>> optionsSupportedPerFramework =
        singletonMap(Constants.KTOR,
                    Arrays.asList(
                        Constants.AUTOMATIC_HEAD_REQUESTS,
                        Constants.CONDITIONAL_HEADERS,
                        Constants.HSTS,
                        Constants.CORS,
                        Constants.COMPRESSION
                    ));

    /**
     * Constructs an instance of `KotlinServerCodegen`.
     */
    public KotlinServerCodegen() {
        super();

        artifactId = "kotlin-server";
        packageName = "io.swagger.server";
        outputFolder = "generated-code" + File.separator + "kotlin-server";
        modelTemplateFiles.put("model.mustache", ".kt");
        apiTemplateFiles.put("api.mustache", ".kt");
        apiPackage = packageName + ".apis";
        modelPackage = packageName + ".models";

        supportedLibraries.put("ktor", "ktor framework");

        // TODO: Configurable server engine. Defaults to netty in build.gradle.
        CliOption library = new CliOption(CodegenConstants.LIBRARY, "library template (sub-template) to use");
        library.setDefault(DEFAULT_LIBRARY);
        library.setEnum(supportedLibraries);

        cliOptions.add(library);

        addSwitch(Constants.AUTOMATIC_HEAD_REQUESTS, Constants.AUTOMATIC_HEAD_REQUESTS_DESC, getAutoHeadFeatureEnabled());
        addSwitch(Constants.CONDITIONAL_HEADERS, Constants.CONDITIONAL_HEADERS_DESC, getConditionalHeadersFeatureEnabled());
        addSwitch(Constants.HSTS, Constants.HSTS_DESC, getHstsFeatureEnabled());
        addSwitch(Constants.CORS, Constants.CORS_DESC, getCorsFeatureEnabled());
        addSwitch(Constants.COMPRESSION, Constants.COMPRESSION_DESC, getCompressionFeatureEnabled());
    }

    public Boolean getAutoHeadFeatureEnabled() {
        return autoHeadFeatureEnabled;
    }

    public void setAutoHeadFeatureEnabled(Boolean autoHeadFeatureEnabled) {
        this.autoHeadFeatureEnabled = autoHeadFeatureEnabled;
    }

    public Boolean getCompressionFeatureEnabled() {
        return compressionFeatureEnabled;
    }

    public void setCompressionFeatureEnabled(Boolean compressionFeatureEnabled) {
        this.compressionFeatureEnabled = compressionFeatureEnabled;
    }

    public Boolean getConditionalHeadersFeatureEnabled() {
        return conditionalHeadersFeatureEnabled;
    }

    public void setConditionalHeadersFeatureEnabled(Boolean conditionalHeadersFeatureEnabled) {
        this.conditionalHeadersFeatureEnabled = conditionalHeadersFeatureEnabled;
    }

    public Boolean getCorsFeatureEnabled() {
        return corsFeatureEnabled;
    }

    public void setCorsFeatureEnabled(Boolean corsFeatureEnabled) {
        this.corsFeatureEnabled = corsFeatureEnabled;
    }

    public String getHelp() {
        return "Generates a kotlin server.";
    }

    public Boolean getHstsFeatureEnabled() {
        return hstsFeatureEnabled;
    }

    public void setHstsFeatureEnabled(Boolean hstsFeatureEnabled) {
        this.hstsFeatureEnabled = hstsFeatureEnabled;
    }

    public String getName() {
        return "kotlin-server";
    }

    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
    * Handle typealias for schema of Array type
    */
    @Override
    public CodegenModel fromModel(String name, Schema schema, Map<String, Schema> allDefinitions) {
        CodegenModel codegenModel = super.fromModel(name, schema, allDefinitions);

        if (schema instanceof ArraySchema) {
            codegenModel.dataType = getTypeDeclaration(schema);
        }
        return codegenModel;
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Schema> schemas, OpenAPI openAPI) {
        // Ensure that the parameter names in the path are valid kotlin names
        // they need to match the names in the generated data class, this is required by ktor Location
        String modifiedPath = path;
        if (operation.getParameters() != null) {
            for (Parameter param : operation.getParameters()) {
                String pathParamName = param.getName();
                String kotlinName = toVarName(pathParamName);
                modifiedPath = modifiedPath.replace("{" + pathParamName + "}", "{" + kotlinName + "}");
            }
        }

        return super.fromOperation(modifiedPath, httpMethod, operation, schemas, openAPI);
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (!additionalProperties.containsKey(GENERATE_APIS)) {
            additionalProperties.put(GENERATE_APIS, true);
        }

        if (additionalProperties.containsKey(CodegenConstants.LIBRARY)) {
            this.setLibrary((String) additionalProperties.get(CodegenConstants.LIBRARY));
        } else {
            this.setLibrary(DEFAULT_LIBRARY);
        }

        if (additionalProperties.containsKey(Constants.AUTOMATIC_HEAD_REQUESTS)) {
            setAutoHeadFeatureEnabled(convertPropertyToBooleanAndWriteBack(Constants.AUTOMATIC_HEAD_REQUESTS));
        } else {
            additionalProperties.put(Constants.AUTOMATIC_HEAD_REQUESTS, getAutoHeadFeatureEnabled());
        }

        if (additionalProperties.containsKey(Constants.CONDITIONAL_HEADERS)) {
            setConditionalHeadersFeatureEnabled(convertPropertyToBooleanAndWriteBack(Constants.CONDITIONAL_HEADERS));
        } else {
            additionalProperties.put(Constants.CONDITIONAL_HEADERS, getConditionalHeadersFeatureEnabled());
        }

        if (additionalProperties.containsKey(Constants.HSTS)) {
            setHstsFeatureEnabled(convertPropertyToBooleanAndWriteBack(Constants.HSTS));
        } else {
            additionalProperties.put(Constants.HSTS, getHstsFeatureEnabled());
        }

        if (additionalProperties.containsKey(Constants.CORS)) {
            setCorsFeatureEnabled(convertPropertyToBooleanAndWriteBack(Constants.CORS));
        } else {
            additionalProperties.put(Constants.CORS, getCorsFeatureEnabled());
        }

        if (additionalProperties.containsKey(Constants.COMPRESSION)) {
            setCompressionFeatureEnabled(convertPropertyToBooleanAndWriteBack(Constants.COMPRESSION));
        } else {
            additionalProperties.put(Constants.COMPRESSION, getCompressionFeatureEnabled());
        }

        String packageFolder = (sourceFolder + File.separator + packageName).replace(".", File.separator);
        String resourcesFolder = "src/main/resources"; // not sure this can be user configurable.
        Boolean generateApis = additionalProperties.containsKey(GENERATE_APIS) && (Boolean)additionalProperties.get(GENERATE_APIS);

        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("Dockerfile.mustache", "", "Dockerfile"));

        supportingFiles.add(new SupportingFile("build.gradle.mustache", "", "build.gradle"));
        supportingFiles.add(new SupportingFile("settings.gradle.mustache", "", "settings.gradle"));
        supportingFiles.add(new SupportingFile("gradle.properties", "", "gradle.properties"));

        supportingFiles.add(new SupportingFile("AppMain.kt.mustache", packageFolder, "AppMain.kt"));
        supportingFiles.add(new SupportingFile("Configuration.kt.mustache", packageFolder, "Configuration.kt"));

        if (generateApis) {
            supportingFiles.add(new SupportingFile("Paths.kt.mustache", packageFolder, "Paths.kt"));
        }

        supportingFiles.add(new SupportingFile("application.conf.mustache", resourcesFolder, "application.conf"));
        supportingFiles.add(new SupportingFile("logback.xml", resourcesFolder, "logback.xml"));

        final String infrastructureFolder = (sourceFolder + File.separator + packageName + File.separator + "infrastructure").replace(".", File.separator);

        supportingFiles.add(new SupportingFile("ApiKeyAuth.kt.mustache", infrastructureFolder, "ApiKeyAuth.kt"));
    }

    @Override
    public void addHandlebarHelpers(Handlebars handlebars) {
        super.addHandlebarHelpers(handlebars);
        handlebars.registerHelpers(StringHelpers.class);
    }

    @Override
    public String getDefaultTemplateDir() {
        return "kotlin-server";
    }

    public static class Constants {
        public final static String KTOR = "ktor";
        public final static String AUTOMATIC_HEAD_REQUESTS = "featureAutoHead";
        public final static String AUTOMATIC_HEAD_REQUESTS_DESC = "Automatically provide responses to HEAD requests for existing routes that have the GET verb defined.";
        public final static String CONDITIONAL_HEADERS = "featureConditionalHeaders";
        public final static String CONDITIONAL_HEADERS_DESC = "Avoid sending content if client already has same content, by checking ETag or LastModified properties.";
        public final static String HSTS = "featureHSTS";
        public final static String HSTS_DESC = "Avoid sending content if client already has same content, by checking ETag or LastModified properties.";
        public final static String CORS = "featureCORS";
        public final static String CORS_DESC = "Ktor by default provides an interceptor for implementing proper support for Cross-Origin Resource Sharing (CORS). See enable-cors.org.";
        public final static String COMPRESSION = "featureCompression";
        public final static String COMPRESSION_DESC = "Adds ability to compress outgoing content using gzip, deflate or custom encoder and thus reduce size of the response.";
    }
}
