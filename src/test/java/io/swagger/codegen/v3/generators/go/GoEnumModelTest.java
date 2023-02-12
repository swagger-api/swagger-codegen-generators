package io.swagger.codegen.v3.generators.go;

import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GoEnumModelTest {

    DefaultCodegenConfig codegen = new GoClientCodegen();

    @Test
    public void enumVarNameStartsWithUpperCaseLetterTest() {
        final String value = "1";
        final String actual = codegen.toEnumVarName(value, "int32");

        final char[] firstSymbolArray = new char[1];
        actual.getChars(0, 1, firstSymbolArray, 0);

        final char firstSymbol = firstSymbolArray[0];
        Assert.assertTrue(Character.isLetter(firstSymbol), firstSymbol + " is not a letter");
        Assert.assertTrue(Character.isUpperCase(firstSymbol), firstSymbol + " is not an uppercase letter");
    }

    @Test
    public void enumVarNameStartsWithPrefixTest() {
        final String value = "1";
        Assert.assertEquals(
            codegen.toEnumVarName(value, "int32"),
            GoClientCodegen.ENUM_SYMBOL_PREFIX + value
        );
    }
}
