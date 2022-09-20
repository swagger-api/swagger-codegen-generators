package io.swagger.codegen.v3.generators.java;

import static io.swagger.codegen.v3.generators.java.JavaVertXServerCodegen.MOUNT_OPERATION_FROM_EXTENSIONS;
import static io.swagger.codegen.v3.generators.java.JavaVertXServerCodegen.MOUNT_OPERATION_FROM_INTERFACE;
import static io.swagger.codegen.v3.generators.java.JavaVertXServerCodegen.MOUNT_OPERATION_FROM_OPTION;
import static io.swagger.codegen.v3.generators.java.JavaVertXServerCodegen.RX_INTERFACE_OPTION;
import static io.swagger.codegen.v3.generators.java.JavaVertXServerCodegen.USE_DATAOBJECT_OPTION;
import static io.swagger.codegen.v3.generators.java.JavaVertXServerCodegen.USE_FUTURE_OPTION;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.CodegenArgument;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.rules.TemporaryFolder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JavaVertxServerGeneratorCodegenTest extends AbstractCodegenTest {

    static final String PACKAGE_INFO = "package-info.java";

    static final List<String> SERVICE_AS_EXTENSION_FILENAMES = Arrays.asList("GetOrderByIdAddressApi.java",
      "MyFeedPetAddressApi.java", "MyUploadFileAddressApi.java", "DeleteOrderAddressApi.java",
      "MyFindPetsByStatusAddressApi.java", "PlaceOrderAddressApi.java",
      "MyPetAddressApi.java", "UserAddressApi.java", "MyPetIdAddressApi.java",
      "GetInventoryAddressApi.java", "PetAddressApi.java", "MyFindByTagsAddressApi.java",
      "TestAddressApi.java", "MyGetPetByIdAddressApi.java"
    );

    static final Map<String, String> SERVICE_AS_INTERFACE = new HashMap<String, String>() {{
        put("DefaultApi.java", "DefaultApi");
        put("PetApi.java", "PetApi");
        put("StoreApi.java", "StoreApi");
        put("UserApi.java", "UserApi");
    }};

    private TemporaryFolder folder = null;

    @BeforeMethod
    public void setUp() throws Exception {
        folder = new TemporaryFolder();
        folder.create();
    }

    @AfterMethod
    public void tearDown() {
        folder.delete();
    }

    @Test(description = "verify that main verticle, openapi verticle and service are written as expected (OAS 2.x & web-api-service & Future)")
    public void testUseOas2AndWebApiServiceAndFuture() throws Exception {
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
          .setLang("java-vertx")
          .setInputSpecURL("src/test/resources/3_0_0/petstore-vertx.yaml")
          .setOutputDir(output.getAbsolutePath())
          .addAdditionalProperty(USE_DATAOBJECT_OPTION, true)
          .addAdditionalProperty(MOUNT_OPERATION_FROM_OPTION, MOUNT_OPERATION_FROM_EXTENSIONS)
          .addAdditionalProperty(USE_FUTURE_OPTION, true);

        configurator.setCodegenArguments(Collections.singletonList(
          new CodegenArgument()
            .option(CodegenConstants.USE_OAS2_OPTION)
            .type("boolean")
            .value(Boolean.TRUE.toString())));

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output,
          "/src/main/java/io/swagger/server/api/MainApiVerticle.java");
        String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains(
          "vertx.deployVerticle(\"io.swagger.server.api.verticle.SwaggerPetstoreVerticle\")"));
        Assert.assertTrue(content.contains("startPromise.fail(error);"));
        Assert.assertTrue(content.contains("startPromise.complete();"));

        final File petVerticleFile = new File(output,
          "/src/main/java/io/swagger/server/api/verticle/SwaggerPetstoreVerticle.java");
        content = FileUtils.readFileToString(petVerticleFile);

        Assert.assertTrue(content.contains("RouterBuilder.create(this.vertx, \"openapi.yaml\")"));
        Assert.assertTrue(content.contains("routerBuilder.mountServicesFromExtensions();"));
        Assert.assertTrue(content.contains("router.route(\"/*\").subRouter(openapiRouter);"));
        Assert.assertTrue(content.contains("onSuccess(server -> startPromise.complete())"));
        Assert.assertTrue(content.contains("onFailure(startPromise::fail);"));

        final File packageInfoModelFile = new File(output,
          "/src/main/java/io/swagger/server/api/model/" + PACKAGE_INFO);
        content = FileUtils.readFileToString(packageInfoModelFile);
        Assert.assertTrue(content.contains("@ModuleGen(name = \"model\", groupPackage = \"io.swagger.server.api.model\")"));
        Assert.assertTrue(content.contains("package io.swagger.server.api.model;"));
        Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));

        final File petServiceFiles = new File(output,
          "/src/main/java/io/swagger/server/api/service");
        for (File file : petServiceFiles.listFiles()) {
            Assert.assertTrue(SERVICE_AS_EXTENSION_FILENAMES.contains(file.getName()) || PACKAGE_INFO.equals(file.getName()));
            content = FileUtils.readFileToString(file);

            if (PACKAGE_INFO.equals(file.getName())) {
                Assert.assertTrue(content.contains("@ModuleGen(name = \"service\", groupPackage = \"io.swagger.server.api.service\", useFutures = true)"));
                Assert.assertTrue(content.contains("package io.swagger.server.api.service;"));
                Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));
            } else {
                Assert.assertTrue(content.contains("@WebApiServiceGen"));
                Assert.assertTrue(content.contains("String WEBSERVICE_ADDRESS_"));
                Assert.assertTrue(content.contains("Future<ServiceResponse>"));
                Assert.assertTrue(content.contains("ServiceRequest request);"));
                Assert.assertFalse(content.contains("Handler<AsyncResult<ServiceResponse>>"));
            }
        }
    }

    @Test(description = "verify that main verticle, openapi verticle and service are written as expected (OAS 2.x & web-api-service and not future)")
    public void testUseOas2AndWebApiServiceAndNotFuture() throws Exception {
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
          .setLang("java-vertx")
          .setInputSpecURL("src/test/resources/3_0_0/petstore-vertx.yaml")
          .setOutputDir(output.getAbsolutePath())
          .addAdditionalProperty(USE_DATAOBJECT_OPTION, true)
          .addAdditionalProperty(MOUNT_OPERATION_FROM_OPTION, MOUNT_OPERATION_FROM_EXTENSIONS);

        configurator.setCodegenArguments(Collections.singletonList(
          new CodegenArgument()
            .option(CodegenConstants.USE_OAS2_OPTION)
            .type("boolean")
            .value(Boolean.TRUE.toString())));

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output,
          "/src/main/java/io/swagger/server/api/MainApiVerticle.java");
        String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains(
          "vertx.deployVerticle(\"io.swagger.server.api.verticle.SwaggerPetstoreVerticle\")"));
        Assert.assertTrue(content.contains("startPromise.fail(error);"));
        Assert.assertTrue(content.contains("startPromise.complete();"));

        final File petVerticleFile = new File(output,
          "/src/main/java/io/swagger/server/api/verticle/SwaggerPetstoreVerticle.java");
        content = FileUtils.readFileToString(petVerticleFile);

        Assert.assertTrue(content.contains("RouterBuilder.create(this.vertx, \"openapi.yaml\")"));
        Assert.assertTrue(content.contains("routerBuilder.mountServicesFromExtensions();"));
        Assert.assertTrue(content.contains("router.route(\"/*\").subRouter(openapiRouter);"));
        Assert.assertTrue(content.contains("onSuccess(server -> startPromise.complete())"));
        Assert.assertTrue(content.contains("onFailure(startPromise::fail);"));

        final File packageInfoModelFile = new File(output,
          "/src/main/java/io/swagger/server/api/model/" + PACKAGE_INFO);
        content = FileUtils.readFileToString(packageInfoModelFile);
        Assert.assertTrue(content.contains("@ModuleGen(name = \"model\", groupPackage = \"io.swagger.server.api.model\")"));
        Assert.assertTrue(content.contains("package io.swagger.server.api.model;"));
        Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));

        final File petServiceFiles = new File(output,
          "/src/main/java/io/swagger/server/api/service");
        for (File file : petServiceFiles.listFiles()) {
            Assert.assertTrue(SERVICE_AS_EXTENSION_FILENAMES.contains(file.getName()) || PACKAGE_INFO.equals(file.getName()));
            content = FileUtils.readFileToString(file);

            if (PACKAGE_INFO.equals(file.getName())) {
                Assert.assertTrue(content.contains("@ModuleGen(name = \"service\", groupPackage = \"io.swagger.server.api.service\", useFutures = false)"));
                Assert.assertTrue(content.contains("package io.swagger.server.api.service;"));
                Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));
            } else {
                Assert.assertTrue(content.contains("@WebApiServiceGen"));
                Assert.assertTrue(content.contains("String WEBSERVICE_ADDRESS_"));
                Assert.assertFalse(content.contains("Future<ServiceResponse>"));
                Assert.assertTrue(content.contains("ServiceRequest request,"));
                Assert.assertTrue(content.contains("Handler<AsyncResult<ServiceResponse>> resultHandler);"));
            }
        }
    }

    @Test(description = "verify that main verticle, openapi verticle and service are written as expected (OAS 3.x & web-api-service & Future)")
    public void testUseOas3AndWebApiServiceAndFuture() throws Exception {
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
          .setLang("java-vertx")
          .setInputSpecURL("src/test/resources/3_0_0/petstore-vertx.yaml")
          .setOutputDir(output.getAbsolutePath())
          .addAdditionalProperty(USE_DATAOBJECT_OPTION, true)
          .addAdditionalProperty(MOUNT_OPERATION_FROM_OPTION, MOUNT_OPERATION_FROM_EXTENSIONS)
          .addAdditionalProperty(USE_FUTURE_OPTION, true);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output,
          "/src/main/java/io/swagger/server/api/MainApiVerticle.java");
        String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains(
          "vertx.deployVerticle(\"io.swagger.server.api.verticle.SwaggerPetstoreVerticle\")"));
        Assert.assertTrue(content.contains("startPromise.fail(error);"));
        Assert.assertTrue(content.contains("startPromise.complete();"));

        final File petVerticleFile = new File(output,
          "/src/main/java/io/swagger/server/api/verticle/SwaggerPetstoreVerticle.java");
        content = FileUtils.readFileToString(petVerticleFile);

        Assert.assertTrue(
          content.contains("RouterBuilder.create(this.vertx, \"openapi.yaml\")"));
        Assert.assertTrue(content.contains("routerBuilder.mountServicesFromExtensions();"));
        Assert.assertTrue(content.contains("router.route(\"/*\").subRouter(openapiRouter);"));
        Assert.assertTrue(content.contains("onSuccess(server -> startPromise.complete())"));
        Assert.assertTrue(content.contains("onFailure(startPromise::fail);"));

        final File packageInfoModelFile = new File(output,
          "/src/main/java/io/swagger/server/api/model/" + PACKAGE_INFO);
        content = FileUtils.readFileToString(packageInfoModelFile);
        Assert.assertTrue(content.contains("@ModuleGen(name = \"model\", groupPackage = \"io.swagger.server.api.model\")"));
        Assert.assertTrue(content.contains("package io.swagger.server.api.model;"));
        Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));

        final File petServiceFiles = new File(output,
          "/src/main/java/io/swagger/server/api/service");
        for (File file : petServiceFiles.listFiles()) {
            Assert.assertTrue(SERVICE_AS_EXTENSION_FILENAMES.contains(file.getName()) || PACKAGE_INFO.equals(file.getName()));
            content = FileUtils.readFileToString(file);

            if (PACKAGE_INFO.equals(file.getName())) {
                Assert.assertTrue(content.contains("@ModuleGen(name = \"service\", groupPackage = \"io.swagger.server.api.service\", useFutures = true)"));
                Assert.assertTrue(content.contains("package io.swagger.server.api.service;"));
                Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));
            } else {
                Assert.assertTrue(content.contains("@WebApiServiceGen"));
                Assert.assertTrue(content.contains("String WEBSERVICE_ADDRESS_"));
                Assert.assertTrue(content.contains("Future<ServiceResponse>"));
                Assert.assertTrue(content.contains("ServiceRequest request);"));
                Assert.assertFalse(content.contains("Handler<AsyncResult<ServiceResponse>>"));
            }
        }
    }

    @Test(description = "verify that main verticle, openapi verticle and service are written as expected (OAS 3.x & web-api-service and not future)")
    public void testUseOas3AndWebApiServiceAndNotFuture() throws Exception {
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
          .setLang("java-vertx")
          .setInputSpecURL("src/test/resources/3_0_0/petstore-vertx.yaml")
          .setOutputDir(output.getAbsolutePath())
          .addAdditionalProperty(USE_DATAOBJECT_OPTION, true)
          .addAdditionalProperty(MOUNT_OPERATION_FROM_OPTION, MOUNT_OPERATION_FROM_EXTENSIONS);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output,
          "/src/main/java/io/swagger/server/api/MainApiVerticle.java");
        String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains(
          "vertx.deployVerticle(\"io.swagger.server.api.verticle.SwaggerPetstoreVerticle\")"));
        Assert.assertTrue(content.contains("startPromise.fail(error);"));
        Assert.assertTrue(content.contains("startPromise.complete();"));

        final File petVerticleFile = new File(output,
          "/src/main/java/io/swagger/server/api/verticle/SwaggerPetstoreVerticle.java");
        content = FileUtils.readFileToString(petVerticleFile);

        Assert.assertTrue(
          content.contains("RouterBuilder.create(this.vertx, \"openapi.yaml\")"));
        Assert.assertTrue(content.contains("routerBuilder.mountServicesFromExtensions();"));
        Assert.assertTrue(content.contains("router.route(\"/*\").subRouter(openapiRouter);"));
        Assert.assertTrue(content.contains("onSuccess(server -> startPromise.complete())"));
        Assert.assertTrue(content.contains("onFailure(startPromise::fail);"));

        final File packageInfoModelFile = new File(output,
          "/src/main/java/io/swagger/server/api/model/" + PACKAGE_INFO);
        content = FileUtils.readFileToString(packageInfoModelFile);
        Assert.assertTrue(content.contains("@ModuleGen(name = \"model\", groupPackage = \"io.swagger.server.api.model\")"));
        Assert.assertTrue(content.contains("package io.swagger.server.api.model;"));
        Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));

        final File petServiceFiles = new File(output,
          "/src/main/java/io/swagger/server/api/service");
        for (File file : petServiceFiles.listFiles()) {
            Assert.assertTrue(SERVICE_AS_EXTENSION_FILENAMES.contains(file.getName()) || PACKAGE_INFO.equals(file.getName()));
            content = FileUtils.readFileToString(file);

            if (PACKAGE_INFO.equals(file.getName())) {
                Assert.assertTrue(content.contains("@ModuleGen(name = \"service\", groupPackage = \"io.swagger.server.api.service\", useFutures = false)"));
                Assert.assertTrue(content.contains("package io.swagger.server.api.service;"));
                Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));
            } else {
                Assert.assertTrue(content.contains("@WebApiServiceGen"));
                Assert.assertTrue(content.contains("String WEBSERVICE_ADDRESS_"));
                Assert.assertTrue(content.contains("ServiceRequest request,"));
                Assert.assertFalse(content.contains("Future<ServiceResponse>"));
                Assert.assertTrue(content.contains("Handler<AsyncResult<ServiceResponse>> resultHandler);"));
            }
        }
    }

    @Test(description = "verify that main verticle, openapi verticle and service are written as expected (OAS 3.x & web-api-service & Future)")
    public void testRxUseOas3AndWebApiServiceAndFuture() throws Exception {
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
          .setLang("java-vertx")
          .setInputSpecURL("src/test/resources/3_0_0/petstore-vertx.yaml")
          .setOutputDir(output.getAbsolutePath())
          .addAdditionalProperty(USE_DATAOBJECT_OPTION, true)
          .addAdditionalProperty(MOUNT_OPERATION_FROM_OPTION, MOUNT_OPERATION_FROM_EXTENSIONS)
          .addAdditionalProperty(RX_INTERFACE_OPTION, true)
          .addAdditionalProperty(USE_FUTURE_OPTION, true);

        final List<CodegenArgument> arguments = new ArrayList<>();
        arguments.add(new CodegenArgument()
          .option(RX_INTERFACE_OPTION)
          .type("boolean")
          .value(Boolean.TRUE.toString()));
        configurator.setCodegenArguments(arguments);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output,
          "/src/main/java/io/swagger/server/api/MainApiVerticle.java");
        String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains(
          "vertx.rxDeployVerticle(\"io.swagger.server.api.verticle.SwaggerPetstoreVerticle\")"));
        Assert.assertFalse(content.contains("startPromise.fail(error);"));
        Assert.assertFalse(content.contains("startPromise.complete();"));
        Assert.assertTrue(content.contains("ignoreElement();"));

        final File petVerticleFile = new File(output,
          "/src/main/java/io/swagger/server/api/verticle/SwaggerPetstoreVerticle.java");
        content = FileUtils.readFileToString(petVerticleFile);

        Assert.assertTrue(content.contains("return RouterBuilder.rxCreate(this.vertx, \"openapi.yaml\")"));
        Assert.assertTrue(content.contains("routerBuilder.mountServicesFromExtensions();"));
        Assert.assertTrue(content.contains("router.route(\"/*\").subRouter(openapiRouter);"));
        Assert.assertFalse(content.contains("onSuccess(server -> startPromise.complete())"));
        Assert.assertFalse(content.contains("onFailure(startPromise::fail);"));
        Assert.assertTrue(content.contains("rxStart()"));
        Assert.assertTrue(content.contains("rxStop()"));
        Assert.assertTrue(content.contains("rxListen()"));
        Assert.assertTrue(content.contains("ignoreElement();"));

        final File packageInfoModelFile = new File(output,
          "/src/main/java/io/swagger/server/api/model/" + PACKAGE_INFO);
        content = FileUtils.readFileToString(packageInfoModelFile);
        Assert.assertTrue(content.contains("@ModuleGen(name = \"model\", groupPackage = \"io.swagger.server.api.model\")"));
        Assert.assertTrue(content.contains("package io.swagger.server.api.model;"));
        Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));

        final File petServiceFiles = new File(output,
          "/src/main/java/io/swagger/server/api/service");
        for (File file : petServiceFiles.listFiles()) {
            Assert.assertTrue(SERVICE_AS_EXTENSION_FILENAMES.contains(file.getName()) || PACKAGE_INFO.equals(file.getName()));
            content = FileUtils.readFileToString(file);

            if (PACKAGE_INFO.equals(file.getName())) {
                Assert.assertTrue(content.contains("@ModuleGen(name = \"service\", groupPackage = \"io.swagger.server.api.service\", useFutures = true)"));
                Assert.assertTrue(content.contains("package io.swagger.server.api.service;"));
                Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));
            } else {
                Assert.assertTrue(content.contains("@WebApiServiceGen"));
                Assert.assertTrue(content.contains("String WEBSERVICE_ADDRESS_"));
                Assert.assertTrue(content.contains("Future<ServiceResponse>"));
                Assert.assertTrue(content.contains("ServiceRequest request);"));
            }

        }

    }

    @Test(description = "verify that main verticle, openapi verticle and service are written as expected (OAS 3.x & web-api-service & not Future)")
    public void testRxUseOas3AndWebApiServiceAndNotFuture() throws Exception {
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
          .setLang("java-vertx")
          .setInputSpecURL("src/test/resources/3_0_0/petstore-vertx.yaml")
          .setOutputDir(output.getAbsolutePath())
          .addAdditionalProperty(USE_DATAOBJECT_OPTION, true)
          .addAdditionalProperty(MOUNT_OPERATION_FROM_OPTION, MOUNT_OPERATION_FROM_EXTENSIONS)
          .addAdditionalProperty(RX_INTERFACE_OPTION, true);

        final List<CodegenArgument> arguments = new ArrayList<>();
        arguments.add(new CodegenArgument()
          .option(RX_INTERFACE_OPTION)
          .type("boolean")
          .value(Boolean.TRUE.toString()));
        configurator.setCodegenArguments(arguments);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output,
          "/src/main/java/io/swagger/server/api/MainApiVerticle.java");
        String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains(
          "vertx.rxDeployVerticle(\"io.swagger.server.api.verticle.SwaggerPetstoreVerticle\")"));
        Assert.assertFalse(content.contains("startPromise.fail(error);"));
        Assert.assertFalse(content.contains("startPromise.complete();"));
        Assert.assertTrue(content.contains("ignoreElement();"));

        final File petVerticleFile = new File(output,
          "/src/main/java/io/swagger/server/api/verticle/SwaggerPetstoreVerticle.java");
        content = FileUtils.readFileToString(petVerticleFile);

        Assert.assertTrue(content.contains("return RouterBuilder.rxCreate(this.vertx, \"openapi.yaml\")"));
        Assert.assertTrue(content.contains("routerBuilder.mountServicesFromExtensions();"));
        Assert.assertTrue(content.contains("router.route(\"/*\").subRouter(openapiRouter);"));
        Assert.assertFalse(content.contains("onSuccess(server -> startPromise.complete())"));
        Assert.assertFalse(content.contains("onFailure(startPromise::fail);"));
        Assert.assertTrue(content.contains("rxStart()"));
        Assert.assertTrue(content.contains("rxStop()"));
        Assert.assertTrue(content.contains("rxListen()"));
        Assert.assertTrue(content.contains("ignoreElement();"));

        final File packageInfoModelFile = new File(output,
          "/src/main/java/io/swagger/server/api/model/" + PACKAGE_INFO);
        content = FileUtils.readFileToString(packageInfoModelFile);
        Assert.assertTrue(content.contains("@ModuleGen(name = \"model\", groupPackage = \"io.swagger.server.api.model\")"));
        Assert.assertTrue(content.contains("package io.swagger.server.api.model;"));
        Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));

        final File petServiceFiles = new File(output,
          "/src/main/java/io/swagger/server/api/service");
        for (File file : petServiceFiles.listFiles()) {
            Assert.assertTrue(SERVICE_AS_EXTENSION_FILENAMES.contains(file.getName()) || PACKAGE_INFO.equals(file.getName()));
            content = FileUtils.readFileToString(file);

            if (PACKAGE_INFO.equals(file.getName())) {
                Assert.assertTrue(content.contains("@ModuleGen(name = \"service\", groupPackage = \"io.swagger.server.api.service\", useFutures = false)"));
                Assert.assertTrue(content.contains("package io.swagger.server.api.service;"));
                Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));
            } else {
                Assert.assertTrue(content.contains("@WebApiServiceGen"));
                Assert.assertTrue(content.contains("String WEBSERVICE_ADDRESS_"));
                Assert.assertTrue(content.contains("ServiceRequest request,"));
                Assert.assertFalse(content.contains("Future<ServiceResponse>"));
                Assert.assertTrue(content.contains("Handler<AsyncResult<ServiceResponse>> resultHandler);"));
            }

        }

    }

    @Test(description = "verify that main verticle, openapi verticle and service are written as expected (OAS 3.x & mount from interface & future)")
    public void testUseOas3AndMountFromInterfaceAndFuture() throws Exception {
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
          .setLang("java-vertx")
          .setInputSpecURL("src/test/resources/3_0_0/petstore-vertx.yaml")
          .setOutputDir(output.getAbsolutePath())
          .addAdditionalProperty(USE_DATAOBJECT_OPTION, true)
          .addAdditionalProperty(MOUNT_OPERATION_FROM_OPTION, MOUNT_OPERATION_FROM_INTERFACE)
          .addAdditionalProperty(USE_FUTURE_OPTION, true);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output,
          "/src/main/java/io/swagger/server/api/MainApiVerticle.java");
        String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains("vertx.deployVerticle(\"io.swagger.server.api.verticle.SwaggerPetstoreVerticle\")"));
        Assert.assertTrue(content.contains("startPromise.fail(error);"));
        Assert.assertTrue(content.contains("startPromise.complete();"));

        final File petVerticleFile = new File(output,
          "/src/main/java/io/swagger/server/api/verticle/SwaggerPetstoreVerticle.java");
        content = FileUtils.readFileToString(petVerticleFile);

        Assert.assertTrue(content.contains("RouterBuilder.create(this.vertx, \"openapi.yaml\")"));
        Assert.assertFalse(content.contains("routerBuilder.mountServicesFromExtensions();"));
        Assert.assertTrue(content.contains("routerBuilder.mountServiceInterface"));
        Assert.assertTrue(content.contains("router.route(\"/*\").subRouter(openapiRouter);"));
        Assert.assertTrue(content.contains("onSuccess(server -> startPromise.complete())"));
        Assert.assertTrue(content.contains("onFailure(startPromise::fail);"));

        final File packageInfoModelFile = new File(output,
          "/src/main/java/io/swagger/server/api/model/" + PACKAGE_INFO);
        content = FileUtils.readFileToString(packageInfoModelFile);
        Assert.assertTrue(content.contains("@ModuleGen(name = \"model\", groupPackage = \"io.swagger.server.api.model\")"));
        Assert.assertTrue(content.contains("package io.swagger.server.api.model;"));
        Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));

        final File petServiceFiles = new File(output,
          "/src/main/java/io/swagger/server/api/service");
        for (File file : petServiceFiles.listFiles()) {
            Assert.assertTrue(SERVICE_AS_INTERFACE.containsKey(file.getName()) || PACKAGE_INFO.equals(file.getName()));
            content = FileUtils.readFileToString(file);

            if (PACKAGE_INFO.equals(file.getName())) {
                Assert.assertTrue(content.contains("@ModuleGen(name = \"service\", groupPackage = \"io.swagger.server.api.service\", useFutures = true)"));
                Assert.assertTrue(content.contains("package io.swagger.server.api.service;"));
                Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));
            } else {
                String api = SERVICE_AS_INTERFACE.getOrDefault(file.getName(), PACKAGE_INFO);
                Assert.assertFalse(content.contains("@WebApiServiceGen"));
                Assert.assertTrue(content.contains("String WEBSERVICE_ADDRESS_" + api.toUpperCase()));
                Assert.assertTrue(content.contains("Future<ServiceResponse>"));
                Assert.assertTrue(content.contains("ServiceRequest request);"));
            }
        }
    }

    @Test(description = "verify that main verticle, openapi verticle and service are written as expected (OAS 3.x & mount from interface & not future)")
    public void testUseOas3AndMountFromInterfaceAndNotFuture() throws Exception {
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
          .setLang("java-vertx")
          .setInputSpecURL("src/test/resources/3_0_0/petstore-vertx.yaml")
          .setOutputDir(output.getAbsolutePath())
          .addAdditionalProperty(USE_DATAOBJECT_OPTION, true)
          .addAdditionalProperty(MOUNT_OPERATION_FROM_OPTION, MOUNT_OPERATION_FROM_INTERFACE);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output,
          "/src/main/java/io/swagger/server/api/MainApiVerticle.java");
        String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains("vertx.deployVerticle(\"io.swagger.server.api.verticle.SwaggerPetstoreVerticle\")"));
        Assert.assertTrue(content.contains("startPromise.fail(error);"));
        Assert.assertTrue(content.contains("startPromise.complete();"));

        final File petVerticleFile = new File(output,
          "/src/main/java/io/swagger/server/api/verticle/SwaggerPetstoreVerticle.java");
        content = FileUtils.readFileToString(petVerticleFile);

        Assert.assertTrue(content.contains("RouterBuilder.create(this.vertx, \"openapi.yaml\")"));
        Assert.assertFalse(content.contains("routerBuilder.mountServicesFromExtensions();"));
        Assert.assertTrue(content.contains("routerBuilder.mountServiceInterface"));
        Assert.assertTrue(content.contains("router.route(\"/*\").subRouter(openapiRouter);"));
        Assert.assertTrue(content.contains("onSuccess(server -> startPromise.complete())"));
        Assert.assertTrue(content.contains("onFailure(startPromise::fail);"));

        final File packageInfoModelFile = new File(output,
          "/src/main/java/io/swagger/server/api/model/" + PACKAGE_INFO);
        content = FileUtils.readFileToString(packageInfoModelFile);
        Assert.assertTrue(content.contains("@ModuleGen(name = \"model\", groupPackage = \"io.swagger.server.api.model\")"));
        Assert.assertTrue(content.contains("package io.swagger.server.api.model;"));
        Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));

        final File petServiceFiles = new File(output,
          "/src/main/java/io/swagger/server/api/service");
        for (File file : petServiceFiles.listFiles()) {
            Assert.assertTrue(SERVICE_AS_INTERFACE.containsKey(file.getName()) || PACKAGE_INFO.equals(file.getName()));
            content = FileUtils.readFileToString(file);

            if (PACKAGE_INFO.equals(file.getName())) {
                Assert.assertTrue(content.contains("@ModuleGen(name = \"service\", groupPackage = \"io.swagger.server.api.service\", useFutures = false)"));
                Assert.assertTrue(content.contains("package io.swagger.server.api.service;"));
                Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));
            } else {
                String api = SERVICE_AS_INTERFACE.getOrDefault(file.getName(), PACKAGE_INFO);
                Assert.assertFalse(content.contains("@WebApiServiceGen"));
                Assert.assertTrue(content.contains("String WEBSERVICE_ADDRESS_" + api.toUpperCase()));
                Assert.assertTrue(content.contains("ServiceRequest request,"));
                Assert.assertFalse(content.contains("Future<ServiceResponse>"));
                Assert.assertTrue(content.contains("Handler<AsyncResult<ServiceResponse>> resultHandler);"));
            }
        }
    }

    @Test(description = "verify that main verticle, openapi verticle and service are written as expected (OAS 3.x & web-api-service & not dataobject & Future)")
    public void testUseOas3AndWebApiServiceAndNoDataobjectAndFuture() throws Exception {
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
          .setLang("java-vertx")
          .setInputSpecURL("src/test/resources/3_0_0/petstore-vertx.yaml")
          .setOutputDir(output.getAbsolutePath())
          .addAdditionalProperty(USE_DATAOBJECT_OPTION, false)
          .addAdditionalProperty(MOUNT_OPERATION_FROM_OPTION, MOUNT_OPERATION_FROM_EXTENSIONS)
          .addAdditionalProperty(USE_FUTURE_OPTION, true);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output,
          "/src/main/java/io/swagger/server/api/MainApiVerticle.java");
        String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains(
          "vertx.deployVerticle(\"io.swagger.server.api.verticle.SwaggerPetstoreVerticle\")"));
        Assert.assertTrue(content.contains("startPromise.fail(error);"));
        Assert.assertTrue(content.contains("startPromise.complete();"));

        final File petVerticleFile = new File(output,
          "/src/main/java/io/swagger/server/api/verticle/SwaggerPetstoreVerticle.java");
        content = FileUtils.readFileToString(petVerticleFile);

        Assert.assertTrue(
          content.contains("RouterBuilder.create(this.vertx, \"openapi.yaml\")"));
        Assert.assertTrue(content.contains("routerBuilder.mountServicesFromExtensions();"));
        Assert.assertTrue(content.contains("router.route(\"/*\").subRouter(openapiRouter);"));
        Assert.assertTrue(content.contains("onSuccess(server -> startPromise.complete())"));
        Assert.assertTrue(content.contains("onFailure(startPromise::fail);"));

        final File packageInfoModelFile = new File(output,
          "/src/main/java/io/swagger/server/api/model/" + PACKAGE_INFO);
        Assert.assertFalse(packageInfoModelFile.exists());

        final File petServiceFiles = new File(output,
          "/src/main/java/io/swagger/server/api/service");
        for (File file : petServiceFiles.listFiles()) {
            Assert.assertTrue(SERVICE_AS_EXTENSION_FILENAMES.contains(file.getName()) || PACKAGE_INFO.equals(file.getName()));
            content = FileUtils.readFileToString(file);

            if (PACKAGE_INFO.equals(file.getName())) {
                Assert.assertTrue(content.contains("@ModuleGen(name = \"service\", groupPackage = \"io.swagger.server.api.service\", useFutures = true)"));
                Assert.assertTrue(content.contains("package io.swagger.server.api.service;"));
                Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));
            } else {
                Assert.assertTrue(content.contains("@WebApiServiceGen"));
                Assert.assertTrue(content.contains("String WEBSERVICE_ADDRESS_"));
                Assert.assertTrue(content.contains("Future<ServiceResponse>"));
                Assert.assertTrue(content.contains("RequestParameter body, ServiceRequest request);"));
            }
        }
    }

    @Test(description = "verify that main verticle, openapi verticle and service are written as expected (OAS 3.x & web-api-service & not dataobject & not Future)")
    public void testUseOas3AndWebApiServiceAndNoDataobjectAndNotFuture() throws Exception {
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
          .setLang("java-vertx")
          .setInputSpecURL("src/test/resources/3_0_0/petstore-vertx.yaml")
          .setOutputDir(output.getAbsolutePath())
          .addAdditionalProperty(USE_DATAOBJECT_OPTION, false)
          .addAdditionalProperty(MOUNT_OPERATION_FROM_OPTION, MOUNT_OPERATION_FROM_EXTENSIONS);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output,
          "/src/main/java/io/swagger/server/api/MainApiVerticle.java");
        String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains(
          "vertx.deployVerticle(\"io.swagger.server.api.verticle.SwaggerPetstoreVerticle\")"));
        Assert.assertTrue(content.contains("startPromise.fail(error);"));
        Assert.assertTrue(content.contains("startPromise.complete();"));

        final File petVerticleFile = new File(output,
          "/src/main/java/io/swagger/server/api/verticle/SwaggerPetstoreVerticle.java");
        content = FileUtils.readFileToString(petVerticleFile);

        Assert.assertTrue(
          content.contains("RouterBuilder.create(this.vertx, \"openapi.yaml\")"));
        Assert.assertTrue(content.contains("routerBuilder.mountServicesFromExtensions();"));
        Assert.assertTrue(content.contains("router.route(\"/*\").subRouter(openapiRouter);"));
        Assert.assertTrue(content.contains("onSuccess(server -> startPromise.complete())"));
        Assert.assertTrue(content.contains("onFailure(startPromise::fail);"));

        final File packageInfoModelFile = new File(output,
          "/src/main/java/io/swagger/server/api/model/" + PACKAGE_INFO);
        Assert.assertFalse(packageInfoModelFile.exists());

        final File petServiceFiles = new File(output,
          "/src/main/java/io/swagger/server/api/service");
        for (File file : petServiceFiles.listFiles()) {
            Assert.assertTrue(SERVICE_AS_EXTENSION_FILENAMES.contains(file.getName()) || PACKAGE_INFO.equals(file.getName()));
            content = FileUtils.readFileToString(file);

            if (PACKAGE_INFO.equals(file.getName())) {
                Assert.assertTrue(content.contains("@ModuleGen(name = \"service\", groupPackage = \"io.swagger.server.api.service\", useFutures = false)"));
                Assert.assertTrue(content.contains("package io.swagger.server.api.service;"));
                Assert.assertTrue(content.contains("import io.vertx.codegen.annotations.ModuleGen;"));
            } else {
                Assert.assertTrue(content.contains("@WebApiServiceGen"));
                Assert.assertTrue(content.contains("String WEBSERVICE_ADDRESS_"));
                Assert.assertTrue(content.contains("RequestParameter body, ServiceRequest request,"));
                Assert.assertFalse(content.contains("Future<ServiceResponse>"));
                Assert.assertTrue(content.contains("Handler<AsyncResult<ServiceResponse>> resultHandler);"));
            }
        }
    }

}
