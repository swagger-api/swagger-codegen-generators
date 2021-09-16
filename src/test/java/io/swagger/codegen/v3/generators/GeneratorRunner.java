package io.swagger.codegen.v3.generators;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.codegen.v3.service.GenerationRequest;
import io.swagger.codegen.v3.service.GeneratorService;
import io.swagger.codegen.v3.service.Options;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 *
 * helper methods to test specific generator result (set of files)
 *
 */
public abstract class GeneratorRunner {

    public static List<File> runGenerator(
        String name,
        String specPath,
        GenerationRequest.CodegenVersion codegenVersion,
        boolean v2Spec,
        boolean yaml,
        boolean flattenInlineComposedSchema,
        String outFolder
    ) throws Exception {

        String path = outFolder;
        if (StringUtils.isBlank(path)) {
            path = getTmpFolder().getAbsolutePath();
        }
        GenerationRequest request = new GenerationRequest();
        request
            .codegenVersion(codegenVersion) // use V2 to target Swagger/OpenAPI 2.x Codegen version
            .type(GenerationRequest.Type.CLIENT)
            .lang(name)
            .spec(loadSpecAsNode(   specPath,
                yaml, // YAML file, use false for JSON
                v2Spec)) // OpenAPI 3.x - use true for Swagger/OpenAPI 2.x definitions
            .options(
                new Options()
                    .flattenInlineComposedSchema(flattenInlineComposedSchema)
                    .outputDir(path)
            );

        List<File> files = new GeneratorService().generationRequest(request).generate();
        return files;
    }

    public static File getOutFolder(String path, boolean delete) {
        try {
            File outputFolder = new File(path);
            if (delete) {
                // TODO delete..
            }
            return outputFolder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getTmpFolder() {
        try {
            File outputFolder = Files.createTempDirectory("codegentest-").toFile();
            outputFolder.deleteOnExit();
            return outputFolder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonNode loadSpecAsNode(final String file, boolean yaml, boolean v2) {
        InputStream in = null;

        try {
            in = GeneratorRunner.class.getClassLoader().getResourceAsStream(file);
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
