package io.swagger.codegen.v3.generators.python;

import io.swagger.codegen.v3.generators.GeneratorRunner;
import io.swagger.codegen.v3.service.GenerationRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Regression test for the Python generator `Map&lt;String, Enum&gt;` setter validation.
 *
 * <p>The {@code model.mustache} template used to emit
 * {@code set(x.keys()).issubset(allowed_values)} for properties declared as
 * {@code type: object} with {@code additionalProperties.enum}. Per OpenAPI 3.0
 * semantics, {@code additionalProperties} describes map values, so the enum
 * constraint applies to the values, not the keys. The fix validates
 * {@code set(x.values()).issubset(allowed_values)} and updates the error message
 * from "Invalid keys in" to "Invalid values in".
 */
public class PythonMapEnumValidationTest {

    @Test
    public void mapOfEnumSetterValidatesValuesNotKeys() throws Exception {
        final List<File> files = GeneratorRunner.runGenerator(
            "python",
            "3_0_0/map_of_inner_enum.yaml",
            GenerationRequest.CodegenVersion.V3,
            false,
            true,
            true,
            null,
            options -> {});

        Assert.assertFalse(files.isEmpty());

        final File modelFile = files.stream()
            .filter(f -> f.getName().equals("employee_with_map_of_enum.py"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("employee_with_map_of_enum.py was not generated"));

        final String content = new String(Files.readAllBytes(Paths.get(modelFile.toURI())));

        Assert.assertTrue(
            content.contains("set(project_role.values()).issubset(set(allowed_values))"),
            "Setter should validate map values (not keys) against the enum.\nGenerated:\n" + content);
        Assert.assertTrue(
            content.contains("Invalid values in `project_role`"),
            "Setter error message should reference 'Invalid values in'.\nGenerated:\n" + content);
        Assert.assertFalse(
            content.contains("set(project_role.keys()).issubset(set(allowed_values))"),
            "Setter must not validate map keys against the enum.\nGenerated:\n" + content);
        Assert.assertFalse(
            content.contains("Invalid keys in `project_role`"),
            "Setter error message must not reference 'Invalid keys in'.\nGenerated:\n" + content);
    }
}
