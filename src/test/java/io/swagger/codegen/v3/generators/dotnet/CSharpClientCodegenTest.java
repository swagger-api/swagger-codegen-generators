package io.swagger.codegen.v3.generators.dotnet;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.ISchemaHandler;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import io.swagger.codegen.v3.generators.CodegenWrapper;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CSharpClientCodegenTest extends AbstractCodegenTest {

    @Test
    public void checkOneOfModelCreation() {
        final OpenAPI openAPI = getOpenAPI("3_0_0/composed_schemas.yaml");
        final CodegenConfig config = new CSharpClientCodegen();
        final CodegenWrapper codegenWrapper = processSchemas(config, openAPI);

        CodegenModel codegenModel = codegenWrapper.getAllModels().get("PartMaster");

        boolean hasOneOfProperty = codegenModel.getVars()
            .stream()
            .anyMatch(codegenProperty -> codegenProperty.datatype.equals("OneOfPartMasterDestination"));

        Assert.assertTrue(hasOneOfProperty);

        hasOneOfProperty = codegenModel.getVars()
            .stream()
            .anyMatch(codegenProperty -> codegenProperty.datatype.equals("OneOfPartMasterOrigin"));

        Assert.assertTrue(hasOneOfProperty);

        final ISchemaHandler schemaHandler = codegenWrapper.getSchemaHandler();

        boolean hasComposedModel = schemaHandler.getModels()
            .stream()
            .anyMatch(model -> model.name.equals("OneOfPartMasterDestination"));

        Assert.assertTrue(hasComposedModel);

        hasComposedModel = schemaHandler.getModels()
            .stream()
            .anyMatch(model -> model.name.equals("OneOfPartMasterOrigin"));

        Assert.assertTrue(hasComposedModel);
    }

    @Test
    public void renameReservedWordModel() {
        final OpenAPI openAPI = getOpenAPI("3_0_0/composed_schemas.yaml");
        final CodegenConfig config = new CSharpClientCodegen();
        final CodegenWrapper codegenWrapper = processSchemas(config, openAPI);

        CodegenModel codegenModel = codegenWrapper.getAllModels().get("ModelClient");
        Assert.assertNotNull(codegenModel);

        codegenModel = codegenWrapper.getAllModels().get("ModelList");
        Assert.assertNotNull(codegenModel);
    }

    @Test
    public void testProcessOpts_WithExplicitSourceFolder() {
        // Arrange
        AbstractCSharpCodegen codegen = new CSharpClientCodegen();
        String expectedCustomFolder = "custom_src_directory";

        // Pass the explicit key configuration
        codegen.additionalProperties().put(CodegenConstants.SOURCE_FOLDER, expectedCustomFolder);

        // Act
        codegen.processOpts();

        // Assert: Verify your single-line logical bindings hold true
        Assert.assertEquals(codegen.getSourceFolder(), expectedCustomFolder, "sourceFolder should be updated via explicit option.");
        Assert.assertEquals(codegen.getTestFolder(), expectedCustomFolder, "testFolder should explicitly match sourceFolder values when custom defined.");
    }

    @Test
    public void testProcessOpts_WithDefaultSourceFolder() {
        // Arrange
        AbstractCSharpCodegen codegen = new CSharpClientCodegen();

        // Ensure the option is completely absent from execution properties
        codegen.additionalProperties().remove(CodegenConstants.SOURCE_FOLDER);
        String fallbackValue = codegen.sourceFolder; // Capture initial setup default

        // Act
        codegen.processOpts();

        // Assert: Ensure properties dictionary falls back gracefully
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SOURCE_FOLDER), fallbackValue, "Properties must append default placeholder context.");
    }


}
