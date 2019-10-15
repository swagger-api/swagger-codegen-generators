package io.swagger.codegen.v3.generators.java;

import com.github.jknack.handlebars.Lambda;
import com.google.common.collect.ImmutableMap;
import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.features.BeanValidationFeatures;
import io.swagger.codegen.v3.generators.features.OptionalFeatures;
import io.swagger.codegen.v3.generators.handlebars.lambda.*;
import io.swagger.codegen.v3.utils.URLPathUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static io.swagger.codegen.v3.CodegenConstants.HAS_ENUMS_EXT_NAME;
import static io.swagger.codegen.v3.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;
import static java.util.Collections.singletonList;

/**
 * @author Franz See <franz@see.net.ph> <https://see.net.ph>
 */
public class MicronautCodegen extends AbstractJavaCodegen implements BeanValidationFeatures, OptionalFeatures {

    private static Logger LOGGER = LoggerFactory.getLogger(MicronautCodegen.class);
    private static final String TITLE = "title";
    private static final String CONFIG_PACKAGE = "configPackage";
    private static final String BASE_PACKAGE = "basePackage";
    private static final String USE_TAGS = "useTags";
    private static final String IMPLICIT_HEADERS = "implicitHeaders";

    private String title = "swagger-petstore";
    private String configPackage = "io.swagger.configuration";
    private String basePackage = "io.swagger";
    private boolean useTags = false;
    private boolean useBeanValidation = true;
    private boolean implicitHeaders = false;
    private boolean useOptional = false;

    @SuppressWarnings("unused")
    public MicronautCodegen() {
        super();
        init();
    }

    private void init() {
        outputFolder = "generated-code/javaMicronaut";
        apiPackage = "io.swagger.api";
        modelPackage = "io.swagger.model";
        invokerPackage = "io.swagger.api";
        artifactId = "swagger-micronaut";

        additionalProperties.put(CONFIG_PACKAGE, configPackage);
        additionalProperties.put(BASE_PACKAGE, basePackage);

        // micronaut uses the jackson lib
        additionalProperties.put("jackson", "true");

        cliOptions.add(new CliOption(TITLE, "server title name or client service name"));
        cliOptions.add(new CliOption(CONFIG_PACKAGE, "configuration package for generated code"));
        cliOptions.add(new CliOption(BASE_PACKAGE, "base package (invokerPackage) for generated code"));
        cliOptions.add(CliOption.newBoolean(USE_TAGS, "use tags for creating interface and controller classnames"));
        cliOptions.add(CliOption.newBoolean(USE_BEANVALIDATION, "Use BeanValidation API annotations"));
        cliOptions.add(CliOption.newBoolean(IMPLICIT_HEADERS, "Use of @ApiImplicitParams for headers."));
        cliOptions.add(CliOption.newBoolean(USE_OPTIONAL,
                "Use Optional container for optional parameters"));

        supportedLibraries.put(DEFAULT_LIBRARY, "Java Micronaut Server application.");
        setLibrary(DEFAULT_LIBRARY);

        CliOption library = new CliOption(CodegenConstants.LIBRARY, "library template (sub-template) to use");
        library.setDefault(DEFAULT_LIBRARY);
        library.setEnum(supportedLibraries);
        library.setDefault(DEFAULT_LIBRARY);
        cliOptions.add(library);
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "micronaut";
    }

    @Override
    public String getHelp() {
        return "Generates a Java Micronaut Server application.";
    }

    @Override
    public void processOpts() {
        setUseOas2(false);
        additionalProperties.put(CodegenConstants.USE_OAS2, false);

        if (additionalProperties.containsKey("httpMethod")) {
            String httpMethod = (String) additionalProperties.get("httpMethod");
            String httpMethodNormalCase = Character.toUpperCase(httpMethod.charAt(0)) + httpMethod.substring(1);
            additionalProperties.put("httpMethodNormalCase", httpMethodNormalCase);
        }


        // set invokerPackage as basePackage
        if (additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            this.setBasePackage((String) additionalProperties.get(CodegenConstants.INVOKER_PACKAGE));
            additionalProperties.put(BASE_PACKAGE, basePackage);
            LOGGER.info("Set base package to invoker package (" + basePackage + ")");
        }

        super.processOpts();

        if (StringUtils.isBlank(templateDir)) {
            embeddedTemplateDir = templateDir = getTemplateDir();
        }

        // clear model and api doc template as this codegen
        // does not support auto-generated markdown doc at the moment
        //TODO: add doc templates
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        if (additionalProperties.containsKey(TITLE)) {
            this.setTitle((String) additionalProperties.get(TITLE));
        }

        if (additionalProperties.containsKey(CONFIG_PACKAGE)) {
            this.setConfigPackage((String) additionalProperties.get(CONFIG_PACKAGE));
        }

        if (additionalProperties.containsKey(BASE_PACKAGE)) {
            this.setBasePackage((String) additionalProperties.get(BASE_PACKAGE));
        }

        if (additionalProperties.containsKey(USE_TAGS)) {
            this.setUseTags(Boolean.valueOf(additionalProperties.get(USE_TAGS).toString()));
        }

        if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
            this.setUseBeanValidation(convertPropertyToBoolean(USE_BEANVALIDATION));
        }

