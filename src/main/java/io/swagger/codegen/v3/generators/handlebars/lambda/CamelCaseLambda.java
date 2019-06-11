package io.swagger.codegen.v3.generators.handlebars.lambda;

import com.github.jknack.handlebars.Lambda;
import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;

import java.io.IOException;

/**
 * Converts text in a fragment to camelCase.
 *
 * Register:
 * <pre>
 * additionalProperties.put("camelcase", new CamelCaseLambda());
 * </pre>
 *
 * Use:
 * <pre>
 * {{#camelcase}}{{name}}{{/camelcase}}
 * </pre>
 */
public class CamelCaseLambda implements Lambda {

    private CodegenConfig generator = null;
    private Boolean escapeParam = false;

    public CamelCaseLambda() {

    }

    public CamelCaseLambda generator(final CodegenConfig generator) {
        this.generator = generator;
        return this;
    }

    public CamelCaseLambda escapeAsParamName(final Boolean escape) {
        this.escapeParam = escape;
        return this;
    }
    @Override
    public Object apply(Object o, com.github.jknack.handlebars.Template template) throws IOException {
        String executed = template.apply(o);
        String text = DefaultCodegenConfig.camelize(executed, true);
        if (generator != null) {
            text = ((DefaultCodegenConfig)generator).sanitizeName(text);
            if (generator.reservedWords().contains(text)) {
                // Escaping must be done *after* camelize, because generators may escape using characters removed by camelize function.
                text = generator.escapeReservedWord(text);
            }

            if (escapeParam) {
                // NOTE: many generators call escapeReservedWord in toParamName, but we can't assume that's always the case.
                //       Here, we'll have to accept that we may be duplicating some work.
                text = generator.toParamName(text);
            }
        }
        return text;
    }

}
