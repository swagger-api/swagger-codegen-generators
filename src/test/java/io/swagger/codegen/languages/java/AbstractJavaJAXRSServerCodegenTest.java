package io.swagger.codegen.languages.java;

import io.swagger.codegen.CodegenArgument;
import io.swagger.codegen.CodegenType;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.List;

public class AbstractJavaJAXRSServerCodegenTest {

    private final AbstractJavaJAXRSServerCodegen fakeJavaJAXRSCodegen = new AbstractJavaJAXRSServerCodegen() {
        @Override
        public String getArgumentsLocation() {
            return null;
        }

        @Override
        public CodegenType getTag() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public List<CodegenArgument> readLanguageArguments() {
            return null;
        }
    };

    @Test
    public void convertApiName() throws Exception {
        Assert.assertEquals(fakeJavaJAXRSCodegen.toApiName("name"), "NameApi");
        Assert.assertEquals(fakeJavaJAXRSCodegen.toApiName("$name"), "NameApi");
        Assert.assertEquals(fakeJavaJAXRSCodegen.toApiName("nam#e"), "NameApi");
        Assert.assertEquals(fakeJavaJAXRSCodegen.toApiName("$another-fake?"), "AnotherFakeApi");
        Assert.assertEquals(fakeJavaJAXRSCodegen.toApiName("fake_classname_tags 123#$%^"), "FakeClassnameTags123Api");
    }
}
