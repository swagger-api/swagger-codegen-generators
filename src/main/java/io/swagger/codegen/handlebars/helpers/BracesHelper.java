package io.swagger.codegen.handlebars.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

public class BracesHelper implements Helper<String> {

    public static final String NAME = "braces";
    private static final String RIGHTT_ORIENTATION = "right";
    private static final String LEFT_CURLY_BRACES = "{{";
    private static final String RIGHT_CURLY_BRACES = "}}";

    @Override
    public Object apply(String orientation, Options options) {
        if (RIGHTT_ORIENTATION.equalsIgnoreCase(orientation)) {
            return RIGHT_CURLY_BRACES;
        }
        return LEFT_CURLY_BRACES;
    }


}
