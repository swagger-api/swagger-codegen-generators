package io.swagger.codegen.v3.generators;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;

public class SchemaHandler {

    private CodegenConfig codegenConfig;

    public SchemaHandler(CodegenConfig codegenConfig) {
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
            final CodegenModel oneOfModel = createFromOneOfSchemas(oneOf);
            codegenProperty.vendorExtensions.put("anyOf-model", oneOfModel);
        }

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
