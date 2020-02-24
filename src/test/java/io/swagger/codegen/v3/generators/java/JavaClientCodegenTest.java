package io.swagger.codegen.v3.generators.java;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenModelFactory;
import io.swagger.codegen.v3.CodegenModelType;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenSchema;
import io.swagger.codegen.v3.ISchemaHandler;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import io.swagger.codegen.v3.generators.CodegenWrapper;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.SchemaHandler;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class JavaClientCodegenTest extends AbstractCodegenTest {

    @Test
    public void modelInheritanceSupportInGson() throws Exception {
        List allModels = new ArrayList();

        CodegenModel parent1 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        parent1.setName("parent1");
        parent1.setClassname("test.Parent1");

        Map<String, Object> modelMap = new HashMap<>();
        modelMap.put("model", parent1);
        allModels.add(modelMap);

        CodegenModel parent2 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        parent2.setName("parent2");
        parent2.setClassname("test.Parent2");

        modelMap = new HashMap<>();
        modelMap.put("model", parent2);
        allModels.add(modelMap);

        CodegenModel model1 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        model1.setName("model1");
        model1.setClassname("test.Model1");
        model1.setParentModel(parent1);

        modelMap = new HashMap<>();
        modelMap.put("model", model1);
        allModels.add(modelMap);

        CodegenModel model2 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        model2.setName("model2");
        model2.setClassname("test.Model2");
        model2.setParentModel(parent1);

        modelMap = new HashMap<>();
        modelMap.put("model", model2);
        allModels.add(modelMap);

        CodegenModel model3 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        model3.setName("model3");
        model3.setClassname("test.Model3");
        model3.setParentModel(parent1);

        modelMap = new HashMap<>();
        modelMap.put("model", model3);
        allModels.add(modelMap);

        CodegenModel model4 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        model4.setName("model4");
        model4.setClassname("test.Model4");
        model4.setParentModel(parent2);

        modelMap = new HashMap<>();
        modelMap.put("model", model4);
        allModels.add(modelMap);

        CodegenModel model5 = CodegenModelFactory.newInstance(CodegenModelType.MODEL);
        model5.setName("model5");
        model5.setClassname("test.Model5");
        model5.setParentModel(parent2);

        modelMap = new HashMap<>();
        modelMap.put("model", model5);
        allModels.add(modelMap);

        JavaClientCodegen clientCodegen = new JavaClientCodegen();
        List<Map<String, Object>> parentsList = clientCodegen.modelInheritanceSupportInGson(allModels);

        Assert.assertNotNull(parentsList);
        Assert.assertEquals(parentsList.size(), 2);

        Map<String, Object> parent = parentsList.get(0);
        Assert.assertEquals(parent.get("classname"), "test.Parent1");

        List<CodegenModel> children = (List<CodegenModel>) parent.get("children");
        Assert.assertNotNull(children);
        Assert.assertEquals(children.size(), 3);

        Map<String, Object> models = (Map<String, Object>) children.get(0);
        Assert.assertEquals(models.get("name"), "model1");
        Assert.assertEquals(models.get("classname"), "test.Model1");

        models = (Map<String, Object>) children.get(1);
        Assert.assertEquals(models.get("name"), "model2");
        Assert.assertEquals(models.get("classname"), "test.Model2");

        models = (Map<String, Object>) children.get(2);
        Assert.assertEquals(models.get("name"), "model3");
        Assert.assertEquals(models.get("classname"), "test.Model3");


        parent = parentsList.get(1);
        Assert.assertEquals(parent.get("classname"), "test.Parent2");

        children = (List<CodegenModel>) parent.get("children");
        Assert.assertNotNull(children);
        Assert.assertEquals(children.size(), 2);

        models = (Map<String, Object>) children.get(0);
        Assert.assertEquals(models.get("name"), "model4");
        Assert.assertEquals(models.get("classname"), "test.Model4");

        models = (Map<String, Object>) children.get(1);
        Assert.assertEquals(models.get("name"), "model5");
        Assert.assertEquals(models.get("classname"), "test.Model5");
    }

    @Test
    public void arraysInRequestBody() throws Exception {
        final JavaClientCodegen codegen = new JavaClientCodegen();

        RequestBody body1 = new RequestBody();
        body1.setDescription("A list of ids");
        body1.setContent(new Content().addMediaType("application/json", new MediaType().schema(new ArraySchema().items(new StringSchema()))));
        CodegenParameter codegenParameter1 = codegen.fromRequestBody(body1, null, null, new HashMap<String, Schema>(), new HashSet<String>());
        Assert.assertEquals(codegenParameter1.description, "A list of ids");
        Assert.assertEquals(codegenParameter1.dataType, "List<String>");
        Assert.assertEquals(codegenParameter1.baseType, "String");

        RequestBody body2 = new RequestBody();
        body2.setDescription("A list of list of values");
        body2.setContent(new Content().addMediaType("application/json", new MediaType().schema(new ArraySchema().items(new ArraySchema().items(new IntegerSchema())))));
        CodegenParameter codegenParameter2 = codegen.fromRequestBody(body2, null, null, new HashMap<String, Schema>(), new HashSet<String>());
        Assert.assertEquals(codegenParameter2.description, "A list of list of values");
        Assert.assertEquals(codegenParameter2.dataType, "List<List<Integer>>");
        Assert.assertEquals(codegenParameter2.baseType, "List");
        RequestBody body3 = new RequestBody();
        body3.setDescription("A list of points");
        body3.setContent(new Content().addMediaType("application/json", new MediaType().schema(new ArraySchema().items(new ObjectSchema().$ref("#/components/schemas/Point")))));
        ObjectSchema point = new ObjectSchema();
        point.addProperties("message", new StringSchema());
        point.addProperties("x", new IntegerSchema().format(SchemaTypeUtil.INTEGER32_FORMAT));
        point.addProperties("y", new IntegerSchema().format(SchemaTypeUtil.INTEGER32_FORMAT));
        CodegenParameter codegenParameter3 = codegen.fromRequestBody(body3, null, null, Collections.<String, Schema>singletonMap("Point", point), new HashSet<String>());
        Assert.assertEquals(codegenParameter3.description, "A list of points");
        Assert.assertEquals(codegenParameter3.dataType, "List<Point>");
        Assert.assertEquals(codegenParameter3.baseType, "Point");
    }

    @Test
    public void nullValuesInComposedSchema() throws Exception {
        final JavaClientCodegen codegen = new JavaClientCodegen();
        CodegenModel result = codegen.fromModel("CompSche",
            new ComposedSchema());
        Assert.assertEquals(result.name, "CompSche");
    }

    @Test
    public void testInitialPackageNamesValues() throws Exception {
        final JavaClientCodegen codegen = new JavaClientCodegen();
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "io.swagger.client.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "io.swagger.client.model");
        Assert.assertEquals(codegen.apiPackage(), "io.swagger.client.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "io.swagger.client.api");
        Assert.assertEquals(codegen.invokerPackage, "io.swagger.client");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "io.swagger.client");
    }

    @Test
    public void testPackageNamesSetWithSetters() throws Exception {
        final JavaClientCodegen codegen = new JavaClientCodegen();
        codegen.setModelPackage("xxx.yyyyy.zzzzzzz.model");
        codegen.setApiPackage("xxx.yyyyy.zzzzzzz.api");
        codegen.setInvokerPackage("xxx.yyyyy.zzzzzzz.invoker");
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "xxx.yyyyy.zzzzzzz.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xxx.yyyyy.zzzzzzz.model");
        Assert.assertEquals(codegen.apiPackage(), "xxx.yyyyy.zzzzzzz.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xxx.yyyyy.zzzzzzz.api");
        Assert.assertEquals(codegen.invokerPackage, "xxx.yyyyy.zzzzzzz.invoker");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "xxx.yyyyy.zzzzzzz.invoker");
    }

    @Test
    public void testPackageNamesSetWithAdditionalProperties() throws Exception {
        final JavaClientCodegen codegen = new JavaClientCodegen();
        codegen.additionalProperties().put(CodegenConstants.MODEL_PACKAGE, "xxx.yyyyy.zzzzzzz.mmmmm.model");
        codegen.additionalProperties().put(CodegenConstants.API_PACKAGE, "xxx.yyyyy.zzzzzzz.aaaaa.api");
        codegen.additionalProperties().put(CodegenConstants.INVOKER_PACKAGE, "xxx.yyyyy.zzzzzzz.iiii.invoker");
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "xxx.yyyyy.zzzzzzz.mmmmm.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xxx.yyyyy.zzzzzzz.mmmmm.model");
        Assert.assertEquals(codegen.apiPackage(), "xxx.yyyyy.zzzzzzz.aaaaa.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xxx.yyyyy.zzzzzzz.aaaaa.api");
        Assert.assertEquals(codegen.invokerPackage, "xxx.yyyyy.zzzzzzz.iiii.invoker");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "xxx.yyyyy.zzzzzzz.iiii.invoker");
    }

    @Test
    public void testPackageNamesSetInvokerDerivedFromApi() throws Exception {
        final JavaClientCodegen codegen = new JavaClientCodegen();
        codegen.additionalProperties().put(CodegenConstants.MODEL_PACKAGE, "xxx.yyyyy.zzzzzzz.mmmmm.model");
        codegen.additionalProperties().put(CodegenConstants.API_PACKAGE, "xxx.yyyyy.zzzzzzz.aaaaa.api");
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "xxx.yyyyy.zzzzzzz.mmmmm.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xxx.yyyyy.zzzzzzz.mmmmm.model");
        Assert.assertEquals(codegen.apiPackage(), "xxx.yyyyy.zzzzzzz.aaaaa.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xxx.yyyyy.zzzzzzz.aaaaa.api");
        Assert.assertEquals(codegen.invokerPackage, "xxx.yyyyy.zzzzzzz.aaaaa");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "xxx.yyyyy.zzzzzzz.aaaaa");
    }

    @Test
    public void testPackageNamesSetInvokerDerivedFromModel() throws Exception {
        final JavaClientCodegen codegen = new JavaClientCodegen();
        codegen.additionalProperties().put(CodegenConstants.MODEL_PACKAGE, "xxx.yyyyy.zzzzzzz.mmmmm.model");
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage(), "xxx.yyyyy.zzzzzzz.mmmmm.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xxx.yyyyy.zzzzzzz.mmmmm.model");
        Assert.assertEquals(codegen.apiPackage(), "io.swagger.client.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "io.swagger.client.api");
        Assert.assertEquals(codegen.invokerPackage, "xxx.yyyyy.zzzzzzz.mmmmm");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.INVOKER_PACKAGE), "xxx.yyyyy.zzzzzzz.mmmmm");
    }

    @Test
    public void customTemplates() throws Exception {
        final JavaClientCodegen codegen = new JavaClientCodegen();
        codegen.processOpts();
        Assert.assertEquals(codegen.templateDir(), "handlebars" + File.separator + "Java");

        codegen.additionalProperties().put(CodegenConstants.TEMPLATE_DIR, String.join(File.separator, "user", "custom", "location"));
        codegen.processOpts();
        Assert.assertEquals(codegen.templateDir(), String.join(File.separator, "user", "custom", "location"));
    }

    @Test
    public void testModelNamedFile() {
        final OpenAPI openAPI = getOpenAPI("3_0_0/model_named_file.yaml");
        final DefaultCodegenConfig config = new JavaClientCodegen();
        config.setIgnoreImportMapping(true);
        config.preprocessOpenAPI(openAPI);

        final Schema modelFile = openAPI.getComponents().getSchemas().get("File");
        final Schema modelSetting = openAPI.getComponents().getSchemas().get("Setting");

        final CodegenModel codegenModelFile = config.fromModel("File", modelFile, openAPI.getComponents().getSchemas());
        final CodegenModel codegenModelSetting = config.fromModel("Setting", modelSetting, openAPI.getComponents().getSchemas());

        Assert.assertEquals(codegenModelFile.name, "File");
        Assert.assertEquals(codegenModelSetting.name, "Setting");

        final List<CodegenProperty> codegenProperties = codegenModelSetting.getVars();

        Assert.assertEquals(codegenProperties.size(), 4);

        CodegenProperty fileProperty = codegenProperties.stream().filter(property -> property.name.equals("file")).findAny().get();

        Assert.assertEquals(fileProperty.name, "file");
        Assert.assertEquals(fileProperty.baseType, "File");
        Assert.assertEquals(fileProperty.datatype, "File");

        CodegenProperty documentProperty = codegenProperties.stream().filter(property -> property.name.equals("document")).findAny().get();

        Assert.assertEquals(documentProperty.name, "document");
        Assert.assertEquals(documentProperty.baseType, "File");
        Assert.assertEquals(documentProperty.datatype, "java.io.File");

        Assert.assertFalse(codegenModelSetting.imports.stream().anyMatch(_import -> _import.equals("java.io.File")));
    }

    @Test
    public void checkOneOfModelCreation() {
        final OpenAPI openAPI = getOpenAPI("3_0_0/composed_schemas.yaml");
        final CodegenConfig config = new JavaClientCodegen();
        final CodegenWrapper codegenWrapper = processSchemas(config, openAPI);

        CodegenModel codegenModel = codegenWrapper.getAllModels().get("PartMaster");

        boolean hasOneOfProperty = codegenModel.getVars()
            .stream()
            .anyMatch(codegenProperty -> codegenProperty.datatype.equals("OneOfPartMasterDestination"));

        Assert.assertTrue(hasOneOfProperty);

        hasOneOfProperty = codegenModel.getVars()
            .stream()
            .anyMatch(codegenProperty -> codegenProperty.datatype.equals("OneOfPartMasterOrigin"));

        Assert.assertTrue(hasOneOfProperty);

        final ISchemaHandler schemaHandler = codegenWrapper.getSchemaHandler();

        boolean hasComposedModel = schemaHandler.getModels()
                .stream()
                .anyMatch(model -> model.name.equals("OneOfPartMasterDestination"));

        Assert.assertTrue(hasComposedModel);

        hasComposedModel = schemaHandler.getModels()
                .stream()
                .anyMatch(model -> model.name.equals("OneOfPartMasterOrigin"));

        Assert.assertTrue(hasComposedModel);
    }
}
