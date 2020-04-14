package io.swagger.codegen.v3.generators.handlebars.java;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import io.swagger.codegen.v3.CodegenContent;
import io.swagger.codegen.v3.CodegenParameter;

import java.io.IOException;
import java.util.function.Predicate;

public class HasParamWithExtensionHelper implements Helper<CodegenContent> {

    private Predicate<CodegenParameter> hasNotExtensionKey(Object extensionKey) { return param -> !param.vendorExtensions.containsKey(extensionKey);}

    @Override
    public Object apply(CodegenContent codegenContent, Options options) throws IOException {
        if (options.hash("vendorExtension") != null) {
            Object extensionKey = options.hash("vendorExtension");
            if (codegenContent.getParameters().stream().anyMatch(hasNotExtensionKey(extensionKey))) {
                return options.fn();
            }
        }

        return options.inverse();
    }
}