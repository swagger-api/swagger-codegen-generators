package io.swagger.codegen.v3.generators.java;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.codegen.v3.CodegenArgument;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.CodegenConstants.HAS_ENUMS_EXT_NAME;
import static io.swagger.codegen.v3.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class JavaInflectorServerCodegen extends AbstractJavaCodegen {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaInflectorServerCodegen.class);

    protected String title = "Swagger Inflector";
    protected String implFolder = "src/main/java";

    public JavaInflectorServerCodegen() {
        super();
        sourceFolder = "src/gen/java";
        apiTestTemplateFiles.clear(); // TODO: add test template
        invokerPackage = "io.swagger.controllers";
        artifactId = "swagger-inflector-server";
        dateLibrary = "legacy"; //TODO: add joda support

        // clear model and api doc template as this codegen
        // does not support auto-generated markdown doc at the moment
        //TODO: add doc templates
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        apiPackage = System.getProperty("swagger.codegen.inflector.apipackage", "io.swagger.controllers");
        modelPackage = System.getProperty("swagger.codegen.inflector.modelpackage", "io.swagger.model");

        additionalProperties.put("title", title);
        // java inflector uses the jackson lib
        additionalProperties.put("jackson", "true");
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "inflector";
    }

    @Override
    public String getHelp() {
        return "Generates a Java Inflector Server application.";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        writeOptional(outputFolder, new SupportingFile("pom.mustache", "", "pom.xml"));
        writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
        writeOptional(outputFolder, new SupportingFile("web.mustache", "src/main/webapp/WEB-INF", "web.xml"));
        writeOptional(outputFolder, new SupportingFile("inflector.mustache", "", "inflector.yaml"));
        supportingFiles.add(new SupportingFile("openapi3.mustache",
                "src/main/resources",
                "openapi3.yaml")
        );
        supportingFiles.add(new SupportingFile("StringUtil.mustache",
                (sourceFolder + '/' + invokerPackage).replace(".", "/"), "StringUtil.java"));
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
        } else {
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
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        if (operations != null) {
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for (CodegenOperation operation : ops) {
                if (operation.returnType == null) {
                    operation.returnType = "Void";
                } else if (operation.returnType.startsWith("List")) {
                    String rt = operation.returnType;
                    int end = rt.lastIndexOf(">");
                    if (end > 0) {
                        operation.returnType = rt.substring("List<".length(), end);
                        operation.returnContainer = "List";
                    }
                } else if (operation.returnType.startsWith("Map")) {
                    String rt = operation.returnType;
                    int end = rt.lastIndexOf(">");
                    if (end > 0) {
                        operation.returnType = rt.substring("Map<".length(), end);
                        operation.returnContainer = "Map";
                    }
                } else if (operation.returnType.startsWith("Set")) {
                    String rt = operation.returnType;
                    int end = rt.lastIndexOf(">");
                    if (end > 0) {
                        operation.returnType = rt.substring("Set<".length(), end);
                        operation.returnContainer = "Set";
                    }
                }
            }
        }
        return objs;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);

        //Add imports for Jackson
        boolean isEnum = getBooleanValue(model, IS_ENUM_EXT_NAME);
        if(!BooleanUtils.toBoolean(isEnum)) {
            model.imports.add("JsonProperty");
            boolean hasEnums = getBooleanValue(model, HAS_ENUMS_EXT_NAME);
            if(BooleanUtils.toBoolean(hasEnums)) {
                model.imports.add("JsonValue");
            }
        }
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
            if (Boolean.TRUE.equals(isEnum) && cm.allowableValues != null) {
                cm.imports.add(importMapping.get("JsonValue"));
                Map<String, String> item = new HashMap<String, String>();
                item.put("import", importMapping.get("JsonValue"));
                imports.add(item);
            }
        }

        return objs;
    }

    @Override
    protected String getOrGenerateOperationId(Operation operation, String path, String httpMethod) {
        return super.getOrGenerateOperationId(operation, path, httpMethod.toUpperCase());
    }

    @Override
    public void setLanguageArguments(List<CodegenArgument> languageArguments) {
        if (languageArguments == null) {
            languageArguments = new ArrayList<>();
        }
        if (!languageArguments.stream()
                .anyMatch(codegenArgument -> CodegenConstants.MODEL_DOCS_OPTION.equalsIgnoreCase(codegenArgument.getOption()) && StringUtils.isNotBlank(codegenArgument.getValue()))) {
            languageArguments.add(new CodegenArgument()
                    .option(CodegenConstants.MODEL_DOCS_OPTION)
                    .type("boolean")
                    .value(Boolean.FALSE.toString()));
        }
        if (!languageArguments.stream()
                .anyMatch(codegenArgument -> CodegenConstants.API_DOCS_OPTION.equalsIgnoreCase(codegenArgument.getOption()) && StringUtils.isNotBlank(codegenArgument.getValue()))) {
            languageArguments.add(new CodegenArgument()
                    .option(CodegenConstants.API_DOCS_OPTION)
                    .type("boolean")
                    .value(Boolean.FALSE.toString()));
        }
        if (!languageArguments.stream()
                .anyMatch(codegenArgument -> CodegenConstants.MODEL_TESTS_OPTION.equalsIgnoreCase(codegenArgument.getOption()) && StringUtils.isNotBlank(codegenArgument.getValue()))) {
            languageArguments.add(new CodegenArgument()
                    .option(CodegenConstants.MODEL_TESTS_OPTION)
                    .type("boolean")
                    .value(Boolean.FALSE.toString()));
        }
        if (!languageArguments.stream()
                .anyMatch(codegenArgument -> CodegenConstants.API_TESTS_OPTION.equalsIgnoreCase(codegenArgument.getOption()) && StringUtils.isNotBlank(codegenArgument.getValue()))) {
            languageArguments.add(new CodegenArgument()
                    .option(CodegenConstants.API_TESTS_OPTION)
                    .type("boolean")
                    .value(Boolean.FALSE.toString()));
        }
        super.setLanguageArguments(languageArguments);
    }

    public String apiFilename(String templateName, String tag) {
        String result = super.apiFilename(templateName, tag);

        if ( templateName.endsWith("api.mustache") ) {
            int ix = result.indexOf(sourceFolder);
            String beg = result.substring(0, ix);
            String end = result.substring(ix + sourceFolder.length());
            new java.io.File(beg + implFolder).mkdirs();
            result = beg + implFolder + end;
        }
        return result;
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        OpenAPI openAPI = (OpenAPI) objs.get("openAPI");
        if(openAPI != null) {
            try {
                objs.put("openapi3-yaml", Yaml.mapper().writeValueAsString(openAPI));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return super.postProcessSupportingFileData(objs);
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultController";
        }
        name = name.replaceAll("[^a-zA-Z0-9]+", "_");
        return camelize(name)+ "Controller";
    }

    @Override
    public String getArgumentsLocation() {
        return "/arguments/inflector.yaml";
    }

    @Override
    public String getDefaultTemplateDir() {
        return "JavaInflector";
    }
}
