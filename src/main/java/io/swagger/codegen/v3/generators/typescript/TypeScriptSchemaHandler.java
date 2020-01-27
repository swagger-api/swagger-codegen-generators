package io.swagger.codegen.v3.generators.typescript;

import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.SchemaHandler;

public class TypeScriptSchemaHandler extends SchemaHandler {

    private AbstractTypeScriptClientCodegen codegenConfig;

    public TypeScriptSchemaHandler(DefaultCodegenConfig codegenConfig) {
        super(codegenConfig);
        this.codegenConfig = (AbstractTypeScriptClientCodegen) codegenConfig;
    }
}
