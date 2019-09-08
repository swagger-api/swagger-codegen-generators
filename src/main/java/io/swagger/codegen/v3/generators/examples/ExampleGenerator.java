package io.swagger.codegen.v3.generators.examples;

import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import io.swagger.v3.core.util.Json;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ExampleGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ExampleGenerator.class);

    // TODO: move constants to more appropriate location
    private static final String MIME_TYPE_JSON = "application/json";
    private static final String MIME_TYPE_XML = "application/xml";

    private static final String EXAMPLE = "example";
    private static final String CONTENT_TYPE = "contentType";
    private static final String OUTPUT = "output";
    private static final String NONE = "none";
    private static final String URL = "url";
    private static final String URI = "uri";

    protected Map<String, Schema> examples;
    private Random random;
    private OpenAPI openAPI;

    public ExampleGenerator(OpenAPI openAPI) {
        //this.examples = examples;
        this.openAPI = openAPI;
        // use a fixed seed to make the "random" numbers reproducible.
        this.random = new Random("ExampleGenerator".hashCode());
    }

    public List<Map<String, String>> generateFromSchema(Schema schema, Set<String> producesInfo) {
        return null;
    }

    public List<Map<String, String>> generate(Map<String, Object> examples, List<String> mediaTypes, Schema schema) {
        List<Map<String, String>> output = new ArrayList<>();
        Set<String> processedModels = new HashSet<>();
        if (examples == null) {
            if (mediaTypes == null) {
                // assume application/json for this
                mediaTypes = Collections.singletonList(MIME_TYPE_JSON); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.
            }

            if (schema == null) {
                return output;
            }

            for (String mediaType : mediaTypes) {
                Map<String, String> kv = new HashMap<>();
                kv.put(CONTENT_TYPE, mediaType);

                if (mediaType.startsWith(MIME_TYPE_JSON)) {
                    String example = Json.pretty(resolveSchemaToExample("", mediaType, schema, processedModels));
                    if (example != null) {
                        kv.put(EXAMPLE, example);
                        output.add(kv);
                    }
                } else if (mediaType.startsWith(MIME_TYPE_XML)) {
                    /** todo: add xml generator
                    String example = new XmlExampleGenerator(this.examples).toXml(property);
                    if (example != null) {
                        kv.put(EXAMPLE, example);
                        output.add(kv);
                    }
                     */
                }
            }
        } else {
            for (Map.Entry<String, Object> entry : examples.entrySet()) {
                final Map<String, String> kv = new HashMap<>();
                kv.put(CONTENT_TYPE, entry.getKey());
                kv.put(EXAMPLE, Json.pretty(entry.getValue()));
                output.add(kv);
            }
        }
        if (output.size() == 0) {
            Map<String, String> kv = new HashMap<>();
            kv.put(OUTPUT, NONE);
            output.add(kv);
        }
        return output;
    }

    public List<Map<String, String>> generate(Map<String, Object> examples, List<String> mediaTypes, String modelName) {
        List<Map<String, String>> output = new ArrayList<>();
        Set<String> processedModels = new HashSet<>();
        if (examples == null) {
            if (mediaTypes == null) {
                // assume application/json for this
                mediaTypes = Collections.singletonList(MIME_TYPE_JSON); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.
            }
            for (String mediaType : mediaTypes) {
                Map<String, String> kv = new HashMap<>();
                kv.put(CONTENT_TYPE, mediaType);
                if (modelName != null && mediaType.startsWith(MIME_TYPE_JSON)) {
                    final Schema schema = this.examples.get(modelName);
                    if (schema != null) {
                        String example = Json.pretty(resolveModelToExample(modelName, mediaType, schema, processedModels));

                        if (example != null) {
                            kv.put(EXAMPLE, example);
                            output.add(kv);
                        }
                    }
                } else if (modelName != null && mediaType.startsWith(MIME_TYPE_XML)) {
                    final Schema schema = this.examples.get(modelName);
                    /** todo: add xml example generator
                     * String example = new XmlExampleGenerator(this.examples).toXml(schema, 0, Collections.<String>emptySet());
                    if (example != null) {
                        kv.put(EXAMPLE, example);
                        output.add(kv);
                    }
                    */
                }
            }
        } else {
            for (Map.Entry<String, Object> entry : examples.entrySet()) {
                final Map<String, String> kv = new HashMap<>();
                kv.put(CONTENT_TYPE, entry.getKey());
                kv.put(EXAMPLE, Json.pretty(entry.getValue()));
                output.add(kv);
            }
        }
        if (output.size() == 0) {
            Map<String, String> kv = new HashMap<>();
            kv.put(OUTPUT, NONE);
            output.add(kv);
        }
        return output;
    }

    private Object resolveSchemaToExample(String propertyName, String mediaType, Schema schema, Set<String> processedModels) {
        if (processedModels.contains(schema.get$ref())) {
            return schema.getExample();
        }
        if (StringUtils.isNotBlank(schema.get$ref())) {
            processedModels.add(schema.get$ref());
        }
        if (schema.getExample() != null) {
            logger.debug("Example set in swagger spec, returning example: '{}'", schema.getExample().toString());
            return schema.getExample();
        } else if (schema instanceof StringSchema) {
            logger.debug("String property");
            String defaultValue = ((StringSchema) schema).getDefault();
            if (defaultValue != null && !defaultValue.isEmpty()) {
                logger.debug("Default value found: '{}'", defaultValue);
                return defaultValue;
            }
            List<String> enumValues = ((StringSchema) schema).getEnum();
            if (enumValues != null && !enumValues.isEmpty()) {
                logger.debug("Enum value found: '{}'", enumValues.get(0));
                return enumValues.get(0);
            }
            String format = schema.getFormat();
            if (format != null && (URI.equals(format) || URL.equals(format))) {
                logger.debug("URI or URL format, without default or enum, generating random one.");
                return "http://example.com/aeiou";
            }
            logger.debug("No values found, using property name " + propertyName + " as example");
            return propertyName;
        } else if (schema instanceof BooleanSchema) {
            Object defaultValue = schema.getDefault();
            if (defaultValue != null) {
                return defaultValue;
            }
            return Boolean.TRUE;
        } else if (schema instanceof ArraySchema) {
            Schema innerType = ((ArraySchema) schema).getItems();
            if (innerType != null) {
                int arrayLength = schema.getMaxItems() != null ? schema.getMaxItems() : 2;
                Object[] objectProperties = new Object[arrayLength];
                Object objProperty = resolveSchemaToExample(propertyName, mediaType, innerType, processedModels);
                for(int i=0; i < arrayLength; i++) {
                    objectProperties[i] = objProperty;
                }
                return objectProperties;
            }
        } else if (schema instanceof DateSchema) {
            return "2000-01-23";
        } else if (schema instanceof DateTimeSchema) {
            return "2000-01-23T04:56:07.000+00:00";
        } else if (schema instanceof NumberSchema) {
            Double min = schema.getMinimum() == null ? null : schema.getMinimum().doubleValue();
            Double max = schema.getMaximum() == null ? null : schema.getMaximum().doubleValue();
            if(SchemaTypeUtil.FLOAT_FORMAT.equals(schema.getFormat())) {
                return (float) randomNumber(min, max);
            }
            return randomNumber(min, max);
        } else if (schema instanceof FileSchema) {
            return "";  // TODO
        } else if (schema instanceof IntegerSchema) {
            Double min = schema.getMinimum() == null ? null : schema.getMinimum().doubleValue();
            Double max = schema.getMaximum() == null ? null : schema.getMaximum().doubleValue();
            if(SchemaTypeUtil.INTEGER32_FORMAT.equals(schema.getFormat())) {
                return (long) randomNumber(min, max);
            }
            return (int) randomNumber(min, max);
        } else if (schema instanceof MapSchema && schema.getAdditionalProperties() != null && schema.getAdditionalProperties() instanceof Schema) {
            Map<String, Object> mp = new HashMap<String, Object>();
            if (schema.getName() != null) {
                mp.put(schema.getName(),
                        resolveSchemaToExample(propertyName, mediaType, (Schema) schema.getAdditionalProperties(), processedModels));
            } else {
                mp.put("key",
                        resolveSchemaToExample(propertyName, mediaType, (Schema) schema.getAdditionalProperties(), processedModels));
            }
            return mp;
        } else if (!StringUtils.isEmpty(schema.get$ref())) { // model
            String simpleName = OpenAPIUtil.getSimpleRef(schema.get$ref());
            Schema model = null;
            if (openAPI != null && openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
                model = openAPI.getComponents().getSchemas().get(simpleName);
            }

            if (model == null) { // couldn't find the model/schema
                return "{}";
            }
            return resolveSchemaToExample(propertyName, mediaType, model, processedModels);
        } else if (schema instanceof ObjectSchema || schema.getProperties() != null) {
            Map<String, Object> values = new HashMap<>();
            if (schema.getProperties() != null) {
                logger.debug("Creating example from model values");
                for (Object propName : schema.getProperties().keySet()) {
                    Schema schemaProperty = (Schema) schema.getProperties().get(propName.toString());
                    values.put(propName.toString(), resolveSchemaToExample(propName.toString(), mediaType, schemaProperty, processedModels));
                }
                schema.setExample(values);
            }
            return values;
        } else if (schema instanceof UUIDSchema) {
            return "046b6c7f-0b8a-43b9-b35d-6489e6daee91";
        }
        return "";
    }

    private double randomNumber(Double min, Double max) {
        if (min != null && max != null) {
            double range = max - min;
            return random.nextDouble() * range + min;
        } else if (min != null) {
            return random.nextDouble() + min;
        } else if (max != null) {
            return random.nextDouble() * max;
        } else {
            return random.nextDouble() * 10;
        }
    }

    private Object resolveModelToExample(String name, String mediaType, Schema schema, Set<String> processedModels) {
        if (processedModels.contains(schema.get$ref())) {
            return schema.getExample();
        }
        if (StringUtils.isNotBlank(schema.get$ref())) {
            processedModels.add(schema.get$ref());
        }
        Map<String, Object> values = new HashMap<>();

        logger.debug("Resolving model '{}' to example", name);

        if (schema.getExample() != null) {
            logger.debug("Using example from spec: {}", schema.getExample());
            return schema.getExample();
        } else if (schema.getProperties() != null) {
            logger.debug("Creating example from model values");
            for (Object propertyName : schema.getProperties().keySet()) {
                schema.getProperties().get(propertyName.toString());
                values.put(propertyName.toString(), resolveSchemaToExample(propertyName.toString(), mediaType, schema, processedModels));
            }
            schema.setExample(values);
        }
        return values;
    }
}

