package io.swagger.codegen.v3.generators.html;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenModelFactory;
import io.swagger.codegen.v3.CodegenModelType;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.SchemaHandler;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;

public class HtmlSchemaHandler extends SchemaHandler {

    public HtmlSchemaHandler(DefaultCodegenConfig codegenConfig) {
        super(codegenConfig);
    }

    @Override
    public void processComposedSchemas(CodegenModel codegenModel, Schema schema, Map<String, CodegenModel> allModels) {
        // no ops for html generator
    }
}
