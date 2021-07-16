package io.swagger.codegen.v3.generators.kotlin;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class KotlinClientCodegen extends AbstractKotlinCodegen {

    public static final String DATE_LIBRARY = "dateLibrary";
    public static final String USE_RX_RETROFIT_2 = "useRxRetrofit2";

    private static Logger LOGGER = LoggerFactory.getLogger(KotlinClientCodegen.class);

    protected String dateLibrary = DateLibrary.JAVA8.value;
    protected boolean useRxRetrofit2 = false;

    public enum DateLibrary {
        STRING("string"),
        THREETENBP("threetenbp"),
        JAVA8("java8");

        public final String value;

        DateLibrary(String value) {
            this.value = value;
        }
    }

    /**
     * Constructs an instance of `KotlinClientCodegen`.
     */
    public KotlinClientCodegen() {
        super();

        artifactId = "kotlin-client";
        packageName = "io.swagger.client";

        outputFolder = "generated-code" + File.separator + "kotlin-client";

        CliOption dateLibrary = new CliOption(DATE_LIBRARY, "Option. Date library to use");
        Map<String, String> dateOptions = new HashMap<>();
        dateOptions.put(DateLibrary.THREETENBP.value, "Threetenbp");
        dateOptions.put(DateLibrary.STRING.value, "String");
        dateOptions.put(DateLibrary.JAVA8.value, "Java 8 native JSR310");
        dateLibrary.setEnum(dateOptions);
        cliOptions.add(dateLibrary);
        cliOptions.add(new CliOption(USE_RX_RETROFIT_2, "Use RxJava 2 with Retrofit 2"));
    }

    @Override
    public void addHandlebarHelpers(Handlebars handlebars) {
        super.addHandlebarHelpers(handlebars);
        handlebars.registerHelpers(ConditionalHelpers.class);
    }

    @Override
    public String getDefaultTemplateDir() {
        return "kotlin-client";
    }

    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    public String getName() {
        return "kotlin-client";
    }

    public String getHelp() {
        return "Generates a kotlin client.";
    }

    public void setDateLibrary(String library) {
        this.dateLibrary = library;
    }

    public void setUseRxRetrofit2(boolean useRxRetrofit2) {
        this.useRxRetrofit2 = useRxRetrofit2;
    }

    @Override
    public void processOpts() {
        super.processOpts();
        if (additionalProperties.containsKey(DATE_LIBRARY)) {
            setDateLibrary(additionalProperties.get(DATE_LIBRARY).toString());
        }

        if (DateLibrary.THREETENBP.value.equals(dateLibrary)) {
            additionalProperties.put(DateLibrary.THREETENBP.value, true);
            typeMapping.put("date", "LocalDate");
            typeMapping.put("DateTime", "LocalDateTime");
            importMapping.put("LocalDate", "org.threeten.bp.LocalDate");
            importMapping.put("LocalDateTime", "org.threeten.bp.LocalDateTime");
            defaultIncludes.add("org.threeten.bp.LocalDate");
            defaultIncludes.add("org.threeten.bp.LocalDateTime");
        } else if (DateLibrary.STRING.value.equals(dateLibrary)) {
            typeMapping.put("date-time", "kotlin.String");
            typeMapping.put("date", "kotlin.String");
            typeMapping.put("Date", "kotlin.String");
            typeMapping.put("DateTime", "kotlin.String");
        } else if (DateLibrary.JAVA8.value.equals(dateLibrary)) {
            additionalProperties.put(DateLibrary.JAVA8.value, true);
        }

        if (additionalProperties.containsKey(USE_RX_RETROFIT_2)) {
            setUseRxRetrofit2(convertPropertyToBoolean(USE_RX_RETROFIT_2));
        }
        additionalProperties.put(USE_RX_RETROFIT_2, useRxRetrofit2);

        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));

        supportingFiles.add(new SupportingFile("build.gradle.mustache", "", "build.gradle"));
        supportingFiles.add(new SupportingFile("settings.gradle.mustache", "", "settings.gradle"));

        addSupportingFiles(useRxRetrofit2);
        addTemplateFiles();
    }

    private void addSupportingFiles(boolean useRxRetrofit2) {
        if (useRxRetrofit2) {
            final String apiInfrastructureFolder = (sourceFolder + File.separator + apiPackage + File.separator + "infrastructure").replace(".", File.separator);
            final String apiTestInfrastructureFolder = (sourceTestFolder + File.separator + apiPackage + File.separator + "infrastructure").replace(".", File.separator);
            supportingFiles.add(new SupportingFile("infrastructure/Parameters.kt.mustache", apiInfrastructureFolder, "Parameters.kt"));
            supportingFiles.add(new SupportingFile("infrastructure/Parameters.kt.mustache", apiTestInfrastructureFolder, "Parameters.kt"));
        } else {
            final String infrastructureFolder = (sourceFolder + File.separator + packageName + File.separator + "infrastructure").replace(".", File.separator);

            supportingFiles.add(new SupportingFile("infrastructure/ApiClient.kt.mustache", infrastructureFolder, "ApiClient.kt"));
            supportingFiles.add(new SupportingFile("infrastructure/ApiAbstractions.kt.mustache", infrastructureFolder, "ApiAbstractions.kt"));
            supportingFiles.add(new SupportingFile("infrastructure/ApiInfrastructureResponse.kt.mustache", infrastructureFolder, "ApiInfrastructureResponse.kt"));
            supportingFiles.add(new SupportingFile("infrastructure/ApplicationDelegates.kt.mustache", infrastructureFolder, "ApplicationDelegates.kt"));
            supportingFiles.add(new SupportingFile("infrastructure/RequestConfig.kt.mustache", infrastructureFolder, "RequestConfig.kt"));
            supportingFiles.add(new SupportingFile("infrastructure/RequestMethod.kt.mustache", infrastructureFolder, "RequestMethod.kt"));
            supportingFiles.add(new SupportingFile("infrastructure/ResponseExtensions.kt.mustache", infrastructureFolder, "ResponseExtensions.kt"));
            supportingFiles.add(new SupportingFile("infrastructure/Serializer.kt.mustache", infrastructureFolder, "Serializer.kt"));
            supportingFiles.add(new SupportingFile("infrastructure/Errors.kt.mustache", infrastructureFolder, "Errors.kt"));
            supportingFiles.add(new SupportingFile("infrastructure/LocalDateAdapter.kt.mustache", infrastructureFolder, "LocalDateAdapter.kt"));
            supportingFiles.add(new SupportingFile("infrastructure/LocalDateTimeAdapter.kt.mustache", infrastructureFolder, "LocalDateTimeAdapter.kt"));
        }
    }

    private void addTemplateFiles() {
        modelTemplateFiles.put("model.mustache", ".kt");
        apiTemplateFiles.put("api.mustache", ".kt");
        modelTestTemplateFiles.put("model_test.mustache", ".kt");
        apiTestTemplateFiles.put("api_test.mustache", ".kt");
        modelDocTemplateFiles.put("model_doc.mustache", ".md");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");
    }

    @Override
    public String toApiTestFilename(String name) {
        String apiTestFileName = super.toApiFilename(name);
        apiTestFileName += "URLs";
        return apiTestFileName;
    }

    @Override
    public String toModelTestFilename(String name) {
        return toModelFilename(name);
    }

}