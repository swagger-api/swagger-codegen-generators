package io.swagger.codegen.v3.generators.java;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import io.swagger.codegen.v3.service.GenerationRequest;
import io.swagger.codegen.v3.service.GeneratorService;
import io.swagger.codegen.v3.service.Options;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class SpringGeneratorTest extends AbstractCodegenTest {
    @Test
    public void testGenerator() throws Exception {

        System.setProperty("supportingFiles", "false");
        System.setProperty("generateModels", "true");
        System.setProperty("generateApis", "true");
        String path = getOutFolder(false).getAbsolutePath();
        GenerationRequest request = new GenerationRequest();
        request
            .codegenVersion(GenerationRequest.CodegenVersion.V3) // use V2 to target Swagger/OpenAPI 2.x Codegen version
            .type(GenerationRequest.Type.CLIENT)
            .lang("spring")
            // .specURL("https://petstore3.swagger.io/api/v3/openapi.yaml")
            .spec(loadSpecAsNode(   "3_0_0/nullable-required.yaml",
                true, // YAML file, use false for JSON
                false)) // OpenAPI 3.x - use true for Swagger/OpenAPI 2.x definitions
            .options(
                new Options()
                    .addAdditionalProperty("dateLibrary", "legacy")
                    .addAdditionalProperty("serializableModel", false)
                    .addAdditionalProperty("useTags", true)
                    .addAdditionalProperty("generateForOpenFeign", true)
                    .addAdditionalProperty("configPackage", "test.configPackage")
                    // .addAdditionalProperty("generateSupportingFiles", false)
                    .library("spring-cloud")
                    .addAdditionalProperty("jakarta", true)
                    // .addAdditionalProperty("validationMode", "loose")
                    // .addAdditionalProperty("validationMode", "legacy")
                    // .addAdditionalProperty("validationMode", "legacyNullable")
                    .addAdditionalProperty("useBeanValidation", false)
                    // .addAdditionalProperty("useNullableForNotNull", false)
                    .modelPackage("test.foo")
                    .outputDir(path)
            );

        List<File> files = new GeneratorService().generationRequest(request).generate();
        Assert.assertFalse(files.isEmpty());
/*        for (File f: files) {
            // test stuff
            if (f.getName().endsWith("Pet.java")) {
                String content = new String(Files.readAllBytes(f.toPath()));
                System.out.println(content);
            }
            if (f.getName().endsWith("application.properties")) {
                String content = new String(Files.readAllBytes(f.toPath()));
                System.out.println(content);
            }
            if (f.getName().endsWith("pom.xml")) {
                String content = new String(Files.readAllBytes(f.toPath()));
                // System.out.println(content);
            }
            if (f.getName().endsWith("Boot.java")) {
                String content = new String(Files.readAllBytes(f.toPath()));
                // System.out.println(content);
            }
        }*/
    }

    protected static File getTmpFolder() {
        try {
            File outputFolder = Files.createTempFile("codegentest-", "-tmp").toFile();
            outputFolder.delete();
            outputFolder.mkdir();
            outputFolder.deleteOnExit();
            return outputFolder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    protected static File getOutFolder(boolean delete) {
        try {
            File outputFolder = getTmpFolder();

            System.out.println(outputFolder.getAbsolutePath());
            if (delete) {
                // delete..
            }
            return outputFolder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected JsonNode loadSpecAsNode(final String file, boolean yaml, boolean v2) {
        InputStream in = null;
        String s = "";
        try {
            in = getClass().getClassLoader().getResourceAsStream(file);
            if (yaml) {
                if (v2) {
                    return Yaml.mapper().readTree(in);
                } else {
                    return io.swagger.v3.core.util.Yaml.mapper().readTree(in);
                }
            }
            if (v2) {
                return Json.mapper().readTree(in);
            }
            return io.swagger.v3.core.util.Json.mapper().readTree(in);
        } catch (Exception e) {
            throw new RuntimeException("could not load file " + file);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
