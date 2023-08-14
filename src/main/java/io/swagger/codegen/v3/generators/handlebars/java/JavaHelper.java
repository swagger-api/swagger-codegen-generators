package io.swagger.codegen.v3.generators.handlebars.java;

import com.github.jknack.handlebars.Options;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.java.JavaClientCodegen;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class JavaHelper {

    public CharSequence getClassDefinition(CodegenModel codegenModel, Options options) throws IOException {
        final Boolean serializableModel = options.get(CodegenConstants.SERIALIZABLE_MODEL) != null ? options.get(CodegenConstants.SERIALIZABLE_MODEL) : Boolean.FALSE;
        final Boolean parceableModel = options.get(JavaClientCodegen.PARCELABLE_MODEL) != null ? options.get(JavaClientCodegen.PARCELABLE_MODEL) : Boolean.FALSE;
        final StringBuilder builder = new StringBuilder();
        builder.append(codegenModel.classname);
        if (StringUtils.isNotBlank(codegenModel.parent)) {
            builder.append(StringUtils.SPACE);
            builder.append("extends ");
            builder.append(codegenModel.parent);
        }
        if (parceableModel && serializableModel) {
            builder.append(" implements Parcelable, Serializable");
        } else {
            if (serializableModel) {
                builder.append(" implements Serializable");
            }
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

        boolean supportJava6 = Boolean.valueOf(String.valueOf(templateData.get("supportJava6")));
        if (supportJava6) {
            builder.append("import org.apache.commons.lang3.ObjectUtils;\n");
        } else {
            builder.append("import java.util.Objects;\n");
        }

        for (Map<String, String> importMap : imports) {
            builder.append("import ");
            builder.append(importMap.get("import"));
            builder.append(";\n");
        }
        boolean serializableMode = Boolean.valueOf(String.valueOf(templateData.get("serializableModel")));
        boolean jackson = Boolean.valueOf(String.valueOf(templateData.get("jackson")));
        boolean withXml = Boolean.valueOf(String.valueOf(templateData.get("withXml")));
        boolean parcelableModel = Boolean.valueOf(String.valueOf(templateData.get("parcelableModel")));
        boolean useBeanValidation = Boolean.valueOf(String.valueOf(templateData.get("useBeanValidation")));
        boolean jakarta = Boolean.valueOf(String.valueOf(templateData.get("jakarta")));
        if (serializableMode) {
            builder.append("import java.io.Serializable;\n");
        }
        if (jackson && withXml) {
            builder.append("import com.fasterxml.jackson.dataformat.xml.annotation.*;\n");
        }
        if (withXml && jakarta) {
            builder.append("import jakarta.xml.bind.annotation.*;\n");
        } else if (withXml) {
            builder.append("import javax.xml.bind.annotation.*;\n");
        }
        if (parcelableModel) {
            builder.append("import android.os.Parcelable;\n");
            builder.append("import android.os.Parcel;\n");
        }
        if (useBeanValidation && jakarta) {
            builder.append("import jakarta.validation.constraints.*;\n");
            builder.append("import jakarta.validation.Valid;\n");
        } else if (useBeanValidation) {
            builder.append("import javax.validation.constraints.*;\n");
            builder.append("import javax.validation.Valid;\n");
        }
        return builder.toString();
    }

    public CharSequence getXmlAttributeName(String xmlName, String baseName) {
        if (StringUtils.isNotBlank(xmlName)) {
            return xmlName;
        }
        return baseName;
    }

    public CharSequence getXmlElementName(String xmlNamespace, String  xmlName, String baseName) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(xmlNamespace)) {
            builder.append("namespace=\"");
            builder.append(xmlNamespace);
            builder.append("\", ");
        }
        builder.append("name=\"");
        if (StringUtils.isNotBlank(xmlName)) {
            builder.append(xmlName);
        } else {
            builder.append(baseName);
        }
        builder.append("\"");
        return builder.toString();
    }

    public CharSequence getJacksonXmlProperty(CodegenProperty codegenProperty) {
        final boolean isXmlAttribute = getBooleanValue(codegenProperty, CodegenConstants.IS_XML_ATTRIBUTE_EXT_NAME);
        StringBuilder builder = new StringBuilder();
        if (isXmlAttribute) {
            builder.append("isAttribute = true, ");
        }
        if (StringUtils.isNotBlank(codegenProperty.getXmlNamespace())) {
            builder.append("namespace=\"");
            builder.append(codegenProperty.getXmlNamespace());
            builder.append("\", ");
        }
        builder.append("localName = \"");
        if (StringUtils.isNotBlank(codegenProperty.getXmlName())) {
            builder.append(codegenProperty.getXmlNamespace());
        } else {
            builder.append(codegenProperty.getBaseName());
        }
        builder.append("\"");
        return builder.toString();
    }

    public CharSequence getJacksonXmlElementWrapper(CodegenProperty codegenProperty) {
        final boolean isXmlWrapped = getBooleanValue(codegenProperty, CodegenConstants.IS_XML_WRAPPED_EXT_NAME);
        StringBuilder builder = new StringBuilder();
        builder.append("useWrapping = ");
        builder.append(isXmlWrapped);
        builder.append(", ");
        if (StringUtils.isNotBlank(codegenProperty.getXmlNamespace())) {
            builder.append("namespace=\"");
            builder.append(codegenProperty.getXmlNamespace());
            builder.append("\", ");
        }
        builder.append("localName = \"");
        if (StringUtils.isNotBlank(codegenProperty.getXmlName())) {
            builder.append(codegenProperty.getXmlNamespace());
        } else {
            builder.append(codegenProperty.getBaseName());
        }
        builder.append("\"");
        return builder.toString();
    }

    public CharSequence backslash() {
        return "\\";
    }
}
