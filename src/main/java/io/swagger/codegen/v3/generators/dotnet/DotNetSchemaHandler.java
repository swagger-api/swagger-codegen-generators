package io.swagger.codegen.v3.generators.dotnet;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.SchemaHandler;
import io.swagger.v3.oas.models.media.ArraySchema;

import java.util.Map;

public class DotNetSchemaHandler extends SchemaHandler {

    public DotNetSchemaHandler(DefaultCodegenConfig codegenConfig) {
        super(codegenConfig);
    }

    protected CodegenModel processArrayItemSchema(CodegenModel codegenModel, ArraySchema arraySchema, Map<String, CodegenModel> allModels) {
        final CodegenModel composedModel = super.processArrayItemSchema(codegenModel, arraySchema, allModels);
        if (composedModel == null) {
            return null;
        }
        if (codegenModel.getParent().equals("List<>")) {
            codegenModel.setParent("List<" + composedModel.getClassname() + ">");
        }
        return composedModel;
    }
}
