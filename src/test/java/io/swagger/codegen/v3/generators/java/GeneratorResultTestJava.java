package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.generators.GeneratorRunner;
import io.swagger.codegen.v3.service.GenerationRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

public class GeneratorResultTestJava {


    @Test
    public void testJavaGenerator_OneOf() throws Exception {

        String name = "java";
        String specPath = "3_0_0/composedFlatten/allof_inline_ref.yaml";
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
        }
    }
}
