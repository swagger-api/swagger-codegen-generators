package io.swagger.codegen.v3.generators.java;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;

public class JavaJAXRSSpecServerCodegenTest extends AbstractCodegenTest {

    /**
     * Base path that API files will be generated in to. 
     */
    private static final String API_PATH = "/src/gen/java/io/swagger/api/";

    private final TemporaryFolder folder = new TemporaryFolder();
    private File output = null;

    /**
     * Generate code for this generator.
     * 
     * @param additionalProperties map containing additional properties which should
     *                             be passed to the generator.
     * @throws IOException if an error occurs while generating code.
     */
    private void generateCode(Map<String, Object> additionalProperties) throws IOException {

        this.folder.create();
        output = this.folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setLang("jaxrs-spec")
                .setInputSpecURL("src/test/resources/3_0_0/composed_schemas.yaml")
                .setOutputDir(output.getAbsolutePath())
                .setAdditionalProperties(additionalProperties);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        listFiles(output, 0);

    }

    private void listFiles(File dir, int level) {
        File[] files = dir.listFiles();

        String prefix = "JAXRS";
        for (int index = 0; index < level; index++) {
            prefix += "-";
        }

        for (File file : files) {
            System.out.println(prefix + " " + file.getName());
            if (file.isDirectory()) {
                listFiles(file, level + 1);
            }
        }
    }

    @After
    public void cleanUp() {
        this.folder.delete();
    }

    @Test(description = "test generation for the default configuration options")
    public void testGenerate() throws IOException {

        /* Generate the code */
        generateCode(new HashMap<>());

        /* Verify the files which are expected are generated */
        testModels();

        /* Verify that the file generated is a class */
        testIsClass(API_PATH, "InventoryApi");


        /* Verify that the application class is generated */
        final File application = new File(output, "/src/gen/java/io/swagger/api/RestApplication.java");
        Assert.assertTrue(application.exists(), "Expected application file to exist");

    }

    @Test(description = "test generation for API interfaces")
    public void testGenerateForInterface() throws IOException {

        /* Generate the code */
        final Map<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put(JavaJAXRSSpecServerCodegen.INTERFACE_ONLY, true);
        generateCode(additionalProperties);

        /* Verify the model files which are expected are generated */
        testModels();

        /* Verify that the file generated is an interface */
        testIsInterface(API_PATH, "InventoryApi");

        /* Verify that the application class is not generated */
        final File application = new File(output, "/src/gen/java/io/swagger/api/RestApplication.java");
        Assert.assertFalse(application.exists(), "Expected application file to not exist");
        
    }

    @Test(description = "test generation for Tags")
    public void testGenerateForTags() throws IOException {
        /* Generate the code */
        final Map<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put(AbstractJavaJAXRSServerCodegen.USE_TAGS, true);
        generateCode(additionalProperties);

        /* Verify the model files which are expected are generated */
        testModels();

        /* Verify that the API files generated are classes */
        testIsClass(API_PATH, "AdminsApi");
        testIsClass(API_PATH, "DevelopersApi");

        /* Verify that the application class is generated */
        final File application = new File(output, "/src/gen/java/io/swagger/api/RestApplication.java");
        Assert.assertTrue(application.exists(), "Expected application file to exist");
    }

    @Test(description = "test generation for Tags & API interfaces")
    public void testGenerateForTagsAndInterface() throws IOException {

        /* Generate the code */
        final Map<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put(JavaJAXRSSpecServerCodegen.INTERFACE_ONLY, true);
        additionalProperties.put(AbstractJavaJAXRSServerCodegen.USE_TAGS, true);
        generateCode(additionalProperties);

        /* Verify the model files which are expected are generated */
        testModels();

        /* Verify that the API files generated are interfaces */
        testIsInterface(API_PATH, "AdminsApi");
        testIsInterface(API_PATH, "DevelopersApi");

        /* Verify that the application class is not generated */
        final File application = new File(output, "/src/gen/java/io/swagger/api/RestApplication.java");
        Assert.assertFalse(application.exists(), "Expected application file to not exist");

    }

    /**
     * Helper method to verify that models were generated.
     */
    private void testModels() {

        final List<String> expectedModels = Arrays.asList("Cat", "Dog", "House", "PartOne", "PartTwo", "PartThree",
                "PartFour", "PartMaster", "Pet", "Pup");

        for (String model : expectedModels) {
            Assert.assertTrue(new File(output, "/src/gen/java/io/swagger/model/" + model + ".java").exists(),
                    "Expected model to exist: " + model);
        }
    }

    // private void assertFilesExist(final String path, final List<String> file)

    /**
     * Helper method to check that a generated file is a class. 
     * @param path the path the file is expected to be in
     * @param className the name of the class generated to check
     * @throws IOException
     */
    private void testIsClass(final String path, final String className) throws IOException {
        final String expectedContent = 
            "public class " + className + " {";

        final File controllerFile = new File(output, path + className + ".java");
        final String content = FileUtils.readFileToString(controllerFile);

        Assert.assertTrue(content.contains(expectedContent));
    }

    /**
     * Helper method to check that a generated file is an interface. 
     * @param path the path the file is expected to be in
     * @param className the name of the class generated to check
     * @throws IOException
     */
    private void testIsInterface(final String path, final String className) throws IOException {
        final String expectedContent = 
            "public interface " + className + " {";

        final File controllerFile = new File(output, path + className + ".java");
        final String content = FileUtils.readFileToString(controllerFile);

        Assert.assertTrue(content.contains(expectedContent));
    }
}