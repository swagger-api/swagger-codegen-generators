package io.swagger.codegen.v3.generators.dotnet;

import io.swagger.codegen.v3.CodegenConfig;
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
}
