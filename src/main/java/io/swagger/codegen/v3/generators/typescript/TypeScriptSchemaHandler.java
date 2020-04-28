package io.swagger.codegen.v3.generators.typescript;

import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.SchemaHandler;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;

public class TypeScriptSchemaHandler extends SchemaHandler {

    private AbstractTypeScriptClientCodegen codegenConfig;

    public TypeScriptSchemaHandler(DefaultCodegenConfig codegenConfig) {
        super(codegenConfig);
        this.codegenConfig = (AbstractTypeScriptClientCodegen) codegenConfig;
    }

    public void processComposedSchemas(CodegenModel codegenModel, Schema schema, Map<String, CodegenModel> allModels) {
        if (!(schema instanceof ComposedSchema)) {
            return;
        }
        final ComposedSchema composedSchema = (ComposedSchema) schema;
        final boolean isAlias = composedSchema.getOneOf() != null && !composedSchema.getOneOf().isEmpty()
            || composedSchema.getAnyOf() != null && !composedSchema.getAnyOf().isEmpty();

        if (isAlias) {
            codegenModel.getVendorExtensions().put(CodegenConstants.IS_ALIAS_EXT_NAME, Boolean.TRUE);
            codegenModel.dataType = this.codegenConfig.getSchemaType(schema);
            this.codegenConfig.addImport(codegenModel, codegenModel.dataType);
        }
    }
}
