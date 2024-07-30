package io.swagger.codegen.v3.generators.handlebars.csharp;

public class CsharpHelper {

    public CharSequence backslash() {
        return "\\";
    }

    public String capitalize(String string){
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}
