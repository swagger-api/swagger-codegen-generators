package io.swagger.codegen.v3.generators.scala;

import io.swagger.codegen.v3.CodegenModelFactory;
import io.swagger.codegen.v3.CodegenModelType;
import io.swagger.codegen.v3.CodegenOperation;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;
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
        Iterator<String> expectedResult = new LinkedList<String>(){{add("pet");}}.iterator();
        Assert.assertEquals((Iterator<String>) codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.PATHS), expectedResult);

        codegenOperation.path = "/some/pet";
        AkkaHttpServerCodegen.splitToPaths(codegenOperation);
        expectedResult = new LinkedList<String>(){{add("some");add("pet");}}.iterator();
        Assert.assertEquals((Iterator<String>) codegenOperation.getVendorExtensions().get(AkkaHttpServerCodegen.PATHS), expectedResult);

    }
}