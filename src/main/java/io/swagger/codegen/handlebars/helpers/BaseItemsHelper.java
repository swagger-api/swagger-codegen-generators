package io.swagger.codegen.handlebars.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Options.Buffer;

import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenProperty;

import java.io.IOException;

import static io.swagger.codegen.handlebars.helpers.ExtensionHelper.getBooleanValue;

public class BaseItemsHelper implements Helper<CodegenProperty> {
    public static final String NAME = "baseItems";

    @Override
    public Object apply(CodegenProperty codegenProperty, Options options) throws IOException {
        final Buffer buffer = options.buffer();

        if (codegenProperty == null) {
            buffer.append(options.inverse());
            return buffer;
        }

        CodegenProperty baseItems = getBaseItemsProperty(codegenProperty);

        if (baseItems != null) {
            buffer.append(options.fn(baseItems));
        } else {
            buffer.append(options.inverse());
        }
        return buffer;
    }

    public static CodegenProperty getBaseItemsProperty(CodegenProperty property) {
        CodegenProperty currentProperty = property;
        while (currentProperty != null
                && (getBooleanValue(currentProperty, CodegenConstants.IS_MAP_CONTAINER_EXT_NAME)
                || getBooleanValue(currentProperty, CodegenConstants.IS_LIST_CONTAINER_EXT_NAME))) {
            currentProperty = currentProperty.items;
        }
        return currentProperty;
    }
}
