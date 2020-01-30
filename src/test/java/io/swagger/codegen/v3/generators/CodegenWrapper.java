package io.swagger.codegen.v3.generators;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenSchema;
import io.swagger.codegen.v3.ISchemaHandler;
import io.swagger.v3.oas.models.media.Schema;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodegenWrapper {

    private ISchemaHandler schemaHandler;
    private List<CodegenSchema> codegenSchemas;
    private Map<String, CodegenModel> allModels;

    public CodegenWrapper(){}

    public CodegenWrapper(ISchemaHandler schemaHandler){
        this.schemaHandler = schemaHandler;
    }

    public void addCodegenSchema(CodegenModel codegenModel, Schema schema) {
        if (codegenSchemas == null) {
            codegenSchemas = new ArrayList<>();
        }
        codegenSchemas.add(new CodegenSchema(codegenModel, schema));
        addModel(codegenModel);
    }

    public void addModel(CodegenModel codegenModel) {
        if (allModels == null) {
            allModels = new HashMap<>();
        }
        allModels.put(codegenModel.classname, codegenModel);
    }

    public ISchemaHandler getSchemaHandler() {
        return schemaHandler;
    }

    public List<CodegenSchema> getCodegenSchemas() {
        return codegenSchemas;
    }

    public Map<String, CodegenModel> getAllModels() {
        return allModels;
    }
}
