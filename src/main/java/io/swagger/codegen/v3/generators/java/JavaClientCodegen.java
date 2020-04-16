package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.features.GzipFeatures;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.swagger.codegen.v3.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class JavaClientCodegen extends AbstractJavaCodegen implements GzipFeatures {
    static final String MEDIA_TYPE = "mediaType";

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaClientCodegen.class);

    public static final String USE_RUNTIME_EXCEPTION = "useRuntimeException";

    protected String gradleWrapperPackage = "gradle.wrapper";
    protected boolean useGzipFeature = false;
    protected boolean useRuntimeException = false;


    public JavaClientCodegen() {
        super();
        outputFolder = "generated-code" + File.separator + "java";
        invokerPackage = "io.secuconnect.client";
        groupId = "io.secuconnect.client";
        artifactId = "swagger-java-client";
        apiPackage = "io.secuconnect.client.api";
        modelPackage = "io.secuconnect.client.model";

        cliOptions.add(CliOption.newBoolean(USE_GZIP_FEATURE, "Send gzip-encoded requests"));
        cliOptions.add(CliOption.newBoolean(USE_RUNTIME_EXCEPTION, "Use RuntimeException instead of Exception"));

        supportedLibraries.put("okhttp-gson", "HTTP client: OkHttp 2.7.5. JSON processing: Gson 2.8.1. Enable gzip request encoding using '-DuseGzipFeature=true'.");

        CliOption libraryOption = new CliOption(CodegenConstants.LIBRARY, "library template (sub-template) to use");
        libraryOption.setEnum(supportedLibraries);
        // set okhttp-gson as the default
        libraryOption.setDefault("okhttp-gson");
        cliOptions.add(libraryOption);
        setLibrary("okhttp-gson");

    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "java";
    }

    @Override
    public String getHelp() {
        return "Generates a Java client library.";
    }

    @Override
    public void processOpts() {
        super.processOpts();
        if (StringUtils.isBlank(templateDir)) {
            String templateVersion = getTemplateVersion();
            embeddedTemplateDir = templateDir = getTemplateDir();
        }

        if (additionalProperties.containsKey(USE_GZIP_FEATURE)) {
            this.setUseGzipFeature(convertPropertyToBooleanAndWriteBack(USE_GZIP_FEATURE));
        }

        if (additionalProperties.containsKey(USE_RUNTIME_EXCEPTION)) {
            this.setUseRuntimeException(convertPropertyToBooleanAndWriteBack(USE_RUNTIME_EXCEPTION));
        }

        final String invokerFolder = (sourceFolder + File.separator + invokerPackage).replace(".", File.separator);
        final String authFolder = (sourceFolder + File.separator + invokerPackage + ".auth").replace(".", File.separator);
        final String apiFolder = (sourceFolder + File.separator + apiPackage).replace(".", File.separator);

        //Common files
        writeOptional(outputFolder, new SupportingFile("pom.mustache", "", "pom.xml"));
        writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
        writeOptional(outputFolder, new SupportingFile("build.gradle.mustache", "", "build.gradle"));
        writeOptional(outputFolder, new SupportingFile("build.sbt.mustache", "", "build.sbt"));
        writeOptional(outputFolder, new SupportingFile("settings.gradle.mustache", "", "settings.gradle"));
        writeOptional(outputFolder, new SupportingFile("gradle.properties.mustache", "", "gradle.properties"));
        writeOptional(outputFolder, new SupportingFile("manifest.mustache", projectFolder, "AndroidManifest.xml"));
        supportingFiles.add(new SupportingFile("travis.mustache", "", ".travis.yml"));
        supportingFiles.add(new SupportingFile("ApiClient.mustache", invokerFolder, "ApiClient.java"));
        supportingFiles.add(new SupportingFile("StringUtil.mustache", invokerFolder, "StringUtil.java"));

        supportingFiles.add(new SupportingFile("auth/OAuth.mustache", authFolder, "OAuth.java"));

        supportingFiles.add(new SupportingFile( "gradlew.mustache", "", "gradlew") );
        supportingFiles.add(new SupportingFile( "gradlew.bat.mustache", "", "gradlew.bat") );
        supportingFiles.add(new SupportingFile( "gradle-wrapper.properties.mustache",
                gradleWrapperPackage.replace( ".", File.separator ), "gradle-wrapper.properties") );
        supportingFiles.add(new SupportingFile( "gradle-wrapper.jar",
                gradleWrapperPackage.replace( ".", File.separator ), "gradle-wrapper.jar") );
        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));

        supportingFiles.add(new SupportingFile("apiException.mustache", invokerFolder, "ApiException.java"));
        supportingFiles.add(new SupportingFile("Configuration.mustache", invokerFolder, "Configuration.java"));
        supportingFiles.add(new SupportingFile("Pair.mustache", invokerFolder, "Pair.java"));
        supportingFiles.add(new SupportingFile("auth/Authentication.mustache", authFolder, "Authentication.java"));

        if ("okhttp-gson".equals(getLibrary()) || StringUtils.isEmpty(getLibrary())) {
            // the "okhttp-gson" library template requires "ApiCallback.mustache" for async call
            supportingFiles.add(new SupportingFile("ApiCallback.mustache", invokerFolder, "ApiCallback.java"));
            supportingFiles.add(new SupportingFile("ApiResponse.mustache", invokerFolder, "ApiResponse.java"));
            supportingFiles.add(new SupportingFile("JSON.mustache", invokerFolder, "JSON.java"));
            supportingFiles.add(new SupportingFile("ProgressRequestBody.mustache", invokerFolder, "ProgressRequestBody.java"));
            supportingFiles.add(new SupportingFile("ProgressResponseBody.mustache", invokerFolder, "ProgressResponseBody.java"));
            supportingFiles.add(new SupportingFile("GzipRequestInterceptor.mustache", invokerFolder, "GzipRequestInterceptor.java"));
            additionalProperties.put("gson", "true");
        } else {
            LOGGER.error("Unknown library option (-l/--library): " + getLibrary());
        }
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        return super.apiFilename(templateName, tag);
    }

    /**
     *  Prioritizes consumes mime-type list by moving json-vendor and json mime-types up front, but
     *  otherwise preserves original consumes definition order.
     *  [application/vnd...+json,... application/json, ..as is..]
     *
     * @param consumes consumes mime-type list
     * @return
     */
    static List<Map<String, String>> prioritizeContentTypes(List<Map<String, String>> consumes) {
        if ( consumes.size() <= 1 )
            return consumes;

        List<Map<String, String>> prioritizedContentTypes = new ArrayList<>(consumes.size());

        List<Map<String, String>> jsonVendorMimeTypes = new ArrayList<>(consumes.size());
        List<Map<String, String>> jsonMimeTypes = new ArrayList<>(consumes.size());

        for ( Map<String, String> consume : consumes) {
            if ( isJsonVendorMimeType(consume.get(MEDIA_TYPE))) {
                jsonVendorMimeTypes.add(consume);
            }
            else if ( isJsonMimeType(consume.get(MEDIA_TYPE))) {
                jsonMimeTypes.add(consume);
            }
            else
                prioritizedContentTypes.add(consume);

            consume.put("hasMore", "true");
        }

        prioritizedContentTypes.addAll(0, jsonMimeTypes);
        prioritizedContentTypes.addAll(0, jsonVendorMimeTypes);

        prioritizedContentTypes.get(prioritizedContentTypes.size()-1).put("hasMore", null);

        return prioritizedContentTypes;
    }

    private static boolean isMultipartType(List<Map<String, String>> consumes) {
        Map<String, String> firstType = consumes.get(0);
        if (firstType != null) {
            if ("multipart/form-data".equals(firstType.get(MEDIA_TYPE))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        boolean isEnum = getBooleanValue(model, IS_ENUM_EXT_NAME);
        if(!BooleanUtils.toBoolean(isEnum)) {
            //final String lib = getLibrary();
            //Needed imports for Jackson based libraries
            if(additionalProperties.containsKey("gson")) {
                model.imports.add("SerializedName");
//                model.imports.add("TypeAdapter");
//                model.imports.add("JsonAdapter");
//                model.imports.add("JsonReader");
//                model.imports.add("JsonWriter");
//                model.imports.add("IOException");
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
        Map<String, Object> allProcessedModels = super.postProcessAllModels(objs);
        if(!additionalProperties.containsKey("gsonFactoryMethod")) {
            List<Object> allModels = new ArrayList<Object>();
            for (String name: allProcessedModels.keySet()) {
                Map<String, Object> models = (Map<String, Object>)allProcessedModels.get(name);
                try {
                    allModels.add(((List<Object>) models.get("models")).get(0));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            additionalProperties.put("parent", modelInheritanceSupportInGson(allModels));
        }
        return allProcessedModels;
    }

    @Override
    public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
        objs = super.postProcessModelsEnum(objs);
        //Needed import for Gson based libraries
        if (additionalProperties.containsKey("gson")) {
            List<Map<String, String>> imports = (List<Map<String, String>>)objs.get("imports");
            List<Object> models = (List<Object>) objs.get("models");
            for (Object _mo : models) {
                Map<String, Object> mo = (Map<String, Object>) _mo;
                CodegenModel cm = (CodegenModel) mo.get("model");
                // for enum model
                boolean isEnum = getBooleanValue(cm, IS_ENUM_EXT_NAME);
                if (Boolean.TRUE.equals(isEnum) && cm.allowableValues != null) {
                    cm.imports.add(importMapping.get("SerializedName"));
                    Map<String, String> item = new HashMap<String, String>();
                    item.put("import", importMapping.get("SerializedName"));
                    imports.add(item);
                }
            }
        }
        return objs;
    }

    @Override
    public String getDefaultTemplateDir() {
        return "Java";
    }

    protected List<Map<String, Object>> modelInheritanceSupportInGson(List<?> allModels) {
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

    public void setUseGzipFeature(boolean useGzipFeature) {
        this.useGzipFeature = useGzipFeature;
    }

    public void setUseRuntimeException(boolean useRuntimeException) {
        this.useRuntimeException = useRuntimeException;
    }

    final private static Pattern JSON_MIME_PATTERN = Pattern.compile("(?i)application\\/json(;.*)?");
    final private static Pattern JSON_VENDOR_MIME_PATTERN = Pattern.compile("(?i)application\\/vnd.(.*)+json(;.*)?");

    /**
     * Check if the given MIME is a JSON MIME.
     * JSON MIME examples:
     *   application/json
     *   application/json; charset=UTF8
     *   APPLICATION/JSON
     */
    static boolean isJsonMimeType(String mime) {
        return mime != null && ( JSON_MIME_PATTERN.matcher(mime).matches());
    }

    /**
     * Check if the given MIME is a JSON Vendor MIME.
     * JSON MIME examples:
     *   application/vnd.mycompany+json
     *   application/vnd.mycompany.resourceA.version1+json
     */
    static boolean isJsonVendorMimeType(String mime) {
        return mime != null && JSON_VENDOR_MIME_PATTERN.matcher(mime).matches();
    }

}
