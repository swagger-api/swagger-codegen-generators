package io.swagger.codegen.v3.generators.handlebars;

import io.swagger.codegen.v3.VendorExtendable;

public class HasNotHelper extends NoneExtensionHelper {

    public static final String NAME = "hasNot";

    @Override
    public String getPreffix() {
        return VendorExtendable.PREFIX_HAS;
    }
}
