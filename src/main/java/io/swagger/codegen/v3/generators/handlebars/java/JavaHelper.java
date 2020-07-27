package io.swagger.codegen.v3.generators.handlebars.java;

import com.github.jknack.handlebars.Options;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class JavaHelper {

    public CharSequence getClassDefinition(CodegenModel codegenModel, Options options) throws IOException {
        final Boolean serializableModel = options.get(CodegenConstants.SERIALIZABLE_MODEL) != null ? options.get(CodegenConstants.SERIALIZABLE_MODEL) : Boolean.FALSE;
        final StringBuilder builder = new StringBuilder();
        builder.append(codegenModel.classname);
        if (StringUtils.isNotBlank(codegenModel.parent)) {
            builder.append(StringUtils.SPACE);
            builder.append("extends ");
            builder.append(codegenModel.parent);
        }
        if (serializableModel) {
            builder.append(" implements Serializable");
        }
        return builder.toString();
    }

    public CharSequence getJavaProperty(CodegenProperty codegenProperty, Options options) throws IOException {
        final StringBuilder builder = new StringBuilder();
        if (getBooleanValue(codegenProperty, CodegenConstants.IS_CONTAINER_EXT_NAME)) {
            builder.append(codegenProperty.getDatatypeWithEnum());
            builder.append(StringUtils.SPACE);
            builder.append(codegenProperty.getName());
            builder.append(" = ");
            if (codegenProperty.getRequired()) {
                builder.append(codegenProperty.getDefaultValue());
            } else {
                builder.append("null");
            }
            return builder.toString();
        }
        return String.format("%s %s = %s", codegenProperty.getDatatypeWithEnum(), codegenProperty.getName(), codegenProperty.getDefaultValue());
    }

    public CharSequence getModelImports(Map<String, Object> templateData, Options options) throws IOException {
        if (options == null) {
            return null;
        }
        final List<Map<String, String>> imports = options.get("imports");
        if (imports == null || imports.isEmpty()) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();

        builder.append("import java.util.Objects;\n");

        for (Map<String, String> importMap : imports) {
            builder.append("import ");
            builder.append(importMap.get("import"));
            builder.append(";\n");
        }
        return builder.toString();
    }

    public CharSequence backslash() {
        return "\\";
    }
}
