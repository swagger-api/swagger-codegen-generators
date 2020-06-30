package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.features.JbossFeature;
import io.swagger.v3.oas.models.Operation;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.CodegenConstants.HAS_ENUMS_EXT_NAME;
import static io.swagger.codegen.v3.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class JavaResteasyServerCodegen extends AbstractJavaJAXRSServerCodegen implements JbossFeature {

    protected boolean generateJbossDeploymentDescriptor = true;

    public JavaResteasyServerCodegen() {

        super();

        artifactId = "swagger-jaxrs-resteasy-server";

        outputFolder = "generated-code/JavaJaxRS-Resteasy";

        dateLibrary = "legacy";// TODO: change to joda

        cliOptions.add(CliOption.newBoolean(GENERATE_JBOSS_DEPLOYMENT_DESCRIPTOR, "Generate Jboss Deployment Descriptor"));
    }

    @Override
    public String getName() {
        return "jaxrs-resteasy";
    }

    @Override
    public String getHelp() {
        return "[WORK IN PROGRESS: generated code depends from Swagger v2 libraries] "
                + "Generates a Java JAXRS-Resteasy Server application.";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        apiTemplateFiles.put("apiService.mustache", ".java");
        apiTemplateFiles.put("apiServiceImpl.mustache", ".java");
        apiTestTemplateFiles.clear(); // TODO: add test template

        // clear model and api doc template as AbstractJavaJAXRSServerCodegen
        // does not support auto-generated markdown doc at the moment
        // TODO: add doc templates
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        if (additionalProperties.containsKey(GENERATE_JBOSS_DEPLOYMENT_DESCRIPTOR)) {
            boolean generateJbossDeploymentDescriptorProp = convertPropertyToBooleanAndWriteBack(GENERATE_JBOSS_DEPLOYMENT_DESCRIPTOR);
            this.setGenerateJbossDeploymentDescriptor(generateJbossDeploymentDescriptorProp);
        }

        writeOptional(outputFolder, new SupportingFile("pom.mustache", "", "pom.xml"));
        writeOptional(outputFolder, new SupportingFile("gradle.mustache", "", "build.gradle"));
        writeOptional(outputFolder, new SupportingFile("settingsGradle.mustache", "", "settings.gradle"));
        writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("ApiException.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiException.java"));
        supportingFiles.add(new SupportingFile("ApiOriginFilter.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiOriginFilter.java"));
        supportingFiles.add(new SupportingFile("ApiResponseMessage.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiResponseMessage.java"));
        supportingFiles.add(new SupportingFile("NotFoundException.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "NotFoundException.java"));
        writeOptional(outputFolder, new SupportingFile("web.mustache", ("src/main/webapp/WEB-INF"), "web.xml"));

        if (generateJbossDeploymentDescriptor) {
            writeOptional(outputFolder, new SupportingFile("jboss-web.mustache", ("src/main/webapp/WEB-INF"), "jboss-web.xml"));
        }

        writeOptional(outputFolder, new SupportingFile("RestApplication.mustache", (sourceFolder + '/' + invokerPackage).replace(".", "/"), "RestApplication.java"));
        supportingFiles.add(new SupportingFile("StringUtil.mustache", (sourceFolder + '/' + invokerPackage).replace(".", "/"), "StringUtil.java"));
        supportingFiles.add(new SupportingFile("JacksonConfig.mustache", (sourceFolder + '/' + invokerPackage).replace(".", "/"), "JacksonConfig.java"));
        supportingFiles.add(new SupportingFile("RFC3339DateFormat.mustache", (sourceFolder + '/' + invokerPackage).replace(".", "/"), "RFC3339DateFormat.java"));

        if ("joda".equals(dateLibrary)) {
            supportingFiles.add(new SupportingFile("JodaDateTimeProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "JodaDateTimeProvider.java"));
            supportingFiles.add(new SupportingFile("JodaLocalDateProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "JodaLocalDateProvider.java"));
        }
        else if (dateLibrary.startsWith("java8")) {
            supportingFiles.add(new SupportingFile("OffsetDateTimeProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "OffsetDateTimeProvider.java"));
            supportingFiles.add(new SupportingFile("LocalDateProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "LocalDateProvider.java"));
        }
    }


    @Override
    public String getArgumentsLocation() {
        return "";
    }

    @Override
    public String getDefaultTemplateDir() {
        return JAXRS_TEMPLATE_DIRECTORY_NAME +  "/resteasy";
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        String basePath = resourcePath;
        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        int pos = basePath.indexOf("/");
        if (pos > 0) {
            basePath = basePath.substring(0, pos);
        }

        if (basePath == "") {
            basePath = "default";
        }
        else {
            if (co.path.startsWith("/" + basePath)) {
                co.path = co.path.substring(("/" + basePath).length());
            }
            co.subresourceOperation = !co.path.isEmpty();
        }
        List<CodegenOperation> opList = operations.get(basePath);
        if (opList == null) {
            opList = new ArrayList<CodegenOperation>();
            operations.put(basePath, opList);
        }
        opList.add(co);
        co.baseName = basePath;
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        return super.postProcessOperations(objs);
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        boolean isEnum = getBooleanValue(model, IS_ENUM_EXT_NAME);
        // Add imports for Jackson
        if (!BooleanUtils.toBoolean(isEnum)) {
            model.imports.add("JsonProperty");
            boolean hasEnums = getBooleanValue(model, HAS_ENUMS_EXT_NAME);
            if (BooleanUtils.toBoolean(hasEnums)) {
                model.imports.add("JsonValue");
            }
        }
    }

    @Override
    public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
        objs = super.postProcessModelsEnum(objs);

        // Add imports for Jackson
        List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");
        List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");
            // for enum model
            boolean isEnum = getBooleanValue(cm, IS_ENUM_EXT_NAME);
            if (Boolean.TRUE.equals(isEnum) && cm.allowableValues != null) {
                cm.imports.add(importMapping.get("JsonValue"));
                Map<String, String> item = new HashMap<String, String>();
                item.put("import", importMapping.get("JsonValue"));
                imports.add(item);
            }
        }

        return objs;
    }

    public void setGenerateJbossDeploymentDescriptor(boolean generateJbossDeploymentDescriptor) {
        this.generateJbossDeploymentDescriptor = generateJbossDeploymentDescriptor;
    }
}
