package io.swagger.codegen.v3.generators.javascript;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import io.swagger.codegen.v3.generators.handlebars.java.JavaHelper;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.lang.Character;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;
import static java.util.Collections.reverse;
import static java.util.Collections.sort;

import java.io.IOException;

public class AsanaTypeScriptAngularClientCodegen extends JavaScriptClientCodegen {
    @Override
    public String getName() {
        return "asana-node";
    }

    @Override
    public void addHandlebarHelpers(Handlebars handlebars) {
        super.addHandlebarHelpers(handlebars);
        handlebars.registerHelpers(new JavaHelper());
        handlebars.registerHelper("eq", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                Object b = options.param(0, null);
                boolean result = new EqualsBuilder().append(a, b).isEquals();
                if (options.tagType == TagType.SECTION) {
                    return result ? options.fn() : options.inverse();
                }
                return result
                    ? options.hash("yes", true)
                    : options.hash("no", false);
            }
        });
        handlebars.registerHelper("neq", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                Object b = options.param(0, null);
                boolean result = !new EqualsBuilder().append(a, b).isEquals();
                if (options.tagType == TagType.SECTION) {
                    return result ? options.fn() : options.inverse();
                }
                return result
                    ? options.hash("yes", true)
                    : options.hash("no", false);
            }
        });
        handlebars.registerHelper("toCamelCase", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                String s = (String)a;
                return camelize(s.toLowerCase());
            }
        });
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

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        // Customize returnTypes
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");
        for (CodegenOperation operation : operationList) {
            if (operation.returnType.startsWith("[")) {
                operation.returnType = operation.returnType.substring(1, operation.returnType.indexOf("]"));
                operation.returnContainer = "GetCollection";
            } else {
                operation.returnContainer = "Get";
            }

            // sorting operation parameters to make sure path params are parsed before query params
            if (operation.queryParams != null) {
                reverse(operation.queryParams);
                sort(operation.queryParams, new Comparator<CodegenParameter>() {
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

                Iterator<CodegenParameter> iterator = operation.queryParams.iterator();
                while (iterator.hasNext()){
                    CodegenParameter param = iterator.next();
                    param.getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, iterator.hasNext());
                }
            }
        }

        return objs;
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultApi";
        }
        // e.g. phone_number_api => PhoneNumberApi
        return camelize(name);
    }

    @Override
    public String toApiFilename(String name) {
        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_");

        return snakeCase(name);
    }

    @Override
    public String snakeCase(String name) {
        StringBuilder sb = new StringBuilder();
        boolean middleOfWord = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (Character.isUpperCase(c)) {
                if (middleOfWord) {
                    sb.append("_");
                    middleOfWord = false;
                }
            } else {
                middleOfWord = true;
            }

            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    @Override
    public String apiPackage() {
        return "resources/gen";
    }
}
