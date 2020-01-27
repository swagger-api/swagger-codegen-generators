package io.swagger.codegen.v3.generators;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenModelFactory;
import io.swagger.codegen.v3.CodegenModelType;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.ISchemaHandler;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SchemaHandler implements ISchemaHandler {

    public static final String ONE_OF_PREFFIX = "OneOf";
    public static final String ANY_OF_PREFFIX = "AnyOf";
    public static final String ARRAY_ITEMS_SUFFIX = "Items";

    protected DefaultCodegenConfig codegenConfig;
    private List<CodegenModel> composedModels = new ArrayList<>();

    public SchemaHandler(DefaultCodegenConfig codegenConfig) {
        this.codegenConfig = codegenConfig;
    }

    @Override
    public void processComposedSchemas(CodegenModel codegenModel, Schema schema, Map<String, CodegenModel> allModels) {
        if (schema instanceof ComposedSchema) {
            this.processComposedSchema(codegenModel, (ComposedSchema) schema, allModels);
            return;
        }
        if (schema instanceof ArraySchema) {
            this.processArrayItemSchema(codegenModel, (ArraySchema) schema, allModels);
            return;
        }
        final Map<String, Schema> properties = schema.getProperties();
        if (properties == null || properties.isEmpty()) {
            return;
        }
        for (String name : properties.keySet()) {
            final Schema property = properties.get(name);
            final Optional<CodegenProperty> optionalCodegenProperty = codegenModel.getVars()
                .stream()
                .filter(codegenProperty -> codegenProperty.baseName.equals(name))
                .findFirst();
            if (!optionalCodegenProperty.isPresent()) {
                continue;
            }
            final CodegenProperty codegenProperty = optionalCodegenProperty.get();
            final String codegenName = codegenModel.getName() + codegenConfig.toModelName(codegenProperty.getName());
            if (property instanceof ComposedSchema) {
                processComposedSchema(codegenName, codegenProperty, (ComposedSchema) property, allModels);
                continue;
            }
            if (schema instanceof ArraySchema) {
                this.processArrayItemSchema(codegenName, codegenProperty, (ArraySchema) property, allModels);
                continue;
            }
        }
    }

    @Override
    public List<CodegenModel> getModels() {
        return composedModels;
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
        composedModels.add(composedModel);
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
        this.addInterfaces(schemas, composedModel, allModels);
        codegenProperty.datatype = composedModel.getName();
        codegenProperty.datatypeWithEnum = composedModel.getName();
        codegenProperty.baseType = composedModel.getName();
        codegenProperty.complexType = composedModel.getName();

        composedModels.add(composedModel);
    }

    protected void processArrayItemSchema(CodegenModel codegenModel, ArraySchema arraySchema, Map<String, CodegenModel> allModels) {
        final Schema itemsSchema = arraySchema.getItems();
        if (itemsSchema instanceof ComposedSchema) {
            final CodegenModel itemsModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
            itemsModel.setName(codegenModel.name + ARRAY_ITEMS_SUFFIX);
            this.processComposedSchema(itemsModel, (ComposedSchema) itemsSchema, allModels);
        }
    }

    protected void processArrayItemSchema(String codegenModelName, CodegenProperty codegenProperty, ArraySchema arraySchema, Map<String, CodegenModel> allModels) {
        final Schema itemsSchema = arraySchema.getItems();
        if (itemsSchema instanceof ComposedSchema) {
            this.processComposedSchema(codegenModelName, codegenProperty, (ComposedSchema) itemsSchema, allModels);
        }
    }

    protected CodegenModel createComposedModel(String name, List<Schema> schemas) {
        if (schemas == null || schemas.isEmpty()) {
            return null;
        }
        final CodegenModel composedModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        composedModel.setIsComposedModel(true);
        composedModel.setInterfaces(new ArrayList<>());
        this.configureModel(composedModel, name);

        return composedModel;
    }

    private void addInterfaceModel(CodegenModel codegenModel, CodegenModel interfaceModel) {
        if (codegenModel == null) {
            return;
        }
        if (codegenModel.getInterfaceModels() == null) {
            codegenModel.setInterfaceModels(new ArrayList<>());
        }
        codegenModel.getInterfaceModels().add(interfaceModel);
    }

    private void addInterfaces(List<Schema> schemas, CodegenModel codegenModel, Map<String, CodegenModel> allModels) {
        for (Schema interfaceSchema : schemas) {
            final String ref = interfaceSchema.get$ref();
            if (StringUtils.isBlank(ref)) {
                continue;
            }
            final String schemaName = ref.substring(ref.lastIndexOf("/") + 1);
            this.addInterfaceModel(allModels.get(codegenConfig.toModelName(schemaName)), codegenModel);
        }
    }

    /**
     * creates a codegen model object based on a composed schema property.
     * @param composedProperty
     * @param codegenProperty
     */
    @Deprecated
    protected void createCodegenModel(ComposedSchema composedProperty, CodegenProperty codegenProperty) {
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

    @Deprecated
    protected void configureComposedModelFromSchemaItems(CodegenModel codegenModel, ComposedSchema items) {
        List<Schema> oneOfList = items.getOneOf();
        if (oneOfList != null && !oneOfList.isEmpty()){
            String name = "OneOf" + codegenModel.name + "Items";
            final CodegenModel oneOfModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
            this.configureModel(oneOfModel, name);
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
            final CodegenModel anyOfModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
            this.configureModel(anyOfModel, name);
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

    @Deprecated
    public void configureOneOfModel(CodegenModel codegenModel, List<Schema> oneOf) {
        final CodegenModel oneOfModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        this.configureModel(oneOfModel, "OneOf" + codegenModel.name);

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

    @Deprecated
    public void configureAnyOfModel(CodegenModel codegenModel, List<Schema> anyOf) {
        final CodegenModel anyOfModel = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        this.configureModel(anyOfModel, "AnyOf" + codegenModel.name);
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

    @Deprecated
    protected void configureOneOfModelFromProperty(CodegenProperty codegenProperty, CodegenModel codegenModel) {
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

    @Deprecated
    protected void configureAnyOfModelFromProperty(CodegenProperty codegenProperty, CodegenModel codegenModel) {
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

    @Deprecated
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

    protected void configureModel(CodegenModel codegenModel, String name) {
        codegenModel.name = name;
        codegenModel.classname = codegenConfig.toModelName(name);
        codegenModel.classVarName = codegenConfig.toVarName(name);
        codegenModel.classFilename = codegenConfig.toModelFilename(name);
    }

    protected boolean hasNonObjectSchema(List<Schema> schemas) {
        for  (Schema schema : schemas) {
            if (!codegenConfig.isObjectSchema(schema)) {
                return true;
            }
        }
        return false;
    }
}
