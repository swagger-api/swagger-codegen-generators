package io.swagger.codegen.languages;

import io.swagger.codegen.CodegenArgument;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenType;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.List;

public class DefaultCodegenConfigTest {

    @Test
    public void testInitialValues() throws Exception {
        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage, "");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "");
        Assert.assertEquals(codegen.apiPackage, "");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "");
        Assert.assertEquals(codegen.sortParamsByRequiredFlag, Boolean.TRUE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.TRUE);
    }

    @Test
    public void testAdditionalProperties() throws Exception {
        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.setModelPackage("xxx.yyyyy.zzzzzzz.model");
        codegen.setApiPackage("xxx.yyyyy.zzzzzzz.api");
        codegen.setSortParamsByRequiredFlag(false);
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage, "xxx.yyyyy.zzzzzzz.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xxx.yyyyy.zzzzzzz.model");
        Assert.assertEquals(codegen.apiPackage, "xxx.yyyyy.zzzzzzz.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xxx.yyyyy.zzzzzzz.api");
        Assert.assertEquals(codegen.sortParamsByRequiredFlag, Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.FALSE);
    }

    private static class P_DefaultCodegenConfig extends DefaultCodegenConfig{
        @Override
        public String getArgumentsLocation() {
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