        if (additionalProperties.containsKey(USE_OPTIONAL)) {
            this.setUseOptional(convertPropertyToBoolean(USE_OPTIONAL));
        }

        if (useBeanValidation) {
            writePropertyBack(USE_BEANVALIDATION, useBeanValidation);
        }

        if (additionalProperties.containsKey(IMPLICIT_HEADERS)) {
            this.setImplicitHeaders(Boolean.valueOf(additionalProperties.get(IMPLICIT_HEADERS).toString()));
        }

        if (useOptional) {
            writePropertyBack(USE_OPTIONAL, useOptional);
        }

        supportingFiles.add(new SupportingFile("pom.mustache", "", "pom.xml"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("mvnw", "", "mvnw"));
        supportingFiles.add(new SupportingFile("mvnw.cmd", "", "mvnw.cmd"));
        supportingFiles.add(new SupportingFile("unsupportedOperationExceptionHandler.mustache",
                (sourceFolder + File.separator + configPackage).replace(".", File.separator), "UnsupportedOperationExceptionHandler.java"));
        supportingFiles.add(new SupportingFile("mainApplication.mustache", (sourceFolder + File.separator).replace(".", File.separator), "MainApplication.java"));

        addHandlebarsLambdas(additionalProperties);
    }

    private void addHandlebarsLambdas(Map<String, Object> objs) {
        Map<String, Lambda> lambdas = new ImmutableMap.Builder<String, Lambda>()
                .put("lowercase", new LowercaseLambda().generator(this))
                .put("uppercase", new UppercaseLambda())
                .put("titlecase", new TitlecaseLambda())
                .put("camelcase", new CamelCaseLambda().generator(this))
                .put("camelcase_param", new CamelCaseLambda().generator(this).escapeAsParamName(true))
                .put("indented", new IndentedLambda())
                .put("indented_8", new IndentedLambda(8, " "))
                .put("indented_12", new IndentedLambda(12, " "))
                .put("indented_16", new IndentedLambda(16, " "))
                .put("capitalise", new CapitaliseLambda())
                .put("escapeDoubleQuote", new EscapeDoubleQuotesLambda())
                .put("removeLineBreak", new RemoveLineBreakLambda())
                .build();

        if (objs.containsKey("lambda")) {
            LOGGER.warn("An property named 'lambda' already exists. Mustache lambdas renamed from 'lambda' to '_lambda'. " +
                    "You'll likely need to use a custom template, " +
                    "see https://github.com/swagger-api/swagger-codegen#modifying-the-client-library-format. ");
            objs.put("_lambda", lambdas);
        } else {
            objs.put("lambda", lambdas);
        }
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        if(!useTags) {
            String basePath = resourcePath;
            if (basePath.startsWith("/")) {
                basePath = basePath.substring(1);
            }
            int pos = basePath.indexOf("/");
            if (pos > 0) {
                basePath = basePath.substring(0, pos);
            }

            if (basePath.equals("")) {
                basePath = "default";
            } else {
                co.subresourceOperation = !co.path.isEmpty();
            }
            List<CodegenOperation> opList = operations.computeIfAbsent(basePath, k -> new ArrayList<>());
            opList.add(co);
            co.baseName = basePath;
        } else {
            super.addOperationToGroup(tag, resourcePath, operation, co, operations);
        }
    }

    @Override
    public String getArgumentsLocation() {
        return null;
    }

    @Override
    public String getDefaultTemplateDir() {
        return "JavaMicronaut";
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);

