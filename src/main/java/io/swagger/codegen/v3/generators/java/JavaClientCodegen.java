package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import org.apache.commons.lang3.BooleanUtils;
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

public class JavaClientCodegen extends AbstractJavaCodegen {
    static final String MEDIA_TYPE = "mediaType";

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaClientCodegen.class);

    public JavaClientCodegen() {
        super();
        artifactVersion = "3.0.0";
        outputFolder = "generated-code" + File.separator + "java";
        invokerPackage = "com.secuconnect.client";
        groupId = "com.secuconnect";
        artifactId = "secuconnect-java-sdk";
        apiPackage = "com.secuconnect.client.api";
        modelPackage = "com.secuconnect.client.model";
        scmConnection = "scm:git:git@github.com:secuconnect/secuconnect-java-sdk.git";
        scmDeveloperConnection = "scm:git:git@github.com:secuconnect/secuconnect-java-sdk.git";
        scmUrl = "https://github.com/secuconnect/secuconnect-java-sdk";
        licenseName = "The Apache Software License, Version 2.0";
        licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt";
        developerName = "secuconnect";
        developerEmail = "info@secuconnect.com";
        developerOrganization = "secuconnect KG";
        developerOrganizationUrl = "http://secuconnect.com";
        artifactUrl = "https://github.com/secuconnect/secuconnect-java-sdk";
        artifactDescription = "Secuconnect Java";
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

        final String invokerFolder = (sourceFolder + File.separator + invokerPackage).replace(".", File.separator);

        //Common files
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));

        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));

        supportingFiles.add(new SupportingFile("ApiException.mustache", invokerFolder, "ApiException.java"));
        supportingFiles.add(new SupportingFile("Pair.mustache", invokerFolder, "Pair.java"));

        supportingFiles.add(new SupportingFile("ApiCallback.mustache", invokerFolder, "ApiCallback.java"));
        supportingFiles.add(new SupportingFile("ApiResponse.mustache", invokerFolder, "ApiResponse.java"));
        supportingFiles.add(new SupportingFile("JSON.mustache", invokerFolder, "JSON.java"));
        supportingFiles.add(new SupportingFile("ProgressRequestBody.mustache", invokerFolder, "ProgressRequestBody.java"));
        supportingFiles.add(new SupportingFile("ProgressResponseBody.mustache", invokerFolder, "ProgressResponseBody.java"));
        additionalProperties.put("gson", "true");
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        return super.apiFilename(templateName, tag);
    }

    @Override
    public void setGitRepoBaseURL(String s) {

    }

    @Override
    public String getGitRepoBaseURL() {
        return null;
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
            //Needed imports for Jackson based libraries
            if(additionalProperties.containsKey("gson")) {
                model.imports.add("SerializedName");
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
            List<CodegenModel> childrenModels = parentModelEntry.getValue();
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
