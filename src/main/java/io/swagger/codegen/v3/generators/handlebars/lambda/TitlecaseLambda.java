package io.swagger.codegen.v3.generators.handlebars.lambda;

import com.github.jknack.handlebars.Lambda;

import java.io.IOException;

/**
 * Converts text in a fragment to title case.
 *
 * Register:
 * <pre>
 * additionalProperties.put("titlecase", new TitlecaseLambda());
 * </pre>
 *
 * Use:
 * <pre>
 * {{#titlecase}}{{classname}}{{/titlecase}}
 * </pre>
 */
public class TitlecaseLambda implements Lambda {
    private String delimiter;

    /**
     * Constructs a new instance of {@link io.swagger.codegen.mustache.TitlecaseLambda}, which will convert all text
     * in a space delimited string to title-case.
     */
    public TitlecaseLambda() {
        this(" ");
    }

    /**
     * Constructs a new instance of {@link io.swagger.codegen.mustache.TitlecaseLambda}, splitting on the specified
     * delimiter and converting each word to title-case.
     * <p>
     * NOTE: passing {@code null} results in a title-casing the first word only.
     *
     * @param delimiter Provided to allow an override for the default space delimiter.
     */
    public TitlecaseLambda(String delimiter) {
        this.delimiter = delimiter;
    }

    private String titleCase(final String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    @Override
    public Object apply(Object o, com.github.jknack.handlebars.Template template) throws IOException {
        String text = template.apply(o);
        if (text == null || text.length() == 0) {
            return text;
        }
        if (delimiter == null) {
            return titleCase(text);
        }

        // Split accepts regex. \Q and \E wrap the delimiter to create a literal regex,
        // so things like "." and "|" aren't treated as their regex equivalents.
        StringBuffer sb = new StringBuffer();
        String[] parts = text.split("\\Q" + delimiter + "\\E");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            sb.append(titleCase(part));
            if (i != parts.length - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }
}
