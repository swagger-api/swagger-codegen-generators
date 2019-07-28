package io.swagger.codegen.v3.generators.examples;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ExampleGeneratorTest {

    private OpenAPI openAPI;

    @BeforeClass
    public void setUp() throws Exception {
        final String content = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("3_0_0/petstore.yaml").getFile()));
        final ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setFlatten(true);
        final SwaggerParseResult result = new OpenAPIParser().readContents(content, null, options);
        this.openAPI = result.getOpenAPI();
    }

    @Test
    public void testExampleFromSchema() throws Exception {
        final Schema petSchema = openAPI.getComponents().getSchemas().get("Pet");
        final ExampleGenerator exampleGenerator = new ExampleGenerator(openAPI);

        final List<Map<String, String>> exampleList = exampleGenerator.generate(null, null, petSchema);
        Map<String, String> exampleMap = exampleList.get(0);

        String example = exampleMap.get("example");
        Assert.assertNotNull(example);
        Assert.assertTrue(example.contains("\"name\" : \"doggie\""));
    }

    @Test
    public void testExampleWithRecursiveNodes() throws Exception {
        final Schema categorySchema = openAPI.getComponents().getSchemas().get("Category");
        final ExampleGenerator exampleGenerator = new ExampleGenerator(openAPI);

        final List<Map<String, String>> exampleList = exampleGenerator.generate(null, null, categorySchema);
        Assert.assertEquals(exampleList.size(), 1);
        final Map<String, String> example = exampleList.get(0);
        Assert.assertEquals(example.get("contentType"), "application/json");
        Assert.assertTrue(example.get("example").contains("\"name\" : \"Yinotheria\""));
    }
}
