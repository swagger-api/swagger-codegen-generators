package io.swagger.codegen.languages.java;

import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenResponse;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JavaCXFClientCodegenTest {

    @Test
    public void responseWithoutContent() throws Exception {
        final Schema listOfPets = new ArraySchema()
                .items(new Schema<>().$ref("#/components/schemas/Pet"));
        Operation operation = new Operation().responses(new ApiResponses()
                .addApiResponse("200", new ApiResponse()
                        .description("Return a list of pets")
                        .content(new Content().addMediaType("application/json", 
                                new MediaType().schema(listOfPets))))
                .addApiResponse("400", new ApiResponse()
                        .description("Error")));
        final Map<String, Schema> allDefinitions = Collections.singletonMap("Pet", new ObjectSchema());

        final JavaCXFClientCodegen codegen = new JavaCXFClientCodegen();
        final CodegenOperation co = codegen.fromOperation("getAllPets", "GET", operation, allDefinitions);

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
