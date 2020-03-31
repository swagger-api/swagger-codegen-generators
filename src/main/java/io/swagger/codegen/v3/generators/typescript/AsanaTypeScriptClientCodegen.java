package io.swagger.codegen.v3.generators.typescript;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import io.swagger.codegen.v3.generators.handlebars.java.JavaHelper;

import java.lang.Character;


import java.io.IOException;

public class AsanaTypeScriptClientCodegen extends TypeScriptAngularClientCodegen {
    @Override
    public String getName() {
        return "asana-api-explorer";
    }

    @Override
    public void addHandlebarHelpers(Handlebars handlebars) {
        super.addHandlebarHelpers(handlebars);
        handlebars.registerHelpers(new JavaHelper());
        handlebars.registerHelper("eq", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                Object b = null;
                int index = 0;
                while (index < options.params.length) {
                    b = options.param(index, null);
                    boolean result = new EqualsBuilder().append(a, b).isEquals();
                    if (result) {
                        if (options.tagType == TagType.SECTION) {
                            return options.fn();
                        }
                        return options.hash("yes", true);
                    }
                    index++;
                }

                if (options.tagType == TagType.SECTION) {
                    return options.inverse();
                }
                return options.hash("no", false);
            }
        });
        handlebars.registerHelper("neq", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                Object b = null;
                int index = 0;
                while (index < options.params.length) {
                    b = options.param(index, null);
                    boolean result = new EqualsBuilder().append(a, b).isEquals();
                    if (result) {
                        if (options.tagType == TagType.SECTION) {
                            return options.inverse();
                        }
                        return options.hash("no", false);
                    }
                    index++;
                }

                if (options.tagType == TagType.SECTION) {
                    return options.fn();
                }
                return options.hash("yes", true);
            }
        });
        handlebars.registerHelper("toCamelCase", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                String s = (String)a;
                return camelize(s.toLowerCase());
            }
        });

        handlebars.registerHelper("toSnakeCase", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                String s = (String)a;
                return snakeCase(s.toLowerCase());
            }
        });

        handlebars.registerHelper("capitalize", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                String s = (String)a;
                return s.toUpperCase();
            }
        });

        handlebars.registerHelper("parsePath", new Helper<Object>() {
            @Override
            public Object apply(final Object a, final Options options) throws IOException {
                String s = (String)a;
                String[] split = s.split("/");
                for (int i = 0; i < split.length; i++) {
                    if (split[i].startsWith("$")) {
                        split[i] = "%s";
                    }
                }
        
                String processedPath = String.join("/", split);
                return processedPath;
            }
        });
    }

    @Override
    public String toApiFilename(String name) {
        if (name.length() == 0) {
            return "default";
        }
        return snakeCase(name) + "_base";
    }

    @Override
    public String snakeCase(String name) {
        StringBuilder sb = new StringBuilder();
        boolean middleOfWord = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (Character.isUpperCase(c)) {
                if (middleOfWord) {
                    sb.append("_");
                    middleOfWord = false;
                }
            } else {
                middleOfWord = true;
            }

            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    @Override
    public String apiPackage() {
        return "resources/gen";
    }
}
