package io.swagger.codegen.v3.generators.java;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;

public class JavaJAXRSSpecServerCodegenTest extends AbstractCodegenTest{
    
    private TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void responseWithoutContent() throws Exception {
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setLang("jaxrs-spec")
                .setInputSpecURL("src/test/resources/3_0_0/petstore.yaml")
                .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        File petFile = new File(output, "src/gen/java/io/swagger/model/Pet.java");
        final String content = FileUtils.readFileToString(petFile);
        
        Assert.assertTrue(content.contains("import com.fasterxml.jackson.annotation.JsonCreator;")); 
        Assert.assertTrue(content.contains("import com.fasterxml.jackson.annotation.JsonValue;"));
        folder.delete();
    }
}
