package io.swagger.codegen.v3.generators.handlebars;

import static io.swagger.codegen.v3.VendorExtendable.PREFIX_IS;

public class IsHelper extends ExtensionHelper {

    public static final String NAME = "is";

    @Override
    public String getPreffix() {
        return PREFIX_IS;
    }
}