        if(!additionalProperties.containsKey(TITLE)) {
            // From the title, compute a reasonable name for the package and the API
            String title = openAPI.getInfo().getTitle();

            // Drop any API suffix
            if (title != null) {
                title = title.trim().replace(" ", "-");
                if (title.toUpperCase().endsWith("API")) {
                    title = title.substring(0, title.length() - 3);
                }

                this.title = camelize(sanitizeName(title), true);
            }
            additionalProperties.put(TITLE, this.title);
        }

        final URL urlInfo = URLPathUtil.getServerURL(openAPI);
        String port = "8080"; // Default value for a JEE Server
        if ( urlInfo != null && urlInfo.getPort() != 0) {
            port = String.valueOf(urlInfo.getPort());
        }

        this.additionalProperties.put("serverPort", port);
        if (openAPI.getPaths() != null) {
            for (String pathname : openAPI.getPaths().keySet()) {
                PathItem pathItem = openAPI.getPaths().get(pathname);
                final List<Operation> operations = pathItem.readOperations();
                for (Operation operation : operations) {
                    if (operation.getTags() != null) {
                        List<Map<String, String>> tags = new ArrayList<>();
                        for (String tag : operation.getTags()) {
                            Map<String, String> value = new HashMap<>();
                            value.put("tag", tag);
                            value.put("hasMore", "true");
                            tags.add(value);
                        }
                        if (tags.size() > 0) {
                            tags.get(tags.size() - 1).remove("hasMore");
                        }
                        if (operation.getTags().size() > 0) {
                            String tag = operation.getTags().get(0);
                            operation.setTags(singletonList(tag));
                        }
                        operation.addExtension("x-tags", tags);
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        @SuppressWarnings("unchecked") Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        if (operations != null) {
            @SuppressWarnings("unchecked") List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for (final CodegenOperation operation : ops) {
                List<CodegenResponse> responses = operation.responses;
                if (responses != null) {
                    for (final CodegenResponse resp : responses) {
                        if ("0".equals(resp.code)) {
                            resp.code = "200";
                        }
                        doDataTypeAssignment(resp.dataType, new DataTypeAssigner() {
                            @Override
                            public void setReturnType(final String returnType) {
                                resp.dataType = returnType;
                            }

                            @Override
                            public void setReturnContainer(final String returnContainer) {
                                resp.containerType = returnContainer;
                            }
                        });
                    }
                }

                doDataTypeAssignment(operation.returnType, new DataTypeAssigner() {

                    @Override
                    public void setReturnType(final String returnType) {
                        operation.returnType = returnType;
                    }

                    @Override
                    public void setReturnContainer(final String returnContainer) {
                        operation.returnContainer = returnContainer;
                    }
                });

                if(implicitHeaders){
                    removeHeadersFromAllParams(operation.allParams);
                    removeHeadersFromContents(operation.contents);
                }
            }
        }

        return objs;
    }

    private interface DataTypeAssigner {
        void setReturnType(String returnType);
        void setReturnContainer(String returnContainer);
    }

    /**
     *
     * @param returnType The return type that needs to be converted
     * @param dataTypeAssigner An object that will assign the data to the respective fields in the model.
     */
    private void doDataTypeAssignment(final String returnType, DataTypeAssigner dataTypeAssigner) {
        if (returnType == null) {
            dataTypeAssigner.setReturnType("Void");
        } else if (returnType.startsWith("List")) {
            int end = returnType.lastIndexOf(">");
            if (end > 0) {
                dataTypeAssigner.setReturnType(returnType.substring("List<".length(), end).trim());
                dataTypeAssigner.setReturnContainer("List");
            }
        } else if (returnType.startsWith("Map")) {
            int end = returnType.lastIndexOf(">");
            if (end > 0) {
                dataTypeAssigner.setReturnType(returnType.substring("Map<".length(), end).split(",")[1].trim());
                dataTypeAssigner.setReturnContainer("Map");
            }
        } else if (returnType.startsWith("Set")) {
            int end = returnType.lastIndexOf(">");
            if (end > 0) {
                dataTypeAssigner.setReturnType(returnType.substring("Set<".length(), end).trim());
                dataTypeAssigner.setReturnContainer("Set");
            }
        }
    }

    /**
     * This method removes header parameters from the list of parameters and also
     * corrects last allParams hasMore state.
     * @param allParams list of all parameters
     */
    private void removeHeadersFromAllParams(List<CodegenParameter> allParams) {
        if(allParams.isEmpty()){
            return;
        }
        final ArrayList<CodegenParameter> copy = new ArrayList<>(allParams);
        allParams.clear();

        for(CodegenParameter p : copy){
            if(!getBooleanValue(p, CodegenConstants.IS_HEADER_PARAM_EXT_NAME)){
                allParams.add(p);
            }
        }
        allParams.get(allParams.size()-1).getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, Boolean.FALSE);
    }

    private void removeHeadersFromContents(List<CodegenContent> contents) {
        if(contents == null || contents.isEmpty()){
            return;
        }
        for (final CodegenContent codegenContent : contents) {
            final List<CodegenParameter> parameters = codegenContent.getParameters();
            if (parameters == null || parameters.isEmpty()) {
                continue;
            }
            final List<CodegenParameter> filteredParameters = parameters.stream()
                    .filter(codegenParameter -> !getBooleanValue(codegenParameter, CodegenConstants.IS_HEADER_PARAM_EXT_NAME))
                    .collect(Collectors.toList());
            parameters.clear();
            parameters.addAll(filteredParameters);
            parameters.get(parameters.size() - 1).getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, Boolean.FALSE);
        }
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        @SuppressWarnings("unchecked") List<CodegenSecurity> authMethods = (List<CodegenSecurity>) objs.get("authMethods");
        if (authMethods != null) {
            for (CodegenSecurity authMethod : authMethods) {
                authMethod.name = camelize(sanitizeName(authMethod.name), true);
            }
        }
        return objs;
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultApi";
        }
        name = sanitizeName(name);
        return camelize(name) + "Api";
    }

