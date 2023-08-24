package io.swagger.codegen.v3.generators.java;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;

public class MicronautGeneratorCodegenTest extends AbstractCodegenTest {
    private final TemporaryFolder folder = new TemporaryFolder();
    private File output = null;

    @BeforeClass
    public void generateCode() throws IOException {
        this.folder.create();
        output = this.folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setLang("micronaut")
                .setInputSpecURL("src/test/resources/3_0_0/parameterValidation.yaml")
                .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();
    }

    @AfterClass
    public void cleanUp() {
        this.folder.delete();
    }

    @Test(description = "verify that model classes are generated")
    public void testModels() {
        Assert.assertTrue(new File(output, "/src/main/java/io/swagger/model/LocalizedText.java").exists());
    }

    @Test(description = "verify that configuration classes are generated")
    public void testConfigurations() {
        Assert.assertTrue(new File(output, "/src/main/java/io/swagger/configuration/UnsupportedOperationExceptionHandler.java").exists());
    }

    @Test(description = "verify interface api generated")
    public void testApiInterface() throws IOException {
        final String expectedContent = "public interface AdminApi {";
        final File controllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String content = FileUtils.readFileToString(controllerFile);
        Assert.assertTrue(content.contains(expectedContent));
    }

    @Test(description = "verify that parameters are listed as follows: header, path, query, cookie, body")
    public void testApiParameters() throws IOException {
        final String expectedContent = "default Single<HttpResponse<LocalizedText>> updateTest(@NotNull @Valid @Parameter(description = \"Localized Text object.\") @Body LocalizedText body"
                                 + ",@NotNull @Pattern(regexp=\"[0-9]+\") @Parameter(description = \"header description\") @Header(value = \"x-header\") String xHeader"
                                 + ",@Parameter(description = \"path description\") @PathVariable(\"id\") Long id"
                                 + ",@Nullable @Parameter(description = \"query description\") @QueryValue(value = \"name\") String name";
        final File controllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String content = FileUtils.readFileToString(controllerFile);
        Assert.assertTrue(content.contains(expectedContent));
    }
}