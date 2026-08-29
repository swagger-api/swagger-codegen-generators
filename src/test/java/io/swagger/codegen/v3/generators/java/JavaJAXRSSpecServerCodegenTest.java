package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static io.swagger.codegen.v3.generators.java.JavaJAXRSSpecServerCodegen.INTERFACE_ONLY;
import static io.swagger.codegen.v3.generators.java.JavaJAXRSSpecServerCodegen.RETURN_RESPONSE;

public class JavaJAXRSSpecServerCodegenTest extends AbstractCodegenTest {

    private static final String LANG = "jaxrs-spec";
    private static final String SPEC_URL = "src/test/resources/3_0_0/petstore.yaml";
    private static final String API_FILE = "src/gen/java/io/swagger/api/PetApi.java";

    private TemporaryFolder temporaryFolder;

    @BeforeMethod
    public void setUp() throws IOException {
        temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
    }

    @AfterMethod
    public void tearDown() {
        temporaryFolder.delete();
    }

    @Test(description = "verify that api method returns component")
    public void testInterfaceOnlyWithoutReturnResponse() throws IOException {
        final File output = temporaryFolder.getRoot();

        final CodegenConfigurator configurator = configurator(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        List<File> files = new DefaultGenerator().opts(clientOptInput).generate();

        File petApiFile = files.stream()
            .filter(f -> f.getPath().endsWith(API_FILE))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Could not find file " + API_FILE));

        final String content = FileUtils.readFileToString(petApiFile);

        Assert.assertTrue(content.contains("Pet getPetById( @PathParam(\"petId\")"));
    }

    @Test(description = "verify that api method returns response")
    public void testInterfaceOnlyWithReturnResponse() throws IOException {
        final File output = temporaryFolder.getRoot();

        final CodegenConfigurator configurator = configurator(output.getAbsolutePath())
            .addAdditionalProperty(RETURN_RESPONSE, true);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        List<File> files = new DefaultGenerator().opts(clientOptInput).generate();

        File petApiFile = files.stream()
            .filter(f -> f.getPath().endsWith(API_FILE))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Could not find file " + API_FILE));
        final String content = FileUtils.readFileToString(petApiFile);

        Assert.assertTrue(content.contains("Response getPetById( @PathParam(\"petId\")"));
    }

    private static CodegenConfigurator configurator(String outputDir) {
        return new CodegenConfigurator()
            .setLang(LANG)
            .setInputSpecURL(SPEC_URL)
            .setOutputDir(outputDir)
            .addAdditionalProperty(INTERFACE_ONLY, true);
    }
}

