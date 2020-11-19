package io.swagger.codegen.v3.generators.handlebars;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import javax.annotation.Nonnull;

public class PathToKotlinStringTemplateHelper implements Helper<String> {

    public static final String NAME = "pathToKotlinStringTemplate";

    @Nonnull
    private RemoveLeadingSlashHelper removeLeadingSlashHelper;

    public PathToKotlinStringTemplateHelper(@Nonnull RemoveLeadingSlashHelper removeLeadingSlashHelper) {
        this.removeLeadingSlashHelper = removeLeadingSlashHelper;
    }

    @Override
    public Object apply(String s, Options options) {
        String kotlinStringTemplatePath = s.replace("{", "$").replace("}", "");
        return removeLeadingSlashHelper.apply(kotlinStringTemplatePath, options);
    }
}