    @Override
    public String toApiTestFilename(String name) {
        return toApiName(name) + "ControllerTest";
    }

    public String toBooleanGetter(String name) {
        return getterAndSetterCapitalize(name);
    }

    @SuppressWarnings("WeakerAccess")
    public void setTitle(String title) {
        this.title = title;
    }

    @SuppressWarnings("WeakerAccess")
    public void setConfigPackage(String configPackage) {
        this.configPackage = configPackage;
    }

    @SuppressWarnings("WeakerAccess")
    public void setBasePackage(String configPackage) {
        this.basePackage = configPackage;
    }

    @SuppressWarnings("WeakerAccess")
    public void setUseTags(boolean useTags) {
        this.useTags = useTags;
    }

    @SuppressWarnings("WeakerAccess")
    public void setImplicitHeaders(boolean implicitHeaders) {
        this.implicitHeaders = implicitHeaders;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);

        if ("null".equals(property.example)) {
            property.example = null;
        }

        //Add imports for Jackson
        boolean isEnum = getBooleanValue(model, IS_ENUM_EXT_NAME);
        if (!Boolean.TRUE.equals(isEnum)) {
            model.imports.add("JsonProperty");
            boolean hasEnums = getBooleanValue(model, HAS_ENUMS_EXT_NAME);
            if (Boolean.TRUE.equals(hasEnums)) {
                model.imports.add("JsonValue");
            }
        } else { // enum class
            //Needed imports for Jackson's JsonCreator
            if (additionalProperties.containsKey("jackson")) {
                model.imports.add("JsonCreator");
            }
        }
        if (model.discriminator != null && model.discriminator.getPropertyName().equals(property.baseName)) {
            property.vendorExtensions.put("x-is-discriminator-property", true);

            //model.imports.add("JsonTypeId");
        }
    }

    @Override
    public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
        objs = super.postProcessModelsEnum(objs);

        //Add imports for Jackson
        @SuppressWarnings("unchecked") List<Map<String, String>> imports = (List<Map<String, String>>)objs.get("imports");
        @SuppressWarnings("unchecked") List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            @SuppressWarnings("unchecked") Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");
            // for enum model
            boolean isEnum = getBooleanValue(cm, IS_ENUM_EXT_NAME);
            if (Boolean.TRUE.equals(isEnum) && cm.allowableValues != null) {
                cm.imports.add(importMapping.get("JsonValue"));
                Map<String, String> item = new HashMap<>();
                item.put("import", importMapping.get("JsonValue"));
                imports.add(item);
            }
        }

        return objs;
    }

    public void setUseBeanValidation(boolean useBeanValidation) {
        this.useBeanValidation = useBeanValidation;
    }

    @Override
    public void setUseOptional(boolean useOptional) {
        this.useOptional = useOptional;
    }
}
