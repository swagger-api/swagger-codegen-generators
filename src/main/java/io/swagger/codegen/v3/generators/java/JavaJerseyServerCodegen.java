package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.SupportingFile;
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

public class JavaJerseyServerCodegen extends AbstractJavaJAXRSServerCodegen {

    protected static final String LIBRARY_JERSEY1 = "jersey1";
    protected static final String LIBRARY_JERSEY2 = "jersey2";

    /**
     * Default library template to use. (Default:{@value #DEFAULT_JERSEY_LIBRARY})
     */
    public static final String DEFAULT_JERSEY_LIBRARY = LIBRARY_JERSEY2;
    public static final String USE_TAGS = "useTags";

    protected boolean useTags = false;

    public JavaJerseyServerCodegen() {
        super();

        outputFolder = "generated-code/JavaJaxRS-Jersey";

        CliOption library = new CliOption(CodegenConstants.LIBRARY, "library template (sub-template) to use");

        supportedLibraries.put(LIBRARY_JERSEY1, "Jersey core 1.x");
        supportedLibraries.put(LIBRARY_JERSEY2, "Jersey core 2.x");
        library.setEnum(supportedLibraries);
        library.setDefault(DEFAULT_JERSEY_LIBRARY);

        cliOptions.add(library);
        cliOptions.add(CliOption.newBoolean(USE_TAGS, "use tags for creating interface and controller classnames"));
    }

    @Override
    public String getName() {
        return "jaxrs-jersey";
    }

    @Override
    public String getHelp() {
        return "[WORK IN PROGRESS: generated code depends from Swagger v2 libraries] "
                + "Generates a Java JAXRS Server application based on Jersey framework.";
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        if ("null".equals(property.example)) {
            property.example = null;
        }

        // Add imports for Jackson
        boolean isEnum = getBooleanValue(model, IS_ENUM_EXT_NAME);
        if (!BooleanUtils.toBoolean(isEnum)) {
            model.imports.add("JsonProperty");
            boolean hasEnums = getBooleanValue(model, HAS_ENUMS_EXT_NAME);
            if (BooleanUtils.toBoolean(hasEnums)) {
                model.imports.add("JsonValue");
            }
        }
    }

    @Override
    public void processOpts() {
        if ("jersey1".equalsIgnoreCase(library)) {
            setUseOas2(true);
            additionalProperties.put(CodegenConstants.USE_OAS2, true);
        }

        super.processOpts();

        addTemplateFiles();

        // use default library if unset
        if (StringUtils.isEmpty(library)) {
            setLibrary(DEFAULT_JERSEY_LIBRARY);
        }

        if (additionalProperties.containsKey(CodegenConstants.IMPL_FOLDER)) {
            implFolder = (String) additionalProperties.get(CodegenConstants.IMPL_FOLDER);
        }

        if (additionalProperties.containsKey(USE_TAGS)) {
            this.setUseTags(Boolean.valueOf(additionalProperties.get(USE_TAGS).toString()));
        }

        addDateLibrary();
        addSupportingFiles();
    }

