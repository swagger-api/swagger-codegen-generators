package io.swagger.codegen.v3.generators.handlebars.csharp;

public class CsharpHelper {

    public CharSequence backslash() {
        return "\\";
    }

    public String capitalize(String str){
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
