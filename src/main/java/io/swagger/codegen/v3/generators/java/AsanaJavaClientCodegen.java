package io.swagger.codegen.v3.generators.java;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import io.swagger.codegen.v3.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import io.swagger.codegen.v3.generators.handlebars.java.JavaHelper;
import io.swagger.codegen.v3.generators.features.BeanValidationFeatures;
import io.swagger.codegen.v3.generators.features.GzipFeatures;
import io.swagger.codegen.v3.generators.features.PerformBeanValidationFeatures;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.regex.Pattern;

import static io.swagger.codegen.v3.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;
import static java.util.Collections.reverse;
import static java.util.Collections.sort;

import java.io.IOException;

public class AsanaJavaClientCodegen extends JavaClientCodegen {
    @Override
    public String getName() {
        return "asana-java";
    }

    @Override
    public void addHandlebarHelpers(Handlebars handlebars) {
        super.addHandlebarHelpers(handlebars);
        handlebars.registerHelpers(new JavaHelper());
        handlebars.registerHelper("eq", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                Object b = null;
                int index = 0;
                while (index < options.params.length) {
                    b = options.param(index, null);
                    boolean result = new EqualsBuilder().append(a, b).isEquals();
                    if (result) {
                        if (options.tagType == TagType.SECTION) {
                            return options.fn();
                        }
                        return options.hash("yes", true);
                    }
                    index++;
                }

                if (options.tagType == TagType.SECTION) {
                    return options.inverse();
                }
                return options.hash("no", false);
            }
        });
        handlebars.registerHelper("neq", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                Object b = null;
                int index = 0;
                while (index < options.params.length) {
                    b = options.param(index, null);
                    boolean result = new EqualsBuilder().append(a, b).isEquals();
                    if (result) {
                        if (options.tagType == TagType.SECTION) {
                            return options.inverse();
                        }
                        return options.hash("no", false);
                    }
                    index++;
                }

                if (options.tagType == TagType.SECTION) {
                    return options.fn();
                }
                return options.hash("yes", true);
            }
        });
        handlebars.registerHelper("isRequestModel", new Helper<Object>() {
            @Override public Object apply(final Object baseName, final Options options) throws IOException {
                boolean result = ((String)baseName).endsWith("Request");
                if (options.tagType == TagType.SECTION) {
                    return result ? options.fn() : options.inverse();
                }
                return result
                        ? options.hash("yes", true)
                        : options.hash("no", false);
            }
        });
        handlebars.registerHelper("moreThanCommon", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                CodegenContent params = (CodegenContent) a;

                List<String> commonParams = Arrays.asList("opt_pretty", "opt_fields", "limit", "offset");
                for (CodegenParameter param : params.getParameters()) {
                    if (param.getBooleanValue("x-is-query-param") && commonParams.indexOf(param.paramName) < 0) {
                        if (options.tagType == TagType.SECTION) {
                            return options.fn();
                        }
                        return options.hash("yes", true);
                    }
                }
                if (options.tagType == TagType.SECTION) {
                    return options.inverse();
                }
                return options.hash("no", false);
            }
        });
        handlebars.registerHelper("firstClassResponseObject", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                String responseType = (String) a;

                List<String> firstClassModel = Arrays.asList("Attachment", "CustomFieldSetting", "CustomField", "Job", "OrganizationExport", "Portfolio", "PortfolioMembership", "Project", "ProjectMembership", "ProjectStatus", "Section", "Story", "Tag", "Task", "Team", "User", "UserTaskList", "Webhook", "Workspace");

                int index = firstClassModel.indexOf(responseType.replace("Response", ""));
                if (index >= 0) {
                    if (options.tagType == TagType.SECTION) {
                        return options.fn();
                    }
                    return firstClassModel.get(index);
                } else {
                    index = firstClassModel.indexOf(responseType.replace("Compact", ""));
                    if (index >= 0) {
                        if (options.tagType == TagType.SECTION) {
                            return options.fn();
                        }
                        return firstClassModel.get(index);
                    } else {
                        index = firstClassModel.indexOf(responseType.substring(0, responseType.length() - 1));
                        if (index >= 0) {
                            if (options.tagType == TagType.SECTION) {
                                return options.fn();
                            }
                            return firstClassModel.get(index);
                        } else {
                            index = firstClassModel.indexOf(responseType.substring(0, responseType.length() - 3) + "y");
                            if (index >= 0) {
                                if (options.tagType == TagType.SECTION) {
                                    return options.fn();
                                }
                                return firstClassModel.get(index);
                            } else {
                                index = firstClassModel.indexOf(responseType.substring(0, responseType.length() - 2));
                                if (index >= 0) {
                                    if (options.tagType == TagType.SECTION) {
                                        return options.fn();
                                    }
                                    return firstClassModel.get(index);
                                }
                            }
                        }
                    }
                }
                if (options.tagType == TagType.SECTION) {
                    return options.inverse();
                }
                return options.hash("no", false);
            }
        });
        handlebars.registerHelper("needsFileImport", new Helper<Object>() {
            @Override public Object apply(final Object baseName, final Options options) throws IOException {
                String baseNameString = (String)baseName;
                if (baseNameString.startsWith("Attachment")) {
                    if (options.tagType == TagType.SECTION) {
                        return options.fn();
                    }
                    return baseNameString;
                }
                if (options.tagType == TagType.SECTION) {
                    return options.inverse();
                }
                return options.hash("no", false);
            }
        });
        handlebars.registerHelper("getRequestModel", new Helper<Object>() {
            @Override public Object apply(final Object baseName, final Options options) throws IOException {
                String baseNameString = (String)baseName;
                return baseNameString.endsWith("Request")
                        ? baseNameString.replace("Request", "Response")
                        : "";
            }
        });
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "Default";
        }
        return camelize(name) + "Base";
    }

    @Override
    public String toApiFilename(String name) {
        return toApiName(name);
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);
        if (openAPI == null || openAPI.getPaths() == null){
            return;
        }
        for (String pathname : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(pathname);

            for (Operation operation : pathItem.readOperations()) {
                if (operation == null) {
                    continue;
                }
                //only add content-Type if its no a GET-Method
                if (!operation.equals(pathItem.getGet())) {
                    String contentType = getContentType(operation.getRequestBody());
                    if (StringUtils.isBlank(contentType)) {
                        contentType = DEFAULT_CONTENT_TYPE;
                    }
                    operation.addExtension("x-contentType", contentType);
                }
                String accepts = getAccept(operation);
                operation.addExtension("x-accepts", accepts);

                ApiResponses responses = operation.getResponses();
                if (responses == null) {
                    continue;
                }
                for(Map.Entry<String,ApiResponse> responseEntry : responses.entrySet()){
                    String statusCode = responseEntry.getKey();
                    if (statusCode.startsWith("2")) {
                        ApiResponse response = responseEntry.getValue();
                        if (response == null) {
                            continue;
                        }

                        Content content = response.getContent();
                        if (content == null) {
                            continue;
                        }

                        MediaType mediaType = content.get("application/json");

                        if (mediaType == null) {
                            continue;
                        }

                        Schema schema = mediaType.getSchema();
                        if (schema == null) {
                            continue;
                        }

                        Map<String, Schema> properties = schema.getProperties();
                        if (properties == null) {
                            continue;
                        }
                        if (properties.containsKey("data")) {
                            mediaType.setSchema(properties.get("data"));
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        super.postProcessOperations(objs);
        if (usesAnyRetrofitLibrary()) {
            Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
            if (operations != null) {
                List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
                for (CodegenOperation operation : ops) {
                    boolean hasConsumes = getBooleanValue(operation, CodegenConstants.HAS_CONSUMES_EXT_NAME);
                    if (hasConsumes) {

                        if (isMultipartType(operation.consumes)) {
                            operation.getVendorExtensions().put(CodegenConstants.IS_MULTIPART_EXT_NAME, Boolean.TRUE);
                        }
                        else {
                            operation.prioritizedContentTypes = prioritizeContentTypes(operation.consumes);
                        }
                    }
                    if (operation.returnType == null) {
                        operation.returnType = "Void";
                    }
                    if (usesRetrofit2Library() && StringUtils.isNotEmpty(operation.path) && operation.path.startsWith("/")){
                        operation.path = operation.path.substring(1);
                    }

                    // sorting operation parameters to make sure path params are parsed before query params
                    if (operation.allParams != null) {
                        sort(operation.allParams, new Comparator<CodegenParameter>() {
                            @Override
                            public int compare(CodegenParameter one, CodegenParameter another) {
                                if (getBooleanValue(one, CodegenConstants.IS_PATH_PARAM_EXT_NAME)
                                        && getBooleanValue(another, CodegenConstants.IS_QUERY_PARAM_EXT_NAME)) {
                                    return -1;
                                }
                                if (getBooleanValue(one, CodegenConstants.IS_QUERY_PARAM_EXT_NAME)
                                        && getBooleanValue(another, CodegenConstants.IS_PATH_PARAM_EXT_NAME)){
                                    return 1;
                                }

                                return 0;
                            }
                        });
                        Iterator<CodegenParameter> iterator = operation.allParams.iterator();
                        while (iterator.hasNext()){
                            CodegenParameter param = iterator.next();
                            param.getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, iterator.hasNext());
                        }
                    }
                }
            }

        }

        // camelize path variables for Feign client
        if ("feign".equals(getLibrary())) {
            Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
            List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");
            for (CodegenOperation op : operationList) {
                String path = op.path;
                String[] items = path.split("/", -1);

                for (int i = 0; i < items.length; ++i) {
                    if (items[i].matches("^\\{(.*)\\}$")) { // wrap in {}
                        // camelize path variable
                        items[i] = "{" + camelize(items[i].substring(1, items[i].length()-1), true) + "}";
                    }
                }
                op.path = StringUtils.join(items, "/");
            }
        }

        // Customize returnTypes
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");
        for (CodegenOperation operation : operationList) {
            if (operation.returnType.startsWith("List")) {
                operation.returnType = operation.returnType.substring(5, operation.returnType.indexOf(">"));
                operation.returnContainer = "CollectionRequest";
            } else {
                operation.returnContainer = "ItemRequest";
            }

            // sorting operation parameters to make sure path params are parsed before query params
            if (operation.allParams != null) {
                sort(operation.allParams, new Comparator<CodegenParameter>() {
                    @Override
                    public int compare(CodegenParameter one, CodegenParameter another) {
                        if (getBooleanValue(one, CodegenConstants.IS_PATH_PARAM_EXT_NAME)
                                && !getBooleanValue(another, CodegenConstants.IS_PATH_PARAM_EXT_NAME)) {
                            return -1;
                        }
                        if (getBooleanValue(another, CodegenConstants.IS_PATH_PARAM_EXT_NAME)
                                && !getBooleanValue(one, CodegenConstants.IS_PATH_PARAM_EXT_NAME)) {
                            return 1;
                        }
                        if (getBooleanValue(one, CodegenConstants.IS_BODY_PARAM_EXT_NAME)
                                && getBooleanValue(another, CodegenConstants.IS_QUERY_PARAM_EXT_NAME)) {
                            return -1;
                        }
                        if (getBooleanValue(one, CodegenConstants.IS_QUERY_PARAM_EXT_NAME)
                                && getBooleanValue(another, CodegenConstants.IS_BODY_PARAM_EXT_NAME)){
                            return 1;
                        }
                        if (getBooleanValue(one, CodegenConstants.IS_QUERY_PARAM_EXT_NAME)
                                && getBooleanValue(another, CodegenConstants.IS_QUERY_PARAM_EXT_NAME)){
                            List<String> commonParams = Arrays.asList(new String[]{"opt_pretty", "opt_fields", "limit", "offset"});

                            int oneIndex = commonParams.indexOf(one.baseName);
                            int anotherIndex = commonParams.indexOf(another.baseName);

                            if (oneIndex == -1) {
                                return -1;
                            } else if (anotherIndex == -1) {
                                return 1;
                            }

                            return anotherIndex - oneIndex;
                        }

                        return 0;
                    }
                });

                // Remove body params for now
                int index_to_remove;
                for (index_to_remove = 0; index_to_remove < operation.allParams.size(); index_to_remove++) {
                    if (operation.allParams.get(index_to_remove).getIsBodyParam() || operation.allParams.get(index_to_remove).getIsFormParam()) {
                         break;
                    }
                }
                if (index_to_remove < operation.allParams.size()) {
                    operation.allParams.remove(index_to_remove);
                }

                Iterator<CodegenParameter> iterator = operation.allParams.iterator();
                while (iterator.hasNext()){
                    CodegenParameter param = iterator.next();
                    param.getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, iterator.hasNext());
                }
            }
        }

        return objs;
    }

    private boolean usesAnyRetrofitLibrary() {
        return getLibrary() != null && getLibrary().contains(RETROFIT_1);
    }

    private boolean usesRetrofit2Library() {
        return getLibrary() != null && getLibrary().contains(RETROFIT_2);
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

    private static String getAccept(Operation operation) {
        String accepts = null;
        if (operation != null && operation.getResponses() != null && !operation.getResponses().isEmpty()) {
            StringBuilder mediaTypeBuilder = new StringBuilder();

            responseLoop:
            for (ApiResponse response : operation.getResponses().values()) {
                if(response.getContent() == null || response.getContent().isEmpty()) {
                    continue;
                }

                mediaTypeLoop:
                for (String mediaTypeKey : response.getContent().keySet()) {
                    if (DEFAULT_CONTENT_TYPE.equalsIgnoreCase(mediaTypeKey)) {
                        accepts = DEFAULT_CONTENT_TYPE;
                        break responseLoop;
                    } else {
                        if (mediaTypeBuilder.length() > 0) {
                            mediaTypeBuilder.append(",");
                        }
                        mediaTypeBuilder.append(mediaTypeKey);
                    }
                }
            }
            if (accepts == null) {
                accepts = mediaTypeBuilder.toString();
            }
        } else {
            accepts = DEFAULT_CONTENT_TYPE;
        }
        return accepts;
    }
}
