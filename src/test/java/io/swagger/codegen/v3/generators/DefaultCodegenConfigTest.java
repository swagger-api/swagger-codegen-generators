package io.swagger.codegen.v3.generators;

import io.swagger.codegen.v3.CodegenArgument;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenResponse;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.generators.java.JavaClientCodegen;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DefaultCodegenConfigTest {

    @Test
    public void testInitialValues() throws Exception {
        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage, "");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), null);
        Assert.assertEquals(codegen.apiPackage, "");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), null);
        Assert.assertEquals(codegen.sortParamsByRequiredFlag, Boolean.TRUE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.TRUE);
        Assert.assertEquals(codegen.hideGenerationTimestamp, Boolean.TRUE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.TRUE);
    }

    @Test
    public void testSetters() throws Exception {
        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.setModelPackage("xxx.yyyyy.zzzzzzz.model");
        codegen.setApiPackage("xxx.yyyyy.zzzzzzz.api");
        codegen.setSortParamsByRequiredFlag(false);
        codegen.setHideGenerationTimestamp(false);
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage, "xxx.yyyyy.zzzzzzz.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xxx.yyyyy.zzzzzzz.model");
        Assert.assertEquals(codegen.apiPackage, "xxx.yyyyy.zzzzzzz.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xxx.yyyyy.zzzzzzz.api");
        Assert.assertEquals(codegen.sortParamsByRequiredFlag, Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.FALSE);
        Assert.assertEquals(codegen.hideGenerationTimestamp, Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
    }

    @Test
    public void testPutAdditionalProperties() throws Exception {
        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.additionalProperties().put(CodegenConstants.MODEL_PACKAGE, "xx.yyyyy.model");
        codegen.additionalProperties().put(CodegenConstants.API_PACKAGE, "xx.yyyyy.api");
        codegen.additionalProperties().put(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG, false);
        codegen.additionalProperties().put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, false);
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage, "xx.yyyyy.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xx.yyyyy.model");
        Assert.assertEquals(codegen.apiPackage, "xx.yyyyy.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xx.yyyyy.api");
        Assert.assertEquals(codegen.sortParamsByRequiredFlag, Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.FALSE);
        Assert.assertEquals(codegen.hideGenerationTimestamp, Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
    }

    @Test
    public void testNumberSchemaMinMax() {
        Schema schema = new NumberSchema()
                .minimum(BigDecimal.valueOf(50))
                .maximum(BigDecimal.valueOf(1000));

        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        CodegenProperty codegenProperty = codegen.fromProperty("test", schema);

        Assert.assertEquals(codegenProperty.minimum, "50");
        Assert.assertEquals(codegenProperty.maximum, "1000");
    }

    @Test
    public void testFromOperation_BodyParamsUnique() {
        PathItem dummyPath = new PathItem()
            .post(new Operation())
            .get(new Operation());
      
        OpenAPI openAPI = new OpenAPI()
            .path("dummy", dummyPath);

        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.setEnsureUniqueParams(false);
        final Operation operation = new Operation();

        RequestBody body = new RequestBody();
        body.setDescription("A list of list of values");
        body.setContent(new Content().addMediaType("application/json", new MediaType().schema(new ArraySchema().items(new ArraySchema().items(new IntegerSchema())))));
        operation.setRequestBody(body);
        Parameter param = new Parameter().in("query").name("testParameter");
        operation.addParametersItem(param);
        
        CodegenOperation codegenOperation = codegen.fromOperation("/path", "GET", operation, null, openAPI);

        Assert.assertEquals(true, codegenOperation.allParams.get(0).getVendorExtensions().get("x-has-more"));
        Assert.assertEquals(false, codegenOperation.bodyParams.get(0).getVendorExtensions().get("x-has-more"));

        codegenOperation.allParams.get(0).getVendorExtensions().put("x-has-more", false);
        codegenOperation.bodyParams.get(0).getVendorExtensions().put("x-has-more", true);

        Assert.assertEquals(false, codegenOperation.allParams.get(0).getVendorExtensions().get("x-has-more"));
        Assert.assertEquals(true, codegenOperation.bodyParams.get(0).getVendorExtensions().get("x-has-more"));
    }

    @Test(dataProvider = "testGetCollectionFormatProvider")
    public void testGetCollectionFormat(Parameter.StyleEnum style, Boolean explode, String expectedCollectionFormat) {
        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        
        ArraySchema paramSchema = new ArraySchema()
                .items(new IntegerSchema());
        Parameter param = new Parameter()
                .in("query")
                .name("testParameter")
                .schema(paramSchema)
                .style(style)
                .explode(explode);
        
        CodegenParameter codegenParameter = codegen.fromParameter(param, new HashSet<>());
        
        Assert.assertEquals(codegenParameter.collectionFormat, expectedCollectionFormat);
    }
    
    @DataProvider(name = "testGetCollectionFormatProvider")
    public Object[][] provideData_testGetCollectionFormat() {
        // See: https://swagger.io/docs/specification/serialization/#query
        return new Object[][] {
            { null,                                 null,           "multi" },
            { Parameter.StyleEnum.FORM,             null,           "multi" },
            { null,                                 Boolean.TRUE,   "multi" },
            { Parameter.StyleEnum.FORM,             Boolean.TRUE,   "multi" },
            
            { null,                                 Boolean.FALSE,  "csv" },
            { Parameter.StyleEnum.FORM,             Boolean.FALSE,  "csv" },
            
            { Parameter.StyleEnum.SPACEDELIMITED,   Boolean.TRUE,   "multi" },
            { Parameter.StyleEnum.SPACEDELIMITED,   Boolean.FALSE,  "space" },
            { Parameter.StyleEnum.SPACEDELIMITED,   null,           "multi" },
            
            { Parameter.StyleEnum.PIPEDELIMITED,    Boolean.TRUE,   "multi" },
            { Parameter.StyleEnum.PIPEDELIMITED,    Boolean.FALSE,  "pipe" },
            { Parameter.StyleEnum.PIPEDELIMITED,    null,           "multi" },
        };
    }
    
    /**
     * Tests that {@link DefaultCodegenConfig#fromOperation(String, String, Operation, java.util.Map, OpenAPI)} correctly
     * resolves the consumes list when the request body is specified via reference rather than inline.
     */
    @Test
    public void testRequestBodyRefConsumesList() {
        final OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/resources/3_0_0/requestBodyRefTest.json");
        final P_DefaultCodegenConfig codegen = new P_DefaultCodegenConfig(); 
        final String path = "/test/requestBodyRefTest";
        final Operation op = openAPI.getPaths().get(path).getPost();
        final CodegenOperation codegenOp = codegen.fromOperation(path, "post", op, openAPI.getComponents().getSchemas(), openAPI);

        Assert.assertTrue(codegenOp.getHasConsumes());
        Assert.assertNotNull(codegenOp.consumes);
        Assert.assertEquals(codegenOp.consumes.size(), 2);
        Assert.assertEquals(codegenOp.consumes.get(0).get("mediaType"), "application/json");
        Assert.assertEquals(codegenOp.consumes.get(1).get("mediaType"), "application/xml");
    }

    /**
     * Tests when a 'application/x-www-form-urlencoded' request body is marked as required that all form
     * params are also marked as required.
     * 
     * @see #testOptionalFormParams()
     */
    @Test
    public void testRequiredFormParams() {
        // Setup
        final P_DefaultCodegenConfig codegen = new P_DefaultCodegenConfig(); 

        final OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/resources/3_0_0/requiredFormParamsTest.yaml");
        final String path = "/test_required";
        
        final Operation op = openAPI.getPaths().get(path).getPost();
        Assert.assertNotNull(op);
        
        // Test
        final CodegenOperation codegenOp = codegen.fromOperation(path, "post", op, openAPI.getComponents().getSchemas(), openAPI);
        
        // Verification
        List<CodegenParameter> formParams = codegenOp.getFormParams();
        Assert.assertNotNull(formParams);
        Assert.assertEquals(formParams.size(), 2);
        
        for (CodegenParameter formParam : formParams) {
            Assert.assertTrue(formParam.getRequired(), "Form param '" + formParam.getParamName() + "' is not required.");
        }

        // Required params must be updated as well.
        List<CodegenParameter> requiredParams = codegenOp.getRequiredParams();
        Assert.assertNotNull(requiredParams);
        Assert.assertEquals(requiredParams.size(), 2);
        requiredParams.get(0).getParamName().equals("id");
        requiredParams.get(1).getParamName().equals("name");
    }

    /**
     * Tests when a 'application/x-www-form-urlencoded' request body is marked as optional that all form
     * params are also marked as optional.
     * 
     * @see #testRequiredFormParams()
     */
    @Test
    public void testOptionalFormParams() {
        // Setup
        final P_DefaultCodegenConfig codegen = new P_DefaultCodegenConfig(); 

        final OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/resources/3_0_0/requiredFormParamsTest.yaml");
        final String path = "/test_optional";
        
        final Operation op = openAPI.getPaths().get(path).getPost();
        Assert.assertNotNull(op);
        
        // Test
        final CodegenOperation codegenOp = codegen.fromOperation(path, "post", op, openAPI.getComponents().getSchemas(), openAPI);
        
        // Verification
        List<CodegenParameter> formParams = codegenOp.getFormParams();
        Assert.assertNotNull(formParams);
        Assert.assertEquals(formParams.size(), 2);
        
        for (CodegenParameter formParam : formParams) {
            Assert.assertFalse(formParam.getRequired(), "Form param '" + formParam.getParamName() + "' is required.");
        }

        // Required params must be updated as well.
        List<CodegenParameter> requiredParams = codegenOp.getRequiredParams();
        Assert.assertTrue(requiredParams == null || requiredParams.size() == 0);
    }

    @Test
    public void testFromResponse_inlineHeaders() {
        final String RESPONSE_CODE = "200";

        ApiResponse apiResponse = new ApiResponse();
        Header inlineHeader = new Header().description("This is header1").schema(new Schema().type("string").example("header_val"));
        apiResponse.addHeaderObject("header1", inlineHeader);

        OpenAPI openAPI = new OpenAPI().components(new Components().responses(new HashMap<>()));
        openAPI.getComponents().addHeaders("ref-header1", inlineHeader);

        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.preprocessOpenAPI(openAPI);
        CodegenResponse codegenResponse = codegen.fromResponse(RESPONSE_CODE, apiResponse);

        Assert.assertEquals(codegenResponse.code, RESPONSE_CODE);

        CodegenProperty headerProperty = codegenResponse.headers.get(0);
        Assert.assertNotNull(headerProperty);
        Assert.assertEquals(headerProperty.description, inlineHeader.getSchema().getDescription());
        Assert.assertEquals(headerProperty.datatype, "String");
        Assert.assertEquals(headerProperty.example, inlineHeader.getSchema().getExample());
    }

    @Test
    public void testFromResponse_referenceHeaders() {
        final String RESPONSE_CODE = "200";

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.addHeaderObject("header1", new Header().$ref("#/components/ref-header1"));

        OpenAPI openAPI = new OpenAPI().components(new Components().responses(new HashMap<>()));
        Header referencedHeader = new Header().schema(new Schema().description("This is header1").type("string").example("header_val"));
        openAPI.getComponents().addHeaders("ref-header1", referencedHeader);

        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.preprocessOpenAPI(openAPI);
        CodegenResponse codegenResponse = codegen.fromResponse(RESPONSE_CODE, apiResponse);

        Assert.assertEquals(codegenResponse.code, RESPONSE_CODE);

        CodegenProperty headerProperty = codegenResponse.headers.get(0);
        Assert.assertNotNull(headerProperty);
        Assert.assertEquals(headerProperty.description, referencedHeader.getSchema().getDescription());
        Assert.assertEquals(headerProperty.datatype, "String");
        Assert.assertEquals(headerProperty.example, referencedHeader.getSchema().getExample());
    }
    
    
    @Test
    public void customTemplates_embeddedTemplateDir_must_be_init_to_getTemplateDir() throws Exception {
        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.additionalProperties().put(CodegenConstants.TEMPLATE_DIR, String.join(File.separator, "user", "custom", "location"));
        codegen.processOpts();
        Assert.assertEquals(codegen.embeddedTemplateDir(), codegen.getTemplateDir());
    }

    @Test(dataProvider = "testCommonPrefixProvider")
    public void testCommonPrefix(List<Object> vars, String expectedPrefix) {
        DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        Assert.assertEquals(codegen.findCommonPrefixOfVars(vars), expectedPrefix);
    }

    @DataProvider(name = "testCommonPrefixProvider")
    public Object[][] provideData_testCommonPrefix() {
        return new Object[][]{
            {Collections.singletonList("FOO_BAR"), ""},
            {Arrays.asList("FOO_BAR", "FOO_BAZ"), "FOO_"},
            {Arrays.asList("FOO_BAR", "FOO_BAZ", "TEST"), ""},
            {Arrays.asList("STATUS-ON", "STATUS-OFF", "STATUS"), ""}
        };
    }

    private static class P_DefaultCodegenConfig extends DefaultCodegenConfig{
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
