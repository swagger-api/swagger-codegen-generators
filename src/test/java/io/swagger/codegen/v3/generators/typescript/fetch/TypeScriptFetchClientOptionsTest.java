package io.swagger.codegen.v3.generators.typescript.fetch;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.generators.AbstractOptionsTest;
import io.swagger.codegen.v3.generators.options.TypeScriptFetchClientOptionsProvider;
import io.swagger.codegen.v3.generators.typescript.TypeScriptFetchClientCodegen;
import mockit.Expectations;
import mockit.Tested;

public class TypeScriptFetchClientOptionsTest extends AbstractOptionsTest {

    @Tested
    private TypeScriptFetchClientCodegen clientCodegen;

    public TypeScriptFetchClientOptionsTest() {
        super(new TypeScriptFetchClientOptionsProvider());
    }

    @Override
    protected CodegenConfig getCodegenConfig() {
        return clientCodegen;
    }

    @SuppressWarnings("unused")
    @Override
    protected void setExpectations() {
        new Expectations(clientCodegen) {
            {
                clientCodegen.setSortParamsByRequiredFlag(
                        Boolean.valueOf(TypeScriptFetchClientOptionsProvider.SORT_PARAMS_VALUE));
                times = 1;
                clientCodegen.setModelPropertyNaming(TypeScriptFetchClientOptionsProvider.MODEL_PROPERTY_NAMING_VALUE);
                times = 1;
                clientCodegen.setSupportsES6(Boolean.valueOf(TypeScriptFetchClientOptionsProvider.SUPPORTS_ES6_VALUE));
                times = 1;
            }
        };
    }
}
