package io.swagger.codegen.v3.generators.html;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenModelFactory;
import io.swagger.codegen.v3.CodegenModelType;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.SchemaHandler;

public class HtmlSchemaHandler extends SchemaHandler {

    public HtmlSchemaHandler(DefaultCodegenConfig codegenConfig) {
        super(codegenConfig);
    }

    protected CodegenModel createComposedModel(String name) {
        final CodegenModel composedModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        this.configureModel(composedModel, name);
        return composedModel;
    }
}
