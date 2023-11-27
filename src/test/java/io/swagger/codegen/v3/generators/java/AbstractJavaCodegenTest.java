package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.CodegenArgument;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractJavaCodegenTest {

    private final AbstractJavaCodegen fakeJavaCodegen = new P_AbstractJavaCodegen();

    @Test
    public void toEnumVarNameShouldNotShortenUnderScore() throws Exception {
        Assert.assertEquals("UNDERSCORE", fakeJavaCodegen.toEnumVarName("_", "String"));
        Assert.assertEquals("__", fakeJavaCodegen.toEnumVarName("__", "String"));
        Assert.assertEquals("__", fakeJavaCodegen.toEnumVarName("_,.", "String"));
    }

    @Test
    public void toEnumVarNameShouldNotCreateSingleUnderscore() throws Exception {
        Assert.assertEquals("_U", fakeJavaCodegen.toEnumVarName(",.", "String"));
    }

    @Test
    public void toVarNameShouldAvoidOverloadingGetClassMethod() throws Exception {
        Assert.assertEquals("propertyClass", fakeJavaCodegen.toVarName("class"));
        Assert.assertEquals("propertyClass", fakeJavaCodegen.toVarName("_class"));
        Assert.assertEquals("propertyClass", fakeJavaCodegen.toVarName("__class"));
    }

    @Test
    public void toModelNameShouldUseProvidedMapping() throws Exception {
        fakeJavaCodegen.importMapping().put("json_myclass", "com.test.MyClass");
        Assert.assertEquals("com.test.MyClass", fakeJavaCodegen.toModelName("json_myclass"));
    }

    @Test
    public void toModelNameUsesPascalCase() throws Exception {
        Assert.assertEquals("JsonAnotherclass", fakeJavaCodegen.toModelName("json_anotherclass"));
    }

    @Test
    public void preprocessSwaggerWithFormParamsSetsContentType() {
        PathItem dummyPath = new PathItem()
                .post(new Operation().requestBody(new RequestBody()))
                .get(new Operation());

        OpenAPI openAPI = new OpenAPI()
                .path("dummy", dummyPath);

        fakeJavaCodegen.preprocessOpenAPI(openAPI);

        Assert.assertNull(openAPI.getPaths().get("dummy").getGet().getExtensions().get("x-contentType"));
        // TODO: Assert.assertEquals(openAPI.getPath("dummy").getPost().getVendorExtensions().get("x-contentType"), "application/x-www-form-urlencoded");
    }

    @Test
    public void preprocessSwaggerWithBodyParamsSetsContentType() {
        PathItem dummyPath = new PathItem()
                .post(new Operation().requestBody(new RequestBody()))
                .get(new Operation());

        OpenAPI openAPI = new OpenAPI()
                .path("dummy", dummyPath);

        fakeJavaCodegen.preprocessOpenAPI(openAPI);

        Assert.assertNull(openAPI.getPaths().get("dummy").getGet().getExtensions().get("x-contentType"));
        Assert.assertEquals(openAPI.getPaths().get("dummy").getPost().getExtensions().get("x-contentType"), "application/json");
    }

    @Test
    public void preprocessSwaggerWithNoFormOrBodyParamsDoesNotSetContentType() {
        PathItem dummyPath = new PathItem()
                .post(new Operation())
                .get(new Operation());

        OpenAPI openAPI = new OpenAPI()
                .path("dummy", dummyPath);

        fakeJavaCodegen.preprocessOpenAPI(openAPI);

        Assert.assertNull(openAPI.getPaths().get("dummy").getGet().getExtensions().get("x-contentType"));
        Assert.assertNotNull(openAPI.getPaths().get("dummy").getPost().getExtensions().get("x-contentType"));
    }

    @Test
     public void convertVarName() throws Exception {
        Assert.assertEquals(fakeJavaCodegen.toVarName("name"), "name");
        Assert.assertEquals(fakeJavaCodegen.toVarName("$name"), "$name");
        Assert.assertEquals(fakeJavaCodegen.toVarName("nam$$e"), "nam$$e");
        Assert.assertEquals(fakeJavaCodegen.toVarName("_name"), "_name");
        Assert.assertEquals(fakeJavaCodegen.toVarName("user-name"), "userName");
        Assert.assertEquals(fakeJavaCodegen.toVarName("user_name"), "userName");
        Assert.assertEquals(fakeJavaCodegen.toVarName("_user_name"), "_userName");
        Assert.assertEquals(fakeJavaCodegen.toVarName(":user_name"), "userName");
        Assert.assertEquals(fakeJavaCodegen.toVarName("_"), "u");
    }

    @Test
    public void convertModelName() throws Exception {
        Assert.assertEquals(fakeJavaCodegen.toModelName("name"), "Name");
        Assert.assertEquals(fakeJavaCodegen.toModelName("$name"), "Name");
        Assert.assertEquals(fakeJavaCodegen.toModelName("nam#e"), "Name");
        Assert.assertEquals(fakeJavaCodegen.toModelName("$another-fake?"), "AnotherFake");
    }

    @Test
    public void testInitialPackageNamesValues() throws Exception {
        final AbstractJavaCodegen codegen = new P_AbstractJavaCodegen();
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "invalidPackageName");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), null);
        Assert.assertEquals(codegen.apiPackage(), "invalidPackageName");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), null);
        Assert.assertEquals(codegen.invokerPackage, "io.swagger");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "io.swagger");
    }

    @Test
    public void testPackageNamesSetWithSetters() throws Exception {
        final AbstractJavaCodegen codegen = new P_AbstractJavaCodegen();
        codegen.setModelPackage("xxx.yyyyy.zzzzzzz.model");
        codegen.setApiPackage("xxx.yyyyy.zzzzzzz.api");
        codegen.setInvokerPackage("xxx.yyyyy.zzzzzzz.invoker");
        codegen.setSortParamsByRequiredFlag(false);
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "xxx.yyyyy.zzzzzzz.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xxx.yyyyy.zzzzzzz.model");
        Assert.assertEquals(codegen.apiPackage(), "xxx.yyyyy.zzzzzzz.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xxx.yyyyy.zzzzzzz.api");
        Assert.assertEquals(codegen.invokerPackage, "xxx.yyyyy.zzzzzzz.invoker");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "xxx.yyyyy.zzzzzzz.invoker");
        Assert.assertEquals(codegen.getSortParamsByRequiredFlag(), Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.FALSE);
    }

    @Test
    public void testPackageNamesSetWithAdditionalProperties() throws Exception {
        final AbstractJavaCodegen codegen = new P_AbstractJavaCodegen();
        codegen.additionalProperties().put(CodegenConstants.MODEL_PACKAGE, "xxx.yyyyy.model.xxxxxx");
        codegen.additionalProperties().put(CodegenConstants.API_PACKAGE, "xxx.yyyyy.api.xxxxxx");
        codegen.additionalProperties().put(CodegenConstants.INVOKER_PACKAGE, "xxx.yyyyy.invoker.xxxxxx");
        codegen.setSortParamsByRequiredFlag(true);
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "xxx.yyyyy.model.xxxxxx");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xxx.yyyyy.model.xxxxxx");
        Assert.assertEquals(codegen.apiPackage(), "xxx.yyyyy.api.xxxxxx");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xxx.yyyyy.api.xxxxxx");
        Assert.assertEquals(codegen.invokerPackage, "xxx.yyyyy.invoker.xxxxxx");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "xxx.yyyyy.invoker.xxxxxx");
        Assert.assertEquals(codegen.getSortParamsByRequiredFlag(), Boolean.TRUE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.TRUE);
    }

    @Test
    public void testFixUpParentAndInterfaces_propertyNameDifferent_getterSetterSame_typeDifferent() {
        AbstractJavaCodegen codegen = new P_AbstractJavaCodegen();

        CodegenModel parentModel = new CodegenModel();
        parentModel.name = "parent_type";
        CodegenModel childModel = new CodegenModel();
        childModel.name = "child_type";
        childModel.parentModel = parentModel;
        parentModel.children = new ArrayList<>();

        CodegenProperty parentValueProperty1 = new CodegenProperty();
        parentValueProperty1.name = "value";
        parentValueProperty1.baseName = "value";
        parentValueProperty1.getter = "getValue";
        parentValueProperty1.setter = "setValue";
        parentValueProperty1.datatype = "value_type";
        CodegenProperty parentValueProperty2 = new CodegenProperty();
        parentValueProperty2.name = "other_value";
        parentValueProperty2.baseName = "other_value";
        parentValueProperty2.getter = "getOtherValue";
        parentValueProperty2.setter = "setOtherValue";
        parentValueProperty2.datatype = "other_type";
        parentModel.vars = new ArrayList<>();
        parentModel.vars.add(parentValueProperty1);
        parentModel.vars.add(parentValueProperty2);

        CodegenProperty childValueProperty1 = new CodegenProperty();
        childValueProperty1.name = "_value"; // different to parent "value"
        childValueProperty1.baseName = "_value";
        childValueProperty1.getter = "getValue"; // same as parent "getValue"
        childValueProperty1.setter = "setValue"; // same as parent "setValue"
        childValueProperty1.datatype = "different_type"; // different to parent "value_type"
        CodegenProperty childValueProperty2 = new CodegenProperty();
        childValueProperty2.name = "third_value";
        childValueProperty2.baseName = "third_value";
        childValueProperty2.getter = "getThirdValue";
        childValueProperty2.setter = "setThirdValue";
        childValueProperty2.datatype = "other_type";
        childModel.vars = new ArrayList<>();
        childModel.vars.add(childValueProperty1);
        childModel.vars.add(childValueProperty2);

        Map<String, CodegenModel> allModels = new HashMap<>();
        allModels.put(parentModel.name, parentModel);
        allModels.put(childModel.name, childModel);

        codegen.fixUpParentAndInterfaces(childModel, Collections.EMPTY_MAP);
        Assert.assertEquals(childModel.vars.get(0).baseName, "_value");
        Assert.assertEquals(childModel.vars.get(0).name, "childTypeValue");
        Assert.assertEquals(childModel.vars.get(0).getter, "getChildTypeValue");
        Assert.assertEquals(childModel.vars.get(0).setter, "setChildTypeValue");

        // unchanged
        Assert.assertEquals(childModel.vars.get(1).baseName, childValueProperty2.baseName);
        Assert.assertEquals(childModel.vars.get(1).name, childValueProperty2.name);
        Assert.assertEquals(childModel.vars.get(1).getter, childValueProperty2.getter);
        Assert.assertEquals(childModel.vars.get(1).setter, childValueProperty2.setter);
    }

    @Test
    public void testFixUpParentAndInterfaces_propertyNameSame_getterSetterSame_typeDifferent() {
        AbstractJavaCodegen codegen = new P_AbstractJavaCodegen();

        CodegenModel parentModel = new CodegenModel();
        parentModel.name = "parent_type";
        CodegenModel childModel = new CodegenModel();
        childModel.name = "child_type";
        childModel.parentModel = parentModel;
        parentModel.children = new ArrayList<>();

        CodegenProperty parentValueProperty1 = new CodegenProperty();
        parentValueProperty1.name = "value";
        parentValueProperty1.baseName = "value";
        parentValueProperty1.getter = "getValue";
        parentValueProperty1.setter = "setValue";
        parentValueProperty1.datatype = "value_type";
        CodegenProperty parentValueProperty2 = new CodegenProperty();
        parentValueProperty2.name = "other_value";
        parentValueProperty2.baseName = "other_value";
        parentValueProperty2.getter = "getOtherValue";
        parentValueProperty2.setter = "setOtherValue";
        parentValueProperty2.datatype = "other_type";
        parentModel.vars = new ArrayList<>();
        parentModel.vars.add(parentValueProperty1);
        parentModel.vars.add(parentValueProperty2);

        CodegenProperty childValueProperty1 = new CodegenProperty();
        childValueProperty1.name = "value"; // same as parent "value"
        childValueProperty1.baseName = "value";
        childValueProperty1.getter = "getValue"; // same as parent "getValue"
        childValueProperty1.setter = "setValue"; // same as parent "setValue"
        childValueProperty1.datatype = "different_type"; // different to parent "value_type"
        CodegenProperty childValueProperty2 = new CodegenProperty();
        childValueProperty2.name = "third_value";
        childValueProperty2.baseName = "third_value";
        childValueProperty2.getter = "getThirdValue";
        childValueProperty2.setter = "setThirdValue";
        childValueProperty2.datatype = "other_type";
        childModel.vars = new ArrayList<>();
        childModel.vars.add(childValueProperty1);
        childModel.vars.add(childValueProperty2);

        Map<String, CodegenModel> allModels = new HashMap<>();
        allModels.put(parentModel.name, parentModel);
        allModels.put(childModel.name, childModel);

        codegen.fixUpParentAndInterfaces(childModel, Collections.EMPTY_MAP);
        Assert.assertEquals(childModel.vars.get(0).baseName, "value");
        Assert.assertEquals(childModel.vars.get(0).name, "childTypeValue");
        Assert.assertEquals(childModel.vars.get(0).getter, "getChildTypeValue");
        Assert.assertEquals(childModel.vars.get(0).setter, "setChildTypeValue");

        // unchanged
        Assert.assertEquals(childModel.vars.get(1).baseName, childValueProperty2.baseName);
        Assert.assertEquals(childModel.vars.get(1).name, childValueProperty2.name);
        Assert.assertEquals(childModel.vars.get(1).getter, childValueProperty2.getter);
        Assert.assertEquals(childModel.vars.get(1).setter, childValueProperty2.setter);
    }
    
    /**
     * Issue #1066 - testing case when the conflicting property is actually not the first one but the
     * second
     */
    @Test
    public void testFixUpParentAndInterfaces_2ndproperty_propertyNameSame_getterSetterSame_typeDifferent() {
        AbstractJavaCodegen codegen = new P_AbstractJavaCodegen();

        CodegenModel parentModel = new CodegenModel();
        parentModel.name = "parent_type";
        CodegenModel childModel = new CodegenModel();
        childModel.name = "child_type";
        childModel.parentModel = parentModel;
        parentModel.children = new ArrayList<>();

        CodegenProperty parentValueProperty1 = new CodegenProperty();
        parentValueProperty1.name = "value";
        parentValueProperty1.baseName = "value";
        parentValueProperty1.getter = "getValue";
        parentValueProperty1.setter = "setValue";
        parentValueProperty1.datatype = "value_type";
        CodegenProperty parentValueProperty2 = new CodegenProperty();
        parentValueProperty2.name = "other_value";
        parentValueProperty2.baseName = "other_value";
        parentValueProperty2.getter = "getOtherValue";
        parentValueProperty2.setter = "setOtherValue";
        parentValueProperty2.datatype = "other_type";
        parentModel.vars = new ArrayList<>();
        parentModel.vars.add(parentValueProperty1);
        parentModel.vars.add(parentValueProperty2);

        CodegenProperty childValueProperty1 = new CodegenProperty();
        childValueProperty1.name = "third_value";
        childValueProperty1.baseName = "third_value";
        childValueProperty1.nameInCamelCase = "ThirdValue";
        childValueProperty1.getter = "getThirdValue";
        childValueProperty1.setter = "setThirdValue";
        childValueProperty1.datatype = "other_type";
        CodegenProperty childValueProperty2 = new CodegenProperty();
        childValueProperty2.name = "value"; // same as parent "value"
        childValueProperty2.baseName = "value";
        childValueProperty2.getter = "getValue"; // same as parent "getValue"
        childValueProperty2.setter = "setValue"; // same as parent "setValue"
        childValueProperty2.datatype = "different_type"; // different to parent "value_type"

        childModel.vars = new ArrayList<>();
        childModel.vars.add(childValueProperty1);
        childModel.vars.add(childValueProperty2);

        Map<String, CodegenModel> allModels = new HashMap<>();
        allModels.put(parentModel.name, parentModel);
        allModels.put(childModel.name, childModel);

        codegen.fixUpParentAndInterfaces(childModel, Collections.EMPTY_MAP);
        Assert.assertEquals(childModel.vars.get(1).baseName, "value");
        Assert.assertEquals(childModel.vars.get(1).name, "childTypeValue");
        Assert.assertEquals(childModel.vars.get(1).nameInCamelCase, "ChildTypeValue");
        Assert.assertEquals(childModel.vars.get(1).getter, "getChildTypeValue");
        Assert.assertEquals(childModel.vars.get(1).setter, "setChildTypeValue");

        // unchanged
        Assert.assertEquals(childModel.vars.get(0).baseName, childValueProperty1.baseName);
        Assert.assertEquals(childModel.vars.get(0).name, childValueProperty1.name);
        Assert.assertEquals(childModel.vars.get(0).nameInCamelCase, childValueProperty1.nameInCamelCase);
        Assert.assertEquals(childModel.vars.get(0).getter, childValueProperty1.getter);
        Assert.assertEquals(childModel.vars.get(0).setter, childValueProperty1.setter);
    }    

    public static class P_AbstractJavaCodegen extends AbstractJavaCodegen {
        @Override
        public String getArgumentsLocation() {
            return null;
        }

        @Override
        public String getDefaultTemplateDir() {
            return null;
        }

        @Override
        public CodegenType getTag() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public List<CodegenArgument> readLanguageArguments() {
            return null;
        }
    }
}
