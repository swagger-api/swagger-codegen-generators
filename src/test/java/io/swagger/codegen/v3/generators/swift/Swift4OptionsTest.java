package io.swagger.codegen.v3.generators.swift;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.generators.AbstractOptionsTest;
import io.swagger.codegen.v3.generators.options.Swift4OptionsProvider;
import mockit.Expectations;
import mockit.Tested;

import java.nio.channels.spi.SelectorProvider;

public class Swift4OptionsTest extends AbstractOptionsTest {

    @Tested
    private Swift4Codegen clientCodegen;

    public Swift4OptionsTest() {
        super(new Swift4OptionsProvider());
    }

    @Override
    protected CodegenConfig getCodegenConfig() {
        return clientCodegen;
    }

    @SuppressWarnings("unused")
    @Override
    protected void setExpectations() {
        new Expectations(clientCodegen) {{
            clientCodegen.setSortParamsByRequiredFlag(Boolean.valueOf(Swift4OptionsProvider.SORT_PARAMS_VALUE));
            times = 1;
            clientCodegen.setProjectName(Swift4OptionsProvider.PROJECT_NAME_VALUE);
            times = 1;
            clientCodegen.setResponseAs(Swift4OptionsProvider.RESPONSE_AS_VALUE.split(","));
            times = 1;
            clientCodegen.setUnwrapRequired(Boolean.valueOf(Swift4OptionsProvider.UNWRAP_REQUIRED_VALUE));
            times = 1;
            clientCodegen.setObjcCompatible(Boolean.valueOf(Swift4OptionsProvider.OBJC_COMPATIBLE_VALUE));
            times = 1;
            clientCodegen.setLenientTypeCast(Boolean.valueOf(Swift4OptionsProvider.LENIENT_TYPE_CAST_VALUE));
            times = 1;
            clientCodegen.setPackageName(Swift4OptionsProvider.PACKAGE_NAME_VALUE);
            times = 1;

        }};
    }
}
