package io.swagger.codegen.v3.generators;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenModelFactory;
import io.swagger.codegen.v3.CodegenModelType;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.ISchemaHandler;
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
            this.addComposedModel(this.processComposedSchema(codegenModel, (ComposedSchema) schema, allModels));
            return;
        }
        if (schema instanceof ArraySchema) {
            this.addComposedModel(this.processArrayItemSchema(codegenModel, (ArraySchema) schema, allModels));
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
                this.addComposedModel(this.processComposedSchema(codegenName, codegenProperty, (ComposedSchema) property, allModels));
                continue;
            }
            if (property instanceof ArraySchema) {
                this.addComposedModel(this.processArrayItemSchema(codegenName, codegenProperty, (ArraySchema) property, allModels));
                continue;
            }
        }
    }

    @Override
    public List<CodegenModel> getModels() {
        return composedModels;
    }

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
        return composedModel;
    }

    protected CodegenModel processComposedSchema(String name, ComposedSchema composedSchema, Map<String, CodegenModel> allModels) {
        List<Schema> schemas = composedSchema.getOneOf();
        CodegenModel composedModel = this.createComposedModel(ONE_OF_PREFFIX + name, schemas);
        if (composedModel == null) {
            schemas = composedSchema.getAnyOf();
            composedModel = this.createComposedModel(ANY_OF_PREFFIX + name, schemas);
            if (composedModel == null) {
                return null;
            }
        }
        this.addInterfaces(schemas, composedModel, allModels);
        return composedModel;
    }

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
        this.addInterfaces(schemas, composedModel, allModels);
        codegenProperty.datatype = composedModel.getClassname();
        codegenProperty.datatypeWithEnum = composedModel.getClassname();
        codegenProperty.baseType = composedModel.getClassname();
        codegenProperty.complexType = composedModel.getClassname();
        return composedModel;
    }

    protected CodegenModel processArrayItemSchema(CodegenModel codegenModel, ArraySchema arraySchema, Map<String, CodegenModel> allModels) {
        final Schema itemsSchema = arraySchema.getItems();
        if (itemsSchema instanceof ComposedSchema) {
            final CodegenModel composedModel = this.processComposedSchema(codegenModel.name + ARRAY_ITEMS_SUFFIX, (ComposedSchema) itemsSchema, allModels);
            this.updateArrayModel(codegenModel, composedModel.name, arraySchema);
            return composedModel;
        }
        return null;
    }

    protected CodegenModel processArrayItemSchema(String codegenModelName, CodegenProperty codegenProperty, ArraySchema arraySchema, Map<String, CodegenModel> allModels) {
        final Schema itemsSchema = arraySchema.getItems();
        if (itemsSchema instanceof ComposedSchema) {
            final CodegenModel composedModel = this.processComposedSchema(codegenModelName + ARRAY_ITEMS_SUFFIX, codegenProperty.items, (ComposedSchema) itemsSchema, allModels);
            if (composedModel == null) {
                return null;
            }
            this.updatePropertyDataType(codegenProperty, composedModel.name, arraySchema);
            return composedModel;
        }
        return null;
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

    protected void addInterfaceModel(CodegenModel codegenModel, CodegenModel interfaceModel) {
        if (codegenModel == null) {
            return;
        }
        if (codegenModel.getInterfaceModels() == null) {
            codegenModel.setInterfaceModels(new ArrayList<>());
        }
        codegenModel.getInterfaceModels().add(interfaceModel);
    }

    protected void addInterfaces(List<Schema> schemas, CodegenModel codegenModel, Map<String, CodegenModel> allModels) {
        for (Schema interfaceSchema : schemas) {
            final String ref = interfaceSchema.get$ref();
            if (StringUtils.isBlank(ref)) {
                continue;
            }
            final String schemaName = ref.substring(ref.lastIndexOf("/") + 1);
            this.addInterfaceModel(allModels.get(codegenConfig.toModelName(schemaName)), codegenModel);
        }
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

    protected void addComposedModel(CodegenModel composedModel) {
        if (composedModel == null) {
            return;
        }
        this.composedModels.add(composedModel);
    }

    protected void updatePropertyDataType(CodegenProperty codegenProperty, String schemaName, ArraySchema arraySchema) {
        final Schema items = arraySchema.getItems();
        final Schema refSchema = new Schema();
        refSchema.set$ref("#/components/schemas/" + schemaName);
        arraySchema.setItems(refSchema);
        codegenProperty.setDatatype(this.codegenConfig.getTypeDeclaration(arraySchema));
        codegenProperty.setDatatypeWithEnum(codegenProperty.getDatatype());

        codegenProperty.defaultValue = this.codegenConfig.toDefaultValue(arraySchema);
        codegenProperty.defaultValueWithParam = this.codegenConfig.toDefaultValueWithParam(codegenProperty.baseName, arraySchema);

        arraySchema.setItems(items);
    }

    protected void updateArrayModel(CodegenModel codegenModel, String schemaName, ArraySchema arraySchema) {
        final Schema items = arraySchema.getItems();
        final Schema refSchema = new Schema();
        refSchema.set$ref("#/components/schemas/" + schemaName);
        arraySchema.setItems(refSchema);

        this.codegenConfig.addParentContainer(codegenModel, codegenModel.name, arraySchema);
        codegenModel.defaultValue = this.codegenConfig.toDefaultValue(arraySchema);
        codegenModel.arrayModelType = this.codegenConfig.fromProperty(codegenModel.name, arraySchema).complexType;

        arraySchema.setItems(items);
    }
}
