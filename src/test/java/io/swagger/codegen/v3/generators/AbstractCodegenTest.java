package io.swagger.codegen.v3.generators;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenSchema;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.ISchemaHandler;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.util.List;
import java.util.Map;

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

    protected CodegenWrapper processSchemas(CodegenConfig codegenConfig, OpenAPI openAPI) {
        codegenConfig.preprocessOpenAPI(openAPI);
        final Map<String, Schema> schemaMap = openAPI.getComponents().getSchemas();

        final CodegenWrapper codegenWrapper = new CodegenWrapper(((DefaultCodegenConfig)codegenConfig).getSchemaHandler());
        for (String name : schemaMap.keySet()) {
            final Schema schema = schemaMap.get(name);
            final CodegenModel codegenModel = codegenConfig.fromModel(name, schema, schemaMap);
            codegenWrapper.addCodegenSchema(codegenModel, schema);
        }

        generateComposedObjects(codegenWrapper.getSchemaHandler(),
                codegenWrapper.getCodegenSchemas(),
                codegenWrapper.getAllModels());

        return codegenWrapper;
    }

    protected void generateComposedObjects(ISchemaHandler schemaHandler, List<CodegenSchema> codegenSchemas, Map<String, CodegenModel> allModels) {
        for (CodegenSchema codegenSchema : codegenSchemas) {
            schemaHandler.processComposedSchemas(codegenSchema.getCodegenModel(), codegenSchema.getSchema(), allModels);
        }
    }
}
