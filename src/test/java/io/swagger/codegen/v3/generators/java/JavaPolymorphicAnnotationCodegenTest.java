package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import org.apache.commons.io.FileUtils;
import org.junit.internal.matchers.StringContains;
import org.junit.rules.TemporaryFolder;
import org.testng.annotations.Test;

import java.io.File;

import static org.junit.Assert.assertThat;

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
        final String content = FileUtils.readFileToString(petControllerFile);

        assertThat(content, StringContains.containsString(
            "@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = \"type\", visible = true )" + System.lineSeparator() +
                "@JsonSubTypes({" + System.lineSeparator() +
            "        @JsonSubTypes.Type(value = Error.class, name = \"Error\")," + System.lineSeparator() +
                "        @JsonSubTypes.Type(value = Success.class, name = \"Success\")," + System.lineSeparator() +
                "})"));

        this.folder.delete();
    }
}
