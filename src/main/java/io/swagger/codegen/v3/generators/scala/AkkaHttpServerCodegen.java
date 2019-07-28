package io.swagger.codegen.v3.generators.scala;

import io.swagger.codegen.v3.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class AkkaHttpServerCodegen extends AbstractScalaCodegen  {
    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaHttpServerCodegen.class);

    protected String groupId = "io.swagger";
    protected String artifactId = "swagger-scala-akka-http-server";
    protected String artifactVersion = "1.0.0";
    protected String invokerPackage = "io.swagger.server";

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "scala-akka-http-server";
    }

    @Override
    public String getHelp() {
        return "Generates an akka http server in scala";
    }

    public AkkaHttpServerCodegen() {
        super();

        //GENERAL PROPERTIES
        additionalProperties.put(CodegenConstants.GROUP_ID, groupId);
        additionalProperties.put(CodegenConstants.ARTIFACT_ID, artifactId);
        additionalProperties.put(CodegenConstants.ARTIFACT_VERSION, artifactVersion);
        additionalProperties.put(CodegenConstants.INVOKER_PACKAGE, invokerPackage);
        apiPackage = "io.swagger.server.api";
        modelPackage = "io.swagger.server.model";

        apiTemplateFiles.put("api.mustache", ".scala");
        modelTemplateFiles.put("model.mustache", ".scala");

        supportingFiles.add(new SupportingFile("build.sbt.mustache", "", "build.sbt"));
        supportingFiles.add(new SupportingFile("controller.mustache",
                (sourceFolder + File.separator + invokerPackage).replace(".", java.io.File.separator), "Controller.scala"));
        supportingFiles.add(new SupportingFile("helper.mustache",
                (sourceFolder + File.separator + invokerPackage).replace(".", java.io.File.separator), "AkkaHttpHelper.scala"));

    }

    @Override
    public void processOpts() {
        super.processOpts();
        embeddedTemplateDir = templateDir = getTemplateDir();
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        return setComplexTypes(objs);
    }

    public static Map<String, Object> setComplexTypes(Map<String, Object> objs) {
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");
        Boolean hasComplexTypes = Boolean.FALSE;
        Boolean hasCookieParams = Boolean.FALSE;
        Set<String> complexRequestTypes = new HashSet<>();
        List<CodegenResponse> complexReturnTypes = new ArrayList<>();
        for (CodegenOperation op : operationList) {
            List<CodegenResponse> complexOperationReturnTypes = new ArrayList<>();
            for(CodegenParameter parameter : op.allParams) {
                if(!parameter.getIsPrimitiveType()){
                    if(parameter.getIsBodyParam()){
                        hasComplexTypes = Boolean.TRUE;
                        complexRequestTypes.add(parameter.dataType);
                    }
                }
                if(parameter.getIsCookieParam()){
                    hasCookieParams = Boolean.TRUE;
                }
            }
            for(CodegenResponse response : op.responses) {
                if(!response.getIsPrimitiveType()){
                    hasComplexTypes = Boolean.TRUE;
                    complexReturnTypes.add(response);
                    complexOperationReturnTypes.add(response);
                }
            }
            op.getVendorExtensions().put("complexReturnTypes", complexOperationReturnTypes);
        }
        objs.put("hasComplexTypes", hasComplexTypes);
        objs.put("hasCookieParams", hasCookieParams);
        objs.put("complexRequestTypes", complexRequestTypes);
        objs.put("complexReturnTypes", complexReturnTypes);

        return objs;
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Schema> schemas, OpenAPI openAPI){
        CodegenOperation codegenOperation =  super.fromOperation(path, httpMethod, operation, schemas, openAPI);

        addLowercaseHttpMethod(codegenOperation);

        addPathMatcher(codegenOperation);
        addQueryParamsWithSupportedType(codegenOperation);
        addAllParamsWithSupportedTypes(codegenOperation);

        return codegenOperation;
    }

    @Override
    public String getDefaultTemplateDir() {
        return "scala/akka-http-server";
    }

    protected static String LOWERCASE_HTTP_METHOD = "lowercaseHttpMethod";

    /**
     *  Provide a lowercase representation of the http method to map to the method directives of akka http
     *  @see <a href="https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/method-directives/index.html">Akka Http Documentation</a>
     */
    protected static void addLowercaseHttpMethod(CodegenOperation codegenOperation) {
        codegenOperation.getVendorExtensions().put(LOWERCASE_HTTP_METHOD, codegenOperation.httpMethod.toLowerCase());
    }

    protected static String PATHS = "paths";

    /**
     *  Split the path as a string to a list of strings to map to the path directives of akka http
     *  @see <a href="https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/path-directives/index.html">Akka Http Documentation</a>
     */
    protected static void addPathMatcher(CodegenOperation codegenOperation) {
        LinkedList<String> allPaths = new LinkedList<>(Arrays.asList(codegenOperation.path.split("/")));
        allPaths.removeIf(""::equals);

        LinkedList<TextOrMatcher> paths = replacePathsWithMatchers(allPaths, codegenOperation);
        codegenOperation.getVendorExtensions().put(PATHS, paths);
    }

    /**
     *  Mapping from parameter data types in path to akka http path matcher
     *  @see <a href="https://doc.akka.io/docs/akka-http/current/routing-dsl/path-matchers.html#basic-pathmatchers">Akka Http Documentation</a>
     */
    private static Map<String, String> pathTypeToMatcher = new HashMap<String, String>(){{
        put("Int", "IntNumber");
        put("Long", "LongNumber");
        put("Float","FloatNumber"); //Custom implementation in AkkaHttpHelper object
        put("Double","DoubleNumber");
        //put("List[???]","???"); TODO Could be implemented with a custom path matcher
        //put("Object","???"); TODO Could be implemented with a custom path matcher
        put("Boolean","Boolean"); //Custom implementation in AkkaHttpHelper object
        put("String", "Segment");
    }};

    protected static String FALLBACK_DATA_TYPE = "String";
    protected static String COOKIE_DATA_TYPE = "HttpCookiePair";

    private static LinkedList<TextOrMatcher> replacePathsWithMatchers(LinkedList<String> paths, CodegenOperation codegenOperation) {
        LinkedList<TextOrMatcher> result = new LinkedList<>();
        for(String path: paths){
            TextOrMatcher textOrMatcher = new TextOrMatcher("", true, true);
            if(path.startsWith("{") && path.endsWith("}")) {
                String parameterName = path.substring(1, path.length()-1);
                for(CodegenParameter pathParam: codegenOperation.pathParams){
                    if(pathParam.paramName.equals(parameterName)) {
                        String matcher = pathTypeToMatcher.get(pathParam.dataType);
                        if(matcher == null) {
                            LOGGER.warn("The path parameter " + pathParam.paramName +
                                    " with the datatype " + pathParam.dataType +
                                    " could not be translated to a corresponding path matcher of akka http" +
                                    " and therefore has been translated to string.");
                            matcher = pathTypeToMatcher.get(FALLBACK_DATA_TYPE);
                        }
                        textOrMatcher.value = matcher;
                        textOrMatcher.isText = false;
                        result.add(textOrMatcher);
                    }
                }
            } else {
                textOrMatcher.value = path;
                textOrMatcher.isText = true;
                result.add(textOrMatcher);
            }
        }
        result.getLast().hasMore = false;
        return result;
    }

    /**
     *  Mapping from parameter data types in java to corresponding data types in java
     */
    private static Set<String> primitiveParamTypes = new HashSet<String>(){{
        addAll(Arrays.asList(
            "Int",
            "Long",
            "Float",
            "Double",
            "Boolean",
            "String"
        ));
    }};


    protected static String QUERY_PARAMS_WITH_SUPPORTED_TYPE = "queryParamsWithSupportedType";

    /**
     *  Replace all not supported types of query parameters by the fallback type
     */
    protected static void addQueryParamsWithSupportedType(CodegenOperation codegenOperation) {
        LinkedList<CodegenParameter> queryParamsWithSupportedType = new LinkedList<CodegenParameter>();
        for(CodegenParameter parameter: codegenOperation.queryParams) {
            CodegenParameter parameterCopy = parameter.copy();
            if(!primitiveParamTypes.contains(parameter.dataType)){
                parameterCopy.dataType = FALLBACK_DATA_TYPE;
            }
            queryParamsWithSupportedType.add(parameterCopy);
        }
        codegenOperation.getVendorExtensions().put(QUERY_PARAMS_WITH_SUPPORTED_TYPE, queryParamsWithSupportedType);
    }

    protected static String PARAMS_WITH_SUPPORTED_TYPE = "paramsWithSupportedType";

    public static void addAllParamsWithSupportedTypes(CodegenOperation codegenOperation) {
        LinkedList<CodegenParameter> allParamsWithSupportedType = new LinkedList<CodegenParameter>();
        for(CodegenParameter parameter: codegenOperation.allParams) {
            CodegenParameter parameterCopy = parameter.copy();
            if(containsParam(codegenOperation.pathParams, parameter)){
                if(!pathTypeToMatcher.containsKey(parameter.dataType)){
                    parameterCopy.dataType = FALLBACK_DATA_TYPE;
                }
            } else if(containsParam(codegenOperation.queryParams, parameter)){
                if(!primitiveParamTypes.contains(parameter.dataType)){
                    parameterCopy.dataType = FALLBACK_DATA_TYPE;
                }
            } else if(containsParam(codegenOperation.formParams, parameter)){
                if(!primitiveParamTypes.contains(parameter.dataType)){
                    parameterCopy.dataType = FALLBACK_DATA_TYPE;
                }
            } else if(parameter.getIsCookieParam()){
                parameterCopy.dataType = COOKIE_DATA_TYPE;
            }
            allParamsWithSupportedType.add(parameterCopy);
        }
        codegenOperation.getVendorExtensions().put(PARAMS_WITH_SUPPORTED_TYPE, allParamsWithSupportedType);
    }

    private static boolean containsParam(List<CodegenParameter> parameters, CodegenParameter param) {
        for(CodegenParameter elem: parameters){
            if(param.paramName.equals(elem.paramName)){
                return true;
            }
        }
        return false;
    }

}

class TextOrMatcher {
    String value;
    boolean isText;
    boolean hasMore;

    public TextOrMatcher(String value, boolean isText, boolean hasMore) {
        this.value = value;
        this.isText = isText;
        this.hasMore = hasMore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextOrMatcher that = (TextOrMatcher) o;
        return isText == that.isText &&
                hasMore == that.hasMore &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isText, hasMore);
    }
}