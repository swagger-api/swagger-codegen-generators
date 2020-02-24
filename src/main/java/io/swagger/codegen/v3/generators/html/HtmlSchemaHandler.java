package io.swagger.codegen.v3.generators.html;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenModelFactory;
import io.swagger.codegen.v3.CodegenModelType;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.SchemaHandler;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HtmlSchemaHandler extends SchemaHandler {

    public HtmlSchemaHandler(DefaultCodegenConfig codegenConfig) {
        super(codegenConfig);
    }

    protected void processComposedSchema(CodegenModel codegenModel, ComposedSchema composedSchema, Map<String, CodegenModel> allModels) {
        List<Schema> schemas = composedSchema.getOneOf();
        CodegenModel composedModel = this.createComposedModel(ONE_OF_PREFFIX + codegenModel.getName(), schemas);
        if (composedModel == null) {
            schemas = composedSchema.getAnyOf();
            composedModel = this.createComposedModel(ANY_OF_PREFFIX + codegenModel.getName(), schemas);
            if (composedModel == null) {
                return;
            }
        }
        this.addInterfaceModel(codegenModel, composedModel);
        this.addInterfaces(schemas, composedModel, allModels);

        if (composedModel.getName().startsWith(ONE_OF_PREFFIX)) {
            codegenModel.vendorExtensions.put("oneOf-model", composedModel);
        } else if (composedModel.getName().startsWith(ONE_OF_PREFFIX)) {
            codegenModel.vendorExtensions.put("anyOf-model", composedModel);
        }
    }

    protected void processComposedSchema(String codegenModelName, CodegenProperty codegenProperty, ComposedSchema composedSchema, Map<String, CodegenModel> allModels) {
        List<Schema> schemas = composedSchema.getOneOf();
        CodegenModel composedModel = this.createComposedModel(ONE_OF_PREFFIX + codegenModelName, schemas);
        if (composedModel == null) {
            schemas = composedSchema.getAnyOf();
            composedModel = this.createComposedModel(ANY_OF_PREFFIX + codegenModelName, schemas);
            if (composedModel == null) {
                return;
            }
        }
        if (composedModel.getName().startsWith(ONE_OF_PREFFIX)) {
            codegenProperty.vendorExtensions.put("oneOf-model", composedModel);

        } else if (composedModel.getName().startsWith(ONE_OF_PREFFIX)) {
            codegenProperty.vendorExtensions.put("anyOf-model", composedModel);
        }
        final List<String> modelNames = new ArrayList<>();
        for (Schema interfaceSchema : schemas) {
            if (StringUtils.isNotBlank(interfaceSchema.get$ref())) {
                String schemaName = OpenAPIUtil.getSimpleRef(interfaceSchema.get$ref());
                modelNames.add(codegenConfig.toModelName(schemaName));
            }
        }
        composedModel.vendorExtensions.put("x-model-names", modelNames);
    }
}
