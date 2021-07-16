package io.swagger.codegen.v3.generators.kotlin;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.Handlebars;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractKotlinCodegen extends DefaultCodegenConfig {

    private static Logger LOGGER = LoggerFactory.getLogger(AbstractKotlinCodegen.class);

    private Set<String> instantiationLibraryFunction;

    protected String artifactId;
    protected String artifactVersion = "1.0.0";
    protected String groupId = "io.swagger";
    protected String packageName;

    protected String sourceFolder = "src/main/kotlin";
    protected String sourceTestFolder = "src/test/kotlin";

    protected String apiDocPath = "docs/";
    protected String modelDocPath = "docs/";

    public AbstractKotlinCodegen() {
        super();
        supportsInheritance = true;

        languageSpecificPrimitives = new HashSet<>(Arrays.asList(
                "kotlin.Any",
                "kotlin.Byte",
                "kotlin.Short",
                "kotlin.Int",
                "kotlin.Long",
                "kotlin.Float",
                "kotlin.Double",
                "kotlin.Boolean",
                "kotlin.Char",
                "kotlin.String",
                "kotlin.Array",
                "kotlin.collections.List",
                "kotlin.collections.Map",
                "kotlin.collections.Set",
                "kotlin.ByteArray",
                "kotlin.CharArray",
                "kotlin.ShortArray",
                "kotlin.IntArray",
                "kotlin.LongArray",
                "kotlin.FloatArray",
                "kotlin.DoubleArray",
                "kotlin.BooleanArray"
        ));

        // this includes hard reserved words defined by https://github.com/JetBrains/kotlin/blob/master/core/descriptors/src/org/jetbrains/kotlin/renderer/KeywordStringsGenerated.java
        // as well as keywords from https://kotlinlang.org/docs/reference/keyword-reference.html
        reservedWords = new HashSet<>(Arrays.asList(
                "abstract",
                "actual",
                "annotation",
                "as",
                "break",
                "case",
                "catch",
                "class",
                "companion",
                "const",
                "constructor",
                "continue",
                "crossinline",
                "data",
                "delegate",
                "do",
                "else",
                "enum",
                "expect",
                "external",
                "false",
                "final",
                "finally",
                "for",
                "fun",
                "if",
                "in",
                "infix",
                "init",
                "inline",
                "inner",
                "interface",
                "internal",
                "is",
                "it",
                "lateinit",
                "lazy",
                "noinline",
                "null",
                "object",
                "open",
                "operator",
                "out",
                "override",
                "package",
                "private",
                "protected",
                "public",
                "reified",
                "return",
                "sealed",
                "super",
                "suspend",
                "tailrec",
                "this",
                "throw",
                "true",
                "try",
                "typealias",
                "typeof",
                "val",
                "var",
                "vararg",
                "when",
                "while"
        ));

        defaultIncludes = new HashSet<>(Arrays.asList(
                "kotlin.Byte",
                "kotlin.Short",
                "kotlin.Int",
                "kotlin.Long",
                "kotlin.Float",
                "kotlin.Double",
                "kotlin.Boolean",
                "kotlin.Char",
                "kotlin.Array",
                "kotlin.collections.List",
                "kotlin.collections.Set",
                "kotlin.collections.Map",
                "kotlin.ByteArray",
                "kotlin.CharArray",
                "kotlin.ShortArray",
                "kotlin.IntArray",
                "kotlin.LongArray",
                "kotlin.FloatArray",
                "kotlin.DoubleArray",
                "kotlin.BooleanArray"
        ));

        instantiationLibraryFunction = new HashSet<>(Arrays.asList(
                "arrayOf",
                "mapOf"
        ));

        typeMapping = new HashMap<>();
        typeMapping.put("string", "kotlin.String");
        typeMapping.put("boolean", "kotlin.Boolean");
        typeMapping.put("integer", "kotlin.Int");
        typeMapping.put("float", "kotlin.Float");
        typeMapping.put("long", "kotlin.Long");
        typeMapping.put("double", "kotlin.Double");
        typeMapping.put("number", "Long");
        typeMapping.put("date-time", "java.time.LocalDateTime");
        typeMapping.put("date", "java.time.LocalDateTime");
        typeMapping.put("file", "java.io.File");
        typeMapping.put("array", "kotlin.Array");
        typeMapping.put("list", "kotlin.Array");
        typeMapping.put("map", "kotlin.collections.Map");
        typeMapping.put("object", "kotlin.Any");
        typeMapping.put("binary", "kotlin.ByteArray");
        typeMapping.put("Date", "java.time.LocalDateTime");
        typeMapping.put("DateTime", "java.time.LocalDateTime");

        instantiationTypes.put("array", "arrayOf");
        instantiationTypes.put("list", "arrayOf");
        instantiationTypes.put("map", "mapOf");

        importMapping = new HashMap<>();
        importMapping.put("BigDecimal", "Long");
        importMapping.put("UUID", "java.util.UUID");
        importMapping.put("File", "java.io.File");
        importMapping.put("Date", "java.util.Date");
        importMapping.put("Timestamp", "java.sql.Timestamp");
        importMapping.put("DateTime", "java.time.LocalDateTime");
        importMapping.put("LocalDateTime", "java.time.LocalDateTime");
        importMapping.put("LocalDate", "java.time.LocalDate");
        importMapping.put("LocalTime", "java.time.LocalTime");

        specialCharReplacements.put(";", "Semicolon");
        specialCharReplacements.put(":", "Colon");

        cliOptions.clear();
        addOption(CodegenConstants.SOURCE_FOLDER, CodegenConstants.SOURCE_FOLDER_DESC, sourceFolder);
        addOption(CodegenConstants.PACKAGE_NAME, "Generated artifact package name (e.g. io.swagger).", packageName);
        addOption(CodegenConstants.GROUP_ID, "Generated artifact package's organization (i.e. maven groupId).", groupId);
        addOption(CodegenConstants.ARTIFACT_ID, "Generated artifact id (name of jar).", artifactId);
        addOption(CodegenConstants.ARTIFACT_VERSION, "Generated artifact's package version.", artifactVersion);
    }

    protected void addOption(String key, String description) {
        addOption(key, description, null);
    }

    protected void addOption(String key, String description, String defaultValue) {
        CliOption option = new CliOption(key, description);
        if (defaultValue != null) option.defaultValue(defaultValue);
        cliOptions.add(option);
    }

    protected void addSwitch(String key, String description, Boolean defaultValue) {
        CliOption option = CliOption.newBoolean(key, description);
        if (defaultValue != null) option.defaultValue(defaultValue.toString());
        cliOptions.add(option);
    }

    @Override
    public String getArgumentsLocation() {
        return "";
    }

    @Override
    public String apiDocFileFolder() {
        return (outputFolder + "/" + apiDocPath).replace('/', File.separatorChar);
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String apiTestFileFolder() {
        return outputFolder + File.separator + sourceTestFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }

    @Override
    public String escapeReservedWord(String name) {
        // TODO: Allow enum escaping as an option (e.g. backticks vs append/prepend underscore vs match model property escaping).
        return String.format("_%s", name);
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }

    protected void updateCodegenModelEnumVars(CodegenModel codegenModel) {
        super.updateCodegenModelEnumVars(codegenModel);
        for (CodegenProperty var : codegenModel.allVars) {
            updateCodegenPropertyEnum(var);
        }
    }

    /**
     * Output the type declaration of the property
     *
     * @param propertySchema Swagger Property object
     * @return a string presentation of the property type
     */
    @Override
    public String getTypeDeclaration(Schema propertySchema) {
        if (propertySchema instanceof ArraySchema) {
            return getArrayTypeDeclaration((ArraySchema) propertySchema);
        } else if (propertySchema instanceof MapSchema && hasSchemaProperties(propertySchema)) {
            Schema inner = (Schema) propertySchema.getAdditionalProperties();
            if (inner == null) {
                LOGGER.warn(propertySchema.getName() + "(map property) does not have a proper inner type defined");
                // TODO maybe better defaulting to StringProperty than returning null
                return null;
            }
            // Maps will be keyed only by primitive Kotlin string
            return String.format("%s<kotlin.String, %s>", getSchemaType(propertySchema), getTypeDeclaration(inner));
        } else if (propertySchema instanceof MapSchema && hasTrueAdditionalProperties(propertySchema)) {
            Schema inner = new ObjectSchema();
            return String.format("%s<kotlin.String, %s>", getSchemaType(propertySchema), getTypeDeclaration(inner));
        }
        return super.getTypeDeclaration(propertySchema);
    }

    @Override
    public String getAlias(String name) {
        if (typeAliases != null && typeAliases.containsKey(name)) {
            return typeAliases.get(name);
        }
        return name;
    }

    @Override
    public String getSchemaType(Schema schema) {
        String schemaType = super.getSchemaType(schema);

        // don't apply renaming on types from the typeMapping
        if (typeMapping.containsKey(schemaType)) {
            return toModelName(typeMapping.get(schemaType));
        }

        if (null == schemaType) {
            if (schema.getName() != null) {
                LOGGER.warn("No Type defined for Property " + schema.getName());
                return toModelName(schema.getName());
            } else {
                return toModelName("kotlin.Any");
            }
        }
        return toModelName(schemaType);
    }

    @Override
    public String modelDocFileFolder() {
        return (outputFolder + "/" + modelDocPath).replace('/', File.separatorChar);
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator + modelPackage().replace('.', File.separatorChar);
    }

    @Override
    public String modelTestFileFolder() {
        return outputFolder + File.separator + sourceTestFolder + File.separator + modelPackage().replace('.', File.separatorChar);
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        return postProcessModelsEnum(super.postProcessModels(objs));
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(CodegenConstants.SOURCE_FOLDER)) {
            this.setSourceFolder((String) additionalProperties.get(CodegenConstants.SOURCE_FOLDER));
        } else {
            additionalProperties.put(CodegenConstants.SOURCE_FOLDER, sourceFolder);
        }

        if (additionalProperties.containsKey(CodegenConstants.PACKAGE_NAME)) {
            this.setPackageName((String) additionalProperties.get(CodegenConstants.PACKAGE_NAME));
            if (!additionalProperties.containsKey(CodegenConstants.MODEL_PACKAGE))
                this.setModelPackage(packageName + ".models");
            if (!additionalProperties.containsKey(CodegenConstants.API_PACKAGE))
                this.setApiPackage(packageName + ".apis");
        } else {
            additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
        }

        if (additionalProperties.containsKey(CodegenConstants.ARTIFACT_ID)) {
            this.setArtifactId((String) additionalProperties.get(CodegenConstants.ARTIFACT_ID));
        } else {
            additionalProperties.put(CodegenConstants.ARTIFACT_ID, artifactId);
        }

        if (additionalProperties.containsKey(CodegenConstants.GROUP_ID)) {
            this.setGroupId((String) additionalProperties.get(CodegenConstants.GROUP_ID));
        } else {
            additionalProperties.put(CodegenConstants.GROUP_ID, groupId);
        }

        if (additionalProperties.containsKey(CodegenConstants.ARTIFACT_VERSION)) {
            this.setArtifactVersion((String) additionalProperties.get(CodegenConstants.ARTIFACT_VERSION));
        } else {
            additionalProperties.put(CodegenConstants.ARTIFACT_VERSION, artifactVersion);
        }

        if (additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            LOGGER.warn(CodegenConstants.INVOKER_PACKAGE + " with " + this.getName() + " generator is ignored. Use " + CodegenConstants.PACKAGE_NAME + ".");
        }

        additionalProperties.put(CodegenConstants.API_PACKAGE, apiPackage());
        additionalProperties.put(CodegenConstants.MODEL_PACKAGE, modelPackage());

        additionalProperties.put("apiDocPath", apiDocPath);
        additionalProperties.put("modelDocPath", modelDocPath);
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    /**
     * Return the sanitized variable name for enum
     *
     * @param value    enum variable name
     * @param datatype data type
     * @return the sanitized variable name for enum
     */
    @Override
    public String toEnumVarName(String value, String datatype) {
        String modified;
        if (value.length() == 0) {
            modified = "EMPTY";
        } else {
            modified = value;
            modified = sanitizeKotlinSpecificNames(modified);
        }

        modified = modified.toUpperCase();

        if (reservedWords.contains(modified) || Character.isDigit(modified.charAt(0))) {
            return escapeReservedWord(modified);
        }

        return modified;
    }

    @Override
    public String toEnumValue(String value, String datatype) {
        if (isPrimivite(datatype)) {
            return value;
        }
        return super.toEnumValue(value, datatype);
    }

    @Override
    public boolean isPrimivite(String datatype) {
        return "kotlin.Byte".equalsIgnoreCase(datatype)
            || "kotlin.Short".equalsIgnoreCase(datatype)
            || "kotlin.Int".equalsIgnoreCase(datatype)
            || "kotlin.Long".equalsIgnoreCase(datatype)
            || "kotlin.Float".equalsIgnoreCase(datatype)
            || "kotlin.Double".equalsIgnoreCase(datatype)
            || "kotlin.Boolean".equalsIgnoreCase(datatype);
    }

    @Override
    public String toInstantiationType(Schema p) {
        if (p instanceof ArraySchema) {
            return getArrayTypeDeclaration((ArraySchema) p);
        }
        return super.toInstantiationType(p);
    }

    /**
     * Return the fully-qualified "Model" name for import
     *
     * @param name the name of the "Model"
     * @return the fully-qualified "Model" name for import
     */
    @Override
    public String toModelImport(String name) {
        // toModelImport is called while processing operations, but DefaultCodegen doesn't
        // define imports correctly with fully qualified primitives and models as defined in this generator.
        if (needToImport(name)) {
            return super.toModelImport(name);
        }

        return name;
    }

    /**
     * Output the proper model name (capitalized).
     * In case the name belongs to the TypeSystem it won't be renamed.
     *
     * @param name the name of the model
     * @return capitalized model name
     */
    @Override
    public String toModelName(final String name) {
        // Allow for explicitly configured kotlin.* and java.* types
        if (name.startsWith("kotlin.") || name.startsWith("java.")) {
            return name;
        }

        // If importMapping contains name, assume this is a legitimate model name.
        if (importMapping.containsKey(name)) {
            return importMapping.get(name);
        }

        String modifiedName = name.replaceAll("\\.", "");

        if (isReservedWord(modifiedName)) {
            modifiedName = escapeReservedWord(modifiedName);
        }

        modifiedName = sanitizeKotlinSpecificNames(modifiedName);

        return titleCase(modifiedName);
    }

    @Override
    public String toVarName(String name) {
        return super.toVarName(sanitizeKotlinSpecificNames(name));
    }

    @Override
    public void addHandlebarHelpers(Handlebars handlebars) {
        super.addHandlebarHelpers(handlebars);
        handlebars.registerHelpers(StringHelpers.class);
    }

    /**
     * Provides a strongly typed declaration for simple arrays of some type and arrays of arrays of some type.
     *
     * @param arraySchema
     * @return
     */
    private String getArrayTypeDeclaration(ArraySchema arraySchema) {
        // TODO: collection type here should be fully qualified namespace to avoid model conflicts
        // This supports arrays of arrays.
        String arrayType = typeMapping.get("array");
        StringBuilder instantiationType = new StringBuilder(arrayType);
        Schema items = arraySchema.getItems();
        String nestedType = getTypeDeclaration(items);
        // TODO: We may want to differentiate here between generics and primitive arrays.
        instantiationType.append("<").append(nestedType).append(">");
        return instantiationType.toString();
    }

    /**
     * Sanitize against Kotlin specific naming conventions, which may differ from those required by {@link DefaultCodegenConfig#sanitizeName}.
     *
     * @param name string to be sanitize
     * @return sanitized string
     */
    private String sanitizeKotlinSpecificNames(final String name) {
        String word = removeNonNameElementToCamelCase(name);

        for (Map.Entry<String, String> specialCharacters : specialCharReplacements.entrySet()) {
            // Underscore is the only special character we'll allow
            if (!specialCharacters.getKey().equals("_")) {
                word = word.replaceAll("\\Q" + specialCharacters.getKey() + "\\E", specialCharacters.getValue());
            }
        }

        // Fallback, replace unknowns with underscore.
        word = word.replaceAll("\\W+", "_");
        if (word.matches("\\d.*")) {
            word = "_" + word;
        }

        // _, __, and ___ are reserved in Kotlin. Treat all names with only underscores consistently, regardless of count.
        if (word.matches("^_*$")) {
            word = word.replaceAll("\\Q_\\E", "Underscore");
        }

        return word;
    }

    private String titleCase(final String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    @Override
    protected boolean isReservedWord(String word) {
        // We want case-sensitive escaping, to avoid unnecessary backtick-escaping.
        return reservedWords.contains(word);
    }

    /**
     * Check the type to see if it needs import the library/module/package
     *
     * @param type name of the type
     * @return true if the library/module/package of the corresponding type needs to be imported
     */
    @Override
    protected boolean needToImport(String type) {
        // provides extra protection against improperly trying to import language primitives and java types
        boolean imports =
                !type.startsWith("kotlin.") &&
                        !type.startsWith("java.") &&
                        !defaultIncludes.contains(type) &&
                        !languageSpecificPrimitives.contains(type) &&
                        !instantiationLibraryFunction.contains(type);

        return imports;
    }
}