    public void addDateLibrary() {
        if ("joda".equals(dateLibrary)) {
            supportingFiles.add(new SupportingFile("JodaDateTimeProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "JodaDateTimeProvider.java"));
            supportingFiles.add(new SupportingFile("JodaLocalDateProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "JodaLocalDateProvider.java"));
        }
        else if (dateLibrary.startsWith("java8")) {
            supportingFiles.add(new SupportingFile("OffsetDateTimeProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "OffsetDateTimeProvider.java"));
            supportingFiles.add(new SupportingFile("LocalDateProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "LocalDateProvider.java"));
        }
    }

    public void addTemplateFiles() {
        apiTemplateFiles.put("apiService.mustache", ".java");
        apiTemplateFiles.put("apiServiceImpl.mustache", ".java");
        apiTemplateFiles.put("apiServiceFactory.mustache", ".java");
        apiTestTemplateFiles.clear(); // TODO: add test template
        // clear model and api doc template as this codegen
        // does not support auto-generated markdown doc at the moment
        // TODO: add doc templates
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");
    }

    public void addSupportingFiles() {
        writeOptional(outputFolder, new SupportingFile("pom.mustache", "", "pom.xml"));
        writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("ApiException.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiException.java"));
        supportingFiles.add(new SupportingFile("ApiOriginFilter.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiOriginFilter.java"));
        supportingFiles.add(new SupportingFile("ApiResponseMessage.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "ApiResponseMessage.java"));
        supportingFiles.add(new SupportingFile("NotFoundException.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "NotFoundException.java"));
        supportingFiles.add(new SupportingFile("jacksonJsonProvider.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "JacksonJsonProvider.java"));
        supportingFiles.add(new SupportingFile("RFC3339DateFormat.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "RFC3339DateFormat.java"));
        writeOptional(outputFolder, new SupportingFile("bootstrap.mustache", (implFolder + '/' + apiPackage).replace(".", "/"), "Bootstrap.java"));
        writeOptional(outputFolder, new SupportingFile("web.mustache", ("src/main/webapp/WEB-INF"), "web.xml"));
        supportingFiles.add(new SupportingFile("StringUtil.mustache", (sourceFolder + '/' + apiPackage).replace(".", "/"), "StringUtil.java"));
    }

    @Override
    public String getDefaultTemplateDir() {
        return JAXRS_TEMPLATE_DIRECTORY_NAME;
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
            if (isEnum && cm.allowableValues != null) {
                cm.imports.add(importMapping.get("JsonValue"));
                Map<String, String> item = new HashMap<String, String>();
                item.put("import", importMapping.get("JsonValue"));
                imports.add(item);
            }
        }

        return objs;
    }

    /* (non-Javadoc)
     * @see io.swagger.codegen.languages.AbstractJavaJAXRSServerCodegen#postProcessOperations(java.util.Map)
     */
    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        objs = super.postProcessOperations(objs);

        if (useTags) {
            @SuppressWarnings("unchecked")
            Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
            if (operations != null) {

                // collect paths
                List<String> allPaths = new ArrayList<>();
                @SuppressWarnings("unchecked")
                List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
                for (CodegenOperation operation: ops) {
                    String path = operation.path;
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    allPaths.add(path);
                }

                if (!allPaths.isEmpty()) {
                    // find common prefix
                    StringBuilder basePathSB = new StringBuilder();
                    String firstPath = allPaths.remove(0);
                    String[] parts = firstPath.split("/");
                    partsLoop:
                    for (String part : parts) {
                        for (String path : allPaths) {
                            if (!path.startsWith(basePathSB.toString() + part)) {
                                break partsLoop;
                            }
                        }
                        basePathSB.append(part).append("/");
                    }
                    String basePath = basePathSB.toString();
                    if (basePath.endsWith("/")) {
                        basePath = basePath.substring(0, basePath.length() - 1);
                    }

                    if (basePath.length() > 0) {
                        // update operations
                        for (CodegenOperation operation: ops) {
                            operation.path = operation.path.substring(basePath.length() + (operation.path.startsWith("/") ? 1 : 0));
                            operation.baseName = basePath;
                            operation.subresourceOperation = !operation.path.isEmpty();
                        }

                        // save base path in objects
                        objs.put("apiBasePath", basePath);
                    }
                }
            }
        }

        return objs;
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        if (useTags) {
            // only add operations to group; base path extraction is done in postProcessOperations
            List<CodegenOperation> opList = operations.get(tag);
            if (opList == null) {
                opList = new ArrayList<CodegenOperation>();
                operations.put(tag, opList);
            }
            opList.add(co);
        } else  {
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
    }

    public void setUseTags(boolean useTags) {
        this.useTags = useTags;
    }

}
