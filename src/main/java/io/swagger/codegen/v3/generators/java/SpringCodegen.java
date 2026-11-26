package io.swagger.codegen.v3.generators.java;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenContent;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenResponse;
import io.swagger.codegen.v3.CodegenSecurity;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.features.BeanValidationFeatures;
import io.swagger.codegen.v3.generators.features.NotNullAnnotationFeatures;
import io.swagger.codegen.v3.generators.features.OptionalFeatures;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.codegen.v3.utils.URLPathUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static io.swagger.codegen.v3.CodegenConstants.HAS_ENUMS_EXT_NAME;
import static io.swagger.codegen.v3.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class SpringCodegen extends AbstractJavaCodegen implements BeanValidationFeatures, OptionalFeatures, NotNullAnnotationFeatures {
    static Logger LOGGER = LoggerFactory.getLogger(SpringCodegen.class);
    public static final String DEFAULT_LIBRARY = "spring-boot";
    public static final String TITLE = "title";
    public static final String CONFIG_PACKAGE = "configPackage";
    public static final String BASE_PACKAGE = "basePackage";
    public static final String INTERFACE_ONLY = "interfaceOnly";
    public static final String DELEGATE_PATTERN = "delegatePattern";
    public static final String SINGLE_CONTENT_TYPES = "singleContentTypes";
    public static final String ASYNC = "async";
    public static final String RESPONSE_WRAPPER = "responseWrapper";
    public static final String USE_TAGS = "useTags";
    public static final String SPRING_MVC_LIBRARY = "spring-mvc";
    public static final String SPRING_CLOUD_LIBRARY = "spring-cloud";
    public static final String SPRING_BOOT_3_LIBRARY = "spring-boot3";
    public static final String SPRING_BOOT_4_LIBRARY = "spring-boot4";
    public static final String IMPLICIT_HEADERS = "implicitHeaders";
    public static final String SWAGGER_DOCKET_CONFIG = "swaggerDocketConfig";
    public static final String TARGET_OPENFEIGN = "generateForOpenFeign";
    public static final String DEFAULT_INTERFACES = "defaultInterfaces";
    public static final String SPRING_BOOT_VERSION = "springBootVersion";
    public static final String SPRING_BOOT3_VERSION = "springBoot3Version";
    public static final String SPRING_BOOT3_JAVA_VERSION = "springBoot3JavaVersion";
    public static final String SPRING_BOOT4_VERSION = "springBoot4Version";
    public static final String SPRING_BOOT4_JAVA_VERSION = "springBoot4JavaVersion";
    public static final String SPRINGDOC_VERSION = "springdocVersion";
    /** Template flag: generated code targets Jackson 3 ({@code tools.jackson.*}). Set only for {@link #SPRING_BOOT_4_LIBRARY}. */
    public static final String JACKSON3 = "jackson3";
    public static final String SPRING_BOOT_VERSION_2 = "springBootV2";
    public static final String DATE_PATTERN = "datePattern";
    public static final String DATE_TIME_PATTERN = "dateTimePattern";
    public static final String THROWS_EXCEPTION = "throwsException";

    public static final String VALIDATION_MODE_OPTION = "validationMode";
    public static final String VALIDATION_MODE_LEGACY = "legacy";
    public static final String VALIDATION_MODE_LEGACY_NULLABLE = "legacyNullable";
    public static final String VALIDATION_MODE_STRICT = "strict";
    public static final String VALIDATION_MODE_LOOSE = "loose";

    protected String title = "swagger-petstore";
    protected String configPackage = "io.swagger.configuration";
    protected String basePackage = "io.swagger";
    protected boolean interfaceOnly = false;
    protected boolean delegatePattern = false;
    protected boolean delegateMethod = false;
    protected boolean singleContentTypes = false;
    protected boolean java8 = false;
    protected boolean java11 = false;
    protected boolean async = false;
    protected String responseWrapper = "";
    protected boolean useTags = false;
    protected boolean useBeanValidation = true;
    protected boolean implicitHeaders = false;
    protected boolean swaggerDocketConfig = false;
    protected boolean useOptional = false;
    protected boolean openFeign = false;
    protected boolean defaultInterfaces = true;
    protected String springBootVersion = "2.1.16.RELEASE";
    protected String springBoot3Version = "3.4.2";
    protected String springBoot3JavaVersion = "21";
    protected String springBoot4Version = "4.1.3";
    protected String springBoot4JavaVersion = "17";
    protected boolean throwsException = false;
    private boolean notNullJacksonAnnotation = false;
    protected String validationMode = VALIDATION_MODE_STRICT;

    public SpringCodegen() {
        super();
        outputFolder = "generated-code/javaSpring";
        apiPackage = "io.swagger.api";
        modelPackage = "io.swagger.model";
        invokerPackage = "io.swagger.api";
        artifactId = "swagger-spring";

        additionalProperties.put(CONFIG_PACKAGE, configPackage);
        additionalProperties.put(BASE_PACKAGE, basePackage);

        // spring uses the jackson lib
        additionalProperties.put("jackson", "true");

        cliOptions.add(new CliOption(TITLE, "server title name or client service name"));
        cliOptions.add(new CliOption(CONFIG_PACKAGE, "configuration package for generated code"));
        cliOptions.add(new CliOption(BASE_PACKAGE, "base package (invokerPackage) for generated code"));
        cliOptions.add(CliOption.newBoolean(INTERFACE_ONLY, "Whether to generate only API interface stubs without the server files."));
        cliOptions.add(CliOption.newBoolean(DELEGATE_PATTERN, "Whether to generate the server files using the delegate pattern"));
        cliOptions.add(CliOption.newBoolean(SINGLE_CONTENT_TYPES, "Whether to select only one produces/consumes content-type by operation."));
        cliOptions.add(CliOption.newBoolean(ASYNC, "use async Callable controllers"));
        cliOptions.add(new CliOption(RESPONSE_WRAPPER, "wrap the responses in given type (Future,Callable,CompletableFuture,ListenableFuture,DeferredResult,HystrixCommand,RxObservable,RxSingle or fully qualified type)"));
        cliOptions.add(CliOption.newBoolean(USE_TAGS, "use tags for creating interface and controller classnames"));
        cliOptions.add(CliOption.newBoolean(USE_BEANVALIDATION, "Use BeanValidation API annotations"));
        cliOptions.add(CliOption.newBoolean(IMPLICIT_HEADERS, "Use of @ApiImplicitParams for headers."));
        cliOptions.add(CliOption.newBoolean(SWAGGER_DOCKET_CONFIG, "Generate Spring Swagger Docket configuration class."));
        cliOptions.add(CliOption.newBoolean(USE_OPTIONAL,
                "Use Optional container for optional parameters"));
        cliOptions.add(CliOption.newBoolean(TARGET_OPENFEIGN,"Generate for usage with OpenFeign (instead of feign)"));
        cliOptions.add(CliOption.newBoolean(DEFAULT_INTERFACES, "Generate default implementations for interfaces").defaultValue("true"));
        cliOptions.add(CliOption.newBoolean(THROWS_EXCEPTION, "Throws Exception in operation methods").defaultValue("false"));
        cliOptions.add(CliOption.newBoolean(DATE_PATTERN, "use pattern for date parameters").defaultValue("true"));
        cliOptions.add(CliOption.newBoolean(DATE_TIME_PATTERN, "use pattern for date time parameters").defaultValue("true"));

        supportedLibraries.put(DEFAULT_LIBRARY, "Spring-boot Server application using the SpringFox integration.");
        supportedLibraries.put(SPRING_BOOT_3_LIBRARY, "Spring-boot v3 Server application.");
        supportedLibraries.put(SPRING_BOOT_4_LIBRARY, "Spring-boot v4 Server application (Spring Framework 7, Jackson 3, Java 17+). Java 8+ code shape (default interface methods) is always enabled.");
        supportedLibraries.put(SPRING_MVC_LIBRARY, "Spring-MVC Server application using the SpringFox integration.");
        supportedLibraries.put(SPRING_CLOUD_LIBRARY, "Spring-Cloud-Feign client with Spring-Boot auto-configured settings.");
        setLibrary(DEFAULT_LIBRARY);

        CliOption library = new CliOption(CodegenConstants.LIBRARY, "library template (sub-template) to use");
        library.setEnum(supportedLibraries);
        library.setDefault(DEFAULT_LIBRARY);
        cliOptions.add(library);

        cliOptions.add(versionOption(SPRING_BOOT_VERSION, "Spring boot version", null, "1.5.22.RELEASE", "2.1.7.RELEASE"));
        cliOptions.add(versionOption(SPRING_BOOT3_VERSION, "Spring boot 3 version", null, "3.3.4", "3.4.2"));
        cliOptions.add(versionOption(SPRING_BOOT3_JAVA_VERSION, "Spring boot 3 Java version", null, "17", "21"));
        cliOptions.add(versionOption(SPRING_BOOT4_VERSION, "Spring boot 4 version", springBoot4Version, "4.0.9", springBoot4Version));
        cliOptions.add(versionOption(SPRING_BOOT4_JAVA_VERSION, "Spring boot 4 Java version", springBoot4JavaVersion, "17", "21"));

        CliOption cliOption = createValidationModeCliOption();
        cliOptions.add(cliOption);
    }

    private static CliOption createValidationModeCliOption() {
        CliOption cliOption = new CliOption(VALIDATION_MODE_OPTION, "Validation mode to apply");
        cliOption.setDefault(VALIDATION_MODE_STRICT);
        Map<String, String> validationModeOptions = new HashMap<String, String>();
        validationModeOptions.put(VALIDATION_MODE_STRICT, "Use Helper JsonNullable/NotUndefined on required+nullable fields, @NotNull on required, jackson validation on default");
        validationModeOptions.put(VALIDATION_MODE_LOOSE, "Use Helper JsonNullable/NotUndefined on required+nullable fields, @NotNull on required, no validation on default");
        validationModeOptions.put(VALIDATION_MODE_LEGACY, "Apply @NotNull on required fields");
        validationModeOptions.put(VALIDATION_MODE_LEGACY_NULLABLE, "Apply @NotNull when nullable is not defined or false, if useNullableForNotNull=false Apply @NotNull on required fields");
        cliOption.setEnum(validationModeOptions);
        return cliOption;
    }

    @Override
    protected void addCodegenContentParameters(CodegenOperation codegenOperation, List<CodegenContent> codegenContents) {
        for (CodegenContent content : codegenContents) {
            addParameters(content, codegenOperation.headerParams);
            addParameters(content, codegenOperation.queryParams);
            addParameters(content, codegenOperation.pathParams);
            addParameters(content, codegenOperation.cookieParams);
            if (content.getIsForm()) {
                addParameters(content, codegenOperation.formParams);
            } else {
                addParameters(content, codegenOperation.bodyParams);
            }
        }
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "spring";
    }

    @Override
    public String getHelp() {
        return "Generates a Java SpringBoot Server application using the SpringFox integration.";
    }

    @Override
    public void processOpts() {
        // Spring Boot 4 needs Java 17+, so the Java 8+ code shape (default interface methods) is always on.
        if (isSpringBoot4Library()) {
            additionalProperties.put(JAVA8_MODE, true);
        }

        // Process java8 option before common java ones to change the default dateLibrary to java8.
        if (additionalProperties.containsKey(JAVA8_MODE)) {
            this.setJava8(Boolean.parseBoolean(additionalProperties.get(JAVA8_MODE).toString()));
        }

        if (additionalProperties.containsKey(DATE_LIBRARY) && additionalProperties.get(DATE_LIBRARY).toString().startsWith("java8")) {
            this.setJava8(true);
        }

        if (this.java8) {
            additionalProperties.put("javaVersion", "1.8");
            additionalProperties.put("jdk8", true);
            if (!additionalProperties.containsKey(DATE_LIBRARY)) {
                setDateLibrary("java8");
            }
        }

        if (additionalProperties.containsKey(JAVA11_MODE)) {
            this.setJava11(Boolean.parseBoolean(additionalProperties.get(JAVA11_MODE).toString()));
        }
        if (this.java11) {
            additionalProperties.put("javaVersion", "11");
            additionalProperties.put("jdk11", "true");
        }

        additionalProperties.put("isJava8or11", this.java8 || this.java11);
        this.defaultInterfaces = this.java8 || this.java11;

        // set invokerPackage as basePackage
        if (additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            this.setBasePackage((String) additionalProperties.get(CodegenConstants.INVOKER_PACKAGE));
            additionalProperties.put(BASE_PACKAGE, basePackage);
            LOGGER.info("Set base package to invoker package (" + basePackage + ")");
        }

        if (isSpringBoot3OrLater()) {
            setDateLibrary("java8");
            setJakarta(true);
            additionalProperties.put(JAKARTA, jakarta);
        }

        // Jackson 3 only on the spring-boot4 path; every other library keeps Jackson 2 output byte-for-byte.
        if (isSpringBoot4Library()) {
            additionalProperties.put(JACKSON3, true);
            additionalProperties.put(JACKSON_DATABIND_PACKAGE, JACKSON3_DATABIND_PACKAGE);
        } else {
            additionalProperties.remove(JACKSON3);
            additionalProperties.put(JACKSON_DATABIND_PACKAGE, JACKSON2_DATABIND_PACKAGE);
        }

        super.processOpts();

        // clear model and api doc template as this codegen
        // does not support auto-generated markdown doc at the moment
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        if (additionalProperties.containsKey(TITLE)) {
            this.setTitle((String) additionalProperties.get(TITLE));
        }

        if (additionalProperties.containsKey(VALIDATION_MODE_OPTION)) {
            this.setValidationMode((String) additionalProperties.get(VALIDATION_MODE_OPTION));
        }
        additionalProperties.put("is" + validationMode.substring(0, 1).toUpperCase() + validationMode.substring(1) + "Validation", true);

        if (additionalProperties.containsKey(CONFIG_PACKAGE)) {
            this.setConfigPackage((String) additionalProperties.get(CONFIG_PACKAGE));
        }

        if (additionalProperties.containsKey(BASE_PACKAGE)) {
            this.setBasePackage((String) additionalProperties.get(BASE_PACKAGE));
        }

        if (additionalProperties.containsKey(INTERFACE_ONLY)) {
            this.setInterfaceOnly(Boolean.parseBoolean(additionalProperties.get(INTERFACE_ONLY).toString()));
        }

        if (additionalProperties.containsKey(THROWS_EXCEPTION)) {
            this.setThrowsException(Boolean.parseBoolean(additionalProperties.get(THROWS_EXCEPTION).toString()));
            additionalProperties.put(THROWS_EXCEPTION, throwsException);
        }

        if (additionalProperties.containsKey(DELEGATE_PATTERN)) {
            this.setDelegatePattern(Boolean.parseBoolean(additionalProperties.get(DELEGATE_PATTERN).toString()));
        }

        if (additionalProperties.containsKey(SINGLE_CONTENT_TYPES)) {
            this.setSingleContentTypes(Boolean.parseBoolean(additionalProperties.get(SINGLE_CONTENT_TYPES).toString()));
        }

        if (additionalProperties.containsKey(ASYNC)) {
            this.setAsync(Boolean.parseBoolean(additionalProperties.get(ASYNC).toString()));
        }

        if (additionalProperties.containsKey(RESPONSE_WRAPPER)) {
            this.setResponseWrapper((String) additionalProperties.get(RESPONSE_WRAPPER));
        }

        if (additionalProperties.containsKey(USE_TAGS)) {
            this.setUseTags(Boolean.parseBoolean(additionalProperties.get(USE_TAGS).toString()));
        }

        if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
            this.setUseBeanValidation(convertPropertyToBoolean(USE_BEANVALIDATION));
            writePropertyBack(USE_BEANVALIDATION, this.useBeanValidation);
        }

        if (additionalProperties.containsKey(USE_OPTIONAL)) {
            this.setUseOptional(convertPropertyToBoolean(USE_OPTIONAL));
        }

        if (additionalProperties.containsKey(TARGET_OPENFEIGN)) {
            this.setOpenFeign(convertPropertyToBoolean(TARGET_OPENFEIGN));
        }

        if (additionalProperties.containsKey(DEFAULT_INTERFACES)) {
            this.setDefaultInterfaces(Boolean.parseBoolean(additionalProperties.get(DEFAULT_INTERFACES).toString()));
        }
        additionalProperties.put(DEFAULT_INTERFACES, this.defaultInterfaces);

        this.springBootVersion = readVersion(SPRING_BOOT_VERSION, this.springBootVersion);
        if (springBootVersion.startsWith("2")) {
            additionalProperties.put(SPRING_BOOT_VERSION_2, true);
            this.setOpenFeign(true);
        }
        this.springBoot3Version = readVersion(SPRING_BOOT3_VERSION, this.springBoot3Version);
        this.springBoot3JavaVersion = readVersion(SPRING_BOOT3_JAVA_VERSION, this.springBoot3JavaVersion);
        this.springBoot4Version = readVersion(SPRING_BOOT4_VERSION, this.springBoot4Version);
        this.springBoot4JavaVersion = readVersion(SPRING_BOOT4_JAVA_VERSION, this.springBoot4JavaVersion);

        if (isSpringBoot4Library() && !additionalProperties.containsKey(SPRINGDOC_VERSION)) {
            additionalProperties.put(SPRINGDOC_VERSION, springdocVersionFor(this.springBoot4Version));
        }

        if (useBeanValidation) {
            writePropertyBack(USE_BEANVALIDATION, useBeanValidation);
            if (VALIDATION_MODE_LOOSE.equals(validationMode) || VALIDATION_MODE_STRICT.equals(validationMode)) {
                writeOptional(outputFolder, new SupportingFile("NotUndefined.mustache",
                    packageDir(sourceFolder, configPackage), "NotUndefined.java"), true);
                writeOptional(outputFolder, new SupportingFile("NotUndefinedValidator.mustache",
                    packageDir(sourceFolder, configPackage), "NotUndefinedValidator.java"), true);
            }
        }

        if (additionalProperties.containsKey(IMPLICIT_HEADERS)) {
            this.setImplicitHeaders(Boolean.parseBoolean(additionalProperties.get(IMPLICIT_HEADERS).toString()));
        }

        if (additionalProperties.containsKey(SWAGGER_DOCKET_CONFIG)) {
            this.setSwaggerDocketConfig(Boolean.parseBoolean(additionalProperties.get(SWAGGER_DOCKET_CONFIG).toString()));
        }

        typeMapping.put("file", "Resource");
        typeMapping.put("binary", "Resource");
        importMapping.put("Resource", "org.springframework.core.io.Resource");

        if (useOptional) {
            writePropertyBack(USE_OPTIONAL, useOptional);
        }

        if (this.interfaceOnly && this.delegatePattern) {
            if (this.java8) {
                this.delegateMethod = true;
                additionalProperties.put("delegate-method", true);
            } else {
                throw new IllegalArgumentException(
                        String.format("Can not generate code with `%s` and `%s` true while `%s` is false.",
                                DELEGATE_PATTERN, INTERFACE_ONLY, JAVA8_MODE));
            }
        }

        supportingFiles.add(new SupportingFile("pom.mustache", "", "pom.xml"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));

        if (!this.interfaceOnly) {
            if (isSpringBoot3OrLater()) {
                useOas2 = false;
                additionalProperties.remove("threetenbp");
                additionalProperties.put(JAKARTA, jakarta = true);
                apiTestTemplateFiles.clear();
                supportingFiles.add(new SupportingFile("homeController.mustache", packageDir(sourceFolder, configPackage), "HomeController.java"));
                supportingFiles.add(new SupportingFile("openAPISpringBoot.mustache", packageDir(sourceFolder, basePackage), "OpenAPISpringBoot.java"));
                supportingFiles.add(new SupportingFile("swaggerUiConfiguration.mustache", packageDir(sourceFolder, configPackage), "SwaggerUiConfiguration.java"));
                supportingFiles.add(new SupportingFile("application.mustache", packageDir("src.main.resources"), "application.properties"));
            }
            if (isDefaultLibrary()) {
                apiTestTemplateFiles.clear();
                supportingFiles.add(new SupportingFile("homeController.mustache",
                        packageDir(sourceFolder, configPackage), "HomeController.java"));
                supportingFiles.add(new SupportingFile("swagger2SpringBoot.mustache",
                        packageDir(sourceFolder, basePackage), "Swagger2SpringBoot.java"));
                supportingFiles.add(new SupportingFile("RFC3339DateFormat.mustache",
                        packageDir(sourceFolder, basePackage), "RFC3339DateFormat.java"));
                supportingFiles.add(new SupportingFile("swaggerUiConfiguration.mustache",
                        packageDir(sourceFolder, configPackage), "SwaggerUiConfiguration.java"));
                supportingFiles.add(new SupportingFile("application.mustache",
                        packageDir("src.main.resources"), "application.properties"));
            }
            if (isSpringMvcLibrary()) {
                forceOas2();
                supportingFiles.add(new SupportingFile("webApplication.mustache",
                        packageDir(sourceFolder, configPackage), "WebApplication.java"));
                supportingFiles.add(new SupportingFile("webMvcConfiguration.mustache",
                        packageDir(sourceFolder, configPackage), "WebMvcConfiguration.java"));
                supportingFiles.add(new SupportingFile("swaggerUiConfiguration.mustache",
                        packageDir(sourceFolder, configPackage), "SwaggerUiConfiguration.java"));
                supportingFiles.add(new SupportingFile("RFC3339DateFormat.mustache",
                        packageDir(sourceFolder, configPackage), "RFC3339DateFormat.java"));
                supportingFiles.add(new SupportingFile("application.properties.mustache",
                        packageDir("src.main.resources"), "swagger.properties"));
            }
            if (isSpringCloudLibrary()) {
                forceOas2();
                supportingFiles.add(new SupportingFile("apiKeyRequestInterceptor.mustache",
                        packageDir(sourceFolder, configPackage), "ApiKeyRequestInterceptor.java"));
                supportingFiles.add(new SupportingFile("clientConfiguration.mustache",
                        packageDir(sourceFolder, configPackage), "ClientConfiguration.java"));
                apiTemplateFiles.put("apiClient.mustache", "Client.java");
                if (!additionalProperties.containsKey(SINGLE_CONTENT_TYPES)) {
                    additionalProperties.put(SINGLE_CONTENT_TYPES, "true");
                    this.setSingleContentTypes(true);
                }
                if (additionalProperties.containsKey(CodegenConstants.GENERATE_API_TESTS)
                    && Boolean.parseBoolean(additionalProperties.get(CodegenConstants.GENERATE_API_TESTS).toString())) {

                    apiTestTemplateFiles.clear();
                    supportingFiles.add(new SupportingFile("application-test.mustache",
                        packageDir("src.test.resources"), "application.yml"));
                    supportingFiles.add(new SupportingFile("TestUtils.mustache",
                        packageDir(testFolder, basePackage), "TestUtils.java"));
                    supportingFiles.add(new SupportingFile("Application.mustache",
                        packageDir(testFolder, basePackage), "Application.java"));
                    }

            } else {
                apiTemplateFiles.put("apiController.mustache", "Controller.java");
                supportingFiles.add(new SupportingFile("apiException.mustache",
                        packageDir(sourceFolder, apiPackage), "ApiException.java"));
                supportingFiles.add(new SupportingFile("apiResponseMessage.mustache",
                        packageDir(sourceFolder, apiPackage), "ApiResponseMessage.java"));
                supportingFiles.add(new SupportingFile("notFoundException.mustache",
                        packageDir(sourceFolder, apiPackage), "NotFoundException.java"));
                supportingFiles.add(new SupportingFile("apiOriginFilter.mustache",
                        packageDir(sourceFolder, apiPackage), "ApiOriginFilter.java"));
                if (!isSpringBoot3OrLater()) {
                    supportingFiles.add(new SupportingFile("swaggerDocumentationConfig.mustache",
                        packageDir(sourceFolder, configPackage), "SwaggerDocumentationConfig.java"));
                }
                supportingFiles.add(new SupportingFile("LocalDateConverter.mustache",
                    packageDir(sourceFolder, configPackage), "LocalDateConverter.java"));
                supportingFiles.add(new SupportingFile("LocalDateTimeConverter.mustache",
                    packageDir(sourceFolder, configPackage), "LocalDateTimeConverter.java"));
            }
        } else if ( this.swaggerDocketConfig && !isSpringCloudLibrary() && !isSpringBoot3OrLater()) {
            supportingFiles.add(new SupportingFile("swaggerDocumentationConfig.mustache",
                    packageDir(sourceFolder, configPackage), "SwaggerDocumentationConfig.java"));
        }

        if (this.interfaceOnly) {
            apiTestTemplateFiles.clear();
        }
        if ("threetenbp".equals(dateLibrary)) {
            supportingFiles.add(new SupportingFile("customInstantDeserializer.mustache",
                    packageDir(sourceFolder, configPackage), "CustomInstantDeserializer.java"));
            if (isDefaultLibrary() || isSpringCloudLibrary()) {
                supportingFiles.add(new SupportingFile("jacksonConfiguration.mustache",
                        packageDir(sourceFolder, configPackage), "JacksonConfiguration.java"));
            }
        }

        if ((!this.delegatePattern && (this.java8 || this.java11)) || this.delegateMethod) {
            additionalProperties.put("jdk8-no-delegate", true);
        }


        if (this.delegatePattern && !this.delegateMethod) {
            additionalProperties.put("isDelegate", "true");
            apiTemplateFiles.put("apiDelegate.mustache", "Delegate.java");
        }

        if (this.java8) {
            if (this.async) {
                additionalProperties.put(RESPONSE_WRAPPER, "CompletableFuture");
            }
        } else if (this.async) {
            additionalProperties.put(RESPONSE_WRAPPER, "Callable");
        }

        if(this.openFeign){
            additionalProperties.put("isOpenFeign", true);
        }

        // Some well-known Spring or Spring-Cloud response wrappers
        switch (this.responseWrapper) {
            case "Future":
            case "Callable":
            case "CompletableFuture":
                additionalProperties.put(RESPONSE_WRAPPER, "java.util.concurrent" + this.responseWrapper);
                break;
            case "ListenableFuture":
                additionalProperties.put(RESPONSE_WRAPPER, "org.springframework.util.concurrent.ListenableFuture");
                break;
            case "DeferredResult":
                additionalProperties.put(RESPONSE_WRAPPER, "org.springframework.web.context.request.async.DeferredResult");
                break;
            case "HystrixCommand":
                additionalProperties.put(RESPONSE_WRAPPER, "com.netflix.hystrix.HystrixCommand");
                break;
            case "RxObservable":
                additionalProperties.put(RESPONSE_WRAPPER, "rx.Observable");
                break;
            case "RxSingle":
                additionalProperties.put(RESPONSE_WRAPPER, "rx.Single");
                break;
            default:
                break;
        }

        // add lambda for mustache templates
        additionalProperties.put("lambdaEscapeDoubleQuote", new Mustache.Lambda() {
            @Override
            public void execute(Template.Fragment fragment, Writer writer) throws IOException {
                writer.write(fragment.execute().replaceAll("\"", Matcher.quoteReplacement("\\\"")));
            }
        });
        additionalProperties.put("lambdaRemoveLineBreak", new Mustache.Lambda() {
            @Override
            public void execute(Template.Fragment fragment, Writer writer) throws IOException {
                writer.write(fragment.execute().replaceAll("\\r|\\n", ""));
            }
        });

        if (!this.java8 || !this.defaultInterfaces) {
            additionalProperties.put("fullController", true);
        }
    }

    @Override
    public CodegenProperty fromProperty(String name, Schema propertySchema) {
        CodegenProperty codegenProperty = super.fromProperty(name, propertySchema);
        if (propertySchema != null && propertySchema.get$ref() != null) {
            Schema refSchema = OpenAPIUtil.getSchemaFromRefSchema(propertySchema, this.openAPI);
            if (refSchema != null  && !isObjectSchema(refSchema) && !(refSchema instanceof ArraySchema) && !(refSchema instanceof MapSchema) && refSchema.getEnum() == null) {
                setSchemaProperties(name, codegenProperty, refSchema);
                processPropertySchemaTypes(name, codegenProperty, refSchema);
            }
        }
        return codegenProperty;
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        if((isDefaultLibrary() || isSpringMvcLibrary()) && !useTags) {
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
            List<CodegenOperation> opList = operations.computeIfAbsent(basePath, k -> new ArrayList<CodegenOperation>());
            opList.add(co);
            co.baseName = basePath;
        } else {
            super.addOperationToGroup(tag, resourcePath, operation, co, operations);
        }
    }

    @Override
    public String getArgumentsLocation() {
        return "/arguments/spring.yaml";
    }

    @Override
    public String getDefaultTemplateDir() {
        return "JavaSpring";
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
        if (urlInfo != null && urlInfo.getPort() > 0) {
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
                        if (!tags.isEmpty()) {
                            tags.get(tags.size() - 1).remove("hasMore");
                        }
                        if (!operation.getTags().isEmpty()) {
                            String tag = operation.getTags().get(0);
                            operation.setTags(Collections.singletonList(tag));
                        }
                        operation.addExtension("x-tags", tags);
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        if (operations != null) {
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for (final CodegenOperation operation : ops) {
                List<CodegenResponse> responses = operation.responses;
                if (responses != null) {
                    for (final CodegenResponse resp : responses) {
                        if ("0".equals(resp.code)) {
                            resp.code = "200";
                        }
                        if (resp.baseType == null) {
                            // set vendorExtensions.x-java-is-response-void to true as baseType is set to "Void"
                            resp.vendorExtensions.put("x-java-is-response-void", true);
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
                if (operation.examples != null){
                    for (Map<String, String> example : operation.examples)
                    {
                        for (Map.Entry<String, String> entry : example.entrySet())
                        {
                            // Replace " with \", \r, \n with \\r, \\n
                            String val = entry.getValue().replace("\"", "\\\"")
                                .replace("\r","\\r")
                                .replace("\n","\\n");
                            entry.setValue(val);
                        }
                    }
                }
            }
        }

        return objs;
    }

    @Override
    public void setNotNullJacksonAnnotation(boolean notNullJacksonAnnotation) {
        this.notNullJacksonAnnotation = notNullJacksonAnnotation;
    }

    @Override
    public boolean isNotNullJacksonAnnotation() {
        return notNullJacksonAnnotation;
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
    private void doDataTypeAssignment(String returnType, SpringCodegen.DataTypeAssigner dataTypeAssigner) {
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
                String mapTypes = returnType.substring("Map<".length(), end);
                String mapKey = mapTypes.split(",")[0];
                String mapValue = mapTypes.substring(mapKey.length() + 1).trim();
                dataTypeAssigner.setReturnType(mapValue);
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

    /**
     * Forces Oas2 specification, use it when Oas3 is not supported.
     */
    private void forceOas2() {
        setUseOas2(true);
        additionalProperties.put(CodegenConstants.USE_OAS2, true);
        importMapping.put("ApiModelProperty", "io.swagger.annotations.ApiModelProperty");
        importMapping.put("ApiModel", "io.swagger.annotations.ApiModel");
        importMapping.remove("Schema");
    }

    private boolean isSpringCloudLibrary() {
        return library.equals(SPRING_CLOUD_LIBRARY);
    }

    private boolean isSpringMvcLibrary() {
        return library.equals(SPRING_MVC_LIBRARY);
    }

    private boolean isDefaultLibrary() {
        return library.equals(DEFAULT_LIBRARY);
    }
    private boolean isSpringBoot3Library() {
        return library.equals(SPRING_BOOT_3_LIBRARY);
    }

    private boolean isSpringBoot4Library() {
        return library.equals(SPRING_BOOT_4_LIBRARY);
    }

    /** spring-boot3 and spring-boot4 share the springdoc / jakarta / java8-date setup. */
    private boolean isSpringBoot3OrLater() {
        return isSpringBoot3Library() || isSpringBoot4Library();
    }

    /** Joins folder/package segments and turns package dots into path separators, e.g. ("src/main/java", "io.swagger.api"). */
    private static String packageDir(String... segments) {
        return String.join(File.separator, segments).replace(".", File.separator);
    }

    /** Takes the version from additionalProperties when the user set one, else keeps the default; writes the result back for the templates. */
    private String readVersion(String key, String defaultValue) {
        String value = additionalProperties.containsKey(key) ? additionalProperties.get(key).toString() : defaultValue;
        additionalProperties.put(key, value);
        return value;
    }

    private static CliOption versionOption(String name, String description, String defaultValue, String... values) {
        CliOption option = new CliOption(name, description);
        Map<String, String> enumValues = new LinkedHashMap<>();
        for (String value : values) {
            enumValues.put(value, value);
        }
        option.setEnum(enumValues);
        if (defaultValue != null) {
            option.setDefault(defaultValue);
        }
        return option;
    }

    /**
     * springdoc-openapi line matching a Spring Boot 4 minor: 3.0.x for Boot 4.0.x, 3.2.x for Boot 4.1+.
     * See https://springdoc.org/faq.html
     */
    static String springdocVersionFor(String springBootVersion) {
        if (springBootVersion != null && springBootVersion.startsWith("4.0.")) {
            return "3.0.3";
        }
        return "3.2.0";
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        if (isSpringCloudLibrary()) {
            List<CodegenSecurity> authMethods = (List<CodegenSecurity>) objs.get("authMethods");
            if (authMethods != null) {
                for (CodegenSecurity authMethod : authMethods) {
                    authMethod.name = camelize(sanitizeName(authMethod.name), true);
                }
            }
        }
        return objs;
    }

    @Override
    public String toApiName(String name) {
        if (name.isEmpty()) {
            return "DefaultApi";
        }
        name = sanitizeName(name);
        return camelize(name) + "Api";
    }

    @Override
    public String toApiTestFilename(String name) {
        if (isSpringMvcLibrary()) {
            return toApiName(name) + "ControllerIT";
        }
        if (isSpringCloudLibrary()) {
            return toApiName(name) + "Test";
        }
        return toApiName(name) + "ControllerIntegrationTest";
    }

    @Override
    public void setParameterExampleValue(CodegenParameter p) {
        String type = p.baseType;
        if (type == null) {
            type = p.dataType;
        }

        if ("File".equalsIgnoreCase(type)) {
            String example;

            if (p.defaultValue == null) {
                example = p.example;
            } else {
                example = p.defaultValue;
            }

            if (example == null) {
                example = "/path/to/file";
            }
            example = "new org.springframework.core.io.FileSystemResource(new java.io.File(\"" + escapeText(example) + "\"))";
            p.example = example;
        } else {
            super.setParameterExampleValue(p);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setConfigPackage(String configPackage) {
        this.configPackage = configPackage;
    }

    public void setValidationMode(String validationMode) {
        this.validationMode = validationMode;
    }

    public void setBasePackage(String configPackage) {
        this.basePackage = configPackage;
    }

    public void setInterfaceOnly(boolean interfaceOnly) { this.interfaceOnly = interfaceOnly; }

    public void setDelegatePattern(boolean delegatePattern) { this.delegatePattern = delegatePattern; }

    public void setSingleContentTypes(boolean singleContentTypes) {
        this.singleContentTypes = singleContentTypes;
    }

    public void setJava8(boolean java8) { this.java8 = java8; }

    public void setJava11(boolean java11) { this.java11 = java11; }

    public void setAsync(boolean async) { this.async = async; }

    public void setResponseWrapper(String responseWrapper) { this.responseWrapper = responseWrapper; }

    public void setUseTags(boolean useTags) {
        this.useTags = useTags;
    }

    public void setImplicitHeaders(boolean implicitHeaders) {
        this.implicitHeaders = implicitHeaders;
    }

    public void setSwaggerDocketConfig(boolean swaggerDocketConfig) {
        this.swaggerDocketConfig = swaggerDocketConfig;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);

        if ("null".equals(property.example)) {
            property.example = null;
        }

        //Add imports for Jackson
        boolean isEnum = getBooleanValue(model, IS_ENUM_EXT_NAME);
        if (!isEnum) {
            model.imports.add("JsonProperty");
            boolean hasEnums = getBooleanValue(model, HAS_ENUMS_EXT_NAME);
            if (hasEnums) {
                model.imports.add("JsonValue");
            }
        } else { // enum class
            //Needed imports for Jackson's JsonCreator
            if (additionalProperties.containsKey("jackson")) {
                model.imports.add("JsonCreator");
            }
        }
    }

    @Override
    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
        Map<String, Object> allProcessedModels = super.postProcessAllModels(objs);

        List<Object> allModels = new ArrayList<>();
        for (String name: allProcessedModels.keySet()) {
            Map<String, Object> models = (Map<String, Object>)allProcessedModels.get(name);
            try {
                allModels.add(((List<Object>) models.get("models")).get(0));
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        additionalProperties.put("parent", modelInheritanceSupport(allModels));

        return allProcessedModels;
    }

    protected List<Map<String, Object>> modelInheritanceSupport(List<?> allModels) {
        Map<CodegenModel, List<CodegenModel>> byParent = new LinkedHashMap<>();
        for (Object model : allModels) {
            Map entry = (Map) model;
            CodegenModel parent = ((CodegenModel)entry.get("model")).parentModel;
            if(null!= parent) {
                byParent.computeIfAbsent(parent, k -> new LinkedList<>()).add((CodegenModel)entry.get("model"));
            }
        }

        List<Map<String, Object>> parentsList = new ArrayList<>();
        for (Map.Entry<CodegenModel, List<CodegenModel>> parentModelEntry : byParent.entrySet()) {
            CodegenModel parentModel = parentModelEntry.getKey();
            List<Map<String, Object>> childrenList = new ArrayList<>();
            Map<String, Object> parent = new HashMap<>();
            parent.put("classname", parentModel.classname);
            List<CodegenModel> childrenModels = byParent.get(parentModel);

            if (childrenModels == null || childrenModels.isEmpty()) {
                continue;
            }

            for (CodegenModel model : childrenModels) {
                Map<String, Object> child = new HashMap<>();
                child.put("name", model.name);
                child.put("classname", model.classname);
                childrenList.add(child);
            }
            parent.put("children", childrenList);
            parent.put("discriminator", parentModel.discriminator);
            if(parentModel.discriminator != null && parentModel.discriminator.getMapping() != null)
            {
                parentModel.discriminator.getMapping().replaceAll((key, value) -> OpenAPIUtil.getSimpleRef(value));
            }
            parentsList.add(parent);
        }

        return parentsList;
    }

    @Override
    public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
        objs = super.postProcessModelsEnum(objs);

        //Add imports for Jackson
        List<Map<String, String>> imports = (List<Map<String, String>>)objs.get("imports");
        List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");
            // for enum model
            boolean isEnum = getBooleanValue(cm, IS_ENUM_EXT_NAME);
            if (isEnum && cm.allowableValues != null) {
                cm.imports.add(importMapping.get("JsonValue"));
                Map<String, String> item = new HashMap<String, String>();
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

    public void setOpenFeign(boolean openFeign) {
        this.openFeign = openFeign;
    }

    public void setDefaultInterfaces(boolean defaultInterfaces) {
        this.defaultInterfaces = defaultInterfaces;
    }

    public void setThrowsException(boolean throwsException) {
        this.throwsException = throwsException;
    }
}
