package io.swagger.codegen.v3.generators.java;

import static io.swagger.codegen.v3.generators.java.AbstractJavaCodegen.JAKARTA;
import static io.swagger.codegen.v3.generators.java.AbstractJavaCodegen.JAVA8_MODE;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.CodegenArgument;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;

public class SpringGeneratorCodegenTest extends AbstractCodegenTest {
    @Test(description = "verify that parameters are listed in following order: header, query, path, cookie, body (OAS 2.x)")
    public void testParameterOrdersUseOas2() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath());

        configurator.setCodegenArguments(Collections.singletonList(
          new CodegenArgument()
            .option(CodegenConstants.USE_OAS2_OPTION)
            .type("boolean")
            .value(Boolean.TRUE.toString())));

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains("ResponseEntity<LocalizedText> updateTest(@ApiParam(value = \"description\", required=true) @PathVariable(\"id\") Long id"));
        Assert.assertTrue(content.contains("@ApiParam(value = \"Localized Text object containing updated data.\", required=true ) @Valid @RequestBody LocalizedText body"));

        final File adminApiControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApiController.java");
        final String contentAdminApiController = FileUtils.readFileToString(adminApiControllerFile);

        Assert.assertFalse(contentAdminApiController.contains("jakarta"));
        Assert.assertTrue(contentAdminApiController.contains("javax"));

        folder.delete();
    }

    @Test(description = "verify that parameters are listed in following order: header, query, path, cookie, body (OAS 3.x)")
    public void testParameterOrdersUseOas3() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains("ResponseEntity<LocalizedText> updateTest(@Parameter(in = ParameterIn.PATH, description = \"description\", required=true, schema=@Schema()) @PathVariable(\"id\") Long id"));
        Assert.assertTrue(content.contains("@Parameter(in = ParameterIn.DEFAULT, description = \"Localized Text object containing updated data.\", required=true, schema=@Schema()) @Valid @RequestBody LocalizedText body"));

        final File adminApiControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApiController.java");
        final String contentAdminApiController = FileUtils.readFileToString(adminApiControllerFile);

        Assert.assertFalse(contentAdminApiController.contains("jakarta"));
        Assert.assertTrue(contentAdminApiController.contains("javax"));

        folder.delete();
    }

    @Test(description = "verify oas2 & jakarta")
    public void testOas2AndJakarta() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath())
            .addAdditionalProperty(JAKARTA, true);

        configurator.setCodegenArguments(Collections.singletonList(
          new CodegenArgument()
            .option(CodegenConstants.USE_OAS2_OPTION)
            .type("boolean")
            .value(Boolean.TRUE.toString())));

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File adminApiFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String contentAdminApi = FileUtils.readFileToString(adminApiFile);

        Assert.assertTrue(contentAdminApi.contains("ResponseEntity<LocalizedText> updateTest(@ApiParam(value = \"description\", required=true) @PathVariable(\"id\") Long id"));
        Assert.assertTrue(contentAdminApi.contains("@ApiParam(value = \"Localized Text object containing updated data.\", required=true ) @Valid @RequestBody LocalizedText body"));

        final File adminApiControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApiController.java");
        final String contentAdminApiController = FileUtils.readFileToString(adminApiControllerFile);

        Assert.assertTrue(contentAdminApiController.contains("jakarta"));
        Assert.assertFalse(contentAdminApiController.contains("javax"));

        folder.delete();
    }

    @Test(description = "verify oas3 & jakarta")
    public void testUseOas3AndJakarta() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath())
            .addAdditionalProperty(JAKARTA, true);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File adminApiFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String contentAdminApi = FileUtils.readFileToString(adminApiFile);

        Assert.assertTrue(contentAdminApi.contains("ResponseEntity<LocalizedText> updateTest(@Parameter(in = ParameterIn.PATH, description = \"description\", required=true, schema=@Schema()) @PathVariable(\"id\") Long id"));
        Assert.assertTrue(contentAdminApi.contains("@Parameter(in = ParameterIn.DEFAULT, description = \"Localized Text object containing updated data.\", required=true, schema=@Schema()) @Valid @RequestBody LocalizedText body"));


        final File adminApiControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApiController.java");
        final String contentAdminApiController = FileUtils.readFileToString(adminApiControllerFile);

        Assert.assertTrue(contentAdminApiController.contains("jakarta"));
        Assert.assertFalse(contentAdminApiController.contains("javax"));

        folder.delete();
    }

    @Test(description = "verify java8 & jakarta")
    public void testJava8Jakarta() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath())
            .addAdditionalProperty(JAKARTA, true)
            .addAdditionalProperty(JAVA8_MODE, true);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File adminApiFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String contentAdminApi = FileUtils.readFileToString(adminApiFile);

        Assert.assertTrue(contentAdminApi.contains("import jakarta.servlet.http.HttpServletRequest;"));

        folder.delete();
    }

    @Test(description = "verify java8 & javax")
    public void testJava8Javax() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath())
            .addAdditionalProperty(JAKARTA, false)
            .addAdditionalProperty(JAVA8_MODE, true);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File adminApiFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String contentAdminApi = FileUtils.readFileToString(adminApiFile);

        Assert.assertTrue(contentAdminApi.contains("import javax.servlet.http.HttpServletRequest;"));

        folder.delete();
    }

}
