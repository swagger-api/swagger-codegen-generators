package io.swagger.codegen.v3.generators.handlebars;

import static io.swagger.codegen.v3.VendorExtendable.PREFIX_HAS;

public class HasHelper extends ExtensionHelper {

    public static final String NAME = "has";

    @Override
    public String getPreffix() {
        return PREFIX_HAS;
    }
}
