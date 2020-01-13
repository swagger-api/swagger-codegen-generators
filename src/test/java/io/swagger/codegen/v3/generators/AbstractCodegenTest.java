package io.swagger.codegen.v3.generators;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCodegenTest {

    protected OpenAPI openAPI;
    protected Schema schema;

    @BeforeTest
    public void setUp() {
        this.openAPI = Mockito.mock(OpenAPI.class);
        final Components components = Mockito.mock(Components.class);
        final Map<String, Schema> schemas = Mockito.mock(HashMap.class);
        this.schema = Mockito.mock(Schema.class);

        Mockito.when(openAPI.getComponents()).thenReturn(components);
        Mockito.when(components.getSchemas()).thenReturn(schemas);
    }
}
