package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class SpringGeneratorCodegenTest extends AbstractCodegenTest {

    private TemporaryFolder folder = new TemporaryFolder();

    @Test(description = "verify that parameters are listed in following order: header, query, path, cookie, body")
    public void testParameterOrders() throws Exception {
        this.folder.create();
        final File output = this.folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String content = FileUtils.readFileToString(petControllerFile);



        Assert.assertTrue(content.contains("ResponseEntity<LocalizedText> updateTest(@Parameter(description = \"description\",required=true, schema=@Schema()) @PathVariable(\"id\") Long id" + System.lineSeparator() +
            ", @Parameter(description = \"Localized Text object containing updated data.\" ,required=true, schema=@Schema())@Valid @RequestBody LocalizedText body" + System.lineSeparator() +
            ");"));

        this.folder.delete();
    }


}
