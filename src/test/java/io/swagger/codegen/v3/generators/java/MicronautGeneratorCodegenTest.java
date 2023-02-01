package io.swagger.codegen.v3.generators.java;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.internal.matchers.StringContains;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;

import static org.junit.Assert.assertThat;

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
        final String expectedContent = "@Controller" + System.lineSeparator()
                                       + "public interface AdminApi {";
        final File controllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String content = FileUtils.readFileToString(controllerFile);
        Assert.assertTrue(content.contains(expectedContent));
        assertThat(content, StringContains.containsString(expectedContent));
    }

    @Test(description = "verify that parameters are listed as follows: header, path, query, cookie, body")
    public void testApiParameters() throws IOException {
        final String expectedContent = "default Single<HttpResponse<LocalizedText>> updateTest(@Parameter(description = \"Localized Text object.\") @Valid @Body LocalizedText body" + System.lineSeparator()
                                 + ",@NotNull @Pattern(regexp=\"[0-9]+\") @Parameter(description = \"header description\") @Valid @Header(value = \"x-header\") String xHeader" + System.lineSeparator()
                                 + ",@Parameter(description = \"path description\") @PathVariable(\"id\") Long id" + System.lineSeparator()
                                 + ",@Nullable @Parameter(description = \"query description\") @Valid @QueryValue(value = \"name\") String name" + System.lineSeparator()
                                 + ",@Nullable @Parameter(description = \"boolean with default value\") @Valid @QueryValue(value = \"itemWithDefault\", defaultValue = \"false\") Boolean itemWithDefault";
        final File controllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String content = FileUtils.readFileToString(controllerFile);
        assertThat(content, StringContains.containsString(expectedContent));
    }
}