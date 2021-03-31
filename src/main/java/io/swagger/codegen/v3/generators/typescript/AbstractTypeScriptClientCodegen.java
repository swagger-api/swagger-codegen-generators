package io.swagger.codegen.v3.generators.typescript;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.ISchemaHandler;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public abstract class AbstractTypeScriptClientCodegen extends DefaultCodegenConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTypeScriptClientCodegen.class);

    private static final String UNDEFINED_VALUE = "undefined";

    protected String modelPropertyNaming= "camelCase";
    protected Boolean supportsES6 = true;
    protected HashSet<String> languageGenericTypes;

    public AbstractTypeScriptClientCodegen() {
        super();

        // clear import mapping (from default generator) as TS does not use it
        // at the moment
        importMapping.clear();

        supportsInheritance = true;
        setReservedWordsLowerCase(Arrays.asList(
                // local variable names used in API methods (endpoints)
                "varLocalPath", "queryParameters", "headerParams", "formParams", "useFormData", "varLocalDeferred",
                "requestOptions",
                // Typescript reserved words
                "abstract", "await", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "debugger", "default", "delete", "do", "double", "else", "enum", "export", "extends", "false", "final", "finally", "float", "for", "function", "goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this", "throw", "transient", "true", "try", "typeof", "var", "void", "volatile", "while", "with", "yield"));

        languageSpecificPrimitives = new HashSet<>(Arrays.asList(
                "string",
                "String",
                "boolean",
                "Boolean",
                "Double",
                "Integer",
                "Long",
                "Float",
                "Object",
                "Array",
                "Date",
                "number",
                "any",
                "File",
                "Error",
                "Map"
                ));

        languageGenericTypes = new HashSet<String>(Arrays.asList(
                "Array"
        ));

        instantiationTypes.put("array", "Array");

        typeMapping = new HashMap<String, String>();
        typeMapping.put("Array", "Array");
        typeMapping.put("array", "Array");
        typeMapping.put("List", "Array");
        typeMapping.put("boolean", "boolean");
        typeMapping.put("string", "string");
        typeMapping.put("int", "number");
        typeMapping.put("float", "number");
        typeMapping.put("number", "number");
        typeMapping.put("BigDecimal", "number");
        typeMapping.put("long", "number");
        typeMapping.put("short", "number");
        typeMapping.put("char", "string");
        typeMapping.put("double", "number");
        typeMapping.put("object", "any");
        typeMapping.put("integer", "number");
        typeMapping.put("Map", "any");
        typeMapping.put("date", "string");
        typeMapping.put("DateTime", "Date");
        //TODO binary should be mapped to byte array
        // mapped to String as a workaround
        typeMapping.put("binary", "string");
        typeMapping.put("ByteArray", "string");
        typeMapping.put("UUID", "string");
        typeMapping.put("File", "any");
        typeMapping.put("Error", "Error");

        cliOptions.add(new CliOption(CodegenConstants.MODEL_PROPERTY_NAMING, CodegenConstants.MODEL_PROPERTY_NAMING_DESC).defaultValue("camelCase"));
        cliOptions.add(new CliOption(CodegenConstants.SUPPORTS_ES6, CodegenConstants.SUPPORTS_ES6_DESC).defaultValue("false"));
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(CodegenConstants.MODEL_PROPERTY_NAMING)) {
            setModelPropertyNaming((String) additionalProperties.get(CodegenConstants.MODEL_PROPERTY_NAMING));
        }

        if (additionalProperties.containsKey(CodegenConstants.SUPPORTS_ES6)) {
            setSupportsES6(Boolean.valueOf(additionalProperties.get(CodegenConstants.SUPPORTS_ES6).toString()));
            additionalProperties.put("supportsES6", getSupportsES6());
        }
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String escapeReservedWord(String name) {
        if(this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + modelPackage().replace('.', File.separatorChar);
    }

    @Override
    public String toParamName(String name) {
        // should be the same as variable name
        return toVarName(name);
    }

    @Override
    public String toVarName(String name) {
        // sanitize name
        name = sanitizeName(name);

        if("_".equals(name)) {
            name = "_u";
        }

        // if it's all uppper case, do nothing
        if (name.matches("^[A-Z_]*$")) {
            return name;
        }

        name = getNameUsingModelPropertyNaming(name);

        // for reserved word or word starting with number, append _
        if (isReservedWord(name) || name.matches("^\\d.*")) {
            name = escapeReservedWord(name);
        }

        return name;
    }

    @Override
    public String toModelName(String name) {
        name = sanitizeName(name); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        if (!StringUtils.isEmpty(modelNamePrefix)) {
            name = modelNamePrefix + "_" + name;
        }

        if (!StringUtils.isEmpty(modelNameSuffix)) {
            name = name + "_" + modelNameSuffix;
        }

        // model name cannot use reserved keyword, e.g. return
        if (isReservedWord(name)) {
            String modelName = camelize("model_" + name);
            LOGGER.warn(name + " (reserved word) cannot be used as model name. Renamed to " + modelName);
            return modelName;
        }

        // model name starts with number
        if (name.matches("^\\d.*")) {
            String modelName = camelize("model_" + name); // e.g. 200Response => Model200Response (after camelize)
            LOGGER.warn(name + " (model name starts with number) cannot be used as model name. Renamed to " + modelName);
            return modelName;
        }

        if (languageSpecificPrimitives.contains(name)) {
            String modelName = camelize("model_" + name);
            LOGGER.warn(name + " (model name matches existing language type) cannot be used as a model name. Renamed to " + modelName);
            return modelName;
        }

        // camelize the model name
        // phone_number => PhoneNumber
        return camelize(name);
    }

    @Override
    public String toModelFilename(String name) {
        // should be the same as the model name
        return toModelName(name);
    }

    @Override
    public String getTypeDeclaration(Schema propertySchema) {
        if (propertySchema instanceof ArraySchema) {
            Schema inner = ((ArraySchema) propertySchema).getItems();
            return String.format("%s<%s>", getSchemaType(propertySchema), getTypeDeclaration(inner));
        } else if (propertySchema instanceof MapSchema   && hasSchemaProperties(propertySchema)) {
            Schema inner = (Schema) propertySchema.getAdditionalProperties();
            return String.format("{ [key, string]: %s;}", getTypeDeclaration(inner));
        } else if (propertySchema instanceof MapSchema && hasTrueAdditionalProperties(propertySchema)) {
            Schema inner = new ObjectSchema();
            return String.format("{ [key, string]: %s;}", getTypeDeclaration(inner));
        }
        return super.getTypeDeclaration(propertySchema);
    }

    @Override
    public void addImport(CodegenModel codegenModel, String type) {
        if (type == null) {
            return;
        }
        String[] names = type.split("( [|&] )|[<>]");
        for (String name : names) {
            if (needToImport(name)) {
                codegenModel.imports.add(name);
            }
        }
    }

    @Override
    public String toDefaultValue(Schema propertySchema) {
        if (propertySchema instanceof StringSchema) {
            StringSchema sp = (StringSchema) propertySchema;
            if (sp.getDefault() != null) {
                return "\"" + sp.getDefault() + "\"";
            }
            return UNDEFINED_VALUE;
        } else if (propertySchema instanceof BooleanSchema) {
            return UNDEFINED_VALUE;
        } else if (propertySchema instanceof DateSchema) {
            return UNDEFINED_VALUE;
        } else if (propertySchema instanceof DateTimeSchema) {
            return UNDEFINED_VALUE;
        } else if (propertySchema instanceof NumberSchema) {
            NumberSchema dp = (NumberSchema) propertySchema;
            if (dp.getDefault() != null) {
                return dp.getDefault().toString();
            }
            return UNDEFINED_VALUE;
        } else if (propertySchema instanceof IntegerSchema) {
            IntegerSchema ip = (IntegerSchema) propertySchema;
            if (ip.getDefault() != null) {
                return ip.getDefault().toString();
            }
            return UNDEFINED_VALUE;
        } else {
            return UNDEFINED_VALUE;
        }
    }

    @Override
    public String  getSchemaType(Schema schema) {
        String swaggerType = super.getSchemaType(schema);
        if (schema instanceof ComposedSchema) {
            ComposedSchema composedSchema = (ComposedSchema)schema;
            if (composedSchema.getAllOf() != null && !composedSchema.getAllOf().isEmpty()) {
                return String.join(" & ", getTypesFromInterfaces(composedSchema.getAllOf()));
            } else if (composedSchema.getOneOf() != null && !composedSchema.getOneOf().isEmpty()) {
                return String.join(" | ", getTypesFromInterfaces(composedSchema.getOneOf()));
            } else if (composedSchema.getAnyOf() != null && !composedSchema.getAnyOf().isEmpty()) {
                return String.join(" | ", getTypesFromInterfaces(composedSchema.getAnyOf()));
            } else {
                return "object";
            }
        }
        String type = null;
        if (typeMapping.containsKey(swaggerType)) {
            type = typeMapping.get(swaggerType);
            if (languageSpecificPrimitives.contains(type))
                return type;
        } else {
            type = swaggerType;
        }
        return toModelName(type);
    }

    private List<String> getTypesFromInterfaces(List<Schema> interfaces) {
        return interfaces.stream().map(schema -> {
                String schemaType = getSchemaType(schema);
                if (schema instanceof ArraySchema) {
                    ArraySchema ap = (ArraySchema) schema;
                    Schema inner = ap.getItems();
                    schemaType = schemaType + "<" + getSchemaType(inner) + ">";
                }
                return schemaType;
            }).distinct().collect(Collectors.toList());
    }

    @Override
    public String toOperationId(String operationId) {
        // throw exception if method name is empty
        if (StringUtils.isEmpty(operationId)) {
            throw new RuntimeException("Empty method name (operationId) not allowed");
        }

        // method name cannot use reserved keyword, e.g. return
        // append _ at the beginning, e.g. _return
        if (isReservedWord(operationId)) {
            return escapeReservedWord(camelize(sanitizeName(operationId), true));
        }

        return camelize(sanitizeName(operationId), true);
    }

    public void setModelPropertyNaming(String naming) {
        if ("original".equals(naming) || "camelCase".equals(naming) ||
            "PascalCase".equals(naming) || "snake_case".equals(naming)) {
            this.modelPropertyNaming = naming;
        } else {
            throw new IllegalArgumentException("Invalid model property naming '" +
                                               naming + "'. Must be 'original', 'camelCase', " +
                                               "'PascalCase' or 'snake_case'");
        }
    }

    public String getModelPropertyNaming() {
        return this.modelPropertyNaming;
    }

    public String getNameUsingModelPropertyNaming(String name) {
        switch (CodegenConstants.MODEL_PROPERTY_NAMING_TYPE.valueOf(getModelPropertyNaming())) {
            case original:    return name;
            case camelCase:   return camelize(name, true);
            case PascalCase:  return camelize(name);
            case snake_case:  return underscore(name);
            default:          throw new IllegalArgumentException("Invalid model property naming '" +
                                                                 name + "'. Must be 'original', 'camelCase', " +
                                                                 "'PascalCase' or 'snake_case'");
        }

    }

    @Override
    public String toEnumValue(String value, String datatype) {
        if ("number".equals(datatype)) {
            return value;
        } else {
            return "\'" + escapeText(value) + "\'";
        }
    }

    @Override
    public String toEnumDefaultValue(String value, String datatype) {
        return datatype + "_" + value;
    }

    @Override
    public String toEnumVarName(String name, String datatype) {
        if (name.length() == 0) {
            return "Empty";
        }

        // for symbol, e.g. $, #
        if (getSymbolName(name) != null) {
            return camelize(getSymbolName(name));
        }

        // number
        if ("number".equals(datatype)) {
            String varName = "NUMBER_" + name;

            varName = varName.replaceAll("-", "MINUS_");
            varName = varName.replaceAll("\\+", "PLUS_");
            varName = varName.replaceAll("\\.", "_DOT_");
            return varName;
        }

        // string
        String enumName = sanitizeName(name);
        enumName = enumName.replaceFirst("^_", "");
        enumName = enumName.replaceFirst("_$", "");

        // camelize the enum variable name
        // ref: https://basarat.gitbooks.io/typescript/content/docs/enums.html
        enumName = camelize(enumName);

        if (enumName.matches("\\d.*")) { // starts with number
            return "_" + enumName;
        } else {
            return enumName;
        }
    }

    @Override
    public String toEnumName(CodegenProperty property) {
        String enumName = toModelName(property.name) + "Enum";

        if (enumName.matches("\\d.*")) { // starts with number
            return "_" + enumName;
        } else {
            return enumName;
        }
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        // process enum in models
        List<Object> models = (List<Object>) postProcessModelsEnum(objs).get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");
            cm.imports = new TreeSet(cm.imports);
            // name enum with model name, e.g. StatusEnum => Pet.StatusEnum
            for (CodegenProperty var : cm.vars) {
                if (Boolean.TRUE.equals(var.getIsEnum())) {
                    var.datatypeWithEnum = var.datatypeWithEnum.replace(var.enumName, cm.classname + "." + var.enumName);
                }
            }
            if (cm.parent != null) {
                for (CodegenProperty var : cm.allVars) {
                    if (Boolean.TRUE.equals(var.getIsEnum())) {
                        var.datatypeWithEnum = var.datatypeWithEnum
                            .replace(var.enumName, cm.classname + "." + var.enumName);
                    }
                }
            }
        }

        return objs;
    }

    @Override
    protected void postProcessAllCodegenModels(Map<String, CodegenModel> allModels) {
        super.postProcessAllCodegenModels(allModels);
        for (CodegenModel cm : allModels.values()) {
            if(cm.discriminator != null) {
                cm.dataType = String.join(" | ", cm.discriminator.getMapping().keySet());
                for (Map.Entry<String, String> discriminatorMapping : cm.discriminator.getMapping().entrySet()) {
                    String simpleRef = OpenAPIUtil.getSimpleRef(discriminatorMapping.getValue());
                    String discriminatorValue = toModelName(simpleRef);
                    CodegenModel discriminatorModel = allModels.get(discriminatorValue);
                    cm.imports.add(discriminatorMapping.getKey());
                    cm.vendorExtensions.put(CodegenConstants.IS_ALIAS_EXT_NAME, Boolean.TRUE);
                    this.setDiscriminatorValue(discriminatorModel, cm.discriminator.getPropertyName(), discriminatorMapping.getKey());
                }
            }
        }
    }

    public void setSupportsES6(Boolean value) {
        supportsES6 = value;
    }

    public Boolean getSupportsES6() {
        return supportsES6;
    }

    private void setDiscriminatorValue(CodegenModel model, String propertyName, String value) {
        // override the datatype with the value for the taggedUnion type
        for (CodegenProperty prop : model.vars) {
            if (prop.baseName.equals(propertyName)) {
                prop.datatype = "'" + value + "'";
            }
        }
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove ', " to avoid code injection
        return input.replace("\"", "").replace("'", "");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }

    @Override
    public ISchemaHandler getSchemaHandler() {
        return new TypeScriptSchemaHandler(this);
    }
}
