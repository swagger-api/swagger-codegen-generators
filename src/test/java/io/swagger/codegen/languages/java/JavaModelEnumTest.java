package io.swagger.codegen.languages.java;

import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.handlebars.helpers.BaseItemsHelper;
import io.swagger.codegen.languages.DefaultCodegenConfig;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.swagger.codegen.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.handlebars.helpers.ExtensionHelper.getBooleanValue;

public class JavaModelEnumTest {
    @Test(description = "convert a java model with an enum")
    public void converterTest() {
        final StringSchema enumSchema = new StringSchema();
        enumSchema.setEnum(Arrays.asList("VALUE1", "VALUE2", "VALUE3"));
        final Schema model = new Schema().type("object").addProperties("name", enumSchema);

        final DefaultCodegenConfig codegen = new JavaClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model);

        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty enumVar = cm.vars.get(0);
        Assert.assertEquals(enumVar.baseName, "name");
        Assert.assertEquals(enumVar.datatype, "String");
        Assert.assertEquals(enumVar.datatypeWithEnum, "NameEnum");
        Assert.assertEquals(enumVar.name, "name");
        Assert.assertEquals(enumVar.defaultValue, "null");
        Assert.assertEquals(enumVar.baseType, "String");
        Assert.assertTrue(getBooleanValue(enumVar, IS_ENUM_EXT_NAME));
        CodegenProperty baseItems = BaseItemsHelper.getBaseItemsProperty(enumVar);
        Assert.assertEquals(baseItems, enumVar);
    }

    @Test(description = "convert a java model with an enum inside a list")
    public void converterInArrayTest() {
        final ArraySchema enumSchema = new ArraySchema().items(
                        new StringSchema().addEnumItem("Aaaa").addEnumItem("Bbbb"));
        final Schema model = new Schema().type("object").addProperties("name", enumSchema);

        final DefaultCodegenConfig codegen = new JavaClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model);

        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty enumVar = cm.vars.get(0);
        Assert.assertEquals(enumVar.baseName, "name");
        Assert.assertEquals(enumVar.datatype, "List<String>");
        Assert.assertEquals(enumVar.datatypeWithEnum, "List<NameEnum>");
        Assert.assertEquals(enumVar.name, "name");
        Assert.assertEquals(enumVar.defaultValue, "new ArrayList<NameEnum>()");
        Assert.assertEquals(enumVar.baseType, "List");
        Assert.assertTrue(getBooleanValue(enumVar, IS_ENUM_EXT_NAME));
        CodegenProperty baseItems = BaseItemsHelper.getBaseItemsProperty(enumVar);
        Assert.assertEquals(baseItems.baseName, "name");
        Assert.assertEquals(baseItems.datatype, "String");
        Assert.assertEquals(baseItems.datatypeWithEnum, "NameEnum");
        Assert.assertEquals(baseItems.name, "name");
        Assert.assertEquals(baseItems.defaultValue, "null");
        Assert.assertEquals(baseItems.baseType, "String");
    }

    @Test(description = "convert a java model with an enum inside a list")
    public void converterInArrayInArrayTest() {
        final ArraySchema enumSchema = new ArraySchema().items(
                new ArraySchema().items(
                        new StringSchema().addEnumItem("Aaaa").addEnumItem("Bbbb")));
        final Schema model = new Schema().type("object").addProperties("name", enumSchema);

        final DefaultCodegenConfig codegen = new JavaClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model);

        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty enumVar = cm.vars.get(0);
        Assert.assertEquals(enumVar.baseName, "name");
        Assert.assertEquals(enumVar.datatype, "List<List<String>>");
        Assert.assertEquals(enumVar.datatypeWithEnum, "List<List<NameEnum>>");
        Assert.assertEquals(enumVar.name, "name");
        Assert.assertEquals(enumVar.defaultValue, "new ArrayList<List<NameEnum>>()");
        Assert.assertEquals(enumVar.baseType, "List");
        Assert.assertTrue(getBooleanValue(enumVar, IS_ENUM_EXT_NAME));
        CodegenProperty baseItems = BaseItemsHelper.getBaseItemsProperty(enumVar);
        Assert.assertEquals(baseItems.baseName, "name");
        Assert.assertEquals(baseItems.datatype, "String");
        Assert.assertEquals(baseItems.datatypeWithEnum, "NameEnum");
        Assert.assertEquals(baseItems.name, "name");
        Assert.assertEquals(baseItems.defaultValue, "null");
        Assert.assertEquals(baseItems.baseType, "String");
    }

    @Test(description = "not override identical parent enums")
    public void overrideEnumTest() {
        final StringSchema identicalEnumProperty = new StringSchema();
        identicalEnumProperty.setEnum(Arrays.asList("VALUE1", "VALUE2", "VALUE3"));

        final StringSchema subEnumProperty = new StringSchema();
        subEnumProperty.setEnum(Arrays.asList("SUB1", "SUB2", "SUB3"));

        // Add one enum property to the parent
        final Map<String, Schema> parentProperties = new HashMap<>();
        parentProperties.put("sharedThing", identicalEnumProperty);

        // Add TWO enums to the subType model; one of which is identical to the one in parent class
        final Map<String, Schema> subProperties = new HashMap<>();
        subProperties.put("unsharedThing", subEnumProperty);

        final Schema parentModel = new Schema();
        parentModel.setProperties(parentProperties);
        parentModel.name("parentModel");

        final ComposedSchema composedSchema = new ComposedSchema()
                .addAllOfItem(new Schema().$ref(parentModel.getName()));

        final DefaultCodegenConfig codegen = new JavaClientCodegen();
        final Map<String, Schema> allModels = new HashMap<String, Schema>();
        allModels.put(parentModel.getName(), parentModel);
        allModels.put(composedSchema.getName(), composedSchema);

        final CodegenModel cm = codegen.fromModel("sample", composedSchema, allModels);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.parent, "ParentModel");
        Assert.assertTrue(cm.imports.contains("ParentModel"));
    }
}
