package io.swagger.codegen.v3.generators.handlebars.lambda;

import com.github.jknack.handlebars.Lambda;
import io.swagger.codegen.v3.CodegenConfig;

import java.io.IOException;

/**
 * Converts text in a fragment to lowercase.
 *
 * Register:
 * <pre>
 * additionalProperties.put("lowercase", new LowercaseLambda());
 * </pre>
 *
 * Use:
 * <pre>
 * {{#lowercase}}{{httpMethod}}{{/lowercase}}
 * </pre>
 */
public class LowercaseLambda implements Lambda {
    private CodegenConfig generator = null;

    public LowercaseLambda() {

    }

    public LowercaseLambda generator(final CodegenConfig generator) {
        this.generator = generator;
        return this;
    }

    @Override
    public Object apply(Object o, com.github.jknack.handlebars.Template template) throws IOException {
        String text = template.apply(o);
        if (text == null || text.length() == 0) {
            return text;
        }
        text = text.toLowerCase();
        if (generator != null && generator.reservedWords().contains(text)) {
            text = generator.escapeReservedWord(text);
        }

        return text;
    }
}
