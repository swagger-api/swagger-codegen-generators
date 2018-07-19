package io.swagger.codegen.v3.generators.handlebars;

import io.swagger.codegen.v3.VendorExtendable;

public class IsNotHelper extends NoneExtensionHelper {

    public static final String NAME = "isNot";


    @Override
    public String getPreffix() {
        return VendorExtendable.PREFIX_IS;
    }
}
