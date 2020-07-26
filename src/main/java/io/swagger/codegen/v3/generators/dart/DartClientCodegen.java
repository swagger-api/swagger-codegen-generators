package io.swagger.codegen.v3.generators.dart;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class DartClientCodegen extends DefaultCodegenConfig {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DartClientCodegen.class);

    public static final String BROWSER_CLIENT = "browserClient";
    public static final String PUB_NAME = "pubName";
    public static final String PUB_VERSION = "pubVersion";
    public static final String PUB_DESCRIPTION = "pubDescription";
    public static final String USE_ENUM_EXTENSION = "useEnumExtension";
    protected boolean browserClient = true;
    protected String pubName = "swagger";
    protected String pubVersion = "1.0.0";
    protected String pubDescription = "Swagger API client";
    protected boolean useEnumExtension = false;
    protected String sourceFolder = "";
    protected String apiDocPath = "docs/";
    protected String modelDocPath = "docs/";

    public DartClientCodegen() {
        super();

        // clear import mapping (from default generator) as dart does not use it
        // at the moment
        importMapping.clear();

        outputFolder = "generated-code/dart";
        modelTemplateFiles.put("model.mustache", ".dart");
        apiTemplateFiles.put("api.mustache", ".dart");
        apiTestTemplateFiles.put("api_test.mustache", ".dart");
        embeddedTemplateDir = templateDir = "dart";
        apiPackage = "lib.api";
        modelPackage = "lib.model";
        testPackage = "tests";
        modelDocTemplateFiles.put("object_doc.mustache", ".md");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");

        // default HIDE_GENERATION_TIMESTAMP to true
        hideGenerationTimestamp = Boolean.TRUE;

        setReservedWordsLowerCase(
            Arrays.asList(
                "abstract", "as", "assert", "async", "async*", "await",
                "break", "case", "catch", "class", "const", "continue",
                "default", "deferred", "do", "dynamic", "else", "enum",
                "export", "external", "extends", "factory", "false", "final",
                "finally", "for", "get", "if", "implements", "import", "in",
                "is", "library", "new", "null", "operator", "part", "rethrow",
                "return", "set", "static", "super", "switch", "sync*", "this",
                "throw", "true", "try", "typedef", "var", "void", "while",
                "int", "double", "with", "yield", "yield*" )
        );

        languageSpecificPrimitives = new HashSet<String>(
            Arrays.asList(
                "String",
                "bool",
                "int",
                "num",
                "double")
        );
        instantiationTypes.put("array", "List");
        instantiationTypes.put("map", "Map");

        typeMapping = new HashMap<>();
        typeMapping.put("Array", "List");
        typeMapping.put("array", "List");
        typeMapping.put("List", "List");
        typeMapping.put("boolean", "bool");
        typeMapping.put("string", "String");
        typeMapping.put("char", "String");
        typeMapping.put("int", "int");
        typeMapping.put("long", "int");
        typeMapping.put("short", "int");
        typeMapping.put("number", "num");
        typeMapping.put("float", "double");
        typeMapping.put("double", "double");
        typeMapping.put("BigDecimal", "double");
        typeMapping.put("object", "Object");
        typeMapping.put("integer", "int");
        typeMapping.put("Date", "DateTime");
        typeMapping.put("date", "DateTime");
        typeMapping.put("File", "MultipartFile");
        typeMapping.put("UUID", "String");
        //TODO binary should be mapped to byte array
        // mapped to String as a workaround
        typeMapping.put("binary", "String");
        typeMapping.put("ByteArray", "String");

        cliOptions.add(new CliOption(BROWSER_CLIENT, "Is the client browser based"));
        cliOptions.add(new CliOption(PUB_NAME, "Name in generated pubspec"));
        cliOptions.add(new CliOption(PUB_VERSION, "Version in generated pubspec"));
        cliOptions.add(new CliOption(PUB_DESCRIPTION, "Description in generated pubspec"));
        cliOptions.add(new CliOption(USE_ENUM_EXTENSION, "Allow the 'x-enum-values' extension for enums"));
        cliOptions.add(new CliOption(CodegenConstants.SOURCE_FOLDER, "source folder for generated code"));
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "dart";
    }

    @Override
    public String getHelp() {
        return "Generates a Dart client library.";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(BROWSER_CLIENT)) {
            this.setBrowserClient(convertPropertyToBooleanAndWriteBack(BROWSER_CLIENT));
        } else {
            //not set, use to be passed to template
            additionalProperties.put(BROWSER_CLIENT, browserClient);
        }

        if (additionalProperties.containsKey(PUB_NAME)) {
            this.setPubName((String) additionalProperties.get(PUB_NAME));
        } else {
            //not set, use to be passed to template
            additionalProperties.put(PUB_NAME, pubName);
        }

        if (additionalProperties.containsKey(PUB_VERSION)) {
            this.setPubVersion((String) additionalProperties.get(PUB_VERSION));
        } else {
            //not set, use to be passed to template
            additionalProperties.put(PUB_VERSION, pubVersion);
        }

        if (additionalProperties.containsKey(PUB_DESCRIPTION)) {
            this.setPubDescription((String) additionalProperties.get(PUB_DESCRIPTION));
        } else {
            //not set, use to be passed to template
            additionalProperties.put(PUB_DESCRIPTION, pubDescription);
        }

        if (additionalProperties.containsKey(USE_ENUM_EXTENSION)) {
            this.setUseEnumExtension(convertPropertyToBooleanAndWriteBack(USE_ENUM_EXTENSION));
        } else {
            // Not set, use to be passed to template.
            additionalProperties.put(USE_ENUM_EXTENSION, useEnumExtension);
        }

        if (additionalProperties.containsKey(CodegenConstants.SOURCE_FOLDER)) {
            this.setSourceFolder((String) additionalProperties.get(CodegenConstants.SOURCE_FOLDER));
        }

        // make api and model doc path available in mustache template
        additionalProperties.put("apiDocPath", apiDocPath);
        additionalProperties.put("modelDocPath", modelDocPath);

        final String libFolder = sourceFolder + File.separator + "lib";
        supportingFiles.add(new SupportingFile("pubspec.mustache", "", "pubspec.yaml"));
        supportingFiles.add(new SupportingFile("analysis_options.mustache", "", ".analysis_options"));
        supportingFiles.add(new SupportingFile("api_client.mustache", libFolder, "api_client.dart"));
        supportingFiles.add(new SupportingFile("api_exception.mustache", libFolder, "api_exception.dart"));
        supportingFiles.add(new SupportingFile("api_helper.mustache", libFolder, "api_helper.dart"));
        supportingFiles.add(new SupportingFile("apilib.mustache", libFolder, "api.dart"));

        final String authFolder = sourceFolder + File.separator + "lib" + File.separator + "auth";
        supportingFiles.add(new SupportingFile("auth/authentication.mustache", authFolder, "authentication.dart"));
        supportingFiles.add(new SupportingFile("auth/http_basic_auth.mustache", authFolder, "http_basic_auth.dart"));
        supportingFiles.add(new SupportingFile("auth/api_key_auth.mustache", authFolder, "api_key_auth.dart"));
        supportingFiles.add(new SupportingFile("auth/oauth.mustache", authFolder, "oauth.dart"));
        supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));
        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
    }

    @Override
    public String escapeReservedWord(String name) {
        return name + "_";
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + modelPackage().replace('.', File.separatorChar);
    }

    @Override
    public String apiTestFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + testPackage().replace('/', File.separatorChar);
    }

    @Override
    public String apiDocFileFolder() {
        return (outputFolder + "/" + apiDocPath).replace('/', File.separatorChar);
    }

    @Override
    public String modelDocFileFolder() {
        return (outputFolder + "/" + modelDocPath).replace('/', File.separatorChar);
    }

    @Override
    public String toVarName(String name) {
        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_"); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // if it's all uppper case, do nothing
        if (name.matches("^[A-Z_]*$")) {
            return name;
        }

        // camelize (lower first character) the variable name
        // pet_id => petId
        name = camelize(name, true);

        if (name.matches("^\\d.*")) {
            name = "n" + name;
        }

        if (isReservedWord(name)) {
            name = escapeReservedWord(name);
        }

        return name;
    }

    @Override
    public String toParamName(String name) {
        // should be the same as variable name
        return toVarName(name);
    }

    @Override
    public String toModelName(String name) {
        // model name cannot use reserved keyword, e.g. return
        if (isReservedWord(name)) {
            LOGGER.warn(name + " (reserved word) cannot be used as model filename. Renamed to " + camelize("model_" + name));
            name = "model_" + name; // e.g. return => ModelReturn (after camelize)
        }
        if (Character.isDigit(name.charAt(0))) {
            LOGGER.warn(name + " start with number. Renamed to " + camelize("model_" + name));
            name = "model_" + name; // e.g. return => ModelReturn (after camelize)
        }

        // camelize the model name
        // phone_number => PhoneNumber
        return camelize(name);
    }

    @Override
    public String toModelFilename(String name) {
        return underscore(toModelName(name));
    }

    @Override
    public String toApiFilename(String name) {
        return underscore(toApiName(name));
    }

    @Override
    public String toApiTestFilename(String name) {
        return underscore(toApiName(name)) + "_test";
    }

    @Override
    public String toDefaultValue(Schema schema) {
        if (schema instanceof MapSchema) {
            return "{}";
        } else if (schema instanceof ArraySchema) {
            return "[]";
        } else if (schema instanceof StringSchema) {
            if (schema.getDefault() != null) {
                String _default = schema.getDefault().toString();
                if (schema.getEnum() == null) {
                    return String.format("\"%s\"", escapeText(_default));
                } else {
                    // convert to enum var name later in postProcessModels
                    return _default;
                }
            }
        }
        return super.toDefaultValue(schema);
    }

    @Override
    public String getTypeDeclaration(Schema schema) {
        if (schema instanceof ArraySchema) {
            final ArraySchema ap = (ArraySchema) schema;
            final Schema inner = ap.getItems();
            return getSchemaType(schema) + "<" + getTypeDeclaration(inner) + ">";
        } else if (schema instanceof MapSchema) {
            final MapSchema mp = (MapSchema) schema;
            final Object innerObject = mp.getAdditionalProperties();
            if (innerObject instanceof Schema) {
                final Schema inner = (Schema) innerObject;
                return getSchemaType(schema) + "<String, " + getTypeDeclaration(inner) + ">";
            }
        }
        return super.getTypeDeclaration(schema);
    }

    @Override
    public String getSchemaType(Schema schema) {
        String swaggerType = super.getSchemaType(schema);
        String type = null;
        if (typeMapping.containsKey(swaggerType)) {
            type = typeMapping.get(swaggerType);
            if (languageSpecificPrimitives.contains(type)) {
                return type;
            }
        } else {
            type = swaggerType;
        }
        if (type == null) {
            type = "Object";
        }
        return toModelName(type);
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        return postProcessModelsEnum(objs);
    }

    @Override
    public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
        List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            Map<String, Object> modelMap = (Map<String, Object>) _mo;
            CodegenModel codegenModel = (CodegenModel) modelMap.get("model");
            boolean succes = buildEnumFromVendorExtension(codegenModel) || buildEnumFromValues(codegenModel);
            for (CodegenProperty var : codegenModel.vars) {
                updateCodegenPropertyEnum(var);
            }
        }
        return objs;
    }

    /**
     * Builds the set of enum members from their declared value.
     *
     * @return {@code true} if the enum was built
     */
    private boolean buildEnumFromValues(CodegenModel codegenModel) {
        if (!getBooleanValue(codegenModel, IS_ENUM_EXT_NAME) || codegenModel.allowableValues == null) {
            return false;
        }
        final Map<String, Object> allowableValues = codegenModel.allowableValues;
        final List<Object> values = (List<Object>) allowableValues.get("values");
        final List<Map<String, String>> enumVars = new ArrayList<>();
        final String commonPrefix = findCommonPrefixOfVars(values);
        int truncateIdx = commonPrefix.length();
        for (Object value : values) {
            Map<String, String> enumVar = new HashMap<String, String>();
            String enumName;
            if (truncateIdx == 0) {
                enumName = value.toString();
            } else {
                enumName = value.toString().substring(truncateIdx);
                if ("".equals(enumName)) {
                    enumName = value.toString();
                }
            }
            enumVar.put("name", toEnumVarName(enumName, codegenModel.dataType));
            enumVar.put("value", toEnumValue(value.toString(), codegenModel.dataType));
            enumVars.add(enumVar);
        }
        codegenModel.allowableValues.put("enumVars", enumVars);
        return true;
    }

    /**
     * Builds the set of enum members from a vendor extension.
     *
     * @return {@code true} if the enum was built
     */
    private boolean buildEnumFromVendorExtension(CodegenModel codegenModel) {
        if (!getBooleanValue(codegenModel, IS_ENUM_EXT_NAME) || codegenModel.allowableValues == null ||
            !useEnumExtension ||
            !codegenModel.vendorExtensions.containsKey("x-enum-values")) {
            return false;
        }
        Object extension = codegenModel.vendorExtensions.get("x-enum-values");
        List<Map<String, Object>> values =
            (List<Map<String, Object>>) extension;
        List<Map<String, String>> enumVars =
            new ArrayList<Map<String, String>>();
        for (Map<String, Object> value : values) {
            Map<String, String> enumVar = new HashMap<String, String>();
            String name = camelize((String) value.get("identifier"), true);
            if (isReservedWord(name)) {
                name = escapeReservedWord(name);
            }
            enumVar.put("name", name);
            enumVar.put("value", toEnumValue(
                value.get("numericValue").toString(), codegenModel.dataType));
            if (value.containsKey("description")) {
                enumVar.put("description", value.get("description").toString());
            }
            enumVars.add(enumVar);
        }
        codegenModel.allowableValues.put("enumVars", enumVars);
        return true;
    }

    @Override
    public String toEnumVarName(String value, String datatype) {
        if (value.length() == 0) {
            return "empty";
        }
        String var = value.replaceAll("\\W+", "_");
        if ("number".equalsIgnoreCase(datatype) ||
            "int".equalsIgnoreCase(datatype)) {
            var = "Number" + var;
        }
        return escapeReservedWord(camelize(var, true));
    }

    @Override
    public String toEnumValue(String value, String datatype) {
        if ("number".equalsIgnoreCase(datatype) ||
            "int".equalsIgnoreCase(datatype)) {
            return value;
        } else {
            return "\"" + escapeText(value) + "\"";
        }
    }

    @Override
    public String toOperationId(String operationId) {
        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            String newOperationId = camelize("call_" + operationId, true);
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to " + newOperationId);
            return newOperationId;
        }

        return camelize(operationId, true);
    }

    public void setBrowserClient(boolean browserClient) {
        this.browserClient = browserClient;
    }

    public void setPubName(String pubName) {
        this.pubName = pubName;
    }

    public void setPubVersion(String pubVersion) {
        this.pubVersion = pubVersion;
    }

    public void setPubDescription(String pubDescription) {
        this.pubDescription = pubDescription;
    }

    public void setUseEnumExtension(boolean useEnumExtension) {
        this.useEnumExtension = useEnumExtension;
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }


    @Override
    public String getDefaultTemplateDir() {
        return "dart";
    }
}
