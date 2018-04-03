package io.swagger.codegen.languages.php;

import io.swagger.codegen.CodegenConstants;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PhpClientCodegenTest {
    @Test
    public void testInitialValues() throws Exception {
        final PhpClientCodegen codegen = new PhpClientCodegen();
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "Swagger\\Client\\Model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "Swagger\\Client\\Model");
        Assert.assertEquals(codegen.apiPackage(), "Swagger\\Client\\Api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "Swagger\\Client\\Api");
        Assert.assertEquals(codegen.invokerPackage, "Swagger\\Client");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "Swagger\\Client");
        Assert.assertEquals(codegen.getSortParamsByRequiredFlag(), Boolean.TRUE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.TRUE);
        Assert.assertEquals(codegen.getHideGenerationTimestamp(), Boolean.TRUE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.TRUE);
    }

    @Test
    public void testSetters() throws Exception {
        final PhpClientCodegen codegen = new PhpClientCodegen();
        codegen.setModelPackage("My\\Client\\Model");
        codegen.setApiPackage("My\\Client\\Api");
        codegen.setInvokerPackage("My\\Client\\Invoker");
        codegen.setSortParamsByRequiredFlag(false);
        codegen.setHideGenerationTimestamp(false);
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "My\\Client\\Model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "My\\Client\\Model");
        Assert.assertEquals(codegen.apiPackage(), "My\\Client\\Api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE),"My\\Client\\Api");
        Assert.assertEquals(codegen.invokerPackage, "My\\Client\\Invoker");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "My\\Client\\Invoker");
        Assert.assertEquals(codegen.getSortParamsByRequiredFlag(), Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.FALSE);
        Assert.assertEquals(codegen.getHideGenerationTimestamp(), Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
    }

    @Test
    public void testPutAdditionalProperties() throws Exception {
        final PhpClientCodegen codegen = new PhpClientCodegen();
        codegen.additionalProperties().put(CodegenConstants.MODEL_PACKAGE, "Xmodel");
        codegen.additionalProperties().put(CodegenConstants.API_PACKAGE, "Xapi");
        codegen.additionalProperties().put(CodegenConstants.INVOKER_PACKAGE, "Xinvoker");
        codegen.additionalProperties().put(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG, false);
        codegen.additionalProperties().put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, false);
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "Xinvoker\\Xmodel");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "Xinvoker\\Xmodel");
        Assert.assertEquals(codegen.apiPackage(), "Xinvoker\\Xapi");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "Xinvoker\\Xapi");
        Assert.assertEquals(codegen.getSortParamsByRequiredFlag(), Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.FALSE);
        Assert.assertEquals(codegen.getHideGenerationTimestamp(), Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
    }
}