package io.swagger.codegen.v3.generators.handlebars;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.util.Arrays;
import java.util.stream.Stream;

public class SwiftPackagePrefixHelper implements Helper<String> {

    private final String[] foundationTypes = {"String", "Date", "Int", "Bool"};

    public interface FuncPackage {
        String name();
    }

    public static final String NAME = "swiftPackagePrefix";
    private final FuncPackage packagefunc;

    public SwiftPackagePrefixHelper(FuncPackage packagefunc) {
        this.packagefunc = packagefunc;
    }

    // String & Date mocks are defined in SkyTestFoundation, we DO NOT append prefix package name for these types
    // i.e. if packageName == Selfcare AND s == Notification AND s is the name of type of Selfcare THEN we generate Selfcare.Notification
    @Override
    public Object apply(String s, Options options) {
        if (!isCollection(s)) {
            String packageName = (isFoundationType(s) ? "" : packagefunc.name() + ".");
            return packageName + s;
        } else {
            String typeOfCollection = typeOfCollection(s);
            String packageName = (isFoundationType(typeOfCollection) ? "" : packagefunc.name() + ".");
            return "[" + packageName + typeOfCollection + "]";
        }
    }

    private boolean isCollection(String s) {
        return s.contains("[");
    }

    private String typeOfCollection(String s) {
        return s.replace("[","").replace("]","");
    }

    private boolean isFoundationType(String s) {
        return Arrays.stream(foundationTypes).anyMatch(s::equals);
    }
}
