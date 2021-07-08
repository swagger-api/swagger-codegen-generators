package io.swagger.codegen.v3.generators.handlebars;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

public class RemoveLeadingSlashHelper implements Helper<String> {

    public static final String NAME = "removeLeadingSlash";
    private static final String SLASH = "/";

    @Override
    public Object apply(String s, Options options) {
        if(s.startsWith(SLASH)) {
            return s.substring(1);
        } else {
            return s;
        }
    }
}
