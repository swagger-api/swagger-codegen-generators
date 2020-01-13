package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenResponse;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JavaCXFClientCodegenTest extends AbstractCodegenTest {

    @Test
    public void responseWithoutContent() throws Exception {
        final OpenAPI openAPI = getOpenAPI("3_0_0/response_without_content.yaml");
        final Operation operation = openAPI.getPaths().get("/pets").getGet();

        final JavaCXFClientCodegen codegen = new JavaCXFClientCodegen();
        codegen.preprocessOpenAPI(openAPI);
        final CodegenOperation co = codegen.fromOperation("getAllPets", "GET", operation, openAPI.getComponents().getSchemas(), openAPI);

        Map<String, Object> objs = new HashMap<>();
        objs.put("operations", Collections.singletonMap("operation", Collections.singletonList(co)));
        objs.put("imports", Collections.emptyList());
        codegen.postProcessOperations(objs);

        Assert.assertEquals(co.responses.size(), 2);
        CodegenResponse cr1 = co.responses.get(0);
        Assert.assertEquals(cr1.code, "200");
        Assert.assertEquals(cr1.baseType, "Pet");
        Assert.assertEquals(cr1.dataType, "List<Pet>");
        Assert.assertFalse(cr1.vendorExtensions.containsKey("x-java-is-response-void"));

        CodegenResponse cr2 = co.responses.get(1);
        Assert.assertEquals(cr2.code, "400");
        Assert.assertEquals(cr2.baseType, "Void");
        Assert.assertEquals(cr2.dataType, "void");
        Assert.assertEquals(cr2.vendorExtensions.get("x-java-is-response-void"), Boolean.TRUE);
    }


}
