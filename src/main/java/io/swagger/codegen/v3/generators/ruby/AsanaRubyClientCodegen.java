package io.swagger.codegen.v3.generators.ruby;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.generators.python.PythonClientCodegen;
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

import java.io.File;
import java.util.*;

import static io.swagger.codegen.languages.RubyClientCodegen.*;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;
import static java.util.Collections.reverse;
import static java.util.Collections.sort;

import java.io.IOException;

public class AsanaRubyClientCodegen extends PythonClientCodegen {
    public AsanaRubyClientCodegen() {
        super();

        modelPackage = "models";
        apiPackage = "api";
        outputFolder = "generated-code" + File.separator + "ruby";
        modelTemplateFiles.put("model.mustache", ".rb");
        apiTemplateFiles.put("api.mustache", ".rb");
        modelDocTemplateFiles.put("model_doc.mustache", ".md");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");
        embeddedTemplateDir = templateDir = "ruby-client";

        // default HIDE_GENERATION_TIMESTAMP to true
        hideGenerationTimestamp = Boolean.TRUE;

        // local variable names used in API methods (endpoints)
        for (String word : Arrays.asList(
            "local_var_path", "query_params", "header_params", "_header_accept", "_header_accept_result",
            "_header_content_type", "form_params", "post_body", "auth_names", "send")) {
            reservedWords.add(word.toLowerCase(Locale.ROOT));
        }

        languageSpecificPrimitives.add("int");
        languageSpecificPrimitives.add("array");
        languageSpecificPrimitives.add("map");
        languageSpecificPrimitives.add("string");

        // remove modelPackage and apiPackage added by default
        cliOptions.removeIf(opt -> CodegenConstants.MODEL_PACKAGE.equals(opt.getOpt()) ||
            CodegenConstants.API_PACKAGE.equals(opt.getOpt()));

        cliOptions.add(new CliOption(GEM_NAME, GEM_NAME).
            defaultValue("swagger_client"));

        cliOptions.add(new CliOption(MODULE_NAME, MODULE_NAME).
            defaultValue("SwaggerClient"));

        cliOptions.add(new CliOption(GEM_VERSION, "gem version.").defaultValue("1.0.0"));

        cliOptions.add(new CliOption(GEM_LICENSE, "gem license. ").
            defaultValue("unlicense"));

        cliOptions.add(new CliOption(GEM_REQUIRED_RUBY_VERSION, "gem required Ruby version. ").
            defaultValue(">= 1.9"));

        cliOptions.add(new CliOption(GEM_HOMEPAGE, "gem homepage. ").
            defaultValue("http://org.openapitools"));

        cliOptions.add(new CliOption(GEM_SUMMARY, "gem summary. ").
            defaultValue("A ruby wrapper for the REST APIs"));

        cliOptions.add(new CliOption(GEM_DESCRIPTION, "gem description. ").
            defaultValue("This gem maps to a REST API"));

        cliOptions.add(new CliOption(GEM_AUTHOR, "gem author (only one is supported)."));

        cliOptions.add(new CliOption(GEM_AUTHOR_EMAIL, "gem author email (only one is supported)."));

        cliOptions.add(new CliOption(CodegenConstants.HIDE_GENERATION_TIMESTAMP, CodegenConstants.HIDE_GENERATION_TIMESTAMP_DESC).
            defaultValue(Boolean.TRUE.toString()));

        supportedLibraries.put("faraday", "Faraday (https://github.com/lostisland/faraday) (Beta support)");
        supportedLibraries.put("typhoeus", "Typhoeus >= 1.0.1 (https://github.com/typhoeus/typhoeus)");

        CliOption libraryOption = new CliOption(CodegenConstants.LIBRARY, "HTTP library template (sub-template) to use");
        libraryOption.setEnum(supportedLibraries);
        // set TYPHOEUS as the default
        libraryOption.setDefault("typhoeus");
        cliOptions.add(libraryOption);
        setLibrary("typhoeus");
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
        handlebars.registerHelper("toLowerCase", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                return ((String)a).toLowerCase();
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
            if (operation.returnType.startsWith("list")) {
                operation.returnType = operation.returnType.substring(5, operation.returnType.indexOf("]"));
                operation.returnContainer = "get_collection";
            } else {
                operation.returnContainer = "get";
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
    public String getName() {
        return "asana-ruby";
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

        // e.g. PhoneNumberApi.py => phone_number_api.py
        return underscore(name);
    }

    @Override
    public String apiFileFolder() {
        return "lib" + File.separatorChar + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String apiPackage() {
        return "asana.resources.gen";
    }
}
