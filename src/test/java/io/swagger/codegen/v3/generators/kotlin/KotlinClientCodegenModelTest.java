package io.swagger.codegen.v3.generators.kotlin;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

@SuppressWarnings("static-method")
public class KotlinClientCodegenModelTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(KotlinClientCodegenModelTest.class);

    private Schema getArrayTestSchema() {
        final Schema propertySchema = new ArraySchema()
                .items(new StringSchema())
                .description("an array property");
        return new Schema()
                .type("object")
                .description("a sample model")
                .addProperties("examples", propertySchema);
    }

    private Schema getSimpleSchema() {
        return new Schema()
                .type("object")
                .description("a sample model")
                .addProperties("id", new IntegerSchema()
                        .format(SchemaTypeUtil.INTEGER64_FORMAT))
                .addProperties("first-name", new StringSchema()
                        .example("Tony"))
                .addProperties("createdAt", new DateTimeSchema())
                .addRequiredItem("id")
                .addRequiredItem("first-name");
    }

    private Schema getMapSchema() {
        return new Schema()
                .type("object")
                .description("a sample model")
                .addProperties("mapping", new MapSchema()
                        .additionalProperties(new StringSchema()))
                .addRequiredItem("id");
    }

    private Schema getComplexSchema() {
        return new Schema()
                .description("a sample model")
                .type("object")
                .addProperties("child", new Schema().$ref("#/components/schemas/Child"));
    }

    @Test(description = "convert a simple model")
    public void simpleModelTest() {
        final Schema schema = getSimpleSchema();
        final KotlinClientCodegen codegen = new KotlinClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", schema);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 3);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "id");
        Assert.assertEquals(property1.datatype, "kotlin.Long");
        Assert.assertEquals(property1.name, "id");
        Assert.assertEquals(property1.defaultValue, "null");
        Assert.assertEquals(property1.baseType, "kotlin.Long");
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertTrue(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME));
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));

        final CodegenProperty property2 = cm.vars.get(1);
        Assert.assertEquals(property2.baseName, "first-name");
        Assert.assertEquals(property2.datatype, "kotlin.String");
        Assert.assertEquals(property2.name, "firstName");
        Assert.assertEquals(property2.defaultValue, "null");
        Assert.assertEquals(property2.baseType, "kotlin.String");
        Assert.assertTrue(getBooleanValue(property2, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertTrue(property2.required);
        Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME));
        Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));

        final CodegenProperty property3 = cm.vars.get(2);
        Assert.assertEquals(property3.baseName, "createdAt");
        Assert.assertEquals(property3.datatype, "java.time.LocalDateTime");
        Assert.assertEquals(property3.name, "createdAt");
        Assert.assertEquals(property3.defaultValue, "null");
        Assert.assertEquals(property3.baseType, "java.time.LocalDateTime");
        Assert.assertFalse(getBooleanValue(property3, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertFalse(property3.required);
        Assert.assertTrue(getBooleanValue(property3, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
    }

    @Test(description = "convert a simple model: threetenbp")
    public void selectDateLibraryAsThreetenbp() {
        final Schema schema = getSimpleSchema();
        final KotlinClientCodegen codegen = new KotlinClientCodegen();
        codegen.setDateLibrary(KotlinClientCodegen.DateLibrary.THREETENBP.value);
        codegen.processOpts();

        final CodegenModel cm = codegen.fromModel("sample", schema);

        final CodegenProperty property3 = cm.vars.get(2);
        Assert.assertEquals(property3.baseName, "createdAt");
        Assert.assertEquals(property3.datatype, "org.threeten.bp.LocalDateTime");
        Assert.assertEquals(property3.name, "createdAt");
        Assert.assertEquals(property3.defaultValue, "null");
        Assert.assertEquals(property3.baseType, "org.threeten.bp.LocalDateTime");
        Assert.assertFalse(getBooleanValue(property3, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertFalse(property3.required);
        Assert.assertTrue(getBooleanValue(property3, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
    }

    @Test(description = "convert a simple model: date string")
    public void selectDateLibraryAsString() {
        final Schema schema = getSimpleSchema();
        final KotlinClientCodegen codegen = new KotlinClientCodegen();
        codegen.setDateLibrary(KotlinClientCodegen.DateLibrary.STRING.value);
        codegen.processOpts();

        final CodegenModel cm = codegen.fromModel("sample", schema);

        final CodegenProperty property3 = cm.vars.get(2);
        Assert.assertEquals(property3.baseName, "createdAt");
        Assert.assertEquals(property3.datatype, "kotlin.String");
        Assert.assertEquals(property3.name, "createdAt");
        Assert.assertEquals(property3.defaultValue, "null");
        Assert.assertEquals(property3.baseType, "kotlin.String");
        Assert.assertFalse(getBooleanValue(property3, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertFalse(property3.required);
        Assert.assertTrue(getBooleanValue(property3, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
    }

    @Test(description = "convert a simple model: date java8")
    public void selectDateLibraryAsJava8() {
        final Schema schema = getSimpleSchema();
        final KotlinClientCodegen codegen = new KotlinClientCodegen();
        codegen.setDateLibrary(KotlinClientCodegen.DateLibrary.JAVA8.value);
        codegen.processOpts();

        final CodegenModel cm = codegen.fromModel("sample", schema);

        final CodegenProperty property3 = cm.vars.get(2);
        Assert.assertEquals(property3.baseName, "createdAt");
        Assert.assertEquals(property3.datatype, "java.time.LocalDateTime");
        Assert.assertEquals(property3.name, "createdAt");
        Assert.assertEquals(property3.defaultValue, "null");
        Assert.assertEquals(property3.baseType, "java.time.LocalDateTime");
        Assert.assertFalse(getBooleanValue(property3, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertFalse(property3.required);
        Assert.assertTrue(getBooleanValue(property3, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
    }

    @Test(description = "convert a model with array property to default kotlin.Array")
    public void arrayPropertyTest() {
        final Schema schema = getArrayTestSchema();

        final CodegenConfig codegen = new KotlinClientCodegen();
        final CodegenModel generated = codegen.fromModel("sample", schema);

        Assert.assertEquals(generated.name, "sample");
        Assert.assertEquals(generated.classname, "Sample");
        Assert.assertEquals(generated.description, "a sample model");
        Assert.assertEquals(generated.vars.size(), 1);

        final CodegenProperty property = generated.vars.get(0);
        Assert.assertEquals(property.baseName, "examples");
        Assert.assertEquals(property.getter, "getExamples");
        Assert.assertEquals(property.setter, "setExamples");
        Assert.assertEquals(property.datatype, "kotlin.Array<kotlin.String>");
        Assert.assertEquals(property.name, "examples");
        Assert.assertEquals(property.defaultValue, "null");
        Assert.assertEquals(property.baseType, "kotlin.Array");
        Assert.assertEquals(property.containerType, "array");
        Assert.assertFalse(property.required);
        Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
    }

    @Test(description = "convert a model with a map property")
    public void mapPropertyTest() {
        final Schema schema = getMapSchema();
        final CodegenConfig codegen = new KotlinClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", schema);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "mapping");
        Assert.assertEquals(property1.datatype, "kotlin.collections.Map<kotlin.String, kotlin.String>");
        Assert.assertEquals(property1.name, "mapping");
        Assert.assertEquals(property1.baseType, "kotlin.collections.Map");
        Assert.assertEquals(property1.containerType, "map");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_CONTAINER_EXT_NAME));
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME));
    }

    @Test(description = "convert a model with complex property")
    public void complexPropertyTest() {
        final Schema schema = getComplexSchema();
        final CodegenConfig codegen = new KotlinClientCodegen();
        codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
        final CodegenModel cm = codegen.fromModel("sample", schema);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "child");
        Assert.assertEquals(property1.datatype, "Child");
        Assert.assertEquals(property1.name, "child");
        Assert.assertEquals(property1.baseType, "Child");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
    }

    @Test(description = "convert a model with complex property")
    public void prefixSuffixTest() {
        final Schema schema = getComplexSchema();
        final KotlinClientCodegen codegen = new KotlinClientCodegen();
        codegen.setDateLibrary(KotlinClientCodegen.DateLibrary.JAVA8.value);
        codegen.setModelNamePrefix("Swagger");
        codegen.setModelNameSuffix("Dto");
        codegen.processOpts();

        final CodegenModel cm = codegen.fromModel("sample", schema);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "SwaggerSampleDto");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "child");
        Assert.assertEquals(property1.datatype, "SwaggerChildDto");
        Assert.assertEquals(property1.name, "child");
        Assert.assertEquals(property1.baseType, "SwaggerChildDto");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
    }

    @DataProvider(name = "modelNames")
    public static Object[][] modelNames() {
        return new Object[][]{
                {"TestNs.TestClass", new ModelNameTest("TestNs.TestClass", "TestNsTestClass")},
                {"$", new ModelNameTest("$", "Dollar")},
                {"for", new ModelNameTest("`for`", "For")},
                {"package", new ModelNameTest("`package`", "Package")},
                {"abstract", new ModelNameTest("`abstract`", "Abstract")},
                {"companion", new ModelNameTest("`companion`", "_Companion")},
                {"One<Two", new ModelNameTest("One<Two", "OneLess_ThanTwo")},
                {"this is a test", new ModelNameTest("this is a test", "This_is_a_test")}
        };
    }

    @Test(dataProvider = "modelNames", description = "sanitize model names")
    public void sanitizeModelNames(final String name, final ModelNameTest testCase) {
        final Schema schema = getComplexSchema();
        final CodegenConfig codegen = new KotlinClientCodegen();
        codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));

        final CodegenModel cm = codegen.fromModel(name, schema);

        Assert.assertEquals(cm.name, testCase.expectedName);
        Assert.assertEquals(cm.classname, testCase.expectedClassName);
    }

    @DataProvider
    public static Object[][] enumNames() {
        return new Object[][]{
                {"VALUE1", "VALUE1"},
                {"1", "_1"},
                {"1X2", "_1X2"},
                {"1x2", "_1X2"}
        };
    }

    @Test(dataProvider = "enumNames", description = "sanitize Enum var names")
    public void sanitizeEnumVarNames(final String name, final String expectedName) {
        final KotlinClientCodegen codegen = new KotlinClientCodegen();
        Assert.assertEquals(codegen.toEnumVarName(name, "String"), expectedName);

    }

    private static class ModelNameTest {
        private String expectedName;
        private String expectedClassName;

        private ModelNameTest(String nameAndClass) {
            this.expectedName = nameAndClass;
            this.expectedClassName = nameAndClass;
        }

        private ModelNameTest(String expectedName, String expectedClassName) {
            this.expectedName = expectedName;
            this.expectedClassName = expectedClassName;
        }
    }
}