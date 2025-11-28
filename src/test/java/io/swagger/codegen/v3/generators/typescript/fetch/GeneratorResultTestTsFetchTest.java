package io.swagger.codegen.v3.generators.typescript.fetch;

import io.swagger.codegen.v3.generators.GeneratorRunner;
import io.swagger.codegen.v3.service.GenerationRequest;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GeneratorResultTestTsFetchTest {


    @Test
    public void testJavaGenerator_OneOf() throws Exception {

        String name = "typescript-fetch";
        String specPath = "3_0_0/composed_schemas.yaml";
        GenerationRequest.CodegenVersion codegenVersion = GenerationRequest.CodegenVersion.V3;
        boolean v2Spec = false; // 3.0 spec
        boolean yaml = true;
        boolean flattenInlineComposedSchema = true;
        String outFolder = null; // temporary folder

        List<File> files = GeneratorRunner.runGenerator(
            name,
            specPath,
            codegenVersion,
            v2Spec,
            yaml,
            flattenInlineComposedSchema,
            outFolder);

        Assert.assertFalse(files.isEmpty());

        for (File f: files) {
            // TODO test stuff

            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(f);
                if (f.getName().equalsIgnoreCase("api.ts")) {
                    String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    Assert.assertTrue(result.contains("Map<any, any>"), "Generator should create Maps with any");
                }
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
