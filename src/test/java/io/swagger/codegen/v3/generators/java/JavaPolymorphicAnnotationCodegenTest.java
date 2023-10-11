package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.Charset;

public class JavaPolymorphicAnnotationCodegenTest {

    private TemporaryFolder folder = new TemporaryFolder();

    @Test(description = "verify that jackson-polymorphism annotations are generated")
    public void testParameterOrders() throws Exception {
        this.folder.create();
        final File output = this.folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/polymorphicSchema.yaml")
            .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output, "/src/main/java/io/swagger/model/PolymorphicResponse.java");
        final String content = FileUtils.readFileToString(petControllerFile, Charset.forName("UTF-8"));

        Assert.assertTrue(content.contains("@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = \"type\", visible = true )"));  
        Assert.assertTrue(content.contains("@JsonSubTypes.Type(value = Error.class, name = \"Error\"),"));
        Assert.assertTrue(content.contains("@JsonSubTypes.Type(value = Success.class, name = \"Success\"),"));    
        this.folder.delete();
    }
}
