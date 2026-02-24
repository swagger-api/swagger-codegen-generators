package io.swagger.codegen.v3.generators.java;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;

public class JaxRsSpecReturnResponseTest {
    private TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void returnResponse() throws Exception {
        folder.create();
        final File output = folder.getRoot();

        Map<String, Object> additionalProperties = new HashMap<String, Object>();
        additionalProperties.put(JavaJAXRSSpecServerCodegen.RETURN_RESPONSE, true);
        additionalProperties.put(JavaJAXRSSpecServerCodegen.INTERFACE_ONLY, true);
        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setLang("jaxrs-spec").setAdditionalProperties(additionalProperties)
                .setInputSpecURL("src/test/resources/3_0_0/petstore.yaml")
                .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        File petFile = new File(output, "src/gen/java/io/swagger/api/PetApi.java");
        final String content = FileUtils.readFileToString(petFile);
        
        Assert.assertTrue(content.contains("Response ")); 
        folder.delete();
    }
}
