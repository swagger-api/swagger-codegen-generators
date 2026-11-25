package io.swagger.codegen.v3.generators.java;

import static io.swagger.codegen.v3.generators.java.AbstractJavaCodegen.JAKARTA;
import static io.swagger.codegen.v3.generators.java.AbstractJavaCodegen.JAVA8_MODE;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.CodegenArgument;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SpringGeneratorCodegenTest extends AbstractCodegenTest {
    @Test(description = "verify that parameters are listed in following order: header, query, path, cookie, body (OAS 2.x)")
    public void testParameterOrdersUseOas2() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath());

        configurator.setCodegenArguments(Collections.singletonList(
          new CodegenArgument()
            .option(CodegenConstants.USE_OAS2_OPTION)
            .type("boolean")
            .value(Boolean.TRUE.toString())));

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains("ResponseEntity<LocalizedText> updateTest(@ApiParam(value = \"description\", required=true) @PathVariable(\"id\") Long id"));
        Assert.assertTrue(content.contains("@ApiParam(value = \"Localized Text object containing updated data.\", required=true ) @Valid @RequestBody LocalizedText body"));

        final File adminApiControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApiController.java");
        final String contentAdminApiController = FileUtils.readFileToString(adminApiControllerFile);

        Assert.assertFalse(contentAdminApiController.contains("jakarta"));
        Assert.assertTrue(contentAdminApiController.contains("javax"));

        folder.delete();
    }

    @Test(description = "verify that parameters are listed in following order: header, query, path, cookie, body (OAS 3.x)")
    public void testParameterOrdersUseOas3() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File petControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String content = FileUtils.readFileToString(petControllerFile);

        Assert.assertTrue(content.contains("ResponseEntity<LocalizedText> updateTest(@Parameter(in = ParameterIn.PATH, description = \"description\", required=true, schema=@Schema()) @PathVariable(\"id\") Long id"));
        Assert.assertTrue(content.contains("@Parameter(in = ParameterIn.DEFAULT, description = \"Localized Text object containing updated data.\", required=true, schema=@Schema()) @Valid @RequestBody LocalizedText body"));

        final File adminApiControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApiController.java");
        final String contentAdminApiController = FileUtils.readFileToString(adminApiControllerFile);

        Assert.assertFalse(contentAdminApiController.contains("jakarta"));
        Assert.assertTrue(contentAdminApiController.contains("javax"));

        folder.delete();
    }

    @Test(description = "verify oas2 & jakarta")
    public void testOas2AndJakarta() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath())
            .addAdditionalProperty(JAKARTA, true);

        configurator.setCodegenArguments(Collections.singletonList(
          new CodegenArgument()
            .option(CodegenConstants.USE_OAS2_OPTION)
            .type("boolean")
            .value(Boolean.TRUE.toString())));

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File adminApiFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String contentAdminApi = FileUtils.readFileToString(adminApiFile);

        Assert.assertTrue(contentAdminApi.contains("ResponseEntity<LocalizedText> updateTest(@ApiParam(value = \"description\", required=true) @PathVariable(\"id\") Long id"));
        Assert.assertTrue(contentAdminApi.contains("@ApiParam(value = \"Localized Text object containing updated data.\", required=true ) @Valid @RequestBody LocalizedText body"));

        final File adminApiControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApiController.java");
        final String contentAdminApiController = FileUtils.readFileToString(adminApiControllerFile);

        Assert.assertTrue(contentAdminApiController.contains("jakarta"));
        Assert.assertFalse(contentAdminApiController.contains("javax"));

        folder.delete();
    }

    @Test(description = "verify oas3 & jakarta")
    public void testUseOas3AndJakarta() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath())
            .addAdditionalProperty(JAKARTA, true);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File adminApiFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String contentAdminApi = FileUtils.readFileToString(adminApiFile);

        Assert.assertTrue(contentAdminApi.contains("ResponseEntity<LocalizedText> updateTest(@Parameter(in = ParameterIn.PATH, description = \"description\", required=true, schema=@Schema()) @PathVariable(\"id\") Long id"));
        Assert.assertTrue(contentAdminApi.contains("@Parameter(in = ParameterIn.DEFAULT, description = \"Localized Text object containing updated data.\", required=true, schema=@Schema()) @Valid @RequestBody LocalizedText body"));


        final File adminApiControllerFile = new File(output, "/src/main/java/io/swagger/api/AdminApiController.java");
        final String contentAdminApiController = FileUtils.readFileToString(adminApiControllerFile);

        Assert.assertTrue(contentAdminApiController.contains("jakarta"));
        Assert.assertFalse(contentAdminApiController.contains("javax"));

        folder.delete();
    }

    @Test(description = "verify java8 & jakarta")
    public void testJava8Jakarta() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath())
            .addAdditionalProperty(JAKARTA, true)
            .addAdditionalProperty(JAVA8_MODE, true);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File adminApiFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String contentAdminApi = FileUtils.readFileToString(adminApiFile);

        Assert.assertTrue(contentAdminApi.contains("import jakarta.servlet.http.HttpServletRequest;"));

        folder.delete();
    }

    @Test(description = "verify java8 & javax")
    public void testJava8Javax() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/parameterOrder.yaml")
            .setOutputDir(output.getAbsolutePath())
            .addAdditionalProperty(JAKARTA, false)
            .addAdditionalProperty(JAVA8_MODE, true);

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File adminApiFile = new File(output, "/src/main/java/io/swagger/api/AdminApi.java");
        final String contentAdminApi = FileUtils.readFileToString(adminApiFile);

        Assert.assertTrue(contentAdminApi.contains("import javax.servlet.http.HttpServletRequest;"));

        folder.delete();
    }

    @Test(description = "verify that schema with required and nullable=true is generated correctly")
    public void testNullableTrue() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setInputSpecURL("src/test/resources/3_0_0/cc32744.yaml")
            .setOutputDir(output.getAbsolutePath());

        // configurator.addAdditionalProperty("useNullableForNotNull", true); // DEFAULT
        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        final File file = new File(output, "src/main/java/io/swagger/model/NullableRoot.java");
        final String content = FileUtils.readFileToString(file);
        Assert.assertFalse(content.contains("@NotNull"));
        folder.delete();
    }

    // ---------------------------------------------------------------------------------------------
    // spring-boot4 / Jackson 3
    // ---------------------------------------------------------------------------------------------

    private static final String JACKSON2_DATABIND_IMPORT = "com.fasterxml.jackson.databind";
    private static final String JACKSON2_DATAFORMAT_IMPORT = "com.fasterxml.jackson.dataformat";
    private static final String JACKSON3_ROOT = "tools.jackson";

    private static File generateSpringPetstore(File output, String library, Map<String, Object> additionalProperties) {
        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("spring")
            .setLibrary(library)
            .setInputSpecURL("src/test/resources/3_0_0/petstore.yaml")
            .setOutputDir(output.getAbsolutePath());
        additionalProperties.forEach(configurator::addAdditionalProperty);
        new DefaultGenerator().opts(configurator.toClientOptInput()).generate();
        return output;
    }

    private static String read(File output, String relativePath) throws IOException {
        return FileUtils.readFileToString(new File(output, relativePath), StandardCharsets.UTF_8);
    }

    /** Every generated .java / pom.xml file whose content contains {@code needle}. */
    private static List<String> filesContaining(File output, String needle) throws IOException {
        try (Stream<Path> paths = Files.walk(output.toPath())) {
            final List<String> hits = new ArrayList<>();
            for (Path path : (Iterable<Path>) paths::iterator) {
                final String name = path.getFileName().toString();
                if (Files.isRegularFile(path) && (name.endsWith(".java") || name.equals("pom.xml"))
                        && new String(Files.readAllBytes(path), StandardCharsets.UTF_8).contains(needle)) {
                    hits.add(output.toPath().relativize(path).toString());
                }
            }
            return hits;
        }
    }

    /** True when {@code regex} matches somewhere in {@code text}; use \\s* where the template may change indentation. */
    private static boolean matches(String text, String regex) {
        return Pattern.compile(regex).matcher(text).find();
    }

    @Test(description = "spring-boot4 API and controller use Jackson 3 ObjectMapper and JacksonException, no IOException")
    public void testSpringBoot4ApiUsesJackson3() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final Map<String, Object> props = new HashMap<>();
        props.put(CodegenConstants.WITH_XML, true);
        generateSpringPetstore(output, SpringCodegen.SPRING_BOOT_4_LIBRARY, props);

        // java8 mode is forced for spring-boot4, so the example code lives in the interface's default methods
        final String api = read(output, "/src/main/java/io/swagger/api/PetApi.java");
        Assert.assertTrue(api.contains("import tools.jackson.databind.ObjectMapper;"));
        Assert.assertTrue(api.contains("import tools.jackson.core.JacksonException;"));
        Assert.assertTrue(api.contains("catch (JacksonException e)"));
        Assert.assertTrue(api.contains("default "));
        Assert.assertFalse(api.contains("IOException"));

        final String controller = read(output, "/src/main/java/io/swagger/api/PetApiController.java");
        Assert.assertTrue(controller.contains("implements PetApi"));
        Assert.assertFalse(controller.contains("IOException"));
        Assert.assertFalse(controller.contains("javax."));

        folder.delete();
    }

    @Test(description = "spring-boot4 application config and model use Jackson 3 modules and XML annotations; shared jackson-annotations stay on com.fasterxml")
    public void testSpringBoot4ConfigAndModelUseJackson3() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final Map<String, Object> props = new HashMap<>();
        props.put(CodegenConstants.WITH_XML, true);
        generateSpringPetstore(output, SpringCodegen.SPRING_BOOT_4_LIBRARY, props);

        final String application = read(output, "/src/main/java/io/swagger/OpenAPISpringBoot.java");
        Assert.assertTrue(application.contains("import tools.jackson.databind.JacksonModule;"));
        Assert.assertTrue(application.contains("import org.openapitools.jackson.nullable.JsonNullableJackson3Module;"));
        Assert.assertTrue(matches(application, "public\\s+JacksonModule\\s+jsonNullableModule\\(\\)"));
        Assert.assertTrue(application.contains("return new JsonNullableJackson3Module();"));

        final String model = read(output, "/src/main/java/io/swagger/model/Pet.java");
        Assert.assertTrue(model.contains("import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;"));
        Assert.assertTrue(model.contains("import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;"));
        // annotations artifact is shared by Jackson 2 and 3 and must stay on com.fasterxml.jackson.annotation
        Assert.assertTrue(model.contains("import com.fasterxml.jackson.annotation.JsonProperty;"));
        Assert.assertTrue(model.contains("import org.openapitools.jackson.nullable.JsonNullable;"));

        folder.delete();
    }

    @Test(description = "spring-boot4 pom uses the Boot 4 parent, webmvc starter and Jackson 3 XML artifact")
    public void testSpringBoot4PomUsesBoot4() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final Map<String, Object> props = new HashMap<>();
        props.put(CodegenConstants.WITH_XML, true);
        generateSpringPetstore(output, SpringCodegen.SPRING_BOOT_4_LIBRARY, props);

        final String pom = read(output, "/pom.xml");
        Assert.assertTrue(pom.contains("<artifactId>spring-boot-starter-webmvc</artifactId>"));
        Assert.assertFalse(pom.contains("spring-boot-starter-web</artifactId>"));
        Assert.assertFalse(pom.contains("spring-plugin-core"));
        Assert.assertTrue(matches(pom, "<artifactId>spring-boot-starter-parent</artifactId>\\s*<version>4\\.1\\.3</version>"));
        Assert.assertTrue(pom.contains("<maven.compiler.release>21</maven.compiler.release>"));
        Assert.assertTrue(pom.contains("<springdoc-version>3.2.0</springdoc-version>"));
        Assert.assertTrue(pom.contains("<jackson-databind-nullable-version>0.2.13</jackson-databind-nullable-version>"));
        Assert.assertTrue(pom.contains("<groupId>tools.jackson.dataformat</groupId>"));
        Assert.assertFalse(pom.contains("com.fasterxml.jackson.dataformat"));

        folder.delete();
    }

    @Test(description = "spring-boot4 output contains no Jackson 2 databind/dataformat imports and no javax")
    public void testSpringBoot4HasNoJackson2() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final Map<String, Object> props = new HashMap<>();
        props.put(CodegenConstants.WITH_XML, true);
        generateSpringPetstore(output, SpringCodegen.SPRING_BOOT_4_LIBRARY, props);

        Assert.assertEquals(filesContaining(output, JACKSON2_DATABIND_IMPORT), Collections.emptyList());
        Assert.assertEquals(filesContaining(output, JACKSON2_DATAFORMAT_IMPORT), Collections.emptyList());
        Assert.assertEquals(filesContaining(output, "javax."), Collections.emptyList());

        folder.delete();
    }

    @Test(description = "spring-boot4 version options drive parent, compiler release and springdoc line")
    public void testSpringBoot4VersionOptions() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final Map<String, Object> props = new HashMap<>();
        props.put(SpringCodegen.SPRING_BOOT4_VERSION, "4.0.9");
        props.put(SpringCodegen.SPRING_BOOT4_JAVA_VERSION, "17");
        generateSpringPetstore(output, SpringCodegen.SPRING_BOOT_4_LIBRARY, props);

        final String pom = read(output, "/pom.xml");
        Assert.assertTrue(matches(pom, "<artifactId>spring-boot-starter-parent</artifactId>\\s*<version>4\\.0\\.9</version>"));
        Assert.assertTrue(pom.contains("<maven.compiler.release>17</maven.compiler.release>"));
        Assert.assertTrue(pom.contains("<springdoc-version>3.0.3</springdoc-version>"));

        folder.delete();
    }

    @Test(description = "springdoc line is derived from the Spring Boot 4 minor")
    public void testSpringdocVersionFor() {
        Assert.assertEquals(SpringCodegen.springdocVersionFor("4.0.9"), "3.0.3");
        Assert.assertEquals(SpringCodegen.springdocVersionFor("4.1.2"), "3.2.0");
        Assert.assertEquals(SpringCodegen.springdocVersionFor("4.2.0"), "3.2.0");
        Assert.assertEquals(SpringCodegen.springdocVersionFor(null), "3.2.0");
    }

    @Test(description = "spring-boot3 keeps Jackson 2 output (regression guard for the shared templates)")
    public void testSpringBoot3KeepsJackson2() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final Map<String, Object> props = new HashMap<>();
        props.put(CodegenConstants.WITH_XML, true);
        generateSpringPetstore(output, SpringCodegen.SPRING_BOOT_3_LIBRARY, props);

        final String controller = read(output, "/src/main/java/io/swagger/api/PetApiController.java");
        Assert.assertTrue(controller.contains("import com.fasterxml.jackson.databind.ObjectMapper;"));
        Assert.assertTrue(controller.contains("import java.io.IOException;"));
        Assert.assertTrue(controller.contains("catch (IOException e)"));

        final String application = read(output, "/src/main/java/io/swagger/OpenAPISpringBoot.java");
        Assert.assertTrue(application.contains("import com.fasterxml.jackson.databind.Module;"));
        Assert.assertTrue(application.contains("return new JsonNullableModule();"));

        final String model = read(output, "/src/main/java/io/swagger/model/Pet.java");
        Assert.assertTrue(model.contains("import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;"));

        final String pom = read(output, "/pom.xml");
        Assert.assertTrue(pom.contains("<artifactId>spring-boot-starter-web</artifactId>"));
        Assert.assertTrue(pom.contains("<groupId>com.fasterxml.jackson.dataformat</groupId>"));

        // "tools.jackson." alone would also match org.openapitools.jackson.nullable, hence the import / groupId prefixes
        Assert.assertEquals(filesContaining(output, "import " + JACKSON3_ROOT + "."), Collections.emptyList());
        Assert.assertEquals(filesContaining(output, "<groupId>" + JACKSON3_ROOT), Collections.emptyList());

        folder.delete();
    }

    @Test(description = "default spring-boot library keeps Jackson 2 output (regression guard for the shared templates)")
    public void testDefaultSpringBootKeepsJackson2() throws Exception {
        final TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        final File output = folder.getRoot();

        final Map<String, Object> props = new HashMap<>();
        props.put(CodegenConstants.WITH_XML, true);
        generateSpringPetstore(output, "spring-boot", props);

        final String controller = read(output, "/src/main/java/io/swagger/api/PetApiController.java");
        Assert.assertTrue(controller.contains("import com.fasterxml.jackson.databind.ObjectMapper;"));
        Assert.assertTrue(controller.contains("catch (IOException e)"));

        final String model = read(output, "/src/main/java/io/swagger/model/Pet.java");
        Assert.assertTrue(model.contains("import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;"));

        // "tools.jackson." alone would also match org.openapitools.jackson.nullable, hence the import / groupId prefixes
        Assert.assertEquals(filesContaining(output, "import " + JACKSON3_ROOT + "."), Collections.emptyList());
        Assert.assertEquals(filesContaining(output, "<groupId>" + JACKSON3_ROOT), Collections.emptyList());

        folder.delete();
    }
}
