package io.swagger.codegen.languages.swift;

import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static io.swagger.codegen.handlebars.helpers.ExtensionHelper.getBooleanValue;

public class Swift4CodegenTest {

    Swift4Codegen swiftCodegen = new Swift4Codegen();

    @Test
    public void testCapitalizedReservedWord() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("AS", null), "_as");
    }

    @Test
    public void testReservedWord() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("Public", null), "_public");
    }

    @Test
    public void shouldNotBreakNonReservedWord() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("Error", null), "error");
    }

    @Test
    public void shouldNotBreakCorrectName() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("EntryName", null), "entryName");
    }

    @Test
    public void testSingleWordAllCaps() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("VALUE", null), "value");
    }

    @Test
    public void testSingleWordLowercase() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("value", null), "value");
    }

    @Test
    public void testCapitalsWithUnderscore() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("ENTRY_NAME", null), "entryName");
    }

    @Test
    public void testCapitalsWithDash() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("ENTRY-NAME", null), "entryName");
    }

    @Test
    public void testCapitalsWithSpace() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("ENTRY NAME", null), "entryName");
    }

    @Test
    public void testLowercaseWithUnderscore() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("entry_name", null), "entryName");
    }

    @Test
    public void testStartingWithNumber() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("123EntryName", null), "_123entryName");
        Assert.assertEquals(swiftCodegen.toEnumVarName("123Entry_name", null), "_123entryName");
        Assert.assertEquals(swiftCodegen.toEnumVarName("123EntryName123", null), "_123entryName123");
    }

    @Test(description = "returns Data when response format is binary")
    public void binaryDataTest() throws IOException {
        final OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/resources/3_0_0/binaryDataTest.json");
        final Swift4Codegen codegen = new Swift4Codegen();
        final String path = "/tests/binaryResponse";
        final Operation p = openAPI.getPaths().get(path).getPost();
        final CodegenOperation op = codegen.fromOperation(path, "post", p, openAPI.getComponents().getSchemas());

        Assert.assertEquals(op.returnType, "Data");
        // TODO - must be checked. It seems that there is a new expected value "Object" set in the method:
        // io.swagger.codegen.languages.DefaultCodegenConfig.fromRequestBody
//        Assert.assertEquals(op.bodyParam.dataType, "Data");
        Assert.assertTrue(getBooleanValue(op.bodyParam, CodegenConstants.IS_BINARY_EXT_NAME));
        // TODO - have to be checked - in the old code generator there was an expected value set but in the current it isn't
        // I have no idea why...
//        Assert.assertTrue(getBooleanValue(op.responses.get(0), CodegenConstants.IS_BINARY_EXT_NAME));
    }

    @Test(description = "returns Date when response format is date")
    public void dateTest() {
        final OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/resources/3_0_0/datePropertyTest.json");
        final Swift4Codegen codegen = new Swift4Codegen();
        final String path = "/tests/dateResponse";
        final Operation p = openAPI.getPaths().get(path).getPost();
        final CodegenOperation op = codegen.fromOperation(path, "post", p, openAPI.getComponents().getSchemas());

        Assert.assertEquals(op.returnType, "Date");
        Assert.assertEquals(op.bodyParam.dataType, "Date");
    }

    @Test
    public void testDefaultPodAuthors() throws Exception {
        // Given

        // When
        swiftCodegen.processOpts();

        // Then
        final String podAuthors = (String) swiftCodegen.additionalProperties().get(Swift4Codegen.POD_AUTHORS);
        Assert.assertEquals(podAuthors, Swift4Codegen.DEFAULT_POD_AUTHORS);
    }

    @Test
    public void testPodAuthors() throws Exception {
        // Given
        final String swaggerDevs = "Swagger Devs";
        swiftCodegen.additionalProperties().put(Swift4Codegen.POD_AUTHORS, swaggerDevs);

        // When
        swiftCodegen.processOpts();

        // Then
        final String podAuthors = (String) swiftCodegen.additionalProperties().get(Swift4Codegen.POD_AUTHORS);
        Assert.assertEquals(podAuthors, swaggerDevs);
    }

    @Test
    public void testInitialConfigValues() throws Exception {
        final Swift4Codegen codegen = new Swift4Codegen();
        codegen.processOpts();

        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.TRUE);
        Assert.assertEquals(codegen.getHideGenerationTimestamp().booleanValue(), true);
    }

    @Test
    public void testSettersForConfigValues() throws Exception {
        final Swift4Codegen codegen = new Swift4Codegen();
        codegen.setHideGenerationTimestamp(false);
        codegen.processOpts();

        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
        Assert.assertEquals(codegen.getHideGenerationTimestamp().booleanValue(), false);
    }

    @Test
    public void testAdditionalPropertiesPutForConfigValues() throws Exception {
        final Swift4Codegen codegen = new Swift4Codegen();
        codegen.additionalProperties().put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, false);
        codegen.processOpts();

        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
        Assert.assertEquals(codegen.getHideGenerationTimestamp().booleanValue(), false);
    }

}
