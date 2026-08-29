package io.swagger.codegen.v3.generators.nodejs;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class NodeJSServerCodegenTest  extends AbstractCodegenTest {

    private TemporaryFolder folder = new TemporaryFolder();

    @Test(description = "verify that parameters are listed in following order: body, query, path, header, cookie")
    public void testParameterOrders() throws Exception {
        this.folder.create();
        final File output = this.folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("nodejs-server")
            .setInputSpecURL("src/test/resources/3_0_0/petstore.yaml")
            .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output, "controllers/Pet.js");
        final String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains("module.exports.feedPet = function feedPet (req, res, next, body, petType, status, petId, token, sessionId)"));
        Assert.assertTrue(content.contains("module.exports.getPetById = function getPetById (req, res, next, queryParam1, petId)"));

        this.folder.delete();
    }

}
