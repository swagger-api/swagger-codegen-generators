package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenResponse;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.generators.features.BeanValidationFeatures;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.codegen.v3.utils.ModelUtils;
import io.swagger.codegen.v3.utils.URLPathUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public abstract class AbstractJavaJAXRSServerCodegen extends AbstractJavaCodegen implements BeanValidationFeatures {
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractJavaJAXRSServerCodegen.class);

    /**
     * Name of the sub-directory in "src/main/resource" where to find the
     * Mustache template for the JAX-RS Codegen.
     */
    protected static final String JAXRS_TEMPLATE_DIRECTORY_NAME = "JavaJaxRS";
    /**
     * Name of the configuration setting for switching between tag name based setup and path based.
     */
    public static final String USE_TAGS = "useTags";

    protected String implFolder = "src/main/java";
    protected String testResourcesFolder = "src/test/resources";
    protected String title = "Swagger Server";

    protected boolean useBeanValidation = true;
    protected boolean useTags = false;

    public AbstractJavaJAXRSServerCodegen() {
        super();

        sourceFolder = "src/gen/java";
        invokerPackage = "io.swagger.api";
        artifactId = "swagger-jaxrs-server";
        dateLibrary = "legacy"; //TODO: add joda support to all jax-rs

        apiPackage = "io.swagger.api";
        modelPackage = "io.swagger.model";

        additionalProperties.put("title", title);
        // java inflector uses the jackson lib
        additionalProperties.put("jackson", "true");

        cliOptions.add(new CliOption(CodegenConstants.IMPL_FOLDER, CodegenConstants.IMPL_FOLDER_DESC));
        cliOptions.add(new CliOption("title", "a title describing the application"));

        cliOptions.add(CliOption.newBoolean(USE_BEANVALIDATION, "Use BeanValidation API annotations"));
        cliOptions.add(new CliOption("serverPort", "The port on which the server should be started"));
        cliOptions.add(CliOption.newBoolean(USE_TAGS, "use tags for creating interface and controller classnames"));
    }


    // ===============
    // COMMONS METHODS
    // ===============

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(CodegenConstants.IMPL_FOLDER)) {
            implFolder = (String) additionalProperties.get(CodegenConstants.IMPL_FOLDER);
        }

        if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
            this.setUseBeanValidation(convertPropertyToBoolean(USE_BEANVALIDATION));
        }
        if (additionalProperties.containsKey(USE_TAGS)) {
            this.setUseTags(Boolean.valueOf(additionalProperties.get(USE_TAGS).toString()));
        }

        if (useBeanValidation) {
            writePropertyBack(USE_BEANVALIDATION, useBeanValidation);
        }

    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        //this.openAPIUtil = new OpenAPIUtil(openAPI);
        this.openAPI = openAPI;
        if (!this.additionalProperties.containsKey("serverPort")) {
            final URL urlInfo = URLPathUtil.getServerURL(openAPI);
            String port = "8080"; // Default value for a JEE Server
            if ( urlInfo != null && urlInfo.getPort() > 0) {
                    port = String.valueOf(urlInfo.getPort());
            }
            this.additionalProperties.put("serverPort", port);
        }

        if (openAPI.getPaths() != null) {
            for (String pathname : openAPI.getPaths().keySet()) {
                PathItem pathItem = openAPI.getPaths().get(pathname);
                final Operation[] operations = ModelUtils.createOperationArray(pathItem);
                for (Operation operation : operations) {
                    if (operation != null && operation.getTags() != null) {
                        List<Map<String, String>> tags = new ArrayList<Map<String, String>>();
                        for (String tag : operation.getTags()) {
                            Map<String, String> value = new HashMap<String, String>();
                            value.put("tag", tag);
                            value.put("hasMore", "true");
                            tags.add(value);
                        }
                        if (tags.size() > 0) {
                            tags.get(tags.size() - 1).remove("hasMore");
                        }
                        if (operation.getTags().size() > 0) {
                            String tag = operation.getTags().get(0);
                            operation.setTags(Arrays.asList(tag));
                        }
                        operation.addExtension("x-tags", tags);
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> processedOperations = jaxrsPostProcessOperations(objs);

        if (useTags) {
            /* Perform additional processing */
            processedOperations = jaxrsPostProcessOperationsForTags(processedOperations);
        }

        return processedOperations;
    }

    static Map<String, Object> jaxrsPostProcessOperations(Map<String, Object> objs) {
        @SuppressWarnings("unchecked")
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        if ( operations != null ) {
            @SuppressWarnings("unchecked")
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for ( CodegenOperation operation : ops ) {
                boolean hasConsumes = getBooleanValue(operation, CodegenConstants.HAS_CONSUMES_EXT_NAME);
                if (hasConsumes) {
                    Map<String, String> firstType = operation.consumes.get(0);
                    if (firstType != null) {
                        if ("multipart/form-data".equals(firstType.get("mediaType"))) {
                            operation.getVendorExtensions().put(CodegenConstants.IS_MULTIPART_EXT_NAME, Boolean.TRUE);
                        }
                    }
                }

                boolean isMultipartPost = false;
                List<Map<String, String>> consumes = operation.consumes;
                if(consumes != null) {
                    for(Map<String, String> consume : consumes) {
                        String mt = consume.get("mediaType");
                        if(mt != null) {
                            if(mt.startsWith("multipart/form-data")) {
                                isMultipartPost = true;
                            }
                        }
                    }
                }

                for(CodegenParameter parameter : operation.allParams) {
                    if(isMultipartPost) {
                        parameter.vendorExtensions.put("x-multipart", "true");
                    }
                }

                List<CodegenResponse> responses = operation.responses;
                if ( responses != null ) {
                    for ( CodegenResponse resp : responses ) {
                        if ( "0".equals(resp.code) ) {
                            resp.code = "200";
                        }

                        if (resp.baseType == null) {
                            resp.dataType = "void";
                            resp.baseType = "Void";
                            // set vendorExtensions.x-java-is-response-void to true as baseType is set to "Void"
                            resp.vendorExtensions.put("x-java-is-response-void", true);
                        }

                        if ("array".equals(resp.containerType)) {
                            resp.containerType = "List";
                        } else if ("map".equals(resp.containerType)) {
                            resp.containerType = "Map";
                        }
                    }
                }

                if ( operation.returnBaseType == null ) {
                    operation.returnType = "void";
                    operation.returnBaseType = "Void";
                    // set vendorExtensions.x-java-is-response-void to true as returnBaseType is set to "Void"
                    operation.vendorExtensions.put("x-java-is-response-void", true);
                }

                if ("array".equals(operation.returnContainer)) {
                    operation.returnContainer = "List";
                } else if ("map".equals(operation.returnContainer)) {
                    operation.returnContainer = "Map";
                }
            }
        }
        return objs;
    }

    /**
     * If this generator is configured to with useTags set to true, this will be called to perform additional
     * processing. 
     * @param objs the map containing the operations
     * @return the input map updated with processed information
     */
    protected Map<String, Object> jaxrsPostProcessOperationsForTags(Map<String, Object> objs) {
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
    public String toApiName(final String name) {
        String computed = name;
        if ( computed.length() == 0 ) {
            return "DefaultApi";
        }
        computed = sanitizeName(computed);
        return camelize(computed) + "Api";
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        String result = super.apiFilename(templateName, tag);

        if ( templateName.endsWith("Impl.mustache") ) {
            int ix = result.lastIndexOf('/');
            result = result.substring(0, ix) + "/impl" + result.substring(ix, result.length() - 5) + "ServiceImpl.java";
            result = result.replace(apiFileFolder(), implFileFolder(implFolder));
        } else if ( templateName.endsWith("Factory.mustache") ) {
            int ix = result.lastIndexOf('/');
            result = result.substring(0, ix) + "/factories" + result.substring(ix, result.length() - 5) + "ServiceFactory.java";
            result = result.replace(apiFileFolder(), implFileFolder(implFolder));
        } else if ( templateName.endsWith("Service.mustache") ) {
            int ix = result.lastIndexOf('.');
            result = result.substring(0, ix) + "Service.java";
        }
        return result;
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        if (useTags) {
            // only add operations to group; base path extraction is done in postProcessOperations
            List<CodegenOperation> opList = operations.computeIfAbsent(tag, k -> new ArrayList<CodegenOperation>());
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

            if (basePath.isEmpty()) {
                basePath = "default";
            }
            else {
                if (co.path.startsWith("/" + basePath)) {
                    co.path = co.path.substring(("/" + basePath).length());
                }
                co.subresourceOperation = !co.path.isEmpty();
            }
            List<CodegenOperation> opList = operations.computeIfAbsent(basePath, k -> new ArrayList<CodegenOperation>());
            opList.add(co);
            co.baseName = basePath;
        }
    }

    public void setUseTags(boolean useTags) {
        this.useTags = useTags;
    }

    private String implFileFolder(String output) {
        return outputFolder + "/" + output + "/" + apiPackage().replace('.', '/');
    }

    public void setUseBeanValidation(boolean useBeanValidation) {
        this.useBeanValidation = useBeanValidation;
    }

    @Override
    public String getArgumentsLocation() {
        return "/arguments/server.yaml";
    }
}
