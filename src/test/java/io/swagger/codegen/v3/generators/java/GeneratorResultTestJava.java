package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.generators.GeneratorRunner;
import io.swagger.codegen.v3.service.GenerationRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            outFolder, options -> {});

        Assert.assertFalse(files.isEmpty());
        for (File f: files) {
            // TODO test stuff
        }
    }

    @Test
    public void interfaceWithCustomDiscriminator() throws Exception {

        String name = "java";
        String specPath = "3_0_0/sample_interface_with_discriminator.json";
        GenerationRequest.CodegenVersion codegenVersion = GenerationRequest.CodegenVersion.V3;
        boolean v2Spec = false; // 3.0 spec
        boolean yaml = false;
        boolean flattenInlineComposedSchema = true;
        String outFolder = null; // temporary folder


        List<File> files = GeneratorRunner.runGenerator(
            name,
            specPath,
            codegenVersion,
            v2Spec,
            yaml,
            flattenInlineComposedSchema,
            outFolder,
            options -> options.setLibrary("resttemplate"));



        File interfaceFile = files.stream().filter(f -> f.getName().equals("Item.java")).findAny().orElseThrow(() -> new RuntimeException("No interface generated"));

        String interfaceContent = new String(Files.readAllBytes(Paths.get(interfaceFile.toURI())));

        Pattern typeInfoPattern = Pattern.compile( 	"(.*)(@JsonTypeInfo\\()(.*)(}\\))(.*)", Pattern.DOTALL);

        Matcher matcher = typeInfoPattern.matcher(interfaceContent);

        Assert.assertTrue(matcher.matches(), "No JsonTypeInfo generated into the interface file");

        String generatedTypeinfoLines = matcher.group(2)+matcher.group(3)+matcher.group(4);

        Assert.assertEquals( generatedTypeinfoLines,"@JsonTypeInfo(\n" +
            "  use = JsonTypeInfo.Id.NAME,\n" +
            "  include = JsonTypeInfo.As.PROPERTY,\n" +
            "  property = \"aCustomProperty\")\n" +
            "@JsonSubTypes({\n" +
            "  @JsonSubTypes.Type(value = ClassA.class, name = \"typeA\"),\n" +
            "  @JsonSubTypes.Type(value = ClassB.class, name = \"typeB\"),\n" +
            "  @JsonSubTypes.Type(value = ClassC.class, name = \"typeC\")\n" +
            "})", "Wrong json subtypes generated");

    }
}
