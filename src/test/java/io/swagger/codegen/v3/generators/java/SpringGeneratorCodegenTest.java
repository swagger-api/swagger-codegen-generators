package io.swagger.codegen.v3.generators.java;

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

        Assert.assertTrue(content.contains("ResponseEntity<LocalizedText> updateTest(@ApiParam(value = \"description\",required=true) @PathVariable(\"id\") Long id" +
            ",@ApiParam(value = \"Localized Text object containing updated data.\" ,required=true ) @Valid @RequestBody LocalizedText body" + System.lineSeparator() +
            ");"));

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

        Assert.assertTrue(content.contains("ResponseEntity<LocalizedText> updateTest(@Parameter(in = ParameterIn.PATH, description = \"description\" ,required=true,schema=@Schema()) @PathVariable(\"id\") Long id" +
            ",@Parameter(in = ParameterIn.DEFAULT, description = \"Localized Text object containing updated data.\" ,required=true,schema=@Schema()) @Valid @RequestBody LocalizedText body" + System.lineSeparator() +
            ");"));

        folder.delete();
    }

}
