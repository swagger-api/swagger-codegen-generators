package io.swagger.codegen.v3.generators;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

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
             final CodegenModel oneOfModel = createFromOneOfSchemas(oneOf);
            codegenProperty.vendorExtensions.put("oneOf-model", oneOfModel);
        }
        if (anyOf != null && !anyOf.isEmpty()) {
            final CodegenModel anyOfModel = createFromOneOfSchemas(anyOf);
            codegenProperty.vendorExtensions.put("anyOf-model", anyOfModel);
        }

    }

    public void configureOneOfModel(CodegenModel codegenModel, List<Schema> oneOf) {
        String oneOfModelName = "OneOf" + codegenModel.name;
        final CodegenModel oneOfModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        oneOfModel.name = oneOfModelName;
        oneOfModel.classname = codegenConfig.toModelName(oneOfModelName);
        oneOfModel.classVarName = codegenConfig.toVarName(oneOfModelName);
        oneOfModel.classFilename = codegenConfig.toModelFilename(oneOfModelName);
        oneOfModel.vendorExtensions.put("x-is-composed-model", Boolean.TRUE);
        oneOfModel.modelJson = Json.pretty(null);

        final List<String> modelNames = new ArrayList<>();

        for (Schema interfaceSchema : oneOf) {
            String schemaName = OpenAPIUtil.getSimpleRef(interfaceSchema.get$ref());
            modelNames.add(codegenConfig.toModelName(schemaName));
        }
        oneOfModel.vendorExtensions.put("x-model-names", modelNames);
        codegenModel.vendorExtensions.put("oneOf-model", oneOfModel);
        if (codegenModel.interfaceModels == null) {
            codegenModel.interfaceModels = new ArrayList<>();
        }
        codegenModel.interfaceModels.add(oneOfModel);
    }

    public void configureAnyOfModel(CodegenModel codegenModel, List<Schema> oneOf) {
        String oneOfModelName = "AnyOf" + codegenModel.name;
        final CodegenModel oneOfModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        oneOfModel.name = oneOfModelName;
        oneOfModel.classname = codegenConfig.toModelName(oneOfModelName);
        oneOfModel.classVarName = codegenConfig.toVarName(oneOfModelName);
        oneOfModel.classFilename = codegenConfig.toModelFilename(oneOfModelName);
        oneOfModel.vendorExtensions.put("x-is-composed-model", Boolean.TRUE);
        oneOfModel.modelJson = Json.pretty(null);

        final List<String> modelNames = new ArrayList<>();

        for (Schema interfaceSchema : oneOf) {
            String schemaName = OpenAPIUtil.getSimpleRef(interfaceSchema.get$ref());
            modelNames.add(codegenConfig.toModelName(schemaName));
        }
        oneOfModel.vendorExtensions.put("x-model-names", modelNames);
        codegenModel.vendorExtensions.put("anyOf-model", oneOfModel);
        if (codegenModel.interfaceModels == null) {
            codegenModel.interfaceModels = new ArrayList<>();
        }
        codegenModel.interfaceModels.add(oneOfModel);
    }

    public void configureOneOfModelFromProperty(CodegenProperty codegenProperty, CodegenModel codegenModel) {
        String name = "OneOf" + codegenConfig.toModelName(codegenModel.name);
        name += codegenConfig.toModelName(codegenProperty.name);
        CodegenModel oneOfModel = (CodegenModel) codegenProperty.vendorExtensions.get("oneOf-model");
        oneOfModel.name = name;
        oneOfModel.classname = codegenConfig.toModelName(name);
        oneOfModel.classVarName = codegenConfig.toVarName(name);
        oneOfModel.classFilename = codegenConfig.toModelFilename(name);
        oneOfModel.vendorExtensions.put("x-is-composed-model", Boolean.TRUE);
        codegenProperty.vendorExtensions.remove("oneOf-model");

        codegenProperty.datatype = name;
        codegenProperty.datatypeWithEnum = name;
        codegenProperty.baseType = name;

        codegenModel.vendorExtensions.put("oneOf-model", oneOfModel);
    }

    public void configureAnyOfModelFromProperty(CodegenProperty codegenProperty, CodegenModel codegenModel) {
        String name = "AnyOf" + codegenConfig.toModelName(codegenModel.name);
        name += codegenConfig.toModelName(codegenProperty.name);
        CodegenModel anyOfModel = (CodegenModel) codegenProperty.vendorExtensions.get("anyOf-model");
        anyOfModel.name = name;
        anyOfModel.classname = codegenConfig.toModelName(name);
        anyOfModel.classVarName = codegenConfig.toVarName(name);
        anyOfModel.classFilename = codegenConfig.toModelFilename(name);
        anyOfModel.vendorExtensions.put("x-is-composed-model", Boolean.TRUE);
        codegenProperty.vendorExtensions.remove("anyOf-model");

        codegenProperty.datatype = name;
        codegenProperty.datatypeWithEnum = name;
        codegenProperty.baseType = name;

        codegenModel.vendorExtensions.put("anyOf-model", anyOfModel);
    }

    public void configureAnyOfModelFromProperty() {

    }

    private CodegenModel createFromOneOfSchemas(List<Schema> schemas) {
        final CodegenModel codegenModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        final List<String> modelNames = new ArrayList<>();

        for (Schema interfaceSchema : schemas) {
            String schemaName = OpenAPIUtil.getSimpleRef(interfaceSchema.get$ref());
            modelNames.add(codegenConfig.toModelName(schemaName));
        }
        codegenModel.vendorExtensions.put("x-model-names", modelNames);
        return codegenModel;
    }
}
