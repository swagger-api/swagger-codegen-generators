package io.swagger.codegen.v3.generators.handlebars.lambda;

import com.github.jknack.handlebars.Lambda;

import java.io.IOException;

/**
 * Converts text in a fragment to uppercase.
 *
 * Register:
 * <pre>
 * additionalProperties.put("uppercase", new UppercaseLambda());
 * </pre>
 *
 * Use:
 * <pre>
 * {{#uppercase}}{{summary}}{{/uppercase}}
 * </pre>
 */
public class UppercaseLambda implements Lambda {

    @Override
    public Object apply(Object o, com.github.jknack.handlebars.Template template) throws IOException {
        String text = template.apply(o);
        if (text == null || text.length() == 0) {
            return text;
        }
        return text.toUpperCase();
    }
}
