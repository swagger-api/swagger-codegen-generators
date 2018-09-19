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
    public void testAddPathMatcher() {
        CodegenOperation codegenOperation = CodegenModelFactory.newInstance(CodegenModelType.OPERATION);

        codegenOperation.path = "/pet";
        Assert.assertNull(codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.PATHS));

        AkkaHttpServerCodegen.addPathMatcher(codegenOperation);

        LinkedList<TextOrMatcher> expectedPaths = new LinkedList<TextOrMatcher>(){{
            add(new TextOrMatcher("pet", true, false));
        }};
        Assert.assertEquals((LinkedList<TextOrMatcher>) codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.PATHS), expectedPaths);

        codegenOperation.path = "/some/pet";

        AkkaHttpServerCodegen.addPathMatcher(codegenOperation);

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

        AkkaHttpServerCodegen.addPathMatcher(codegenOperation);

        expectedPaths = new LinkedList<TextOrMatcher>(){{
            add(new TextOrMatcher("pet", true, true));
            add(new TextOrMatcher("LongNumber", false, true));
            add(new TextOrMatcher("name", true, true));
            add(new TextOrMatcher("Segment", false, true));
            add(new TextOrMatcher("Segment", false, false));
        }};
        Assert.assertEquals((LinkedList<TextOrMatcher>) codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.PATHS), expectedPaths);
    }

    @Test
    public void testAddQueryParamsWithSupportedTypes() {
        CodegenOperation codegenOperation = CodegenModelFactory.newInstance(CodegenModelType.OPERATION);

        CodegenParameter petAge = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        petAge.paramName = "petAge";
        petAge.dataType = "Int";
        CodegenParameter petInfos = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        petInfos.paramName = "petInfos";
        petInfos.dataType = "Object";
        codegenOperation.queryParams.add(petAge);
        codegenOperation.queryParams.add(petInfos);
        CodegenParameter petInfosWithString = petInfos.copy();
        petInfosWithString.dataType = AkkaHttpServerCodegen.FALLBACK_DATA_TYPE;

        AkkaHttpServerCodegen.addQueryParamsWithSupportedType(codegenOperation);

        LinkedList<CodegenParameter> expectedMatchedPathParams = new LinkedList<CodegenParameter>(){{
            add(petAge);
            add(petInfosWithString);
        }};
        Assert.assertEquals((LinkedList<CodegenParameter>) codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.QUERY_PARAMS_WITH_SUPPORTED_TYPE), expectedMatchedPathParams);
    }

    @Test
    public void testAddAllParamsWithSupportedTypes() {
        CodegenOperation codegenOperation = CodegenModelFactory.newInstance(CodegenModelType.OPERATION);

        CodegenParameter petId = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        petId.paramName = "petId";
        petId.dataType = "Long";
        CodegenParameter petName = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        petName.paramName = "petName";
        petName.dataType = "String";
        CodegenParameter unknownMatcher = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        unknownMatcher.paramName = "unknownMatcher";
        unknownMatcher.dataType = "List[String]";
        CodegenParameter petAge = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        petAge.paramName = "petAge";
        petAge.dataType = "Int";
        CodegenParameter petInfos = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        petInfos.paramName = "petInfos";
        petInfos.dataType = "Object";
        CodegenParameter petForm1 = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        petForm1.paramName = "petForm1";
        petForm1.dataType = "String";
        CodegenParameter petForm2 = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        petForm2.paramName = "petForm2";
        petForm2.dataType = "Object";
        codegenOperation.pathParams.add(petId);
        codegenOperation.pathParams.add(petName);
        codegenOperation.pathParams.add(unknownMatcher);
        codegenOperation.queryParams.add(petAge);
        codegenOperation.queryParams.add(petInfos);
        codegenOperation.formParams.add(petForm1);
        codegenOperation.formParams.add(petForm2);
        codegenOperation.allParams.add(petId.copy());
        codegenOperation.allParams.add(petName.copy());
        codegenOperation.allParams.add(unknownMatcher.copy());
        codegenOperation.allParams.add(petAge.copy());
        codegenOperation.allParams.add(petInfos.copy());
        codegenOperation.allParams.add(petForm1.copy());
        codegenOperation.allParams.add(petForm2.copy());
        CodegenParameter unknownMatcherWithString = unknownMatcher.copy();
        unknownMatcherWithString.dataType = AkkaHttpServerCodegen.FALLBACK_DATA_TYPE;
        CodegenParameter petInfosWithString = petInfos.copy();
        petInfosWithString.dataType = AkkaHttpServerCodegen.FALLBACK_DATA_TYPE;
        CodegenParameter petForm2WithString = petForm2.copy();
        petForm2WithString.dataType = AkkaHttpServerCodegen.FALLBACK_DATA_TYPE;

        AkkaHttpServerCodegen.addAllParamsWithSupportedTypes(codegenOperation);

        LinkedList<CodegenParameter> expectedMatchedPathParams = new LinkedList<CodegenParameter>(){{
            add(petId);
            add(petName);
            add(unknownMatcherWithString);
            add(petAge);
            add(petInfosWithString);
            add(petForm1);
            add(petForm2WithString);
        }};
        Assert.assertEquals((LinkedList<CodegenParameter>) codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.PARAMS_WITH_SUPPORTED_TYPE), expectedMatchedPathParams);
    }
}