package io.swagger.codegen.v3.generators.handlebars.lambda;

import com.github.jknack.handlebars.Lambda;
import com.github.jknack.handlebars.Template;
import io.swagger.codegen.v3.CodegenConfig;

import java.io.IOException;

/**
 * @author Franz See <franz@see.net.ph> <https://see.net.ph>
 */
public class RemoveLineBreakLambda implements Lambda {

    private CodegenConfig generator = null;

    public RemoveLineBreakLambda() {

    }

    public RemoveLineBreakLambda generator(final CodegenConfig generator) {
        this.generator = generator;
        return this;
    }

    @Override
    public Object apply(Object o, Template template) throws IOException {
        String text = template.apply(o);
        if (text == null || text.length() == 0) {
            return text;
        }
        text = text.replaceAll("\\r|\\n", "");
        if (generator != null && generator.reservedWords().contains(text)) {
            text = generator.escapeReservedWord(text);
        }

        return text;
    }
}
