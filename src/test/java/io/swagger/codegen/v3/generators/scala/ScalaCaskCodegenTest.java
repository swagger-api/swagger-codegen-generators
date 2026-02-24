package io.swagger.codegen.v3.generators.scala;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScalaCaskCodegenTest extends AbstractCodegenTest {
    private TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testPetstore() throws Exception {
        folder.create();
        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("scala-cask")
            .setSkipOverwrite(false)
            .setApiPackage("the.api.packg")
            .setModelNamePrefix("Model")
            .setInvokerPackage("in.voker.pckg")
            .setInputSpecURL("src/test/resources/3_0_0/petstore.yaml")
            .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();

        // the generator should simply complete w/o exception
        List<File> result = new DefaultGenerator().opts(clientOptInput).generate();
        Set<String> generatedNames = result.stream().map(f -> f.getName()).collect(Collectors.toSet());
        Set<String> expectedFiles = new HashSet<>();
        expectedFiles.add("DefaultService.scala");
        expectedFiles.add(".gitignore");
        expectedFiles.add("ServiceResponse.scala");
        expectedFiles.add("ModelCategory.scala");
        expectedFiles.add("App.scala");
        expectedFiles.add("ModelUser.scala");
        expectedFiles.add("DefaultRoutes.scala");
        expectedFiles.add("ModelPet.scala");
        expectedFiles.add("plugins.sbt");
        expectedFiles.add("build.properties");
        expectedFiles.add("PetRoutes.scala");
        expectedFiles.add(".scalafmt.conf");
        expectedFiles.add("UserService.scala");
        expectedFiles.add("build.sbt");
        expectedFiles.add("PetService.scala");
        expectedFiles.add("ModelOrder.scala");
        expectedFiles.add("UserRoutes.scala");
        expectedFiles.add("StoreRoutes.scala");
        expectedFiles.add("README.md");
        expectedFiles.add("ModelApiResponse.scala");
        expectedFiles.add("ModelTest.scala");
        expectedFiles.add("ModelTag.scala");
        expectedFiles.add("Modelpet_petId_body.scala");
        expectedFiles.add("StoreService.scala");
        expectedFiles.add("VERSION");
        expectedFiles.add("package.scala");
        Assert.assertEquals(generatedNames, expectedFiles);

    }

}