package io.swagger.codegen.v3.generators.options;

import java.util.Map;

public interface OptionsProvider {

    String getLanguage();
    Map<String, String> createOptions();
    boolean isServer();
}
