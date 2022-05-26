package io.swagger.codegen.v3.generators.python;

import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PythonClientCodegenTest extends AbstractCodegenTest {
    @Test
    public void testToModelName() {
        PythonClientCodegen pythonClientCodegen = new PythonClientCodegen();

        // no type - this is 'object' in Python
        Assert.assertEquals(pythonClientCodegen.toModelName(null), "object");
        // assume this is a model type - "null" is not special in Python
        Assert.assertEquals(pythonClientCodegen.toModelName("null"), "Null");
        // reserved word
        Assert.assertEquals(pythonClientCodegen.toModelName("return"), "ModelReturn");
        Assert.assertEquals(pythonClientCodegen.toModelName("None"), "ModelNone");
        // $
        Assert.assertEquals(pythonClientCodegen.toModelName("my$result"), "Myresult");
        // Starts with number
        Assert.assertEquals(pythonClientCodegen.toModelName("999Bad"), "Model999Bad");
        // Camel Case
        Assert.assertEquals(pythonClientCodegen.toModelName("camel_case"), "CamelCase");
    }

    @Test
    public void testToModelNamePrefixSuffix() {
        PythonClientCodegen pythonClientCodegen = new PythonClientCodegen();
        pythonClientCodegen.setModelNamePrefix("xprefixx");

        // Camel Case
        Assert.assertEquals(pythonClientCodegen.toModelName("camel_case"), "XprefixxCamelCase");

        pythonClientCodegen.setModelNamePrefix(null);
        pythonClientCodegen.setModelNameSuffix("xsuffixx");

        // Camel Case
        Assert.assertEquals(pythonClientCodegen.toModelName("camel_case"), "CamelCaseXsuffixx");
    }
}
