package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.CodegenArgument;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class AbstractJavaJAXRSServerCodegenTest {

    private final AbstractJavaJAXRSServerCodegen fakeJavaJAXRSCodegen = new P_AbstractJavaJAXRSServerCodegen();

    @Test
    public void convertApiName() throws Exception {
        Assert.assertEquals(fakeJavaJAXRSCodegen.toApiName("name"), "NameApi");
        Assert.assertEquals(fakeJavaJAXRSCodegen.toApiName("$name"), "NameApi");
        Assert.assertEquals(fakeJavaJAXRSCodegen.toApiName("nam#e"), "NameApi");
        Assert.assertEquals(fakeJavaJAXRSCodegen.toApiName("$another-fake?"), "AnotherFakeApi");
        Assert.assertEquals(fakeJavaJAXRSCodegen.toApiName("fake_classname_tags 123#$%^"), "FakeClassnameTags123Api");
    }

    @Test
    public void testInitialPackageNamesValues() throws Exception {
        final AbstractJavaJAXRSServerCodegen codegen = new P_AbstractJavaJAXRSServerCodegen();
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "io.swagger.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "io.swagger.model");
        Assert.assertEquals(codegen.apiPackage(), "io.swagger.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "io.swagger.api");
        Assert.assertEquals(codegen.invokerPackage, "io.swagger.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "io.swagger.api");
    }

    @Test
    public void testPackageNamesSetWithSetters() throws Exception {
        final AbstractJavaJAXRSServerCodegen codegen = new P_AbstractJavaJAXRSServerCodegen();
        codegen.setModelPackage("xx.yyyyyyyy.model");
        codegen.setApiPackage("xx.yyyyyyyy.api");
        codegen.setInvokerPackage("xx.yyyyyyyy.invoker");
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "xx.yyyyyyyy.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xx.yyyyyyyy.model");
        Assert.assertEquals(codegen.apiPackage(), "xx.yyyyyyyy.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xx.yyyyyyyy.api");
        Assert.assertEquals(codegen.invokerPackage, "xx.yyyyyyyy.invoker");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "xx.yyyyyyyy.invoker");
    }

    @Test
    public void testPackageNamesSetWithAdditionalProperties() throws Exception {
        final AbstractJavaJAXRSServerCodegen codegen = new P_AbstractJavaJAXRSServerCodegen();
        codegen.additionalProperties().put(CodegenConstants.MODEL_PACKAGE, "xxx.yyyyy.mmmmm.model");
        codegen.additionalProperties().put(CodegenConstants.API_PACKAGE, "xxx.yyyyy.aaaaa.api");
        codegen.additionalProperties().put(CodegenConstants.INVOKER_PACKAGE,"xxx.yyyyy.iiii.invoker");
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "xxx.yyyyy.mmmmm.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xxx.yyyyy.mmmmm.model");
        Assert.assertEquals(codegen.apiPackage(), "xxx.yyyyy.aaaaa.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xxx.yyyyy.aaaaa.api");
        Assert.assertEquals(codegen.invokerPackage, "xxx.yyyyy.iiii.invoker");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "xxx.yyyyy.iiii.invoker");
    }

    private static class P_AbstractJavaJAXRSServerCodegen extends AbstractJavaJAXRSServerCodegen {
        @Override
        public String getArgumentsLocation() {
            return null;
        }

        @Override
        public String getDefaultTemplateDir() {
            return null;
        }

        @Override
        public CodegenType getTag() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public List<CodegenArgument> readLanguageArguments() {
            return null;
        }
    }
}
