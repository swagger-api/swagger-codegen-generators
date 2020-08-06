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

    /**
     * List containing the operationIds which are tagged as Admin
     */
    private static final List<String> ADMIN_OPERATIONIDS = Arrays.asList("inventoryAdmin");

    /**
     * List containing the operationIds which are tagged as Developer
     */
    private static final List<String> DEVELOPER_OPERATIONIDS = Arrays.asList("searchInventory");

    /**
     * All the operationIds in the test file. 
     */
    private static final List<String> ALL_OPERATIONIDS = Arrays.asList("inventoryAdmin", "searchInventory");

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

        /* Verify the API file contains the operations we expected */
        testContainsOperations(API_PATH, "InventoryApi", ALL_OPERATIONIDS, true);


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

        /* Verify the API file contains the operations we expected */
        testContainsOperations(API_PATH, "InventoryApi", ALL_OPERATIONIDS, true);

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

        /* Verify the API file contains the operations we expected, and doesn't contain the ones it shouldn't */
        testContainsOperations(API_PATH, "AdminsApi", ADMIN_OPERATIONIDS, true);
        testContainsOperations(API_PATH, "AdminsApi", DEVELOPER_OPERATIONIDS, false);
        testContainsOperations(API_PATH, "DevelopersApi", ADMIN_OPERATIONIDS, false);
        testContainsOperations(API_PATH, "DevelopersApi", DEVELOPER_OPERATIONIDS, true);

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

        /* Verify the API file contains the operations we expected, and doesn't contain the ones it shouldn't */
        testContainsOperations(API_PATH, "AdminsApi", ADMIN_OPERATIONIDS, true);
        testContainsOperations(API_PATH, "AdminsApi", DEVELOPER_OPERATIONIDS, false);
        testContainsOperations(API_PATH, "DevelopersApi", ADMIN_OPERATIONIDS, false);
        testContainsOperations(API_PATH, "DevelopersApi", DEVELOPER_OPERATIONIDS, true);

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

        final String content = getFileContents(path, className);

        Assert.assertTrue(content.contains(expectedContent));
    }

    /**
     * Helper method to check that a generated file is an interface. 
     * @param path the path the file is expected to be in
     * @param className the name of the class generated to check
     * @throws IOException if an error occurs reading the file. 
     */
    private void testIsInterface(final String path, final String className) throws IOException {
        final String expectedContent = 
            "public interface " + className + " {";

        final String content = getFileContents(path, className);

        Assert.assertTrue(content.contains(expectedContent));
    }

    /**
     * Helper method to check that a file contains methods for the supplied opertationIds. 
     * @param path the path the file is expected to be in
     * @param className the name of the class generated to read
     * @param operationIds list of operationIds to look for in the file
     * @param expected boolean controlling if this is testing if the operation is expected (true) or not (false). 
     * @param IOException if an error occurs reading the file.
     */
    private void testContainsOperations(final String path, final String className, final List<String> operationIds, boolean expected) throws IOException {

        final String content = getFileContents(path, className);

        Assert.assertNotNull(operationIds, "Expected operationIds to be non-null");
        Assert.assertFalse(operationIds.isEmpty(), "Expected operationIds to be non-empty");

        for (String operationId : operationIds) {
            /* Operations always become the method name and are immediately followed with an open bracket. */
            Assert.assertEquals(content.contains(operationId + "("), expected, 
                "Operation " + operationId + " in " + className);
        }

    }

    /**
     * Get the contents of a file as a String. 
     * @param path the path the file is expected to be in
     * @param className the name of the class generated to read
     * @throws IOException if an error occurs reading the file. 
     */
    private String getFileContents(final String path, final String className) throws IOException {

        final File controllerFile = new File(output, path + className + ".java");
        final String content = FileUtils.readFileToString(controllerFile);

        return content;
    }
}