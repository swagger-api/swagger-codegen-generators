package io.swagger.codegen.v3.generators.ruby;

import io.swagger.codegen.v3.*;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests for RubyClientCodegen-generated templates
 */
public class RubyClientCodegenTest {

  private TemporaryFolder folder = null;

  @BeforeMethod
  public void setUp() throws Exception {
      folder = new TemporaryFolder();
      folder.create();
  }

  @AfterMethod
  public void tearDown() {
      folder.delete();
  }

  @Test
  public void testGenerateRubyClientWithHtmlEntity() throws Exception {
      final File output = folder.getRoot();

      final OpenAPI openAPI = new OpenAPIParser()
              .readLocation("src/test/resources/3_0_0/requiredFormParamsTest.yaml", null, null)
              .getOpenAPI();
      CodegenConfig codegenConfig = new RubyClientCodegen();
      codegenConfig.setOutputDir(output.getAbsolutePath());

      ClientOptInput clientOptInput = new ClientOptInput()
              .opts(new ClientOpts())
              .openAPI(openAPI)
              .config(codegenConfig);

      DefaultGenerator generator = new DefaultGenerator();
      List<File> files = generator.opts(clientOptInput).generate();
      boolean apiFileGenerated = false;
      for (File file : files) {
        if (file.getName().equals("default_api.rb")) {
          apiFileGenerated = true;
          // Ruby client should set the path unescaped in the api file
          assertTrue(FileUtils.readFileToString(file, StandardCharsets.UTF_8).contains("local_var_path = '/test_optional'"));
        }
      }
      if (!apiFileGenerated) {
        fail("Default api file is not generated!");
      }
  }

  @Test
  public void testInitialConfigValues() throws Exception {
      final RubyClientCodegen codegen = new RubyClientCodegen();
      codegen.processOpts();

      Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.TRUE);
      Assert.assertTrue(codegen.getHideGenerationTimestamp());
      Assert.assertEquals(codegen.modelPackage(), "models");
      Assert.assertEquals(codegen.apiPackage(), "api");
  }

  @Test
  public void testSettersForConfigValues() throws Exception {
      final RubyClientCodegen codegen = new RubyClientCodegen();
      codegen.setHideGenerationTimestamp(false);
      codegen.processOpts();

      Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
      Assert.assertFalse(codegen.getHideGenerationTimestamp());
  }

  @Test
  public void testAdditionalPropertiesPutForConfigValues() throws Exception {
      final RubyClientCodegen codegen = new RubyClientCodegen();
      codegen.additionalProperties().put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, false);
      codegen.additionalProperties().put(CodegenConstants.MODEL_PACKAGE, "ruby-models");
      codegen.additionalProperties().put(CodegenConstants.API_PACKAGE, "ruby-api");
      codegen.processOpts();

      Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
      Assert.assertFalse(codegen.getHideGenerationTimestamp());
      Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "ruby-models");
      Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "ruby-api");
  }
}
