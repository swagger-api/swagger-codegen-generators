package io.swagger.codegen.v3.generators;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenModelFactory;
import io.swagger.codegen.v3.CodegenModelType;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SchemaHandler {

    private DefaultCodegenConfig codegenConfig;

    public SchemaHandler(DefaultCodegenConfig codegenConfig) {
        this.codegenConfig = codegenConfig;
    }


    /**
     * creates a codegen model object based on a composed schema property.
     * @param composedProperty
     * @param codegenProperty
     */
    public void createCodegenModel(ComposedSchema composedProperty, CodegenProperty codegenProperty) {
        final List<Schema> oneOf = composedProperty.getOneOf();
        final List<Schema> anyOf = composedProperty.getAnyOf();

        if (oneOf != null && !oneOf.isEmpty()) {
            if (!hasNonObjectSchema(oneOf)) {
                final CodegenModel oneOfModel = createFromOneOfSchemas(oneOf);
                codegenProperty.vendorExtensions.put("oneOf-model", oneOfModel);
            }
        }
        if (anyOf != null && !anyOf.isEmpty()) {
            if (!hasNonObjectSchema(anyOf)) {
                final CodegenModel anyOfModel = createFromOneOfSchemas(anyOf);
                codegenProperty.vendorExtensions.put("anyOf-model", anyOfModel);
            }
        }

    }

    public void configureComposedModelFromSchemaItems(CodegenModel codegenModel, ComposedSchema items) {
        List<Schema> oneOfList = items.getOneOf();
        if (oneOfList != null && !oneOfList.isEmpty()){
            String name = "OneOf" + codegenModel.name + "Items";
            final CodegenModel oneOfModel = createComposedModel(name);
            // setting name to be used as instance type on composed model.
            items.addExtension("x-model-name", codegenConfig.toModelName(name));

            final List<String> modelNames = new ArrayList<>();
            for (Schema interfaceSchema : oneOfList) {
                if (StringUtils.isNotBlank(interfaceSchema.get$ref())) {
                    String schemaName = OpenAPIUtil.getSimpleRef(interfaceSchema.get$ref());
                    modelNames.add(codegenConfig.toModelName(schemaName));
                }
            }
            oneOfModel.vendorExtensions.put("x-model-names", modelNames);
            if (!modelNames.isEmpty()) {
                codegenModel.vendorExtensions.put("oneOf-model", oneOfModel);
            }
        }
        List<Schema> anyOfList = items.getAnyOf();
        if (anyOfList != null && !anyOfList.isEmpty()){
            String name = "AnyOf" + codegenModel.name + "Items";
            final CodegenModel anyOfModel = createComposedModel(name);
            items.addExtension("x-model-name", codegenConfig.toModelName(name));

            final List<String> modelNames = new ArrayList<>();
            for (Schema interfaceSchema : anyOfList) {
                if (StringUtils.isNotBlank(interfaceSchema.get$ref())) {
                    String schemaName = OpenAPIUtil.getSimpleRef(interfaceSchema.get$ref());
                    modelNames.add(codegenConfig.toModelName(schemaName));
                }
            }
            anyOfModel.vendorExtensions.put("x-model-names", modelNames);
            if (!modelNames.isEmpty()) {
                codegenModel.vendorExtensions.put("anyOf-model", anyOfModel);
            }
        }
    }

    public void configureOneOfModel(CodegenModel codegenModel, List<Schema> oneOf) {
        final CodegenModel oneOfModel = createComposedModel("OneOf" + codegenModel.name);
        final List<String> modelNames = new ArrayList<>();
        for (Schema interfaceSchema : oneOf) {
            if (StringUtils.isNotBlank(interfaceSchema.get$ref())) {
                String schemaName = OpenAPIUtil.getSimpleRef(interfaceSchema.get$ref());
                modelNames.add(codegenConfig.toModelName(schemaName));
            }
        }
        oneOfModel.vendorExtensions.put("x-model-names", modelNames);
        if (!modelNames.isEmpty()) {
            codegenModel.vendorExtensions.put("oneOf-model", oneOfModel);
            if (codegenModel.interfaceModels == null) {
                codegenModel.interfaceModels = new ArrayList<>();
            }
            codegenModel.interfaceModels.add(oneOfModel);
        }
    }

    public void configureAnyOfModel(CodegenModel codegenModel, List<Schema> anyOf) {
        final CodegenModel anyOfModel = createComposedModel("AnyOf" + codegenModel.name);
        final List<String> modelNames = new ArrayList<>();
        for (Schema interfaceSchema : anyOf) {
            if (StringUtils.isNotBlank(interfaceSchema.get$ref())) {
                String schemaName = OpenAPIUtil.getSimpleRef(interfaceSchema.get$ref());
                modelNames.add(codegenConfig.toModelName(schemaName));
            }
        }
        anyOfModel.vendorExtensions.put("x-model-names", modelNames);
        if (!modelNames.isEmpty()) {
            codegenModel.vendorExtensions.put("anyOf-model", anyOfModel);
            if (codegenModel.interfaceModels == null) {
                codegenModel.interfaceModels = new ArrayList<>();
            }
            codegenModel.interfaceModels.add(anyOfModel);
        }
    }

    public void configureOneOfModelFromProperty(CodegenProperty codegenProperty, CodegenModel codegenModel) {
        String name = "OneOf" + codegenConfig.toModelName(codegenModel.name);
        name += codegenConfig.toModelName(codegenProperty.name);
        CodegenModel oneOfModel = (CodegenModel) codegenProperty.vendorExtensions.get("oneOf-model");
        this.configureModel(oneOfModel, name);
        codegenProperty.vendorExtensions.remove("oneOf-model");

        codegenProperty.datatype = name;
        codegenProperty.datatypeWithEnum = name;
        codegenProperty.baseType = name;
        codegenProperty.complexType = name;

        codegenModel.vendorExtensions.put("oneOf-model", oneOfModel);
    }

    public void configureAnyOfModelFromProperty(CodegenProperty codegenProperty, CodegenModel codegenModel) {
        String name = "AnyOf" + codegenConfig.toModelName(codegenModel.name);
        name += codegenConfig.toModelName(codegenProperty.name);
        CodegenModel anyOfModel = (CodegenModel) codegenProperty.vendorExtensions.get("anyOf-model");
        this.configureModel(anyOfModel, name);
        codegenProperty.vendorExtensions.remove("anyOf-model");

        codegenProperty.datatype = name;
        codegenProperty.datatypeWithEnum = name;
        codegenProperty.baseType = name;
        codegenProperty.complexType = name;

        codegenModel.vendorExtensions.put("anyOf-model", anyOfModel);
    }

    private CodegenModel createFromOneOfSchemas(List<Schema> schemas) {
        final CodegenModel codegenModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        final List<String> modelNames = new ArrayList<>();

        for (Schema interfaceSchema : schemas) {
            if (StringUtils.isNotBlank(interfaceSchema.get$ref())) {
                String schemaName = OpenAPIUtil.getSimpleRef(interfaceSchema.get$ref());
                modelNames.add(codegenConfig.toModelName(schemaName));
            }
        }
        codegenModel.vendorExtensions.put("x-model-names", modelNames);
        return codegenModel;
    }

    private CodegenModel createComposedModel(String name) {
        final CodegenModel composedModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        this.configureModel(composedModel, name);
        return composedModel;
    }

    private void configureModel(CodegenModel codegenModel, String name) {
        codegenModel.name = name;
        codegenModel.classname = codegenConfig.toModelName(name);
        codegenModel.classVarName = codegenConfig.toVarName(name);
        codegenModel.classFilename = codegenConfig.toModelFilename(name);
        codegenModel.vendorExtensions.put("x-is-composed-model", Boolean.TRUE);
    }

    private boolean hasNonObjectSchema(List<Schema> schemas) {
        for  (Schema schema : schemas) {
            if (!codegenConfig.isObjectSchema(schema)) {
                return true;
            }
        }
        return false;
    }
}
