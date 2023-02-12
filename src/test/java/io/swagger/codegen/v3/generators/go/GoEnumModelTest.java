package io.swagger.codegen.v3.generators.go;

import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GoEnumModelTest {

    DefaultCodegenConfig codegen = new GoClientCodegen();

    @Test
    public void enumOfIntegerStartsWithUpperCaseLetterTest() {
        final String value = "1";
        Assert.assertEquals(
            codegen.toEnumVarName(value, "int32"),
            GoClientCodegen.ENUM_SYMBOL_PREFIX + value
        );
    }
}
