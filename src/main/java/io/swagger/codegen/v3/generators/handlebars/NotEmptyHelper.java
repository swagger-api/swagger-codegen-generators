package io.swagger.codegen.v3.generators.handlebars;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.Collection;

public class NotEmptyHelper implements Helper<Collection> {
    public static final String NAME = "notEmpty";

    @Override
    public Object apply(Collection collection, Options options) throws IOException {
        final Options.Buffer buffer = options.buffer();

        if (collection == null || collection.isEmpty()) {
            buffer.append(options.inverse());
        } else {
            buffer.append(options.fn());
        }
        return buffer;
    }
}
