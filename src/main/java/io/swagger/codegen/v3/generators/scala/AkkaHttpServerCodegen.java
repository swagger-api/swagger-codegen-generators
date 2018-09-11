package io.swagger.codegen.v3.generators.scala;

import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;

import java.io.File;
import java.util.*;

public class AkkaHttpServerCodegen extends AbstractScalaCodegen  {
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
        Provide a lowercase representation of the http method to map to the method directives of akka http
        @see <a href="https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/method-directives/index.html">Akka Http Documentation</a>
     */
    protected static void addLowercaseHttpMethod(CodegenOperation codegenOperation) {
        codegenOperation.getVendorExtensions().put(LOWERCASE_HTTP_METHOD, codegenOperation.httpMethod.toLowerCase());
    }

    protected static String PATHS = "paths";

    /**
     *  Split the path as a string to a list of strings to map to the path directives of akka http
     * @see <a href="https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/path-directives/index.html">Akka Http Documentation</a>
     */
    protected static void splitToPaths(CodegenOperation codegenOperation) {
        LinkedList<String> allPaths = new LinkedList<>(Arrays.asList(codegenOperation.path.split("/")));
        allPaths.removeIf(""::equals);
        Iterator<String> paths = allPaths.iterator();
        codegenOperation.getVendorExtensions().put(PATHS, paths);
    }

}
