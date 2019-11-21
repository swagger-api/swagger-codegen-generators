package io.swagger.codegen.v3.generators.typescript;

import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.SchemaHandler;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

public class TypeScriptSchemaHandler extends SchemaHandler {

    private AbstractTypeScriptClientCodegen codegenConfig;

    public TypeScriptSchemaHandler(DefaultCodegenConfig codegenConfig) {
        super(codegenConfig);
        this.codegenConfig = (AbstractTypeScriptClientCodegen) codegenConfig;
    }

    public void configureOneOfModel(CodegenModel codegenModel, List<Schema> oneOf) {
        codegenModel.getVendorExtensions().put(CodegenConstants.IS_ALIAS_EXT_NAME, Boolean.TRUE);
        this.codegenConfig.addImport(codegenModel, codegenModel.dataType);
    }

    public void configureAnyOfModel(CodegenModel codegenModel, List<Schema> anyOf) {
        codegenModel.getVendorExtensions().put(CodegenConstants.IS_ALIAS_EXT_NAME, Boolean.TRUE);
        this.codegenConfig.addImport(codegenModel, codegenModel.dataType);
    }
}
