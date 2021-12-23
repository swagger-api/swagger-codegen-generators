package io.swagger.codegen.v3.generators.java;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import io.swagger.v3.oas.models.media.Schema;

public class JavaClientFqnCodegenTest extends AbstractCodegenTest {

    @Test
    public void testFullyQualifiedNames() throws Exception {
        final JavaClientCodegen codegen = new JavaClientCodegen();
        codegen.setUseFqn(true);
        codegen.setModelPackage("");
        codegen.processOpts();

        Assert.assertEquals(codegen.toModelFilename("io.swagger.test.MyModel"), "io/swagger/test/MyModel");
        Assert.assertEquals(codegen.toModelName("io.swagger.test.MyModel"), "MyModel");
        Assert.assertEquals(codegen.toVarName("io.swagger.test.MyModel"), "myModel");
        Assert.assertEquals(codegen.toModelDocFilename("io.swagger.test.MyModel"), "io/swagger/test/MyModel");
        
        Assert.assertEquals(codegen.additionalProperties().get(AbstractJavaCodegen.USE_FQN), "true");
        
        Schema schema = new Schema();
        schema.set$ref("io.swagger.test.MyModel");
        CodegenModel cm = codegen.fromModel("io.swagger.test.MyModel", schema, new HashMap());
        Assert.assertEquals(cm.getXmlNamespace(), "io.swagger.test"); //TODO: add namespace to CodegenModel
    }

    @Test
    public void testFullyQualifiedNamesWithCustomNamespace() throws Exception {
        final JavaClientCodegen codegen = new JavaClientCodegen();
        codegen.setUseFqn(true);
        codegen.setModelPackage("my.package");
        codegen.processOpts();

        Assert.assertEquals(codegen.toModelFilename("io.swagger.test.MyModel"), "my/package/io/swagger/test/MyModel");
        Assert.assertEquals(codegen.toModelName("io.swagger.test.MyModel"), "MyModel");
        Assert.assertEquals(codegen.toVarName("io.swagger.test.MyModel"), "myModel");
        Assert.assertEquals(codegen.toModelDocFilename("io.swagger.test.MyModel"), "my/package/io/swagger/test/MyModel");
        
        Assert.assertEquals(codegen.additionalProperties().get(AbstractJavaCodegen.USE_FQN), "true");
        
        Schema schema = new Schema();
        schema.set$ref("io.swagger.test.MyModel");
        CodegenModel cm = codegen.fromModel("io.swagger.test.MyModel", schema, new HashMap());
        Assert.assertEquals(cm.getXmlNamespace(), "my.package.io.swagger.test"); //TODO: add namespace to CodegenModel
    }

}
