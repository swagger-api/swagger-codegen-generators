package io.swagger.codegen.v3.generators.handlebars;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

public class BracesHelper implements Helper<String> {

    public static final String NAME = "braces";
    private static final String RIGHTT_ORIENTATION = "right";
    private static final String LEFT_CURLY_BRACES = "{";
    private static final String RIGHT_CURLY_BRACES = "}";
    private static final int DEFAULT_BRACE_COUNT = 1;

    @Override
    public Object apply(String orientation, Options options) {
        int count = DEFAULT_BRACE_COUNT;

        Object[] params = options.params;
        if (params != null && params.length > 0) {
            Object param = params[0];
            if (param instanceof Number) {
                count = ((Number) param).intValue();
                if (count < DEFAULT_BRACE_COUNT) {
                    count = DEFAULT_BRACE_COUNT;
                }
            }
        }
        StringBuilder output = new StringBuilder();
        if (RIGHTT_ORIENTATION.equalsIgnoreCase(orientation)) {
            for (int i = 0; i < count; i++) {
                output.append(RIGHT_CURLY_BRACES);
            }
            return output.toString();
        }
        for (int i = 0; i < count; i++) {
            output.append(LEFT_CURLY_BRACES);
        }
        return output.toString();
    }


}
