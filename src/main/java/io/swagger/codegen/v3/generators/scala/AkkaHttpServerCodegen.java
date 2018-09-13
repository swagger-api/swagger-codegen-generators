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

        //TEMPLATING
        embeddedTemplateDir = templateDir = "v2/scala/akka-http-server";

        apiTemplateFiles.put("api.mustache", ".scala");

        supportingFiles.add(new SupportingFile("build.sbt.mustache", "", "build.sbt"));
        supportingFiles.add(new SupportingFile("controller.mustache",
                (sourceFolder + File.separator + invokerPackage).replace(".", java.io.File.separator), "Controller.scala"));
        supportingFiles.add(new SupportingFile("helper.mustache",
                (sourceFolder + File.separator + invokerPackage).replace(".", java.io.File.separator), "AkkaHttpHelper.scala"));

    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Schema> schemas, OpenAPI openAPI){
        CodegenOperation codegenOperation =  super.fromOperation(path, httpMethod, operation, schemas, openAPI);

        addLowercaseHttpMethod(codegenOperation);
        splitToPaths(codegenOperation);

        return codegenOperation;
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
    protected static String MATCHED_PATH_PARAMS = "matchedPathParams";

    /**
     *  Split the path as a string to a list of strings to map to the path directives of akka http
     *  @see <a href="https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/path-directives/index.html">Akka Http Documentation</a>
     */
    protected static void splitToPaths(CodegenOperation codegenOperation) {
        LinkedList<String> allPaths = new LinkedList<>(Arrays.asList(codegenOperation.path.split("/")));
        allPaths.removeIf(""::equals);

        LinkedList<TextOrMatcher> paths = replacePathsWithMatchers(allPaths, codegenOperation);
        codegenOperation.getVendorExtensions().put(PATHS, paths);

        LinkedList<CodegenParameter> matchedPathParams = new LinkedList<CodegenParameter>();
        for(CodegenParameter parameter: codegenOperation.pathParams) {
            matchedPathParams.add(replaceTypeIfNoMatcherFound(parameter));
        }
        codegenOperation.getVendorExtensions().put(MATCHED_PATH_PARAMS, matchedPathParams);
    }

    /**
     *  Mapping from parameter data types in path to akka http path matcher
     *  @see <a href="https://doc.akka.io/docs/akka-http/current/routing-dsl/path-matchers.html#basic-pathmatchers">Akka Http Documentation</a>
     */
    private static Map<String, String> typeToMatcher = new HashMap<String, String>(){{
        put("Integer", "IntNumber");
        put("Long", "LongNumber");
        put("Float","FloatNumber"); //Custom implementation in AkkaHttpHelper object
        put("Double","DoubleNumber");
        //put("List[???]","???"); TODO Could be implemented with a custom path matcher
        //put("Object","???"); TODO Could be implemented with a custom path matcher
        put("Boolean","Boolean"); //Custom implementation in AkkaHttpHelper object
        put("String", "Segment");
    }};

    private static LinkedList<TextOrMatcher> replacePathsWithMatchers(LinkedList<String> paths, CodegenOperation codegenOperation) {
        LinkedList<TextOrMatcher> result = new LinkedList<>();
        for(String path: paths){
            TextOrMatcher textOrMatcher = new TextOrMatcher("", true, true);
            if(path.startsWith("{") && path.endsWith("}")) {
                String parameterName = path.substring(1, path.length()-1);
                for(CodegenParameter pathParam: codegenOperation.pathParams){
                    if(pathParam.paramName.equals(parameterName)) {
                        String matcher = typeToMatcher.get(pathParam.dataType);
                        if(matcher == null) {
                            LOGGER.warn("The path parameter " + pathParam.paramName +
                                    " with the datatype " + pathParam.dataType +
                                    " could not be translated to a corresponding path matcher of akka http" +
                                    " and therefore has been translated to string.");
                            matcher = typeToMatcher.get("String");
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

    private static CodegenParameter replaceTypeIfNoMatcherFound(CodegenParameter parameter) {
        CodegenParameter result = parameter.copy();
        if(!typeToMatcher.containsKey(parameter.dataType)){
            result.dataType = "String";
        }
        return result;
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