package io.swagger.codegen.v3.generators.html;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenModelFactory;
import io.swagger.codegen.v3.CodegenModelType;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.SchemaHandler;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.util.Json;
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

    public void processComposedSchemas(CodegenModel codegenModel, Schema schema, Map<String, CodegenModel> allModels) {
        if(!(schema instanceof ComposedSchema)) {
            super.processComposedSchemas(codegenModel, schema, allModels);
            return;
        }
        final ComposedSchema composedSchema = (ComposedSchema) schema;
        if (composedSchema.getAllOf() == null || composedSchema.getAllOf().isEmpty()) {
            super.processComposedSchemas(codegenModel, composedSchema, allModels);
            return;
        }
        final List<CodegenProperty> codegenProperties = codegenModel.vars;
        if (codegenProperties == null || codegenProperties.isEmpty()) {
            return;
        }
        for (CodegenProperty codegenProperty : codegenProperties) {
            if (codegenProperty.getIsListContainer()) {
                Schema schemaProperty = OpenAPIUtil.getPropertyFromAllOfSchema(codegenProperty.baseName, composedSchema.getAllOf(), this.codegenConfig.getOpenAPI());
                if (schemaProperty == null || !(schemaProperty instanceof ArraySchema)) {
                    continue;
                }
                this.processArrayItemSchema(StringUtils.EMPTY, codegenProperty, (ArraySchema) schemaProperty, allModels);
                break;
            }
        }
    }

    @Override
    protected CodegenModel processComposedSchema(CodegenModel codegenModel, ComposedSchema composedSchema, Map<String, CodegenModel> allModels) {
        List<Schema> schemas = composedSchema.getOneOf();
        CodegenModel composedModel = this.createComposedModel(ONE_OF_PREFFIX + codegenModel.getName(), schemas);
        if (composedModel == null) {
            schemas = composedSchema.getAnyOf();
            composedModel = this.createComposedModel(ANY_OF_PREFFIX + codegenModel.getName(), schemas);
            if (composedModel == null) {
                return null;
            }
        }
        this.addInterfaceModel(codegenModel, composedModel);
        this.addInterfaces(schemas, composedModel, allModels);

        if (composedModel.getName().startsWith(ONE_OF_PREFFIX)) {
            codegenModel.vendorExtensions.put("oneOf-model", composedModel);
        } else if (composedModel.getName().startsWith(ONE_OF_PREFFIX)) {
            codegenModel.vendorExtensions.put("anyOf-model", composedModel);
        }
        return null;
    }

    @Override
    protected CodegenModel processComposedSchema(String codegenModelName, CodegenProperty codegenProperty, ComposedSchema composedSchema, Map<String, CodegenModel> allModels) {
        List<Schema> schemas = composedSchema.getOneOf();
        CodegenModel composedModel = this.createComposedModel(ONE_OF_PREFFIX + codegenModelName, schemas);
        if (composedModel == null) {
            schemas = composedSchema.getAnyOf();
            composedModel = this.createComposedModel(ANY_OF_PREFFIX + codegenModelName, schemas);
            if (composedModel == null) {
                return null;
            }
        }
        if (composedModel.getName().startsWith(ONE_OF_PREFFIX)) {
            codegenProperty.vendorExtensions.put("oneOf-model", composedModel);

        } else if (composedModel.getName().startsWith(ANY_OF_PREFFIX)) {
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
        return null;
    }

    @Override
    protected CodegenModel processArrayItemSchema(CodegenModel codegenModel, ArraySchema arraySchema, Map<String, CodegenModel> allModels) {
        final Schema itemsSchema = arraySchema.getItems();
        if (itemsSchema instanceof ComposedSchema) {
            this.processComposedSchema(codegenModel, (ComposedSchema) itemsSchema, allModels);
        }
        return null;
    }

    @Override
    protected CodegenModel processArrayItemSchema(String codegenModelName, CodegenProperty codegenProperty, ArraySchema arraySchema, Map<String, CodegenModel> allModels) {
        final Schema itemsSchema = arraySchema.getItems();
        if (itemsSchema instanceof ComposedSchema) {
            this.processComposedSchema(codegenModelName, codegenProperty.items, (ComposedSchema) itemsSchema, allModels);
        }
        return null;
    }
}
