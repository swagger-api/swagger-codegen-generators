package io.swagger.codegen.v3.generators.java;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SpringCodegenTest {

    @Test
    public void testJava11DateLibraryEnablesJava11Mode() {
        SpringCodegen codegen = new SpringCodegen();

        codegen.additionalProperties().put("dateLibrary", "java11");

        codegen.processOpts();

        Assert.assertTrue(codegen.java11);
        Assert.assertEquals(codegen.additionalProperties().get("javaVersion"), "11");
        Assert.assertEquals(codegen.additionalProperties().get("jdk11"), "true");
        Assert.assertEquals(codegen.additionalProperties().get("isJava8or11"), true);
        Assert.assertTrue(codegen.defaultInterfaces);
    }
}