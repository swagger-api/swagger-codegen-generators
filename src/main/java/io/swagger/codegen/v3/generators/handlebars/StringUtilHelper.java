package io.swagger.codegen.v3.generators.handlebars;

import com.github.jknack.handlebars.Options;

public class StringUtilHelper {

    /**
     * concat parameters found to a given string.
     * @param element
     * @param options
     * @return
     */
    public String concat(String element, Options options) {
        final StringBuilder builder = new StringBuilder(element);
        if (options.params != null && options.params.length > 0) {
            for (Object param : options.params) {
                builder.append(param);
            }
        }
        return builder.toString();
    }

    public String toLowerCase(String string) {
        return string.toLowerCase();
    }

    public String toQuotedWord(Object object) {
        String string = object.toString();
        if (string.contains("\"")) {
            return string;
        } else {
            return "\"" + string + "\"";
        }
    }

    public String backSlash() {
        return "\\";
    }

}
