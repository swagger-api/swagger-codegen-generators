package io.swagger.codegen.v3.generators;

import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenContent;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.generators.util.OpenAPIUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OperationParameters {

    private CodegenParameter bodyParam = null;
    private List<CodegenParameter> allParams = new ArrayList<>();
    private List<CodegenParameter> bodyParams = new ArrayList<>();
    private List<CodegenParameter> pathParams = new ArrayList<>();
    private List<CodegenParameter> queryParams = new ArrayList<>();
    private List<CodegenParameter> headerParams = new ArrayList<>();
    private List<CodegenParameter> cookieParams = new ArrayList<>();
    private List<CodegenParameter> formParams = new ArrayList<>();
    private List<CodegenParameter> requiredParams = new ArrayList<>();
    private List<CodegenContent> codegenContents = new ArrayList<>();

    public void setBodyParam(CodegenParameter bodyParam) {
        this.bodyParam = bodyParam;
    }

    public CodegenParameter getBodyParam() {
        return bodyParam;
    }

    public List<CodegenParameter> getAllParams() {
        return allParams;
    }

    public List<CodegenParameter> getBodyParams() {
        return bodyParams;
    }

    public List<CodegenParameter> getPathParams() {
        return pathParams;
    }

    public List<CodegenParameter> getQueryParams() {
        return queryParams;
    }

    public List<CodegenParameter> getHeaderParams() {
        return headerParams;
    }

    public List<CodegenParameter> getCookieParams() {
        return cookieParams;
    }

    public List<CodegenParameter> getFormParams() {
        return formParams;
    }

    public List<CodegenParameter> getRequiredParams() {
        return requiredParams;
    }

    public List<CodegenContent> getCodegenContents() {
        return codegenContents;
    }

    public void addAllParams(CodegenParameter codegenParameter) {
        allParams.add(codegenParameter);
    }

    public void addBodyParams(CodegenParameter codegenParameter) {
        bodyParams.add(codegenParameter);
    }

    public void addPathParams(CodegenParameter codegenParameter) {
        pathParams.add(codegenParameter);
    }

    public void addQueryParams(CodegenParameter codegenParameter) {
        queryParams.add(codegenParameter);
    }

    public void addHeaderParams(CodegenParameter codegenParameter) {
        headerParams.add(codegenParameter);
    }

    public void addCookieParams(CodegenParameter codegenParameter) {
        cookieParams.add(codegenParameter);
    }

    public void addFormParam(CodegenParameter codegenParameter) {
        formParams.add(codegenParameter);
    }

    public void addRequiredParam(CodegenParameter codegenParameter) {
        requiredParams.add(codegenParameter);
    }

    public void addCodegenContents(CodegenContent codegenContent) {
        codegenContents.add(codegenContent);
    }

    public void addParameters(Parameter parameter, CodegenParameter codegenParameter) {
        allParams.add(codegenParameter);

        if (parameter instanceof QueryParameter || "query".equalsIgnoreCase(parameter.getIn())) {
            queryParams.add(codegenParameter.copy());
        } else if (parameter instanceof PathParameter || "path".equalsIgnoreCase(parameter.getIn())) {
            pathParams.add(codegenParameter.copy());
        } else if (parameter instanceof HeaderParameter || "header".equalsIgnoreCase(parameter.getIn())) {
            headerParams.add(codegenParameter.copy());
        } else if (parameter instanceof CookieParameter || "cookie".equalsIgnoreCase(parameter.getIn())) {
            cookieParams.add(codegenParameter.copy());
        }
        if (codegenParameter.required) {
            requiredParams.add(codegenParameter.copy());
        }
    }

    public void addHasMore(CodegenOperation codegenOperation) {
        codegenOperation.allParams = addHasMore(allParams);
        codegenOperation.bodyParams = addHasMore(bodyParams);
        codegenOperation.pathParams = addHasMore(pathParams);
        codegenOperation.queryParams = addHasMore(queryParams);
        codegenOperation.headerParams = addHasMore(headerParams);
        codegenOperation.cookieParams = addHasMore(cookieParams);
        codegenOperation.formParams = addHasMore(formParams);
        codegenOperation.requiredParams = addHasMore(requiredParams);
    }

    public void sortRequiredAllParams() {
        Collections.sort(allParams, (one, another) -> {
            if (one.required == another.required) {
                return 0;
            } else if (one.required) {
                return -1;
            } else {
                return 1;
            }
        });
    }

    public void parseNestedObjects(String name, Schema schema, Set<String> imports, DefaultCodegenConfig codegenConfig, OpenAPI openAPI) {
        schema = OpenAPIUtil.getRefSchemaIfExists(schema, openAPI);
        if (schema == null || !isObjectWithProperties(schema)) {
            return;
        }
        final Map<String, Schema> properties = schema.getProperties();
        for (String key : properties.keySet()) {
            Schema property = properties.get(key);
            property = OpenAPIUtil.getRefSchemaIfExists(property, openAPI);
            boolean required;
            if (schema.getRequired() == null || schema.getRequired().isEmpty()) {
                required = false;
            } else {
                required = schema.getRequired().stream().anyMatch(propertyName -> key.equalsIgnoreCase(propertyName.toString()));
            }
            final String parameterName;
            if (property instanceof ArraySchema) {
                parameterName = String.format("%s[%s][]", name, key);
            } else {
                parameterName = String.format("%s[%s]", name, key);
            }
            if (isObjectWithProperties(property)) {
                parseNestedObjects(parameterName, property, imports, codegenConfig, openAPI);
                continue;
            }
            final Parameter queryParameter = new QueryParameter()
                .name(parameterName)
                .required(required)
                .schema(property);
            final CodegenParameter codegenParameter = codegenConfig.fromParameter(queryParameter, imports);
            addParameters(queryParameter, codegenParameter);
        }
    }

    public static List<CodegenParameter> addHasMore(List<CodegenParameter> codegenParameters) {
        if (codegenParameters == null || codegenParameters.isEmpty()) {
            return codegenParameters;
        }
        for (int i = 0; i < codegenParameters.size(); i++) {
            codegenParameters.get(i).secondaryParam = i > 0;
            codegenParameters.get(i).getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, i < codegenParameters.size() - 1);
        }
        return codegenParameters;
    }

    private boolean isObjectWithProperties(Schema schema) {
        return ("object".equalsIgnoreCase(schema.getType()) || schema.getType() == null)
                && schema.getProperties() != null
                && !schema.getProperties().isEmpty();
    }
}
