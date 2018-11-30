package io.swagger.codegen.v3.generators.util;


import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;

public class OpenAPIUtil {

    private OpenAPI openAPI;

    public OpenAPIUtil(OpenAPI openAPI) {
        this.openAPI = openAPI;
    }

    public void addPropertiesFromRef(Schema refSchema, CodegenProperty codegenProperty) {
        final Map<String, Schema> allSchemas = this.openAPI.getComponents().getSchemas();
        if (allSchemas == null || allSchemas.isEmpty()) {
            return;
        }
        final Schema schema = allSchemas.get(getSimpleRef(refSchema.get$ref()));
        if (schema == null) {
            return;
        }
        codegenProperty.pattern = schema.getPattern();
        codegenProperty.minLength = schema.getMinLength();
        codegenProperty.maxLength = schema.getMaxLength();
    }

    public static String getSimpleRef(String ref) {
        if (ref.startsWith("#/components/")) {
            ref = ref.substring(ref.lastIndexOf("/") + 1);
        }
        return ref;
    }
}
