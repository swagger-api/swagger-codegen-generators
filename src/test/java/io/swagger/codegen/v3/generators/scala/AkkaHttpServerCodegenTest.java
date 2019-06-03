package io.swagger.codegen.v3.generators.scala;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AkkaHttpServerCodegenTest {

    @Test
    public void testSetComplexTypes() {
        CodegenOperation codegenOperation1 = CodegenModelFactory.newInstance(CodegenModelType.OPERATION);
        CodegenOperation codegenOperation2 = CodegenModelFactory.newInstance(CodegenModelType.OPERATION);
        CodegenOperation codegenOperation3 = CodegenModelFactory.newInstance(CodegenModelType.OPERATION);
        CodegenOperation codegenOperation4 = CodegenModelFactory.newInstance(CodegenModelType.OPERATION);

        CodegenParameter param1 = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        CodegenParameter param2 = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        CodegenParameter param3 = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        CodegenParameter param4 = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        CodegenParameter param5 = CodegenModelFactory.newInstance(CodegenModelType.PARAMETER);
        CodegenResponse response1 = CodegenModelFactory.newInstance(CodegenModelType.RESPONSE);
        CodegenResponse response2 = CodegenModelFactory.newInstance(CodegenModelType.RESPONSE);
        CodegenResponse response3 = CodegenModelFactory.newInstance(CodegenModelType.RESPONSE);

        param1.dataType = "Pet";
        param1.getVendorExtensions().put(CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME, Boolean.FALSE);
        param1.getVendorExtensions().put(CodegenConstants.IS_BODY_PARAM_EXT_NAME, Boolean.TRUE);
        param2.dataType = "Pet";
        param2.getVendorExtensions().put(CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME, Boolean.FALSE);
        param2.getVendorExtensions().put(CodegenConstants.IS_BODY_PARAM_EXT_NAME, Boolean.TRUE);
        param3.dataType = "User";
        param3.getVendorExtensions().put(CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME, Boolean.FALSE);
        param3.getVendorExtensions().put(CodegenConstants.IS_BODY_PARAM_EXT_NAME, Boolean.TRUE);
        param4.dataType = "String";
        param4.getVendorExtensions().put(CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME, Boolean.TRUE);
        param4.getVendorExtensions().put(CodegenConstants.IS_BODY_PARAM_EXT_NAME, Boolean.TRUE);
        param5.dataType = "Order";
        param5.getVendorExtensions().put(CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME, Boolean.FALSE);
        param5.getVendorExtensions().put(CodegenConstants.IS_QUERY_PARAM_EXT_NAME, Boolean.TRUE);
        response1.dataType = "Pet";
        response1.getVendorExtensions().put(CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME, Boolean.FALSE);
        response2.dataType = "Pet";
        response2.getVendorExtensions().put(CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME, Boolean.FALSE);
        response3.dataType = "User";
        response3.getVendorExtensions().put(CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME, Boolean.FALSE);
        codegenOperation1.bodyParam = param1;
        codegenOperation1.bodyParams.add(param1);
        codegenOperation1.allParams.add(param1);
        codegenOperation2.bodyParam = param2;
        codegenOperation2.bodyParams.add(param2);
        codegenOperation2.allParams.add(param2);
        codegenOperation3.bodyParam = param3;
        codegenOperation3.bodyParams.add(param3);
        codegenOperation3.allParams.add(param3);
        codegenOperation4.bodyParam = param4;
        codegenOperation4.bodyParams.add(param4);
        codegenOperation4.allParams.add(param4);
        codegenOperation4.queryParams.add(param5);
        codegenOperation4.allParams.add(param5);
        codegenOperation1.responses.add(response1);
        codegenOperation2.responses.add(response2);
        codegenOperation2.responses.add(response3);

        List<CodegenOperation> operationList = new LinkedList<CodegenOperation>(){{
            addAll(Arrays.asList(
                    codegenOperation1,
                    codegenOperation2,
                    codegenOperation3,
                    codegenOperation4
            ));
        }};
        Map<String, Object> operation = new HashMap<>();
        operation.put("operation", operationList);
        Map<String, Object> operations = new HashMap<>();
        operations.put("operations", operation);

        Map<String, Object> result = AkkaHttpServerCodegen.setComplexTypes(operations);

        Assert.assertEquals(result.get("hasComplexTypes"), Boolean.TRUE);
        Assert.assertEquals(result.get("complexRequestTypes"), new HashSet<String>(){{addAll(Arrays.asList("Pet","User"));}});
        Assert.assertEquals(result.get("complexReturnTypes"), new LinkedList<CodegenResponse>(){{addAll(Arrays.asList(response1, response2, response3));}});
        Assert.assertEquals(codegenOperation1.getVendorExtensions().get("complexReturnTypes"), new LinkedList<CodegenResponse>(){{add(response1);}});
        Assert.assertEquals(codegenOperation2.getVendorExtensions().get("complexReturnTypes"), new LinkedList<CodegenResponse>(){{addAll(Arrays.asList(response1, response3));}});
    }

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

    @Test
    public void testFileGeneration() throws IOException {
        TemporaryFolder folder = new TemporaryFolder();

        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setLang("scala-akka-http-server")
                .setInputSpecURL("src/test/resources/3_0_0/petstore.yaml")
                .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/Controller.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/AkkaHttpHelper.scala").exists());

        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/api/DefaultApi.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/api/PetApi.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/api/StoreApi.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/api/UserApi.scala").exists());

        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/model/ApiResponse.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/model/Body.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/model/Category.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/model/Order.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/model/Pet.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/model/Tag.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/model/Tag.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/model/Test.scala").exists());
        Assert.assertTrue(new File(output, "src/main/scala/io/swagger/server/model/User.scala").exists());
        folder.delete();
    }
}