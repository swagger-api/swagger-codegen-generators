package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JavaResteasyOuterEnumCodegenTest extends AbstractCodegenTest {

    private TemporaryFolder folder = null;

    @Before
    public void setUp() throws Exception {
        folder = new TemporaryFolder();
        folder.create();
    }

    @After
    public void tearDown() {
        folder.delete();
    }

    @Test
    public void testOuterEnumResteasy() throws IOException {
        generateAndAssertOuterEnum("jaxrs-resteasy");
    }

    @Test
    public void testOuterEnumResteasyEap() throws IOException {
        generateAndAssertOuterEnum("jaxrs-resteasy-eap");
    }

    private void generateAndAssertOuterEnum(String language) throws IOException {
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang(language)
            .setInputSpecURL("src/test/resources/3_0_0/outer_enum.yaml")
            .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File outerEnumFile = new File(output, "src/gen/java/io/swagger/model/OrderStatus.java");
        Assert.assertTrue(language + ": OrderStatus.java not generated", outerEnumFile.exists());
        final String content = FileUtils.readFileToString(outerEnumFile);

        Assert.assertTrue(language, content.contains("import com.fasterxml.jackson.annotation.JsonValue;"));
        Assert.assertTrue(language, content.contains("public enum OrderStatus {"));
        Assert.assertTrue(language, content.contains("PLACED(\"placed\")"));
        Assert.assertTrue(language, content.contains("APPROVED(\"approved\")"));
        Assert.assertTrue(language, content.contains("DELIVERED(\"delivered\")"));
        Assert.assertTrue(language, content.contains("private String value;"));
        Assert.assertTrue(language, content.contains("OrderStatus(String value) {"));
        Assert.assertTrue(language, content.contains("@JsonValue"));
    }
}
