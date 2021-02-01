package io.swagger.codegen.v3.generators.util;


import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.CodegenConstants.HAS_VALIDATION_EXT_NAME;

public class OpenAPIUtil {

    public static void addPropertiesFromRef(OpenAPI openAPI, Schema refSchema, CodegenProperty codegenProperty) {
        final Map<String, Schema> allSchemas = openAPI.getComponents().getSchemas();
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
        if (codegenProperty.pattern != null || codegenProperty.minLength != null || codegenProperty.maxLength != null) {
            codegenProperty.getVendorExtensions().put(HAS_VALIDATION_EXT_NAME, Boolean.TRUE);
        }
    }

    public static String getSimpleRef(String ref) {
        if (ref.startsWith("#/components/")) {
            ref = ref.substring(ref.lastIndexOf("/") + 1);
        }
        return ref;
    }

    public static Schema getSchemaFromName(String name, OpenAPI openAPI) {
        if (openAPI == null) {
            return null;
        }
        if (openAPI.getComponents() == null) {
            return null;
        }
        final Map<String, Schema> mapSchema = openAPI.getComponents().getSchemas();
        if (mapSchema == null || mapSchema.isEmpty()) {
            return null;
        }
        return mapSchema.get(name);
    }

    public static Schema getSchemaFromRefSchema(Schema refSchema, OpenAPI openAPI) {
        if (StringUtils.isBlank(refSchema.get$ref())) {
            return null;
        }
        final String name = getSimpleRef(refSchema.get$ref());
        return getSchemaFromName(name, openAPI);
    }

    public static Schema getPropertyFromAllOfSchema(String propertyName, List<Schema> schemas, OpenAPI openAPI) {
        for (Schema schema : schemas) {
            if (StringUtils.isNotBlank(schema.get$ref())) {
                schema = getSchemaFromRefSchema(schema, openAPI);
            }
            final Map<String, Schema> schemaProperties = schema.getProperties();
            if (schemaProperties == null) {
                continue;
            }
            if (schemaProperties.containsKey(propertyName)) {
                return schemaProperties.get(propertyName);
            }
        }
        return null;
    }
}
