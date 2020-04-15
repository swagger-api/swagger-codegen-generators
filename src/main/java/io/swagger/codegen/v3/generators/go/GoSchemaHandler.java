package io.swagger.codegen.v3.generators.go;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.SchemaHandler;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class GoSchemaHandler extends SchemaHandler {

    public GoSchemaHandler(DefaultCodegenConfig codegenConfig) {
        super(codegenConfig);
    }

    protected void addInterfaces(List<Schema> schemas, CodegenModel composedModel, Map<String, CodegenModel> allModels) {
        for (Schema interfaceSchema : schemas) {
            final String ref = interfaceSchema.get$ref();
            if (StringUtils.isBlank(ref)) {
                continue;
            }
            final String schemaName = ref.substring(ref.lastIndexOf("/") + 1);
            this.addInterfaceModel(composedModel, allModels.get(codegenConfig.toModelName(schemaName)));
        }
    }
}
