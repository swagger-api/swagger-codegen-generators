package io.swagger.codegen.v3.generators.options;

public class JavaInflectorServerOptionsProvider extends JavaOptionsProvider {
    @Override
    public String getLanguage() {
        return "inflector";
    }

    @Override
    public boolean isServer() {
        return true;
    }
}
