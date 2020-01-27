package io.swagger.codegen.v3.generators.html;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenModelFactory;
import io.swagger.codegen.v3.CodegenModelType;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.SchemaHandler;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class HtmlSchemaHandler extends SchemaHandler {

    public HtmlSchemaHandler(DefaultCodegenConfig codegenConfig) {
        super(codegenConfig);
    }


    @Override
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

    @Override
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

    @Override
    public void configureOneOfModel(CodegenModel codegenModel, List<Schema> oneOf) {
        // no ops for html generator
    }

    @Override
    public void configureAnyOfModel(CodegenModel codegenModel, List<Schema> anyOf) {
        // no ops for html generator
    }

    @Override
    public void configureOneOfModelFromProperty(CodegenProperty codegenProperty, CodegenModel codegenModel) {
        // no ops for html generator
    }

    @Override
    public void configureAnyOfModelFromProperty(CodegenProperty codegenProperty, CodegenModel codegenModel) {
        // no ops for html generator
    }

    @Override
    protected CodegenModel createFromOneOfSchemas(List<Schema> schemas) {
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

    protected CodegenModel createComposedModel(String name) {
        final CodegenModel composedModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        this.configureModel(composedModel, name);
        return composedModel;
    }

    @Override
    protected void configureModel(CodegenModel codegenModel, String name) {
        codegenModel.name = name;
        codegenModel.classname = codegenConfig.toModelName(name);
        codegenModel.classVarName = codegenConfig.toVarName(name);
        codegenModel.classFilename = codegenConfig.toModelFilename(name);
        codegenModel.vendorExtensions.put("x-is-composed-model", Boolean.TRUE);
    }

    @Override
    protected boolean hasNonObjectSchema(List<Schema> schemas) {
        for  (Schema schema : schemas) {
            if (!codegenConfig.isObjectSchema(schema)) {
                return true;
            }
        }
        return false;
    }
}
