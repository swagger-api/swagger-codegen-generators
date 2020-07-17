package io.swagger.codegen.v3.generators.dotnet;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import io.swagger.codegen.v3.generators.CodegenWrapper;
import io.swagger.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AspNetCoreServerCodegenTest extends AbstractCodegenTest {

    @Test(description = "Verify if List<> is fixed for array schema with composed items.")
    public void checkArrayItemsSchemaParent() {
        final OpenAPI openAPI = getOpenAPI("3_0_0/composed_schemas.yaml");
        final CodegenConfig config = new AspNetCoreServerCodegen();
        final CodegenWrapper codegenWrapper = processSchemas(config, openAPI);
        CodegenModel codegenModel = codegenWrapper.getAllModels().get("AllPetsResponse");
        Assert.assertEquals(codegenModel.parent, "List<OneOfAllPetsResponseItems>");
    }

    @Test
    public void checkArrayItemsSchemaProperty() {
        final OpenAPI openAPI = getOpenAPI("3_0_0/composed_schemas.yaml");
        final CodegenConfig config = new AspNetCoreServerCodegen();
        final CodegenWrapper codegenWrapper = processSchemas(config, openAPI);
        final CodegenModel codegenModel = codegenWrapper.getAllModels().get("House");

        final CodegenProperty codegenProperty = codegenModel.vars.stream().filter(property -> property.baseName.equals("pets")).findFirst().get();
        Assert.assertEquals(codegenProperty.datatype, "List<OneOfHousePetsItems>");

    }
}
