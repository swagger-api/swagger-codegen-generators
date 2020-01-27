package io.swagger.codegen.v3.generators;

import io.swagger.codegen.v3.CodegenType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public abstract class AbstractCodegenTest {

    protected OpenAPI getOpenAPI(String filePath) {
        OpenAPIV3Parser openApiParser = new OpenAPIV3Parser();
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setFlatten(true);
        SwaggerParseResult parseResult = openApiParser.readLocation(filePath, null, options);

        return parseResult.getOpenAPI();
    }

    protected DefaultCodegenConfig createConfig() {
        return new DefaultCodegenConfig() {
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
        };
    }
}
