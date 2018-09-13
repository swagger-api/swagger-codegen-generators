package io.swagger.codegen.v3.generators.scala;

import io.swagger.codegen.v3.CodegenModelFactory;
import io.swagger.codegen.v3.CodegenModelType;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedList;

public class AkkaHttpServerCodegenTest {

    @Test
    public void testAddLowercaseHttpMethod() {
        CodegenOperation codegenOperation = CodegenModelFactory.newInstance(CodegenModelType.OPERATION);
        codegenOperation.httpMethod = "GET";
        Assert.assertNull(codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.LOWERCASE_HTTP_METHOD));
        AkkaHttpServerCodegen.addLowercaseHttpMethod(codegenOperation);
        Assert.assertEquals(codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.LOWERCASE_HTTP_METHOD),  "get");
    }

    @Test
    public void testSplitToPaths() {
        CodegenOperation codegenOperation = CodegenModelFactory.newInstance(CodegenModelType.OPERATION);

        codegenOperation.path = "/pet";
        Assert.assertNull(codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.PATHS));
        AkkaHttpServerCodegen.splitToPaths(codegenOperation);
        LinkedList<TextOrMatcher> expectedPaths = new LinkedList<TextOrMatcher>(){{
            add(new TextOrMatcher("pet", true, false));
        }};
        Assert.assertEquals((LinkedList<TextOrMatcher>) codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.PATHS), expectedPaths);

        codegenOperation.path = "/some/pet";
        AkkaHttpServerCodegen.splitToPaths(codegenOperation);
        expectedPaths = new LinkedList<TextOrMatcher>(){{
            add(new TextOrMatcher("some", true, true));
            add(new TextOrMatcher("pet", true, false));
        }};
        Assert.assertEquals((LinkedList<TextOrMatcher>) codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.PATHS), expectedPaths);

        CodegenParameter petId = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        petId.paramName = "petId";
        petId.dataType = "Long";
        CodegenParameter petName = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        petName.paramName = "petName";
        petName.dataType = "String";
        CodegenParameter unknownMatcher = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        unknownMatcher.paramName = "unknownMatcher";
        unknownMatcher.dataType = "List[String]";
        codegenOperation.path = "/pet/{petId}/name/{petName}/{unknownMatcher}";
        codegenOperation.pathParams.add(petId);
        codegenOperation.pathParams.add(petName);
        codegenOperation.pathParams.add(unknownMatcher);

        AkkaHttpServerCodegen.splitToPaths(codegenOperation);
        expectedPaths = new LinkedList<TextOrMatcher>(){{
            add(new TextOrMatcher("pet", true, true));
            add(new TextOrMatcher("LongNumber", false, true));
            add(new TextOrMatcher("name", true, true));
            add(new TextOrMatcher("Segment", false, true));
            add(new TextOrMatcher("Segment", false, false));
        }};
        Assert.assertEquals((LinkedList<TextOrMatcher>) codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.PATHS), expectedPaths);

        CodegenParameter unknownMatcherWithString = unknownMatcher.copy();
        unknownMatcherWithString.dataType = "String";
        LinkedList<CodegenParameter> expectedMatchedPathParams = new LinkedList<CodegenParameter>(){{
            add(petId);
            add(petName);
            add(unknownMatcherWithString);
        }};
        Assert.assertEquals((LinkedList<CodegenParameter>) codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.MATCHED_PATH_PARAMS), expectedMatchedPathParams);

    }
}