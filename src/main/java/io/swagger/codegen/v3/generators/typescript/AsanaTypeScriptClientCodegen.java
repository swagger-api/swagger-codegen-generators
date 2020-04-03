package io.swagger.codegen.v3.generators.typescript;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenConstants;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;
import io.swagger.codegen.v3.generators.handlebars.java.JavaHelper;


import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.lang.Character;
import java.util.Map;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.Iterator;
import static java.util.Collections.reverse;
import static java.util.Collections.sort;
import java.io.IOException;
import java.io.File;



public class AsanaTypeScriptClientCodegen extends TypeScriptAngularClientCodegen {
    @Override
    public String getName() {
        return "asana-api-explorer";
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
        handlebars.registerHelper("toCamelCase", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                String s = (String)a;
                return camelize(s.toLowerCase());
            }
        });

        handlebars.registerHelper("toSnakeCase", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                String s = (String)a;
                return snakeCase(s.toLowerCase());
            }
        });

        handlebars.registerHelper("toUpperCase", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                String s = (String)a;
                return s.toUpperCase();
            }
        });

        handlebars.registerHelper("getExampleFromJson", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                String s = (String)a;
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonTree = jsonParser.parse(s);
                JsonObject jsonObject = jsonTree.getAsJsonObject();

                return jsonObject.get("example");
            }
        });

        handlebars.registerHelper("parsePath", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                String s = (String)a;
                String[] split = s.split("/");
                for (int i = 0; i < split.length; i++) {
                    if (split[i].startsWith("$")) {
                        split[i] = "%s";
                    }
                }
        
                String processedPath = String.join("/", split);
                return processedPath;
            }
        });
    }
    
    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> operations) {
        Map<String, Object> objs = (Map<String, Object>) operations.get("operations");

        // Add filename information for api imports
        objs.put("apiFilename", getApiFilenameFromClassname(objs.get("classname").toString()));

        List<CodegenOperation> ops = (List<CodegenOperation>) objs.get("operation");
        for (CodegenOperation op : ops) {
            if ((boolean) additionalProperties.get("useHttpClient")) {
                op.httpMethod = op.httpMethod.toLowerCase(Locale.ENGLISH);
            } else {
                // Convert httpMethod to Angular's RequestMethod enum
                // https://angular.io/docs/ts/latest/api/http/index/RequestMethod-enum.html
                switch (op.httpMethod) {
                case "GET":
                    op.httpMethod = "RequestMethod.Get";
                    break;
                case "POST":
                    op.httpMethod = "RequestMethod.Post";
                    break;
                case "PUT":
                    op.httpMethod = "RequestMethod.Put";
                    break;
                case "DELETE":
                    op.httpMethod = "RequestMethod.Delete";
                    break;
                case "OPTIONS":
                    op.httpMethod = "RequestMethod.Options";
                    break;
                case "HEAD":
                    op.httpMethod = "RequestMethod.Head";
                    break;
                case "PATCH":
                    op.httpMethod = "RequestMethod.Patch";
                    break;
                default:
                    throw new RuntimeException("Unknown HTTP Method " + op.httpMethod + " not allowed");
                }
            }


            if (op.queryParams != null) {
                reverse(op.queryParams);
                List<String> commonParams = Arrays.asList(new String[]{"opt_pretty", "opt_fields", "limit", "offset"});

                op.queryParams.removeIf(queryParam -> commonParams.contains(queryParam.baseName));
                
                sort(op.queryParams, new Comparator<CodegenParameter>() {
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
                        return 0;
                    }
                });

                Iterator<CodegenParameter> iterator = op.queryParams.iterator();
                while (iterator.hasNext()){
                    CodegenParameter param = iterator.next();
                    param.getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, iterator.hasNext());
                }
            }

            // Prep a string buffer where we're going to set up our new version of the string.
            StringBuilder pathBuffer = new StringBuilder();
            StringBuilder parameterName = new StringBuilder();
            int insideCurly = 0;

            // Iterate through existing string, one character at a time.
            for (int i = 0; i < op.path.length(); i++) {
                switch (op.path.charAt(i)) {
                case '{':
                    // We entered curly braces, so track that.
                    insideCurly++;

                    // Add the more complicated component instead of just the brace.
                    pathBuffer.append("${encodeURIComponent(String(");
                    break;
                case '}':
                    // We exited curly braces, so track that.
                    insideCurly--;

                    // Add the more complicated component instead of just the brace.
                    pathBuffer.append(toVarName(parameterName.toString()));
                    pathBuffer.append("))}");
                    parameterName.setLength(0);
                    break;
                default:
                    if (insideCurly > 0) {
                        parameterName.append(op.path.charAt(i));
                    } else {
                        pathBuffer.append(op.path.charAt(i));
                    }
                    break;
                }
            }

            // Overwrite path to TypeScript template string, after applying everything we just did.
            op.path = pathBuffer.toString();
        }

        // Add additional filename information for model imports in the services
        List<Map<String, Object>> imports = (List<Map<String, Object>>) operations.get("imports");
        for (Map<String, Object> im : imports) {
            im.put("filename", im.get("import"));
            im.put("classname", getModelnameFromModelFilename(im.get("filename").toString()));
        }

        return operations;
    }

    @Override
    public String toApiFilename(String name) {
        if (name.length() == 0) {
            return "default";
        }
        return snakeCase(name) + "_base";
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

    private String getApiFilenameFromClassname(String classname) {
        String name = classname.substring(0, classname.length() - "Service".length());
        return toApiFilename(name);
    }

    private String getModelnameFromModelFilename(String filename) {
        String name = filename.substring((modelPackage() + File.separator).length());
        return camelize(name);
    }

    @Override
    public String apiPackage() {
        return "src/resources/gen";
    }
}
