package io.swagger.codegen.v3.generators.kotlin;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.generators.AbstractOptionsTest;
import io.swagger.codegen.v3.generators.options.KotlinClientCodegenOptionsProvider;
import mockit.Expectations;
import mockit.Tested;

public class KotlinClientCodegenOptionsTest extends AbstractOptionsTest {

    @Tested
    private KotlinClientCodegen codegen;

    public KotlinClientCodegenOptionsTest() {
        super(new KotlinClientCodegenOptionsProvider());
    }

    @Override
    protected CodegenConfig getCodegenConfig() {
        return codegen;
    }

    @SuppressWarnings("unused")
    @Override
    protected void setExpectations() {
        new Expectations(codegen) {{
            codegen.setPackageName(KotlinClientCodegenOptionsProvider.PACKAGE_NAME_VALUE);
            times = 1;
            codegen.setArtifactVersion(KotlinClientCodegenOptionsProvider.ARTIFACT_VERSION_VALUE);
            times = 1;
            codegen.setArtifactId(KotlinClientCodegenOptionsProvider.ARTIFACT_ID);
            times = 1;
            codegen.setGroupId(KotlinClientCodegenOptionsProvider.GROUP_ID);
            times = 1;
            codegen.setSourceFolder(KotlinClientCodegenOptionsProvider.SOURCE_FOLDER);
            times = 1;
            codegen.setDateLibrary(KotlinClientCodegenOptionsProvider.DATE_LIBRARY);
        }};
    }
}

